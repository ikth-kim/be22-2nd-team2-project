package com.team2.nextpage.query.member.service;

import com.team2.nextpage.query.member.dto.response.MemberDto;
import com.team2.nextpage.query.member.mapper.MemberMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 Query 서비스 (조회 전용)
 *
 * @author 김태형
 */
@Service
@Transactional(readOnly = true)
public class MemberQueryService {

    private final MemberMapper memberMapper;

    public MemberQueryService(MemberMapper memberMapper) {
        this.memberMapper = memberMapper;
    }

    /**
     * 마이페이지 정보 조회
     */
    public MemberDto getMyPage(String userEmail) {
        return memberMapper
                .findByUserEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));
    }
}
