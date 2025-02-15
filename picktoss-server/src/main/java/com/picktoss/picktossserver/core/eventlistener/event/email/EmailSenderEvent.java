package com.picktoss.picktossserver.core.eventlistener.event.email;

import com.picktoss.picktossserver.domain.member.entity.Member;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class EmailSenderEvent {
    private final List<Member> members;
}
