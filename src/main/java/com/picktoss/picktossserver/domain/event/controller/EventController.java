package com.picktoss.picktossserver.domain.event.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Event")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class EventController {
}
