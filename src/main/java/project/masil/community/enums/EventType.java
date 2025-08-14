package project.masil.community.enums;


import com.fasterxml.jackson.annotation.JsonCreator;

public enum EventType {
  FLEA_MARKET("플리마켓"),
  CULTURE_ART("문화/예술"),
  OUTDOOR_ACTIVITY("야외활동"),
  FOOD("먹거리");

  private final String description;

  EventType(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  @JsonCreator
  public static EventType from(String value) {
    for (EventType type : EventType.values()) {
      if (type.name().equalsIgnoreCase(value) || type.getDescription().equals(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("잘못된 EventType 값: " + value);
  }
}