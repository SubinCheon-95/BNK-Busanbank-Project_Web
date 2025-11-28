package kr.co.busanbank.service;

import kr.co.busanbank.dto.InvestDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import kr.co.busanbank.dto.PageResponseDTO;
import kr.co.busanbank.mapper.AdminInvestMapper;
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
public class AdminInvestService {
    private final AdminInvestMapper adminInvestMapper;

    @Value("${file.upload.path}")
    private String uploadPath;

    public InvestDTO findById(int id) {
        InvestDTO dto = adminInvestMapper.findById(id);
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

    public PageResponseDTO selectAll(PageRequestDTO pageRequestDTO, String investType) {
        List<InvestDTO> dtoList = adminInvestMapper.findAll(pageRequestDTO, investType);
        int total = adminInvestMapper.selectCount(pageRequestDTO, investType);

        return PageResponseDTO.<InvestDTO>builder()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total(total)
                .build();
    }

    public PageResponseDTO searchAll(PageRequestDTO pageRequestDTO) {
        List<InvestDTO> dtoList = adminInvestMapper.searchAll(pageRequestDTO);
        int total = adminInvestMapper.searchCount(pageRequestDTO);

        return PageResponseDTO.<InvestDTO>builder()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total(total)
                .build();
    }

    public void insertPDF(InvestDTO investDTO) throws IOException {
        log.info("investDTO = {}",investDTO);

        MultipartFile file = investDTO.getUploadFile();
        if (file != null && !file.isEmpty()) {
            String savedFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadPath);
            Files.createDirectories(path);
            file.transferTo(path.resolve(savedFileName));

            investDTO.setFile(savedFileName);
        }

        adminInvestMapper.insertPDF(investDTO);
    }

    public void modifyInvest(InvestDTO investDTO) throws IOException {
        MultipartFile file = investDTO.getUploadFile();
        if (file != null && !file.isEmpty()) {
            String savedFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadPath);
            Files.createDirectories(path);
            file.transferTo(path.resolve(savedFileName));

            investDTO.setFile(savedFileName);
        }
        adminInvestMapper.modifyInvest(investDTO);
    }

    public void singleDelete(int id) {adminInvestMapper.singleDelete(id);}
    public void delete(List<Long> idList) {adminInvestMapper.delete(idList);}
}
