package project.masil.community.enums;


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
}