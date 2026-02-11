package com.example.naebuilding.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String code,
        List<FieldErrorResponse> fieldErrors
) {}
