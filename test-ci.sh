#!/bin/bash
# CI Testing Script

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