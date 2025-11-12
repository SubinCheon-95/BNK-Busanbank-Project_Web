package kr.co.busanbank.project.service;

import kr.co.busanbank.project.dto.UsersDTO;
import kr.co.busanbank.project.mapper.MemberMapper;
import kr.co.busanbank.project.security.AESUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberMapper memberMapper;


    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;


    public void save(UsersDTO userDTO) throws Exception {

        String encodedPass = passwordEncoder.encode(userDTO.getUserPw());
        String encodedAccountPass = passwordEncoder.encode(userDTO.getAccountPassword());

        userDTO.setUserPw(encodedPass);
        userDTO.setAccountPassword(encodedAccountPass);

        String key = AESUtil.generateKey();

        String encryptedName = AESUtil.encrypt(userDTO.getUserName(), key);
        userDTO.setUserName(encryptedName);

        String encryptedHp = AESUtil.encrypt(userDTO.getHp(), key);
        userDTO.setHp(encryptedHp);

        String encryptedEmail = AESUtil.encrypt(userDTO.getEmail(), key);
        userDTO.setEmail(encryptedEmail);

        String encryptedRrn = AESUtil.encrypt(userDTO.getRrn(), key);
        userDTO.setRrn(encryptedRrn);


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

    public UsersDTO login(String userId, String userPw) {
        return memberMapper.login(userId, userPw);
    }



}
