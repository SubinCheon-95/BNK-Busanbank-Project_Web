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

    public void insertReport(BoardDTO boardDTO) throws IOException {
        MultipartFile file = boardDTO.getUploadFile();
        if (file != null && !file.isEmpty()) {
            String savedFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadPath);
            Files.createDirectories(path);
            file.transferTo(path.resolve(savedFileName));

            boardDTO.setFile(savedFileName);
        }

        adminreportMapper.insertReport(boardDTO);
    }

    public void modifyReport(BoardDTO boardDTO) throws IOException {
        MultipartFile file = boardDTO.getUploadFile();
        if (file != null && !file.isEmpty()) {
            String savedFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadPath);
            Files.createDirectories(path);
            file.transferTo(path.resolve(savedFileName));

            boardDTO.setFile(savedFileName);
        }

        adminreportMapper.modifyReport(boardDTO);
    }

    public void singleDelete(int id) {adminreportMapper.singleDelete(id);}

    public void delete(List<Long> idList) {adminreportMapper.delete(idList);}
}
