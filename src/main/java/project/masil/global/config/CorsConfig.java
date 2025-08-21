package project.masil.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import project.masil.global.config.props.CorsProps;

//CorsConfig 설정
@Configuration
@EnableConfigurationProperties(CorsProps.class)
public class CorsConfig {

  @Bean
  public CorsConfigurationSource corsConfigurationSource(CorsProps props) {
    CorsConfiguration cfg = new CorsConfiguration();
    cfg.setAllowedOrigins(props.getAllowedOrigins());      // 정확 매칭
    cfg.setAllowedMethods(props.getAllowedMethods());
    cfg.setAllowedHeaders(props.getAllowedHeaders());
    cfg.setExposedHeaders(props.getExposedHeaders());
    cfg.setAllowCredentials(Boolean.TRUE.equals(props.getAllowCredentials()));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cfg);
    return source;
  }
}