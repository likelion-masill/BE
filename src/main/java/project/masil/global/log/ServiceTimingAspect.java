package project.masil.global.log;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceTimingAspect {

  private static final Logger log = LoggerFactory.getLogger(ServiceTimingAspect.class);

  /**
   * 1) project.masil 하위의 모든 @Service 클래스 메소드
   */
  @Around("within(project.masil..*) && @within(org.springframework.stereotype.Service)")
  public Object timeServices(ProceedingJoinPoint pjp) throws Throwable {
    return proceedAndLog(pjp);
  }

  /**
   * 2) 이름이 *Client / *Manager 인 빈 (project.masil 하위 한정)
   */
  @Around("(bean(*Client) || bean(*Manager)) && within(project.masil..*)")
  public Object timeClientsAndManagers(ProceedingJoinPoint pjp) throws Throwable {
    return proceedAndLog(pjp);
  }

  // 공통 로깅 로직
  private Object proceedAndLog(ProceedingJoinPoint pjp) throws Throwable {
    long start = System.nanoTime();
    String classMethod = pjp.getSignature().getDeclaringType().getSimpleName()
        + "." + pjp.getSignature().getName();
    try {
      Object result = pjp.proceed();
      long tookMs = (System.nanoTime() - start) / 1_000_000;
      log.info("[SERVICE] {} tookMs={}ms", classMethod, tookMs);
      return result;
    } catch (Throwable ex) {
      long tookMs = (System.nanoTime() - start) / 1_000_000;
      log.warn("[SERVICE] {} threw {} after {}ms",
          classMethod, ex.getClass().getSimpleName(), tookMs);
      throw ex;
    }
  }
}