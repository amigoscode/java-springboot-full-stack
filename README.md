# Product Image Management with AWS S3

This Spring Boot application provides a complete product management system with image upload and download capabilities using Amazon S3 for storage.

## Features

- **Product Management**: Create, read, update, and delete products
- **Image Upload**: Upload product images to Amazon S3
- **Image Download**: Download and display product images from S3
- **Web Interface**: Simple HTML interface for testing functionality
- **REST API**: Complete REST API for product and image management

## Prerequisites

- Java 21+
- Maven 3.6+
- PostgreSQL database
- AWS Account with S3 access
- AWS CLI configured (optional, for local development)

## Setup Instructions

### 1. Quick Local Setup (Recommended)

For local development and testing, we use MinIO (S3-compatible storage) and PostgreSQL via Docker:

```bash
# Run the setup script
./setup-local.sh

# Or manually start services
docker compose -f docker-compose-services.yml up -d
```

This will start:
- **PostgreSQL** on port 5333
- **MinIO** (S3-compatible) on port 9000 (API) and 9001 (Console)
- **MinIO bucket initialization** (creates `product-images` bucket)

### 2. Manual Database Setup

**Option A: Use Docker (Recommended)**
```bash
docker compose -f docker-compose-services.yml up -d
```

**Option B: Local PostgreSQL**
- Install PostgreSQL
- Create a database named `jfs`
- Update `application.properties` with your database credentials

### 3. Local Development Configuration

The application is pre-configured for local development with MinIO:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5333/jfs
spring.datasource.username=amigoscode
spring.datasource.password=password

# AWS S3 Configuration (MinIO for local development)
aws.region=us-east-1
aws.s3.bucket=product-images
aws.s3.endpoint-override=http://localhost:9000
aws.s3.path-style-enabled=true
aws.access-key-id=minioadmin
aws.secret-access-key=minioadmin123
```

### 4. Production AWS S3 Configuration

For production deployment with real AWS S3:

#### Create S3 Bucket
1. Log into AWS Console
2. Go to S3 service
3. Create a new bucket (e.g., `your-product-images-bucket`)
4. Note the bucket name for configuration

#### Configure AWS Credentials

**Option A: AWS CLI (Recommended for local development)**
```bash
aws configure
```

**Option B: Environment Variables**
```bash
export AWS_ACCESS_KEY_ID=your_access_key
export AWS_SECRET_ACCESS_KEY=your_secret_key
export AWS_DEFAULT_REGION=us-east-1
```

**Option C: IAM Roles (for EC2/ECS deployment)**
- Attach appropriate IAM role with S3 permissions

#### Update Application Properties for Production

```properties
# AWS S3 Configuration (Production)
aws.region=us-east-1
aws.s3.bucket=your-product-images-bucket
aws.s3.endpoint-override=
aws.s3.path-style-enabled=false
aws.access-key-id=${AWS_ACCESS_KEY_ID}
aws.secret-access-key=${AWS_SECRET_ACCESS_KEY}
```

### 5. Required S3 Permissions

Your AWS credentials need the following S3 permissions:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "s3:GetObject",
                "s3:PutObject",
                "s3:DeleteObject",
                "s3:HeadObject"
            ],
            "Resource": "arn:aws:s3:::your-product-images-bucket/*"
        },
        {
            "Effect": "Allow",
            "Action": [
                "s3:ListBucket"
            ],
            "Resource": "arn:aws:s3:::your-product-images-bucket"
        }
    ]
}
```

## Running the Application

### 1. Start Local Services
```bash
# Quick setup (recommended)
./setup-local.sh

# Or manually
docker compose -f docker-compose-services.yml up -d
```

### 2. Build the Application
```bash
mvn clean package
```

### 3. Run the Application
```bash
mvn spring-boot:run
```

Or run the JAR file:
```bash
java -jar target/product-service.jar
```

### 4. Access the Application
- **Web Interface**: http://localhost:8080
- **API Base URL**: http://localhost:8080/api/v1/products
- **MinIO Console**: http://localhost:9001 (minioadmin/minioadmin123)

## API Endpoints

### Product Management
- `GET /api/v1/products` - Get all products
- `GET /api/v1/products/{id}` - Get product by ID
- `POST /api/v1/products` - Create new product (JSON)
- `POST /api/v1/products` - Create new product with image (multipart/form-data)
- `PUT /api/v1/products/{id}` - Update product
- `DELETE /api/v1/products/{id}` - Delete product

### Image Management
- `POST /api/v1/products/{id}/image` - Upload product image
- `GET /api/v1/products/{id}/image` - Download product image

## Usage Examples

### Create a Product with Image

**Using the Web Interface:**
1. Open http://localhost:8080
2. Fill in the product form
3. Select an image file
4. Click "Create Product"

**Using cURL (Single Request - Recommended):**
```bash
# Create product with image in single request
curl -X POST http://localhost:8080/api/v1/products \
  -F "name=Sample Product" \
  -F "description=A sample product description" \
  -F "price=29.99" \
  -F "stockLevel=100" \
  -F "image=@/path/to/image.jpg"
```

**Using cURL (Separate Requests):**
```bash
# Create product first
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Sample Product",
    "description": "A sample product description",
    "price": 29.99,
    "stockLevel": 100
  }'

# Upload image (replace {product-id} with actual ID)
curl -X POST http://localhost:8080/api/v1/products/{product-id}/image \
  -F "file=@/path/to/image.jpg"
```

### Download Product Image
```bash
curl -O http://localhost:8080/api/v1/products/{product-id}/image
```

## Project Structure

```
src/
├── main/
│   ├── java/com/amigoscode/
│   │   ├── config/
│   │   │   └── AwsS3Config.java          # AWS S3 configuration
│   │   ├── product/
│   │   │   ├── Product.java              # Product entity
│   │   │   ├── ProductController.java    # REST controller
│   │   │   ├── ProductService.java       # Business logic
│   │   │   ├── ProductImageService.java  # Image handling service
│   │   │   └── ProductRepository.java    # Data access
│   │   └── storage/
│   │       └── S3StorageService.java     # S3 operations
│   └── resources/
│       ├── static/
│       │   └── index.html                 # Web interface
│       └── application.properties        # Configuration
```

## Docker Deployment

### Build Docker Image
```bash
mvn jib:build
```

### Run with Docker Compose
```bash
docker compose up -d
```

## Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn verify
```

## Troubleshooting

### Common Issues

1. **MinIO Connection Issues (Local Development)**
   - Ensure MinIO is running: `docker ps | grep minio`
   - Check MinIO logs: `docker logs jfs-minio-local`
   - Verify MinIO is accessible: `curl http://localhost:9000/minio/health/live`
   - Access MinIO console at http://localhost:9001

2. **AWS Credentials Not Found (Production)**
   - Ensure AWS credentials are properly configured
   - Check environment variables or AWS CLI configuration

3. **S3 Bucket Access Denied**
   - Verify bucket name in configuration
   - Check IAM permissions for S3 access
   - For MinIO: ensure bucket exists and is public

4. **Database Connection Issues**
   - Ensure PostgreSQL is running: `docker ps | grep postgres`
   - Check database logs: `docker logs jfs-postgres-local`
   - Verify database credentials in application.properties

5. **Image Upload Fails**
   - Check file size limits
   - Verify image file format is supported
   - Ensure S3/MinIO bucket exists and is accessible
   - Check MinIO bucket policy: should be public for downloads

### Logs
Check application logs for detailed error messages:
```bash
tail -f logs/application.log
```

## Security Considerations

- Use IAM roles instead of access keys when possible
- Implement proper CORS configuration for production
- Add authentication and authorization as needed
- Consider using S3 pre-signed URLs for direct uploads
- Implement file type validation and size limits

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License.
