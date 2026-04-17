#!/usr/bin/env bash
docker exec mkds-kafka kafka-console-consumer \
  --bootstrap-server kafka:29092 \
  --topic orders-cdc \
  --from-beginning \
  --max-messages 5 \
  2>/dev/null | jq -r '.order_id' | nl -ba
