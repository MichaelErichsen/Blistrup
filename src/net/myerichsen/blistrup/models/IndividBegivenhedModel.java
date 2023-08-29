package net.myerichsen.blistrup.models;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Erichsen
 * @version 28. aug. 2023
 *
 */
public class IndividBegivenhedModel {
	private static final String SELECT1 = "SELECT * FROM BLISTRUP.INDIVIDBEGIVENHED";
	private static final String SELECT2 = "SELECT STDNAVN FROM BLISTRUP.PERSONNAVN WHERE INDIVIDID = ?";
	private static final String INSERT1 = "INSERT INTO BLISTRUP.INDIVIDBEGIVENHED "
			+ " (INDIVIDID, ALDER, KILDEID, BEGTYPE, DATO, NOTE, FOEDT, STEDNAVN) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

	/**
	 * @param dbPath
	 * @return
	 * @throws SQLException
	 */
	public static IndividBegivenhedModel[] getData(String dbPath) throws SQLException {
		IndividBegivenhedModel model;
		final List<IndividBegivenhedModel> liste = new ArrayList<>();
		final Connection conn = DriverManager.getConnection("jdbc:derby:" + dbPath);
		final PreparedStatement statement1 = conn.prepareStatement(SELECT1);
		final PreparedStatement statement2 = conn.prepareStatement(SELECT2);
		final ResultSet rs1 = statement1.executeQuery();
		int individId;

		while (rs1.next()) {
			model = new IndividBegivenhedModel();
			model.setId(rs1.getInt("ID"));
			individId = rs1.getInt("INDIVIDID");
			model.setIndividId(individId);
			model.setAlder(rs1.getInt("ALDER"));
			model.setKildeId(rs1.getInt("KILDEID"));
			model.setBegType(rs1.getString("BEGTYPE").trim());
			if (rs1.getString("UNDERTYPE") != null) {
				model.setUnderType(rs1.getString("UNDERTYPE").trim());
			} else {
				model.setUnderType("");
			}
			model.setDato(rs1.getString("DATO"));
			if (rs1.getString("NOTE") != null) {
				model.setNote(rs1.getString("NOTE"));
			} else {
				model.setNote("");
			}
			if (rs1.getString("DETALJER") != null) {
				model.setDetaljer(rs1.getString("DETALJER").trim());
			} else {
				model.setDetaljer("");
			}
			model.setBlistrupId(rs1.getString("BLISTRUPID").trim());
			if (rs1.getString("ROLLE") != null) {
				model.setRolle(rs1.getString("ROLLE").trim());
			} else {
				model.setRolle("");
			}
			if (rs1.getString("FOEDT") != null) {
				model.setFoedt(rs1.getString("FOEDT").trim());
			} else {
				model.setFoedt("");
			}
			model.setStedNavn(rs1.getString("STEDNAVN").trim());
			model.setBem(rs1.getString("BEM").trim());

//			statement2.setInt(1, individId);
//			rs2 = statement2.executeQuery();

			liste.add(model);
		}

		statement1.close();
		statement2.close();

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
	private String dato;
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
	public String getDato() {
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
	 * @return the stdNavn
	 */
//	public String getStdNavn() {
//		return stdNavn;
//	}

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
	 * @param conn
	 * @throws SQLException
	 */
	public int insert(Connection conn) throws SQLException {

		int individBegivenhedId = 0;

		final PreparedStatement statement = conn.prepareStatement(INSERT1, Statement.RETURN_GENERATED_KEYS);
		statement.setInt(1, individId);
		statement.setInt(2, alder);
		statement.setInt(3, kildeId);
		statement.setString(4, begType);
		statement.setString(5, dato);
		statement.setString(6, note);
		statement.setString(7, foedt);
		statement.setString(8, stedNavn);

		statement.executeUpdate();

		final ResultSet generatedKeys = statement.getGeneratedKeys();

		if (generatedKeys.next()) {
			individBegivenhedId = generatedKeys.getInt(1);
		} else {
			individBegivenhedId = 0;
		}
		generatedKeys.close();

		return individBegivenhedId;
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
	 * @param string the dato to set
	 */
	public void setDato(String string) {
		this.dato = string;
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
