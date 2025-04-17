package com.virtualsecretary.virtual_secretary.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.virtualsecretary.virtual_secretary.config.FlaskApiConfig;
import com.virtualsecretary.virtual_secretary.entity.Meeting;
import com.virtualsecretary.virtual_secretary.entity.Member;
import com.virtualsecretary.virtual_secretary.enums.ErrorCode;
import com.virtualsecretary.virtual_secretary.enums.MeetingRole;
import com.virtualsecretary.virtual_secretary.exception.IndicateException;
import com.virtualsecretary.virtual_secretary.repository.MeetingRepository;
import com.virtualsecretary.virtual_secretary.repository.MemberRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Service
public class AudioService {

    MeetingRepository meetingRepository;
    MemberRepository memberRepository;
    FlaskApiConfig flaskApiConfig;


    public String saveAudio(String meetingCode, MultipartFile file) {
        meetingRepository.findByMeetingCode(meetingCode)
                .orElseThrow(() -> new IndicateException(ErrorCode.MEETING_NOT_EXISTED));

        try {
            String audioDirectory = "audio/" + meetingCode;
            Files.createDirectories(Paths.get(audioDirectory));

            String originalFilename = file.getOriginalFilename();
            String savedFilePath = audioDirectory + "/" + originalFilename;
            Path path = Paths.get(savedFilePath);
            Files.write(path, file.getBytes());

            // Gọi chuyển âm ngay sau khi lưu file
            String rawJson = transcribeWithWhisper(savedFilePath);
            String transcriptText = extractTranscriptFromJson(rawJson);

            // Trích xuất tên và vai trò từ tên file
            String[] parts = originalFilename.split("_");
            if (parts.length >= 4) {
                String name = Arrays.stream(parts, 2, parts.length - 1)
                        .collect(Collectors.joining(" "))
                        .replaceAll("\\.\\w+$", "");
                String roleWithExt = parts[parts.length - 1];
                String role = roleWithExt.replaceAll("\\.\\w+$", "");

                String transcriptDirectory = "stt/" + meetingCode;
                Files.createDirectories(Paths.get(transcriptDirectory));

                String transcriptLine = name + " - " + role + ": " + transcriptText.trim() + "\n\n";

                // Append vào file transcript.txt
                File transcriptFile = new File(transcriptDirectory + "/transcript.txt");
                Files.write(Paths.get(transcriptFile.getAbsolutePath()),
                        transcriptLine.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }

            return savedFilePath;

        } catch (IOException e) {
            e.printStackTrace();
            throw new IndicateException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }


    private String extractTranscriptFromJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);
            return root.path("transcript").asText("");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    private String transcribeWithWhisper(String audioFilePath) {
        RestTemplate restTemplate = new RestTemplate();
        File audioFile = new File(audioFilePath);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(audioFile));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        String flaskApiUrl = flaskApiConfig.getUrl();
        ResponseEntity<String> response = restTemplate.exchange(flaskApiUrl, HttpMethod.POST, requestEntity, String.class);

        return response.getBody();
    }

    private String getVietnameseRoleName(MeetingRole role) {
        return switch (role) {
            case PRESIDENT -> "Chủ tọa";
            case SECRETARY -> "Thư ký";
            case COMMISSIONER -> "Ủy viên";
            case CRITIC -> "Phản biện";
            case GUEST -> "Khách mời";
        };
    }

}


