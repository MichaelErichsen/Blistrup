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
 * L�s vielsesdata fra grundtabellen ind i GEDCOM-tabeller
 *
 * @author Michael Erichsen
 * @version 24. jul. 2023
 *
 */
public class VielseLoader {
	private static final String SET_SCHEMA = "SET SCHEMA = 'BLISTRUP'";
	private static final String DELETE1 = "DELETE FROM INDIVID";
	private static final String DELETE2 = "DELETE FROM PERSONNAVN";
	private static final String DELETE3 = "DELETE FROM INDIVIDBEGIVENHED";
	private static final String DELETE4 = "DELETE FROM VIDNE";
	private static final String DELETE5 = "DELETE FROM KILDE";
	private static final String DELETE6 = "DELETE FROM FAMILIE";

	private static final String SELECT1 = "SELECT DISTINCT BEGIV FROM F9PERSONFAMILIEQ WHERE TYPE = 'C' FETCH FIRST 100 ROWS ONLY";
	private static final String SELECT2 = "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'C' AND BEGIV = ? ORDER BY PID";

	private static final String INSERT1 = "INSERT INTO INDIVID (KOEN, BLISTRUPID) VALUES (?, ?)";
	private static final String INSERT2 = "INSERT INTO PERSONNAVN (INDIVIDID, FORNAVN, EFTERNAVN, PRIMAERNAVN, FONETISKNAVN, STDNAVN) VALUES (?, ?, ?, ?, ?, ?)";
	private static final String INSERT3 = "INSERT INTO KILDE (KBNR, AARINTERVAL, KBDEL, TIFNR, OPSLAG, OPNR) VALUES(?, ?, ?, ?, ?, ?)";
	private static final String INSERT4 = "INSERT INTO FAMILIE (HUSFADER) VALUES(?)";
	private static final String INSERT5 = "INSERT INTO VIDNE (INDIVIDID, ROLLE, INDIVIDBEGIVENHEDID) VALUES (?, ?, ?)";
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
			System.out.println("Har indl�st " + taeller + " vielseslinier");
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param input
	 * @return
	 */
	private String afQ(String input) {
		return input.replace("Qo", "�").replace("Qe", "�").replace("Qa", "a").trim();
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
		conn.prepareStatement(DELETE1).executeUpdate();
		conn.prepareStatement(DELETE2).executeUpdate();
		conn.prepareStatement(DELETE3).executeUpdate();
		conn.prepareStatement(DELETE4).executeUpdate();
		conn.prepareStatement(DELETE5).executeUpdate();
		conn.prepareStatement(DELETE6).executeUpdate();
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
		String fornvn = "";
		String efternvn = "";
		String dato = "";
		String mm = "";
		String dd = "";
		int kildeId = 0;
		final int individBegivenhedsId = 0;
		int familieId = 0;
		int taeller = 0;
		StringBuilder sb;
		String navn = "";
		String fader = "";
		int gom = 0;
		int brud = 0;
		int faderId = 0;
		int faderFamilieId = 0;

		final Connection conn = connect();

//		private static final String SELECT1 = "SELECT DISTINCT BEGIV FROM F9PERSONFAMILIEQ WHERE TYPE = 'C' FETCH FIRST 50 ROWS ONLY";

		PreparedStatement statement1 = conn.prepareStatement(SELECT1);
		ResultSet rs1 = statement1.executeQuery();

		while (rs1.next()) {
			blistrupIdListe.add(rs1.getString("BEGIV"));
		}

//		private static final String SELECT2 = "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'C' AND BEGIV = ? ORDER BY PID";

		for (final String blistrupId : blistrupIdListe) {
			sb = new StringBuilder();

			statement1 = conn.prepareStatement(SELECT2);
			statement1.setString(1, blistrupId);
			rs1 = statement1.executeQuery();

//			private static final String INSERT1 = "INSERT INTO INDIVID (KOEN, BLISTRUPID) VALUES (?, ?)";

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

//				private static final String INSERT2 = "INSERT INTO PERSONNAVN (INDIVIDID, FORNAVN, EFTERNAVN, PRIMAERNAVN, FONETISKNAVN) VALUES (?, ?, ?, ?, ?)";

				statement2 = conn.prepareStatement(INSERT2);
				statement2.setInt(1, individId);
				fornvn = afQ(rs1.getString("FORNVN"));
				statement2.setString(2, fornvn);
				efternvn = afQ(rs1.getString("EFTERNVN"));
				statement2.setString(3, efternvn);
				statement2.setString(4, "TRUE");
				try {
					statement2.setString(5, fonkod.generateKey(fornvn + " " + efternvn).trim());
				} catch (final Exception e) {
					statement2.setString(5, "");
				}
				statement2.setString(6, afQ(rs1.getString("STD_NAVN")));
				statement2.executeUpdate();
				statement2.close();

				taeller++;

				if ("gom".equals(rolle)) {
					gom = individId;

//					private static final String INSERT3 = "INSERT INTO KILDE (KBNR, AARINTERVAL, KBDEL, TIFNR, OPSLAG, OPNR) VALUES(?, ?, ?, ?, ?, ?)";

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

//					private static final String INSERT4 = "INSERT INTO FAMILIE (HUSFADER) VALUES(?)";

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
//						private static final String INSERT1 = "INSERT INTO INDIVID (KOEN, BLISTRUPID) VALUES (?, ?)";

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

//						private static final String INSERT4 = "INSERT INTO FAMILIE (HUSFADER) VALUES(?)";

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

//					private static final String UPDATE1 = "UPDATE INDIVID SET FAMC = ? WHERE ID = ?";

					statement2 = conn.prepareStatement(UPDATE1);
					statement2.setInt(1, faderFamilieId);
					statement2.setInt(2, faderId);

				} else if ("brud".equals(rolle)) {
					brud = individId;

//					String UPDATE2 = "UPDATE FAMILIE SET HUSMODER = ? WHERE ID = ?";

					statement2.close();
					statement2 = conn.prepareStatement(UPDATE2);
					statement2.setInt(1, brud);
					statement2.setInt(2, familieId);
					statement2.executeUpdate();

					if (fader != null && !fader.isBlank()) {
//						private static final String INSERT1 = "INSERT INTO INDIVID (KOEN, BLISTRUPID) VALUES (?, ?)";

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

//						private static final String INSERT4 = "INSERT INTO FAMILIE (HUSFADER) VALUES(?)";

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

//					private static final String UPDATE1 = "UPDATE INDIVID SET FAMC = ? WHERE ID = ?";

					statement2 = conn.prepareStatement(UPDATE1);
					statement2.setInt(1, faderFamilieId);
					statement2.setInt(2, faderId);

				} else {
					// Forlover
//					String INSERT5 = "INSERT INTO VIDNE (INDIVIDID, ROLLE, INDIVIDBEGIVENHEDID) VALUES (?, ?, ?)";
					statement2.close();
					statement2 = conn.prepareStatement(INSERT5, Statement.RETURN_GENERATED_KEYS);
					statement2.setInt(1, individId);
					statement2.setString(2, rs1.getString("ROLLE").trim());
					statement2.setInt(3, individBegivenhedsId);
				}
				statement2.executeUpdate();

//				private static final String INSERT6 = "INSERT INTO FAMILIEBEGIVENHED (FAMILIEID, BEGTYPE, DATO, BLISTRUPID, KILDEID, STEDNAVN, BEM) "
//						+ "VALUES (?, ?, ?, ?, ?, ?, ?)";

				statement2 = conn.prepareStatement(INSERT6);
				statement2.setInt(1, familieId);
				statement2.setString(2, "Vielse");

				try {
					dato = rs1.getString("FQODTDATO").trim();
					mm = dato.substring(4, 6);
					dd = dato.substring(6, 8);
					statement2.setString(3, dato.substring(0, 4) + "-" + ("00".equals(mm) ? "01" : mm) + "-"
							+ ("00".equals(dd) ? "01" : dd));
				} catch (final Exception e) {
					statement2.setString(3, "0001-01-01");
				}

				statement2.setString(4, afQ(rs1.getString("BEGIV")));
				statement2.setInt(5, kildeId);
				statement2.setString(6, afQ(rs1.getString("STEDNAVN")));
				statement2.setString(7, afQ(rs1.getString("BEM")));
				statement2.executeUpdate();

			}

		}

		conn.close();
		return taeller;
	}
}
