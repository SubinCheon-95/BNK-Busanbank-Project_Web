package kr.co.busanbank.controller;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.busanbank.dto.AdminDTO;
import kr.co.busanbank.dto.UsersDTO;
import kr.co.busanbank.security.AESUtil;
import kr.co.busanbank.security.AdminUserDetails;
import kr.co.busanbank.security.MyUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;


@ControllerAdvice(basePackages = {"kr.co.busanbank.controller"})
@RequiredArgsConstructor
public class GlobalController {


    @ModelAttribute("user")
    public UsersDTO addUserToModel(@AuthenticationPrincipal MyUserDetails myuser) {
        if (myuser != null) {
            UsersDTO userDTO = myuser.getUsersDTO();

            try {
                userDTO.setUserName(AESUtil.decrypt(userDTO.getUserName()));
                userDTO.setHp(AESUtil.decrypt(userDTO.getHp()));
                userDTO.setEmail(AESUtil.decrypt(userDTO.getEmail()));
                userDTO.setRrn(AESUtil.decrypt(userDTO.getRrn()));
            } catch (Exception e) {
                e.printStackTrace();
            }

            return userDTO;
        }
        return new UsersDTO();
    }

    @ModelAttribute("admin")
    public AdminDTO addUserToModel(@AuthenticationPrincipal AdminUserDetails myadmin) {
        if (myadmin != null) {
            return myadmin.getAdminDTO();
        }

        return new AdminDTO();
    }


}