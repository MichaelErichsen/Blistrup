package net.myerichsen.blistrup.models;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Erichsen
 * @version 20.aug. 2023
 */
public class FamilieBegivenhedModel extends Begivenhedsmodel {
	private static final String SELECT1 = "SELECT * FROM BLISTRUP.FAMILIEBEGIVENHED";

	/**
	 * @param dbPath
	 * @return
	 * @throws SQLException
	 */
	public static FamilieBegivenhedModel[] getData(String dbPath) throws SQLException {
		FamilieBegivenhedModel model;
		final List<FamilieBegivenhedModel> liste = new ArrayList<>();
		final Connection conn = DriverManager.getConnection("jdbc:derby:" + dbPath);
		final PreparedStatement statement = conn.prepareStatement(SELECT1);
		final ResultSet rs = statement.executeQuery();

		while (rs.next()) {
			model = new FamilieBegivenhedModel();
			model.setId(rs.getInt("ID"));
			model.setFamilieId(rs.getInt("FAMILIEID"));
			model.setKildeId(rs.getInt("KILDEID"));
			model.setBegType(rs.getString("BEGTYPE").trim());
			try {
				model.setUnderType(rs.getString("UNDERTYPE").trim());
			} catch (final Exception e1) {
			}
			model.setDato(rs.getDate("DATO"));
			try {
				model.setNote(rs.getString("NOTE"));
			} catch (final Exception e) {
			}
			try {
				model.setDetaljer(rs.getString("DETALJER").trim());
			} catch (final Exception e) {
			}
			model.setBlistrupId(rs.getString("BLISTRUPID").trim());
			try {
				model.setRolle(rs.getString("ROLLE").trim());
			} catch (final Exception e1) {
			}
			try {
				model.setStedNavn(rs.getString("STEDNAVN").trim());
			} catch (final Exception e1) {
			}
			try {
				model.setBem(rs.getString("BEM").trim());
			} catch (final Exception e) {
			}
			liste.add(model);
		}

		statement.close();

		final FamilieBegivenhedModel[] array = new FamilieBegivenhedModel[liste.size()];

		for (int i = 0; i < liste.size(); i++) {
			array[i] = liste.get(i);
		}

		return array;
	}

	/**
	 * @return the select1
	 */
	public static String getSelect1() {
		return SELECT1;
	}

	private int familieBegivenhedId;
	private int familieId;
	private int husfaderAlder;
	private int husmoderAlder;
	private int kildeId;
	private String begType = "";
	private String underType = "";
	private Date dato;
	private String note = "";
	private String detaljer = "";
	private String blistrupId = "";
	private String stedNavn = "";
	private String bem = "";
	private int aar;
	private String rolle = "";
	private int gom;
	private int brud;
	private String gaard = "";

	/**
	 * @return the aar
	 */
	public int getAar() {
		return aar;
	}

	/**
	 * @return the begType
	 */
	@Override
	public String getBegType() {
		return begType;
	}

	/**
	 * @return the bem
	 */
	public String getBem() {
		return bem;
	}

	/**
	 * @return the blistrupId
	 */
	@Override
	public String getBlistrupId() {
		return blistrupId;
	}

	/**
	 * @return the brud
	 */
	public int getBrud() {
		return brud;
	}

	/**
	 * @return the dato
	 */
	@Override
	public Date getDato() {
		return dato;
	}

	/**
	 * @return the detaljer
	 */
	@Override
	public String getDetaljer() {
		return detaljer;
	}

	/**
	 * @return the familieBegivenhedId
	 */
	public int getFamilieBegivenhedId() {
		return familieBegivenhedId;
	}

	/**
	 * @return the familieId
	 */
	public int getFamilieId() {
		return familieId;
	}

	/**
	 * @return the gaard
	 */
	public String getGaard() {
		return gaard;
	}

	/**
	 * @return the gom
	 */
	public int getGom() {
		return gom;
	}

	/**
	 * @return the husfaderAlder
	 */
	public int getHusfaderAlder() {
		return husfaderAlder;
	}

	/**
	 * @return the husmoderAlder
	 */
	public int getHusmoderAlder() {
		return husmoderAlder;
	}

	/**
	 * @return the kildeId
	 */
	@Override
	public int getKildeId() {
		return kildeId;
	}

	/**
	 * @return the note
	 */
	@Override
	public String getNote() {
		return note;
	}

	/**
	 * @return the rolle
	 */
	public String getRolle() {
		return rolle;
	}

	/**
	 * @return the stedNavn
	 */
	public String getStedNavn() {
		return stedNavn;
	}

	/**
	 * @return the underType
	 */
	@Override
	public String getUnderType() {
		return underType;
	}

	/**
	 * @param aar the aar to set
	 */
	public void setAar(int aar) {
		this.aar = aar;
	}

	/**
	 * @param begType the begType to set
	 */
	@Override
	public void setBegType(String begType) {
		this.begType = begType;
	}

	/**
	 * @param bem the bem to set
	 */
	public void setBem(String bem) {
		this.bem = bem;
	}

	/**
	 * @param blistrupId the blistrupId to set
	 */
	@Override
	public void setBlistrupId(String blistrupId) {
		this.blistrupId = blistrupId;
	}

	/**
	 * @param brud the brud to set
	 */
	public void setBrud(int brud) {
		this.brud = brud;
	}

	/**
	 * @param dato the dato to set
	 */
	@Override
	public void setDato(Date dato) {
		this.dato = dato;
	}

	/**
	 * @param detaljer the detaljer to set
	 */
	@Override
	public void setDetaljer(String detaljer) {
		this.detaljer = detaljer;
	}

	/**
	 * @param familieBegivenhedId the familieBegivenhedId to set
	 */
	public void setFamilieBegivenhedId(int familieBegivenhedId) {
		this.familieBegivenhedId = familieBegivenhedId;
	}

	/**
	 * @param familieId the familieId to set
	 */
	public void setFamilieId(int familieId) {
		this.familieId = familieId;
	}

	/**
	 * @param gaard the gaard to set
	 */
	public void setGaard(String gaard) {
		this.gaard = gaard;
	}

	/**
	 * @param gom the gom to set
	 */
	public void setGom(int gom) {
		this.gom = gom;
	}

	/**
	 * @param husfaderAlder the husfaderAlder to set
	 */
	public void setHusfaderAlder(int husfaderAlder) {
		this.husfaderAlder = husfaderAlder;
	}

	/**
	 * @param husmoderAlder the husmoderAlder to set
	 */
	public void setHusmoderAlder(int husmoderAlder) {
		this.husmoderAlder = husmoderAlder;
	}

	/**
	 * @param kildeId the kildeId to set
	 */
	@Override
	public void setKildeId(int kildeId) {
		this.kildeId = kildeId;
	}

	/**
	 * @param note the note to set
	 */
	@Override
	public void setNote(String note) {
		this.note = note;
	}

	/**
	 * @param rolle the rolle to set
	 */
	public void setRolle(String rolle) {
		this.rolle = rolle;
	}

	/**
	 * @param stedNavn the stedNavn to set
	 */
	public void setStedNavn(String stedNavn) {
		this.stedNavn = stedNavn;
	}

	/**
	 * @param underType the underType to set
	 */
	@Override
	public void setUnderType(String underType) {
		this.underType = underType;
	}

	@Override
	public String toString() {
		return familieBegivenhedId + ", " + familieId + ", " + husfaderAlder + ", " + husmoderAlder + ", " + aar + ", "
				+ (rolle != null ? rolle + ", " : "") + gom + ", " + brud + ", " + (gaard != null ? gaard + ", " : "")
				+ kildeId + ", " + (note != null ? note + ", " : "") + (detaljer != null ? detaljer + ", " : "")
				+ (blistrupId != null ? blistrupId + ", " : "") + (stedNavn != null ? stedNavn + ", " : "")
				+ (bem != null ? bem + ", " : "") + id + ", " + (begType != null ? begType + ", " : "")
				+ (underType != null ? underType + ", " : "") + (dato != null ? dato : "");
	}

}
