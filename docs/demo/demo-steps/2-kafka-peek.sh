#!/usr/bin/env bash
docker exec mkds-kafka kafka-console-consumer \
  --bootstrap-server kafka:29092 \
  --topic orders-cdc \
  --from-beginning \
  --max-messages "${1:-2}" \
  2>/dev/null | jq -c '{order_id, amount, _source}'
