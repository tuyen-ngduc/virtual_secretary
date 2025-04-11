package com.virtualsecretary.virtual_secretary.service;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.virtualsecretary.virtual_secretary.entity.Meeting;
import com.virtualsecretary.virtual_secretary.enums.ErrorCode;
import com.virtualsecretary.virtual_secretary.enums.MeetingStatus;
import com.virtualsecretary.virtual_secretary.exception.IndicateException;
import com.virtualsecretary.virtual_secretary.repository.MeetingRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import java.io.*;
import java.nio.file.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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


    public void createDocx(String meetingCode) {
        Meeting meeting = meetingRepository.findByMeetingCode(meetingCode)
                .orElseThrow(() -> new IndicateException(ErrorCode.MEETING_NOT_EXISTED));

        if (meeting.getMeetingStatus() != MeetingStatus.ENDED) {
            throw new IndicateException(ErrorCode.MEETING_NOT_ENDED_YET);
        }
        String transcriptContent = readTranscript(meetingCode);

        try (XWPFDocument docx = new XWPFDocument()) {
            XWPFParagraph paragraph = docx.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText(transcriptContent);

            Path docxPath = Paths.get("stt", meetingCode, "transcript.docx");
            try (FileOutputStream out = new FileOutputStream(docxPath.toFile())) {
                docx.write(out);
            }
        } catch (Exception e) {
            log.error("Error creating DOCX for meeting: {}", meetingCode, e);
        }
    }


    public void convertDocxToPdf(String meetingCode) {
        // Define paths for the DOCX and PDF files
        Path docxPath = Paths.get("stt", meetingCode, "transcript.docx");
        Path pdfPath = Paths.get("stt", meetingCode, "transcript.pdf");

        if (!Files.exists(docxPath)) {
            log.error("File DOCX không tồn tại tại {}", docxPath);
            throw new RuntimeException("File DOCX không tồn tại.");
        }

        try (XWPFDocument docx = new XWPFDocument(new FileInputStream(docxPath.toFile()));
             FileOutputStream fos = new FileOutputStream(pdfPath.toFile());
             PdfWriter writer = new PdfWriter(fos);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document document = new Document(pdfDoc)) {

            PdfFont font;
            try (InputStream fontStream = getClass().getClassLoader().getResourceAsStream("fonts/FreeSerif.ttf")) {
                if (fontStream == null) {
                    log.error("Không tìm thấy tệp font FreeSerif.ttf trong resources/fonts.");
                    throw new RuntimeException("Font không tồn tại. Không thể xuất PDF.");
                }

                byte[] fontBytes = fontStream.readAllBytes();
                font = PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H);
            }

            for (XWPFParagraph paragraph : docx.getParagraphs()) {
                String text = paragraph.getText();
                if (text != null && !text.isEmpty()) {
                    document.add(new Paragraph(text).setFont(font));
                }
            }

            log.info("Đã chuyển đổi thành công file DOCX sang PDF: {}", pdfPath.toAbsolutePath());
        } catch (IOException e) {
            log.error("Lỗi khi chuyển đổi file DOCX sang PDF cho cuộc họp: {}", meetingCode, e);
            throw new RuntimeException("Lỗi khi chuyển đổi file DOCX sang PDF.", e);
        }
    }
}
