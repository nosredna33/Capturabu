package dev.qrtext.repository;

import dev.qrtext.model.Models.Capture;
import java.sql.ResultSet;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CaptureRepository {
  private final JdbcTemplate db;
  public CaptureRepository(JdbcTemplate db) { this.db=db; }
  private Capture map(ResultSet r,int n)throws java.sql.SQLException{return new Capture(r.getLong("id"),r.getLong("user_id"),r.getString("content"),r.getString("source_name"),r.getTimestamp("created_at").toLocalDateTime());}
  public List<Capture> findByUser(long userId){return db.query("select * from qr_captures where user_id=? order by id desc",this::map,userId);}
  public void save(long userId,String content,String source){db.update("insert into qr_captures(user_id,content,source_name) values(?,?,?)",userId,content,source);}
  public void delete(long id,long userId){db.update("delete from qr_captures where id=? and user_id=?",id,userId);}
}
