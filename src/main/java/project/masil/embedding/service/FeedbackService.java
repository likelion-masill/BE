package project.masil.embedding.service;

import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.masil.community.entity.PostEmbedding;
import project.masil.community.repository.PostEmbeddingRepository;
import project.masil.global.util.EmbeddingCodec;
import project.masil.global.util.Vec;
import project.masil.user.entity.UserActionType;
import project.masil.user.entity.UserEmbedding;
import project.masil.user.entity.UserEventLog;
import project.masil.user.repository.UserEmbeddingRepository;
import project.masil.user.repository.UserEventLogRepository;

@Service
@Transactional
@AllArgsConstructor
public class FeedbackService {

  private final UserEmbeddingRepository userEmbeddingRepository;
  private final PostEmbeddingRepository postEmbeddingRepository;
  private final UserEventLogRepository userEventLogRepository;

  // 행동별 가중치
  private static final Map<UserActionType, Float> W = Map.of(
      UserActionType.VIEW, 1f,
      UserActionType.COMMENT, 2f,
      UserActionType.FAVORITE_ADD, 3f,
      UserActionType.FAVORITE_REMOVE, 0.5f
  );

  private static final float ETA = 0.1f; // 기본 학습률

  public void handle(long userId, long postId, UserActionType action) {
    // embedding 값 존재 여부 확인
    if (!postEmbeddingRepository.existsByPostId(postId)) {
      return;
    }

    // 1) 행동 로그 기록
    UserEventLog log = new UserEventLog();
    log.setUserId(userId);
    log.setPostId(postId);
    log.setAction(action);
    userEventLogRepository.save(log);

    // 2) 타겟 벡터 준비
    PostEmbedding pe = postEmbeddingRepository.findById(postId)
        .orElseThrow(() -> new IllegalArgumentException("post embedding not found"));
    float[] x = EmbeddingCodec.fromBytes(pe.getEmbedding());

    if (action == UserActionType.FAVORITE_REMOVE) {
      for (int i = 0; i < x.length; i++) {
        x[i] = -x[i]; // 부정 신호
      }
    }
    x = Vec.l2norm(x);

    // 3) 사용자 벡터 불러와 EMA 업데이트
    UserEmbedding ue = userEmbeddingRepository.findById(userId).orElse(null);
    if (ue == null) {
      ue = new UserEmbedding();
      ue.setUserId(userId);
      ue.setEmbedding(EmbeddingCodec.toBytes(x)); // 첫 신호는 그대로 초기화(정규화 상태)
      userEmbeddingRepository.save(ue);
      return;
    }

    float[] u = EmbeddingCodec.fromBytes(ue.getEmbedding());
    float w = W.get(action);
    float alpha = Math.min(1f, Math.max(0f, ETA * w));

    float[] uNew = Vec.ema(u, x, alpha);
    ue.setEmbedding(EmbeddingCodec.toBytes(uNew));
    userEmbeddingRepository.save(ue);
  }
}
