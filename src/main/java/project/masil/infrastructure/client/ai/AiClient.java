package project.masil.infrastructure.client.ai;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import project.masil.infrastructure.client.ai.dto.AiSummarizeRequest;
import project.masil.infrastructure.client.ai.dto.AiSummarizeResponse;

@Component
public class AiClient {

  private final WebClient client;

  public AiClient(@Qualifier("aiWebClient") WebClient client) {
    this.client = client;
  }

  public AiSummarizeResponse summarize(AiSummarizeRequest request) {
    return client.post()
        .uri("/api/summarize")
        .bodyValue(request)
        .retrieve()
        .onStatus(org.springframework.http.HttpStatusCode::isError, this::toApiException)
        .bodyToMono(AiSummarizeResponse.class)
        .block();
  }

  private reactor.core.publisher.Mono<Throwable> toApiException(
      org.springframework.web.reactive.function.client.ClientResponse resp) {
    return resp.bodyToMono(String.class)
        .defaultIfEmpty("")
        .map(body -> new RuntimeException(
            "AI 서버 요약 API HTTP " + resp.statusCode().value() + " body=" + body));
  }
}
