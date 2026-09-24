package dev.qrtext.model;

import java.time.LocalDateTime;

public final class Models {
  private Models() {}
  public record User(long id, String name, String email, String passwordHash, String role, boolean enabled, String confirmationToken, LocalDateTime createdAt) {}
  public record Capture(long id, long userId, String content, String sourceName, LocalDateTime createdAt) {}
}
