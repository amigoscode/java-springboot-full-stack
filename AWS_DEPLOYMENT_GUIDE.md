# AWS Deployment Guide for S3 Integration

This guide walks you through setting up AWS S3 for your Spring Boot application deployment on EC2.

## Prerequisites
- ✅ EC2 instance already running
- AWS Console access with appropriate permissions

---

## Step 1: Create S3 Bucket

1. **Log into AWS Console**
   - Go to https://console.aws.amazon.com
   - Navigate to **S3** service (search "S3" in the top search bar)

2. **Create Bucket**
   - Click **"Create bucket"** button
   - **Bucket name**: Enter a unique name (e.g., `yourcompany-product-images-prod`)
     - ⚠️ Bucket names must be globally unique across all AWS accounts
     - Use lowercase letters, numbers, hyphens only
     - Example: `amigoscode-product-images-2024`
   
3. **Configure Bucket Settings**
   - **AWS Region**: Select your region (e.g., `us-east-1` - same as your EC2 region)
   - **Object Ownership**: Select **"ACLs disabled (recommended)"**
   - **Block Public Access**: 
     - ✅ **Keep all settings enabled** (unless you need public access to images)
     - If you want public image access, uncheck "Block all public access" and acknowledge
   - **Versioning**: Disable (unless you need versioning)
   - **Encryption**: Choose **"Enable"** → **"Amazon S3 managed keys (SSE-S3)"** (recommended)
   - **Tags**: Optional - add tags for organization
   
4. **Create Bucket**
   - Click **"Create bucket"** button at the bottom
   - **Note down your bucket name** - you'll need it for configuration

---

## Step 2: Create IAM User for Application Access

### Option A: IAM User with Access Keys (Recommended for EC2)

1. **Navigate to IAM**
   - In AWS Console, search for **"IAM"** and open the service
   - Click **"Users"** in the left sidebar
   - Click **"Create user"** button

2. **Set User Details**
   - **User name**: `product-service-s3-user` (or your preferred name)
   - **Select credential type**: ✅ Check **"Provide user access to the AWS Management Console"** (optional, for testing)
   - Or just check **"Access key - Programmatic access"** (for API access only)
   - Click **"Next"**

3. **Set Permissions**
   - Select **"Attach policies directly"**
   - Click **"Create policy"** button (opens in new tab)
   
4. **Create Custom Policy**
   - In the new tab, click **"JSON"** tab
   - Replace the content with:
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
               "Resource": "arn:aws:s3:::YOUR-BUCKET-NAME/*"
           },
           {
               "Effect": "Allow",
               "Action": [
                   "s3:ListBucket"
               ],
               "Resource": "arn:aws:s3:::YOUR-BUCKET-NAME"
           }
       ]
   }
   ```
   - **Replace `YOUR-BUCKET-NAME`** with your actual bucket name (e.g., `amigoscode-product-images-2024`)
   - Click **"Next"**
   - **Policy name**: `ProductServiceS3Policy`
   - **Description**: `Allows S3 access for product image service`
   - Click **"Create policy"**
   - **Go back to the user creation tab**

5. **Attach Policy to User**
   - Refresh the policy list (click refresh icon)
   - Search for `ProductServiceS3Policy`
   - ✅ Check the box next to your policy
   - Click **"Next"**

6. **Review and Create**
   - Review the settings
   - Click **"Create user"**

7. **Save Access Keys** ⚠️ **CRITICAL - DO THIS NOW**
   - After creating user, you'll see **"Access key"** section
   - Click **"Create access key"**
   - **Use case**: Select **"Application running outside AWS"**
   - Click **"Next"**
   - Click **"Create access key"**
   - **⚠️ IMPORTANT**: 
     - **Copy the Access Key ID** (e.g., `AKIAIOSFODNN7EXAMPLE`)
     - **Copy the Secret Access Key** (e.g., `wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY`)
     - **Download CSV file** as backup
     - **You cannot view the secret key again** after closing this page
   - Click **"Done"**

---

## Step 3: Configure EC2 Instance with Credentials

### Method 1: Using IAM Role (Recommended - More Secure)

1. **Create IAM Role**
   - In IAM Console, click **"Roles"** in left sidebar
   - Click **"Create role"**
   - **Select trusted entity**: **"AWS service"**
   - **Use case**: Select **"EC2"**
   - Click **"Next"**

2. **Attach Permissions**
   - Search for `ProductServiceS3Policy` (the policy you created earlier)
   - ✅ Check the box
   - Click **"Next"**

3. **Name Role**
   - **Role name**: `EC2-S3-Access-Role`
   - **Description**: `Allows EC2 instance to access S3 bucket`
   - Click **"Create role"**

4. **Attach Role to EC2 Instance**
   - Go to **EC2 Console** → **Instances**
   - Select your EC2 instance
   - Click **"Actions"** → **"Security"** → **"Modify IAM role"**
   - Select `EC2-S3-Access-Role`
   - Click **"Update IAM role"**

### Method 2: Using Environment Variables (Alternative)

If you prefer using access keys directly:

1. **SSH into your EC2 instance**
   ```bash
   ssh -i your-key.pem ec2-user@your-ec2-ip
   ```

2. **Set environment variables** (add to `/etc/environment` or your deployment script)
   ```bash
   sudo nano /etc/environment
   ```
   
   Add these lines:
   ```
   AWS_ACCESS_KEY_ID=your-access-key-id
   AWS_SECRET_ACCESS_KEY=your-secret-access-key
   AWS_DEFAULT_REGION=us-east-1
   ```

3. **Or create a `.env` file** in your application directory:
   ```bash
   nano ~/app/.env
   ```
   ```
   AWS_ACCESS_KEY_ID=your-access-key-id
   AWS_SECRET_ACCESS_KEY=your-secret-access-key
   AWS_DEFAULT_REGION=us-east-1
   ```

---

## Step 4: Update Application Configuration

### Option A: Using Environment Variables (Recommended)

Create or update your `application.properties` for production:

```properties
# AWS S3 Configuration (Production)
aws.region=us-east-1
aws.s3.bucket=your-bucket-name-here
aws.s3.endpoint-override=
aws.s3.path-style-enabled=false
aws.access-key-id=${AWS_ACCESS_KEY_ID}
aws.secret-access-key=${AWS_SECRET_ACCESS_KEY}
```

### Option B: Direct Configuration (Less Secure)

If not using environment variables:

```properties
# AWS S3 Configuration (Production)
aws.region=us-east-1
aws.s3.bucket=your-bucket-name-here
aws.s3.endpoint-override=
aws.s3.path-style-enabled=false
aws.access-key-id=AKIAIOSFODNN7EXAMPLE
aws.secret-access-key=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
```

⚠️ **Security Note**: Never commit access keys to version control!

---

## Step 5: Update Application Code (if needed)

If using IAM Role (Method 1), you need to update `AwsS3Config.java` to support IAM roles:

The current code uses `StaticCredentialsProvider`. If using IAM roles, AWS SDK will automatically use instance credentials. Update the config:

```java
@Bean
public S3Client s3Client() {
    S3ClientBuilder builder = S3Client.builder()
            .region(Region.of(region))
            .serviceConfiguration(
                    S3Configuration
                            .builder()
                            .pathStyleAccessEnabled(pathStyleEnabled)
                            .build()
            );
    
    // Only use static credentials if access key is provided
    if (StringUtils.isNotBlank(accessKeyId) && !accessKeyId.equals("minioadmin")) {
        builder = builder.credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKeyId, secretAccessKey))
        );
    }
    // Otherwise, AWS SDK will use default credential chain (IAM role, env vars, etc.)
    
    if (StringUtils.isNotBlank(endpointOverride)) {
        builder = builder.endpointOverride(URI.create(endpointOverride));
    }
    return builder.build();
}
```

---

## Step 6: Deploy Application to EC2

1. **Build your application**
   ```bash
   mvn clean package
   ```

2. **Transfer JAR to EC2**
   ```bash
   scp -i your-key.pem target/product-service.jar ec2-user@your-ec2-ip:~/app/
   ```

3. **SSH into EC2**
   ```bash
   ssh -i your-key.pem ec2-user@your-ec2-ip
   ```

4. **Create application.properties for production**
   ```bash
   cd ~/app
   nano application.properties
   ```
   
   Add production configuration (see Step 4)

5. **Run application**
   ```bash
   java -jar product-service.jar --spring.config.location=application.properties
   ```

   Or with environment variables:
   ```bash
   export AWS_ACCESS_KEY_ID=your-key-id
   export AWS_SECRET_ACCESS_KEY=your-secret-key
   export AWS_DEFAULT_REGION=us-east-1
   java -jar product-service.jar
   ```

---

## Step 7: Verify S3 Integration

1. **Test Image Upload**
   - Use your application's API to upload a product image
   - Go to S3 Console → Your bucket
   - Verify the image appears in `products/` folder

2. **Check Permissions**
   - If upload fails, verify:
     - IAM role/policy is attached correctly
     - Bucket name matches configuration
     - Region matches your EC2 instance region

---

## Security Best Practices

1. ✅ **Use IAM Roles** instead of access keys when possible (Method 1)
2. ✅ **Never commit credentials** to version control
3. ✅ **Use environment variables** or AWS Secrets Manager for sensitive data
4. ✅ **Restrict S3 bucket access** to specific IPs if needed (via bucket policy)
5. ✅ **Enable S3 bucket versioning** for production (if needed)
6. ✅ **Enable CloudTrail** to audit S3 access (optional but recommended)

---

## Troubleshooting

### Issue: "Access Denied" when uploading
- **Solution**: Verify IAM policy permissions and bucket name
- Check CloudTrail logs for detailed error messages

### Issue: "Bucket not found"
- **Solution**: Verify bucket name matches exactly (case-sensitive)
- Ensure bucket is in the same region as configured

### Issue: "Invalid endpoint"
- **Solution**: Remove `aws.s3.endpoint-override` property for production
- Set `aws.s3.path-style-enabled=false` for AWS S3

### Testing Credentials
```bash
aws s3 ls s3://your-bucket-name/ --region us-east-1
```

---

## Next Steps

- Set up CloudFront CDN for image delivery (optional)
- Configure S3 lifecycle policies for old images
- Set up monitoring with CloudWatch
- Configure backup strategies

