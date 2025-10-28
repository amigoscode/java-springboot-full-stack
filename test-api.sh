#!/bin/bash

# Test script for Product Image Management API
# This script demonstrates how to use the new single-request endpoint

API_BASE="http://localhost:8080/api/v1/products"

echo "🧪 Testing Product Image Management API"
echo "======================================="

# Test 1: Create a product with image (single request)
echo ""
echo "📝 Test 1: Creating product with image (single request)"
echo "------------------------------------------------------"

# Create a simple test image (1x1 pixel PNG)
echo "Creating test image..."
cat > test_image.png << 'EOF'
iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==
EOF

# Decode base64 to create actual PNG file
echo "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==" | base64 -d > test_image.png

echo "✅ Test image created: test_image.png"

# Test the single-request endpoint
echo "🚀 Testing single-request product creation with image..."
response=$(curl -s -X POST "$API_BASE" \
  -F "name=Test Product with Image" \
  -F "description=This product was created using the new single-request endpoint" \
  -F "price=99.99" \
  -F "stockLevel=50" \
  -F "image=@test_image.png")

if [ $? -eq 0 ]; then
    echo "✅ Product created successfully!"
    echo "📋 Response: $response"
    
    # Extract product ID from response
    product_id=$(echo "$response" | tr -d '"')
    echo "🆔 Product ID: $product_id"
    
    # Test 2: Verify the product was created
    echo ""
    echo "📝 Test 2: Verifying product creation"
    echo "------------------------------------"
    
    product_info=$(curl -s "$API_BASE/$product_id")
    if [ $? -eq 0 ]; then
        echo "✅ Product retrieved successfully!"
        echo "📋 Product Info: $product_info"
    else
        echo "❌ Failed to retrieve product"
    fi
    
    # Test 3: Download the image
    echo ""
    echo "📝 Test 3: Downloading product image"
    echo "------------------------------------"
    
    curl -s -o downloaded_image.png "$API_BASE/$product_id/image"
    if [ $? -eq 0 ]; then
        echo "✅ Image downloaded successfully!"
        echo "📁 Saved as: downloaded_image.png"
        
        # Compare file sizes
        original_size=$(wc -c < test_image.png)
        downloaded_size=$(wc -c < downloaded_image.png)
        
        if [ "$original_size" -eq "$downloaded_size" ]; then
            echo "✅ Image integrity verified (sizes match)"
        else
            echo "⚠️  Image sizes don't match (original: $original_size, downloaded: $downloaded_size)"
        fi
    else
        echo "❌ Failed to download image"
    fi
    
else
    echo "❌ Failed to create product"
    echo "📋 Response: $response"
fi

# Test 4: Create product without image
echo ""
echo "📝 Test 4: Creating product without image"
echo "----------------------------------------"

response2=$(curl -s -X POST "$API_BASE" \
  -F "name=Test Product without Image" \
  -F "description=This product was created without an image" \
  -F "price=49.99" \
  -F "stockLevel=25")

if [ $? -eq 0 ]; then
    echo "✅ Product created successfully without image!"
    echo "📋 Response: $response2"
else
    echo "❌ Failed to create product without image"
    echo "📋 Response: $response2"
fi

# Test 5: List all products
echo ""
echo "📝 Test 5: Listing all products"
echo "------------------------------"

products=$(curl -s "$API_BASE")
if [ $? -eq 0 ]; then
    echo "✅ Products retrieved successfully!"
    echo "📋 Products: $products"
else
    echo "❌ Failed to retrieve products"
fi

# Cleanup
echo ""
echo "🧹 Cleaning up test files..."
rm -f test_image.png downloaded_image.png

echo ""
echo "🎉 Testing completed!"
echo "===================="
echo ""
echo "💡 Key Benefits of the New Single-Request Endpoint:"
echo "  • No more 500 errors from separate requests"
echo "  • Atomic operation (product + image in one request)"
echo "  • Better error handling and validation"
echo "  • Improved user experience"
echo ""
echo "🔗 API Endpoints:"
echo "  • POST /api/v1/products (multipart/form-data) - Create with image"
echo "  • POST /api/v1/products (application/json) - Create without image"
echo "  • GET /api/v1/products/{id}/image - Download image"
echo ""
