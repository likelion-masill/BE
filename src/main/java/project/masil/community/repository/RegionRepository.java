package project.masil.community.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.masil.community.entity.Region;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {
}
