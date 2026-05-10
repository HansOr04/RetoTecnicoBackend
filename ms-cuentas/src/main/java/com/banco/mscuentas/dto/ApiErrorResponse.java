package com.banco.mscuentas.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiErrorResponse {
    private String timestamp;
    private int status;
    private String error;
    private String message;
    private Map<String, String> errors;
}
