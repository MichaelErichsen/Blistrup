package net.myerichsen.blistrup.loaders;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import net.myerichsen.blistrup.util.Fonkod;

/**
 * Læs folketællingsdata fra grundtabellen ind i GEDCOM tabeller
 * 
 * @author Michael Erichsen
 * @version 28. jul. 2023
 *
 */
public class FolketaellingLoader {
	private static final String SET_SCHEMA = "SET SCHEMA = 'BLISTRUP'";

	private static final String SELECT1 = "SELECT DISTINCT BEGIV FROM F9PERSONFAMILIEQ WHERE TYPE = 'F'"
			+ " FETCH FIRST 200 ROWS ONLY";
	private static final String SELECT2 = "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'F' AND BEGIV = ? ORDER BY PID";

	private static final String INSERT1 = "INSERT INTO INDIVID (KOEN, BLISTRUPID, FOEDT) VALUES (?, ?, ?)";
	private static final String INSERT2 = "INSERT INTO PERSONNAVN (INDIVIDID, FORNAVN, EFTERNAVN, PRIMAERNAVN, FONETISKNAVN, STDNAVN) VALUES (?, ?, ?, ?, ?, ?)";
	private static final String INSERT3 = "INSERT INTO KILDE (KBNR, AARINTERVAL, KBDEL, TIFNR, OPSLAG, OPNR) VALUES(?, ?, ?, ?, ?, ?)";
	private static final String INSERT4 = "INSERT INTO FAMILIE (HUSFADER, HUSMODER) VALUES(?, ?)";
	private static final String INSERT5 = "INSERT INTO FAMILIEBEGIVENHED (FAMILIEID, HUSFADERALDER, HUSMODERALDER, KILDEID, BEGTYPE, DATO, NOTE, BLISTRUPID, STEDNAVN, BEM) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	private static final Fonkod fonkod = new Fonkod();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			final int taeller = new FolketaellingLoader().load();
			System.out.println("Har indlæst " + taeller + " folketællingslinier");
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
	private int load() throws SQLException {
		final List<String> blistrupIdListe = new ArrayList<>();
		String rolle = "";
		PreparedStatement statement2;
		ResultSet generatedKeys;
		int individId = 0;
		String fornvn = "";
		String efternvn = "";
		int iAar = 0;
		String mm = "";
		String dd = "";
		int kildeId = 0;
		int familieBegivenhedsId = 0;
		int husfaderId;
		int familieId;
		int headId = 0;
		int taeller = 0;
		StringBuilder sb;
		String navn = "";
		String fader = "";
		String moder = "";
		String stdnavn = "";
		boolean husfader = false;
		String aar = "";
		String koen = "";
		String alder = "";

		final Connection conn = connect();

		// SELECT1 = "SELECT DISTINCT BEGIV FROM F9PERSONFAMILIEQ WHERE TYPE = 'F'
		PreparedStatement statement1 = conn.prepareStatement(SELECT1);
		ResultSet rs1 = statement1.executeQuery();

		while (rs1.next()) {
			blistrupIdListe.add(rs1.getString("BEGIV"));
		}

		for (final String blistrupId : blistrupIdListe) {
			sb = new StringBuilder();
			husfader = true;

			// SELECT2 = "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'F' AND BEGIV = ?
			// ORDER BY PID";

			statement1 = conn.prepareStatement(SELECT2);
			statement1.setString(1, blistrupId);
			rs1 = statement1.executeQuery();

			while (rs1.next()) {
				// First person in household always considered head of household
				rolle = rs1.getString("ROLLE").trim();
				navn = rs1.getString("NAVN").trim();
				koen = rs1.getString("SEX").trim();
				sb.append(rolle + ": " + navn + ", \r\n");

				// INSERT1 = "INSERT INTO INDIVID (KOEN, BLISTRUPID, FOEDT) VALUES (?, ?, ?)";

				statement2 = conn.prepareStatement(INSERT1, Statement.RETURN_GENERATED_KEYS);
				statement2.setString(1, koen);
				statement2.setString(2, rs1.getString("PID").trim());
				statement2.setString(3, rs1.getString("FQODT").trim());
				statement2.executeUpdate();
				generatedKeys = statement2.getGeneratedKeys();

				if (generatedKeys.next()) {
					individId = generatedKeys.getInt(1);
				} else {
					individId = 0;
				}
				generatedKeys.close();

				// INSERT2 = "INSERT INTO PERSONNAVN (INDIVIDID, FORNAVN, EFTERNAVN,
				// PRIMAERNAVN, FONETISKNAVN, STDNAVN) VALUES (?, ?, ?, ?, ?, ?)";

				statement2 = conn.prepareStatement(INSERT2);
				statement2.setInt(1, individId);
				fornvn = afQ(rs1.getString("FORNVN"));
				statement2.setString(2, fornvn);
				efternvn = afQ(rs1.getString("EFTERNVN"));
				statement2.setString(3, efternvn);
				statement2.setString(4, "TRUE");
				stdnavn = afQ(rs1.getString("STD_NAVN"));

				try {
					statement2.setString(5, fonkod.generateKey(stdnavn).trim());
				} catch (final Exception e) {
					statement2.setString(5, "");
				}

				statement2.setString(6, stdnavn);
				statement2.executeUpdate();

				taeller++;

				// Handle roles
				if (husfader) {
					headId = individId;

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

					// INSERT4 = "INSERT INTO FAMILIE (HUSFADER, HUSMODER) VALUES(?, ?)";

					statement2 = conn.prepareStatement(INSERT4, Statement.RETURN_GENERATED_KEYS);

					if (koen.equals("m")) {
						statement2.setInt(1, headId);
						statement2.setInt(2, 0);
					} else {
						statement2.setInt(1, 0);
						statement2.setInt(2, headId);
					}

					statement2.executeUpdate();
					generatedKeys = statement2.getGeneratedKeys();

					if (generatedKeys.next()) {
						familieId = generatedKeys.getInt(1);
					} else {
						familieId = 0;
					}
					generatedKeys.close();

					// INSERT5 = "INSERT INTO FAMILIEBEGIVENHED (FAMILIEID, HUSFADERALDER,
					// HUSMODERALDER, KILDEID, BEGTYPE, DATO, NOTE, BLISTRUPID, STEDNAVN, BEM) "

					statement2 = conn.prepareStatement(INSERT5, Statement.RETURN_GENERATED_KEYS);
					statement2.setInt(1, familieId);

					alder = rs1.getString("ALDER").trim();
					if (alder == null || alder.length() == 0) {
						statement2.setInt(2, 0);
						statement2.setInt(3, 0);
					} else if (koen.equals("m")) {
						statement2.setInt(2, Integer.parseInt(alder));
						statement2.setInt(3, 0);
					} else if (koen.equals("k")) {
						statement2.setInt(2, 0);
						statement2.setInt(3, Integer.parseInt(alder));
					}

					statement2.setInt(4, kildeId);
					statement2.setString(5, "Folketælling");

					iAar = Integer.parseInt(rs1.getString("AAR").trim());

					switch (iAar) {
					case 1771:
						aar = iAar + "-01-01";
						break;
					case 1787:
						aar = iAar + "-07-01";
						break;
					case 1834:
						aar = iAar + "-02-18";
						break;
					case 1925:
					case 1930:
					case 1940:
						aar = iAar + "-11-05";
					default:
						aar = iAar + "-02-01";
						break;
					}

					statement2.setDate(6, Date.valueOf(aar));
					statement2.setString(7, ""); // Note
					statement2.setString(8, afQ(rs1.getString("BEGIV").trim()));
					statement2.setString(9, afQ(rs1.getString("STEDNAVN")));
					statement2.setString(10, afQ(rs1.getString("BEM")));
					statement2.executeUpdate();
					generatedKeys = statement2.getGeneratedKeys();

					if (generatedKeys.next()) {
						familieBegivenhedsId = generatedKeys.getInt(1);
					} else {
						familieBegivenhedsId = 0;
					}
					generatedKeys.close();
				} else {
					// Other roles
					// TODO Use contains
				}

				husfader = false;
			}
		}
		return taeller;
	}

}
