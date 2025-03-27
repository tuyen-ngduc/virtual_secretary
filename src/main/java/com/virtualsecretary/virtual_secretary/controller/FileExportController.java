package com.virtualsecretary.virtual_secretary.controller;

import com.virtualsecretary.virtual_secretary.dto.response.ApiResponse;
import com.virtualsecretary.virtual_secretary.enums.ErrorCode;
import com.virtualsecretary.virtual_secretary.exception.IndicateException;
import com.virtualsecretary.virtual_secretary.service.FileExportService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Slf4j
@RestController
@RequestMapping("/export")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileExportController {
    FileExportService fileExportService;
    
    @PostMapping("/docx/{meetingCode}")
    public ApiResponse<Void> exportToDocx(@PathVariable String meetingCode){
        try {
            fileExportService.createDocx(meetingCode);
            return ApiResponse.<Void>builder()
                    .code(200)
                    .message("Export successfully")
                    .build();
        }catch (Exception e){
            throw new IndicateException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

    }
    @PostMapping("/pdf/{meetingCode}")
    public ApiResponse<Void> convertDocxToPdf(@PathVariable String meetingCode){
        try{
        fileExportService.convertDocxToPdf(meetingCode);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Export successfully")
                .build();
    }catch (Exception e){
        throw new IndicateException(ErrorCode.UNCATEGORIZED_EXCEPTION);}
    }






}
