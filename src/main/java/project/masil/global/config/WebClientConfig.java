package project.masil.global.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import project.masil.global.config.props.AiServerProps;
import project.masil.global.config.props.OpenDataProps;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Configuration
@EnableConfigurationProperties({
    OpenDataProps.class,
    AiServerProps.class
})
@RequiredArgsConstructor
public class WebClientConfig {

  private final OpenDataProps props;
  private final AiServerProps aiProps;

  @Bean
  public WebClient.Builder webClientBuilder() {
    var httpClient = HttpClient.create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
        .responseTimeout(Duration.ofSeconds(10))
        .doOnConnected(conn -> {
          conn.addHandlerLast(new ReadTimeoutHandler(10, TimeUnit.SECONDS));
          conn.addHandlerLast(new WriteTimeoutHandler(10, TimeUnit.SECONDS));
        });

    return WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .filter(logRequest())
        .filter(logResponse());
  }

  @Bean(name = "opendataWebClient")
  public WebClient opendataClient(WebClient.Builder builder) {
    return builder.clone()
        .baseUrl(props.getBaseUrl())
        // 모든 요청에 serviceKey를 쿼리스트링으로 자동 추가
        .filter(appendServiceKeyQuery(props.getApiKey()))
        .build();
  }

  @Bean(name = "aiWebClient")
  public WebClient aiClient(WebClient.Builder builder) {
    return builder.clone()
        .baseUrl(aiProps.getBaseUrl())
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }

  private ExchangeFilterFunction appendServiceKeyQuery(String serviceKey) {
    return (request, next) -> {
      URI newUri = UriComponentsBuilder.fromUri(request.url())
          .queryParam("serviceKey", serviceKey)
          .build()
          .encode()
          .toUri();
      ClientRequest newRequest = ClientRequest.from(request).url(newUri).build();
      return next.exchange(newRequest);
    };
  }

  private ExchangeFilterFunction logRequest() {
    return ExchangeFilterFunction.ofRequestProcessor(req -> {
      // 필요 시 로깅/마스킹 추가
      return Mono.just(req);
    });
  }

  private ExchangeFilterFunction logResponse() {
    return ExchangeFilterFunction.ofResponseProcessor(res -> Mono.just(res));
  }
}