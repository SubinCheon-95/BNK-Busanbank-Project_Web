package kr.co.busanbank.service;

import kr.co.busanbank.dto.SecuritySettingDTO;
import kr.co.busanbank.dto.TermDTO;
import kr.co.busanbank.dto.UsersDTO;
import kr.co.busanbank.mapper.MemberMapper;
import kr.co.busanbank.mapper.TermMapper;
import kr.co.busanbank.security.AESUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 수정일: 2025-11-20 (보안 설정 적용 - 진원)
 * 수정일: 2025-11-26 (약관 조회 메서드 추가 - 진원)
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberMapper memberMapper;
    private final TermMapper termMapper;
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

    /* 2025/12/05
     * 인증 관련 로직 수정
     * 작성자: 오서정 2025-11-20
     */
    // 2025/12/05 – CBC 적용 관련 로직 수정 – 작성자: 오서정
    public int countUser(String type, String plainValue){
        try {

            // userId는 암호화X → 기존 방식 유지
            if(type.equals("userId")){
                return memberMapper.countByUserId(plainValue);
            }

            // 나머지는 CBC 복호화 비교
            List<String> cipherList = switch (type) {
                case "email" -> memberMapper.selectAllEmails();
                case "hp"    -> memberMapper.selectAllHps();
                case "userName" -> memberMapper.selectAllUserNames();
                default -> null;
            };

            if(cipherList == null) return 0;

            int count = 0;

            for(String cipher : cipherList){
                if(cipher == null) continue;

                try {
                    String decrypted = AESUtil.decrypt(cipher);
                    if(plainValue.equals(decrypted)){
                        count++;
                    }
                } catch (Exception ignore){}
            }

            return count;

        } catch (Exception e){
            log.error("countUser error", e);
            return 0;
        }
    }


    public UsersDTO getUserIdInfoEmail(String userName, String email) throws Exception {

        // 1) userName 매칭되는 user 목록 조회
        List<UsersDTO> list = memberMapper.selectAllForIdFind();

        for(UsersDTO user : list){
            String decName  = AESUtil.decrypt(user.getUserName());
            String decEmail = AESUtil.decrypt(user.getEmail());

            if(decName.equals(userName) && decEmail.equals(email)){
                return user; // 찾음!
            }
        }

        return null; // 못 찾음
    }



    public UsersDTO getUserIdInfoHp(String userName, String hp) throws Exception {

        List<UsersDTO> list = memberMapper.selectAllForIdFind();
        // userId, userName, hp 포함된 모든 사용자 조회

        for (UsersDTO user : list) {

            String decName = AESUtil.decrypt(user.getUserName());
            String decHp   = AESUtil.decrypt(user.getHp());

            if (decName.equals(userName) && decHp.equals(hp)) {
                return user; // 찾았음
            }
        }

        return null; // 없음
    }

    public UsersDTO getUserPwInfoEmail(String userName, String userId, String email) throws Exception {

        List<UsersDTO> list = memberMapper.selectAllForPwFind();

        for(UsersDTO user : list){
            String decName  = AESUtil.decrypt(user.getUserName());
            String decEmail = AESUtil.decrypt(user.getEmail());

            if(decName.equals(userName) && decEmail.equals(email) && user.getUserId().equals(userId)){
                return user;
            }
        }

        return null;
    }



    public UsersDTO getUserPwInfoHp(String userName, String userId, String hp) throws Exception {

        List<UsersDTO> list = memberMapper.selectAllForPwFind();
        // userId, userName, hp 포함된 사용자 조회

        for (UsersDTO user : list) {

            String decName = AESUtil.decrypt(user.getUserName());
            String decHp   = AESUtil.decrypt(user.getHp());

            if (decName.equals(userName) && decHp.equals(hp) && user.getUserId().equals(userId)) {
                return user; // OK
            }
        }

        return null;
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
     * 약관 ID로 조회
     * 작성자: 진원, 2025-11-26
     */
    public TermDTO findTermById(int termNo) {
        return termMapper.selectTermById(termNo);
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

    /**
     * 시스템에 비밀번호가 평문으로 오염되서 DB에서 직접 암호화문가지고 오는 거
     * 작성자: 수진, 2025/11/27
     */
//    public String getAccountPasswordFromDB(Long userNo) {
//        return memberMapper.findAccountPasswordByUserNo(userNo);
//    }

}
