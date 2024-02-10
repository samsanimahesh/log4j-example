package com.example.log4j.log4jexample.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.sqs.SqsClient;

import static software.amazon.awssdk.regions.Region.US_EAST_1;
import static software.amazon.awssdk.regions.Region.US_WEST_1;

@Configuration
public class AwsConfig {
  public AWSCredentials credentials() {
    return new BasicAWSCredentials(
            "",
            ""
    );
  }

  @Bean
  public AmazonS3 amazonS3() {
    return AmazonS3ClientBuilder
            .standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials()))
            .withRegion(Regions.US_WEST_1)
            .build();
  }

  @Bean
  public SqsClient sqsClient() {
    return SqsClient.builder()
            .region(US_EAST_1)
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
  }

  @Bean
  public SqsClient dlqSqsClient() {
    return SqsClient.builder()
            .region(US_EAST_1)
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
  }
}
