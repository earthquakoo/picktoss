package com.picktoss.picktossserver.domain.directory.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExample;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExamples;
import com.picktoss.picktossserver.domain.directory.controller.request.CreateDirectoryRequest;
import com.picktoss.picktossserver.domain.directory.controller.request.UpdateDirectoryInfoRequest;
import com.picktoss.picktossserver.domain.directory.controller.response.CreateDirectoryResponse;
import com.picktoss.picktossserver.domain.directory.controller.response.GetAllDirectoriesResponse;
import com.picktoss.picktossserver.domain.directory.controller.response.GetSingleDirectoryResponse;
import com.picktoss.picktossserver.domain.directory.facade.DirectoryFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.*;

@Tag(name = "Directory")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class DirectoryController {

    private final DirectoryFacade directoryFacade;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * GET
     */

    @Operation(summary = "모든 디렉토리 가져오기")
    @GetMapping("/directories")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetAllDirectoriesResponse> getDirectories() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        List<GetAllDirectoriesResponse.GetAllDirectoriesDirectoryDto> directories = directoryFacade.findAllDirectories(memberId);
        return ResponseEntity.ok().body(new GetAllDirectoriesResponse(directories));
    }

    @Operation(summary = "directory_id로 디렉토리 가져오기")
    @GetMapping("/directories/{directory_id}")
    @ApiErrorCodeExample(DIRECTORY_NOT_FOUND)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetSingleDirectoryResponse> getSingleDirectory(@PathVariable("directory_id") Long directoryId) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        GetSingleDirectoryResponse response = directoryFacade.findSingleDirectory(directoryId, memberId);
        return ResponseEntity.ok().body(response);
    }

    /**
     * POST
     */

    @Operation(summary = "디렉토리 생성")
    @PostMapping("/directories")
    @ApiErrorCodeExample(MEMBER_NOT_FOUND)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CreateDirectoryResponse> createDirectory(@Valid @RequestBody CreateDirectoryRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        Long directoryId = directoryFacade.createDirectory(memberId, request.getName(), request.getEmoji());
        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateDirectoryResponse(directoryId));
    }

    /**
     * PATCH
     */

    @Operation(summary = "디렉토리 정보 변경")
    @PatchMapping("/directories/{directory_id}/update-info")
    @ApiErrorCodeExamples({DIRECTORY_NOT_FOUND, UNAUTHORIZED_OPERATION_EXCEPTION})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateDirectoryInfo(
            @PathVariable(name = "directory_id") Long directoryId,
            @Valid @RequestBody UpdateDirectoryInfoRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        directoryFacade.updateDirectoryInfo(memberId, directoryId, request.getName(), request.getEmoji());
    }

    /**
     * DELETE
     */

    @Operation(summary = "디렉토리 삭제")
    @DeleteMapping("/directories/{directory_id}")
    @ApiErrorCodeExamples({DIRECTORY_NOT_FOUND, UNAUTHORIZED_OPERATION_EXCEPTION})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDirectory(@PathVariable(name = "directory_id") Long directoryId) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();
        directoryFacade.deleteDirectory(memberId, directoryId);
    }
}
