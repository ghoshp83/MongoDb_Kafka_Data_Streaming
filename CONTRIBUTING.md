# 🤝 Contributing to MongoDB Kafka Data Streaming

Thank you for your interest in contributing! This document provides guidelines for contributing to this project.

## 🚀 Quick Start for Contributors

### Prerequisites
- Java 11+
- Maven 3.6+
- Docker & Docker Compose
- Git

### Development Setup
```bash
# 1. Fork and clone the repository
git clone https://github.com/YOUR_USERNAME/MongoDb_Kafka_Data_Streaming.git
cd MongoDb_Kafka_Data_Streaming

# 2. Install dependencies
mvn clean install

# 3. Start development environment
cd docker
docker compose up -d mongodb kafka zookeeper

# 4. Run tests
mvn test

# 5. Run the application
java -jar target/enterprise-data-ingest-0.1-jar-with-dependencies.jar
```

## 📋 How to Contribute

### 1. Reporting Issues
- Use the [issue templates](.github/ISSUE_TEMPLATE/)
- Provide clear reproduction steps
- Include environment details
- Add relevant logs or screenshots

### 2. Suggesting Features
- Check existing issues first
- Use the feature request template
- Explain the use case and benefits
- Consider implementation complexity

### 3. Code Contributions

#### Branch Naming Convention
```
feature/add-new-connector
bugfix/fix-memory-leak
docs/update-readme
refactor/improve-error-handling
```

#### Commit Message Format
```
type(scope): description

feat(kafka): add batch processing support
fix(mongo): resolve connection timeout issue
docs(readme): update installation instructions
test(integration): add end-to-end test cases
```

## 🧪 Development Guidelines

### Code Style
- Follow Java naming conventions
- Use meaningful variable and method names
- Add JavaDoc for public methods
- Keep methods under 50 lines
- Maximum line length: 120 characters

### Testing Requirements
- Unit tests for all new features
- Integration tests for external dependencies
- Minimum 80% code coverage
- All tests must pass before PR

### Code Review Process
1. Create feature branch from `develop`
2. Implement changes with tests
3. Update documentation if needed
4. Submit pull request to `develop`
5. Address review feedback
6. Squash commits before merge

## 🏗️ Architecture Guidelines

### Adding New Features
1. **Interfaces First**: Define contracts before implementation
2. **Dependency Injection**: Use constructor injection
3. **Error Handling**: Implement proper exception handling
4. **Logging**: Add appropriate log levels
5. **Configuration**: Make features configurable

### Design Patterns Used
- **Strategy Pattern**: For document processing
- **Observer Pattern**: For event handling
- **Factory Pattern**: For component creation
- **Circuit Breaker**: For resilience

## 📚 Documentation Standards

### Code Documentation
```java
/**
 * Processes MongoDB documents and sends them to Kafka.
 * 
 * @param document The MongoDB document to process
 * @param operation The operation type (insert, update, delete)
 * @param source The source of the document (initial_load, change_stream)
 * @throws DocumentProcessingException if processing fails
 */
public void processDocument(Document document, String operation, String source) {
    // Implementation
}
```

### README Updates
- Update feature list for new capabilities
- Add configuration examples
- Include troubleshooting steps
- Update performance metrics

## 🔍 Testing Strategy

### Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class DocumentProcessorTest {
    @Mock
    private KafkaProducer<String, String> kafkaProducer;
    
    @Test
    void shouldProcessDocumentSuccessfully() {
        // Test implementation
    }
}
```

### Integration Tests
```java
@Testcontainers
class MongoKafkaIntegrationTest {
    @Container
    static MongoDBContainer mongodb = new MongoDBContainer("mongo:5.0");
    
    @Test
    void shouldStreamDocumentsFromMongoToKafka() {
        // Integration test implementation
    }
}
```

## 🚀 Release Process

### Version Numbering
- **Major**: Breaking changes (1.0.0 → 2.0.0)
- **Minor**: New features (1.0.0 → 1.1.0)
- **Patch**: Bug fixes (1.0.0 → 1.0.1)

### Release Checklist
- [ ] All tests passing
- [ ] Documentation updated
- [ ] Performance benchmarks verified
- [ ] Security scan clean
- [ ] Docker image built and tested
- [ ] Release notes prepared

## 🏆 Recognition

### Contributors
We recognize contributors in:
- README.md contributors section
- Release notes
- GitHub releases
- Project documentation

### Contribution Types
- 💻 Code contributions
- 📖 Documentation improvements
- 🐛 Bug reports and fixes
- 💡 Feature suggestions
- 🎨 UI/UX improvements
- 🔧 DevOps and tooling

## 📞 Getting Help

### Communication Channels
- **GitHub Issues**: Bug reports and feature requests
- **GitHub Discussions**: General questions and ideas
- **Code Reviews**: Technical discussions on PRs

### Response Times
- **Issues**: Within 48 hours
- **Pull Requests**: Within 72 hours
- **Security Issues**: Within 24 hours

## 📄 License

By contributing, you agree that your contributions will be licensed under the MIT License.

---

**Happy Contributing! 🎉**

Your contributions help make this project better for everyone. Thank you for being part of the community!