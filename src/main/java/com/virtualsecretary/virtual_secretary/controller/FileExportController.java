package com.virtualsecretary.virtual_secretary.controller;

import com.virtualsecretary.virtual_secretary.dto.response.ApiResponse;
import com.virtualsecretary.virtual_secretary.enums.ErrorCode;
import com.virtualsecretary.virtual_secretary.exception.IndicateException;
import com.virtualsecretary.virtual_secretary.service.FileExportService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/export")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileExportController {
    FileExportService fileExportService;


    @PostMapping("/docx/{meetingCode}")
    public ApiResponse<Map<String, String>> exportDocxAsBase64(@PathVariable String meetingCode) {
        try {
            Map<String, String> fileInfo = fileExportService.createDocx(meetingCode);

            return ApiResponse.<Map<String, String>>builder()
                    .code(200)
                    .message("Export successfully")
                    .result(fileInfo)
                    .build();

        } catch (Exception e) {
            log.error("Error occurred while exporting DOCX for meetingCode: {}", meetingCode, e);
            throw new IndicateException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }


    @PostMapping("/pdf/{meetingCode}")
    public ApiResponse<Map<String, String>> exportPdfAsBase64(@PathVariable String meetingCode) {
        try {
            // Gọi phương thức tạo PDF và lấy thông tin file
            Map<String, String> fileInfo = fileExportService.createPdf(meetingCode);

            return ApiResponse.<Map<String, String>>builder()
                    .code(200)
                    .message("Export PDF successfully")
                    .result(fileInfo)
                    .build();

        } catch (Exception e) {
            log.error("Error occurred while exporting PDF for meetingCode: {}", meetingCode, e);
            throw new IndicateException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    private final RestTemplate restTemplate;

    @PostMapping("/merge/{meetingCode}")
    public ResponseEntity<?> mergeAudio(@PathVariable String meetingCode) {
        String flaskURL = "http://localhost:5010/merge-audio";

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("meeting_code", meetingCode);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(flaskURL, request, Map.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }



}
