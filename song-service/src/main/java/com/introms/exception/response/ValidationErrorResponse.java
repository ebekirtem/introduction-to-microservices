package com.introms.exception.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class ValidationErrorResponse {
    private String errorMessage;
    private Map<String, String> details; // Field-specific validation errors
    private String errorCode;

}