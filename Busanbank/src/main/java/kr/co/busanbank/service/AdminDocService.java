package kr.co.busanbank.service;

import kr.co.busanbank.dto.DocumentsDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import kr.co.busanbank.dto.PageResponseDTO;
import kr.co.busanbank.mapper.AdminDocMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminDocService {
    private final AdminDocMapper adminDocMapper;

    public DocumentsDTO findById(int docId) {return adminDocMapper.findById(docId);}

    public PageResponseDTO selectAll(PageRequestDTO pageRequestDTO, String groupCode, String docCategory) {
        List<DocumentsDTO> dtoList = adminDocMapper.findAll(pageRequestDTO, groupCode, docCategory);
        int total = adminDocMapper.selectCount(pageRequestDTO, groupCode, docCategory);

        return PageResponseDTO.<DocumentsDTO>builder()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total(total)
                .build();
    }

    public PageResponseDTO searchAll(PageRequestDTO pageRequestDTO) {
        List<DocumentsDTO> dtoList = adminDocMapper.searchAll(pageRequestDTO);
        int total = adminDocMapper.searchCount(pageRequestDTO);

        return PageResponseDTO.<DocumentsDTO>builder()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total(total)
                .build();
    }

    public void insertDoc(DocumentsDTO documentsDTO){
        log.info("documentsDTO = {}",documentsDTO);
        adminDocMapper.insertDoc(documentsDTO);
    }

    public void modifyDoc(DocumentsDTO documentsDTO){
        adminDocMapper.modifyDoc(documentsDTO);
    }

    public void singleDelete(int docId) {adminDocMapper.singleDelete(docId);}

    public void delete(List<Long> idList) {adminDocMapper.delete(idList);}
}
