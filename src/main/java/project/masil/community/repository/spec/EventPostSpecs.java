package project.masil.community.repository.spec;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;
import project.masil.community.entity.EventPost;
import project.masil.community.entity.Region;
import project.masil.community.enums.EventType;

public final class EventPostSpecs {

  private EventPostSpecs() {
  }

  public static Specification<EventPost> startEndBetween(LocalDateTime from, LocalDateTime to) {
    if (from == null && to == null) {
      return null;
    }
    return (root, q, cb) -> {
      // 일정 겹침(Overlap): endAt >= from AND startAt <= to
      Predicate p = cb.conjunction();
      if (from != null) {
        p = cb.and(p, cb.greaterThanOrEqualTo(root.get("endAt"), from));
      }
      if (to != null) {
        p = cb.and(p, cb.lessThanOrEqualTo(root.get("startAt"), to));
      }
      return p;
    };
  }

  public static Specification<EventPost> matchSido(String sido) {
    if (sido == null || sido.isBlank()) {
      return null;
    }
    return (root, q, cb) -> {
      Join<EventPost, Region> region = root.join("region", JoinType.INNER);
      return cb.equal(region.get("sido"), sido);
    };
  }

  public static Specification<EventPost> matchSigungu(String sigungu) {
    if (sigungu == null || sigungu.isBlank()) {
      return null;
    }
    return (root, q, cb) -> {
      Join<EventPost, Region> region = root.join("region", JoinType.INNER);
      return cb.equal(region.get("sigungu"), sigungu);
    };
  }

  public static Specification<EventPost> matchCategory(EventType category) {
    if (category == null) {
      return null;
    }
    return (root, q, cb) -> cb.equal(root.get("eventType"), category);
  }
}