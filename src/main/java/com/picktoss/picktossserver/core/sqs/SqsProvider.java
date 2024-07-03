package com.picktoss.picktossserver.core.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.picktoss.picktossserver.global.enums.SubscriptionPlanType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SqsProvider {

    private final AmazonSQS amazonSqs;

    @Value("${cloud.aws.sqs.queue.url}")
    private String url;

    public void sendMessage(Long memberId, String s3Key, Long documentId, SubscriptionPlanType subscriptionPlan) {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            ObjectNode jsonNode = mapper.createObjectNode();
            jsonNode.put("s3_key", s3Key);
            jsonNode.put("db_pk", documentId);
            jsonNode.put("subscription_plan", subscriptionPlan.name());
            jsonNode.put("member_id", memberId);
            String messageBody = mapper.writeValueAsString(jsonNode);

            SendMessageRequest sendMessageRequest = new SendMessageRequest()
                    .withQueueUrl(url)
                    .withMessageBody(messageBody);

            amazonSqs.sendMessage(sendMessageRequest);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
