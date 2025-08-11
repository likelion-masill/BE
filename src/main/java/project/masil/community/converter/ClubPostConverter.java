package project.masil.community.converter;

import project.masil.community.dto.response.ClubPostDetailResponse;
import project.masil.community.dto.response.ClubPostSummaryResponse;
import project.masil.community.entity.ClubPost;

public class ClubPostConverter {

  public static ClubPostDetailResponse toClubPostDetailResponse(
      ClubPost clubPost) {
    return ClubPostDetailResponse.builder()
        .clubId(clubPost.getId())
        .username(clubPost.getUser().getUsername())
        .userImage(clubPost.getUser().getProfileImageUrl())
        .title(clubPost.getTitle())
        .location(clubPost.getLocation())
        .startAt(clubPost.getStartAt())
        .content(clubPost.getContent())
        .favoriteCount(clubPost.getFavoriteCount())
        .commentCount(clubPost.getCommentCount())
        .images(clubPost.getEventPost().getEventImages())
        .createdAt(clubPost.getCreatedAt())
        .build();
  }

  // TODO : CoverImage 디폴트 이미지 설정 필요
  public static ClubPostSummaryResponse toClubPostSummaryResponse(
      ClubPost clubPost, String coverImage) {
    return ClubPostSummaryResponse.builder()
        .clubId(clubPost.getId())
        .title(clubPost.getTitle())
        .location(clubPost.getLocation())
        .startAt(clubPost.getStartAt())
        .favoriteCount(clubPost.getFavoriteCount())
        .commentCount(clubPost.getCommentCount())
        .coverImage(coverImage == null ? null
            : coverImage)
        .createdAt(clubPost.getCreatedAt())
        .build();
  }


}
