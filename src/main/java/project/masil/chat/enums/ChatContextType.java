package project.masil.chat.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * 대화가 시작된 '출처(컨텍스트)' 종류 - EVENT_POST : 이벤트 상세에서 '대화하기' -> 작성자와 DM - COMMENT : 댓글 작성자 프로필에서 '채팅 신청'
 * -> 댓글 작성자와 DM - CLUB_POST : 소모임 게시글에서 '대화하기' -> 모임장과 DM
 * <p>
 * 저장은 ChatRoom.contextType + contextId로 통일해서 관리 - `EVENT_POST` : `contextId = eventPostId` -
 * `COMMENT` : `contextId = commentId` - `CLUB_POST` : `contextId = clubPostId`
 */
public enum ChatContextType {
  EVENT_POST("이벤트"),
  COMMENT("댓글"),
  CLUB_POST("소모임");

  private final String description;

  ChatContextType(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  /**
   * 문자열을 ChatContextType enum 객체로 변환
   *
   * @param value 사용자가 입력한 문자열
   * @return
   */
  @JsonCreator //JSON → 객체 변환 시 호출할 생성자나 팩토리 메서드 지정
  public static ChatContextType from(String value) {
    for (ChatContextType type : ChatContextType.values()) {
      //사용자가 입력한 문자열(value)와 Enum 이름과 대소문자 무시하고 비교,  사용자가 입력한 문자열 Enum의 한글이름과 비교
      if (type.name().equalsIgnoreCase(value) || type.getDescription().equals(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("잘못된 ChatContext 값: " + value);
  }
}