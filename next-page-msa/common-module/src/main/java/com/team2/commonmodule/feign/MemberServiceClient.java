package com.team2.commonmodule.feign;

import com.team2.commonmodule.feign.dto.MemberBatchInfoDto;
import com.team2.commonmodule.feign.dto.MemberInfoDto;
import com.team2.commonmodule.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Member Service Feign Client
 *
 * <p>
 * 다른 MSA 서비스가 member-service의 회원 정보를 조회할 때 사용하는 Feign Client입니다.
 * </p>
 *
 * <p>
 * <b>사용 서비스:</b>
 * <ul>
 *   <li>story-service: Book, Sentence의 작성자 정보 조회</li>
 *   <li>reaction-service: Comment, Vote의 작성자 정보 조회</li>
 * </ul>
 * </p>
 *
 * @author MSA Team
 */
@FeignClient(name = "member-service")
public interface MemberServiceClient {

    /**
     * 단일 회원 정보 조회
     *
     * @param userId 회원 ID
     * @return 회원 정보 (userId, nickname, role)
     */
    @GetMapping("/internal/members/{userId}")
    ApiResponse<MemberInfoDto> getMemberInfo(@PathVariable("userId") Long userId);

    /**
     * 여러 회원 정보 일괄 조회
     *
     * <p>N+1 문제를 방지하기 위해 여러 회원의 정보를 한 번에 조회합니다.</p>
     *
     * @param userIds 회원 ID 리스트
     * @return 회원 정보 리스트
     */
    @GetMapping("/internal/members/batch")
    ApiResponse<MemberBatchInfoDto> getMembersBatch(@RequestParam("userIds") List<Long> userIds);

    /**
     * 회원 존재 여부 확인
     *
     * @param userId 회원 ID
     * @return 존재 여부
     */
    @GetMapping("/internal/members/{userId}/exists")
    ApiResponse<Boolean> memberExists(@PathVariable("userId") Long userId);
}
