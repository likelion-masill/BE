package project.masil.community.converter;

import project.masil.community.dto.response.ClubPostDetailResponse;
import project.masil.community.dto.response.ClubPostSummaryResponse;
import project.masil.community.entity.ClubPost;

public class ClubPostConverter {

  public static ClubPostDetailResponse toClubPostDetailResponse(
      ClubPost clubPost, boolean isLiked, boolean isAuthor) {
    return ClubPostDetailResponse.builder()
        .clubId(clubPost.getId())
        .username(clubPost.getUser().getUsername())
        .userImage(clubPost.getUser().getProfileImageUrlOrDefault())
        .isAuthor(isAuthor)
        .title(clubPost.getTitle())
        .content(clubPost.getContent())
        .location(clubPost.getLocation())
        .startAt(clubPost.getStartAt())
        .content(clubPost.getContent())
        .favoriteCount(clubPost.getFavoriteCount())
        .commentCount(clubPost.getCommentCount())
        .images(clubPost.getEventPost().getEventImages())
        .createdAt(clubPost.getCreatedAt())
        .isLiked(isLiked)
        .build();
  }

  public static ClubPostSummaryResponse toClubPostSummaryResponse(
      ClubPost clubPost, String coverImage, boolean isLiked, boolean isAuthor) {
    return ClubPostSummaryResponse.builder()
        .clubId(clubPost.getId())
        .username(clubPost.getUser().getUsername())
        .userImage(clubPost.getUser().getProfileImageUrlOrDefault())
        .isAuthor(isAuthor)
        .title(clubPost.getTitle())
        .content(clubPost.getContent())
        .location(clubPost.getLocation())
        .startAt(clubPost.getStartAt())
        .favoriteCount(clubPost.getFavoriteCount())
        .commentCount(clubPost.getCommentCount())
        .coverImage(coverImage)
        .createdAt(clubPost.getCreatedAt())
        .isLiked(isLiked)
        .build();
  }


}
