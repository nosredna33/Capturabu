package dev.qrtext.web;

import dev.qrtext.model.Models.User;
import dev.qrtext.repository.CaptureRepository;
import dev.qrtext.repository.UserRepository;
import dev.qrtext.service.AuthService;
import dev.qrtext.service.QrService;
import java.security.Principal;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class WebControllers {
  private final AuthService auth; private final UserRepository users; private final QrService qr;
  public WebControllers(AuthService auth,UserRepository users,QrService qr){this.auth=auth;this.users=users;this.qr=qr;}
  @GetMapping("/") String home(){return "home";}
  @GetMapping("/login") String login(){return "login";}
  @GetMapping("/cadastrar") String register(){return "register";}
  @PostMapping("/cadastrar") String register(@RequestParam String name,@RequestParam String email,@RequestParam String password,Model m){try{auth.register(name,email,password);m.addAttribute("message","Cadastro criado. Verifique seu e-mail para confirmar.");}catch(IllegalArgumentException e){m.addAttribute("error",e.getMessage());}return "register";}
  @GetMapping("/confirmar") String confirm(@RequestParam String token,Model m){m.addAttribute("confirmed",auth.confirm(token));return "confirm";}
  @GetMapping("/capturas") String captures(Model m,Principal p){User u=users.findByEmail(p.getName()).orElseThrow();m.addAttribute("captures",qr.captures().findByUser(u.id()));return "captures";}
  @PostMapping("/capturas/ler") String decode(@RequestParam MultipartFile image,Principal p,Model m){try{User u=users.findByEmail(p.getName()).orElseThrow();String text=qr.decode(image.getInputStream());qr.captures().save(u.id(),text,image.getOriginalFilename());m.addAttribute("success","QR Code lido com sucesso.");}catch(Exception e){m.addAttribute("error","Não foi possível ler um QR Code nessa imagem.");}return captures(m,p);}
  @PostMapping("/capturas/salvar-texto") String saveText(@RequestParam String content,@RequestParam(required=false,defaultValue="imagem") String sourceName,Principal p,Model m){User u=users.findByEmail(p.getName()).orElseThrow();if(content.isBlank())m.addAttribute("error","Nenhum texto de QR Code foi detectado.");else{qr.captures().save(u.id(),content,sourceName);m.addAttribute("success","QR Code lido e salvo com sucesso.");}return captures(m,p);}
  @PostMapping("/capturas/{id}/excluir") String delete(@PathVariable long id,Principal p){users.findByEmail(p.getName()).ifPresent(u->qr.captures().delete(id,u.id()));return "redirect:/capturas";}
  @GetMapping("/csrf-token") @ResponseBody Map<String,String> csrfToken(CsrfToken token){return Map.of("parameterName",token.getParameterName(),"token",token.getToken());}
  @GetMapping("/usuarios") String users(Model m){m.addAttribute("users",users.findAll());return "users";}
  @PostMapping("/usuarios/{id}/editar") String edit(@PathVariable long id,@RequestParam String name,@RequestParam String email,@RequestParam String role,@RequestParam(defaultValue="false") boolean enabled){users.update(id,name,email,role,enabled);return "redirect:/usuarios";}
  @PostMapping("/usuarios/{id}/excluir") String remove(@PathVariable long id){users.delete(id);return "redirect:/usuarios";}
}
