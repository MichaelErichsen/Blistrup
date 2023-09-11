package net.myerichsen.blistrup.loaders;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.myerichsen.blistrup.models.KildeModel;

/**
 * Indlæs matrikler
 *
 * @author Michael Erichsen
 * @version 10. sep. 2023
 *
 */
public class MatrikelLoader extends AbstractLoader {
	private static final String SELECT1 = "SELECT DISTINCT BEGIV, STEDNAVN FROM F9PERSONFAMILIEQ WHERE TYPE = 'H'";
	private static final String SELECT2 = "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'H' AND BEGIV = ? AND STEDNAVN = ?";

	private static final String INSERT1 = "INSERT INTO INDIVID (KOEN, BLISTRUPID, FAM, SLGT) VALUES (?, ?, ?, ?)";
	private static final String INSERT2 = "INSERT INTO PERSONNAVN (INDIVIDID, STDNAVN, FONETISKNAVN, PRIMAERNAVN) VALUES (?, ?, ?, 'TRUE')";
	private static final String INSERT3 = "INSERT INTO INDIVIDBEGIVENHED (INDIVIDID, BEGTYPE, DATO, BLISTRUPID, KILDEID, STEDNAVN) "
			+ "VALUES (?, ?, ?, ?, ?, ?)";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			final int taeller = new MatrikelLoader().load();
			System.out.println("Har indlæst " + taeller + " matrikellinier");
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return
	 * @throws SQLException
	 */
	public int load() throws SQLException {
		final Connection conn = connect("BLISTRUP");
		ResultSet rs1, generatedKeys;
		int individId = 0;
		int taeller = 0;
		String stdnavn = "";
		String koen = "";
		String jordlod = "";
		String blistrupId = "";
		String stedNavn = "";
		String matr = "";

		final KildeModel kModel = new KildeModel();
		kModel.setKbNr("Matrikel");
		kModel.setAarInterval("1844");
		final int kildeId = kModel.insert(conn);

		// SELECT1 = "SELECT DISTINCT BEGIV FROM F9PERSONFAMILIEQ WHERE TYPE = 'H'

		final PreparedStatement statement0 = conn.prepareStatement(SELECT1);
		final PreparedStatement statement1 = conn.prepareStatement(SELECT2);
		final PreparedStatement statement2 = conn.prepareStatement(INSERT1, Statement.RETURN_GENERATED_KEYS);
		final PreparedStatement statement3 = conn.prepareStatement(INSERT2);
		final PreparedStatement statement4 = conn.prepareStatement(INSERT3);

		final ResultSet rs0 = statement0.executeQuery();

		while (rs0.next()) {

			// "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'D' AND BEGIV = ? AND STEDNAVN =
			// ? ORDER BY PID";

			blistrupId = afQ(rs0.getString("BEGIV"));
			statement1.setString(1, blistrupId);
			stedNavn = rs0.getString("STEDNAVN");
			statement1.setString(2, stedNavn);
			rs1 = statement1.executeQuery();

			if (rs1.next()) {
				stedNavn = afQ(stedNavn);
				koen = rs1.getString("SEX");

				// "INSERT INTO INDIVID (KOEN, BLISTRUPID) VALUES (?, ?)";

				statement2.setString(1, "m".equals(koen) ? "M" : "F");
				statement2.setString(2, blistrupId);
				statement2.setString(3, rs1.getString("FAM"));
				statement2.setString(4, rs1.getString("SLGT"));
				statement2.executeUpdate();
				generatedKeys = statement2.getGeneratedKeys();

				if (generatedKeys.next()) {
					individId = generatedKeys.getInt(1);
				} else {
					individId = 0;
				}

				generatedKeys.close();

				// INSERT2 = "INSERT INTO PERSONNAVN (INDIVIDID, STDNAVN, FONETISKNAVN,
				// PRIMAERNAVN) VALUES

				statement3.setInt(1, individId);

				stdnavn = afQ(rs1.getString("STD_NAVN"));
				statement3.setString(2, cleanName(stdnavn));
				try {
					statement3.setString(3, fonkod.generateKey(stdnavn).trim());
				} catch (final Exception e) {
					statement3.setString(3, "");
				}

				statement3.executeUpdate();

				// INSERT3 = "INSERT INTO INDIVIDBEGIVENHED (INDIVIDID, BEGTYPE, DATO,
				// BLISTRUPID, KILDEID, STEDNAVN) "

				statement4.setInt(1, individId);

				statement4.setString(2, "Matrikel");
				statement4.setString(3, "1844-01-01");
				statement4.setString(4, blistrupId);
				statement4.setInt(5, kildeId);
				if (stedNavn.contains("Blistrup")) {
					stedNavn = stedNavn + ", Holbo, Frederiksborg, ";
				} else {
					stedNavn = stedNavn + ", Blistrup, Holbo, Frederiksborg, ";
				}

				matr = afQ(rs1.getString("MATR_"));

				if (matr.isBlank()) {
					jordlod = afQ(rs1.getString("GAARD")) + ", " + stedNavn;
				} else {
					jordlod = "Matr. " + matr + ", " + afQ(rs1.getString("GAARD")) + ", " + stedNavn;
				}
				statement4.setString(6, jordlod);
				statement4.executeUpdate();

				taeller++;

			} else {
				System.err.println("Ikke fundet: SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'H' AND BEGIV = "
						+ blistrupId + " AND STEDNAVN = " + stedNavn);
			}

		}

		statement0.close();
		statement1.close();
		statement2.close();
		statement3.close();
		statement4.close();
		conn.commit();
		conn.close();
		return taeller;
	}
}
