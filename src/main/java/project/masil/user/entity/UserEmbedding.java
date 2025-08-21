package project.masil.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import project.masil.global.common.BaseTimeEntity;

@Entity
@Getter
@Setter
@Table(name = "user_embedding")
public class UserEmbedding extends BaseTimeEntity {

  @Id
  private Long userId;

  @Lob
  @Column(columnDefinition = "LONGBLOB", nullable = false)
  private byte[] embedding; // float32[1536] 직렬화 바이트

  @Column(nullable = false)
  private int obsCount = 0;

}
