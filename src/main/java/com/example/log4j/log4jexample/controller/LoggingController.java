package com.example.log4j.log4jexample.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.util.CollectionUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.io.File;
import java.io.IOException;
import java.util.*;

@RestController
public class LoggingController {

  Logger logger = LoggerFactory.getLogger(LoggingController.class);
  @Autowired
  AmazonS3 amazonS3Client;

  @Autowired
  SqsClient sqsClient;

  @Autowired
  SqsClient dlqSqsClient;

  @Value("${s3.bucket.name}")
  private String bucketName;

  @GetMapping("/upload")
  public ResponseEntity<String> uploadFile() {
    File logsFolder = new File("./Logs");
    if (logsFolder.isDirectory()) {
      List<File> directories = Arrays.stream(Objects.requireNonNull(logsFolder.listFiles()))
              .filter(File::isDirectory).toList().stream().sorted(Comparator.comparing(File::lastModified).reversed()).toList();
      File[] filesInDirectory = directories.get(0).listFiles();
      List<File> latestFiles = Arrays.stream(Objects.requireNonNull(filesInDirectory)).sorted(Comparator.comparing(File::lastModified).reversed()).toList();
      if (!CollectionUtils.isNullOrEmpty(latestFiles)) {
        String fileName = latestFiles.get(0).getName();
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, latestFiles.get(0));
        PutObjectResult result = amazonS3Client.putObject(putObjectRequest);
        return ResponseEntity.ok(result.getETag());
      }
    }
    return ResponseEntity.notFound().build();
    }

  @GetMapping("/publish")
  public void publishMessage() {
    SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
            .queueUrl("https://sqs.us-east-1.amazonaws.com/371670177427/myqueue")
            .messageBody("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. Curabitur pretium tincidunt lacus. Nulla gravida orci a odio. Nullam varius, turpis et commodo pharetra, est eros bibendum elit, nec luctus magna felis sollicitudin mauris.\n" +
                    "\n" +
                    "Integer in mauris eu nibh euismod gravida. Duis ac tellus et risus vulputate vehicula. Donec lobortis risus a elit. Etiam tempor. Ut ullamcorper, ligula eu tempor congue, eros est euismod turpis, id tincidunt sapien risus a quam. Maecenas fermentum consequat mi. Donec fermentum. Pellentesque malesuada nulla a mi. Duis sapien sem, aliquet nec, commodo eget, consequat quis, neque. Aliquam faucibus, elit ut dictum aliquet, felis nisl adipiscing sapien, sed malesuada diam lacus eget erat. Cras mollis scelerisque nunc. Nullam arcu. Aliquam consequat. Curabitur augue lorem, dapibus quis, laoreet et, pretium ac, nisi.\n" +
                    "\n" +
                    "Aenean magna nisl, mollis quis, molestie eu, feugiat in, orci. In hac habitasse platea dictumst. Integer tempus convallis augue. Etiam facilisis. Nunc elementum fermentum wisi. Aenean placerat. Ut imperdiet, enim sed gravida sollicitudin, felis odio placerat quam, ac pulvinar elit purus eget enim. Nunc vitae tortor. Proin tempus nibh sit amet nisl. Vivamus quis tortor vitae risus porta vehicula.\n" +
                    "\n" +
                    "Fusce mauris. Vestibulum luctus nibh at lectus. Sed bibendum, nulla a faucibus semper, leo velit ultricies tellus, ac venenatis arcu wisi vel nisl. Vestibulum diam. Aliquam pellentesque, augue quis sagittis posuere, turpis lacus congue quam, in hendrerit risus eros eget felis. Maecenas eget erat in sapien mattis porttitor. Vestibulum porttitor. Nulla facilisi. Sed a turpis eu lacus commodo facilisis. Morbi fringilla, wisi in dignissim interdum, justo lectus sagittis dui, et vehicula libero dui cursus dui.\n")
            .build();
    try {
      sqsClient.sendMessage(sendMessageRequest);
    } catch(Exception ex){
      SendMessageRequest dlqMessage = SendMessageRequest.builder()
              .queueUrl("https://sqs.us-east-1.amazonaws.com/371670177427/myqueue-dlq").messageBody(sendMessageRequest.messageBody()).build();
      dlqSqsClient.sendMessage(dlqMessage);
    }
  }
  }




