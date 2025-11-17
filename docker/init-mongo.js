// MongoDB initialization script for MongoDB Kafka Data Streaming
// This script runs when MongoDB container starts for the first time

print('=== MongoDB Kafka Streaming - Database Initialization ===');

// Switch to the application database
db = db.getSiblingDB('mydb');

// Create the main collection for streaming
db.createCollection('mycollection');
print('✓ Created collection: mycollection');

// Create indexes for optimal performance
db.mycollection.createIndex({ "name": 1 });
db.mycollection.createIndex({ "category": 1 });
db.mycollection.createIndex({ "created_at": 1 });
db.mycollection.createIndex({ "price": 1 });
db.mycollection.createIndex({ "tags": 1 });
db.mycollection.createIndex({ "_id": 1, "created_at": 1 }); // Compound index for CDC

print('✓ Created indexes: name, category, created_at, price, tags, _id+created_at');

// Create additional collections for testing (optional)
db.createCollection('test_collection');
db.createCollection('metrics');

print('✓ Created additional collections: test_collection, metrics');

// Set up database-level configuration
db.runCommand({
  "collMod": "mycollection",
  "changeStreamPreAndPostImages": { "enabled": true }
});

print('✓ Enabled change stream pre and post images for CDC');

print('=== MongoDB initialization completed successfully! ===');
print('Database: mydb');
print('Main Collection: mycollection');
print('Ready for Change Data Capture (CDC)');
print('');
print('Note: Sample data and replica set will be initialized by mongodb-setup service');
print('      This is required for Change Streams to work properly.');