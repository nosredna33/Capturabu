package dev.qrtext.repository;

import dev.qrtext.model.Models.User;
import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
  private final JdbcTemplate db;
  public UserRepository(JdbcTemplate db) { this.db = db; }
  private User map(ResultSet r, int n) throws java.sql.SQLException { return new User(r.getLong("id"), r.getString("name"), r.getString("email"), r.getString("password_hash"), r.getString("role"), r.getBoolean("enabled"), r.getString("confirmation_token"), r.getTimestamp("created_at").toLocalDateTime()); }
  public Optional<User> findByEmail(String email) { return db.query("select * from users where lower(email)=lower(?)", this::map, email).stream().findFirst(); }
  public Optional<User> findById(long id) { return db.query("select * from users where id=?", this::map, id).stream().findFirst(); }
  public Optional<User> findByToken(String token) { return db.query("select * from users where confirmation_token=?", this::map, token).stream().findFirst(); }
  public List<User> findAll() { return db.query("select * from users order by id desc", this::map); }
  public long create(String name, String email, String hash) { String token=UUID.randomUUID().toString(); db.update("insert into users(name,email,password_hash,confirmation_token) values(?,?,?,?)",name,email,hash,token); return db.queryForObject("select id from users where email=?",Long.class,email); }
  public long createConfirmed(String name, String email, String hash) { db.update("insert into users(name,email,password_hash,role,enabled,confirmation_token) values(?,?,?,'USER',1,null)",name,email,hash); return db.queryForObject("select id from users where email=?",Long.class,email); }
  public void updateTestCredentials(long id, String name, String hash) { db.update("update users set name=?, password_hash=?, enabled=1, confirmation_token=null where id=?", name, hash, id); }
  public void confirm(long id) { db.update("update users set enabled=1, confirmation_token=null where id=?",id); }
  public void update(long id, String name, String email, String role, boolean enabled) { db.update("update users set name=?, email=?, role=?, enabled=? where id=?",name,email,role,enabled,id); }
  public void delete(long id) { db.update("delete from users where id=?",id); }
}
