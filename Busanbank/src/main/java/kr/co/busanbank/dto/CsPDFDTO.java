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
public class CsPDFDTO {
    private int id;
    private String pdfType;
    private String title;
    private String file;

    @Transient
    private MultipartFile uploadFile;
}
