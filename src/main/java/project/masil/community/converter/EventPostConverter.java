package project.masil.community.converter;

import java.util.stream.IntStream;
import org.springframework.stereotype.Component;
import project.masil.community.dto.response.EventImageResponse;
import project.masil.community.dto.response.EventPostResponse;
import project.masil.community.entity.EventPost;

@Component
public class EventPostConverter {

  public EventPostResponse toResponse(EventPost eventPost) {
    return EventPostResponse.builder()
        .eventId(eventPost.getId())
        .username(eventPost.getUser().getUsername())
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
        ).userImage(eventPost.getUser().getProfileImageUrl())
        .createdAt(eventPost.getCreatedAt())
        .viewCount(eventPost.getViewCount())
        .favoriteCount(eventPost.getFavoriteCount())
        .commentCount(eventPost.getCommentCount())
        .location(eventPost.getLocation())
        .build();
  }

}
