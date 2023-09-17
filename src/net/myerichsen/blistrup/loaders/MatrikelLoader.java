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
 * @version 17. sep. 2023
 *
 */
public class MatrikelLoader extends AbstractLoader {
	private static final String SELECT1 = "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'H' ORDER BY BEGIV";

	private static final String INSERT1 = "INSERT INTO INDIVID (KOEN, FAM, SLGT) VALUES (?, ?, ?)";
	private static final String INSERT2 = "INSERT INTO PERSONNAVN (INDIVIDID, STDNAVN, FONETISKNAVN, PRIMAERNAVN) VALUES (?, ?, ?, 'TRUE')";
	private static final String INSERT3 = "INSERT INTO INDIVIDBEGIVENHED (INDIVIDID, BEGTYPE, DATO, KILDEID, STEDNAVN) "
			+ "VALUES (?, ?, ?, ?, ?)";

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
		ResultSet generatedKeys;
		int individId = 0;
		int taeller = 0;
		String stdnavn = "";
		String koen = "";
		String jordlod = "";
		String stedNavn = "";
		String matr = "";

		final KildeModel kModel = new KildeModel();
		kModel.setKbNr("Matrikel");
		kModel.setAarInterval("1844");
		final int kildeId = kModel.insert(conn);

		// SELECT1 = "SELECT DISTINCT BEGIV FROM F9PERSONFAMILIEQ WHERE TYPE = 'H'

		final PreparedStatement statement0 = conn.prepareStatement(SELECT1);
		final PreparedStatement statement2 = conn.prepareStatement(INSERT1, Statement.RETURN_GENERATED_KEYS);
		final PreparedStatement statement3 = conn.prepareStatement(INSERT2);
		final PreparedStatement statement4 = conn.prepareStatement(INSERT3);

		final ResultSet rs0 = statement0.executeQuery();

		while (rs0.next()) {
			koen = rs0.getString("SEX");

			// INSERT1 = "INSERT INTO INDIVID (KOEN, FAM, SLGT)

			statement2.setString(1, "m".equals(koen) ? "M" : "F");
			statement2.setString(2, rs0.getString("FAM"));
			statement2.setString(3, rs0.getString("SLGT"));
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

			stdnavn = afQ(rs0.getString("STD_NAVN"));
			statement3.setString(2, cleanName(stdnavn));
			try {
				statement3.setString(3, fonkod.generateKey(stdnavn).trim());
			} catch (final Exception e) {
				statement3.setString(3, "");
			}

			statement3.executeUpdate();

			// INSERT3 = "INSERT INTO INDIVIDBEGIVENHED (INDIVIDID, BEGTYPE, DATO,
			// KILDEID, STEDNAVN) "

			statement4.setInt(1, individId);

			statement4.setString(2, "Matrikel");
			statement4.setString(3, "1844-01-01");
			statement4.setInt(4, kildeId);
			stedNavn = fixStedNavn(afQ(rs0.getString("STEDNAVN")));

			matr = afQ(rs0.getString("MATR_"));

			if (matr.isBlank()) {
				jordlod = afQ(rs0.getString("GAARD")) + ", " + stedNavn;
			} else {
				jordlod = "Matr. " + matr + ", " + afQ(rs0.getString("GAARD")) + ", " + stedNavn;
			}
			statement4.setString(5, jordlod);
			statement4.executeUpdate();

			taeller++;

		}

		conn.commit();
		conn.close();
		return taeller;
	}
}
