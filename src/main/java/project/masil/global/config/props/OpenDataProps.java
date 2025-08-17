package project.masil.global.config.props;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@AllArgsConstructor
@ConfigurationProperties(prefix = "open-data")
public class OpenDataProps {

  private final String baseUrl;
  private final String apiKey;

}