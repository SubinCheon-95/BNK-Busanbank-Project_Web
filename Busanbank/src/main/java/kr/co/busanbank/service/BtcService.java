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

    public List<UserCouponDTO> couponSearch(int userId) {return btcMapper.findById(userId);}

    public void markUserParticipated(int userNo, int couponId) {
        btcMapper.markUserParticipated(userNo, couponId);
    }
    public void updateEvent(int couponId) {btcMapper.updateEvent(couponId);}
}
