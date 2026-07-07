package com.gahih.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@ControllerAdvice
public class WebExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFoundException(NotFoundException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "error/404";
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleUnauthorizedException(UnauthorizedException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "error/401";
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleForbiddenException(ForbiddenException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "error/403";
    }

    @ExceptionHandler(DomainValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleDomainValidationException(DomainValidationException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "error/400";
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBusinessException(BusinessException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "error/400";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoResourceFoundException(NoResourceFoundException e, Model model) {
        model.addAttribute("message", "요청한 리소스를 찾을 수 없습니다.");
        return "error/404";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e,
            HttpServletRequest request,
            Model model
    ) {
        log.warn(
                "Bad web request parameter type. method={}, uri={}, name={}, value={}, requiredType={}",
                request.getMethod(),
                request.getRequestURI(),
                e.getName(),
                e.getValue(),
                resolveRequiredTypeName(e)
        );

        model.addAttribute("message", "요청 주소 또는 입력값 형식이 올바르지 않습니다.");
        return "error/400";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception e, HttpServletRequest request, Model model) {
        log.error(
                "Unhandled web exception. method={}, uri={}",
                request.getMethod(),
                request.getRequestURI(),
                e
        );

        model.addAttribute("message", "예상치 못한 오류가 발생했습니다.");
        return "error/500";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException e,
            Model model
    ) {
        model.addAttribute("message", "첨부파일 용량이 허용 범위를 초과했습니다.");
        return "error/400";
    }

    private String resolveRequiredTypeName(MethodArgumentTypeMismatchException e) {
        if (e.getRequiredType() == null) {
            return "unknown";
        }

        return e.getRequiredType().getSimpleName();
    }
}