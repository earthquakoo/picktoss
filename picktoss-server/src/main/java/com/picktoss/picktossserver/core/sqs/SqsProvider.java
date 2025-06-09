package com.picktoss.picktossserver.core.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SqsProvider {

    private final AmazonSQS amazonSqs;

    @Value("${cloud.aws.sqs.queue.url}")
    private String url;

    public void sendMessage(Long memberId, String s3Key, Long documentId, Integer starCount) {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            ObjectNode jsonNode = mapper.createObjectNode();
            jsonNode.put("s3_key", s3Key);
            jsonNode.put("db_pk", documentId);
            jsonNode.put("member_id", memberId);
            jsonNode.put("star_count", starCount);
            String messageBody = mapper.writeValueAsString(jsonNode);

            String messageId = UUID.randomUUID().toString();

            SendMessageRequest sendMessageRequest = new SendMessageRequest()
                    .withQueueUrl(url)
                    .withMessageGroupId(messageId)
                    .withMessageDeduplicationId(messageId)
                    .withMessageBody(messageBody);

            amazonSqs.sendMessage(sendMessageRequest);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
