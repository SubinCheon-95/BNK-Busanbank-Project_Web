package kr.co.busanbank.service;

import kr.co.busanbank.dto.SecuritySettingDTO;
import kr.co.busanbank.dto.TermDTO;
import kr.co.busanbank.dto.UsersDTO;
import kr.co.busanbank.mapper.MemberMapper;
import kr.co.busanbank.security.AESUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 수정일: 2025-11-20 (보안 설정 적용 - 진원)
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecuritySettingService securitySettingService;


    /**
     * 회원가입
     * 작성자: 진원, 2025-11-20 (비밀번호 정책 검증 추가)
     */
    public void save(UsersDTO userDTO) throws Exception {
        // 비밀번호 정책 검증
        validatePassword(userDTO.getUserPw());

        String encodedPass = passwordEncoder.encode(userDTO.getUserPw());
        String encodedAccountPass = passwordEncoder.encode(userDTO.getAccountPassword());

        userDTO.setUserPw(encodedPass);
        userDTO.setAccountPassword(encodedAccountPass);

        userDTO.setUserName(AESUtil.encrypt(userDTO.getUserName()));
        userDTO.setHp(AESUtil.encrypt(userDTO.getHp()));
        userDTO.setEmail(AESUtil.encrypt(userDTO.getEmail()));
        userDTO.setRrn(AESUtil.encrypt(userDTO.getRrn()));

        log.info("savedUserDTO = {}", userDTO);

        memberMapper.insertUser(userDTO);
    }


    public int countUser(String type, String value){
        int count = 0;
        if(type.equals("userId")){
            count = memberMapper.countByUserId(value);
        } else if(type.equals("email")){
            count = memberMapper.countByEmail(value);
//            if(count == 0){
//                emailService.sendCode(value);
//            }
        } else if(type.equals("hp")){
            count = memberMapper.countByHp(value);
        }
        return count;
    }

    public UsersDTO getUserIdInfoEmail(String userName, String email) throws Exception {
        String encryptedName = AESUtil.encrypt(userName);
        String encryptedEmail = AESUtil.encrypt(email);
        log.info("encryptedName: {}, encryptedEmail: {}", encryptedName, encryptedEmail);
        return memberMapper.findUserIdInfoEmail(encryptedName, encryptedEmail);
    }


    public UsersDTO getUserIdInfoHp(String userName, String hp) throws Exception {
        String encryptedName = AESUtil.encrypt(userName);
        String encryptedHp = AESUtil.encrypt(hp);
        return memberMapper.findUserIdInfoHp(encryptedName, encryptedHp);
    }

    public UsersDTO getUserPwInfoEmail(String userName, String userId, String email) throws Exception {
        String encryptedName = AESUtil.encrypt(userName);
        String encryptedEmail = AESUtil.encrypt(email);
        log.info("encryptedName: {}, encryptedEmail: {}", encryptedName, encryptedEmail);
        return memberMapper.findUserPwInfoEmail(encryptedName, userId, encryptedEmail);
    }


    public UsersDTO getUserPwInfoHp(String userName, String userId, String hp) throws Exception {
        String encryptedName = AESUtil.encrypt(userName);
        String encryptedHp = AESUtil.encrypt(hp);
        return memberMapper.findUserPwInfoHp(encryptedName, userId, encryptedHp);
    }

    /**
     * 비밀번호 변경
     * 작성자: 진원, 2025-11-20 (비밀번호 정책 검증 추가)
     */
    public void modifyPw(String userId, String userPw){
        // 비밀번호 정책 검증
        validatePassword(userPw);

        String encodedPass = passwordEncoder.encode(userPw);

        memberMapper.updatePw(userId, encodedPass);
    }

    public List<TermDTO> findTermsAll(){
        return memberMapper.getTermsAll();
    }

    /**
     * 비밀번호 정책 검증
     * 작성자: 진원, 2025-11-20
     */
    private void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        }

        try {
            // DB에서 비밀번호 최소 길이 설정 조회
            SecuritySettingDTO minLengthSetting = securitySettingService.getSettingByKey("PASSWORD_MIN_LENGTH");
            if (minLengthSetting != null) {
                int minLength = Integer.parseInt(minLengthSetting.getSettingvalue());

                if (password.length() < minLength) {
                    throw new IllegalArgumentException("비밀번호는 최소 " + minLength + "자 이상이어야 합니다.");
                }
            }
        } catch (NumberFormatException e) {
            log.error("비밀번호 최소 길이 설정 값이 잘못되었습니다: {}", e.getMessage());
            // 기본값 8자 적용
            if (password.length() < 8) {
                throw new IllegalArgumentException("비밀번호는 최소 8자 이상이어야 합니다.");
            }
        }
    }

}
