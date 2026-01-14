package com.team2.nextpage.command.book.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateBookRequest {
    @NotBlank(message = "제목은 필수입니다.")
    private String title;
}
