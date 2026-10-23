package com.zjnu.studybuddy.aspect;

import com.zjnu.studybuddy.annotation.OpLog;
import com.zjnu.studybuddy.entity.OperationLog;
import com.zjnu.studybuddy.security.LoginUser;
import com.zjnu.studybuddy.service.LogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class OpLogAspect {

    private static final int DETAIL_MAX = 500;

    private final LogService logService;

    @Around("@annotation(com.zjnu.studybuddy.annotation.OpLog)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        OpLog opLog = resolveAnnotation(pjp);
        if (opLog == null) {

            return pjp.proceed();
        }
        long start = System.currentTimeMillis();
        boolean success = true;
        String detail = null;
        try {
            return pjp.proceed();
        } catch (Throwable t) {
            success = false;
            detail = t.getMessage();
            throw t;
        } finally {
            save(opLog, success, detail, System.currentTimeMillis() - start);
        }
    }

    private OpLog resolveAnnotation(ProceedingJoinPoint pjp) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        OpLog annotation = method.getAnnotation(OpLog.class);
        if (annotation != null) {
            return annotation;
        }
        try {
            Method target = pjp.getTarget().getClass()
                    .getMethod(method.getName(), method.getParameterTypes());
            return target.getAnnotation(OpLog.class);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private void save(OpLog opLog, boolean success, String detail, long costMs) {
        try {
            OperationLog entity = new OperationLog();
            entity.setModule(opLog.module());
            entity.setAction(opLog.action());
            entity.setSuccess(success ? 1 : 0);
            entity.setCostMs(costMs);
            entity.setDetail(abbreviate(detail));

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof LoginUser loginUser) {
                entity.setUserId(loginUser.getUser().getId());
                entity.setUsername(loginUser.getUser().getUsername());
            }

            HttpServletRequest request = currentRequest();
            if (request != null) {
                entity.setHttpMethod(request.getMethod());
                entity.setUri(request.getRequestURI());
                entity.setIp(clientIp(request));
            }
            logService.record(entity);
        } catch (Exception e) {
            log.warn("构建操作日志失败：{}", e.getMessage());
        }
    }

    private HttpServletRequest currentRequest() {
        var attrs = RequestContextHolder.getRequestAttributes();
        return attrs instanceof ServletRequestAttributes sra ? sra.getRequest() : null;
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            int comma = forwarded.indexOf(',');
            return comma > 0 ? forwarded.substring(0, comma).trim() : forwarded.trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        return StringUtils.hasText(realIp) ? realIp : request.getRemoteAddr();
    }

    private String abbreviate(String text) {
        if (text == null) {
            return null;
        }
        return text.length() <= DETAIL_MAX ? text : text.substring(0, DETAIL_MAX - 3) + "...";
    }
}
