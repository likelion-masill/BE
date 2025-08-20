package project.masil.global.config.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openai")
@Getter
@Setter
public class OpenAIProps {

  private String baseUrl;
  private String apiKey;
  private String embeddingModel = "text-embedding-3-small";
  
}
