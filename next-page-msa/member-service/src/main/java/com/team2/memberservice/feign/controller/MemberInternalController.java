package com.team2.memberservice.feign.controller;

import com.team2.commonmodule.feign.dto.MemberBatchInfoDto;
import com.team2.commonmodule.feign.dto.MemberInfoDto;
import com.team2.commonmodule.response.ApiResponse;
import com.team2.memberservice.feign.service.MemberInternalService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 내부 MSA 서비스 간 통신용 컨트롤러
 *
 * <p>
 * 이 컨트롤러는 다른 MSA 서비스(story-service, reaction-service)가
 * Feign Client를 통해 회원 정보를 조회할 때 사용됩니다.
 * </p>
 *
 * <p>
 * <b>주의:</b> 이 API는 Gateway를 거치지 않고 내부 네트워크에서만 접근 가능해야 합니다.
 * 프로덕션 환경에서는 방화벽 규칙으로 외부 접근을 차단해야 합니다.
 * </p>
 *
 * @author MSA Team
 */
@Hidden // Swagger UI에서 숨김 처리
@RestController
@RequestMapping("/internal/members")
@RequiredArgsConstructor
public class MemberInternalController {

    private final MemberInternalService memberInternalService;

    /**
     * 단일 회원 정보 조회 (Feign Client용)
     *
     * @param userId 조회할 회원 ID
     * @return 회원 정보 (userId, nickname, role)
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<MemberInfoDto>> getMemberInfo(@PathVariable Long userId) {
        MemberInfoDto memberInfo = memberInternalService.getMemberInfo(userId);
        return ResponseEntity.ok(ApiResponse.success(memberInfo));
    }

    /**
     * 여러 회원 정보 일괄 조회 (Feign Client용)
     *
     * <p>
     * 성능 최적화를 위해 여러 회원의 정보를 한 번의 요청으로 조회합니다.
     * N+1 문제를 방지하기 위해 리스트 조회 시 사용합니다.
     * </p>
     *
     * @param userIds 조회할 회원 ID 리스트 (쉼표로 구분)
     * @return 회원 정보 리스트
     */
    @GetMapping("/batch")
    public ResponseEntity<ApiResponse<MemberBatchInfoDto>> getMembersBatch(@RequestParam List<Long> userIds) {
        MemberBatchInfoDto members = memberInternalService.getMembersBatch(userIds);
        return ResponseEntity.ok(ApiResponse.success(members));
    }

    /**
     * 회원 존재 여부 확인 (Feign Client용)
     *
     * @param userId 확인할 회원 ID
     * @return 존재 여부 (true/false)
     */
    @GetMapping("/{userId}/exists")
    public ResponseEntity<ApiResponse<Boolean>> memberExists(@PathVariable Long userId) {
        boolean exists = memberInternalService.memberExists(userId);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }
}
