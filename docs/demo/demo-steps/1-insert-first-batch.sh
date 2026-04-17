#!/usr/bin/env bash
docker exec mkds-mongo mongosh --quiet "mongodb://mongo:27017/?replicaSet=rs0" --eval '
  db = db.getSiblingDB("demo");
  db.orders.insertMany([
    {order_id: "A-001", amount: 42.50},
    {order_id: "A-002", amount: 19.99}
  ]);
  print("inserted " + db.orders.countDocuments() + " orders into MongoDB");
'
