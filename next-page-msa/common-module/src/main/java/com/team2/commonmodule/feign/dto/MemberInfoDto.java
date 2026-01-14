package com.team2.commonmodule.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Feign Client용 회원 정보 DTO
 *
 * <p>다른 MSA 서비스가 회원 정보를 조회할 때 사용하는 간소화된 DTO입니다.</p>
 *
 * @author MSA Team
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberInfoDto {
    private Long userId;
    private String userNicknm;
    private String userRole;
}
