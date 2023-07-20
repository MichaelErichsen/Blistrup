package net.myerichsen.blistrup.batch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.myerichsen.archivesearcher.util.Fonkod;

/**
 * Testklasse til at loade data fra grundtabellen ind i GEDCOM-tabeller
 *
 * @author michael
 *
 */
public class InputTest {
	private static final String SET_SCHEMA = "SET SCHEMA = ?";
	private static final String SELECT = "SELECT * FROM F9PERSONFAMILIEQ FETCH FIRST 50 ROWS ONLY";
	private static final String DELETE1 = "DELETE FROM INDIVID";
	private static final String INSERT1 = "INSERT INTO INDIVID (KOEN, BLISTRUPID) VALUES (?, ?)";
	private static final String DELETE2 = "DELETE FROM PERSONNAVN";
	private static final String INSERT2 = "INSERT INTO PERSONNAVN (INDIVIDID, FORNAVN, EFTERNAVN, "
			+ "PRIMAERNAVN, FONETISKNAVN) VALUES (?, ?, ?, ?, ?)";
	private static final String DELETE3 = "DELETE FROM INDIVIDBEGIVENHED";
	private static final String INSERT3 = "INSERT INTO INDIVIDBEGIVENHED (INDIVIDID, ALDER, BEGTYPE, "
			+ "DATO, NOTE, ROLLE, BLISTRUPID, KILDEID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String DELETE4 = "DELETE FROM VIDNE";
	private static final String INSERT4 = "INSERT INTO VIDNE (INDIVIDID, ROLLE, INDIVIDBEGIVENHEDID) VALUES (?, ?, ?)";
	private static final String DELETE5 = "DELETE FROM KILDE";
	private static final String INSERT5 = "INSERT INTO KILDE (KBNR, AARINTERVAL, "
			+ "KBDEL, TIFNR, OPSLAG, OPNR) VALUES(?, ?, ?, ?, ?, ?)";
	private static final String DELETE6 = "DELETE FROM FAMILIE";
//	private static final String INSERT6 = "INSERT INTO FAMILIE (HUSFADER) VALUES(?)";
//	private static final String UPDATE = "UPDATE INDIVID SET FAMC = ? WHERE ID = ?";
	private static final Fonkod fonkod = new Fonkod();

	/**
	 * Hovedklasse
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		final InputTest inputTest = new InputTest();
		try {
			inputTest.execute();
			System.out.println("Færdig");
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
		PreparedStatement statement = conn.prepareStatement(SET_SCHEMA);
		statement.setString(1, "BLISTRUP");
		statement.execute();
		statement = conn.prepareStatement(DELETE1);
		statement.executeUpdate();
		statement = conn.prepareStatement(DELETE2);
		statement.executeUpdate();
		statement = conn.prepareStatement(DELETE3);
		statement.executeUpdate();
		statement = conn.prepareStatement(DELETE4);
		statement.executeUpdate();
		statement = conn.prepareStatement(DELETE5);
		statement.executeUpdate();
		statement = conn.prepareStatement(DELETE6);
		statement.executeUpdate();
		return conn;
	}

	/**
	 * Arbejderklasse
	 *
	 * @throws SQLException
	 */
	private void execute() throws SQLException {
		int individTaeller = 1;
//		int personNavnTaeller = 1;
		int individBegivenhedTaeller = 1;
//		int vidneTaeller = 1;
		int kildeTaeller = 1;
		String koen = "";
		String blistrupId = "";
		String type = "";
		String fornvn = "";
		String efternvn = "";
		String dato = "";
		String mm = "";
		String dd = "";
		String rolle = "";
		ResultSet generatedKeys = null;

		final Connection conn = connect();
		final PreparedStatement statement = conn.prepareStatement(SELECT);
		final PreparedStatement statement1 = conn.prepareStatement(INSERT1, Statement.RETURN_GENERATED_KEYS);
		final PreparedStatement statement2 = conn.prepareStatement(INSERT2, Statement.RETURN_GENERATED_KEYS);
		final PreparedStatement statement3 = conn.prepareStatement(INSERT3, Statement.RETURN_GENERATED_KEYS);
		final PreparedStatement statement4 = conn.prepareStatement(INSERT4, Statement.RETURN_GENERATED_KEYS);
		final PreparedStatement statement5 = conn.prepareStatement(INSERT5, Statement.RETURN_GENERATED_KEYS);
//		final PreparedStatement statement6 = conn.prepareStatement(INSERT6, PreparedStatement.RETURN_GENERATED_KEYS);
//		final PreparedStatement statement7 = conn.prepareStatement(UPDATE, PreparedStatement.RETURN_GENERATED_KEYS);
		final ResultSet rs = statement.executeQuery();

		while (rs.next()) {
			type = rs.getString("TYPE").trim();

			if (testTableRows(rs)) {
				continue;
			}

			if ("A".equals(type)) {
				try {
					koen = rs.getString("SEX").trim();
					blistrupId = rs.getString("PID").trim();
					rolle = afQ(rs.getString("ROLLE"));

					statement1.setString(1, koen);
					statement1.setString(2, blistrupId);
					statement1.executeUpdate();
					generatedKeys = statement1.getGeneratedKeys();

					if (generatedKeys.next()) {
						individTaeller = generatedKeys.getInt(1);
					}

					statement2.setInt(1, individTaeller);
					fornvn = afQ(rs.getString("FORNVN"));
					statement2.setString(2, fornvn);
					efternvn = afQ(rs.getString("EFTERNVN"));
					statement2.setString(3, efternvn);
					statement2.setString(4, "TRUE");
					statement2.setString(5, fonkod.generateKey(fornvn + " " + efternvn).trim());
					statement2.executeUpdate();

					statement5.setString(1, rs.getString("KBNR").trim());
					statement5.setString(2, rs.getString("KILDE").trim());
					statement5.setString(3, rs.getString("KBDEL").trim());
					statement5.setString(4, rs.getString("TIFNR").trim());
					statement5.setString(5, rs.getString("OPSLAG").trim());
					statement5.setString(6, rs.getString("OPNR").trim());
					statement5.executeUpdate();
					generatedKeys = statement5.getGeneratedKeys();

					if (generatedKeys.next()) {
						kildeTaeller = generatedKeys.getInt(1);
					}

					if ("barn".equals(rolle)) {
						statement3.setInt(1, individTaeller);
						statement3.setString(2, "0");
						statement3.setString(3, "Daab");
						dato = rs.getString("FQODTDATO").trim();
						System.out.println(dato);
						mm = dato.substring(4, 6);
						dd = dato.substring(6, 8);
						statement3.setString(4, dato.substring(0, 4) + "-" + ("00".equals(mm) ? "01" : mm) + "-"
								+ ("00".equals(dd) ? "01" : dd));
						statement3.setString(5, afQ(rs.getString("FADER").trim() + " " + rs.getString("MODER")));
						statement3.setString(6, rolle);
						statement3.setString(7, afQ(rs.getString("BEGIV")));
						statement3.setInt(8, kildeTaeller);
						statement3.executeUpdate();
						generatedKeys = statement3.getGeneratedKeys();

						if (generatedKeys.next()) {
							individBegivenhedTaeller = generatedKeys.getInt(1);
						}
					} else {
						// TODO Create parent as individual
						statement4.setInt(1, individTaeller);
						statement4.setString(2, rolle);
						statement4.setInt(3, individBegivenhedTaeller);
						statement4.executeUpdate();
					}

// TODO private static final String INSERT6 = "INSERT INTO FAMILIE (HUSFADER) VALUES(?)";
// TODO private static final String UPDATE = "UPDATE INDIVID SET FAMC = ? WHERE ID = ?";

				} catch (final Exception e) {
					e.printStackTrace();
				}

			}
		}

		conn.close();
	}

	/**
	 * Test om ordet "KONTROL" findes i rækken
	 *
	 * @param rs ResultSet fra query
	 * @return boolean
	 * @throws SQLException
	 */
	private boolean testTableRows(ResultSet rs) throws SQLException {
		final int columnCount = rs.getMetaData().getColumnCount();

		for (int i = 1; i < columnCount + 1; i++) {
			final String string = rs.getString(i);
			if (string != null && "KONTROL".equals(string.trim())) {
				return true;
			}
		}
		return false;
	}

}
