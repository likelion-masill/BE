package project.masil.chat.controller;


import java.security.Principal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpSubscription;

/**
 * 디버깅용 컨트롤러
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class WsDebugController {

  private final SimpUserRegistry userRegistry;

  @MessageMapping("/debug/users")
  public void debugUsers(Principal p) {
    log.info("[USRDEBUG] called by user={}", p != null ? p.getName() : null);
    log.info("[USRDEBUG] userCount={}", userRegistry.getUserCount());

    for (SimpUser u : userRegistry.getUsers()) {
      log.info("[USR] name={}", u.getName());
      u.getSessions().forEach(s -> {
        log.info("  [SESS] id={} subs={}", s.getId(), s.getSubscriptions().size());
        s.getSubscriptions().forEach(sub ->
            log.info("    [SUB] {}", sub.getDestination())
        );
      });
    }
  }
}