package project.masil.community.enums;

import lombok.Getter;

@Getter
public enum PostType {
  EVENT("이벤트"),
  GATHER("모임");

  private final String displayName;

  PostType(String displayName) {
    this.displayName = displayName;
  }
}
