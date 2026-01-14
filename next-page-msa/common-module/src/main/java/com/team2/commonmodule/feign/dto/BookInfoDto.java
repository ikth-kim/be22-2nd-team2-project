package com.team2.commonmodule.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Feign Client용 소설 정보 DTO
 *
 * <p>다른 MSA 서비스가 소설 정보를 조회할 때 사용하는 간소화된 DTO입니다.</p>
 *
 * @author MSA Team
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookInfoDto {
    private Long bookId;
    private String title;
    private Long writerId;
    private String status;
}
