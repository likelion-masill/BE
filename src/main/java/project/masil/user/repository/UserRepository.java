package project.masil.user.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.masil.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  /**
   * 닉네임 존재 여부 확인
   *
   * @param username 닉네임
   * @return 존재하면 true, 아니면 false
   */
  boolean existsByUsername(String username);
}
