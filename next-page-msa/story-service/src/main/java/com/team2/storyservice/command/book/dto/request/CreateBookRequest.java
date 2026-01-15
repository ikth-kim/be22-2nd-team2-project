package com.team2.storyservice.command.book.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 소설 생성 요청 DTO
 *
 * @author 정진호
 */
@Getter
@NoArgsConstructor
public class CreateBookRequest {

    @NotBlank(message = "제목은 필수 입력값입니다.")
    @Size(min = 1, max = 200, message = "제목은 1자 이상 200자 이하여야 합니다.")
    private String title;

    @NotBlank(message = "카테고리는 필수 입력값입니다.")
    private String categoryId;

    @NotNull(message = "최대 문장 수는 필수 입력값입니다.")
    @Min(value = 10, message = "최대 문장 수는 10 이상이어야 합니다.")
    @Max(value = 100, message = "최대 문장 수는 100 이하여야 합니다.")
    private Integer maxSequence;

    @NotBlank(message = "첫 문장은 필수 입력값입니다.")
    @Size(min = 1, max = 200, message = "첫 문장은 1자 이상 200자 이하여야 합니다.")
    private String firstSentence;
}
