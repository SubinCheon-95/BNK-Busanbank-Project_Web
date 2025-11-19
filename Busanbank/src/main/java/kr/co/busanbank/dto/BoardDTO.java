package kr.co.busanbank.dto;

import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardDTO {
    private int id;
    private String boardType;
    private String title;
    private String content;
    private String createdAt;
    private String updatedAt;
    private String status;

    // DB에 저장할 파일명
    private String file;

    @Transient // JPA 사용 시 DB 매핑 제외
    private MultipartFile uploadFile;
}
