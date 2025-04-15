package com.picktoss.picktossserver.domain.document.service;

import com.picktoss.picktossserver.core.s3.S3Provider;
import com.picktoss.picktossserver.domain.discord.dto.DiscordMessage;
import com.picktoss.picktossserver.domain.discord.service.DiscordCollectionComplaintMessageSendService;
import com.picktoss.picktossserver.domain.document.entity.DocumentComplaint;
import com.picktoss.picktossserver.domain.document.entity.DocumentComplaintFile;
import com.picktoss.picktossserver.domain.document.repository.DocumentComplaintFileRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentComplaintMessageSendService {

    private final S3Provider s3Provider;
    private final DocumentComplaintFileRepository documentComplaintFileRepository;
    private final DiscordCollectionComplaintMessageSendService discordCollectionComplaintMessageSendService;

    public void sendCollectionComplaintDiscordMessage(DocumentComplaint documentComplaint) {
        List<DocumentComplaintFile> documentComplaintFiles = documentComplaintFileRepository.findAllByDocumentComplaintId(documentComplaint.getId());
        Member member = documentComplaint.getMember();

        List<String> documentComplaintImageUrls = new ArrayList<>();

        for (DocumentComplaintFile documentComplaintFile : documentComplaintFiles) {
            String documentComplaintImageUrl = s3Provider.findImage(documentComplaintFile.getS3Key());
            documentComplaintImageUrls.add(documentComplaintImageUrl);
        }

        DiscordMessage discordMessage = discordCollectionComplaintMessageSendService.createCollectionComplaintMessage(documentComplaintImageUrls, documentComplaint.getContent(), member.getId(), member.getName());
        discordCollectionComplaintMessageSendService.sendDiscordWebhookCollectionComplaintMessage(discordMessage);
    }
}
