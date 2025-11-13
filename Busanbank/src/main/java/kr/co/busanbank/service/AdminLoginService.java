package kr.co.busanbank.service;


import kr.co.busanbank.dto.AdminDTO;
import kr.co.busanbank.mapper.AdminLoginMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminLoginService {

    private final AdminLoginMapper adminLoginMapper;

}
