package kr.co.busanbank.service;

import kr.co.busanbank.dto.BoardDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import kr.co.busanbank.dto.PageResponseDTO;
import kr.co.busanbank.mapper.AdminNoticeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminNoticeService {
    private final AdminNoticeMapper adminNoticeMapper;

    public BoardDTO findById(int id) {return adminNoticeMapper.findById(id);}

    public PageResponseDTO selectAll(PageRequestDTO pageRequestDTO) {
        List<BoardDTO> dtoList = adminNoticeMapper.findAll(pageRequestDTO);
        int total = adminNoticeMapper.selectCount(pageRequestDTO);

        return PageResponseDTO.<BoardDTO>builder()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total(total)
                .build();
    }

    public PageResponseDTO searchAll(PageRequestDTO pageRequestDTO) {
        List<BoardDTO> dtoList = adminNoticeMapper.searchAll(pageRequestDTO);
        int total = adminNoticeMapper.searchCountTotal(pageRequestDTO);

        return PageResponseDTO.<BoardDTO>builder()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total(total)
                .build();
    }

    public void insertNotice(BoardDTO boardDTO) {
        adminNoticeMapper.insertNotice(boardDTO);
    }

    public void modifyNotice(BoardDTO boardDTO) {
        adminNoticeMapper.modifyNotice(boardDTO);
    }
    public void singleDelete(int id) {adminNoticeMapper.singleDelete(id);}
    public void delete(List<Long> idList) {adminNoticeMapper.delete(idList);}
}
