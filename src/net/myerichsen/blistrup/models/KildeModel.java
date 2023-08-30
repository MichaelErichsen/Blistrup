package net.myerichsen.blistrup.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Klasse der repræsenterer en kilde
 *
 * @author Michael Erichsen
 * @version 27. aug. 2023
 *
 */
public class KildeModel {
	private static final String SELECT1 = "SELECT * FROM BLISTRUP.KILDE WHERE KBNR = ? AND AARINTERVAL = ?";
	private static final String INSERT1 = "INSERT INTO BLISTRUP.KILDE (KBNR, AARINTERVAL, KBDEL, TIFNR, SIDE, OPSLAG, OPNR) VALUES (?, ?, ?, ?, ?, ?, ?)";

	private int id = 0;
	private String kbNr = "";
	private String aarInterval = "";
	private String kbDel = "";
	private String tifNr = "";
	private String side = "";
	private String opslag = "";
	private String opNr = "";

	/**
	 * @return the aarInterval
	 */
	public String getAarInterval() {
		return aarInterval;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the kbDel
	 */
	public String getKbDel() {
		return kbDel;
	}

	/**
	 * @return the kbNr
	 */
	public String getKbNr() {
		return kbNr;
	}

	/**
	 * @return the opNr
	 */
	public String getOpNr() {
		return opNr;
	}

	/**
	 * @return the opslag
	 */
	public String getOpslag() {
		return opslag;
	}

	/**
	 * @return the side
	 */
	public String getSide() {
		return side;
	}

	/**
	 * @return the tifNr
	 */
	public String getTifNr() {
		return tifNr;
	}

	/**
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public int insert(Connection conn) throws SQLException {
		int id = 0;

		PreparedStatement statement = conn.prepareStatement(SELECT1);
		statement.setString(1, kbNr);
		statement.setString(2, aarInterval);
		final ResultSet rs = statement.executeQuery();

		if (rs.next()) {
			return rs.getInt("ID");
		}

		statement = conn.prepareStatement(INSERT1, Statement.RETURN_GENERATED_KEYS);
		statement.setString(1, kbNr);
		statement.setString(2, aarInterval);
		statement.setString(3, kbDel);
		statement.setString(4, tifNr);
		statement.setString(5, side);
		statement.setString(6, opslag);
		statement.setString(7, opNr);
		statement.executeUpdate();

		final ResultSet generatedKeys = statement.getGeneratedKeys();

		if (generatedKeys.next()) {
			id = generatedKeys.getInt(1);
		} else {
			id = 0;
		}
		generatedKeys.close();

		return id;
	}

	/**
	 * @param aarInterval the aarInterval to set
	 */
	public void setAarInterval(String aarInterval) {
		this.aarInterval = aarInterval;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @param kbDel the kbDel to set
	 */
	public void setKbDel(String kbDel) {
		this.kbDel = kbDel;
	}

	/**
	 * @param kbNr the kbNr to set
	 */
	public void setKbNr(String kbNr) {
		this.kbNr = kbNr;
	}

	/**
	 * @param opNr the opNr to set
	 */
	public void setOpNr(String opNr) {
		this.opNr = opNr;
	}

	/**
	 * @param opslag the opslag to set
	 */
	public void setOpslag(String opslag) {
		this.opslag = opslag;
	}

	/**
	 * @param side the side to set
	 */
	public void setSide(String side) {
		this.side = side;
	}

	/**
	 * @param tifNr the tifNr to set
	 */
	public void setTifNr(String tifNr) {
		this.tifNr = tifNr;
	}
}
