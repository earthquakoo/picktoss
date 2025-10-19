package com.picktoss.picktossserver.domain.document.controller;

import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExamples;
import com.picktoss.picktossserver.domain.document.dto.response.GetAllDocumentsResponse;
import com.picktoss.picktossserver.domain.document.service.DocumentReadServiceForLocalization;
import com.picktoss.picktossserver.global.enums.document.DocumentSortOption;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@Tag(name = "Document - Localization")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class DocumentLocalizationController {

    private final JwtTokenProvider jwtTokenProvider;
    private final DocumentReadServiceForLocalization documentReadServiceForLocalization;

//    @Operation(summary = "모든 문서 가져오기")
//    @GetMapping("/documents")
//    @ApiErrorCodeExamples({ErrorInfo.AMAZON_SERVICE_EXCEPTION, ErrorInfo.DOCUMENT_SORT_OPTION_NOT_SELECT})
//    @ResponseStatus(HttpStatus.OK)
//    public ResponseEntity<GetAllDocumentsResponse> getAllDocumentsForLocalization(
//            @RequestHeader(value = "Accept-Language", required = false) Locale locale,
//            @RequestParam(defaultValue = "CREATED_AT", value = "sort-option") DocumentSortOption documentSortOption) {
//
//        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
//        Long memberId = jwtUserInfo.getMemberId();
//
//        String resolvedLang = locale.getLanguage(); // "ko", "en", "ja" 등
//        System.out.println("resolvedLang = " + resolvedLang);
//        GetAllDocumentsResponse response = documentReadServiceForLocalization.findAllDocumentsForLocalization(memberId, documentSortOption, resolvedLang);
//        return ResponseEntity.ok().body(response);
//    }

    @Operation(summary = "모든 문서 가져오기")
    @GetMapping("/documents")
    @ApiErrorCodeExamples({ErrorInfo.AMAZON_SERVICE_EXCEPTION, ErrorInfo.DOCUMENT_SORT_OPTION_NOT_SELECT})
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetAllDocumentsResponse> getAllDocumentsForLocalization(
            @RequestParam(defaultValue = "CREATED_AT", value = "sort-option") DocumentSortOption documentSortOption) {

        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        Locale currentLocale = LocaleContextHolder.getLocale();
        System.out.println("Current Language: " + currentLocale.getLanguage());
        String lang = currentLocale.getLanguage();
        GetAllDocumentsResponse response = documentReadServiceForLocalization.findAllDocumentsForLocalization(memberId, documentSortOption, lang);
        return ResponseEntity.ok().body(response);
    }
}

