package com.amigoscode.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
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

    @Value("${aws.access-key-id:minioadmin}")
    private String accessKeyId;

    @Value("${aws.secret-access-key:minioadmin123}")
    private String secretAccessKey;

    @Value("${spring.profiles.active:local}")
    private String activeProfile;

    @Bean
    public S3Client s3Client() {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(pathStyleEnabled)
                        .build());
        
        // Use MinIO credentials for local development, AWS profile for other environments
        if ("local".equals(activeProfile)) {
            builder = builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKeyId, secretAccessKey)));
        } else {
            builder = builder.credentialsProvider(ProfileCredentialsProvider.create());
        }
        
        if (StringUtils.isNotBlank(endpointOverride)) {
            builder = builder.endpointOverride(URI.create(endpointOverride));
        }
        return builder.build();
    }
}
