package dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

import logic.Board;

@Repository
public class BoardDao {
	private NamedParameterJdbcTemplate template;
	private Map<String,Object> param = new HashMap<>();
	private RowMapper<Board> mapper = 
			   new BeanPropertyRowMapper<>(Board.class);
	private String select = "select num, writer, pass, title, content, file1 fileurl, boardid, regdate, "
			+ "readcnt, grp, grplevel, grpstep, boardid from board";
	@Autowired
	public void setDataSource(DataSource dataSource) {
		template = new NamedParameterJdbcTemplate(dataSource);
	}
	public int maxNum() {
		return template.queryForObject
				("select ifnull(max(num),0) from board", param, Integer.class);
	}
	public void insert(Board board) {
		SqlParameterSource param = new BeanPropertySqlParameterSource(board);
		String sql = "insert into board(num, writer, pass, title, content, file1, boardid, regdate,"
				+ " readcnt, grp, grplevel, grpstep) values "
				+ "(:num, :writer, :pass, :title, :content, :fileurl, :boardid, now(),"
				+ " 0, :grp, :grplevel, :grpstep)";
		template.update(sql, param);
	}
	public int boardcount(String boardid, String searchtype, String searchcontent) {
		String sql = "select count(*) from board where boardid=:boardid";
		param.clear();
		param.put("boardid", boardid);
		if(searchtype != null && searchcontent != null) {
			sql += " and " + searchtype + " like:searchcontent";
			param.put("searchcontent","%"+searchcontent + "%");
		}
		return template.queryForObject(sql,param,Integer.class);
	}
	public List<Board> boardlist(Integer pageNum, int limit, String boardid, String searchtype, String searchcontent) {
	    param.clear();
	    String sql = select;
	    sql += " where boardid=:boardid ";
	    if(searchtype != null && searchcontent != null) {
	    	sql += " and " + searchtype + " like:searchcontent";
	    	param.put("searchcontent","%"+searchcontent + "%");
	    }
	    sql += " order by grp desc, grpstep asc limit :startrow, :limit";
	    param.put("startrow", (pageNum != null ? (pageNum-1) : 0) * limit);
	    param.put("limit", limit);
	    param.put("boardid", boardid);
	    return template.query(sql, param, mapper);
	}
	public Board selectOne(Integer num) {
		param.clear();
		String sql = select;
		param.put("num", num);
		sql += " where num=:num";
		return template.queryForObject(sql, param, mapper);
	}
	public void addReadcnt(Integer num) {
		param.clear();
		param.put("num", num);
		String sql = "update board set readcnt = readcnt + 1 where num=:num";
		template.update(sql, param);
	}
	public void grpStepAdd(int grp, int grpstep) {
		param.clear();
		param.put("grp", grp); //원글의 grp
		param.put("grpstep", grpstep); //원글의 grpstep
		String sql = "update board set grpstep = grpstep + 1 where grp = :grp and grpstep > :grpstep ";
		template.update(sql, param);
	}
	public void updatepost(Board board ) {
		SqlParameterSource param = new BeanPropertySqlParameterSource(board);
		String sql = "update board set writer=:writer, title=:title, content=:content, file1=:fileurl"
				+ " where num=:num";
		template.update(sql, param);
	}
	public void deletepost(int num) {
		param.clear();
		param.put("num", num);
		String sql = "delete from board where num=:num";
		template.update(sql, param);
		
	}

}
