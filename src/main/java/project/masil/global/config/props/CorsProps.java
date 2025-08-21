package project.masil.global.config.props;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "cors")
public class CorsProps {

  private List<String> allowedOrigins = new ArrayList<>();
  private List<String> allowedMethods = new ArrayList<>(
      List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
  private List<String> allowedHeaders = new ArrayList<>(List.of("*"));
  private List<String> exposedHeaders = new ArrayList<>();
  private Boolean allowCredentials = true;

}