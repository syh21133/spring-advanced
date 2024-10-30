package org.example.expert.aop;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j(topic = "Aop")
@Aspect
@Component
public class Aop {


    private final HttpServletRequest request;

    public Aop(HttpServletRequest request) {
        this.request = request;
    }

    @Pointcut("execution(* org.example.expert.domain.comment.controller.CommentAdminController.*(..))")
    private void comment() {
    }

    @Pointcut("execution(* org.example.expert.domain.user.controller.UserAdminController.*(..))")
    private void user() {
    }


    @Around("comment() || user()")
    public Object logAdminAccess(ProceedingJoinPoint joinPoint) throws Throwable {

        String userId = request.getUserPrincipal() != null ? request.getUserPrincipal().getName()
            : "Unknown User";

        // 현재 시각, URL, 요청 바디 등 정보 로깅
        LocalDateTime requestTime = LocalDateTime.now();
        String requestUrl = request.getRequestURI();
        String requestBody = getRequestBody(joinPoint.getArgs());

        log.info("Admin API Access - User ID: {}, Time: {}, URL: {}, Request Body: {}",
            userId, requestTime, requestUrl, requestBody);

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable ex) {
            log.error("Exception in Admin API Access: {}", ex.getMessage());
            throw ex;
        }

        log.info("Response Body: {}", result != null ? result.toString() : "No Response Body");

        return result;
    }

    // Request Body를 문자열로 변환하는 메서드
    private String getRequestBody(Object[] args) {
        StringBuilder requestBody = new StringBuilder();
        for (Object arg : args) {
            requestBody.append(arg.toString()).append("; ");
        }
        return requestBody.toString().trim();
    }


}