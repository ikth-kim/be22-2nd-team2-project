package com.team2.nextpage.query.member.mapper;

import com.team2.nextpage.query.member.dto.response.MemberDto;
import org.apache.ibatis.annotations.Mapper;
import java.util.Optional;

/**
 * 회원 Query Mapper (MyBatis)
 *
 * @author 김태형
 */
@Mapper
public interface MemberMapper {

    /**
     * 이메일로 회원 조회
     */
    Optional<MemberDto> findByUserEmail(String userEmail);
}
