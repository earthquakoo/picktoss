package com.picktoss.picktossserver.domain.collection.service;

import com.picktoss.picktossserver.core.s3.S3Provider;
import com.picktoss.picktossserver.domain.collection.entity.CollectionComplaint;
import com.picktoss.picktossserver.domain.collection.entity.CollectionComplaintFile;
import com.picktoss.picktossserver.domain.collection.repository.CollectionComplaintFileRepository;
import com.picktoss.picktossserver.domain.discord.dto.DiscordMessage;
import com.picktoss.picktossserver.domain.discord.service.DiscordCollectionComplaintMessageSendService;
import com.picktoss.picktossserver.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollectionComplaintMessageSendService {

    private final S3Provider s3Provider;
    private final CollectionComplaintFileRepository collectionComplaintFileRepository;
    private final DiscordCollectionComplaintMessageSendService discordCollectionComplaintMessageSendService;

    public void sendCollectionComplaintDiscordMessage(CollectionComplaint collectionComplaint) {
        List<CollectionComplaintFile> collectionComplaintFiles = collectionComplaintFileRepository.findAllByCollectionComplaintId(collectionComplaint.getId());
        Member member = collectionComplaint.getMember();

        List<String> collectionComplaintImageUrls = new ArrayList<>();

        for (CollectionComplaintFile collectionComplaintFile : collectionComplaintFiles) {
            String collectionComplaintImageUrl = s3Provider.findImage(collectionComplaintFile.getS3Key());
            collectionComplaintImageUrls.add(collectionComplaintImageUrl);
        }

        DiscordMessage discordMessage = discordCollectionComplaintMessageSendService.createCollectionComplaintMessage(collectionComplaintImageUrls, collectionComplaint.getContent(), member.getId(), member.getName());
        discordCollectionComplaintMessageSendService.sendDiscordWebhookCollectionComplaintMessage(discordMessage);
    }
}
