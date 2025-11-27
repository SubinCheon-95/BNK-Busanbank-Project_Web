package kr.co.busanbank.service;

import kr.co.busanbank.dto.BoardDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import kr.co.busanbank.dto.PageResponseDTO;
import kr.co.busanbank.mapper.AdminreportMapper;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminReportService {
    private final AdminreportMapper adminreportMapper;

    @Value("${file.upload.path}")
    private String uploadPath;

    public BoardDTO findById(int id) {return adminreportMapper.findById(id);}

    public PageResponseDTO selectAll(PageRequestDTO pageRequestDTO) {
        List<BoardDTO> dtoList = adminreportMapper.findAll(pageRequestDTO);
        int total = adminreportMapper.selectCount(pageRequestDTO);

        return PageResponseDTO.<BoardDTO>builder()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total(total)
                .build();
    }

    public PageResponseDTO searchAll(PageRequestDTO pageRequestDTO) {
        List<BoardDTO> dtoList = adminreportMapper.searchAll(pageRequestDTO);
        int total = adminreportMapper.searchCountTotal(pageRequestDTO);

        return PageResponseDTO.<BoardDTO>builder()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total(total)
                .build();
    }

    public void insertReport(BoardDTO boardDTO) throws IOException {
        MultipartFile file = boardDTO.getUploadFile();
        if (file != null && !file.isEmpty()) {
            try {
                // 디렉토리 생성
                Path uploadDir = Paths.get(uploadPath);
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                    log.info("업로드 디렉토리 생성: {}", uploadDir);
                }

                // 파일명 생성 및 저장
                String originalFilename = file.getOriginalFilename();
                String savedFileName = UUID.randomUUID() + "_" + originalFilename;
                Path filePath = uploadDir.resolve(savedFileName);

                file.transferTo(filePath.toFile());
                log.info("파일 저장 완료: {}", filePath);

                boardDTO.setFile(savedFileName);
            } catch (IOException e) {
                log.error("파일 저장 실패: {}", e.getMessage(), e);
                throw e;
            }
        }

        adminreportMapper.insertReport(boardDTO);
    }

    public void modifyReport(BoardDTO boardDTO) throws IOException {
        MultipartFile file = boardDTO.getUploadFile();
        if (file != null && !file.isEmpty()) {
            try {
                Path uploadDir = Paths.get(uploadPath);
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                String savedFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path filePath = uploadDir.resolve(savedFileName);

                file.transferTo(filePath.toFile());
                log.info("파일 수정 저장 완료: {}", filePath);

                boardDTO.setFile(savedFileName);
            } catch (IOException e) {
                log.error("파일 수정 실패: {}", e.getMessage(), e);
                throw e;
            }
        }

        adminreportMapper.modifyReport(boardDTO);
    }

    public void singleDelete(int id) {adminreportMapper.singleDelete(id);}

    public void delete(List<Long> idList) {adminreportMapper.delete(idList);}
}
