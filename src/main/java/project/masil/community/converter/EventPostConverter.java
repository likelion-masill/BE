package project.masil.community.converter;

import org.springframework.stereotype.Component;
import project.masil.community.dto.response.EventPostResponse;
import project.masil.community.entity.EventPost;

@Component
public class EventPostConverter {

  public EventPostResponse toResponse(EventPost eventPost) {
    return EventPostResponse.builder()
        .eventId(eventPost.getId())
        .username(eventPost.getUser().getUsername())
        .title(eventPost.getTitle())
        .content(eventPost.getContent())
        .summary(eventPost.getSummary())
        .startAt(eventPost.getStartAt())
        .endAt(eventPost.getEndAt())
        .favoriteCount(eventPost.getFavoriteCount())
        .commentCount(eventPost.getCommentCount())
        .images(eventPost.getEventImages())
        .userImage(eventPost.getUser().getProfileImageUrl())
        .createdAt(eventPost.getCreatedAt())
        .viewCount(eventPost.getViewCount())
        .favoriteCount(eventPost.getFavoriteCount())
        .commentCount(eventPost.getCommentCount())
        .location(eventPost.getLocation())
        .build();
  }

}
