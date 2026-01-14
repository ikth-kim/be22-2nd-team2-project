package com.team2.nextpage.query.member.controller;

import com.team2.nextpage.common.error.BusinessException;
import com.team2.nextpage.common.error.ErrorCode;
import com.team2.nextpage.common.response.ApiResponse;
import com.team2.nextpage.common.util.SecurityUtil;
import com.team2.nextpage.query.member.dto.response.MemberDto;
import com.team2.nextpage.query.member.service.MemberQueryService;
import com.team2.nextpage.command.member.controller.MemberController;
import com.team2.nextpage.query.book.controller.BookQueryController;
import com.team2.nextpage.query.reaction.controller.ReactionQueryController;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * 회원 Query 컨트롤러
 *
 * @author 김태형
 */
@Tag(name = "Member Queries", description = "회원 조회(Query) API")
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberQueryController {

  private final MemberQueryService memberQueryService;

  /**
   * 마이페이지 조회 API
   * GET /api/members/me
   *
   * @return 현재 로그인한 사용자 정보 (활동 통계 포함)
   * @throws BusinessException 로그인하지 않은 경우
   */
  @Operation(summary = "내 정보 조회 (마이페이지)", description = "현재 로그인한 사용자의 상세 정보와 활동 통계를 조회합니다.")
  @GetMapping("/me")
  public ResponseEntity<ApiResponse<MemberDto>> getMyInfo() {
    String userEmail = SecurityUtil.getCurrentUserEmail();

    if (userEmail == null) {
      throw new BusinessException(ErrorCode.UNAUTHENTICATED);
    }

    MemberDto memberInfo = memberQueryService.getMyPage(userEmail);

    // HATEOAS Links
    memberInfo.add(linkTo(methodOn(MemberQueryController.class).getMyInfo()).withSelfRel());
    memberInfo.add(linkTo(methodOn(BookQueryController.class).getMySentences(0, 10)).withRel("my-sentences"));
    memberInfo.add(linkTo(methodOn(ReactionQueryController.class).getMyComments(0, 10)).withRel("my-comments"));
    memberInfo.add(linkTo(methodOn(MemberController.class).withdraw()).withRel("withdraw"));

    return ResponseEntity.ok(ApiResponse.success(memberInfo));
  }
}
