package com.virtualsecretary.virtual_secretary.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    DEPARTMENT_NOT_EXISTED(1009, "Department not existed", HttpStatus.BAD_REQUEST),
    DEPARTMENT_CODE_ALREADY_EXISTS(1017, "Department code already exists", HttpStatus.CONFLICT),
    EMAIL_EXISTED(1010, "Email existed", HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR(400, "Invalid input", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS(1011, "Incorrect password", HttpStatus.OK),
    JWT_SIGNING_ERROR(1012, "JWT Signing error", HttpStatus.INTERNAL_SERVER_ERROR),
    JWT_INVALID(9998, "JWT Invalid", HttpStatus.BAD_REQUEST),
    OLD_PASSWORD_INCORRECT(1013, "Old password incorrect", HttpStatus.BAD_REQUEST),
    NEW_PASSWORD_MUST_BE_DIFFERENT(1014, "New password must be different", HttpStatus.BAD_REQUEST),
    ROOM_NOT_EXISTED(1015, "Room not existed", HttpStatus.NOT_FOUND),
    ROOM_CODE_ALREADY_EXISTS(1016, "Room code already exists", HttpStatus.CONFLICT),
    MEETING_EXISTED(1017, "Meeting already existed", HttpStatus.CONFLICT),
    ROLE_INVALID(1018, "Role invalid", HttpStatus.BAD_REQUEST),
    MEETING_NOT_EXISTED(1019, "Meeting not existed", HttpStatus.NOT_FOUND),
    MEMBER_EXISTED(1020, "Member existed", HttpStatus.NOT_FOUND),
    MEMBER_NOT_EXISTED (1021, "Member not existed", HttpStatus.NOT_FOUND),
    MEETING_TIME_CONFLICT(1022,"MEETING_TIME_CONFLICT", HttpStatus.CONFLICT ),

    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    int code;
    String message;
    HttpStatusCode statusCode;
}
