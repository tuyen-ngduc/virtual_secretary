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
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        Meeting meeting = meetingRepository.findByMeetingCode(meetingCode)
                .orElseThrow(() -> new IndicateException(ErrorCode.MEETING_NOT_EXISTED));

        Member member = memberRepository.findByUser_EmployeeCodeAndMeeting_MeetingCode(name, meetingCode)
                .orElseThrow(() -> new IndicateException(ErrorCode.MEMBER_NOT_EXISTED));

        Path audioDirectory = Paths.get("audio", meetingCode);
        try {
            Files.createDirectories(audioDirectory);
        } catch (IOException e) {
            throw new IndicateException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss"));
        String extension = Optional.ofNullable(file.getOriginalFilename())
                .filter(f -> f.contains("."))
                .map(f -> f.substring(f.lastIndexOf(".")))
                .orElse(".wav");

        String formattedName = member.getUser().getName().replaceAll("\\s+", "_");
        String role = getVietnameseRoleName(member.getMeetingRole());
        String fileName = String.format("%s_%s_%s%s", timestamp, formattedName, role, extension);

        Path filePath = audioDirectory.resolve(fileName);

        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi lưu file audio", e);
        }

        return filePath.toString();
    }

    public String transcribeAudioFiles(String meetingCode) {
        meetingRepository.findByMeetingCode(meetingCode)
                .orElseThrow(() -> new IndicateException(ErrorCode.MEETING_NOT_EXISTED));

        String audioDirectory = "audio/" + meetingCode;
        String transcriptDirectory = "stt/" + meetingCode;
        File audioDir = new File(audioDirectory);
        File transcriptFile = new File(transcriptDirectory + "/transcript.txt");

        if (!audioDir.exists() || !audioDir.isDirectory()) {
            return "Audio directory does not exist!";
        }

        try {
            File[] audioFiles = audioDir.listFiles((dir, name) ->
                    name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".ogg"));
            if (audioFiles == null || audioFiles.length == 0) {
                return "No audio files found!";
            }
            Arrays.sort(audioFiles, Comparator.comparing(File::getName));

            Files.createDirectories(Paths.get(transcriptDirectory));
            StringBuilder transcriptBuilder = new StringBuilder();

            for (File audioFile : audioFiles) {
                String audioFilePath = audioFile.getAbsolutePath();
                String rawTranscriptJson = transcribeWithWhisper(audioFilePath);

                String transcriptText = extractTranscriptFromJson(rawTranscriptJson);

                String filename = audioFile.getName();
                String[] parts = filename.split("_");

                if (parts.length < 4) {
                    continue; // Bỏ qua file có tên không đúng định dạng
                }

                String name = Arrays.stream(parts, 2, parts.length - 1)
                        .collect(Collectors.joining(" "))
                        .replaceAll("\\.\\w+$", "");
                String roleWithExt = parts[parts.length - 1];
                String role = roleWithExt.replaceAll("\\.\\w+$", "");

                transcriptBuilder.append(name).append(" - ").append(role)
                        .append(": ").append(transcriptText.trim())
                        .append("\n\n");
            }

            Files.write(Paths.get(transcriptFile.getAbsolutePath()), transcriptBuilder.toString().getBytes());

            return transcriptFile.getAbsolutePath();

        } catch (IOException e) {
            e.printStackTrace();
            return "Error while transcribing audio files: " + e.getMessage();
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


