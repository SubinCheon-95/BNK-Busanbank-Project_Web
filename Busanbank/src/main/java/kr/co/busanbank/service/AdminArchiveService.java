package kr.co.busanbank.service;

import kr.co.busanbank.dto.CsPDFDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import kr.co.busanbank.dto.PageResponseDTO;
import kr.co.busanbank.mapper.AdminArchiveMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminArchiveService {
    private final AdminArchiveMapper adminArchiveMapper;

    @Value("${file.upload.path}")
    private String uploadPath;

    public CsPDFDTO findById(int id) {
        CsPDFDTO dto = adminArchiveMapper.findById(id);
        if(dto.getFile() != null) {
            File f = new File(uploadPath + dto.getFile());
            if(f.exists()) {
                dto.setFileSize(formatFileSize(f.length()));
            }
        }
        return dto;
    }

    private String formatFileSize(long bytes) {
        if(bytes == 0) return "0 Bytes";
        int k = 1024;
        String[] sizes = {"Bytes","KB","MB"};
        int i = (int)(Math.log(bytes)/Math.log(k));
        return Math.round(bytes / Math.pow(k, i) * 100.0) / 100.0 + " " + sizes[i];
    }


    public PageResponseDTO selectAll(PageRequestDTO pageRequestDTO, String groupCode, String archiveCategory) {
        List<CsPDFDTO> dtoList = adminArchiveMapper.findAll(pageRequestDTO,groupCode,archiveCategory);
        int total = adminArchiveMapper.selectCount(pageRequestDTO,groupCode,archiveCategory);

        return PageResponseDTO.<CsPDFDTO>builder()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total(total)
                .build();
    }

    public PageResponseDTO searchAll(PageRequestDTO pageRequestDTO) {
        List<CsPDFDTO> dtoList = adminArchiveMapper.searchAll(pageRequestDTO);
        int total = adminArchiveMapper.searchCount(pageRequestDTO);

        return PageResponseDTO.<CsPDFDTO>builder()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total(total)
                .build();
    }

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

    public void modifyArchive(CsPDFDTO csPDFDTO) throws IOException {
        MultipartFile file = csPDFDTO.getUploadFile();
        if (file != null && !file.isEmpty()) {
            String savedFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadPath);
            Files.createDirectories(path);
            file.transferTo(path.resolve(savedFileName));

            csPDFDTO.setFile(savedFileName);
        }

        adminArchiveMapper.modifyArchive(csPDFDTO);
    }

    public void singleDelete(int id) {adminArchiveMapper.singleDelete(id);}

    public void delete(List<Long> idList) {adminArchiveMapper.delete(idList);}
}
