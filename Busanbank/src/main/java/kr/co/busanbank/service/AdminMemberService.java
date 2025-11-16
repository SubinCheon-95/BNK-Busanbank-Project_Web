package kr.co.busanbank.service;

import kr.co.busanbank.dto.AdminMemberDTO;
import kr.co.busanbank.dto.PageRequestDTO;
import kr.co.busanbank.dto.PageResponseDTO;
import kr.co.busanbank.mapper.AdminMemberMapper;
import kr.co.busanbank.security.AESUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminMemberService {
    private final AdminMemberMapper adminMemberMapper;

    public PageResponseDTO selectAll(PageRequestDTO pageRequestDTO) {
        List<AdminMemberDTO> dtoList = adminMemberMapper.findAll(pageRequestDTO);
        dtoList.forEach(dto -> {
            try {
                String userName = AESUtil.decrypt(dto.getUserName());
                if(userName != null && !userName.isEmpty()){
                    dto.setUserName(userName.substring(0,1) + "**");
                }

                dto.setUserId(dto.getUserId().substring(0,3) + "***");

                String hp = AESUtil.decrypt(dto.getHp());
                if(hp != null && !hp.isEmpty()){
                    String hp1 = hp.substring(0,4);
                    String hp2 = hp.substring(8);
                    dto.setHp(hp1 + "****" + hp2);
                }

            } catch (Exception e) {
                log.error("복호화 실패: " + dto.getUserId(), e);
            }
        });

        int total = adminMemberMapper.selectCount(pageRequestDTO);

        return PageResponseDTO.<AdminMemberDTO>builder()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total(total)
                .build();
    }

    public AdminMemberDTO findByUserNo(int userNo) {
        AdminMemberDTO dto = adminMemberMapper.findByUserNo(userNo);

        if(dto != null) {
            try {
                dto.setUserName(AESUtil.decrypt(dto.getUserName()));
                dto.setHp(AESUtil.decrypt(dto.getHp()));
            } catch (Exception e) {
                log.error("복호화 실패: " + dto.getUserId(), e);
            }
        }
        return dto;
    }
}
