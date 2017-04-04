package com.techelevator.city.model;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

@Component
public class JDBCLandmarkDAO implements LandmarkDAO {

	private JdbcTemplate jdbcTemplate;

	@Autowired
	public JDBCLandmarkDAO(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public void addLandmark(Landmark newLandmark) {
		String sqlInsertLandmark = "INSERT INTO landmark("
				+ "id, reviewid, name, longitude, latitude, address, website, open_time, close_time, phone)"
				+ "VALUES(?,?,?,?,?,?,?,?,?,?)";
		Long id = getNextId();
		jdbcTemplate.update(sqlInsertLandmark, id, newLandmark.getId(), newLandmark.getReviewId(),
				newLandmark.getLongitude(), newLandmark.getLatitude(), newLandmark.getAddress(),
				newLandmark.getWebsite(), newLandmark.getOpenTime(), newLandmark.getCloseTime(),
				newLandmark.getPhone());
				newLandmark.setId(id);
	}

	@Override
	public Landmark searchLandmarkById(int landmarkId) {
		Landmark landmark = null;
		String sqlSelectLandmarkById ="SELECT * FROM landmark WHERE id = ?";
		SqlRowSet results = jdbcTemplate.queryForRowSet(sqlSelectLandmarkById, landmarkId);
		if(results.next()) {
			landmark = mapRowToLandmark(results);
		}
		return landmark;
	}

	private Landmark mapRowToLandmark(SqlRowSet results) {
		Landmark landmark = new Landmark();
		landmark.setId(results.getLong("id"));
		landmark.setName(results.getString("name"));
		landmark.setLongitude(results.getFloat("longitude"));
		landmark.setLatitude(results.getFloat("latitude"));
		landmark.setAddress(results.getString("address"));
		landmark.setWebsite(results.getString("website"));
		landmark.setOpenTime(results.getInt("open_time"));
		landmark.setCloseTime(results.getInt("close_time"));
		landmark.setPhone(results.getString("phone"));
		landmark.setReviewId(results.getInt("reviewId"));
		return landmark;
	}

	private Long getNextId() {
		String sqlSelectNextId = "SELECT NEXTVAL('seq_landmark_id')";
		SqlRowSet result = jdbcTemplate.queryForRowSet(sqlSelectNextId);
		if (result.next()) {
			return result.getLong(1);
		} else {
			throw new RuntimeException("Something went wrong while getting the next order id");
		}
	}


}