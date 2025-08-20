package project.masil.community.entity;

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
@Table(name = "post_embedding")
public class PostEmbedding extends BaseTimeEntity {

  @Id
  private Long postId;

  @Lob
  @Column(columnDefinition = "LONGBLOB", nullable = false)
  private byte[] embedding; // float32[1536] 직렬화 바이트

}
