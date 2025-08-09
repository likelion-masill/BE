package project.masil.community.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import project.masil.global.exception.model.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum RegionErrorCode implements BaseErrorCode {
  SIDO_NOT_FOUND("REGION_4041", "존재하지 않는 광역지방자치단체입니다.", HttpStatus.NOT_FOUND),
  SIGUNGU_NOT_FOUND("REGION_4042", "존재하지 않는 기초지방자치단체입니다.", HttpStatus.NOT_FOUND),
  REGION_NOT_FOUND("REGION_4043", "존재하지 않는 행정구역입니다.", HttpStatus.NOT_FOUND);

  private final String code;
  private final String message;
  private final HttpStatus status;

}
