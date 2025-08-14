package project.masil.mypage.dto.response;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OwnerVerifyResponse {

  private int request_cnt;
  private String status_code;
  private List<Map<String, Object>> data;
  
}