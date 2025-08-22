package project.masil.community.converter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.IntStream;
import org.springframework.stereotype.Component;
import project.masil.community.dto.response.EventImageResponse;
import project.masil.community.dto.response.EventPostResponse;
import project.masil.community.dto.response.RegionResponse;
import project.masil.community.entity.EventPost;

@Component
public class EventPostConverter {

  public EventPostResponse toResponse(EventPost eventPost, boolean isLiked, boolean isAuthor,
      RegionResponse regionResponse) {

    long remainingSeconds = 0L;
    if (eventPost.isUp() && eventPost.getUpExpiresAt() != null) {
      remainingSeconds = Math.max(0L,                      // 만료 시 음수 방지
          Duration.between(LocalDateTime.now(), eventPost.getUpExpiresAt()).getSeconds());
    }

    return EventPostResponse.builder()
        .eventId(eventPost.getId())
        .username(eventPost.getUser().getUsername())
        .userImage(eventPost.getUser().getProfileImageUrlOrDefault())
        .isBusinessVerified(eventPost.getUser().isBusinessVerified())
        .isAuthor(isAuthor)
        .eventType(eventPost.getEventType())
        .title(eventPost.getTitle())
        .content(eventPost.getContent())
        .summary(eventPost.getSummary())
        .startAt(eventPost.getStartAt())
        .endAt(eventPost.getEndAt())
        .favoriteCount(eventPost.getFavoriteCount())
        .commentCount(eventPost.getCommentCount())
        .images(
            IntStream.range(0, eventPost.getEventImages().size())
                .mapToObj(i -> EventImageResponse.builder()
                    .sequence(i + 1) // DB sequence 컬럼이 0부터면 +1
                    .imageUrl(eventPost.getEventImages().get(i))
                    .build()
                )
                .toList()
        )
        .createdAt(eventPost.getCreatedAt())
        .viewCount(eventPost.getViewCount())
        .favoriteCount(eventPost.getFavoriteCount())
        .commentCount(eventPost.getCommentCount())
        .region(regionResponse)
        .isUp(eventPost.isUp()) // isUp 게시물인지
        .location(eventPost.getLocation())
        .isLiked(isLiked)
        // ↓ 내려주면 프론트에서 "남은 기간: 6일 23:59:53" UI 만들기 쉬움
        .upEndAt(eventPost.getUpExpiresAt())
        .upStartedAt(eventPost.getUpAt())
        .upRemainingSeconds(remainingSeconds)  // ← Long 필드에 세팅 (오토박싱)
        .build();
  }

}
