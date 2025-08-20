package project.masil.global.util.parser;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class KoreanTimeParser {

  // === 정책 상수 ===
  public static final Duration DEFAULT_RANGE = Duration.ofDays(14); // 실패 시 now~+14일
  public static final Map<String, TimeBand> TIME_BANDS = Map.of(
      "오전", new TimeBand(LocalTime.of(6, 0), LocalTime.of(11, 59, 59)),
      "오후", new TimeBand(LocalTime.of(12, 0), LocalTime.of(17, 59, 59)),
      "저녁", new TimeBand(LocalTime.of(18, 0), LocalTime.of(23, 59, 59)),
      "밤", new TimeBand(LocalTime.of(20, 0), LocalTime.of(23, 59, 59)),
      "새벽", new TimeBand(LocalTime.of(0, 0), LocalTime.of(5, 59, 59)),
      "점심", new TimeBand(LocalTime.of(11, 30), LocalTime.of(13, 30))
  );
  private static final Map<String, DayOfWeek> DOW = Map.of(
      "월", DayOfWeek.MONDAY, "화", DayOfWeek.TUESDAY, "수", DayOfWeek.WEDNESDAY,
      "목", DayOfWeek.THURSDAY, "금", DayOfWeek.FRIDAY, "토", DayOfWeek.SATURDAY, "일", DayOfWeek.SUNDAY
  );

  private static final Pattern DOW_EXPR =
      Pattern.compile("(다다음|다음|저번|이번)?(월|화|수|목|금|토|일)(?:요일)?");


  public record TimeBand(LocalTime start, LocalTime end) {

  }

  public record TimeSpan(LocalDateTime start, LocalDateTime end) {

  }

  // === 공개 엔트리 ===

  /**
   * now 기준으로 query를 해석해 (start,end) 반환. 실패 시 기본 2주.
   *
   * @param query 해석할 문자열 예: "이번주 금요일 오후", "다음주말", "오후 2시~오후 4시"
   * @param now   현재 시간 (보통 LocalDateTime.now())
   */
  public static TimeSpan parse(String query, LocalDateTime now) {
    String q = normalize(query);

    // 1) 절대 날짜/시간
    TimeSpan abs = tryAbsoluteRange(q, now);
    if (abs != null) {
      return abs;
    }

    // 2) (이번/다음/다다음/저번) + 요일 (+ 시간대)  ← ★ 우선순위 상향
    TimeSpan dow = tryDayOfWeek(q, now);
    if (dow != null) {
      return dow;
    }

    // 3) 주/주말/평일 표현  ← ★ 요일 다음으로
    TimeSpan weekish = tryWeekish(q, now);
    if (weekish != null) {
      return weekish;
    }

    // 4) 상대일자 (오늘/내일/모레/글피)
    TimeSpan rel = tryRelativeDays(q, now);
    if (rel != null) {
      return rel;
    }

    // 5) 시간대 단독 (오늘 해당 밴드)
    TimeSpan bandOnly = tryBandOnly(q, now);
    if (bandOnly != null) {
      return bandOnly;
    }

    // 6) 실패 → 기본 2주
    return new TimeSpan(now, endOfDay(now.plus(DEFAULT_RANGE).toLocalDate()));
  }

  // === 1) 절대 날짜/시간 ===
  private static final Pattern DATE_YMD = Pattern.compile(
      "(20\\d{2})[-./년\\s]?(\\d{1,2})[-./월\\s]?(\\d{1,2})[일]?");
  private static final Pattern DATE_MD = Pattern.compile("(\\d{1,2})[./월\\s]?(\\d{1,2})[일]?");
  private static final Pattern TIME_HM = Pattern.compile(
      "(오전|오후)?\\s?(\\d{1,2})[:시]\\s?(\\d{0,2})"); // 3시, 14:30, 오후3시
  private static final Pattern RANGE_CONNECTOR = Pattern.compile("[~∼~-]");

  private static TimeSpan tryAbsoluteRange(String q, LocalDateTime now) {
    // 1) "M/D HH:mm~HH:mm" 또는 "M월D일 HH:mm~HH:mm"
    Matcher md = DATE_MD.matcher(q);
    if (md.find()) {
      int month = Integer.parseInt(md.group(1));
      int day = Integer.parseInt(md.group(2));
      int year = now.getYear(); // 연도 생략 시 올해
      LocalDate base = safeDate(year, month, day);

      String after = q.substring(md.end());
      List<LocalTime> times = extractTimes(after);
      if (RANGE_CONNECTOR.matcher(after).find() && times.size() >= 2) {
        LocalDateTime s = LocalDateTime.of(base, times.get(0));
        LocalDateTime e = LocalDateTime.of(base, times.get(1));
        if (e.isBefore(s)) {
          e = e.plusDays(1);
        }
        return new TimeSpan(s, e);
      } else if (times.size() == 1) {
        LocalDateTime s = LocalDateTime.of(base, times.get(0));
        return new TimeSpan(s, s.plusHours(2));
      } else {
        return new TimeSpan(startOfDay(base), endOfDay(base));
      }
    }

    // 2) "YYYY-MM-DD ..." 형태
    Matcher ymd = DATE_YMD.matcher(q);
    if (ymd.find()) {
      int y = Integer.parseInt(ymd.group(1));
      int m = Integer.parseInt(ymd.group(2));
      int d = Integer.parseInt(ymd.group(3));
      LocalDate base = safeDate(y, m, d);

      String tail = q.substring(ymd.end());
      List<LocalTime> times = extractTimes(tail);
      if (RANGE_CONNECTOR.matcher(tail).find() && times.size() >= 2) {
        LocalDateTime s = LocalDateTime.of(base, times.get(0));
        LocalDateTime e = LocalDateTime.of(base, times.get(1));
        if (e.isBefore(s)) {
          e = e.plusDays(1);
        }
        return new TimeSpan(s, e);
      } else if (times.size() == 1) {
        LocalDateTime s = LocalDateTime.of(base, times.get(0));
        return new TimeSpan(s, s.plusHours(2));
      } else {
        return new TimeSpan(startOfDay(base), endOfDay(base));
      }
    }

    // 3) "HH:mm~HH:mm" (오늘로 간주)
    if (RANGE_CONNECTOR.matcher(q).find() && countMatches(TIME_HM, q) >= 2) {
      List<LocalTime> t = extractTimes(q);
      if (t.size() >= 2) {
        LocalDateTime s = LocalDateTime.of(now.toLocalDate(), t.get(0));
        LocalDateTime e = LocalDateTime.of(now.toLocalDate(), t.get(1));
        if (e.isBefore(s)) {
          e = e.plusDays(1);
        }
        return new TimeSpan(s, e);
      }
    }

    // 4) 단일 시각만 있을 때 → 오늘 그 시각부터 2시간 윈도우
    if (countMatches(TIME_HM, q) == 1) {
      LocalTime t = extractTimes(q).get(0);
      LocalDateTime s = LocalDateTime.of(now.toLocalDate(), t);
      return new TimeSpan(s, s.plusHours(2));
    }
    return null;
  }

  // === 2) 주/주말/평일 ===
  private static TimeSpan tryWeekish(String q, LocalDateTime now) {
    // ★ 요일 토큰이 함께 있으면 '주' 해석을 건너뛴다 (중복/충돌 방지)
    if (containsDow(q)) {
      return null;
    }

    LocalDate base = now.toLocalDate();

    // 0) 주차 offset 계산 (다다음/다음/이번/지난)
    int offset = 0;
    if (q.contains("다다음주")) {
      offset = 2;
    } else if (q.contains("다음주")) {
      offset = 1;
    } else if (q.contains("지난주") || q.contains("저번주")) {
      offset = -1;
    }
    // "이번주"는 offset=0

    TimeBand band = findTimeBand(q); // "오전/오후/저녁/밤/점심/새벽" 있으면

    // A) 주말 처리 (토~일)
    if (q.contains("주말")) {
      LocalDate mon = mondayOf(base).plusWeeks(offset);
      LocalDate sat = mon.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
      LocalDate sun = mon.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
      if (band != null) {
        return new TimeSpan(LocalDateTime.of(sat, band.start()),
            LocalDateTime.of(sun, band.end()));
      } else {
        return new TimeSpan(startOfDay(sat), endOfDay(sun));
      }
    }

    // B) 평일 처리 (월~금)
    if (q.contains("평일")) {
      LocalDate mon = mondayOf(base).plusWeeks(offset);
      LocalDate fri = mon.plusDays(4);
      if (band != null) {
        return new TimeSpan(LocalDateTime.of(mon, band.start()),
            LocalDateTime.of(fri, band.end()));
      } else {
        return new TimeSpan(startOfDay(mon), endOfDay(fri));
      }
    }

    // C) '주' 자체
    if (q.contains("이번주") || q.contains("다음주") || q.contains("다다음주")
        || q.contains("지난주") || q.contains("저번주")) {
      var range = weekRange(base, offset); // (월, 일)
      if (band != null) {
        return new TimeSpan(LocalDateTime.of(range.left, band.start()),
            LocalDateTime.of(range.right, band.end()));
      } else {
        return new TimeSpan(startOfDay(range.left), endOfDay(range.right));
      }
    }

    // D) 해당 없음
    return null;
  }

  // === 3) (다음/다다음/저번/이번) + 요일 (+ 시간대) ===
  private static TimeSpan tryDayOfWeek(String q, LocalDateTime now) {
    Matcher m = DOW_EXPR.matcher(q);
    if (!m.find()) {
      return null;
    }

    String rel = m.group(1);  // 다다음 | 다음 | 저번 | 이번 | null
    String dowKr = m.group(2); // 월 ~ 일

    int weekOffset = 0;
    if ("다다음".equals(rel)) {
      weekOffset = 2;
    } else if ("다음".equals(rel)) {
      weekOffset = 1;
    } else if ("저번".equals(rel)) {
      weekOffset = -1;
    }

    DayOfWeek dow = DOW.get(dowKr);

    LocalDate baseMon = mondayOf(now.toLocalDate()).plusWeeks(weekOffset);
    // 그 주의 '해당 요일'로 안전하게 이동
    LocalDate day = baseMon.with(TemporalAdjusters.nextOrSame(dow));

    TimeBand band = findTimeBand(q);
    if (band != null) {
      return new TimeSpan(LocalDateTime.of(day, band.start()),
          LocalDateTime.of(day, band.end()));
    } else {
      return new TimeSpan(startOfDay(day), endOfDay(day));
    }
  }

  // === 4) 오늘/내일/모레/글피 등 ===
  private static TimeSpan tryRelativeDays(String q, LocalDateTime now) {
    if (q.contains("오늘")) {
      return new TimeSpan(startOfDay(now.toLocalDate()), endOfDay(now.toLocalDate()));
    }
    if (q.contains("내일")) {
      LocalDate d = now.toLocalDate().plusDays(1);
      return new TimeSpan(startOfDay(d), endOfDay(d));
    }
    if (q.contains("모레")) {
      LocalDate d = now.toLocalDate().plusDays(2);
      return new TimeSpan(startOfDay(d), endOfDay(d));
    }
    if (q.contains("글피")) {
      LocalDate d = now.toLocalDate().plusDays(3);
      return new TimeSpan(startOfDay(d), endOfDay(d));
    }
    return null;
  }

  // === 5) 시간대 단독 ===
  private static TimeSpan tryBandOnly(String q, LocalDateTime now) {
    TimeBand band = findTimeBand(q);
    if (band == null) {
      return null;
    }
    LocalDate d = now.toLocalDate();
    return new TimeSpan(LocalDateTime.of(d, band.start()),
        LocalDateTime.of(d, band.end()));
  }

  // === 유틸 ===
  private static String normalize(String s) {
    return s.replaceAll("\\s+", "");
  }

  private static int countMatches(Pattern p, String s) {
    int c = 0;
    Matcher m = p.matcher(s);
    while (m.find()) {
      c++;
    }
    return c;
  }

  private static List<LocalTime> extractTimes(String s) {
    List<LocalTime> out = new ArrayList<>();
    Matcher m = TIME_HM.matcher(s);
    while (m.find()) {
      String ampm = m.group(1); // 오전/오후 (nullable)
      String hh = m.group(2);
      String mm = m.group(3);
      int H = Integer.parseInt(hh);
      int M = (mm == null || mm.isEmpty()) ? 0 : Integer.parseInt(mm);
      if ("오후".equals(ampm) && H < 12) {
        H += 12;
      }
      if ("오전".equals(ampm) && H == 12) {
        H = 0; // 오전 12시 = 00시
      }
      out.add(LocalTime.of(Math.min(H, 23), Math.min(M, 59)));
    }
    return out;
  }

  private static LocalDateTime startOfDay(LocalDate d) {
    return d.atStartOfDay();
  }

  private static LocalDateTime endOfDay(LocalDate d) {
    return d.atTime(23, 59, 59);
  }

  private static Pair<LocalDate, LocalDate> weekRange(LocalDate base, int offsetWeeks) {
    LocalDate mon = mondayOf(base).plusWeeks(offsetWeeks);
    LocalDate sun = mon.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    return Pair.of(mon, sun);
  }

  private static Pair<LocalDate, LocalDate> weekendRange(LocalDate base, int offsetWeeks) {
    LocalDate mon = mondayOf(base).plusWeeks(offsetWeeks);
    LocalDate sat = mon.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
    LocalDate sun = mon.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    return Pair.of(sat, sun);
  }

  private static LocalDate mondayOf(LocalDate d) {
    return d.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
  }

  private static TimeSpan toSpan(Pair<LocalDate, LocalDate> r) {
    return new TimeSpan(startOfDay(r.left), endOfDay(r.right));
  }

  private static TimeBand findTimeBand(String q) {
    for (var e : TIME_BANDS.entrySet()) {
      if (q.contains(e.getKey())) {
        return e.getValue();
      }
    }
    return null;
  }

  // ★ 요일 포함 여부 체크
  private static boolean containsDow(String q) {
    return DOW_EXPR.matcher(q).find();
  }

  private static LocalDate safeDate(int y, int m, int d) {
    try {
      return LocalDate.of(y, m, d);
    } catch (DateTimeException e) {
      return LocalDate.of(y, Math.min(Math.max(m, 1), 12), 1).withDayOfMonth(1);
    }
  }

  // 간단한 Pair
  private static class Pair<L, R> {

    final L left;
    final R right;

    private Pair(L l, R r) {
      left = l;
      right = r;
    }

    public static <L, R> Pair<L, R> of(L l, R r) {
      return new Pair<>(l, r);
    }
  }
}
