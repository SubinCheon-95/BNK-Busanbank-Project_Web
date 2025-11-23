package kr.co.busanbank.controller.admin;

import kr.co.busanbank.dto.PriceHistoryDTO;
import kr.co.busanbank.mapper.PriceHistoryMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/coin")
public class CoinHistoryController {

    private final PriceHistoryMapper priceHistoryMapper;

    public CoinHistoryController(PriceHistoryMapper priceHistoryMapper) {
        this.priceHistoryMapper = priceHistoryMapper;
    }

    @GetMapping("/history/btc")
    public List<PriceHistoryDTO> getBtc(@RequestParam String symbol) {
        return priceHistoryMapper.getBtc(symbol);
    }

    @GetMapping("/history/gold")
    public List<PriceHistoryDTO> getGold(@RequestParam String symbol) {
        return priceHistoryMapper.getGold(symbol);
    }

    @GetMapping("/history/oil")
    public List<PriceHistoryDTO> getOil(@RequestParam String symbol) {
        return priceHistoryMapper.getOil(symbol);
    }
}