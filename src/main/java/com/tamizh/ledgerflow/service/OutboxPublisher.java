package com.tamizh.ledgerflow.service;

import com.tamizh.ledgerflow.persistence.OutboxEventRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxPublisher {
    private final OutboxEventRepository outbox;
    private final KafkaTemplate<String, String> kafka;

    public OutboxPublisher(OutboxEventRepository outbox, KafkaTemplate<String, String> kafka) {
        this.outbox = outbox;
        this.kafka = kafka;
    }

    @Scheduled(fixedDelayString = "${ledgerflow.outbox-interval-ms:500}")
    @Transactional
    public void publish() {
        var events = outbox.findByPublishedAtIsNullOrderByCreatedAtAsc(PageRequest.of(0, 100));
        for (var event : events) {
            kafka.send("payment.requested", event.getAggregateId().toString(), event.getPayload()).join();
            event.markPublished();
        }
    }
}
