#!/bin/bash
# Comprehensive CI test script
set -e

TEST_TYPE=${1:-"unit"}

echo "Running $TEST_TYPE tests..."

case $TEST_TYPE in
  "unit")
    echo "Running unit tests..."
    mvn clean test
    ;;
  "integration")
    echo "Running integration tests..."
    mvn clean verify -Pintegration-tests
    ;;
  "performance")
    echo "Running performance tests..."
    # Generate test data
    echo "Generating test data..."
    
    # Start performance monitoring
    START_TIME=$(date +%s)
    
    # Insert test documents
    for i in {1..1000}; do
      mongosh --eval "db.testdata.insertOne({id: $i, data: 'test-data-$i', timestamp: new Date()})"
    done
    
    # Wait for processing
    sleep 60
    
    END_TIME=$(date +%s)
    DURATION=$((END_TIME - START_TIME))
    
    echo "Performance test completed in ${DURATION} seconds"
    echo "{\"duration\": $DURATION, \"documents\": 1000, \"throughput\": $((1000/DURATION))}" > target/performance-results.json
    ;;
  "security")
    echo "Running security tests..."
    mvn org.owasp:dependency-check-maven:check
    ;;
  "local")
    echo "🧪 Testing CI Pipeline Locally"
    
    # Install act if not present
    if ! command -v act &> /dev/null; then
        echo "Installing act..."
        curl https://raw.githubusercontent.com/nektos/act/master/install.sh | sudo bash
    fi
    
    echo "📋 Available workflows:"
    act -l
    
    echo ""
    echo "🔄 Testing CI Pipeline..."
    act push -j test --artifact-server-path /tmp/artifacts
    
    echo ""
    echo "🐳 Testing Docker Build (dry run)..."
    act push -j build-and-push --dryrun
    
    echo ""
    echo "🔒 Testing Security Scan..."
    act push -j analyze --dryrun
    
    echo "✅ Local CI testing completed!"
    ;;
  *)
    echo "Unknown test type: $TEST_TYPE"
    echo "Available types: unit, integration, performance, security, local"
    exit 1
    ;;
esac

echo "$TEST_TYPE tests completed successfully."