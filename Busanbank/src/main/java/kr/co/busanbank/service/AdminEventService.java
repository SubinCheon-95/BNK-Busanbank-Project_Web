package kr.co.busanbank.service;

import kr.co.busanbank.dto.BoardDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import kr.co.busanbank.dto.PageResponseDTO;
import kr.co.busanbank.mapper.AdminEventMapper;
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
public class AdminEventService {
    private final AdminEventMapper adminEventMapper;

    @Value("${file.upload.path}")
    private String uploadPath;

    public BoardDTO findById(int id) {return adminEventMapper.findById(id);}

    public PageResponseDTO selectAll(PageRequestDTO pageRequestDTO) {
        List<BoardDTO> dtoList = adminEventMapper.findAll(pageRequestDTO);
        int total = adminEventMapper.selectCount(pageRequestDTO);

        return PageResponseDTO.<BoardDTO>builder()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total(total)
                .build();
    }

    public PageResponseDTO searchAll(PageRequestDTO pageRequestDTO) {
        List<BoardDTO> dtoList = adminEventMapper.searchAll(pageRequestDTO);
        int total = adminEventMapper.searchCountTotal(pageRequestDTO);

        return PageResponseDTO.<BoardDTO>builder()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total(total)
                .build();
    }

    public void insertEvent(BoardDTO boardDTO) throws IOException  {
        MultipartFile file = boardDTO.getUploadFile();
        if (file != null && !file.isEmpty()) {
            String savedFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadPath);
            Files.createDirectories(path);
            file.transferTo(path.resolve(savedFileName));

            boardDTO.setFile(savedFileName);
        }

        adminEventMapper.insertEvent(boardDTO);
    }

    public void modifyEvent(BoardDTO boardDTO) throws IOException {
        MultipartFile file = boardDTO.getUploadFile();
        if (file != null && !file.isEmpty()) {
            String savedFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadPath);
            Files.createDirectories(path);
            file.transferTo(path.resolve(savedFileName));

            boardDTO.setFile(savedFileName);
        }
        adminEventMapper.modifyEvent(boardDTO);
    }

    public void singleDelete(int id) {adminEventMapper.singleDelete(id);}
    public void delete(List<Long> idList) {adminEventMapper.delete(idList);}
}
