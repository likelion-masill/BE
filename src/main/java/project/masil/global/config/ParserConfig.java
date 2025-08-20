package project.masil.global.config;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import project.masil.community.repository.RegionRepository;
import project.masil.global.util.parser.EventTypeParser;
import project.masil.global.util.parser.RegionParser;

@Configuration
@RequiredArgsConstructor

public class ParserConfig {

  private final RegionRepository regionRepository;
  // private final RegionAliasRepository aliasRepository;

  @Bean
  public RegionParser regionParser() {
    Map<String, String> sigunguToSido = new HashMap<>();
    for (Object[] row : regionRepository.findAllPairs()) {
      String sido = (String) row[0];
      String sigungu = (String) row[1];
      sigunguToSido.put(sigungu, sido);
    }

/*
    Map<String, String> aliasToSigungu = aliasRepository.findAll()
        .stream()
        .collect(Collectors.toMap(RegionAlias::getAlias, ra -> ra.getRegion().getSigungu()));
*/
    // 별칭 레포지토리 없이 임시로 정의
    Map<String, String> aliasToSigungu = Map.of(
        "강남", "강남구", "홍대", "마포구", "여의도", "영등포구", "서면", "부산진구", "해운대", "해운대구"
    );

    return new RegionParser(sigunguToSido, aliasToSigungu);
  }

  @Bean
  public EventTypeParser eventTypeParser() {
    return new EventTypeParser();
  }
}