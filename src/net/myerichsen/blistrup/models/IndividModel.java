package net.myerichsen.blistrup.models;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Erichsen
 * @version 25. jul. 2023
 */
public class IndividModel {
	private static final String SELECT1 = "SELECT * FROM BLISTRUP.INDIVID FETCH FIRST 200 ROWS ONLY";
	private static final String SELECT2 = "SELECT * FROM BLISTRUP.PERSONNAVN WHERE INDIVIDID = ?";

	/**
	 * @return
	 * @throws SQLException
	 */
	public static IndividModel[] getData(String dbPath) throws SQLException {
		IndividModel model;
		final List<IndividModel> liste = new ArrayList<>();
		final Connection conn = DriverManager.getConnection("jdbc:derby:" + dbPath);
		final PreparedStatement statement1 = conn.prepareStatement(SELECT1);
		final PreparedStatement statement2 = conn.prepareStatement(SELECT2);
		final ResultSet rs1 = statement1.executeQuery();
		ResultSet rs2;
		int id;

		while (rs1.next()) {
			id = rs1.getInt("ID");
			statement2.setInt(1, id);
			rs2 = statement2.executeQuery();

			while (rs2.next()) {
				if ("TRUE".equals(rs2.getString("PRIMAERNAVN").trim())) {
					model = new IndividModel();
					model.setId(id);
					model.setKoen(rs1.getString("KOEN").trim());
					model.setBlistrupId(rs1.getString("BLISTRUPID").trim());
					model.setFamc(rs1.getInt("FAMC"));
					model.setStdNavn(rs2.getString("STDNAVN").trim());
					model.setFonetiskNavn(rs2.getString("FONETISKNAVN").trim());
					liste.add(model);
				}
			}
		}
		final IndividModel[] array = new IndividModel[liste.size()];

		for (int i = 0; i < liste.size(); i++) {
			array[i] = liste.get(i);
		}

		return array;
	}

	private int id = 0;
	private int famc = 0;
	private String koen = "";
	private String BlistrupId = "";
	private List<PersonNavneModel> personNavneListe;
	private String fonetiskNavn = "";
	private String stdNavn = "";

	/**
	 * @return the blistrupId
	 */
	public String getBlistrupId() {
		return BlistrupId;
	}

	/**
	 * @return the famc
	 */
	public int getFamc() {
		return famc;
	}

	/**
	 * @return the fonetiskNavn
	 */
	public String getFonetiskNavn() {
		return fonetiskNavn;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the koen
	 */
	public String getKoen() {
		return koen;
	}

	/**
	 * @return the personNavneListe
	 */
	public List<PersonNavneModel> getPersonNavneListe() {
		return personNavneListe;
	}

	/**
	 * @return the stdNavn
	 */
	public String getStdNavn() {
		return stdNavn;
	}

	/**
	 * @param blistrupId the blistrupId to set
	 */
	public void setBlistrupId(String blistrupId) {
		BlistrupId = blistrupId;
	}

	/**
	 * @param i the famc to set
	 */
	public void setFamc(int i) {
		this.famc = i;
	}

	/**
	 * @param fonetiskNavn the fonetiskNavn to set
	 */
	public void setFonetiskNavn(String fonetiskNavn) {
		this.fonetiskNavn = fonetiskNavn;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @param koen the koen to set
	 */
	public void setKoen(String koen) {
		this.koen = koen;
	}

	/**
	 * @param personNavneListe the personNavneListe to set
	 */
	public void setPersonNavneListe(List<PersonNavneModel> personNavneListe) {
		this.personNavneListe = personNavneListe;
	}

	/**
	 * @param stdNavn the stdNavn to set
	 */
	public void setStdNavn(String stdNavn) {
		this.stdNavn = stdNavn;
	}

	@Override
	public String toString() {
		return id + ", " + famc + ", " + (koen != null ? koen + ", " : "")
				+ (BlistrupId != null ? BlistrupId + ", " : "")
				+ (personNavneListe != null ? personNavneListe + ", " : "")
				+ (fonetiskNavn != null ? fonetiskNavn + ", " : "") + (stdNavn != null ? stdNavn + ", " : "");
	}

}
