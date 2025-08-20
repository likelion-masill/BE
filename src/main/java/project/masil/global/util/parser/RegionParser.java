package project.masil.global.util.parser;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import project.masil.global.util.slot.LocationSlot;


public class RegionParser {

  // "성북구" -> "서울특별시"
  private final Map<String, String> sigunguToSido;
  // "강남" -> "강남구", "홍대" -> "마포구"
  private final Map<String, String> aliasToSigungu;

  /**
   * @param sigunguToSido  키: 시군구(예: "성북구"), 값: 시도(예: "서울특별시")
   * @param aliasToSigungu 키: 별칭(예: "강남"),  값: 정식 시군구(예: "강남구")
   */
  public RegionParser(Map<String, String> sigunguToSido, Map<String, String> aliasToSigungu) {
    this.sigunguToSido = sigunguToSido;
    this.aliasToSigungu = aliasToSigungu;
  }

  /**
   * 입력 문자열을 파싱해 시/도·시/군/구와 세부 텍스트를 추출한다. 실패 시 (null,null,null,0.0)
   */
  public LocationSlot parse(String raw) {
    String q = normalize(raw);

    // 1) 정식 시군구 포함(롱기스트 매치)
    String best = longestContains(q, sigunguToSido.keySet());
    if (best != null) {
      String detail = tailAfter(raw, best);
      return new LocationSlot("region", sigunguToSido.get(best), best, safeDetail(detail), 1.0);
    }

    // 2) 별칭 매칭
    String aliasHit = longestContains(q, aliasToSigungu.keySet());
    if (aliasHit != null) {
      String sg = aliasToSigungu.get(aliasHit);
      String detail = tailAfter(raw, aliasHit);
      return new LocationSlot("region", sigunguToSido.get(sg), sg, safeDetail(detail), 0.9);
    }

    // 3) 시도만 매칭
    String sidoHit = longestContains(q, sigunguToSido.values());
    if (sidoHit != null) {
      String detail = tailAfter(raw, sidoHit);
      return new LocationSlot("region", sidoHit, null, safeDetail(detail), 0.8);
    }

    // 4) 퍼지 매칭 (오타/띄어쓰기)
    String fuzzy = bestFuzzy(q, sigunguToSido.keySet());
    if (fuzzy != null) {
      double sim = trigramSim(q, fuzzy);
      if (sim >= 0.8) {
        String detail = tailAfter(raw, fuzzy);
        return new LocationSlot("region", sigunguToSido.get(fuzzy), fuzzy, safeDetail(detail), sim);
      }
    }

    // 5) 실패 → 지역 필터 스킵
    return new LocationSlot("region", null, null, null, 0.0);
  }


  // ------------ helpers ------------
  private static String normalize(String s) {
    s = s.replaceAll("[()\\[\\]{}·,]", " ")
        .replaceAll("\\s+", " ")
        .trim();
    // 조사 제거(약식): "~에서, ~으로, ~근처, ~주변" 등 문미 처리
    s = s.replaceAll("(에서|으로|근처|주변)$", "");
    return s;
  }

  private static String longestContains(String q, Collection<String> dict) {
    String hit = null;
    int max = -1;
    for (String k : dict) {
      if (q.contains(k) && k.length() > max) {
        hit = k;
        max = k.length();
      }
    }
    return hit;
  }

  private static String tailAfter(String raw, String key) {
    int idx = raw.indexOf(key);
    if (idx < 0) {
      return null;
    }
    String tail = raw.substring(idx + key.length()).trim();
    // 조사/불용어 조금 제거
    tail = tail.replaceAll("^(근처|주변|쪽|근방|에서|로|의|에)+", "").trim();
    return tail.isEmpty() ? null : tail;
  }

  private static String safeDetail(String s) {
    if (s == null) {
      return null;
    }
    if (s.length() < 3) {
      return null; // 너무 짧으면 버림
    }
    return s;
  }

  // 퍼지 매칭 간단 구현: trigram 코사인
  private static String bestFuzzy(String q, Collection<String> dict) {
    double best = -1;
    String bestKey = null;
    for (String k : dict) {
      double sim = trigramSim(q, k);
      if (sim > best) {
        best = sim;
        bestKey = k;
      }
    }
    return bestKey;
  }

  private static double trigramSim(String a, String b) {
    Set<String> A = trigrams(a), B = trigrams(b);
    if (A.isEmpty() || B.isEmpty()) {
      return 0;
    }
    int inter = 0;
    for (String t : A) {
      if (B.contains(t)) {
        inter++;
      }
    }
    return inter / Math.sqrt((double) A.size() * B.size());
  }

  private static Set<String> trigrams(String s) {
    s = s.replaceAll("\\s+", "");
    Set<String> set = new HashSet<>();
    for (int i = 0; i <= s.length() - 3; i++) {
      set.add(s.substring(i, i + 3));
    }
    return set;
  }
}