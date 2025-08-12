package project.masil.community.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import project.masil.global.exception.model.BaseErrorCode;

@Getter
@AllArgsConstructor
public enum ClubPostErrorCode implements BaseErrorCode {
  CLUB_POST_FORBIDDEN("CLUB_POST_403", "작성자가 아니므로 권한이 없습니다.", HttpStatus.FORBIDDEN),
  CLUB_POST_NOT_FOUND("CLUB_POST_404", "존재하지 않는 소모임 게시글입니다.", HttpStatus.NOT_FOUND);

  private final String code;
  private final String message;
  private final HttpStatus status;

}
