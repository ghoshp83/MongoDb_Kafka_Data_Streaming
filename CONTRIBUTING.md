# Contributing to MongoDB Kafka Data Streaming

🎉 Thank you for considering contributing to MongoDB Kafka Data Streaming! 

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
- [Development Setup](#development-setup)
- [Pull Request Process](#pull-request-process)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Documentation](#documentation)

## Code of Conduct

This project adheres to a code of conduct. By participating, you are expected to uphold this code. Please report unacceptable behavior to the project maintainers.

## How Can I Contribute?

### 🐛 Reporting Bugs

Before creating bug reports, please check the existing issues to avoid duplicates. When creating a bug report, include:

- **Clear title and description**
- **Steps to reproduce** the issue
- **Expected vs actual behavior**
- **Environment details** (Java version, OS, etc.)
- **Log files** if applicable

### 💡 Suggesting Enhancements

Enhancement suggestions are welcome! Please provide:

- **Clear title and description**
- **Use case** for the enhancement
- **Proposed implementation** (if you have ideas)
- **Alternative solutions** considered

### 🔧 Code Contributions

1. **Fork** the repository
2. **Create** a feature branch from `main`
3. **Make** your changes
4. **Add** tests for new functionality
5. **Ensure** all tests pass
6. **Submit** a pull request

## Development Setup

### Prerequisites

- Java 11+
- Maven 3.6+
- MongoDB 5.0+ (for integration tests)
- Apache Kafka 2.0+ (for integration tests)
- Docker (optional, for containerized testing)

### Setup Steps

```bash
# 1. Fork and clone your fork
git clone https://github.com/YOUR_USERNAME/MongoDb_Kafka_Data_Streaming.git
cd MongoDb_Kafka_Data_Streaming

# 2. Add upstream remote
git remote add upstream https://github.com/ghoshp83/MongoDb_Kafka_Data_Streaming.git

# 3. Install dependencies
mvn clean install

# 4. Run tests
mvn test

# 5. Run integration tests (requires MongoDB and Kafka)
mvn verify
```

### IDE Setup

#### IntelliJ IDEA
1. Import as Maven project
2. Enable annotation processing
3. Install CheckStyle plugin (optional)

#### Eclipse
1. Import as existing Maven project
2. Enable annotation processing in project properties

## Pull Request Process

### Before Submitting

- [ ] Code follows project coding standards
- [ ] All tests pass (`mvn test`)
- [ ] New functionality includes tests
- [ ] Documentation updated (if applicable)
- [ ] Commit messages follow conventional format

### PR Guidelines

1. **Title**: Use clear, descriptive titles
2. **Description**: Explain what and why, not just how
3. **Link Issues**: Reference related issues with `Fixes #123`
4. **Small PRs**: Keep changes focused and reviewable
5. **Tests**: Include tests for new features/bug fixes

### Commit Message Format

```
type(scope): brief description

Detailed explanation of changes (if needed)

Fixes #123
```

**Types**: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`

**Examples**:
```
feat(kafka): add batch size configuration
fix(mongo): handle connection timeout gracefully
docs(readme): update installation instructions
```

## Coding Standards

### Java Style Guide

- **Indentation**: 4 spaces (no tabs)
- **Line Length**: 120 characters max
- **Naming**: 
  - Classes: `PascalCase`
  - Methods/Variables: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
- **Braces**: Opening brace on same line
- **Imports**: No wildcard imports

### Code Quality

- **Null Safety**: Use `Optional` where appropriate
- **Exception Handling**: Catch specific exceptions
- **Logging**: Use SLF4J with appropriate levels
- **Comments**: Explain why, not what
- **Method Length**: Keep methods focused and short

### Example Code Style

```java
public class DocumentProcessor {
    private static final Logger logger = LoggerFactory.getLogger(DocumentProcessor.class);
    private static final int DEFAULT_BATCH_SIZE = 1000;
    
    private final KafkaProducer<String, String> producer;
    private final int batchSize;
    
    public DocumentProcessor(KafkaProducer<String, String> producer, int batchSize) {
        this.producer = Objects.requireNonNull(producer, "Producer cannot be null");
        this.batchSize = batchSize > 0 ? batchSize : DEFAULT_BATCH_SIZE;
    }
    
    public void processDocument(Document document) {
        try {
            String json = convertToJson(document);
            producer.send(new ProducerRecord<>("topic", json));
            logger.debug("Processed document: {}", document.get("_id"));
        } catch (Exception e) {
            logger.error("Failed to process document: {}", document.get("_id"), e);
            throw new ProcessingException("Document processing failed", e);
        }
    }
}
```

## Testing Guidelines

### Test Structure

```
src/test/java/
├── unit/           # Unit tests (fast, isolated)
├── integration/    # Integration tests (slower, external deps)
└── e2e/           # End-to-end tests (full pipeline)
```

### Test Naming

```java
// Pattern: should_ExpectedBehavior_When_StateUnderTest
@Test
public void should_ProcessDocument_When_ValidDocumentProvided() {
    // Arrange
    Document document = new Document("_id", "test123");
    
    // Act
    processor.processDocument(document);
    
    // Assert
    verify(kafkaProducer).send(any(ProducerRecord.class));
}
```

### Test Categories

- **Unit Tests**: Test individual classes in isolation
- **Integration Tests**: Test component interactions
- **End-to-End Tests**: Test complete workflows

### Mocking Guidelines

- Use Mockito for mocking dependencies
- Mock external services (MongoDB, Kafka)
- Don't mock value objects or simple data structures

## Documentation

### Code Documentation

- **Public APIs**: Comprehensive JavaDoc
- **Complex Logic**: Inline comments explaining why
- **Configuration**: Document all configuration options

### README Updates

When adding features, update:
- Feature list
- Configuration examples
- Usage examples
- Architecture diagrams (if applicable)

### Documentation Files

- `README.md`: Main project documentation
- `docs/`: Detailed documentation
- `CHANGELOG.md`: Version history
- `API.md`: API documentation (if applicable)

## Getting Help

- **Questions**: Open a [Discussion](https://github.com/ghoshp83/MongoDb_Kafka_Data_Streaming/discussions)
- **Bugs**: Create an [Issue](https://github.com/ghoshp83/MongoDb_Kafka_Data_Streaming/issues)
- **Chat**: Join our community discussions

## Recognition

Contributors will be recognized in:
- `CONTRIBUTORS.md` file
- Release notes for significant contributions
- GitHub contributor graphs

Thank you for contributing! 🚀