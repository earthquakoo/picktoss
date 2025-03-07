package com.picktoss.picktossserver.domain.star.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Star")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class StarController {

    private final JwtTokenProvider jwtTokenProvider;
}
