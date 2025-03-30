package com.picktoss.picktossserver.domain.publicquizcollection.service;

import com.picktoss.picktossserver.core.s3.S3Provider;
import com.picktoss.picktossserver.domain.collection.entity.CollectionComplaintFile;
import com.picktoss.picktossserver.domain.discord.dto.DiscordMessage;
import com.picktoss.picktossserver.domain.discord.service.DiscordCollectionComplaintMessageSendService;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.publicquizcollection.entity.PublicQuizCollectionComplaint;
import com.picktoss.picktossserver.domain.publicquizcollection.repository.PublicQuizCollectionComplaintFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicQuizCollectionComplaintMessageSendService {

    private final S3Provider s3Provider;
    private final PublicQuizCollectionComplaintFileRepository publicQuizCollectionComplaintFileRepository;
    private final DiscordCollectionComplaintMessageSendService discordCollectionComplaintMessageSendService;

    public void sendCollectionComplaintDiscordMessage(PublicQuizCollectionComplaint publicQuizCollectionComplaint) {
        List<CollectionComplaintFile> collectionComplaintFiles = publicQuizCollectionComplaintFileRepository.findAllByPublicQuizCollectionComplaintId(publicQuizCollectionComplaint.getId());
        Member member = publicQuizCollectionComplaint.getMember();

        List<String> collectionComplaintImageUrls = new ArrayList<>();

        for (CollectionComplaintFile collectionComplaintFile : collectionComplaintFiles) {
            String collectionComplaintImageUrl = s3Provider.findImage(collectionComplaintFile.getS3Key());
            collectionComplaintImageUrls.add(collectionComplaintImageUrl);
        }

        DiscordMessage discordMessage = discordCollectionComplaintMessageSendService.createCollectionComplaintMessage(collectionComplaintImageUrls, publicQuizCollectionComplaint.getContent(), member.getId(), member.getName());
        discordCollectionComplaintMessageSendService.sendDiscordWebhookCollectionComplaintMessage(discordMessage);
    }
}
