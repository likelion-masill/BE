package project.masil.global.config.props;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "cors")
public class CorsProps {

  private List<String> allowedOrigins = List.of();
  private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
  private List<String> allowedHeaders = List.of("*");
  private Boolean allowCredentials = true;

}