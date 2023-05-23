package dao;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import logic.SaleItem;

@Repository
public class SaleItemDao {
	private NamedParameterJdbcTemplate template;
	private Map<String, Object> param = new HashMap<>();
	private RowMapper<SaleItem> mapper = new BeanPropertyRowMapper<>(SaleItem.class);
	@Autowired
	public void setDataSource(DataSource dataSource) {
		template = new NamedParameterJdbcTemplate(dataSource);
	}
	public void insert(SaleItem saleItem) {
		String sql = "insert into saleitem (saleid,seq,itemid,quantity)"
				+ " values (:saleid,:seq,:itemid,:quantity)";
		SqlParameterSource param = new BeanPropertySqlParameterSource(saleItem);
		template.update(sql, param);
	}
}
