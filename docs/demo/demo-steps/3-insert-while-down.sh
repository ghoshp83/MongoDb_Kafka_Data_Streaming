#!/usr/bin/env bash
docker exec mkds-mongo mongosh --quiet "mongodb://mongo:27017/?replicaSet=rs0" --eval '
  db = db.getSiblingDB("demo");
  db.orders.insertMany([
    {order_id: "C-200", amount: 99.99},
    {order_id: "C-201", amount:  1.00},
    {order_id: "C-202", amount: 250.00}
  ]);
  print("inserted 3 more orders while streamer is DOWN (now " + db.orders.countDocuments() + " in Mongo)");
'
