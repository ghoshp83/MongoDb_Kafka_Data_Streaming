package com.enterprise.department.config;

/**
 * Implementation of the Config interface.
 */
public class AppConfig implements Config {
    private String mongoUri = "mongodb://localhost:27017";
    private String mongoDatabase = "mydb";
    private String mongoCollection = "mycollection";
    private int mongoBatchSize = 1000;
    private int mongoMaxPoolSize = 10;
    private int mongoMinPoolSize = 1;
    
    private String resumeTokenPath = "resume-token.json";
    private String resumeTokenBucket = "mongo-kafka-cdc-tokens";
    private String resumeTokenKey = "resume-token.json";
    
    private String kafkaBootstrapServers = "localhost:9092";
    private String kafkaTopic = "mongodb-data";
    private String kafkaClientId = "mongo-kafka-cdc";
    private int kafkaMaxRequestSize = 1048576;
    private String kafkaAcks = "all";
    private int kafkaBatchSize = 100;
    
    private boolean initialLoadEnabled = true;
    private boolean initialLoadForce = false;
    private int healthPort = 8080;
    private int retryMaxAttempts = 5;
    private int retryBackoffMs = 1000;
    private String awsRegion = "us-east-1";
    
    @Override
    public String getMongoUri() {
        return mongoUri;
    }
    
    public void setMongoUri(String mongoUri) {
        this.mongoUri = mongoUri;
    }
    
    @Override
    public String getMongoDatabase() {
        return mongoDatabase;
    }
    
    public void setMongoDatabase(String mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
    }
    
    @Override
    public String getMongoCollection() {
        return mongoCollection;
    }
    
    public void setMongoCollection(String mongoCollection) {
        this.mongoCollection = mongoCollection;
    }
    
    @Override
    public int getMongoBatchSize() {
        return mongoBatchSize;
    }
    
    public void setMongoBatchSize(int mongoBatchSize) {
        this.mongoBatchSize = mongoBatchSize;
    }
    
    @Override
    public int getMongoMaxPoolSize() {
        return mongoMaxPoolSize;
    }
    
    public void setMongoMaxPoolSize(int mongoMaxPoolSize) {
        this.mongoMaxPoolSize = mongoMaxPoolSize;
    }
    
    @Override
    public int getMongoMinPoolSize() {
        return mongoMinPoolSize;
    }
    
    public void setMongoMinPoolSize(int mongoMinPoolSize) {
        this.mongoMinPoolSize = mongoMinPoolSize;
    }
    
    @Override
    public String getResumeTokenPath() {
        return resumeTokenPath;
    }
    
    public void setResumeTokenPath(String resumeTokenPath) {
        this.resumeTokenPath = resumeTokenPath;
    }
    
    @Override
    public String getResumeTokenBucket() {
        return resumeTokenBucket;
    }
    
    public void setResumeTokenBucket(String resumeTokenBucket) {
        this.resumeTokenBucket = resumeTokenBucket;
    }
    
    @Override
    public String getResumeTokenKey() {
        return resumeTokenKey;
    }
    
    public void setResumeTokenKey(String resumeTokenKey) {
        this.resumeTokenKey = resumeTokenKey;
    }
    
    @Override
    public String getKafkaBootstrapServers() {
        return kafkaBootstrapServers;
    }
    
    public void setKafkaBootstrapServers(String kafkaBootstrapServers) {
        this.kafkaBootstrapServers = kafkaBootstrapServers;
    }
    
    @Override
    public String getKafkaTopic() {
        return kafkaTopic;
    }
    
    public void setKafkaTopic(String kafkaTopic) {
        this.kafkaTopic = kafkaTopic;
    }
    
    @Override
    public String getKafkaClientId() {
        return kafkaClientId;
    }
    
    public void setKafkaClientId(String kafkaClientId) {
        this.kafkaClientId = kafkaClientId;
    }
    
    @Override
    public int getKafkaMaxRequestSize() {
        return kafkaMaxRequestSize;
    }
    
    public void setKafkaMaxRequestSize(int kafkaMaxRequestSize) {
        this.kafkaMaxRequestSize = kafkaMaxRequestSize;
    }
    
    @Override
    public String getKafkaAcks() {
        return kafkaAcks;
    }
    
    public void setKafkaAcks(String kafkaAcks) {
        this.kafkaAcks = kafkaAcks;
    }
    
    @Override
    public int getKafkaBatchSize() {
        return kafkaBatchSize;
    }
    
    public void setKafkaBatchSize(int kafkaBatchSize) {
        this.kafkaBatchSize = kafkaBatchSize;
    }
    
    @Override
    public boolean isInitialLoadEnabled() {
        return initialLoadEnabled;
    }
    
    public void setInitialLoadEnabled(boolean initialLoadEnabled) {
        this.initialLoadEnabled = initialLoadEnabled;
    }
    
    @Override
    public boolean isInitialLoadForce() {
        return initialLoadForce;
    }
    
    public void setInitialLoadForce(boolean initialLoadForce) {
        this.initialLoadForce = initialLoadForce;
    }
    
    @Override
    public int getHealthPort() {
        return healthPort;
    }
    
    public void setHealthPort(int healthPort) {
        this.healthPort = healthPort;
    }
    
    @Override
    public int getRetryMaxAttempts() {
        return retryMaxAttempts;
    }
    
    public void setRetryMaxAttempts(int retryMaxAttempts) {
        this.retryMaxAttempts = retryMaxAttempts;
    }
    
    @Override
    public int getRetryBackoffMs() {
        return retryBackoffMs;
    }
    
    public void setRetryBackoffMs(int retryBackoffMs) {
        this.retryBackoffMs = retryBackoffMs;
    }
    
    @Override
    public String getAwsRegion() {
        return awsRegion;
    }
    
    public void setAwsRegion(String awsRegion) {
        this.awsRegion = awsRegion;
    }
}
