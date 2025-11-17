package kr.co.busanbank.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeDetailDTO {

    private String groupCode;   // FAQ_TYPE
    private String code;        // 01,02,...
    private String codeName;
}
