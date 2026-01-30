package com.assessment.corebanking.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingAspect.class);
    private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile("(\\\"cardNumber\\\"\\s*:\\s*\\\")(.*?)(\\\")");

    private final ObjectMapper objectMapper;

    public LoggingAspect(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Around("execution(* com.assessment.corebanking.controller..*(..))")
    public Object logRequestResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        String requestInfo = buildRequestInfo();
        String signature = joinPoint.getSignature().toShortString();
        String argsJson = maskCardNumber(safeToJson(sanitizeArgs(joinPoint.getArgs())));
        LOGGER.info("Request {} {} args={}", requestInfo, signature, argsJson);

        try {
            Object result = joinPoint.proceed();
            String responseJson = maskCardNumber(safeToJson(result));
            LOGGER.info("Response {} {} result={}", requestInfo, signature, responseJson);
            return result;
        } catch (Exception ex) {
            LOGGER.error("Error {} {} message={}", requestInfo, signature, ex.getMessage());
            throw ex;
        }
    }

    private String buildRequestInfo() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }
        HttpServletRequest request = attributes.getRequest();
        String query = request.getQueryString();
        String path = request.getRequestURI();
        if (query != null && !query.isBlank()) {
            path = path + "?" + query;
        }
        return request.getMethod() + " " + path;
    }

    private List<Object> sanitizeArgs(Object[] args) {
        List<Object> sanitized = new ArrayList<>();
        if (args == null) {
            return sanitized;
        }
        for (Object arg : args) {
            if (arg instanceof HttpServletRequest
                || arg instanceof HttpServletResponse
                || arg instanceof BindingResult) {
                sanitized.add(arg.getClass().getSimpleName());
            } else {
                sanitized.add(arg);
            }
        }
        return sanitized;
    }

    private String safeToJson(Object value) {
        if (value == null) {
            return "null";
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return String.valueOf(value);
        }
    }

    String maskCardNumber(String input) {
        if (input == null) {
            return null;
        }
        Matcher matcher = CARD_NUMBER_PATTERN.matcher(input);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String value = matcher.group(2);
            String masked = maskValue(value);
            matcher.appendReplacement(buffer, matcher.group(1) + Matcher.quoteReplacement(masked) + matcher.group(3));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private String maskValue(String value) {
        if (value == null) {
            return null;
        }
        int length = value.length();
        if (length <= 4) {
            return value;
        }
        String suffix = value.substring(length - 4);
        return "*".repeat(length - 4) + suffix;
    }
}
