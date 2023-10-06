package net.myerichsen.blistrup.loaders;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.myerichsen.blistrup.models.KildeModel;

/**
 * Indlæs lægdsruller
 *
 * @author Michael Erichsen
 * @version 5. okt. 2023
 *
 */

public class LaegdsrulleLoader extends AbstractLoader {
	private static final String SELECT1 = "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'R' ORDER BY AAR, LBNR";

	private static final String INSERTI1 = "INSERT INTO INDIVID (KOEN) VALUES ('M')";
	private static final String INSERTI2 = "INSERT INTO INDIVID (KOEN, FAM, SLGT, FAMC) VALUES ('M', ?, ?, ?)";
	private static final String INSERTI3 = "INSERT INTO PERSONNAVN (INDIVIDID, STDNAVN, FONETISKNAVN, PRIMAERNAVN) VALUES (?, ?, ?, 'TRUE')";
	private static final String INSERTI4 = "INSERT INTO INDIVIDBEGIVENHED (INDIVIDID, ALDER, KILDEID, BEGTYPE, DATO, DETALJER, STEDNAVN) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?)";
	private static final String INSERTF1 = "INSERT INTO FAMILIE (HUSFADER) VALUES (?)";
	private static final String INSERTV1 = "INSERT INTO VIDNE (INDIVIDID, ROLLE, INDIVIDBEGIVENHEDID) VALUES (?, ?, ?)";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			final int taeller = new LaegdsrulleLoader().load();
			System.out.println("Har indlæst " + taeller + " lægdsrullelinier");
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return
	 * @throws SQLException
	 */
	public int load() throws SQLException {
		ResultSet generatedKeys;
		int faderId = 0;
		String faderNavn = "";
		int familieId = 0;
		int individId = 0;
		int taeller = 0;
		String stdNavn = "";
		String detaljer = "";
		String alder = "";
		int individBegivenhedId = 0;

		final Connection conn = connect("BLISTRUP");
		final KildeModel kModel = new KildeModel();
		kModel.setKbNr("Lægdsrulle");
		kModel.setAarInterval("1792-1845");
		final int kildeId = kModel.insert(conn);

		final PreparedStatement statements1 = conn.prepareStatement(SELECT1);
		final PreparedStatement statementi1 = conn.prepareStatement(INSERTI1, Statement.RETURN_GENERATED_KEYS);
		final PreparedStatement statementi2 = conn.prepareStatement(INSERTI2, Statement.RETURN_GENERATED_KEYS);
		final PreparedStatement statementi3 = conn.prepareStatement(INSERTI3);
		final PreparedStatement statementi4 = conn.prepareStatement(INSERTI4, Statement.RETURN_GENERATED_KEYS);
		final PreparedStatement statementf1 = conn.prepareStatement(INSERTF1, Statement.RETURN_GENERATED_KEYS);
		final PreparedStatement statementv1 = conn.prepareStatement(INSERTV1);

		// Hent en linie med søn og fader
		// SELECT1 = "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'R' ORDER BY AAR,
		// LBNR";

		final ResultSet rss1 = statements1.executeQuery();

		while (rss1.next()) {
			// Indsæt fader som person og hans navn
			// INSERTI1 = "INSERT INTO INDIVID (KOEN)

			statementi1.executeUpdate();
			generatedKeys = statementi1.getGeneratedKeys();

			if (generatedKeys.next()) {
				faderId = generatedKeys.getInt(1);
			} else {
				faderId = 0;
			}

			generatedKeys.close();

			// INSERT3 = "INSERT INTO PERSONNAVN (INDIVIDID, STDNAVN, FONETISKNAVN,
			// PRIMAERNAVN) VALUES

			statementi3.setInt(1, faderId);
			faderNavn = afQ(rss1.getString("FADER"));
			statementi3.setString(2, cleanName(faderNavn));

			try {
				statementi3.setString(3, fonkod.generateKey(faderNavn).trim());
			} catch (final Exception e) {
				statementi3.setString(3, "");
			}

			statementi3.executeUpdate();

			// Indsæt familie med fader som fader
			// INSERTF1 = "INSERT INTO FAMILIE (HUSFADER) VALUES (?)";

			statementf1.setInt(1, faderId);
			statementf1.executeUpdate();
			generatedKeys = statementf1.getGeneratedKeys();

			if (generatedKeys.next()) {
				familieId = generatedKeys.getInt(1);
			} else {
				familieId = 0;
			}

			generatedKeys.close();

			// Indsæt søn som person med familien som famc og hans navn
			// INSERTI2 = "INSERT INTO INDIVID (KOEN, FAM, SLGT, FAMC)

			statementi2.setString(1, rss1.getString("FAM"));
			statementi2.setString(2, rss1.getString("SLGT"));
			statementi2.setInt(3, familieId);
			statementi2.executeUpdate();
			generatedKeys = statementi2.getGeneratedKeys();

			if (generatedKeys.next()) {
				individId = generatedKeys.getInt(1);
			} else {
				individId = 0;
			}

			generatedKeys.close();

			// INSERTI3 = "INSERT INTO PERSONNAVN (INDIVIDID, STDNAVN, FONETISKNAVN,
			// PRIMAERNAVN) VALUES (?, ?, ?, 'TRUE')";

			statementi3.setInt(1, individId);
			stdNavn = afQ(rss1.getString("HP"));
			statementi3.setString(2, cleanName(stdNavn));

			try {
				statementi3.setString(3, fonkod.generateKey(faderNavn).trim());
			} catch (final Exception e) {
				statementi3.setString(3, "");
			}

			statementi3.executeUpdate();

			// Indsæt fødsel som hændelse
			// INSERTI4 = "INSERT INTO INDIVIDBEGIVENHED (INDIVIDID, ALDER, KILDEID,
			// BEGTYPE, DATO, DETALJER, STEDNAVN) "

			statementi4.setInt(1, individId);
			statementi4.setString(2, "0");
			statementi4.setInt(3, kildeId);
			statementi4.setString(4, "Fødsel");
			statementi4.setDate(5, Date.valueOf(getFoedtDoebtDato(rss1)));
			statementi4.setString(6, "Lægdsrulleoplysninger");
			statementi4.setString(7, fixStedNavn(rss1.getString("FQODESTED")));
			statementi4.executeUpdate();

			// Indsæt lægdsrulle som hændelse
			// INSERTI4 = "INSERT INTO INDIVIDBEGIVENHED (INDIVIDID, ALDER, KILDEID,
			// BEGTYPE, DATO, DETALJER, STEDNAVN) "

			statementi4.setInt(1, individId);
			alder = rss1.getString("ALDER");

			if (alder == null || alder.isBlank()) {
				alder = "0";
			}

			statementi4.setInt(2, Integer.parseInt(alder.trim()));
			statementi4.setInt(3, kildeId);
			statementi4.setString(4, "Lægdsrulle");
			statementi4.setDate(5, Date.valueOf(rss1.getString("AAR") + "-01-01"));

			detaljer = "Rulle " + rss1.getString("RULLE") + ", opslag " + rss1.getString("OPSLAG") + "\r\n4 CONT Søn: "
					+ stdNavn + "\r\n4 CONT Fader " + faderNavn;

			statementi4.setString(6, detaljer);

			statementi4.setString(7, fixStedNavn(rss1.getString("STEDNAVN")));
			statementi4.executeUpdate();
			generatedKeys = statementi4.getGeneratedKeys();

			if (generatedKeys.next()) {
				individBegivenhedId = generatedKeys.getInt(1);
			} else {
				individBegivenhedId = 0;
			}

			generatedKeys.close();

			// Indsæt fader som vidne til lægdsrullen
			// INSERTV1 = "INSERT INTO VIDNE (INDIVIDID, ROLLE, INDIVIDBEGIVENHEDID) VALUES
			statementv1.setInt(1, faderId);
			statementv1.setString(2, "Fader");
			statementv1.setInt(3, individBegivenhedId);
			statementv1.executeUpdate();

			taeller++;

		}

		conn.commit();
		conn.close();
		return taeller;
	}
}
