package kr.co.busanbank.service;

import kr.co.busanbank.dto.EmailCounselDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import kr.co.busanbank.dto.PageResponseDTO;
import kr.co.busanbank.mapper.AdminEmailMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminEmailService {
    private final AdminEmailMapper adminEmailMapper;

    public EmailCounselDTO findById(int ecounselId) {return adminEmailMapper.findById(ecounselId);}

    public PageResponseDTO selectAll(PageRequestDTO pageRequestDTO) {
        List<EmailCounselDTO> dtoList = adminEmailMapper.findAll(pageRequestDTO);
        int total = adminEmailMapper.selectCount(pageRequestDTO);

        return PageResponseDTO.<EmailCounselDTO>builder()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total(total)
                .build();
    }

    public PageResponseDTO searchAll(PageRequestDTO pageRequestDTO) {
        List<EmailCounselDTO> dtoList = adminEmailMapper.searchAll(pageRequestDTO);
        int total = adminEmailMapper.searchCount(pageRequestDTO);

        return PageResponseDTO.<EmailCounselDTO>builder()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total(total)
                .build();
    }

    public void insertEmail(EmailCounselDTO emailCounselDTO) {
        emailCounselDTO.setStatus("complete");
        adminEmailMapper.insertEmail(emailCounselDTO);
    }

    public void modifyEmail(EmailCounselDTO emailCounselDTO) {
        adminEmailMapper.modifyEmail(emailCounselDTO);
    }
}
