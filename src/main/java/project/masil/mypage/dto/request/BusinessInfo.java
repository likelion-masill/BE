package project.masil.mypage.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BusinessInfo {

  private String b_no;       // 사업자번호 (10자리, '-' 제거)
  private String start_dt;   // 개업일 (YYYYMMDD)
  private String p_nm;       // 대표자 성명
}