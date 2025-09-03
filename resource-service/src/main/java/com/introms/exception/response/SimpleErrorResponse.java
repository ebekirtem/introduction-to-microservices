package com.introms.exception.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SimpleErrorResponse {
    private String errorMessage;
    private String errorCode;
}