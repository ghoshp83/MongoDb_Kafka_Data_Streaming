#!/usr/bin/env bash
docker exec mkds-kafka kafka-run-class kafka.tools.GetOffsetShell \
  --broker-list kafka:29092 \
  --topic orders-cdc
