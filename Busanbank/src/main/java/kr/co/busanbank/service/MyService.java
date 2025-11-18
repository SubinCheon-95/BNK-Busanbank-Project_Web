package kr.co.busanbank.service;


import kr.co.busanbank.dto.UserProductDTO;
import kr.co.busanbank.dto.UsersDTO;
import kr.co.busanbank.mapper.MemberMapper;
import kr.co.busanbank.mapper.MyMapper;
import kr.co.busanbank.security.AESUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class MyService {
    private final MyMapper myMapper;
    private final PasswordEncoder passwordEncoder;

    public int countUserItems(String userId) {
        return myMapper.countUserItems(userId);
    }

    public String findProductRecentlyDate(String userId) {
        return myMapper.getProductRecentlyDate(userId);
    }

    public String findProductLastDate(String userId) {
        return myMapper.getProductLastDate(userId);
    }





    public boolean modifyInfo(String userId, String email, String hp, String zip, String addr1, String addr2) throws Exception {
        String encryptedEmail = AESUtil.encrypt(email);
        String encryptedHp = AESUtil.encrypt(hp);

        int updatedRows = myMapper.updateInfo(userId, encryptedEmail, encryptedHp, zip, addr1, addr2);
        return updatedRows > 0;

    }

    public UsersDTO getUserById(String userId) {
        return myMapper.getUserById(userId);
    }


    public Boolean findUserPw(String userId, String pw) {
        // DB에서 암호화된 비밀번호 가져오기
        String dbEncodedPw = myMapper.getUserPwById(userId);
        // passwordEncoder.matches(raw, encoded) 사용
        return passwordEncoder.matches(pw, dbEncodedPw);
    }

    public void withdrawUser(String userId) {
        myMapper.deleteUser(userId);
    }

    public void modifyPw(String userId, String userPw){
        String encodedPass = passwordEncoder.encode(userPw);
        myMapper.updatePw(userId, encodedPass);
    }

    public List<UserProductDTO> findUserProducts(String userId) {
        return myMapper.getUserProducts(userId);
    }

    public List<UserProductDTO> findUserProductNames(String userId) {
        return myMapper.getUserProductNames(userId);
    }

    public void removeProduct(String userId, String productNo){
        myMapper.deleteProduct(userId, productNo);
    }

    public UserProductDTO findCancelProduct(String userId, String productNo){
        return myMapper.getCancelProduct(userId, productNo);
    }
}
