package dev.qrtext.config;

import dev.qrtext.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class TestUserInitializer implements CommandLineRunner {
  private final UserRepository users;
  private final PasswordEncoder passwordEncoder;
  @Value("${app.test-user.enabled:false}") private boolean enabled;
  @Value("${app.test-user.name:Usuário de Testes}") private String name;
  @Value("${app.test-user.email:teste@localhost}") private String email;
  @Value("${app.test-user.password:Teste123!}") private String password;

  public TestUserInitializer(UserRepository users, PasswordEncoder passwordEncoder) {
    this.users = users;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(String... args) {
    if (!enabled) return;
    String hash = passwordEncoder.encode(password);
    users.findByEmail(email).ifPresentOrElse(
        user -> {
          users.updateTestCredentials(user.id(), name, hash);
          System.out.printf("Usuário de testes atualizado e confirmado: %s%n", email);
        },
        () -> {
          users.createConfirmed(name, email, hash);
          System.out.printf("Usuário de testes criado e confirmado: %s%n", email);
        });
  }
}
