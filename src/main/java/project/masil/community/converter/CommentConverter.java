package project.masil.community.converter;

import project.masil.community.dto.response.CommentResponse;
import project.masil.community.entity.Comment;

public class CommentConverter {

  public static CommentResponse toCommentResponse(Comment comment) {
    return CommentResponse.builder()
        .commentId(comment.getId())
        .content(comment.getContent())
        .username(comment.getUser().getUsername())
        .userProfileImageUrl(comment.getUser().getProfileImageUrl())
        .createdAt(comment.getCreatedAt())
        .replyCommentCount(comment.getReplies().size())
        .build();
  }

}
