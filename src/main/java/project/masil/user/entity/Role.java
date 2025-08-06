package project.masil.user.entity;

import io.swagger.v3.oas.annotations.media.Schema;

public enum Role {
  @Schema(description = "사용자")
  User,
  @Schema(description = "관리자")
  ADMIN,
  @Schema(description = "개발자")
  DEVELOPER;
}