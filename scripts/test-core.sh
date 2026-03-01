#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "$0")/.." && pwd)"
build_dir="$project_root/build/core-test"
mkdir -p "$build_dir"

if command -v javac >/dev/null 2>&1; then
  compiler=(javac)
else
  compiler=(java -m jdk.compiler/com.sun.tools.javac.Main)
fi

"${compiler[@]}" -d "$build_dir" \
  "$project_root"/src/main/java/com/tamizh/ledgerflow/core/*.java \
  "$project_root"/src/core-test/java/com/tamizh/ledgerflow/core/InMemoryPaymentProcessorTest.java

java -ea -cp "$build_dir" com.tamizh.ledgerflow.core.InMemoryPaymentProcessorTest
