package kr.co.busanbank.dto;

import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisclosureDTO {
    private int id;
    private String groupCode;
    private String disclosureCategory;
    private String title;
    private String file;
    private String term1;
    private String term2;
    private String term3;
    private String createdAt;

    @Transient
    private List<MultipartFile> uploadFile;

    private String codeName;

    @Transient
    private String fileSize;
}
