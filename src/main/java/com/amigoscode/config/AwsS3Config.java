package com.amigoscode.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.utils.StringUtils;

import java.net.URI;

@Configuration
public class AwsS3Config {

    @Value("${aws.region:us-east-1}")
    private String region;

    @Value("${aws.s3.endpoint-override:}")
    private String endpointOverride;

    @Value("${aws.s3.path-style-enabled:false}")
    private boolean pathStyleEnabled;

    @Value("${aws.access-key-id:}")
    private String accessKeyId;

    @Value("${aws.secret-access-key:}")
    private String secretAccessKey;

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

        // Use static credentials if provided (for local/MinIO or explicit credentials)
        // Otherwise use default credential chain (IAM roles, environment variables, etc.)
        if (StringUtils.isNotBlank(accessKeyId) && StringUtils.isNotBlank(secretAccessKey)) {
            builder = builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKeyId, secretAccessKey))
            );
        } else {
            // Use default credential chain - will automatically use:
            // 1. IAM role (if running on EC2/ECS/Lambda)
            // 2. Environment variables (AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY)
            // 3. AWS credentials file (~/.aws/credentials)
            builder = builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        if (StringUtils.isNotBlank(endpointOverride)) {
            builder = builder.endpointOverride(URI.create(endpointOverride));
        }
        return builder.build();
    }
}
