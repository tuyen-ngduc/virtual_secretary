package com.virtualsecretary.virtual_secretary.exception;

import com.virtualsecretary.virtual_secretary.enums.ErrorCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IndicateException extends RuntimeException {
    public IndicateException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    private ErrorCode errorCode;

}

