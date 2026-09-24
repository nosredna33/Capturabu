package dev.qrtext.service;

import dev.qrtext.model.Models.User;
import dev.qrtext.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements UserDetailsService {
  private final UserRepository users; private final PasswordEncoder encoder; private final JavaMailSender mail;
  @Value("${app.base-url}") private String baseUrl; @Value("${app.mail.from}") private String from;
  public AuthService(UserRepository users, PasswordEncoder encoder, JavaMailSender mail){this.users=users;this.encoder=encoder;this.mail=mail;}
  @Override public UserDetails loadUserByUsername(String email){ User u=users.findByEmail(email).orElseThrow(()->new UsernameNotFoundException(email)); return org.springframework.security.core.userdetails.User.withUsername(u.email()).password(u.passwordHash()).roles(u.role()).disabled(!u.enabled()).build(); }
  public void register(String name,String email,String password){ if(users.findByEmail(email).isPresent()) throw new IllegalArgumentException("E-mail já cadastrado"); long id=users.create(name,email,encoder.encode(password)); users.findById(id).ifPresent(this::sendConfirmation); }
  private void sendConfirmation(User u){ SimpleMailMessage m=new SimpleMailMessage();m.setFrom(from);m.setTo(u.email());m.setSubject("Confirme seu cadastro");m.setText("Confirme seu cadastro: "+baseUrl+"/confirmar?token="+u.confirmationToken()); try{mail.send(m);}catch(Exception e){System.out.println("Confirmação (SMTP não configurado): "+baseUrl+"/confirmar?token="+u.confirmationToken());}}
  public boolean confirm(String token){return users.findByToken(token).map(u->{users.confirm(u.id());return true;}).orElse(false);}
}
