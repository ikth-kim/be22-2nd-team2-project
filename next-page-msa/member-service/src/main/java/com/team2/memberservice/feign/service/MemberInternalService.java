package com.team2.memberservice.feign.service;

import com.team2.commonmodule.error.BusinessException;
import com.team2.commonmodule.error.ErrorCode;
import com.team2.commonmodule.feign.dto.MemberBatchInfoDto;
import com.team2.commonmodule.feign.dto.MemberInfoDto;
import com.team2.memberservice.command.member.entity.Member;
import com.team2.memberservice.command.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 내부 MSA 서비스 간 통신용 서비스
 *
 * <p>다른 MSA 서비스가 회원 정보를 조회할 때 사용하는 서비스입니다.</p>
 *
 * @author MSA Team
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberInternalService {

    private final MemberRepository memberRepository;

    /**
     * 단일 회원 정보 조회
     *
     * @param userId 회원 ID
     * @return 회원 정보
     * @throws BusinessException 회원을 찾을 수 없는 경우
     */
    public MemberInfoDto getMemberInfo(Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        return new MemberInfoDto(
                member.getUserId(),
                member.getUserNicknm(),
                member.getUserRole().name()
        );
    }

    /**
     * 여러 회원 정보 일괄 조회
     *
     * @param userIds 회원 ID 리스트
     * @return 회원 정보 리스트
     */
    public MemberBatchInfoDto getMembersBatch(List<Long> userIds) {
        List<Member> members = memberRepository.findAllById(userIds);

        List<MemberInfoDto> memberInfoList = members.stream()
                .map(member -> new MemberInfoDto(
                        member.getUserId(),
                        member.getUserNicknm(),
                        member.getUserRole().name()
                ))
                .collect(Collectors.toList());

        return new MemberBatchInfoDto(memberInfoList);
    }

    /**
     * 회원 존재 여부 확인
     *
     * @param userId 회원 ID
     * @return 존재 여부
     */
    public boolean memberExists(Long userId) {
        return memberRepository.existsById(userId);
    }
}
