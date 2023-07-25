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
 * Indlæs begravelser
 * 
 * SELECT1 = "SELECT DISTINCT BEGIV FROM F9PERSONFAMILIEQ WHERE TYPE = 'D';
 * 
 * For hver BEGIV
 * 
 * SELECT2 = "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'D' AND BEGIV = ?
 * ORDER BY PID";
 * 
 * For hver PID heri
 * 
 * INSERT1 = "INSERT INTO INDIVID (KOEN, BLISTRUPID) VALUES (?, ?)";
 * 
 * INSERT2 = "INSERT INTO PERSONNAVN (INDIVIDID, STDNAVN, FONETISKNAVN,
 * PRIMAERNAVN)
 * 
 * Hvis rolle er død, er det primærpersonen
 * 
 * INSERT3 = "INSERT INTO KILDE (KBNR, AARINTERVAL, KBDEL, TIFNR, OPSLAG, OPNR)
 * 
 * INSERT4 = "INSERT INTO INDIVIDBEGIVENHED (INDIVIDID, ALDER, BEGTYPE, DATO,
 * NOTE, ROLLE, BLISTRUPID, KILDEID, STEDNAVN, BEM),
 * 
 * hvor BEGTYPE er Begravelse
 * 
 * @author Michael Erichsen
 * @version 25. jul. 2023
 *
 */
public class BegravelseLoader {
	private static final String SET_SCHEMA = "SET SCHEMA = 'BLISTRUP'";

	private static final String SELECT1 = "SELECT DISTINCT BEGIV FROM F9PERSONFAMILIEQ WHERE TYPE = 'D' FETCH FIRST 50 ROWS ONLY";
	private static final String SELECT2 = "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'D' AND BEGIV = ? ORDER BY PID";

	private static final String INSERT1 = "INSERT INTO INDIVID (KOEN, BLISTRUPID) VALUES (?, ?)";
	private static final String INSERT2 = "INSERT INTO PERSONNAVN (INDIVIDID, STDNAVN, FONETISKNAVN, PRIMAERNAVN) VALUES (?, ?, ?, 'TRUE')";
	private static final String INSERT3 = "INSERT INTO KILDE (KBNR, AARINTERVAL, KBDEL, TIFNR, OPSLAG, OPNR) VALUES(?, ?, ?, ?, ?, ?)";
	private static final String INSERT4 = "INSERT INTO INDIVIDBEGIVENHED (INDIVIDID, ALDER, BEGTYPE, DATO, NOTE, ROLLE, BLISTRUPID, KILDEID, STEDNAVN, BEM, FOEDT) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"; // Begravelse, død, fødsel, dåb
	private static final String INSERT5 = "INSERT INTO FAMILIE (HUSFADER, HUSMODER) VALUES(?, ?)";

	private static final String UPDATE1 = "UPDATE INDIVID SET FAMC = ? WHERE ID = ?";
	private static final String UPDATE2 = "UPDATE INDIVIDBEGIVENHED SET DETALJER = ? WHERE ID = ?";

	private static final Fonkod fonkod = new Fonkod();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			final int taeller = new BegravelseLoader().load();
			System.out.println("Har indlæst " + taeller + " begravelseslinier");
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

//	BEGIV INDBEG
//	PID IND
//	TYPE Begravelse INDBEG
//	ROLLE: død, æfælle, far, mor, barn INDBEG
//	STDNAVN INDNAVN
//	FADER 
//	MODER
//	FAELLE
//	KILDE
//	SEX
//	ERHVERV
//	ALDER
//	FØDT (Aar)
//	FØDESTED
//	FØDTDATO
//	DØBTSTED
//	DØD (DATO)
//	BEGR (DATO)
//	STEDNAVN (DØD)
//	BEM

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

		// SELECT1 = "SELECT DISTINCT BEGIV FROM F9PERSONFAMILIEQ WHERE TYPE = 'D'

		PreparedStatement statement1 = conn.prepareStatement(SELECT1);
		ResultSet rs1 = statement1.executeQuery();

		while (rs1.next()) {
			blistrupIdListe.add(rs1.getString("BEGIV"));
		}

		for (final String blistrupId : blistrupIdListe) {
			sb = new StringBuilder();

			// SELECT2 = "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'D' AND BEGIV = ?
			// ORDER BY PID";

			statement1 = conn.prepareStatement(SELECT2);
			statement1.setString(1, blistrupId);
			rs1 = statement1.executeQuery();

			while (rs1.next()) {
				rolle = afQ(rs1.getString("ROLLE"));
				navn = afQ(rs1.getString("NAVN"));
				sb.append(rolle + ": " + navn + ", \r\n");

				// INSERT1 = "INSERT INTO INDIVID (KOEN, BLISTRUPID) VALUES (?, ?)";

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

				// INSERT2 = "INSERT INTO PERSONNAVN (INDIVIDID, STDNAVN, FONETISKNAVN,
				// PRIMAERNAVN) VALUES

				statement2 = conn.prepareStatement(INSERT2);
				statement2.setInt(1, individId);

				stdnavn = afQ(rs1.getString("STD_NAVN"));
				statement2.setString(2, stdnavn);
				try {
					statement2.setString(3, fonkod.generateKey(stdnavn).trim());
				} catch (final Exception e) {
					statement2.setString(3, "");
				}

				statement2.executeUpdate();

				taeller++;

				// død, barn, far, mor, æfælle
				if ("død".equals(rolle)) {
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
					// NOTE, ROLLE, BLISTRUPID, KILDEID, STEDNAVN, BEM, FOEDT) "

					statement2 = conn.prepareStatement(INSERT4, Statement.RETURN_GENERATED_KEYS);
					statement2.setInt(1, individId);

					try {
						statement2.setInt(2, Integer.parseInt(rs1.getString("ALDER").trim()));
					} catch (Exception e) {
						statement2.setInt(2, 0);
					}

					statement2.setString(3, "Begravelse");
					statement2.setString(4, rs1.getString("AAR").trim() + "-01-01");
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
					statement2.setString(11, rs1.getString("FQODT").trim());
					statement2.executeUpdate();
					generatedKeys = statement2.getGeneratedKeys();

					if (generatedKeys.next()) {
						individBegivenhedsId = generatedKeys.getInt(1);
					} else {
						individBegivenhedsId = 0;
					}
					generatedKeys.close();
				} else if ("barn".equals(rolle)) {

				} else if ("far".equals(rolle)) {
//					husfaderId = individId;

					// INSERT5 = "INSERT INTO VIDNE (INDIVIDID, ROLLE, INDIVIDBEGIVENHEDID) VALUES

//					statement2.close();
//					statement2 = conn.prepareStatement(INSERT5);
//					statement2.setInt(1, individId);
//					statement2.setString(2, rs1.getString("ROLLE").trim());
//					statement2.setInt(3, individBegivenhedsId);
//					statement2.executeUpdate();

					// INSERT6 = "INSERT INTO FAMILIE (HUSFADER) VALUES(?)";

//					statement2 = conn.prepareStatement(INSERT6, Statement.RETURN_GENERATED_KEYS);
//					statement2.setInt(1, husfaderId);
//					statement2.executeUpdate();
//					generatedKeys.close();
//					generatedKeys = statement2.getGeneratedKeys();
//
//					if (generatedKeys.next()) {
//						familieId = generatedKeys.getInt(1);
//					} else {
//						familieId = 0;
//					}
//					generatedKeys.close();
//
//					// UPDATE1 = "UPDATE INDIVID SET FAMC = ? WHERE ID = ?";
//
//					statement2 = conn.prepareStatement(UPDATE1);
//					statement2.setInt(1, familieId);
//					statement2.setInt(2, barnId);
//					statement2.executeUpdate();
				} else if ("mor".equals(rolle)) {
					// TODO If not exist family id
//					husfaderId = individId;
//
//					// INSERT5 = "INSERT INTO VIDNE (INDIVIDID, ROLLE, INDIVIDBEGIVENHEDID) VALUES
//
//					statement2.close();
//					statement2 = conn.prepareStatement(INSERT5);
//					statement2.setInt(1, individId);
//					statement2.setString(2, rs1.getString("ROLLE").trim());
//					statement2.setInt(3, individBegivenhedsId);
//					statement2.executeUpdate();
//
//					// INSERT6 = "INSERT INTO FAMILIE (HUSFADER) VALUES(?)";
//
//					statement2 = conn.prepareStatement(INSERT6, Statement.RETURN_GENERATED_KEYS);
//					statement2.setInt(1, husfaderId);
//					statement2.executeUpdate();
//					generatedKeys.close();
//					generatedKeys = statement2.getGeneratedKeys();
//
//					if (generatedKeys.next()) {
//						familieId = generatedKeys.getInt(1);
//					} else {
//						familieId = 0;
//					}
//					generatedKeys.close();
//
//					// UPDATE1 = "UPDATE INDIVID SET FAMC = ? WHERE ID = ?";
//
//					statement2 = conn.prepareStatement(UPDATE1);
//					statement2.setInt(1, familieId);
//					statement2.setInt(2, barnId);
//					statement2.executeUpdate();
				} else if ("æfælle".equals(rolle)) {

				}

				// INSERT5 = "INSERT INTO FAMILIE (HUSFADER, HUSMODER) VALUES(?, ?)";

				// UPDATE1 = "UPDATE INDIVID SET FAMC = ? WHERE ID = ?";
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
}
