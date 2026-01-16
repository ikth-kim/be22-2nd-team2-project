package com.team2.memberservice.query.member.mapper;

import com.team2.memberservice.query.member.dto.response.MemberDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.Optional;

/**
 * 회원 Query Mapper (MyBatis)
 *
 * @author 김태형
 */
@Mapper
public interface MemberMapper {

    /**
     * 이메일로 회원 기본 정보 조회
     */
    Optional<MemberDto> findByUserEmail(@Param("userEmail") String userEmail);
}
