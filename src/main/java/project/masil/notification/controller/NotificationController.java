package project.masil.notification.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.masil.notification.service.NotificationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
@Tag(name = "Notification", description = "알림 관련 API")
@Slf4j
public class NotificationController {

  private final NotificationService notificationService;


}
