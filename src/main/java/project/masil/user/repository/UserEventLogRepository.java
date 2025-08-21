package project.masil.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.masil.user.entity.UserEventLog;

public interface UserEventLogRepository extends JpaRepository<UserEventLog, Long> {


}
