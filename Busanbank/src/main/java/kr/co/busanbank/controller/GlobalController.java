package kr.co.busanbank.controller;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import kr.co.busanbank.dto.AdminDTO;
import kr.co.busanbank.dto.UsersDTO;
import kr.co.busanbank.security.AESUtil;
import kr.co.busanbank.security.AdminUserDetails;
import kr.co.busanbank.security.MyUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Slf4j
@ControllerAdvice(basePackages = {"kr.co.busanbank.controller"})
@RequiredArgsConstructor
public class GlobalController {


    @ModelAttribute("user")
    public UsersDTO addUserToModel(@AuthenticationPrincipal MyUserDetails myuser, HttpSession session) {
        if (myuser == null) {
            return new UsersDTO();
        }

        UsersDTO sessionUser = (UsersDTO) session.getAttribute("decryptedUser");
        if (sessionUser != null) {
            return sessionUser;
        }

        UsersDTO userDTO = myuser.getUsersDTO();
        try {
            userDTO.setUserName(AESUtil.decrypt(userDTO.getUserName()));
            userDTO.setHp(AESUtil.decrypt(userDTO.getHp()));
            userDTO.setEmail(AESUtil.decrypt(userDTO.getEmail()));
            userDTO.setRrn(AESUtil.decrypt(userDTO.getRrn()));


            // 주민등록번호에서 생년월일, 성별 추출
            String rrn = userDTO.getRrn();
            if (rrn != null && rrn.length() >= 7) {
                String birthPart = rrn.substring(0, 6);
                String genderCode = rrn.substring(6, 7);

                String yearPrefix = ("1".equals(genderCode) || "2".equals(genderCode)) ? "19" : "20";
                String birthFormatted = yearPrefix + birthPart.substring(0, 2) + "-"
                        + birthPart.substring(2, 4) + "-"
                        + birthPart.substring(4, 6);
                userDTO.setBirth(birthFormatted);

                String gender = ("1".equals(genderCode) || "3".equals(genderCode)) ? "남성" : "여성";
                userDTO.setGender(gender);

                if (userDTO.getRegDate() != null) {
                    String regDateStr = userDTO.getRegDate();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime regDateTime = LocalDateTime.parse(regDateStr, formatter);
                    LocalDate regDate = regDateTime.toLocalDate();
                    long days = ChronoUnit.DAYS.between(regDate, LocalDate.now());
                    userDTO.setRegDays(days);
                }
            }

            log.info("userDTO = {}", userDTO);

            session.setAttribute("decryptedUser", userDTO);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return userDTO;
    }



    @ModelAttribute("admin")
    public AdminDTO addUserToModel(@AuthenticationPrincipal AdminUserDetails myadmin) {
        if (myadmin != null) {
            return myadmin.getAdminDTO();
        }

        return new AdminDTO();
    }


}