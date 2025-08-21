package project.masil.embedding.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.masil.global.util.EmbeddingCodec;
import project.masil.user.entity.UserEmbedding;
import project.masil.user.repository.UserEmbeddingRepository;

@Service
@RequiredArgsConstructor
public class UserEmbeddingService {

  private final UserEmbeddingRepository repo;

  @Transactional(readOnly = true)
  public List<Float> loadAsFloatList(long userId) {
    return repo.findById(userId)
        .map(UserEmbedding::getEmbedding)
        .map(EmbeddingCodec::toFloatList)
        .orElse(null);
  }
}
