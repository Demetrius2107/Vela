package com.vela.im.service.common.interceptor;

import com.vela.im.shared.constants.ImConstants;
import com.vela.im.shared.trace.TraceIdContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class TraceIdInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String traceId = request.getHeader(ImConstants.TraceId.HTTP_HEADER_NAME);
        TraceIdContext.set(traceId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        TraceIdContext.clear();
    }
}
