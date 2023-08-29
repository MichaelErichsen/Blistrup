package net.myerichsen.blistrup.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Personnavne
 *
 * @author Michael Erichsen
 * @version 27. jul. 2023
 *
 */
public class PersonNavneModel {
	private int id = 0;
	private int individId = 0;
	private String prefix = "";
	private String fornavn = "";
	private String kaelenavn = "";
	private String efternavnePrefix = "";
	private String efternavn = "";
	private String suffix = "";
	private boolean primaerNavn = false;
	private String fonetiskNavn = "";
	private String noter = "";
	private String stdnavn = "";

	/**
	 * @return the stdnavn
	 */
	public String getStdnavn() {
		return stdnavn;
	}

	/**
	 * @param stdnavn the stdnavn to set
	 */
	public void setStdnavn(String stdnavn) {
		this.stdnavn = stdnavn;
	}

	/**
	 * @return the efternavn
	 */
	public String getEfternavn() {
		return efternavn;
	}

	/**
	 * @return
	 */
	public String getEfternavnePrefix() {
		return efternavnePrefix;
	}

	/**
	 * @return
	 */
	public String getEternavn() {
		return efternavn;
	}

	public String getFonetiskNavn() {
		return fonetiskNavn;
	}

	public String getFornavn() {
		return fornavn;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	public int getIndividId() {
		return individId;
	}

	public String getKaelenavn() {
		return kaelenavn;
	}

	/**
	 * @return the noter
	 */
	public String getNoter() {
		return noter;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public boolean isPrimaerNavn() {
		return primaerNavn;
	}

	/**
	 * @param efternavn the efternavn to set
	 */
	public void setEfternavn(String efternavn) {
		this.efternavn = efternavn;
	}

	public void setEfternavnePrefix(String efternavnePrefix) {
		this.efternavnePrefix = efternavnePrefix;
	}

	public void setEternavn(String eternavn) {
		this.efternavn = eternavn;
	}

	public void setFonetiskNavn(String fonetiskNavn) {
		this.fonetiskNavn = fonetiskNavn;
	}

	public void setFornavn(String fornavn) {
		this.fornavn = fornavn;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	public void setIndividId(int individId2) {
		this.individId = individId2;
	}

	public void setKaelenavn(String kaelenavn) {
		this.kaelenavn = kaelenavn;
	}

	/**
	 * @param noter the noter to set
	 */
	public void setNoter(String noter) {
		this.noter = noter;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void setPrimaerNavn(boolean primaerNavn) {
		this.primaerNavn = primaerNavn;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	@Override
	public String toString() {
		return id + ", " + individId + ", " + (prefix != null ? prefix + ", " : "")
				+ (fornavn != null ? fornavn + ", " : "") + (kaelenavn != null ? kaelenavn + ", " : "")
				+ (efternavnePrefix != null ? efternavnePrefix + ", " : "")
				+ (efternavn != null ? efternavn + ", " : "") + (suffix != null ? suffix + ", " : "") + primaerNavn
				+ ", " + (fonetiskNavn != null ? fonetiskNavn + ", " : "") + (noter != null ? noter : "");
	}

	/**
	 * @param conn
	 * @throws SQLException
	 */
	public int insert(Connection conn) throws SQLException {
		String INSERT1 = "INSERT INTO BLISTRUP.PERSONNAVN "
				+ "(INDIVIDID, STDNAVN, FORNAVN, EFTERNAVN, PRIMAERNAVN, FONETISKNAVN) " + "VALUES(?, ?, ?, ?, ?, ?)";

		int nameId = 0;

		final PreparedStatement statement = conn.prepareStatement(INSERT1, Statement.RETURN_GENERATED_KEYS);
		statement.setInt(1, individId);
		statement.setString(2, stdnavn);
		statement.setString(3, fornavn);
		statement.setString(4, efternavn);
		statement.setString(5, "TRUE");
		statement.setString(6, fonetiskNavn);
		statement.executeUpdate();

		final ResultSet generatedKeys = statement.getGeneratedKeys();

		if (generatedKeys.next()) {
			nameId = generatedKeys.getInt(1);
		} else {
			nameId = 0;
		}
		generatedKeys.close();

		return nameId;
	}
}
