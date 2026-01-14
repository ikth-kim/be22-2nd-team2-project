package com.team2.commonmodule.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Feign Client용 회원 일괄 조회 응답 DTO
 *
 * <p>여러 회원의 정보를 한 번에 조회할 때 사용합니다.</p>
 *
 * @author MSA Team
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberBatchInfoDto {
    private List<MemberInfoDto> members;
}
