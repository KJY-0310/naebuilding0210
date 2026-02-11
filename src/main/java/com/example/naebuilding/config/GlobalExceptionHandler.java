package com.example.naebuilding.config;

import com.example.naebuilding.dto.common.ApiResponse;
import com.example.naebuilding.dto.common.ErrorResponse;
import com.example.naebuilding.dto.common.FieldErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import com.example.naebuilding.exception.NotFoundException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.security.access.AccessDeniedException;
import lombok.extern.slf4j.Slf4j;


import java.util.List;

@Slf4j
@RestControllerAdvice(basePackages = "com.example.naebuilding.controller")
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleNotFound(NotFoundException e) {
        ErrorResponse error = new ErrorResponse("NOT_FOUND", null);

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(e.getMessage(), error));
    }

    // ✅ 400: @Valid 바디 검증 실패 (RequestBody record validation)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        List<FieldErrorResponse> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> new FieldErrorResponse(fe.getField(), fe.getDefaultMessage()))
                .toList();

        ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("요청 값이 올바르지 않습니다.", error));
    }

    // ✅ 400: @ModelAttribute / QueryParam 바인딩 검증 실패 (Pageable 등 포함)
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleBindException(BindException e) {
        List<FieldErrorResponse> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> new FieldErrorResponse(fe.getField(), fe.getDefaultMessage()))
                .toList();

        ErrorResponse error = new ErrorResponse("BIND_ERROR", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("요청 파라미터가 올바르지 않습니다.", error));
    }

    // ✅ 400: enum 변환 실패 (?status=wrong 같은 케이스)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String name = e.getName();
        String value = e.getValue() == null ? "null" : e.getValue().toString();

        ErrorResponse error = new ErrorResponse("TYPE_MISMATCH",
                List.of(new FieldErrorResponse(name, "값이 올바르지 않습니다. 입력값=" + value)));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("요청 값 타입이 올바르지 않습니다.", error));
    }

    // ✅ 400: required param 누락
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleMissingParam(MissingServletRequestParameterException e) {
        ErrorResponse error = new ErrorResponse("MISSING_PARAM",
                List.of(new FieldErrorResponse(e.getParameterName(), "필수 파라미터가 누락되었습니다.")));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("필수 파라미터가 누락되었습니다.", error));
    }

    // ✅ 400: service에서 던진 IllegalArgumentException (현재 너 코드가 이걸 씀)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleIllegalArgument(IllegalArgumentException e) {
        ErrorResponse error = new ErrorResponse("ILLEGAL_ARGUMENT", null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(e.getMessage(), error));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleAccessDenied(AccessDeniedException e) {
        ErrorResponse error = new ErrorResponse("FORBIDDEN", null);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail(e.getMessage(), error));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleException(Exception e) {
        log.error("Unhandled exception", e); // ✅ 추가 (스택트레이스 찍힘)

        ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("서버 오류가 발생했습니다.", error));
    }


    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleMissingPart(MissingServletRequestPartException e) {
        ErrorResponse error = new ErrorResponse("MISSING_PART",
                List.of(new FieldErrorResponse(e.getRequestPartName(), "필수 파트가 누락되었습니다.")));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("multipart 요청이 올바르지 않습니다.", error));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleMaxUpload(MaxUploadSizeExceededException e) {
        ErrorResponse error = new ErrorResponse("PAYLOAD_TOO_LARGE", null);
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ApiResponse.fail("업로드 용량 제한을 초과했습니다.", error));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleMultipart(MultipartException e) {
        // 환경에 따라 size 초과가 여기로 오는 경우가 있어서 안전망
        ErrorResponse error = new ErrorResponse("MULTIPART_ERROR", null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("multipart 요청 처리 중 오류가 발생했습니다.", error));
    }
}
