package project.masil.global.util.parser;

import java.text.Normalizer;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import project.masil.community.enums.EventType;

public class EventTypeParser {

  private final Map<EventType, Pattern> keywordMap;

  public EventTypeParser() {
    keywordMap = new EnumMap<>(EventType.class);

    keywordMap.put(EventType.FLEA_MARKET,
        Pattern.compile("(플리마켓|프리마켓|벼룩시장|중고장터|셀러 모집)"));

    keywordMap.put(EventType.CULTURE_ART,
        Pattern.compile("(문화|예술|전시|공연|연극|뮤지컬|콘서트|영화제)"));

    keywordMap.put(EventType.OUTDOOR_ACTIVITY,
        Pattern.compile("(야외활동|트레킹|하이킹|등산|러닝|마라톤|피크닉|캠핑)"));

    keywordMap.put(EventType.VOLUNTEER,
        Pattern.compile("(자원봉사|봉사활동|플로깅|재능기부|환경정화|헌혈)"));

    keywordMap.put(EventType.FESTIVAL,
        Pattern.compile("(축제|페스티벌|불꽃놀이|퍼레이드|행사)"));

    keywordMap.put(EventType.STORE_EVENT,
        Pattern.compile("(가게행사|오픈행사|팝업스토어|런칭 이벤트|시식회|체험행사|세일)"));

    keywordMap.put(EventType.EDUCATION,
        Pattern.compile("(교육|강의|강연|세미나|워크숍|부트캠프|스터디|특강)"));
  }

  /**
   * 입력 문장에서 가장 매칭이 잘 되는 EventType 반환 매칭 실패 시 null 반환
   */
  public EventType parseTop1(String input) {
    String text = normalize(input);

    for (Map.Entry<EventType, Pattern> entry : keywordMap.entrySet()) {
      Matcher m = entry.getValue().matcher(text);
      if (m.find()) {
        return entry.getKey();
      }
    }
    return null; // 매칭 실패 시
  }

  /**
   * 다중 카테고리 반환 매칭 실패 시 빈 Set 반환
   */
  public Set<EventType> parseMulti(String input) {
    String text = normalize(input);
    Set<EventType> result = new HashSet<>();

    for (Map.Entry<EventType, Pattern> entry : keywordMap.entrySet()) {
      Matcher m = entry.getValue().matcher(text);
      if (m.find()) {
        result.add(entry.getKey());
      }
    }

    // 매칭 실패 시 그냥 빈 Set
    return result;
  }

  private String normalize(String s) {
    if (s == null) {
      return "";
    }
    return Normalizer.normalize(s, Normalizer.Form.NFKC)
        .toLowerCase(Locale.ROOT)
        .replaceAll("\\s+", " ")
        .trim();
  }

}