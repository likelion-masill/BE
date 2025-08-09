package project.masil.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.masil.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {


}
