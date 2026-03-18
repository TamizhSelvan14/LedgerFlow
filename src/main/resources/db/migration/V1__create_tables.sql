create table payments (
    id uuid primary key,
    idempotency_key varchar(200) not null unique,
    amount numeric(19,4) not null check (amount > 0),
    currency varchar(3) not null,
    status varchar(32) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint not null default 0
);

create table outbox_events (
    id uuid primary key,
    aggregate_id uuid not null,
    event_type varchar(100) not null,
    payload text not null,
    created_at timestamptz not null,
    published_at timestamptz
);

create index idx_outbox_unpublished on outbox_events (created_at) where published_at is null;
