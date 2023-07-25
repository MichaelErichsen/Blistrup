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
 * Læs vielsesdata fra grundtabellen ind i GEDCOM-tabeller
 *
 * @author Michael Erichsen
 * @version 25. jul. 2023
 *
 */
public class VielseLoader {
	private static final String SET_SCHEMA = "SET SCHEMA = 'BLISTRUP'";

	private static final String SELECT1 = "SELECT DISTINCT BEGIV FROM F9PERSONFAMILIEQ WHERE TYPE = 'C' FETCH FIRST 100 ROWS ONLY";
	private static final String SELECT2 = "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'C' AND BEGIV = ? ORDER BY PID";

	private static final String INSERT1 = "INSERT INTO INDIVID (KOEN, BLISTRUPID) VALUES (?, ?)";
	private static final String INSERT2 = "INSERT INTO PERSONNAVN (INDIVIDID, FORNAVN, EFTERNAVN, PRIMAERNAVN, FONETISKNAVN, STDNAVN) VALUES (?, ?, ?, ?, ?, ?)";
	private static final String INSERT3 = "INSERT INTO KILDE (KBNR, AARINTERVAL, KBDEL, TIFNR, OPSLAG, OPNR) VALUES(?, ?, ?, ?, ?, ?)";
	private static final String INSERT4 = "INSERT INTO FAMILIE (HUSFADER) VALUES(?)";
	private static final String INSERT5 = "INSERT INTO VIDNE (INDIVIDID, ROLLE, FAMILIEBEGIVENHEDID) VALUES (?, ?, ?)";
	private static final String INSERT6 = "INSERT INTO FAMILIEBEGIVENHED (FAMILIEID, BEGTYPE, DATO, BLISTRUPID, KILDEID, STEDNAVN, BEM) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?)";

	private static final String UPDATE1 = "UPDATE INDIVID SET FAMC = ? WHERE ID = ?";
	private static final String UPDATE2 = "UPDATE FAMILIE SET HUSMODER = ? WHERE ID = ?";

	private static final Fonkod fonkod = new Fonkod();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			final int taeller = new VielseLoader().load();
			System.out.println("Har indlæst " + taeller + " vielseslinier");
		} catch (final Exception e) {
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
	 * @throws Exception
	 */
	public int load() throws Exception {

		final List<String> blistrupIdListe = new ArrayList<>();
		String rolle = "";
		PreparedStatement statement2 = null;
		ResultSet generatedKeys = null;
		int individId = 0;
		String aar = "";
		int kildeId = 0;
		int familieBegivenhedsId = 0;
		int familieId = 0;
		int taeller = 0;
		StringBuilder sb;
		String navn = "";
		String fader = "";
		int gom = 0;
		int brud = 0;
		int faderId = 0;
		int faderFamilieId = 0;
		String stdnavn = "";

		final Connection conn = connect();

// SELECT1 = "SELECT DISTINCT BEGIV FROM F9PERSONFAMILIEQ WHERE TYPE = 'C' FETCH FIRST 50 ROWS ONLY";

		PreparedStatement statement1 = conn.prepareStatement(SELECT1);
		ResultSet rs1 = statement1.executeQuery();

		while (rs1.next()) {
			blistrupIdListe.add(rs1.getString("BEGIV"));
		}

// SELECT2 = "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'C' AND BEGIV = ? ORDER BY PID";

		for (final String blistrupId : blistrupIdListe) {
			sb = new StringBuilder();

			statement1 = conn.prepareStatement(SELECT2);
			statement1.setString(1, blistrupId);
			rs1 = statement1.executeQuery();

// INSERT1 = "INSERT INTO INDIVID (KOEN, BLISTRUPID) VALUES (?, ?)";

			while (rs1.next()) {
				rolle = rs1.getString("ROLLE").trim();
				navn = rs1.getString("NAVN").trim();
				fader = rs1.getString("FADER");
				sb.append(rolle + ": " + navn + "\r\n");

				statement2 = conn.prepareStatement(INSERT1, Statement.RETURN_GENERATED_KEYS);
				statement2.setString(1, rs1.getString("SEX").trim());
				statement2.setString(2, rs1.getString("PID").trim());
				statement2.executeUpdate();
				generatedKeys = statement2.getGeneratedKeys();

				if (generatedKeys.next()) {
					individId = generatedKeys.getInt(1);
				} else {
					individId = 0;
				}
				generatedKeys.close();

// INSERT2 = "INSERT INTO PERSONNAVN (INDIVIDID, FORNAVN, EFTERNAVN, PRIMAERNAVN, FONETISKNAVN) VALUES (?, ?, ?, ?, ?)";

				statement2 = conn.prepareStatement(INSERT2);
				statement2.setInt(1, individId);
				statement2.setString(2, afQ(rs1.getString("FORNVN")));
				statement2.setString(3, afQ(rs1.getString("EFTERNVN")));
				statement2.setString(4, "TRUE");
				stdnavn = afQ(rs1.getString("STD_NAVN"));

				try {
					statement2.setString(5, fonkod.generateKey(stdnavn).trim());
				} catch (final Exception e) {
					statement2.setString(5, "");
				}

				statement2.setString(6, stdnavn);
				statement2.executeUpdate();
				statement2.close();

				taeller++;

				if ("gom".equals(rolle)) {
					gom = individId;

// INSERT3 = "INSERT INTO KILDE (KBNR, AARINTERVAL, KBDEL, TIFNR, OPSLAG, OPNR) VALUES(?, ?, ?, ?, ?, ?)";

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

// INSERT4 = "INSERT INTO FAMILIE (HUSFADER) VALUES(?)";

					statement2 = conn.prepareStatement(INSERT4, Statement.RETURN_GENERATED_KEYS);
					statement2.setInt(1, gom);
					statement2.executeUpdate();
					generatedKeys = statement2.getGeneratedKeys();

					if (generatedKeys.next()) {
						familieId = generatedKeys.getInt(1);
					} else {
						familieId = 0;
					}
					generatedKeys.close();

					if (fader != null && !fader.isBlank()) {
// INSERT1 = "INSERT INTO INDIVID (KOEN, BLISTRUPID) VALUES (?, ?)";

						statement2 = conn.prepareStatement(INSERT1, Statement.RETURN_GENERATED_KEYS);
						statement2.setString(1, rs1.getString("SEX").trim());
						statement2.setString(2, rs1.getString("PID").trim());
						statement2.executeUpdate();
						generatedKeys = statement2.getGeneratedKeys();

						if (generatedKeys.next()) {
							faderId = generatedKeys.getInt(1);
						} else {
							faderId = 0;
						}
						generatedKeys.close();

// INSERT4 = "INSERT INTO FAMILIE (HUSFADER) VALUES(?)";

						statement2 = conn.prepareStatement(INSERT4, Statement.RETURN_GENERATED_KEYS);
						statement2.setInt(1, faderId);
						statement2.executeUpdate();
						generatedKeys = statement2.getGeneratedKeys();

						if (generatedKeys.next()) {
							faderFamilieId = generatedKeys.getInt(1);
						} else {
							faderFamilieId = 0;
						}
						generatedKeys.close();
					}

// UPDATE1 = "UPDATE INDIVID SET FAMC = ? WHERE ID = ?";

					statement2 = conn.prepareStatement(UPDATE1);
					statement2.setInt(1, faderFamilieId);
					statement2.setInt(2, faderId);

// INSERT6 = "INSERT INTO FAMILIEBEGIVENHED (FAMILIEID, BEGTYPE, DATO, BLISTRUPID, KILDEID, STEDNAVN, BEM) "

					statement2 = conn.prepareStatement(INSERT6, Statement.RETURN_GENERATED_KEYS);
					statement2.setInt(1, familieId);
					statement2.setString(2, "Vielse");

					try {
						aar = rs1.getString("AAR").trim();
						statement2.setString(3, aar + "-01-01");
					} catch (final Exception e) {
						statement2.setString(3, "0001-01-01");
					}

					statement2.setString(4, afQ(rs1.getString("BEGIV")));
					statement2.setInt(5, kildeId);
					statement2.setString(6, afQ(rs1.getString("STEDNAVN")));
					statement2.setString(7, afQ(rs1.getString("BEM")));
					statement2.executeUpdate();
					generatedKeys = statement2.getGeneratedKeys();

					if (generatedKeys.next()) {
						familieBegivenhedsId = generatedKeys.getInt(1);
					} else {
						familieBegivenhedsId = 0;
					}
					generatedKeys.close();
				} else if ("brud".equals(rolle)) {
					brud = individId;

// UPDATE2 = "UPDATE FAMILIE SET HUSMODER = ? WHERE ID = ?";

					statement2.close();
					statement2 = conn.prepareStatement(UPDATE2);
					statement2.setInt(1, brud);
					statement2.setInt(2, familieId);
					statement2.executeUpdate();

					if (fader != null && !fader.isBlank()) {
// INSERT1 = "INSERT INTO INDIVID (KOEN, BLISTRUPID) VALUES (?, ?)";

						statement2 = conn.prepareStatement(INSERT1, Statement.RETURN_GENERATED_KEYS);
						statement2.setString(1, rs1.getString("SEX").trim());
						statement2.setString(2, rs1.getString("PID").trim());
						statement2.executeUpdate();
						generatedKeys.close();
						generatedKeys = statement2.getGeneratedKeys();

						if (generatedKeys.next()) {
							faderId = generatedKeys.getInt(1);
						} else {
							faderId = 0;
						}
						generatedKeys.close();

// INSERT4 = "INSERT INTO FAMILIE (HUSFADER) VALUES(?)";

						statement2 = conn.prepareStatement(INSERT4, Statement.RETURN_GENERATED_KEYS);
						statement2.setInt(1, faderId);
						statement2.executeUpdate();
						generatedKeys = statement2.getGeneratedKeys();

						if (generatedKeys.next()) {
							faderFamilieId = generatedKeys.getInt(1);
						} else {
							faderFamilieId = 0;
						}
						generatedKeys.close();
					}

// UPDATE1 = "UPDATE INDIVID SET FAMC = ? WHERE ID = ?";

					statement2 = conn.prepareStatement(UPDATE1);
					statement2.setInt(1, faderFamilieId);
					statement2.setInt(2, faderId);

				} else {
					// Forlover
// INSERT5 = "INSERT INTO VIDNE (INDIVIDID, ROLLE, FAMILIEBEGIVENHEDID) VALUES (?, ?, ?)";
					statement2.close();
					statement2 = conn.prepareStatement(INSERT5, Statement.RETURN_GENERATED_KEYS);
					statement2.setInt(1, individId);
					statement2.setString(2, rs1.getString("ROLLE").trim());
					statement2.setInt(3, familieBegivenhedsId);
				}
				statement2.executeUpdate();

			}

		}

		conn.close();
		return taeller;
	}
}
