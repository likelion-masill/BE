package project.masil.community.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import project.masil.community.enums.EventType;

/**
 * Spring Boot는 Converter<String, EventType> 빈을 자동으로 등록해 @RequestParam, @PathVariable에 적용합니다.
 * 이제 아래 요청이 모두 정상 동작합니다.
 * /eventType/list?eventType=FLEA_MARKET
 * /eventType/list?eventType=flea_market
 * /eventType/list?eventType=플리마켓
 */
@Component
public class EventTypeConverter implements Converter<String, EventType> {
  @Override
  public EventType convert(String source) {
    if (source == null) return null;
    String v = source.trim();

    for (EventType type : EventType.values()) {
      if (type.name().equalsIgnoreCase(v) || type.getDescription().equals(v)) {
        return type;
      }
    }
    throw new IllegalArgumentException("잘못된 EventType 값: " + source);
  }
}
