package project.masil.community.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum EventSort {
  DATE("최신순"),
  COMMENTS("댓글순"),
  POPULARITY("인기순"); // 좋아요순

  private final String description;

  EventSort(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  @JsonCreator
  public static EventSort from(String value) {
    for (EventSort sort : EventSort.values()) {
      // Enum 이름(DATE, COMMENTS, POPULARITY) 또는 한글 설명(최신순, 댓글순, 인기순) 모두 매핑 가능
      if (sort.name().equalsIgnoreCase(value) || sort.getDescription().equals(value)) {
        return sort;
      }
    }
    throw new IllegalArgumentException("잘못된 EventSort 값: " + value);
  }
}
