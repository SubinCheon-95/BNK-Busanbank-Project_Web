package kr.co.busanbank.service;

import kr.co.busanbank.dto.CsPDFDTO;
import kr.co.busanbank.mapper.AdminArchiveMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminArchiveService {
    private final AdminArchiveMapper adminArchiveMapper;

    @Value("${file.upload.path}")
    private String uploadPath;

    public void insertPDF(CsPDFDTO csPDFDTO) throws IOException  {
        log.info("csPDFDTO = {}",csPDFDTO);

        MultipartFile file = csPDFDTO.getUploadFile();
        if (file != null && !file.isEmpty()) {
            String savedFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadPath);
            Files.createDirectories(path);
            file.transferTo(path.resolve(savedFileName));

            csPDFDTO.setFile(savedFileName);
        }

        adminArchiveMapper.insertPDF(csPDFDTO);
    }
}
