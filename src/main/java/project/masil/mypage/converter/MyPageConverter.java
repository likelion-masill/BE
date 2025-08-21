package project.masil.mypage.converter;

import java.util.List;
import java.util.stream.IntStream;
import project.masil.community.dto.response.EventImageResponse;
import project.masil.community.entity.ClubPost;
import project.masil.community.entity.EventPost;
import project.masil.community.entity.Post;
import project.masil.global.exception.CustomException;
import project.masil.mypage.dto.response.PostResponse;
import project.masil.mypage.exception.MyPageErrorCode;

public class MyPageConverter {

  public static PostResponse toPostResponse(Post post) {
    return switch (post.getPostType()) {
      case EVENT -> toPostResponse((EventPost) post);
      case CLUB -> toPostResponse((ClubPost) post);
      default -> throw new CustomException(MyPageErrorCode.UNKNOWN_POST_TYPE);
    };
  }

  private static PostResponse toPostResponse(EventPost post) {
    return PostResponse.builder()
        .eventId(post.getId())
        .postType(post.getPostType())
        .images(IntStream.range(0, post.getEventImages().size())
            .mapToObj(i -> EventImageResponse.builder()
                .sequence(i + 1) // DB sequence 컬럼이 0부터면 +1
                .imageUrl(post.getEventImages().get(i))
                .build()
            )
            .toList())
        .username(post.getUser().getUsername())
        .userImage(post.getUser().getProfileImageUrlOrDefault())
        .title(post.getTitle())
        .location(post.getLocation())
        .startAt(post.getStartAt())
        .endAt(post.getEndAt())
        .favoriteCount(post.getFavoriteCount())
        .commentCount(post.getCommentCount())
        .build();
  }

  private static PostResponse toPostResponse(ClubPost post) {
    List<String> images = post.getEventPost().getEventImages();
    return PostResponse.builder()
        .eventId(post.getEventPost().getId())
        .clubId(post.getId())
        .postType(post.getPostType())
        .images(List.of(post.getCoverImage())
            .stream()
            .map(imageUrl -> EventImageResponse.builder()
                .sequence(1) // ClubPost는 coverImage만 있으므로 sequence는 1로 설정
                .imageUrl(imageUrl)
                .build())
            .toList())
        .username(post.getUser().getUsername())
        .userImage(post.getUser().getProfileImageUrlOrDefault())
        .title(post.getTitle())
        .location(post.getLocation())
        .startAt(post.getStartAt())
        .endAt(null)
        .favoriteCount(post.getFavoriteCount())
        .commentCount(post.getCommentCount())
        .build();
  }

}
