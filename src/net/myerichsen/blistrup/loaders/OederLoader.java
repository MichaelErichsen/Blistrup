package net.myerichsen.blistrup.loaders;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import net.myerichsen.blistrup.util.Fonkod;

/**
 * Læs Oeder date fra grundtabellen ind i GEDCOM-tabeller
 *
 * @author Michael Erichsen
 * @version 26. jul. 2023
 *
 */
public class OederLoader {
	private static final String SET_SCHEMA = "SET SCHEMA = 'BLISTRUP'";

	private static final String SELECT1 = "SELECT DISTINCT BEGIV FROM F9PERSONFAMILIEQ WHERE TYPE = 'E' FETCH FIRST 50 ROWS ONLY";
	private static final String SELECT2 = "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'E' AND BEGIV = ? ORDER BY PID";

	private static final String INSERT1 = "INSERT INTO INDIVID (KOEN, BLISTRUPID, FOEDT, FAM, SLGT) VALUES (?, ?, ?, ?, ?)";
	private static final String INSERT2 = "INSERT INTO PERSONNAVN (INDIVIDID, PRIMAERNAVN, FONETISKNAVN, STDNAVN) VALUES (?, ?, ?, ?)";
	private static final String INSERT3 = "INSERT INTO KILDE (KBNR, AARINTERVAL, KBDEL, TIFNR, OPSLAG, OPNR) VALUES(?, ?, ?, ?, ?, ?)";
	private static final String INSERT4 = "INSERT INTO INDIVIDBEGIVENHED (INDIVIDID, ALDER, BEGTYPE, DATO, BLISTRUPID, KILDEID, STEDNAVN, BEM, FOEDT, ROLLE) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	private static final Fonkod fonkod = new Fonkod();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			final int taeller = new OederLoader().load();
			System.out.println("Har indlæst " + taeller + " Oederlinier");
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param input
	 * @return
	 */
	private String afQ(String input) {
		return input.replace("Qo", "ø").replace("Qe", "æ").replace("Qa", "a").trim();
	}

	/**
	 * Forbind til databasen
	 *
	 * @return conn forbindelse
	 * @throws SQLException
	 */
	private Connection connect() throws SQLException {
		final Connection conn = DriverManager.getConnection("jdbc:derby:C:\\Users\\michael\\BlistrupDB");
		final PreparedStatement statement = conn.prepareStatement(SET_SCHEMA);
		statement.execute();
		return conn;
	}

	/**
	 * @return
	 * @throws SQLException
	 */
	public int load() throws SQLException {
		final Connection conn = connect();
		final List<String> blistrupIdListe = new ArrayList<>();
		String rolle = "";
		PreparedStatement statement2;
		ResultSet generatedKeys;
		int individId = 0;
		int kildeId = 0;
		int taeller = 0;
		StringBuilder sb;
		String navn = "";
		String stdnavn = "";

		// SELECT1 = "SELECT DISTINCT BEGIV FROM F9PERSONFAMILIEQ WHERE TYPE = 'E'
		PreparedStatement statement1 = conn.prepareStatement(SELECT1);
		ResultSet rs1 = statement1.executeQuery();

		while (rs1.next()) {
			blistrupIdListe.add(rs1.getString("BEGIV"));
		}

		for (final String blistrupId : blistrupIdListe) {
			sb = new StringBuilder();

			// SELECT2 = "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'E' AND BEGIV = ?
			// ORDER BY PID";

			statement1 = conn.prepareStatement(SELECT2);
			statement1.setString(1, blistrupId);
			rs1 = statement1.executeQuery();

			while (rs1.next()) {
				navn = rs1.getString("NAVN").trim();
				sb.append(rolle + ": " + navn + "\r\n");

				// INSERT1 = "INSERT INTO INDIVID (KOEN, BLISTRUPID, FOEDT, FAM, SLGT)

				statement2 = conn.prepareStatement(INSERT1, Statement.RETURN_GENERATED_KEYS);
				statement2.setString(1, rs1.getString("SEX").trim());
				statement2.setString(2, rs1.getString("PID").trim());
				statement2.setString(3, rs1.getString("FQODT").trim());
				statement2.setString(4, rs1.getString("FAM").trim());
				statement2.setString(5, rs1.getString("SLGT").trim());
				statement2.executeUpdate();
				generatedKeys = statement2.getGeneratedKeys();

				if (generatedKeys.next()) {
					individId = generatedKeys.getInt(1);
				} else {
					individId = 0;
				}
				generatedKeys.close();

				// INSERT2 = "INSERT INTO PERSONNAVN (INDIVIDID, PRIMAERNAVN, FONETISKNAVN,
				// STDNAVN) VALUES (?, ?, ?, ?, ?, ?)";

				statement2 = conn.prepareStatement(INSERT2);
				statement2.setInt(1, individId);
				statement2.setString(2, "TRUE");
				stdnavn = afQ(rs1.getString("STD_NAVN"));

				try {
					statement2.setString(3, fonkod.generateKey(stdnavn).trim());
				} catch (final Exception e) {
					statement2.setString(3, "");
				}

				statement2.setString(4, stdnavn);
				statement2.executeUpdate();

				taeller++;

				// INSERT3 = "INSERT INTO KILDE (KBNR, AARINTERVAL, KBDEL, TIFNR, OPSLAG, OPNR)

				statement2 = conn.prepareStatement(INSERT3, Statement.RETURN_GENERATED_KEYS);
				statement2.setString(1, rs1.getString("KBNR").trim());
				statement2.setString(2, rs1.getString("KILDE").trim());
				statement2.setString(3, rs1.getString("KBDEL").trim());
				statement2.setString(4, rs1.getString("TIFNR").trim());
				statement2.setString(5, rs1.getString("OPSLAG").trim());
				statement2.setString(6, rs1.getString("OPNR").trim());
				statement2.executeUpdate();
				generatedKeys = statement2.getGeneratedKeys();

				if (generatedKeys.next()) {
					kildeId = generatedKeys.getInt(1);
				} else {
					kildeId = 0;
				}
				generatedKeys.close();

				// INSERT4 = "INSERT INTO INDIVIDBEGIVENHED (INDIVIDID, ALDER, BEGTYPE, DATO,
				// BLISTRUPID, KILDEID, STEDNAVN, BEM, FOEDT, ROLLE) "

				statement2 = conn.prepareStatement(INSERT4);
				statement2.setInt(1, individId);
				statement2.setString(2, rs1.getString("ALDER"));
				statement2.setString(3, "Oeder");
				statement2.setString(4, "1771-01-01");
				statement2.setString(5, afQ(rs1.getString("BEGIV")));
				statement2.setInt(6, kildeId);
				statement2.setString(7, afQ(rs1.getString("STEDNAVN")));
				statement2.setString(8, afQ(rs1.getString("BEM")));
				statement2.setString(9, rs1.getString("FQODT").trim());
				statement2.setString(10, "Husfader");
				statement2.executeUpdate();
			}

		}

		conn.close();
		return taeller;
	}
}
