package project.masil.community.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import project.masil.community.repository.EventPostRepository;

@Service
@RequiredArgsConstructor
public class EventPostUpdater {

  private final EventPostRepository eventPostRepository;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updateSummary(Long postId, String summary) {
    eventPostRepository.updateSummary(postId, summary);
  }
}