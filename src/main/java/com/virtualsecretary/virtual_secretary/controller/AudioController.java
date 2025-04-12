package com.virtualsecretary.virtual_secretary.controller;

import com.virtualsecretary.virtual_secretary.dto.response.ApiResponse;
import com.virtualsecretary.virtual_secretary.exception.IndicateException;
import com.virtualsecretary.virtual_secretary.service.AudioService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/audio")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AudioController {
    AudioService audioService;

    @PostMapping("/{meetingCode}/upload")
    public ApiResponse<String> uploadAudio(
            @PathVariable String meetingCode,
            @RequestParam("file") MultipartFile file) {

        try {

            String filePath = audioService.saveAudio(meetingCode, file);
            return ApiResponse.<String>builder()
                    .code(200)
                    .message("File saved successfully")
                    .result(filePath)
                    .build();
        } catch (IndicateException e) {
            return ApiResponse.<String>builder()
                    .code(400)
                    .message("Error: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            return ApiResponse.<String>builder()
                    .code(500)
                    .message("Server Error: " + e.getMessage())
                    .build();
        }
    }


}
