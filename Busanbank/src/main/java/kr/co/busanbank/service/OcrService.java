package kr.co.busanbank.service;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

@Service
public class OcrService {

    private final ITesseract tesseract;

    public OcrService() {
        tesseract = new Tesseract();
        String path = new File("src/main/resources/tessdata").getAbsolutePath();
        tesseract.setDatapath(path);
        tesseract.setLanguage("kor+eng");

        System.out.println("=============================================");
        System.out.println("✅ Tesseract 초기화 완료");
        System.out.println("📁 데이터 경로: " + path);

        File tessDataDir = new File(path);
        if (tessDataDir.exists() && tessDataDir.isDirectory()) {
            File korFile = new File(tessDataDir, "kor.traineddata");
            File engFile = new File(tessDataDir, "eng.traineddata");

            System.out.println("✅ kor.traineddata: " + (korFile.exists() ? "존재 ✓" : "❌ 없음"));
            System.out.println("✅ eng.traineddata: " + (engFile.exists() ? "존재 ✓" : "❌ 없음"));
        } else {
            System.err.println("❌ tessdata 폴더를 찾을 수 없습니다!");
        }
        System.out.println("=============================================");
    }

    public String extractText(MultipartFile file) throws Exception {

        System.out.println("========================================");
        System.out.println("🔍 OCR 시작");
        System.out.println("📄 파일명: " + file.getOriginalFilename());
        System.out.println("📦 크기: " + file.getSize() + " bytes");
        System.out.println("🏷️ Content-Type: " + file.getContentType());

        try {
            // ✅ 방법 1: BufferedImage로 변환 (헤더 손상 방지)
            System.out.println("🖼️ BufferedImage 변환 시작...");
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());

            if (bufferedImage == null) {
                System.err.println("❌ 이미지를 읽을 수 없습니다!");
                throw new RuntimeException("이미지 파일을 읽을 수 없습니다");
            }

            System.out.println("✅ BufferedImage 변환 성공");
            System.out.println("📐 이미지 크기: " + bufferedImage.getWidth() + " x " + bufferedImage.getHeight());

            // ✅ Tesseract로 직접 OCR (파일 저장 없이!)
            System.out.println("🔍 Tesseract 실행 중...");
            String result = tesseract.doOCR(bufferedImage);

            System.out.println("✅ OCR 성공!");
            System.out.println("📝 추출된 텍스트 길이: " + result.length());

            if (result != null && result.length() > 0) {
                String preview = result.substring(0, Math.min(100, result.length()));
                System.out.println("👀 미리보기: " + preview + "...");
            } else {
                System.out.println("⚠️ 추출된 텍스트가 비어있습니다!");
            }

            System.out.println("========================================");
            return result;

        } catch (TesseractException e) {
            System.err.println("❌ Tesseract 실행 실패!");
            System.err.println("에러: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("OCR 처리 중 오류 발생: " + e.getMessage());

        } catch (IOException e) {
            System.err.println("❌ 파일 처리 실패!");
            System.err.println("에러: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("파일 처리 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * ✅ 파일 확장자 자동 감지
     * 1. 원본 파일명 확인
     * 2. Content-Type 확인
     * 3. 기본값 .jpg
     */
    private String detectFileExtension(MultipartFile file) {
        String extension = ".jpg";  // 기본값

        // 1. 파일명으로 확인
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && !originalFilename.isEmpty()) {
            String lower = originalFilename.toLowerCase();

            if (lower.endsWith(".png")) {
                extension = ".png";
            } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
                extension = ".jpg";
            } else if (lower.endsWith(".bmp")) {
                extension = ".bmp";
            } else if (lower.endsWith(".tiff") || lower.endsWith(".tif")) {
                extension = ".tiff";
            }
        }

        // 2. Content-Type으로 재확인
        String contentType = file.getContentType();
        if (contentType != null) {
            if (contentType.contains("png")) {
                extension = ".png";
            } else if (contentType.contains("jpeg") || contentType.contains("jpg")) {
                extension = ".jpg";
            } else if (contentType.contains("bmp")) {
                extension = ".bmp";
            } else if (contentType.contains("tiff")) {
                extension = ".tiff";
            }
        }

        return extension;
    }
}