package project.masil.global.log;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class RequestTimingInterceptor implements HandlerInterceptor {

  private static final Logger log = LoggerFactory.getLogger(RequestTimingInterceptor.class);
  private static final String START_NANO = "reqStartNano";

  @Override
  public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
    // 단조 증가 시계로 측정(시스템 시간 변경 영향 없음)
    req.setAttribute(START_NANO, System.nanoTime());
    // 간단한 traceId (있으면 Sleuth/OTEL 써도 됨)
    MDC.put("traceId", java.util.UUID.randomUUID().toString().substring(0, 8));
    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object handler,
      Exception ex) {
    Long start = (Long) req.getAttribute(START_NANO);
    long tookMs = (start != null) ? (System.nanoTime() - start) / 1_000_000 : -1;

    String method = req.getMethod();
    String uri = req.getRequestURI();
    String query = req.getQueryString();
    int status = res.getStatus();
    String ip = req.getRemoteAddr();

    if (ex == null) {
      log.info("HTTP {} {}{} status={} tookMs={} ip={} traceId={}",
          method, uri, (query == null ? "" : "?" + query), status, tookMs, ip, MDC.get("traceId"));
    } else {
      // 예외도 시간과 함께 기록
      log.warn("HTTP {} {}{} status={} tookMs={} ip={} traceId={} ex={}",
          method, uri, (query == null ? "" : "?" + query), status, tookMs, ip, MDC.get("traceId"),
          ex.toString());
    }
    MDC.clear();
  }
}