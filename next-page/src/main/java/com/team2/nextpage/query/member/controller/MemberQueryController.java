package com.team2.nextpage.query.member.controller;

import com.team2.nextpage.query.member.dto.response.MemberDto;
import com.team2.nextpage.query.member.service.MemberQueryService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 회원 Query 컨트롤러
 *
 * @author 김태형
 */
@RestController
@RequestMapping("/api/members")
public class MemberQueryController {

    private final MemberQueryService memberQueryService;

    public MemberQueryController(MemberQueryService memberQueryService) {
        this.memberQueryService = memberQueryService;
    }

    /**
     * 마이페이지 조회 API
     */
    @GetMapping("/me")
    public MemberDto getMyInfo(@AuthenticationPrincipal String userEmail) {
      if(userEmail==null){
        throw new RuntimeException("로그인 정보가 유효하지 않습니다.");
      }

      return memberQueryService.getMyPage(userEmail);
    }
}
