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
 * @version 24. jul. 2023
 *
 */
public class IndividBegivenhedModel {
	private static final String SELECT1 = "SELECT * FROM BLISTRUP.INDIVIDBEGIVENHED FETCH FIRST 200 ROWS ONLY";

	/**
	 * @param dbPath
	 * @return
	 * @throws SQLException
	 */
	public static IndividBegivenhedModel[] getData(String dbPath) throws SQLException {
		IndividBegivenhedModel model;
		final List<IndividBegivenhedModel> liste = new ArrayList<>();
		final Connection conn = DriverManager.getConnection("jdbc:derby:" + dbPath);
		final PreparedStatement statement = conn.prepareStatement(SELECT1);
		final ResultSet rs = statement.executeQuery();

		while (rs.next()) {
			model = new IndividBegivenhedModel();
			model.setId(rs.getInt("ID"));
			model.setIndividId(rs.getInt("INDIVIDID"));
			model.setAlder(rs.getInt("ALDER"));
			model.setKildeId(rs.getInt("KILDEID"));
			model.setBegType(rs.getString("BEGTYPE").trim());
			if (rs.getString("UNDERTYPE") != null) {
				model.setUnderType(rs.getString("UNDERTYPE").trim());
			} else {
				model.setUnderType("");
			}
			model.setDato(rs.getDate("DATO"));
			model.setNote(rs.getString("NOTE").trim());
			model.setDetaljer(rs.getString("DETALJER").trim());
			model.setBlistrupId(rs.getString("BLISTRUPID").trim());
			model.setRolle(rs.getString("ROLLE").trim());
			if (rs.getString("FOEDT") != null) {
				model.setFoedt(rs.getString("FOEDT").trim());
			} else {
				model.setFoedt("");
			}
			model.setStedNavn(rs.getString("STEDNAVN").trim());
			model.setBem(rs.getString("BEM").trim());
			liste.add(model);
		}

		final IndividBegivenhedModel[] array = new IndividBegivenhedModel[liste.size()];

		for (int i = 0; i < liste.size(); i++) {
			array[i] = liste.get(i);
		}

		return array;
	}

	private int id;
	private int individId;
	private int alder;
	private int kildeId;
	private String begType;
	private String underType;
	private Date dato;
	private String note;
	private String detaljer;
	private String blistrupId;
	private String rolle;
	private String foedt;
	private String stedNavn;
	private String bem;

	/**
	 * @return the alder
	 */
	public int getAlder() {
		return alder;
	}

	/**
	 * @return the begType
	 */
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
	public String getBlistrupId() {
		return blistrupId;
	}

	/**
	 * @return the dato
	 */
	public Date getDato() {
		return dato;
	}

	/**
	 * @return the detaljer
	 */
	public String getDetaljer() {
		return detaljer;
	}

	/**
	 * @return the foedt
	 */
	public String getFoedt() {
		return foedt;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the individId
	 */
	public int getIndividId() {
		return individId;
	}

	/**
	 * @return the kildeId
	 */
	public int getKildeId() {
		return kildeId;
	}

	/**
	 * @return the note
	 */
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
	public String getUnderType() {
		return underType;
	}

	/**
	 * @param alder the alder to set
	 */
	public void setAlder(int alder) {
		this.alder = alder;
	}

	/**
	 * @param begType the begType to set
	 */
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
	public void setBlistrupId(String blistrupId) {
		this.blistrupId = blistrupId;
	}

	/**
	 * @param dato the dato to set
	 */
	public void setDato(Date dato) {
		this.dato = dato;
	}

	/**
	 * @param detaljer the detaljer to set
	 */
	public void setDetaljer(String detaljer) {
		this.detaljer = detaljer;
	}

	/**
	 * @param foedt the foedt to set
	 */
	public void setFoedt(String foedt) {
		this.foedt = foedt;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @param individId the individId to set
	 */
	public void setIndividId(int individId) {
		this.individId = individId;
	}

	/**
	 * @param kildeId the kildeId to set
	 */
	public void setKildeId(int kildeId) {
		this.kildeId = kildeId;
	}

	/**
	 * @param note the note to set
	 */
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
	public void setUnderType(String underType) {
		this.underType = underType;
	}

	@Override
	public String toString() {
		return id + ", " + individId + ", " + alder + ", " + kildeId + ", " + (begType != null ? begType + ", " : "")
				+ (underType != null ? underType + ", " : "") + (dato != null ? dato + ", " : "")
				+ (note != null ? note + ", " : "") + (detaljer != null ? detaljer + ", " : "")
				+ (blistrupId != null ? blistrupId + ", " : "") + (rolle != null ? rolle + ", " : "")
				+ (foedt != null ? foedt + ", " : "") + (stedNavn != null ? stedNavn + ", " : "")
				+ (bem != null ? bem : "");
	}

}
