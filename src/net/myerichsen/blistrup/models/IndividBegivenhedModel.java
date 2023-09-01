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
 * @version 31. aug. 2023
 *
 */
public class IndividBegivenhedModel extends Begivenhedsmodel {
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
			model.setDato(rs1.getDate("DATO"));
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
			try {
				model.setBlistrupId(rs1.getString("BLISTRUPID").trim());
			} catch (Exception e) {
			}
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
			try {
				model.setBem(rs1.getString("BEM").trim());
			} catch (Exception e) {
			}

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

	private int individId;
	private int alder;
	private String rolle;
	private String foedt;
	private String bem;

	/**
	 * @return the alder
	 */
	public int getAlder() {
		return alder;
	}

	/**
	 * @return the bem
	 */
	public String getBem() {
		return bem;
	}

	/**
	 * @return the foedt
	 */
	public String getFoedt() {
		return foedt;
	}

	/**
	 * @return the individId
	 */
	public int getIndividId() {
		return individId;
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
		statement.setDate(5, dato);
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
	 * @param bem the bem to set
	 */
	public void setBem(String bem) {
		this.bem = bem;
	}

	/**
	 * @param foedt the foedt to set
	 */
	public void setFoedt(String foedt) {
		this.foedt = foedt;
	}

	/**
	 * @param individId the individId to set
	 */
	public void setIndividId(int individId) {
		this.individId = individId;
	}

	/**
	 * @param rolle the rolle to set
	 */
	public void setRolle(String rolle) {
		this.rolle = rolle;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append(individId);
		builder.append(", ");
		builder.append(alder);
		builder.append(", ");
		if (rolle != null) {
			builder.append(rolle);
			builder.append(", ");
		}
		if (foedt != null) {
			builder.append(foedt);
			builder.append(", ");
		}
		if (bem != null) {
			builder.append(bem);
			builder.append(", ");
		}
		if (getClass() != null) {
			builder.append(getClass());
			builder.append(", ");
		}
		builder.append(hashCode());
		builder.append(", ");
		if (super.toString() != null) {
			builder.append(super.toString());
		}
		return builder.toString();
	}

}
