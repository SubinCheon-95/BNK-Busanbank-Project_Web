/*
    날짜 : 2025/11/26
    이름 : 오서정
    내용 : 코모란 서비스 작성
 */

package kr.co.busanbank.service;

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KomoranService {

    private final Komoran komoran;

    public List<String> extractKeywords(String text) {
        KomoranResult result = komoran.analyze(text);
        return result.getMorphesByTags("NNG", "NNP", "NP");
    }
}