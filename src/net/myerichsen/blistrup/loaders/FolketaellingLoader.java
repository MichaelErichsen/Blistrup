package net.myerichsen.blistrup.loaders;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Læs folketællingsdata fra grundtabellen ind i GEDCOM tabeller. Analyser
 * familieforhold så vidt muligt
 *
 * @author Michael Erichsen
 * @version 30. aug. 2023
 *
 */
public class FolketaellingLoader extends AbstractLoader {
	private static final String SELECT1 = "SELECT DISTINCT BEGIV FROM F9PERSONFAMILIEQ WHERE TYPE = 'F'";
//			+ " FETCH FIRST 500 ROWS ONLY";
	private static final String SELECT2 = "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'F' AND BEGIV = ? ORDER BY PID";

	private static final String INSERT1 = "INSERT INTO INDIVID (KOEN, BLISTRUPID, FOEDT, FAMC) VALUES (?, ?, ?, ?)";
	private static final String INSERT2 = "INSERT INTO PERSONNAVN (INDIVIDID, FORNAVN, EFTERNAVN, PRIMAERNAVN, FONETISKNAVN, STDNAVN) VALUES (?, ?, ?, ?, ?, ?)";
	private static final String INSERT3 = "INSERT INTO KILDE (KBNR, AARINTERVAL, KBDEL, TIFNR, OPSLAG, OPNR) VALUES(?, ?, ?, ?, ?, ?)";
	private static final String INSERT4 = "INSERT INTO FAMILIE (HUSFADER, HUSMODER) VALUES(?, ?)";
	private static final String INSERT5 = "INSERT INTO FAMILIEBEGIVENHED (FAMILIEID, HUSFADERALDER, HUSMODERALDER, KILDEID, "
			+ "BEGTYPE, DATO, NOTE, BLISTRUPID, STEDNAVN, BEM) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String INSERT6 = "INSERT INTO VIDNE (INDIVIDID, ROLLE, FAMILIEBEGIVENHEDID) VALUES (?, ?, ?)";

	private static final String UPDATE1 = "UPDATE INDIVID SET FAMC = ? WHERE ID = ?";
	private static final String UPDATE2 = "UPDATE FAMILIE SET HUSMODER = ? WHERE ID = ?";

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
	 * @param rs
	 * @param koen
	 * @param individId
	 * @return
	 * @throws SQLException
	 */
	private int insertFamilie(PreparedStatement statement, ResultSet rs, String koen, int individId)
			throws SQLException {
		int familieId = 0;

		if ("m".equals(koen)) {
			statement.setInt(1, individId);
			statement.setInt(2, 0);
		} else {
			statement.setInt(1, 0);
			statement.setInt(2, individId);
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
	 * @param rs
	 * @param familieId
	 * @param koen
	 * @param kildeId
	 * @return
	 * @throws SQLException
	 */
	private int insertFamilieBegivenhed(PreparedStatement statement, ResultSet rs, int familieId, String koen,
			int kildeId) throws SQLException {

		int familieBegivenhedId = 0;

		statement.setInt(1, familieId);

		final String alder = rs.getString("ALDER").trim();
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

		final int iAar = Integer.parseInt(rs.getString("AAR").trim());

		statement.setDate(6, ftAarToDate(iAar));
		statement.setString(7, ""); // Note
		statement.setString(8, afQ(rs.getString("BEGIV")));
		statement.setString(9, formatPlaceName(afQ(rs.getString("STEDNAVN"))));

		if (rs.getString("BEM") != null) {
			statement.setString(10, afQ(rs.getString("BEM")));
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
	 * @param rs
	 * @param koen
	 * @param familieId
	 * @return
	 * @throws SQLException
	 */
	private int insertIndivid(PreparedStatement statement, ResultSet rs, String koen, int familieId)
			throws SQLException {
		int individId = 0;

		// Sammenblandede parametre rs1 og familieid
		// Caused by: ERROR 22018: Invalid character string format for type
		// INTEGER: PID
		//
		// Fra sønnesøn

		statement.setString(1, koen);
		statement.setString(2, rs.getString("PID").trim());
		statement.setString(3, rs.getString("FQODT").trim());
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
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	private int insertKilde(PreparedStatement statement, ResultSet rs) throws SQLException {
		int kildeId = 0;

		statement.setString(1, rs.getString("KBNR").trim());
		statement.setString(2, rs.getString("KILDE").trim());
		statement.setString(3, rs.getString("KBDEL").trim());
		statement.setString(4, rs.getString("TIFNR").trim());
		statement.setString(5, rs.getString("OPSLAG").trim());
		statement.setString(6, rs.getString("OPNR").trim());
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
	 * @param rs
	 * @param individId
	 * @return
	 * @throws SQLException
	 */
	private String[] insertPersonNavn(PreparedStatement statement, ResultSet rs, int individId) throws SQLException {
		statement.setInt(1, individId);
		final String[] navne = new String[2];

		navne[0] = afQ(rs.getString("FORNVN"));
		statement.setString(2, navne[0]);
		navne[1] = afQ(rs.getString("EFTERNVN"));
		statement.setString(3, navne[1]);
		statement.setString(4, "TRUE");
		final String stdnavn = afQ(rs.getString("STD_NAVN"));

		try {
			statement.setString(5, fonkod.generateKey(stdnavn).trim());
		} catch (final Exception e) {
			statement.setString(5, "");
		}

		statement.setString(6, stdnavn);
		statement.executeUpdate();

		return navne;
	}

	/**
	 * INSERT2 = "INSERT INTO PERSONNAVN (INDIVIDID, FORNAVN, EFTERNAVN,
	 * PRIMAERNAVN, FONETISKNAVN, STDNAVN)
	 *
	 * @param statement
	 * @param rs1
	 * @param soenId
	 * @param fornvn
	 * @param efternvn
	 * @throws SQLException
	 */
	private void insertPersonNavn(PreparedStatement statement, ResultSet rs1, int soenId, String fornvn,
			String efternvn) throws SQLException {

		statement.setInt(1, soenId);
		statement.setString(2, fornvn);
		statement.setString(3, efternvn);
		statement.setString(4, "TRUE");
		final String stdnavn = fornvn + " " + efternvn;

		try {
			statement.setString(5, fonkod.generateKey(stdnavn).trim());
		} catch (final Exception e) {
			statement.setString(5, "");
		}

		statement.setString(6, stdnavn);
		statement.executeUpdate();

	}

	/**
	 * INSERT7 = "INSERT INTO VIDNE (INDIVIDID, ROLLE, FAMILIEBEGIVENHEDID)
	 *
	 * @param statement
	 * @param individId
	 * @param rolle
	 * @param familieBegivenhedId
	 * @throws SQLException
	 */
	private void insertVidne(PreparedStatement statement, int individId, String rolle, int familieBegivenhedId)
			throws SQLException {
		statement.setInt(1, individId);
		statement.setString(2, rolle);
		statement.setInt(3, familieBegivenhedId);
		statement.executeUpdate();
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
		boolean husfader = false;
		String koen = "";
		String[] navne;
		String husfaderFornvn = "";
		int soenId = 0;
		String efternavn = "";
		int familieId2 = 0;
		String mellemfornvn = "";
		int datterId = 0;
		int faderfamilie = 0;
		int moderfamilie = 0;

		final Connection conn = connect("BLISTRUP");
		final PreparedStatement statements1 = conn.prepareStatement(SELECT1);
		final PreparedStatement statements2 = conn.prepareStatement(SELECT2);
		final PreparedStatement statementi1 = conn.prepareStatement(INSERT1, Statement.RETURN_GENERATED_KEYS);
		final PreparedStatement statementi2 = conn.prepareStatement(INSERT2);
		final PreparedStatement statementi3 = conn.prepareStatement(INSERT3, Statement.RETURN_GENERATED_KEYS);
		final PreparedStatement statementi4 = conn.prepareStatement(INSERT4, Statement.RETURN_GENERATED_KEYS);
		final PreparedStatement statementi5 = conn.prepareStatement(INSERT5, Statement.RETURN_GENERATED_KEYS);
		final PreparedStatement statementi6 = conn.prepareStatement(INSERT6);
		final PreparedStatement statementu1 = conn.prepareStatement(UPDATE1);
		final PreparedStatement statementu2 = conn.prepareStatement(UPDATE2);

		// Hent alle husstande
		ResultSet rs1 = statements1.executeQuery();

		while (rs1.next()) {
			blistrupIdListe.add(rs1.getString("BEGIV"));
		}

		/**
		 * For hver husstand
		 */
		for (final String blistrupId : blistrupIdListe) {
			sb = new StringBuilder();
			husfader = true;

			statements2.setString(1, blistrupId);
			rs1 = statements2.executeQuery();

			/**
			 * For hver person
			 */
			while (rs1.next()) {
				/**
				 * Første person i husstanden betragtes altid som overhovede
				 */
				rolle = afQ(rs1.getString("ROLLE"));
				navn = afQ(rs1.getString("NAVN"));
				koen = rs1.getString("SEX").trim();
				sb.append(rolle + ": " + navn + ", \r\n4 CONC ");

				individId = insertIndivid(statementi1, rs1, koen, 0);

				navne = insertPersonNavn(statementi2, rs1, individId);

				taeller++;

				/**
				 * Husfader
				 */
				if (husfader || "Husfader".equals(rolle) || rolle.contains("Husfader ") || rolle.contains("Husfader, ")
						|| rolle.contains("Husfader. ") || rolle.contains("Huusbonde")) {
					headId = individId;
					husfaderFornvn = navne[0];

					kildeId = insertKilde(statementi3, rs1);
					familieId = insertFamilie(statementi4, rs1, koen, headId);
					insertFamilieBegivenhed(statementi5, rs1, familieId, koen, kildeId);

					husfader = false;

					/**
					 * Ignorer plejebørn
					 */
				} else if (rolle.toLowerCase().contains("pleye") || rolle.toLowerCase().contains("leye")
						|| rolle.contains("Konens ")) {
					// Ignore

				} else if (rolle.contains("Hustru") || rolle.contains("Kone") || rolle.contains("Husmoder")
						|| rolle.contains("Familiemoder") || rolle.contains("Gaardmandskone")
						|| rolle.contains("Husbondinde") || rolle.contains("Hosbondinde")) {
					updateFamilie(statementu2, individId, familieId);
					insertVidne(statementi6, individId, rolle, familieId);

					/**
					 * Sønnesøn
					 */
				} else if (rolle.contains("Sønnesøn")) {
					// Indsæt beregnet søn

					mellemfornvn = navne[1].replace("sen", "");

					soenId = insertIndivid(statementi1, rs1, "m", familieId);
					efternavn = husfaderFornvn + "sen";
					insertPersonNavn(statementi2, rs1, soenId, mellemfornvn, efternavn.replace("ss", "s"));
					insertVidne(statementi6, soenId, "Søn", familieId);

					// Indsæt næste generation familie

					familieId2 = insertFamilie(statementi4, rs1, "m", soenId);

					// Indsæt sønnesøn

					updateIndivid(statementu1, familieId2, individId);
					insertVidne(statementi6, individId, rolle, familieId2);

					/**
					 * Datterbørn
					 */
				} else if (rolle.contains("Datter Datter") || rolle.contains("Datterdatter")
						|| rolle.contains("Børnebørn") || rolle.contains("Barnebarn")
						|| rolle.contains("Datters uægte søn") || rolle.contains("Datterbørn")
						|| rolle.contains("Dattersøn")) {
					// Indsæt beregnet datter
					mellemfornvn = navne[1].replace("sdatter", "").replace("datter", "");

					datterId = insertIndivid(statementi1, rs1, "k", familieId);
					efternavn = husfaderFornvn + "sdatter";
					insertPersonNavn(statementi2, rs1, datterId, mellemfornvn, efternavn.replace("ss", "s"));
					insertVidne(statementi6, datterId, "Datter", familieId);

					// Indsæt næste generation familie

					familieId2 = insertFamilie(statementi4, rs1, "k", datterId);

					// Indsæt datterbarn

					updateIndivid(statementu1, familieId2, individId);
					insertVidne(statementi6, individId, rolle, familieId2);

				} else if (rolle.contains("Svigersøn") || rolle.contains("Svigerdatter") || rolle.contains("Sviger-Søn")
						|| rolle.contains("Stifdatter")) {

				} else if (rolle.contains("Svigerfader") || rolle.contains("En Broder til Husfaderen")) {
					// Husmoderens Fader
					// Husmoders Fader
					// Huusmoders Fader

				} else if (rolle.contains("Søn") || rolle.contains("Datter") || rolle.contains("Døttre")
						|| rolle.contains("Barn") || rolle.contains("Børn") || rolle.contains("Forsørges af sin Moder")
						|| rolle.contains("Midlertidigt hos Moderen")) {
					updateIndivid(statementu1, familieId, individId);
					insertVidne(statementi6, individId, rolle, familieId);

					/**
					 * Fader
					 */
				} else if (rolle.contains("Fader") || rolle.contains("far") || rolle.contains("forsørges af Sønnen")
						|| rolle.contains("husfader")) {
					// Indsæt ny familie med ind. som husfader on head som barn
					faderfamilie = insertFamilie(statementi4, rs1, "m", individId);
					updateFamilie(statementu2, headId, faderfamilie);
					insertVidne(statementi6, individId, rolle, familieId);

				} else if ((rolle.contains("Svigermoder") || rolle.contains("svigermor")) || rolle.contains("Bedstemoder") || rolle.contains("stifmoder")) {

				} else if (rolle.contains("Moder") || rolle.contains("mor") || rolle.contains("moder")) {
					// Indsæt ny familie med ind. som husmoder on head som barn
					moderfamilie = insertFamilie(statementi4, rs1, "k", individId);
					updateFamilie(statementu2, headId, moderfamilie);
					insertVidne(statementi6, individId, rolle, familieId);

				} else {

				}

			}
		}
		return taeller;

	}

	/**
	 * UPDATE2 = "UPDATE FAMILIE SET HUSMODER = ? WHERE ID = ?
	 *
	 * @param statement
	 * @param individId
	 * @param familieId
	 * @throws SQLException
	 */
	private void updateFamilie(PreparedStatement statement, int individId, int familieId) throws SQLException {
		statement.setInt(1, individId);
		statement.setInt(2, familieId);
		statement.executeUpdate();
	}

	/**
	 * UPDATE1 = "UPDATE INDIVID SET FAMC = ? WHERE ID = ?
	 *
	 * @param statement
	 * @param familieId
	 * @param individId
	 * @throws SQLException
	 */
	private void updateIndivid(PreparedStatement statement, int familieId, int individId) throws SQLException {
		statement.setInt(1, familieId);
		statement.setInt(2, individId);
		statement.executeUpdate();
	}
}
