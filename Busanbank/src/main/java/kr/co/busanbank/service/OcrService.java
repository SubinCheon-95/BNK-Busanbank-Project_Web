package kr.co.busanbank.service;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class OcrService {

    private final ITesseract tesseract;

    public OcrService() {
        tesseract = new Tesseract();

        // 실제 절대경로로 변환 (Spring Boot에서도 100% 동작)
        String path = new File("src/main/resources/tessdata").getAbsolutePath();
        tesseract.setDatapath(path);

        // kor.traineddata + eng.traineddata 사용
        tesseract.setLanguage("kor+eng");
    }

    public String extractText(MultipartFile file) throws Exception {

        // temp 파일 생성
        File temp = File.createTempFile("ocr_", ".png");
        file.transferTo(temp);

        try {
            return tesseract.doOCR(temp);
        } catch (TesseractException e) {
            throw new RuntimeException("OCR 처리 중 오류 발생: " + e.getMessage());
        } finally {
            temp.delete();
        }
    }
}
