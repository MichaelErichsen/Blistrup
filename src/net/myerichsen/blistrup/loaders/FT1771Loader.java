package net.myerichsen.blistrup.loaders;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import net.myerichsen.blistrup.models.FamilieBegivenhedModel;
import net.myerichsen.blistrup.models.FamilieModel;
import net.myerichsen.blistrup.models.IndividBegivenhedModel;
import net.myerichsen.blistrup.models.IndividModel;
import net.myerichsen.blistrup.models.KildeModel;
import net.myerichsen.blistrup.models.PersonNavneModel;
import net.myerichsen.blistrup.util.Fonkod;

/**
 * Load en FT 1771 tabel
 *
 * @author Michael Erichsen
 * @version 29. aug. 2023
 *
 */
public class FT1771Loader extends AbstractLoader {
	private static final String SELECT1 = "SELECT * FROM APP.FT1771";
	private static PreparedStatement statements1;
	private static final Fonkod fonkod = new Fonkod();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		args = new String[] { "C:\\Users\\michael\\BlistrupDB" };

		try {
			final int taeller = new FT1771Loader().load(args);
			System.out.println("Har indlæst " + taeller + " folketællingslinier");
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Forbind til databasen
	 *
	 * @param dbPath
	 * @return
	 * @throws SQLException
	 */
	private Connection connect(String dbPath) throws SQLException {
		final Connection conn = DriverManager.getConnection("jdbc:derby:" + dbPath);
		statements1 = conn.prepareStatement(SELECT1);
		return conn;

	}

	/**
	 * Formatter en tabelrække som tekst
	 *
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	private String getTableRow(ResultSet rs) throws SQLException {
		final ResultSetMetaData rsmd = rs.getMetaData();
		final StringBuilder sb = new StringBuilder();
		final int columnCount = rsmd.getColumnCount();

		for (int i = 1; i < columnCount + 1; i++) {
			if (rs.getString(i) != null) {
				sb.append("4 CONC " + rsmd.getColumnName(i).trim() + ": ");
				sb.append(rs.getString(i).trim() + "\r\n");
			}
		}
		return sb.toString();
	}

	/**
	 * Load tabeller
	 *
	 * @param args
	 * @return
	 * @throws SQLException
	 */
	private int load(String[] args) throws SQLException {
		int count = 0;
		String stdnavn = "";
		IndividModel iModel;
		int foedt = 0;
		int doed = 0;
		String[] decimaltal;
		IndividBegivenhedModel ibModel;
		PersonNavneModel pModel;
		int faderId;
		String[] parts;
		String efternavn;
		int kildeId = 0;
		int moderId = 0;
		String streng = "";
		FamilieModel fModel;
		int familieId = 0;
		FamilieBegivenhedModel fbModel;

		final Connection conn = connect(args[0]);
		final ResultSet rs = statements1.executeQuery();

		while (rs.next()) {
			/**
			 * Husfader
			 */
			iModel = new IndividModel();
			iModel.setKoen("M");
			if ("Ja".equals(rs.getString("MAND_DØD__JA_NEJ_"))) {
				if (!rs.getString("ENKESTAND_ANTAL_ÅR").isBlank()) {
					decimaltal = rs.getString("ENKESTAND_ANTAL_ÅR").split(",");
					doed = 1771 - Integer.parseInt(decimaltal[0]);
					iModel.setDoed(Integer.toString(doed));
				} else {
					iModel.setDoed("BEF 1771");
				}
			} else {
				foedt = 1771 - Integer.parseInt(rs.getString("MANDENS_ALDER"));
				iModel.setFoedt(Integer.toString(foedt));
			}
			faderId = iModel.insert(conn);

			/**
			 * Faders navn
			 */
			pModel = new PersonNavneModel();
			pModel.setIndividId(faderId);
			stdnavn = rs.getString("MANDENS_NAVN");
			try {
				pModel.setFonetiskNavn(fonkod.generateKey(stdnavn));
			} catch (final Exception e) {
			}
			parts = stdnavn.split(" ");
			efternavn = parts[parts.length - 1];
			pModel.setFornavn(stdnavn.replace(efternavn, "").trim());
			pModel.setEfternavn(efternavn);
			stdnavn = stdnavn.replace(efternavn, "/" + efternavn + "/");
			pModel.setStdnavn(stdnavn);
			pModel.insert(conn);

			/**
			 * Kilde: Folketælling 1771
			 */
			final KildeModel kModel = new KildeModel();
			kModel.setKbNr("Folketælling");
			kModel.setAarInterval("1771");
			kildeId = kModel.insert(conn);

			/**
			 * Erhverv
			 */
			ibModel = new IndividBegivenhedModel();
			ibModel.setIndividId(faderId);
			ibModel.setKildeId(kildeId);
			ibModel.setBegType("Erhverv");
			ibModel.setNote(rs.getString("MANDENS_ERHVERV"));
			ibModel.insert(conn);

			/**
			 * Husmoder
			 */
			iModel = new IndividModel();
			iModel.setKoen("F");
			streng = rs.getString("HUSTRUS_ALDER");
			if (!streng.isBlank()) {
				foedt = 1771 - Integer.parseInt(streng);
				iModel.setFoedt(Integer.toString(foedt));
			}
			if ("Ja".equals(rs.getString("HUSTRU_DØD__JA_NEJ_"))) {
				iModel.setDoed("BEF 1771");
			}
			moderId = iModel.insert(conn);

			/**
			 * Moders navn
			 */
			pModel = new PersonNavneModel();
			pModel.setIndividId(moderId);
			pModel.setStdnavn("? /?/");
			pModel.setFornavn("?");
			pModel.setEfternavn("?");
			pModel.insert(conn);

			/**
			 * Hustru erhverv (hvis hustrus forsørgelse ikke er tom)
			 */
			streng = rs.getString("HUSTRUS_FORSØRGELSE");
			if (!streng.isBlank()) {
				ibModel = new IndividBegivenhedModel();
				ibModel.setIndividId(moderId);
				ibModel.setKildeId(kildeId);
				ibModel.setBegType("Erhverv");
				ibModel.setNote(streng);
				ibModel.insert(conn);
			}

			/**
			 * Familie
			 */
			fModel = new FamilieModel();
			fModel.setFader(faderId);
			fModel.setModer(moderId);
			familieId = fModel.insert(conn);

			/**
			 * Vielse (Familiebegivenhed)
			 */
			fbModel = new FamilieBegivenhedModel();
			fbModel.setFamilieId(familieId);
			fbModel.setBegType("Vielse");
			fbModel.setKildeId(kildeId);
			fbModel.insert(conn);

			/**
			 * Folketælling (Familiebegivenhed)
			 */
			fbModel = new FamilieBegivenhedModel();
			fbModel.setFamilieId(familieId);
			fbModel.setBegType("Folketælling");
			fbModel.setKildeId(kildeId);
			fbModel.setDato(Date.valueOf("1771-02-01"));
			fbModel.setStedNavn("Blistrup,,,");
			fbModel.setDetaljer(getTableRow(rs));
			fbModel.insert(conn);

			count++;
		}

		conn.close();
		return count;
	}
}
