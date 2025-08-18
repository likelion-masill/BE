package project.masil.infrastructure.client.opendata.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OwnerVerifyApiResponse {


  @JsonProperty("request_cnt")
  private int requestCnt;

  @JsonProperty("status_code")
  private String statusCode;

  @JsonProperty("data")
  private List<Map<String, Object>> data;

}