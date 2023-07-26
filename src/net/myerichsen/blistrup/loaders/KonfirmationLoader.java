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
 * Læs konfirmationsdata fra grundtabellen ind i GEDCOM-tabeller
 *
 * @author Michael Erichsen
 * @version 25. jul. 2023
 *
 */
public class KonfirmationLoader {
	private static final String SET_SCHEMA = "SET SCHEMA = 'BLISTRUP'";

	private static final String SELECT1 = "SELECT DISTINCT BEGIV FROM F9PERSONFAMILIEQ WHERE TYPE = 'B' FETCH FIRST 50 ROWS ONLY";
	private static final String SELECT2 = "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'B' AND BEGIV = ? ORDER BY PID";

	private static final String INSERT1 = "INSERT INTO INDIVID (KOEN, BLISTRUPID, FOEDT) VALUES (?, ?, ?)";
	private static final String INSERT2 = "INSERT INTO PERSONNAVN (INDIVIDID, FORNAVN, EFTERNAVN, PRIMAERNAVN, FONETISKNAVN, STDNAVN) VALUES (?, ?, ?, ?, ?, ?)";
	private static final String INSERT3 = "INSERT INTO KILDE (KBNR, AARINTERVAL, KBDEL, TIFNR, OPSLAG, OPNR) VALUES(?, ?, ?, ?, ?, ?)";
	private static final String INSERT4 = "INSERT INTO INDIVIDBEGIVENHED (INDIVIDID, ALDER, BEGTYPE, DATO, NOTE, ROLLE, BLISTRUPID, KILDEID, STEDNAVN, BEM) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String INSERT5 = "INSERT INTO VIDNE (INDIVIDID, ROLLE, INDIVIDBEGIVENHEDID) VALUES (?, ?, ?)";
	private static final String INSERT6 = "INSERT INTO FAMILIE (HUSFADER) VALUES(?)";

	private static final String UPDATE1 = "UPDATE INDIVID SET FAMC = ? WHERE ID = ?";
	private static final String UPDATE2 = "UPDATE INDIVIDBEGIVENHED SET DETALJER = ? WHERE ID = ?";

	private static final Fonkod fonkod = new Fonkod();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			final int taeller = new KonfirmationLoader().load();
			System.out.println("Har indlæst " + taeller + " konfirmationslinier");
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
		String dato = "";
		String mm = "";
		String dd = "";
		int kildeId = 0;
		int individBegivenhedsId = 0;
		int husfaderId = 0;
		int familieId = 0;
		int barnId = 0;
		int taeller = 0;
		StringBuilder sb;
		String navn = "";
		String fader = "";
		String moder = "";
		String stdnavn = "";

		PreparedStatement statement1 = conn.prepareStatement(SELECT1);
		ResultSet rs1 = statement1.executeQuery();

		while (rs1.next()) {
			blistrupIdListe.add(rs1.getString("BEGIV"));
		}

		for (final String blistrupId : blistrupIdListe) {
			sb = new StringBuilder();

			statement1 = conn.prepareStatement(SELECT2);
			statement1.setString(1, blistrupId);
			rs1 = statement1.executeQuery();

			while (rs1.next()) {
				rolle = rs1.getString("ROLLE").trim();
				navn = rs1.getString("NAVN").trim();
				sb.append(rolle + ": " + navn + "\r\n");

				statement2 = conn.prepareStatement(INSERT1, Statement.RETURN_GENERATED_KEYS);
				statement2.setString(1, rs1.getString("SEX").trim());
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

				taeller++;

				if ("barn".equals(rolle)) {
					barnId = individId;

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

					statement2 = conn.prepareStatement(INSERT4, Statement.RETURN_GENERATED_KEYS);
					statement2.setInt(1, individId);
					statement2.setString(2, "0");
					statement2.setString(3, "Konfirmation");

					try {
						dato = rs1.getString("FQODTDATO").trim();
						mm = dato.substring(4, 6);
						dd = dato.substring(6, 8);
						statement2.setString(4, dato.substring(0, 4) + "-" + ("00".equals(mm) ? "01" : mm) + "-"
								+ ("00".equals(dd) ? "01" : dd));
					} catch (final Exception e) {
						statement2.setString(4, "0001-01-01");
					}

					fader = afQ(rs1.getString("FADER"));
					fader = fader.length() > 0 ? "Fader: " + fader : "";
					moder = afQ(rs1.getString("MODER"));
					moder = moder.length() > 0 ? "Moder: " + moder : "";
					statement2.setString(5, (fader + " " + moder).trim());
					statement2.setString(6, rolle);
					statement2.setString(7, afQ(rs1.getString("BEGIV")));
					statement2.setInt(8, kildeId);
					statement2.setString(9, afQ(rs1.getString("STEDNAVN")));
					statement2.setString(10, afQ(rs1.getString("BEM")));
					statement2.executeUpdate();
					generatedKeys = statement2.getGeneratedKeys();

					if (generatedKeys.next()) {
						individBegivenhedsId = generatedKeys.getInt(1);
					} else {
						individBegivenhedsId = 0;
					}
					generatedKeys.close();
				} else {
					if ("far".equals(rolle)) {
						statement2.close();
						statement2 = conn.prepareStatement(INSERT5, Statement.RETURN_GENERATED_KEYS);
						statement2.setInt(1, individId);
						statement2.setString(2, rs1.getString("ROLLE").trim());
						statement2.setInt(3, individBegivenhedsId);
						statement2.executeUpdate();
						generatedKeys.close();
						generatedKeys = statement2.getGeneratedKeys();

						if (generatedKeys.next()) {
							husfaderId = generatedKeys.getInt(1);
						} else {
							husfaderId = 0;
						}
						generatedKeys.close();

						statement2 = conn.prepareStatement(INSERT6, Statement.RETURN_GENERATED_KEYS);
						statement2.setInt(1, husfaderId);
						statement2.executeUpdate();
						generatedKeys = statement2.getGeneratedKeys();

						if (generatedKeys.next()) {
							familieId = generatedKeys.getInt(1);
						} else {
							familieId = 0;
						}
						generatedKeys.close();

						statement2 = conn.prepareStatement(UPDATE1);
						statement2.setInt(1, familieId);
						statement2.setInt(2, barnId);
						statement2.executeUpdate();

						statement2 = conn.prepareStatement(UPDATE1);
						statement2.setInt(1, familieId);
						statement2.setInt(2, individId);
					} else {
						statement2.close();
						statement2 = conn.prepareStatement(INSERT5);
						statement2.setInt(1, individId);
						statement2.setString(2, rs1.getString("ROLLE").trim());
						statement2.setInt(3, individBegivenhedsId);
					}
					statement2.executeUpdate();
				}
			}

			statement2 = conn.prepareStatement(UPDATE2);
			statement2.setString(1, afQ(sb.toString()));
			statement2.setInt(2, individBegivenhedsId);
			statement2.executeUpdate();
			statement2.close();
		}

		conn.close();
		return taeller;
	}
}
