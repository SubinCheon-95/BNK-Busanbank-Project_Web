package kr.co.busanbank.service;

import kr.co.busanbank.dto.*;
import kr.co.busanbank.mapper.CsMapper;
import kr.co.busanbank.mapper.MyMapper;
import kr.co.busanbank.security.AESUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CsService {

    private final CsMapper csMapper;
    private final MyMapper myMapper;

    // FAQ
    public PageResponseDTO<FaqDTO> getFaqList(PageRequestDTO pageRequestDTO) {

        List<FaqDTO> dtoList = csMapper.selectFaqList(pageRequestDTO);
        int total = csMapper.selectFaqTotal(pageRequestDTO);

        return PageResponseDTO.<FaqDTO>builder()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total(total)
                .build();
    }

    public List<CodeDetailDTO> getFaqCategories() {
        return csMapper.selectFaqCategories();
    }

    //EMAILCOUNSEL
    public List<CodeDetailDTO> getCsCategories() {
        return csMapper.selectCodeList("CS_TYPE");
    }

    public UsersDTO getUserById(String userId) throws Exception {
        UsersDTO user = myMapper.getUserById(userId);
        if(user == null) return null;

        user.setUserName(AESUtil.decrypt(user.getUserName()));
        user.setEmail(AESUtil.decrypt(user.getEmail()));
        user.setHp(AESUtil.decrypt(user.getHp()));

        return user;
    }

    public void registerEmailCounsel(EmailCounselDTO dto) {
        if (dto.getGroupCode() == null) dto.setGroupCode("CS_TYPE");
        if (dto.getStatus() == null) dto.setStatus("REGISTERED");
        csMapper.insertEmailCounsel(dto);
    }

    // 로그인 회원의 이메일상담 목록
    public List<EmailCounselDTO> getMyEmailCounselList(int userId) {
        return csMapper.selectEmailCounselList(userId);
    }

    // 상담 1건 상세
    public EmailCounselDTO getEmailCounsel(int ecounselId, int userId) {
        return csMapper.selectEmailCounselById(ecounselId, userId);
    }

}
