# Quick AWS Deployment Checklist

## ✅ What You Need to Do in AWS Console

### 1. Create S3 Bucket
- Go to **S3** → **Create bucket**
- Name: `yourcompany-product-images-prod` (unique name)
- Region: Same as your EC2 (e.g., `us-east-1`)
- Encryption: Enable (SSE-S3)
- **Save bucket name**

### 2. Create IAM Policy
- Go to **IAM** → **Policies** → **Create policy**
- Use JSON tab, paste:
```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": ["s3:GetObject", "s3:PutObject", "s3:DeleteObject", "s3:HeadObject"],
            "Resource": "arn:aws:s3:::YOUR-BUCKET-NAME/*"
        },
        {
            "Effect": "Allow",
            "Action": ["s3:ListBucket"],
            "Resource": "arn:aws:s3:::YOUR-BUCKET-NAME"
        }
    ]
}
```
- Replace `YOUR-BUCKET-NAME` with your bucket name
- Name: `ProductServiceS3Policy`

### 3. Create IAM Role (Recommended)
- Go to **IAM** → **Roles** → **Create role**
- Select **EC2** → **Next**
- Attach `ProductServiceS3Policy` → **Next**
- Name: `EC2-S3-Access-Role` → **Create role**

### 4. Attach Role to EC2
- Go to **EC2** → **Instances**
- Select your instance → **Actions** → **Security** → **Modify IAM role**
- Select `EC2-S3-Access-Role` → **Update**

### 5. Update Application Configuration

Create `application-prod.properties` or use environment variables:

```properties
aws.region=us-east-1
aws.s3.bucket=your-bucket-name-here
aws.s3.endpoint-override=
aws.s3.path-style-enabled=false
aws.access-key-id=
aws.secret-access-key=
```

**Note**: Leave access keys empty if using IAM role (recommended)

### 6. Deploy Application
```bash
# Build
mvn clean package

# Copy to EC2
scp -i key.pem target/product-service.jar ec2-user@your-ec2-ip:~/app/

# SSH into EC2
ssh -i key.pem ec2-user@your-ec2-ip

# Run with production config
cd ~/app
java -jar product-service.jar --spring.config.location=application-prod.properties
```

## 🔐 Alternative: Using Access Keys (Less Secure)

If not using IAM role, create IAM user:
- **IAM** → **Users** → **Create user**
- Attach `ProductServiceS3Policy`
- Create access key → **Save keys securely**
- Set environment variables on EC2:
```bash
export AWS_ACCESS_KEY_ID=your-key-id
export AWS_SECRET_ACCESS_KEY=your-secret-key
```

---

**See `AWS_DEPLOYMENT_GUIDE.md` for detailed step-by-step instructions.**

