package project.masil.community.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.masil.community.converter.CommentConverter;
import project.masil.community.dto.response.CommentResponse;
import project.masil.community.entity.Comment;
import project.masil.community.entity.Post;
import project.masil.community.enums.PostType;
import project.masil.community.exception.CommentErrorCode;
import project.masil.community.exception.PostErrorCode;
import project.masil.community.repository.CommentRepository;
import project.masil.community.repository.PostRepository;
import project.masil.embedding.service.FeedbackService;
import project.masil.global.exception.CustomException;
import project.masil.global.response.ListResponse;
import project.masil.user.entity.User;
import project.masil.user.entity.UserActionType;
import project.masil.user.exception.UserErrorCode;
import project.masil.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

  private final CommentRepository commentRepository;
  private final UserRepository userRepository;
  private final PostRepository postRepository;
  private final FeedbackService feedbackService;

  /**
   * 댓글 작성자 userId를 반환 - 채팅 서비스에서 "댓글 컨텍스트ID로 채팅 시작" 시 대상 사용자 검증 용도 - 존재하지 않으면
   * 예외(CommentErrorCode.COMMENT_NOT_FOUND)
   *
   * @param commentId
   * @return
   */
  @Transactional(readOnly = true)
  public Long getCommentAuthorId(Long commentId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new CustomException(CommentErrorCode.COMMENT_NOT_FOUND));
    return comment.getUser().getId();
  }

  /**
   * 댓글을 생성합니다.
   *
   * @param postId       댓글이 달릴 게시글의 ID
   * @param userId       댓글 작성자의 ID
   * @param content      댓글 내용
   * @param expectedType 게시글의 예상 타입 (예: EVENT, CLUB 등)
   * @return 생성된 댓글 정보
   */
  public CommentResponse createComment(Long postId, Long userId, String content,
      PostType expectedType) {
    log.info("[서비스] 댓글 생성 시도 - postId: {}, userId: {}, content: {}", postId, userId, content);

    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_FOUND));

    if (!post.getPostType().equals(expectedType)) {
      throw new CustomException(PostErrorCode.INVALID_POST_TYPE);
    }

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    Comment comment = Comment.builder()
        .post(post)
        .user(user)
        .content(content)
        .build();
    post.incrementCommentCount();
    Comment saved = commentRepository.save(comment);

    feedbackService.handle(userId, postId, UserActionType.COMMENT);

    return CommentConverter.toCommentResponse(saved);
  }

  /**
   * 게시글에 달린 댓글을 조회합니다.
   *
   * @param postId       댓글이 달린 게시글의 ID
   * @param expectedType 게시글의 예상 타입 (예: EVENT, CLUB 등)
   * @return 댓글 목록
   */
  public ListResponse<CommentResponse> getCommentsByPost(Long postId, PostType expectedType) {
    log.info("[서비스] 댓글 조회 시도 - postId: {}", postId);
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_FOUND));

    if (!post.getPostType().equals(expectedType)) {
      throw new CustomException(PostErrorCode.INVALID_POST_TYPE);
    }

    List<Comment> comments = commentRepository.findByPostAndParentCommentIsNull(post);
    return new ListResponse<CommentResponse>(comments.size(),
        comments.stream()
            .map(CommentConverter::toCommentResponse)
            .toList());
  }

  /**
   * 대댓글을 생성합니다.
   *
   * @param postId          대댓글이 달릴 게시글의 ID
   * @param parentCommentId 부모 댓글의 ID
   * @param userId          대댓글 작성자의 ID
   * @param content         대댓글 내용
   * @param expectedType    게시글의 예상 타입 (예: EVENT, CLUB 등)
   * @return 생성된 대댓글 정보
   */
  public CommentResponse createChildComment(Long postId, Long parentCommentId, Long userId,
      String content, PostType expectedType) {
    log.info("[서비스] 대댓글 생성 시도 - eventId: {}, parentCommentId: {}, userId: {}, content: {}",
        postId, parentCommentId, userId, content);

    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_FOUND));

    if (!post.getPostType().equals(expectedType)) {
      throw new CustomException(PostErrorCode.INVALID_POST_TYPE);
    }

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    Comment parentComment = commentRepository.findById(parentCommentId)
        .orElseThrow(() -> new CustomException(CommentErrorCode.COMMENT_NOT_FOUND));

    Comment childComment = Comment.builder()
        .post(post)
        .parentComment(parentComment)
        .user(user)
        .content(content)
        .build();
    post.incrementCommentCount();

    Comment savedChildComment = commentRepository.save(childComment);

    // 대댓글은 살짝 애매
    // feedbackService.handle(userId, postId, UserActionType.COMMENT);

    return CommentConverter.toCommentResponse(savedChildComment);
  }

  /**
   * 게시글과 부모 댓글에 대한 대댓글을 조회합니다.
   *
   * @param postId          게시글의 ID
   * @param parentCommentId 부모 댓글의 ID
   * @param expectedType    게시글의 예상 타입 (예: EVENT, CLUB 등)
   * @return 대댓글 목록
   */
  public ListResponse<CommentResponse> getReplyCommentsByPostAndParentComment(
      Long postId, Long parentCommentId, PostType expectedType) {
    log.info("[서비스] 대댓글 조회 시도 - postId: {}, parentCommentId: {}", postId, parentCommentId);

    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_FOUND));

    if (!post.getPostType().equals(expectedType)) {
      throw new CustomException(PostErrorCode.INVALID_POST_TYPE);
    }

    Comment parentComment = commentRepository.findById(parentCommentId)
        .orElseThrow(() -> new CustomException(CommentErrorCode.COMMENT_NOT_FOUND));

    List<Comment> childComments = commentRepository.findByPostAndParentComment(post, parentComment);

    return new ListResponse<>(childComments.size(),
        childComments.stream()
            .map(CommentConverter::toCommentResponse)
            .toList());
  }


}
