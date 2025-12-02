package kr.co.busanbank.service;

import kr.co.busanbank.dto.DisclosureDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import kr.co.busanbank.dto.PageResponseDTO;
import kr.co.busanbank.mapper.AdminDisclosureMapper;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminDisclosureService {
    private final AdminDisclosureMapper adminDisclosureMapper;

    @Value("${file.upload.path}")
    private String uploadPath;

    public DisclosureDTO findById(int id) {
        DisclosureDTO dto = adminDisclosureMapper.findById(id);

        List<String> fileList = new ArrayList<>();
        long totalSize = 0;

        // file, term1, term2, term3 모두 체크해서 fileList에 추가
        if(dto.getFile() != null && !dto.getFile().isEmpty()) {
            fileList.add(dto.getFile());
            File f = new File(uploadPath + dto.getFile());
            if(f.exists()) {
                totalSize += f.length();
            }
        }

        if(dto.getTerm1() != null && !dto.getTerm1().isEmpty()) {
            fileList.add(dto.getTerm1());
            File f = new File(uploadPath + dto.getTerm1());
            if(f.exists()) {
                totalSize += f.length();
            }
        }

        if(dto.getTerm2() != null && !dto.getTerm2().isEmpty()) {
            fileList.add(dto.getTerm2());
            File f = new File(uploadPath + dto.getTerm2());
            if(f.exists()) {
                totalSize += f.length();
            }
        }

        if(dto.getTerm3() != null && !dto.getTerm3().isEmpty()) {
            fileList.add(dto.getTerm3());
            File f = new File(uploadPath + dto.getTerm3());
            if(f.exists()) {
                totalSize += f.length();
            }
        }

        // fileList가 비어있지 않으면 설정
        if(!fileList.isEmpty()) {
            dto.setFileList(fileList);
            dto.setFileCount(fileList.size());
            dto.setTotalFileSize(totalSize);
            dto.setFileSize(formatFileSize(totalSize));
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

    public PageResponseDTO selectAll(PageRequestDTO pageRequestDTO, String groupCode, String disclosureCategory) {
        List<DisclosureDTO> dtoList = adminDisclosureMapper.findAll(pageRequestDTO,groupCode,disclosureCategory);
        int total = adminDisclosureMapper.selectCount(pageRequestDTO,groupCode,disclosureCategory);

        return PageResponseDTO.<DisclosureDTO>builder()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total(total)
                .build();
    }

    public PageResponseDTO searchAll(PageRequestDTO pageRequestDTO) {
        List<DisclosureDTO> dtoList = adminDisclosureMapper.searchAll(pageRequestDTO);
        int total = adminDisclosureMapper.searchCount(pageRequestDTO);

        return PageResponseDTO.<DisclosureDTO>builder()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total(total)
                .build();
    }

    public void insertPDF(DisclosureDTO disclosureDTO) throws IOException {
        List<MultipartFile> files = disclosureDTO.getUploadFile();

        if (files != null && !files.isEmpty()) {
            Path path = Paths.get(uploadPath);
            Files.createDirectories(path);

            // 2. 파일 목록을 순회하며 DTO 필드에 파일 경로를 설정합니다.
            for (int i = 0; i < files.size(); i++) {
                log.info("파일 순서 테스트 = {}", files.get(i).getOriginalFilename());
                MultipartFile file = files.get(i);
                if (file.isEmpty()) continue;
                if (i >= 4) break; // 최대 4개 파일만 처리

                String savedFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                file.transferTo(path.resolve(savedFileName));

                // DTO의 해당하는 필드에 파일 경로 설정
                if (i == 0) {
                    disclosureDTO.setTerm3(savedFileName); // TERM3
                } else if (i == 1) {
                    disclosureDTO.setTerm2(savedFileName); // TERM2
                } else if (i == 2) {
                    disclosureDTO.setTerm1(savedFileName); // TERM1
                } else if (i == 3) {
                    disclosureDTO.setFile(savedFileName); // PDFFILE (file)
                }
            }
        } else {
            log.warn("저장할 첨부 파일이 없습니다.");
        }

        // 3. ⭐️ DTO에 모든 정보(메타데이터 + 파일 경로)를 담아 Mapper를 한 번만 호출
        log.info("테스트 disclosureDTO = {}",disclosureDTO);
        adminDisclosureMapper.insertPDF(disclosureDTO);
    }

    public void  modifyDisclosure(DisclosureDTO disclosureDTO) throws IOException {
        List<MultipartFile> files = disclosureDTO.getUploadFile();

        if (files != null && !files.isEmpty()) {
            Path path = Paths.get(uploadPath);
            Files.createDirectories(path);

            // 2. 파일 목록을 순회하며 DTO 필드에 파일 경로를 설정합니다.
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                if (file.isEmpty()) continue;
                if (i >= 4) break; // 최대 4개 파일만 처리

                String savedFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                file.transferTo(path.resolve(savedFileName));

                // DTO의 해당하는 필드에 파일 경로 설정
                if (i == 0) {
                    disclosureDTO.setFile(savedFileName); // PDFFILE (file)
                } else if (i == 1) {
                    disclosureDTO.setTerm1(savedFileName); // TERM1
                } else if (i == 2) {
                    disclosureDTO.setTerm2(savedFileName); // TERM2
                } else if (i == 3) {
                    disclosureDTO.setTerm3(savedFileName); // TERM3
                }
            }
        } else {
            log.warn("저장할 첨부 파일이 없습니다.");
        }

        adminDisclosureMapper.modifyDisclosure(disclosureDTO);
    }

    public void singleDelete(int id) {adminDisclosureMapper.singleDelete(id);}

    public void delete(List<Long> idList) {adminDisclosureMapper.delete(idList);}
}
