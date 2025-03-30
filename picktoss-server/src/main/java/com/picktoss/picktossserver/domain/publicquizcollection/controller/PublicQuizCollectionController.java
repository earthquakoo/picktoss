package com.picktoss.picktossserver.domain.publicquizcollection.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "PublicQuizCollection")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class PublicQuizCollectionController {

    private final JwtTokenProvider jwtTokenProvider;

}
