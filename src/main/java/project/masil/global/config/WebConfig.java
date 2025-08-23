package project.masil.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import project.masil.global.converter.MultipartJackson2HttpMessageConverter;
import project.masil.global.log.RequestTimingInterceptor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  private final RequestTimingInterceptor timingInterceptor;


  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(new MultipartJackson2HttpMessageConverter(new ObjectMapper()));
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(timingInterceptor)
        .addPathPatterns("/**")
        .excludePathPatterns("/health", "/actuator/**", "/static/**");
  }
}
