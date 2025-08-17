package project.masil.chat.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.masil.chat.entity.ChatRoom;
import project.masil.chat.enums.ChatContextType;

/**
 * 채팅방 레포지토리
 * - 단일 방 조회 (contextType, contextId, userAId, userBId)
 * - 내 방 목록 조회 : userId 하나만 받아서 userA==userId OR userB==userId
 */
@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

  /**
   *
   * @param contextType 채팅 유형(이벤트/댓글/소모임)
   * @param contextId 출처 식별자 (EVENT_POST면 eventPostId, CLUB_POST면 clubPostId
   * @param userAId 두 사용자 중 '작은' ID
   * @param userBId 두 사용자 중 '큰' ID
   * @return
   */
  Optional<ChatRoom> findByContextTypeAndContextIdAndUserAIdAndUserBId(
      ChatContextType contextType, Long contextId, Long userAId, Long userBId
  );


  /**
   * 내 방 목록 (userId 하나로 조회)
   * - JPQL OR 조건
   * - 정렬/페이징은 Pageable로 제어 (lastMessageAt desc)
   */
  @Query("""
         select room
         from ChatRoom room
         where room.userAId = :userId or room.userBId = :userId
""")
  Page<ChatRoom> findMyRooms(@Param("userId") Long userId, Pageable pageable);



}
