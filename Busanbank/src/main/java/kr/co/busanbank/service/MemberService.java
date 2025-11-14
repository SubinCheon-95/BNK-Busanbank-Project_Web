package kr.co.busanbank.service;

import kr.co.busanbank.dto.UsersDTO;
import kr.co.busanbank.mapper.MemberMapper;
import kr.co.busanbank.security.AESUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
}