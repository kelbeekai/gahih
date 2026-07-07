package com.gahih.global.exception;

import com.gahih.domain.category.service.CategoryService;
import com.gahih.domain.community.service.CountryCommunityService;
import com.gahih.domain.member.session.LoginMember;
import com.gahih.domain.visit.service.VisitorStatisticsService;
import com.gahih.global.common.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class WebExceptionHandler {

    private static final Pattern COMMUNITY_PATH_PATTERN = Pattern.compile("^/c/([^/]+)(?:/.*)?$");

    private final VisitorStatisticsService visitorStatisticsService;
    private final CountryCommunityService countryCommunityService;
    private final CategoryService categoryService;

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFoundException(NotFoundException e, HttpServletRequest request, Model model) {
        prepareErrorPageModel(model, request);
        model.addAttribute("message", e.getMessage());
        return "error/404";
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleUnauthorizedException(UnauthorizedException e, HttpServletRequest request, Model model) {
        prepareErrorPageModel(model, request);
        model.addAttribute("message", e.getMessage());
        return "error/401";
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleForbiddenException(ForbiddenException e, HttpServletRequest request, Model model) {
        prepareErrorPageModel(model, request);
        model.addAttribute("message", e.getMessage());
        return "error/403";
    }

    @ExceptionHandler(DomainValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleDomainValidationException(DomainValidationException e, HttpServletRequest request, Model model) {
        prepareErrorPageModel(model, request);
        model.addAttribute("message", e.getMessage());
        return "error/400";
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBusinessException(BusinessException e, HttpServletRequest request, Model model) {
        prepareErrorPageModel(model, request);
        model.addAttribute("message", e.getMessage());
        return "error/400";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoResourceFoundException(NoResourceFoundException e, HttpServletRequest request, Model model) {
        prepareErrorPageModel(model, request);
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

        prepareErrorPageModel(model, request);
        model.addAttribute("message", "요청 주소 또는 입력값 형식이 올바르지 않습니다.");
        return "error/400";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException e,
            HttpServletRequest request,
            Model model
    ) {
        prepareErrorPageModel(model, request);
        model.addAttribute("message", "첨부파일 용량이 허용 범위를 초과했습니다.");
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

        prepareErrorPageModel(model, request);
        model.addAttribute("message", "예상치 못한 오류가 발생했습니다.");
        return "error/500";
    }

    private void prepareErrorPageModel(Model model, HttpServletRequest request) {
        model.addAttribute("loginMember", resolveLoginMember(request));
        model.addAttribute("globalCommunities", resolveGlobalCommunities());
        model.addAttribute("visitorStatistics", resolveVisitorStatistics());

        String communityCode = resolveCommunityCode(request);
        if (communityCode == null) {
            model.addAttribute("headerCategories", List.of());
            return;
        }

        try {
            model.addAttribute("currentCommunity", categoryService.findCommunity(communityCode));
            model.addAttribute("headerCategories", categoryService.findHeaderCategories(communityCode));
        } catch (Exception e) {
            log.debug(
                    "Skip community context for error page. uri={}, communityCode={}",
                    request.getRequestURI(),
                    communityCode
            );
            model.addAttribute("headerCategories", List.of());
        }
    }

    private LoginMember resolveLoginMember(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        Object loginMember = session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember instanceof LoginMember resolvedLoginMember) {
            return resolvedLoginMember;
        }

        return null;
    }

    private List<?> resolveGlobalCommunities() {
        try {
            return countryCommunityService.findEnabledCommunities();
        } catch (Exception e) {
            log.debug("Skip global communities for error page.", e);
            return List.of();
        }
    }

    private Object resolveVisitorStatistics() {
        try {
            return visitorStatisticsService.getSummary();
        } catch (Exception e) {
            log.debug("Skip visitor statistics for error page.", e);
            return null;
        }
    }

    private String resolveCommunityCode(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();

        if (contextPath != null && !contextPath.isBlank() && requestUri.startsWith(contextPath)) {
            requestUri = requestUri.substring(contextPath.length());
        }

        Matcher matcher = COMMUNITY_PATH_PATTERN.matcher(requestUri);
        if (!matcher.matches()) {
            return null;
        }

        return matcher.group(1);
    }

    private String resolveRequiredTypeName(MethodArgumentTypeMismatchException e) {
        if (e.getRequiredType() == null) {
            return "unknown";
        }

        return e.getRequiredType().getSimpleName();
    }
}