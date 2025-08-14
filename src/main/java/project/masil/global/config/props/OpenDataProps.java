package project.masil.global.config.props;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "open-data")
public class OpenDataProps {

  private final String baseUrl;
  private final String apiKey;

  public OpenDataProps(String baseUrl, String apiKey) {
    this.baseUrl = baseUrl;
    this.apiKey = apiKey;
  }
}