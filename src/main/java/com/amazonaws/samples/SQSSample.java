/*
 * Copyright 2010-2013 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.amazonaws.samples;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

//AWS API imports
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;

/**
 * This sample demonstrates how to make basic requests to Amazon DynamoDB using
 * the AWS SDK for Java.
 * <p>
 * <b>Prerequisites:</b> You must have a valid Amazon Web Services developer
 * account, and be signed up to use DynamoDB
 * <p>
 * <b>Important:</b> Be sure to fill in your AWS access credentials in
 * ~/.aws/credentials (C:\Users\USER_NAME\.aws\credentials for Windows
 * users) before you try to run this sample.
 */
public class SQSSample {
    static AmazonSQS client = AmazonSQSClientBuilder.defaultClient();

   /*
     * Create your credentials file at ~/.aws/credentials (C:\Users\USER_NAME\.aws\credentials for Windows users)
     * and save the following lines after replacing the underlined values with your own.
     * [default]
     * aws_access_key_id = YOUR_ACCESS_KEY_ID
     * aws_secret_access_key = YOUR_SECRET_ACCESS_KEY
     */
    public static void main(String[] args) throws IOException {
        System.out.println("===========================================");
        System.out.println("Getting Started with Amazon SQS");
        System.out.println("===========================================\n");
        String randomSeed = UUID.randomUUID().toString();
        String queueName = "test-queue-" + randomSeed;
        String deadLetterQueueName =  "deadlettter-queue-" + randomSeed;

        try {
            createQueue(queueName,10);
            createDeadLetterQueue(deadLetterQueueName,queueName);
            listQueues();
            sendTestMessage(queueName);
            receiveTestMessaage(queueName);
            deleteQueue(queueName);
            deleteQueue(deadLetterQueueName);
            System.out.println("============= END ==========================");

        } catch (Exception ace) {
            System.out.println("Error Message: " + ace.getMessage());

        }//end catch
    }//end main

    /**
     * receiveTestMessaage.
     * @param queueName
     */
    private static void receiveTestMessaage(String queueName){
        String queueUrl = client.getQueueUrl(queueName).getQueueUrl();
        List<Message> messages = client.receiveMessage(queueUrl).getMessages();
        System.out.println("Receiving messages from queue :: " + queueName + " Total :: " + messages.size());
        for (Message m : messages) {
            System.out.println(" Message received " + m.getBody());
            client.deleteMessage(queueUrl, m.getReceiptHandle());
            System.out.println("Message deleted successfully after processing.");
        }
        System.out.println("===========================================\n");
    }//end sendTestMessage

    /**
     * Send Test Message.
     * @param queueName
     */
    private static void sendTestMessage(String queueName){
        String queueUrl = client.getQueueUrl(queueName).getQueueUrl();
        SendMessageRequest send_msg_request = new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageBody("hello world")
                .withDelaySeconds(0); //message available immediately for processing.
        client.sendMessage(send_msg_request);
        System.out.println("Sent message to " +  queueUrl + " successfully.");
        System.out.println("===========================================\n");

    }//end sendTestMessage

    /**
     * Create SQS queue
     * @param queueName Queuename
     * @param timeout Visibility timeout
     */
     private static void createQueue(String queueName,int timeout){
         System.out.println("Creating queue ... " + queueName);
         //long polling with ReceiveMessageWaitTimeSeconds of 20 seconds.
         CreateQueueRequest request = new CreateQueueRequest(queueName).addAttributesEntry("ReceiveMessageWaitTimeSeconds", "20");;
         Map<String, String> attributes = new HashMap<String, String>();
         attributes.put("VisibilityTimeout", String.valueOf(timeout));
         request.setAttributes(attributes);
         String queueUrl = client.createQueue(request).getQueueUrl();
         System.out.println(" Queue created successfully  :: " + queueUrl);
         System.out.println("===========================================\n");

     }//end createQueue

    private static void listQueues(){
        System.out.println("Listing Queues :: ");
        for (final String queueUrl : client.listQueues().getQueueUrls()) {
            System.out.println("  QueueUrl: " + queueUrl);
        }
        System.out.println("===========================================");
    }//end listQueues

    /**
     * Delete SQS queue
     * @param queueName
     */
    private static void deleteQueue(String queueName){
        String queueUrl = client.getQueueUrl(queueName).getQueueUrl();
        client.deleteQueue(queueUrl);
        System.out.println("Queue with queue name  :: " + queueName + " deleted successfully ");

    }//end deleteQueue


    /**
     * A dead letter queue must be the same type (FIFO or standard ) as the source queue.
     * A dead letter queue must be created using the same account and region as the source queue.
     */
    private static void createDeadLetterQueue(String deadLetterQueueName,String src_queue_name){
        System.out.println("Creating dead letter queue with name ... " + deadLetterQueueName);
        client.createQueue(deadLetterQueueName);
        /** To designate a dead letter queue to a source queue you must first create a redrive policy
            and and then set the policy in the queues attributes
             A redrive policy is specified as a JSON and specifies the ARN of the dead letter queue and the max
             no. of times the the message can be received and not processed before its sent to the
             dead letter queue.
         */
        String dl_queue_url = client.getQueueUrl(deadLetterQueueName).getQueueUrl();
        System.out.println("Dead letter queue created successfully - Queue URL -- " + dl_queue_url);

        //Get queue attributes.
        GetQueueAttributesResult queue_attrs = client.getQueueAttributes(
                new GetQueueAttributesRequest(dl_queue_url)
                        .withAttributeNames("QueueArn"));

        String dl_queue_arn = queue_attrs.getAttributes().get("QueueArn");
        System.out.println(" Dead letter queue ARN :: " + dl_queue_arn);

        //Get source queue URL
        String src_queue_url = client.getQueueUrl(src_queue_name).getQueueUrl();

        //Set dead letter queue with redrive policy on source queue.
        SetQueueAttributesRequest request = new SetQueueAttributesRequest()
                .withQueueUrl(src_queue_url)
                .addAttributesEntry("RedrivePolicy",
                        "{\"maxReceiveCount\":\"5\", \"deadLetterTargetArn\":\""
                                + dl_queue_arn + "\"}");

        client.setQueueAttributes(request);
        System.out.println("Dead letter queue configured successfully for source queue : " + src_queue_name);
        System.out.println("===========================================");

    }//createDeadLetterQueue end.

}
