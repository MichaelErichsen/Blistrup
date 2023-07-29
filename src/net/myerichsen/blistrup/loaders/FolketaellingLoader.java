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
 * @version 29. jul. 2023
 *
 */
public class FolketaellingLoader {
	private static final String SET_SCHEMA = "SET SCHEMA = 'BLISTRUP'";

	private static final String SELECT1 = "SELECT DISTINCT BEGIV FROM F9PERSONFAMILIEQ WHERE TYPE = 'F'"
			+ " FETCH FIRST 200 ROWS ONLY";
	private static final String SELECT2 = "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'F' AND BEGIV = ? ORDER BY PID";

	private static final String INSERT1 = "INSERT INTO INDIVID (KOEN, BLISTRUPID, FOEDT, FAMC) VALUES (?, ?, ?, ?)";
	private static final String INSERT2 = "INSERT INTO PERSONNAVN (INDIVIDID, FORNAVN, EFTERNAVN, PRIMAERNAVN, FONETISKNAVN, STDNAVN) VALUES (?, ?, ?, ?, ?, ?)";
	private static final String INSERT3 = "INSERT INTO KILDE (KBNR, AARINTERVAL, KBDEL, TIFNR, OPSLAG, OPNR) VALUES(?, ?, ?, ?, ?, ?)";
	private static final String INSERT4 = "INSERT INTO FAMILIE (HUSFADER, HUSMODER) VALUES(?, ?)";
	private static final String INSERT5 = "INSERT INTO FAMILIEBEGIVENHED (FAMILIEID, HUSFADERALDER, HUSMODERALDER, KILDEID, "
			+ "BEGTYPE, DATO, NOTE, BLISTRUPID, STEDNAVN, BEM) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
//	private static final String INSERT6 = "INSERT INTO INDIVID (KOEN, FAMC) VALUES (?, ?)";
//	private static final String INSERT7 = "INSERT INTO VIDNE (INDIVIDID, ROLLE, FAMILIEBEGIVENHEDID) VALUES (?, ?, ?)";

	private static final String UPDATE1 = "UPDATE INDIVID SET FAMC = ? WHERE ID = ?";

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
	 * Convert census year to its date
	 *
	 * @param iAar
	 * @return
	 */
	private Date ftAarToDate(int iAar) {
		String aar;

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
		return Date.valueOf(aar);
	}

	/**
	 * INSERT4 = "INSERT INTO FAMILIE (HUSFADER, HUSMODER)
	 *
	 * @param statement
	 * @param rs1
	 * @param koen
	 * @param headId
	 * @return
	 * @throws SQLException
	 */
	private int insertFamilie(PreparedStatement statement, ResultSet rs1, String koen, int headId) throws SQLException {
		int familieId = 0;

		if ("m".equals(koen)) {
			statement.setInt(1, headId);
			statement.setInt(2, 0);
		} else {
			statement.setInt(1, 0);
			statement.setInt(2, headId);
		}

		statement.executeUpdate();
		final ResultSet generatedKeys = statement.getGeneratedKeys();

		if (generatedKeys.next()) {
			familieId = generatedKeys.getInt(1);
		}
		generatedKeys.close();

		return familieId;
	}

	/**
	 * INSERT5 = "INSERT INTO FAMILIEBEGIVENHED (FAMILIEID, HUSFADERALDER,
	 * HUSMODERALDER, KILDEID, BEGTYPE, DATO, NOTE, BLISTRUPID, STEDNAVN, BEM)
	 *
	 * @param statement
	 * @param rs1
	 * @param familieId
	 * @param koen
	 * @param kildeId
	 * @return
	 * @throws SQLException
	 */
	private int insertFamilieBegivenhed(PreparedStatement statement, ResultSet rs1, int familieId, String koen,
			int kildeId) throws SQLException {

		int familieBegivenhedId = 0;

		statement.setInt(1, familieId);

		final String alder = rs1.getString("ALDER").trim();
		if (alder == null || alder.length() == 0) {
			statement.setInt(2, 0);
			statement.setInt(3, 0);
		} else if ("m".equals(koen)) {
			statement.setInt(2, Integer.parseInt(alder));
			statement.setInt(3, 0);
		} else if ("k".equals(koen)) {
			statement.setInt(2, 0);
			statement.setInt(3, Integer.parseInt(alder));
		} else {
			statement.setInt(2, 0);
			statement.setInt(3, 0);
		}

		statement.setInt(4, kildeId);
		statement.setString(5, "Folketælling");

		final int iAar = Integer.parseInt(rs1.getString("AAR").trim());

		statement.setDate(6, ftAarToDate(iAar));
		statement.setString(7, ""); // Note
		statement.setString(8, afQ(rs1.getString("BEGIV")));
		statement.setString(9, afQ(rs1.getString("STEDNAVN")));

		if (rs1.getString("BEM") != null) {
			statement.setString(10, afQ(rs1.getString("BEM")));
		} else {
			statement.setString(10, "");
		}

		statement.executeUpdate();
		final ResultSet generatedKeys = statement.getGeneratedKeys();

		if (generatedKeys.next()) {
			familieBegivenhedId = generatedKeys.getInt(1);
		}
		generatedKeys.close();

		return familieBegivenhedId;
	}

	/**
	 * INSERT1 = "INSERT INTO INDIVID (KOEN, BLISTRUPID, FOEDT)
	 *
	 * @param statement
	 * @param rs1
	 * @param koen
	 * @param familieId
	 * @return
	 * @throws SQLException
	 */
	private int insertIndivid(PreparedStatement statement, ResultSet rs1, String koen, int familieId)
			throws SQLException {
		int individId = 0;

		// FIXME Sammenblandede parametre rs1 og familieid
		// FIXME Caused by: ERROR 22018: Invalid character string format for type
		// INTEGER: PID
		//
		// Fra sønnesøn

		statement.setString(1, koen);
		statement.setString(2, rs1.getString("PID").trim());
		statement.setString(3, rs1.getString("FQODT").trim());
		statement.setInt(4, familieId);
		statement.executeUpdate();
		final ResultSet generatedKeys = statement.getGeneratedKeys();

		if (generatedKeys.next()) {
			individId = generatedKeys.getInt(1);
		}
		generatedKeys.close();

		return individId;
	}

	/**
	 * INSERT3 = "INSERT INTO KILDE (KBNR, AARINTERVAL, KBDEL, TIFNR, OPSLAG, OPNR)
	 *
	 * @param statement
	 * @param rs1
	 * @return
	 * @throws SQLException
	 */
	private int insertKilde(PreparedStatement statement, ResultSet rs1) throws SQLException {
		int kildeId = 0;

		statement.setString(1, rs1.getString("KBNR").trim());
		statement.setString(2, rs1.getString("KILDE").trim());
		statement.setString(3, rs1.getString("KBDEL").trim());
		statement.setString(4, rs1.getString("TIFNR").trim());
		statement.setString(5, rs1.getString("OPSLAG").trim());
		statement.setString(6, rs1.getString("OPNR").trim());
		statement.executeUpdate();
		final ResultSet generatedKeys = statement.getGeneratedKeys();

		if (generatedKeys.next()) {
			kildeId = generatedKeys.getInt(1);
		}
		generatedKeys.close();

		return kildeId;
	}

	/**
	 * INSERT2 = "INSERT INTO PERSONNAVN (INDIVIDID, FORNAVN, EFTERNAVN,
	 * PRIMAERNAVN, FONETISKNAVN, STDNAVN)
	 *
	 * @param statement
	 * @param rs1
	 * @param individId
	 * @return
	 * @throws SQLException
	 */
	private String insertPersonNavn(PreparedStatement statement, ResultSet rs1, int individId) throws SQLException {
		statement.setInt(1, individId);
		final String fornvn = afQ(rs1.getString("FORNVN"));
		statement.setString(2, fornvn);
		statement.setString(3, afQ(rs1.getString("EFTERNVN")));
		statement.setString(4, "TRUE");
		final String stdnavn = afQ(rs1.getString("STD_NAVN"));

		try {
			statement.setString(5, fonkod.generateKey(stdnavn).trim());
		} catch (final Exception e) {
			statement.setString(5, "");
		}

		statement.setString(6, stdnavn);
		statement.executeUpdate();

		return fornvn;
	}

	/**
	 * @return
	 * @throws SQLException
	 */
	public int load() throws SQLException {
		final List<String> blistrupIdListe = new ArrayList<>();
		String rolle = "";
		int individId = 0;
		int kildeId = 0;
		int familieId = 0;
		int headId = 0;
		int taeller = 0;
		StringBuilder sb;
		String navn = "";
//		final String stdnavn = "";
		boolean husfader = false;
		String koen = "";

		final Connection conn = connect();
		final PreparedStatement statements1 = conn.prepareStatement(SELECT1);
		final PreparedStatement statements2 = conn.prepareStatement(SELECT2);
		final PreparedStatement statementi1 = conn.prepareStatement(INSERT1, Statement.RETURN_GENERATED_KEYS);
		final PreparedStatement statementi2 = conn.prepareStatement(INSERT2);
		final PreparedStatement statementi3 = conn.prepareStatement(INSERT3, Statement.RETURN_GENERATED_KEYS);
		final PreparedStatement statementi4 = conn.prepareStatement(INSERT4, Statement.RETURN_GENERATED_KEYS);
		final PreparedStatement statementi5 = conn.prepareStatement(INSERT5, Statement.RETURN_GENERATED_KEYS);
//		final PreparedStatement statementi6 = conn.prepareStatement(INSERT6, Statement.RETURN_GENERATED_KEYS);
//		final PreparedStatement statementi7 = conn.prepareStatement(INSERT7);
		final PreparedStatement statementu1 = conn.prepareStatement(UPDATE1);

		// SELECT1 = "SELECT DISTINCT BEGIV FROM F9PERSONFAMILIEQ WHERE TYPE = 'F'

		ResultSet rs1 = statements1.executeQuery();

		while (rs1.next()) {
			blistrupIdListe.add(rs1.getString("BEGIV"));
		}

		for (final String blistrupId : blistrupIdListe) {
			sb = new StringBuilder();
			husfader = true;

			// SELECT2 = "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'F' AND BEGIV = ?
			// ORDER BY PID";
			statements2.setString(1, blistrupId);
			rs1 = statements2.executeQuery();

			while (rs1.next()) {
				// First person in household always considered head of household
				rolle = afQ(rs1.getString("ROLLE"));
				navn = afQ(rs1.getString("NAVN"));
				koen = rs1.getString("SEX").trim();
				sb.append(rolle + ": " + navn + ", \r\n");

				// INSERT1 = "INSERT INTO INDIVID (KOEN, BLISTRUPID, FOEDT) VALUES (?, ?, ?)";

				individId = insertIndivid(statementi1, rs1, koen, 0);

				// INSERT2 = "INSERT INTO PERSONNAVN (INDIVIDID, FORNAVN, EFTERNAVN,
				// PRIMAERNAVN, FONETISKNAVN, STDNAVN)

				insertPersonNavn(statementi2, rs1, individId);

				taeller++;

				// Handle roles
				if (husfader || "Husfader".equals(rolle) || rolle.contains("Husfader ") || rolle.contains("Husfader, ")
						|| rolle.contains("Husfader. ") || rolle.contains("Huusbonde")) {
					headId = individId;

					// INSERT3 = "INSERT INTO KILDE (KBNR, AARINTERVAL, KBDEL, TIFNR, OPSLAG, OPNR)

					kildeId = insertKilde(statementi3, rs1);

					// INSERT4 = "INSERT INTO FAMILIE (HUSFADER, HUSMODER)

					familieId = insertFamilie(statementi4, rs1, koen, headId);

					// INSERT5 = "INSERT INTO FAMILIEBEGIVENHED (FAMILIEID, HUSFADERALDER,
					// HUSMODERALDER, KILDEID, BEGTYPE, DATO, NOTE, BLISTRUPID, STEDNAVN, BEM) "

					insertFamilieBegivenhed(statementi5, rs1, familieId, koen, kildeId);

					husfader = false;
				} else if (rolle.contains("Sønnesøn")) {
//					// (Søn) + fornvn + sen
//					// INSERT6 = "INSERT INTO INDIVID (KOEN, FAMC)
//
//					insertIndivid(statementi6, rs1, "m", familieId);
//
//					// INSERT2 = "INSERT INTO PERSONNAVN (INDIVIDID, FORNAVN, EFTERNAVN,
//					// PRIMAERNAVN, FONETISKNAVN, STDNAVN) 
//
//					statementi2.setInt(1, individId);
//					statementi2.setString(2, "(Søn)");
//					statementi2.setString(3, fornvn + "sen");
//					statementi2.setString(4, "TRUE");
//					statementi2.setString(5, "");
//					statementi2.setString(6, "(Søn) " + fornvn + "sen");
//					statementi2.executeUpdate();
//
//					// INSERT4 = "INSERT INTO FAMILIE (HUSFADER, HUSMODER) 
//
//					if (rolle.contains("SQonnesQon")) {
//						statementi4.setInt(1, headId);
//						statementi4.setInt(2, 0);
//					} else {
//						statementi4.setInt(1, 0);
//						statementi4.setInt(2, headId);
//					}
//
//					statementi4.executeUpdate();
//					generatedKeys = statementi4.getGeneratedKeys();
//
//					if (generatedKeys.next()) {
//						familieId2 = generatedKeys.getInt(1);
//					} else {
//						familieId2 = 0;
//					}
//					generatedKeys.close();
//
//					// UPDATE1 = "UPDATE INDIVID SET FAMC = ? WHERE ID = ?";
//
//					updateIndivid(statementu1, familieId2, individId);
//
//					// INSERT7 = "INSERT INTO VIDNE (INDIVIDID, ROLLE, FAMILIEBEGIVENHEDID) 
//
//					statementi7.setInt(1, individId);
//					statementi7.setString(2, rolle);
//					statementi7.setInt(3, familieBegivenhedId);
//					statementi7.executeUpdate();

				} else if (rolle.contains("Datter Datter") || rolle.contains("Datterdatter")
						|| rolle.contains("Børnebørn") || rolle.contains("Barnebarn")) {

//					// Mellemperson
//// (Datter) + fornvn + sdatter etc.
//					// INSERT6 = "INSERT INTO INDIVID (KOEN, FAMC) 
//					// INSERT4 = "INSERT INTO FAMILIE (HUSFADER, HUSMODER)
//
//					// TODO private int insertFamilie(PreparedStatement statement, ResultSet rs1,
//					// String koen, int headId) throws SQLException {
//
//					statementi6.setString(1, "k");
//					statementi6.setInt(2, familieId);
//					statementi6.executeUpdate();
//					generatedKeys.close();
//					generatedKeys = statementi6.getGeneratedKeys();
//
//					if (generatedKeys.next()) {
//						generatedKeys.getInt(1);
//					} else {
//					}
//					generatedKeys.close();
//
//					// INSERT4 = "INSERT INTO FAMILIE (HUSFADER, HUSMODER) 
//
//					// TODO private int insertFamilie(PreparedStatement statement, ResultSet rs1,
//					// String koen, int headId) throws SQLException {
//
//					statementi4.setInt(1, 0);
//					statementi4.setInt(2, headId);
//
//					statementi4.executeUpdate();
//					generatedKeys = statementi4.getGeneratedKeys();
//
//					if (generatedKeys.next()) {
//						familieId2 = generatedKeys.getInt(1);
//					} else {
//						familieId2 = 0;
//					}
//					generatedKeys.close();
//
//					// UPDATE1 = "UPDATE INDIVID SET FAMC = ? WHERE ID = ?";
//
//					updateIndivid(statementu1, familieId2, individId);
				} else if ((rolle.contains("Svigersøn") || rolle.contains("Svigerdatter")
						|| rolle.contains("Sviger-Søn")) || rolle.contains("Søn")) {
					// Anonymt individ med FAMC
					// Nyt individ
					// Ny FAMS med begge

					//
					//
				} else {

					// Other roles
				}

				// UPDATE1 = "UPDATE INDIVID SET FAMC = ? WHERE ID = ?";

				updateIndivid(statementu1, familieId, individId);
			}
		}
		return taeller;
	}

	/**
	 * UPDATE1 = "UPDATE INDIVID SET FAMC = ? WHERE ID = ?
	 *
	 * @param statement
	 * @param familieId2
	 * @param individId
	 * @throws SQLException
	 */
	private void updateIndivid(PreparedStatement statement, int familieId2, int individId) throws SQLException {
		statement.setInt(1, familieId2);
		statement.setInt(2, individId);
		statement.executeUpdate();
	}
}
