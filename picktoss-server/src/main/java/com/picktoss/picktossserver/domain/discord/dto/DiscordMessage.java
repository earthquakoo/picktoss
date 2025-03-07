package com.picktoss.picktossserver.domain.discord.dto;

import lombok.*;

import java.util.List;

@Getter
@Builder
public class DiscordMessage {

    private String content;
    private List<Embed> embeds;

    @Getter
    @Builder
    public static class Embed {
        private String title;
        private String description;
    }
}