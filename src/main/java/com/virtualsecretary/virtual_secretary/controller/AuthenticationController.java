package com.virtualsecretary.virtual_secretary.controller;

import com.nimbusds.jose.JOSEException;
import com.virtualsecretary.virtual_secretary.dto.request.AuthenticationRequest;
import com.virtualsecretary.virtual_secretary.dto.request.ChangePasswordRequest;
import com.virtualsecretary.virtual_secretary.dto.request.IntrospectRequest;
import com.virtualsecretary.virtual_secretary.dto.request.LogoutRequest;
import com.virtualsecretary.virtual_secretary.dto.response.ApiResponse;
import com.virtualsecretary.virtual_secretary.dto.response.AuthenticationResponse;
import com.virtualsecretary.virtual_secretary.dto.response.IntrospectResponse;
import com.virtualsecretary.virtual_secretary.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ApiResponse.<AuthenticationResponse>builder()
                .code(200)
                .message("Authentication Successful")
                .result(authenticationService.authenticate(request))
                .build();
    }
    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
                .code(200)
                .result(result)
                .build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request)
            throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder()
                .code(200)
                .build();
    }

    @PutMapping("/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authenticationService.changePassword(request);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Password changed successfully").build();
    }



}
