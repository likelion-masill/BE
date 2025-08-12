package project.masil.community.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.masil.community.entity.Region;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {

  @Query("select r.sido from Region r group by r.sido order by r.sido asc")
  List<String> findSidoList();

  @Query("select r.sigungu from Region r where r.sido = :sido order by r.sigungu asc")
  List<String> findSigunguList(@Param("sido") String sido);

  Optional<Region> findBySidoAndSigungu(String sido, String sigungu);

  boolean existsBySido(String sido);

  boolean existsById(Long id);

}
