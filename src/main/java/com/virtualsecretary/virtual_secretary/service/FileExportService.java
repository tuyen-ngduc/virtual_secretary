package com.virtualsecretary.virtual_secretary.service;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.signatures.*;
import com.virtualsecretary.virtual_secretary.entity.Meeting;
import com.virtualsecretary.virtual_secretary.enums.ErrorCode;
import com.virtualsecretary.virtual_secretary.exception.IndicateException;
import com.virtualsecretary.virtual_secretary.repository.MeetingRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import com.itextpdf.kernel.geom.Rectangle;

import java.io.*;
import java.nio.file.*;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class FileExportService {
    MeetingRepository meetingRepository;

    private String readTranscript(String meetingCode) {
        Path filePath = Paths.get("stt", meetingCode, "transcript.txt");

        try (Stream<String> lines = Files.lines(filePath)) {
            String content = lines.collect(Collectors.joining(System.lineSeparator()));
            log.info("Transcript content: {}", content);
            return content;
        } catch (IOException e) {
            log.error("Error reading transcript file for meeting: {}", meetingCode, e);
            return "";
        }
    }

    public Map<String, String> createDocx(String meetingCode) {
        Meeting meeting = meetingRepository.findByMeetingCode(meetingCode)
                .orElseThrow(() -> new IndicateException(ErrorCode.MEETING_NOT_EXISTED));

        String transcriptContent = readTranscript(meetingCode);

        try (XWPFDocument docx = new XWPFDocument()) {
            // Tiêu đề: BIÊN BẢN CUỘC HỌP + meetingCode
            XWPFParagraph title = docx.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setBold(true);
            titleRun.setFontSize(16);
            titleRun.setText("BIÊN BẢN CUỘC HỌP " + meetingCode);
            titleRun.addBreak();

            // Nội dung bản ghi
            XWPFParagraph paragraph = docx.createParagraph();
            XWPFRun run = paragraph.createRun();
            String[] lines = transcriptContent.split("\\r?\\n");
            for (String line : lines) {
                run.setText(line);
                run.addBreak();
            }

            // Ghi ra file hệ thống tại thư mục stt/{meetingCode}/
            Path outputDir = Paths.get("stt", meetingCode);
            Files.createDirectories(outputDir);  // Tạo thư mục nếu chưa có

            String fileName = "Bien_ban_cuoc_hop_" + meetingCode + ".docx";
            Path outputFilePath = outputDir.resolve(fileName);

            try (OutputStream fileOut = Files.newOutputStream(outputFilePath)) {
                docx.write(fileOut); // Ghi ra file thật trên ổ đĩa
            }

            // Ghi vào ByteArrayOutputStream để trả về base64
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            docx.write(out);
            byte[] docBytes = out.toByteArray();
            String base64Content = Base64.getEncoder().encodeToString(docBytes);

            Map<String, String> response = new HashMap<>();
            response.put("fileName", fileName);
            response.put("fileContent", base64Content);
            return response;
        } catch (IOException e) {
            log.error("Error creating DOCX for meeting: {}", meetingCode, e);
            throw new IndicateException(ErrorCode.FILE_EXPORT_ERROR);
        }
    }



    public Map<String, String> createPdf(String meetingCode) {
        Meeting meeting = meetingRepository.findByMeetingCode(meetingCode)
                .orElseThrow(() -> new IndicateException(ErrorCode.MEETING_NOT_EXISTED));

        String transcriptContent = readTranscript(meetingCode);

        Path directoryPath = Paths.get("stt", meetingCode);
        Path filePath = directoryPath.resolve("Bien_ban_cuoc_hop_" + meetingCode + ".pdf");

        try {
            Files.createDirectories(directoryPath);

            // 1. Tạo file PDF tạm thời chưa ký
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            String fontPath = "fonts/FreeSerif.ttf";
            PdfFont font = PdfFontFactory.createFont(fontPath);
            PdfFont boldFont = PdfFontFactory.createFont(fontPath);
            PdfFont signatureFont = PdfFontFactory.createFont(fontPath);

            Paragraph title = new Paragraph("BIÊN BẢN CUỘC HỌP " + meetingCode)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFont(boldFont)
                    .setFontSize(16);
            document.add(title);

            String[] lines = transcriptContent.split("\\r?\\n");
            for (String line : lines) {
                document.add(new Paragraph(line).setFont(font));
            }
            document.close();

            // 2. Ghi file PDF tạm ra đĩa
            Path unsignedPdfPath = directoryPath.resolve("unsigned_" + meetingCode + ".pdf");
            Files.write(unsignedPdfPath, baos.toByteArray());

            // 3. Tải keystore
            String keystorePath = "/home/thuanld/virtual_secretary/keystores/keystore.jks";
            String keystorePassword = "thuanld@actvn";
            String keyPassword = "thuanld@actvn";
            String alias = "myalias";

            KeyStore ks = KeyStore.getInstance("JKS");
            try (FileInputStream fis = new FileInputStream(keystorePath)) {
                ks.load(fis, keystorePassword.toCharArray());
            }

            PrivateKey privateKey = (PrivateKey) ks.getKey(alias, keyPassword.toCharArray());
            Certificate[] chain = ks.getCertificateChain(alias);

            // 4. Ký PDF
            try (FileOutputStream signedOut = new FileOutputStream(filePath.toFile())) {
                PdfReader reader = new PdfReader(unsignedPdfPath.toFile());
                PdfSigner signer = new PdfSigner(reader, signedOut, new StampingProperties());

                // Lấy kích thước trang để tính vị trí chữ ký
                PdfDocument pdfDoc = signer.getDocument();
                PdfPage page = pdfDoc.getPage(1);  // Hoặc trang cuối: pdfDoc.getPage(pdfDoc.getNumberOfPages())
                Rectangle pageSize = page.getPageSize();

                // Đặt chữ ký ở góc dưới bên phải, cách mép phải 36pt, mép dưới 50pt
                float sigWidth = 250;
                float sigHeight = 120;
                float x = pageSize.getWidth() - sigWidth - 36;
                float y = 50;

                Rectangle signatureRect = new Rectangle(x, y, sigWidth, sigHeight);

                PdfSignatureAppearance appearance = signer.getSignatureAppearance()
                        .setReason("Ký xác nhận biên bản họp")
                        .setLocation("Virtual Secretary System")
                        .setPageRect(signatureRect)
                        .setPageNumber(1)
                        .setLayer2Font(signatureFont)
                        .setReuseAppearance(false);
                signer.setFieldName("sig");

                IExternalSignature pks = new PrivateKeySignature(privateKey, DigestAlgorithms.SHA256, "SunRsaSign");
                IExternalDigest digest = new BouncyCastleDigest();

                signer.signDetached(digest, pks, chain, null, null, null, 0, PdfSigner.CryptoStandard.CADES);
                Files.deleteIfExists(unsignedPdfPath);

            }


            // 5. Encode base64
            byte[] signedPdfBytes = Files.readAllBytes(filePath);
            String base64Content = Base64.getEncoder().encodeToString(signedPdfBytes);

            Map<String, String> response = new HashMap<>();
            response.put("fileName", filePath.getFileName().toString());
            response.put("fileContent", base64Content);
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            throw new IndicateException(ErrorCode.FILE_EXPORT_ERROR);
        }
    }









}
