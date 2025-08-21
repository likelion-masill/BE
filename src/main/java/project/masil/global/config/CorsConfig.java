package project.masil.global.config;

import lombok.AllArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import project.masil.global.config.props.CorsProps;

//CorsConfig 설정
@Configuration
@AllArgsConstructor
@EnableConfigurationProperties(CorsProps.class)

public class CorsConfig {

  private final CorsProps props;

  @Bean
  public UrlBasedCorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration cfg = new CorsConfiguration();
    cfg.setAllowedOriginPatterns(props.getAllowedOrigins()); // 정확 매칭이면 setAllowedOrigins
    cfg.setAllowedMethods(props.getAllowedMethods());
    cfg.setAllowedHeaders(props.getAllowedHeaders());
    cfg.setAllowCredentials(Boolean.TRUE.equals(props.getAllowCredentials()));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cfg);
    return source;
  }

}
