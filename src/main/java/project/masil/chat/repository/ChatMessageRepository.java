package project.masil.chat.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.masil.chat.entity.ChatMessage;

/**
 * 채팅 메시지 레포지토리
 * - 특정 방 메시지를 시간 오름차순으로 페이징 조회(대화 흐름 자연스럽게)
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

  /**
   * 메시지를 시간 순서대로 정렬해서 페이징 조회
   * @param roomId
   * @param pageable
   * @return
   */
  Page<ChatMessage> findByRoomIdOrderByCreatedAtAsc(Long roomId, Pageable pageable);
}
