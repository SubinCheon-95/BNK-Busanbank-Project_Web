package kr.co.busanbank.service;

import kr.co.busanbank.dto.UserCouponDTO;
import kr.co.busanbank.mapper.BtcMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class BtcService {
    private final BtcMapper btcMapper;

    public List<UserCouponDTO> couponSearch(int userNo) {return btcMapper.findById(userNo);}

    public void updateEvent(int couponId) {btcMapper.updateEvent(couponId);}
}
