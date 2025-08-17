package project.masil.infrastructure.client.opendata;


import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import project.masil.infrastructure.client.opendata.dto.BusinessInfoPayload;
import project.masil.infrastructure.client.opendata.dto.OwnerVerifyApiResponse;

// OpenDataClient.java
@Component
public class OpenDataClient {

  private final WebClient client;

  public OpenDataClient(@Qualifier("opendataWebClient") WebClient client) {
    this.client = client;
  }

  public OwnerVerifyApiResponse verifyBusiness(BusinessInfoPayload payload) {
    Map<String, Object> requestBody = Map.of("businesses", List.of(payload));

    return client.post()
        .uri(uriBuilder -> uriBuilder
            .path("/validate")
            .queryParam("returnType", "JSON")
            .build())
        .bodyValue(requestBody)
        .retrieve()
        .bodyToMono(OwnerVerifyApiResponse.class)
        .block();
  }
}
