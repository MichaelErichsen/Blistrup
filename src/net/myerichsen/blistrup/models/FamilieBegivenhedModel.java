package net.myerichsen.blistrup.models;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Erichsen
 * @version 29. aug. 2023
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

	private int familieBegivenhedId = 0;
	private int familieId = 0;
	private int husfaderAlder = 0;
	private int husmoderAlder = 0;
	private int kildeId = 0;
	private String begType = "";
	private String underType = "";
	private Date dato;
	private String note = "";
	private String detaljer = "";
	private String blistrupId = "";
	private String stedNavn = "";
	private String bem = "";
	private int aar = 0;
	private String rolle = "";
	private int gom = 0;
	private int brud = 0;
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
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public int insert(Connection conn) throws SQLException {
		final String INSERT1 = "INSERT INTO BLISTRUP.FAMILIEBEGIVENHED "
				+ "(FAMILIEID, HUSFADERALDER, HUSMODERALDER, KILDEID, BEGTYPE, "
				+ "UNDERTYPE, DATO, NOTE, DETALJER, BLISTRUPID, STEDNAVN, BEM) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		int familieBegivenhedId = 0;

		final PreparedStatement statement = conn.prepareStatement(INSERT1, Statement.RETURN_GENERATED_KEYS);
		statement.setInt(1, familieId);
		statement.setInt(2, husfaderAlder);
		statement.setInt(3, husmoderAlder);
		statement.setInt(4, kildeId);
		statement.setString(5, begType);
		statement.setString(6, underType);
		if (dato != null) {
			statement.setDate(7, dato);
		} else {
			statement.setDate(7, new Date(1));
		}
		statement.setString(8, note);
		statement.setString(9, detaljer);
		statement.setString(10, blistrupId);
		statement.setString(11, stedNavn);
		statement.setString(12, bem);
		statement.executeUpdate();

		final ResultSet generatedKeys = statement.getGeneratedKeys();

		if (generatedKeys.next()) {
			familieBegivenhedId = generatedKeys.getInt(1);
		} else {
			familieBegivenhedId = 0;
		}
		generatedKeys.close();

		return familieBegivenhedId;
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
		StringBuilder builder = new StringBuilder();
		builder.append(familieBegivenhedId);
		builder.append(", ");
		builder.append(familieId);
		builder.append(", ");
		builder.append(husfaderAlder);
		builder.append(", ");
		builder.append(husmoderAlder);
		builder.append(", ");
		builder.append(kildeId);
		builder.append(", ");
		if (begType != null) {
			builder.append(begType);
			builder.append(", ");
		}
		if (underType != null) {
			builder.append(underType);
			builder.append(", ");
		}
		if (dato != null) {
			builder.append(dato);
			builder.append(", ");
		}
		if (note != null) {
			builder.append(note);
			builder.append(", ");
		}
		if (detaljer != null) {
			builder.append(detaljer);
			builder.append(", ");
		}
		if (blistrupId != null) {
			builder.append(blistrupId);
			builder.append(", ");
		}
		if (stedNavn != null) {
			builder.append(stedNavn);
			builder.append(", ");
		}
		if (bem != null) {
			builder.append(bem);
			builder.append(", ");
		}
		builder.append(aar);
		builder.append(", ");
		if (rolle != null) {
			builder.append(rolle);
			builder.append(", ");
		}
		builder.append(gom);
		builder.append(", ");
		builder.append(brud);
		builder.append(", ");
		if (gaard != null)
			builder.append(gaard);
		return builder.toString();
	}

}
