package project.masil.community.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.masil.community.entity.Favorite;
import project.masil.community.entity.Post;
import project.masil.user.entity.User;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

  Optional<Favorite> findByUserAndPost(User user, Post post);

}
