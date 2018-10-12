# AWS SDK usage for SQS integration in Java application.

A simple Java application illustrating usage of the AWS SDK API for SQS for Java.

## Setup.

You need to create a AWS user and generate access keys for this user.This user must have
dynamoDB full access.

Install AWS CLI and then run command aws configure to generate aws credentials file at the
(C:\Users\USER_NAME\.aws\credentials for Windows users)  location.

    [default]
    aws_access_key_id = <your access key id>
    aws_secret_access_key = <your secret key>

## Running the application.
Maven:

    mvn clean compile exec:java

Gradle:

    gradlew clean build run

## Reference

https://docs.aws.amazon.com/general/latest/gr/aws-sec-cred-types.html#access-keys-and-secret-access-keys

https://docs.aws.amazon.com/cli/latest/userguide/awscli-install-windows.html

https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/examples-sqs-message-queues.html


