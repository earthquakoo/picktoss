package com.picktoss.picktossserver.domain.event.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.document.facade.DocumentFacade;
import com.picktoss.picktossserver.domain.event.controller.request.ChangePointRequest;
import com.picktoss.picktossserver.domain.event.facade.EventFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class EventController {

    private final JwtTokenProvider jwtTokenProvider;
    private final EventFacade eventFacade;

    // 클라이언트 테스트 전용 API(실제 서비스 사용 X)
    @Tag(name = "Client test 전용 API")
    @Operation(summary = "별 개수 변경 API(테스트 혹은 예외처리를 위한 API로서 실제 사용 X)")
    @PatchMapping("/test/change-point")
    @ResponseStatus(HttpStatus.OK)
    public void changePointForTest(
            @Valid @RequestBody ChangePointRequest request
            ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        eventFacade.changePointForTest(memberId, request.getPoint());
    }
}
