package net.myerichsen.blistrup.loaders;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.myerichsen.blistrup.models.IndividBegivenhedModel;
import net.myerichsen.blistrup.models.IndividModel;
import net.myerichsen.blistrup.models.KildeModel;
import net.myerichsen.blistrup.models.PersonNavneModel;

/**
 * Indlæs tilgangslister
 *
 * @author Michael Erichsen
 * @version 30. sep. 2023
 *
 */

public class TilgangsListeLoader extends AbstractLoader {
	private static final String SELECT1 = "SELECT * FROM APP.TILAFGANGSLISTE WHERE Ï__TYPE = 'T' ORDER BY AAR, NR";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			new ClearTables().clear();
			final int taeller = new TilgangsListeLoader().load();
			System.out.println("Har indlæst " + taeller + " tilgange");
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return
	 * @throws SQLException
	 */
	public int load() throws SQLException {
		int individId = 0;
		int taeller = 0;
		String stdNavn = "";
		String detaljer = "";
		String alder = "";
		IndividModel iModel;
		PersonNavneModel pnModel;
		IndividBegivenhedModel ibModel;
		String foedt = "";
		String fra = "";
		String haandtering = "";
		String til = "";
		Date fullDate;

		final Connection conn = connect("BLISTRUP");
		final KildeModel kModel = new KildeModel();
		kModel.setKbNr("Tilgangsliste");
		kModel.setAarInterval("1816-76");
		final int kildeId = kModel.insert(conn);

		final PreparedStatement statements1 = conn.prepareStatement(SELECT1);

		// Hent en linie
		// SELECT1 = "SELECT * FROM APP.TILAFGANGSLISTEILIEQ WHERE TYPE = 'T' ORDER BY
		// AAR, NR

		final ResultSet rs = statements1.executeQuery();

		while (rs.next()) {
			// Indsæt person

			iModel = new IndividModel();
			foedt = rs.getString("FQODT");
			iModel.setFoedt(foedt);
			iModel.setKoen("M".equals(rs.getString("KQON").toUpperCase()) ? "M" : "F");
			individId = iModel.insert(conn);

			// Indsæt navn

			pnModel = new PersonNavneModel();
			pnModel.setIndividId(individId);
			stdNavn = afQ(rs.getString("NAVN"));
			pnModel.setStdnavn(cleanName(stdNavn));
			try {
				pnModel.setFonetiskNavn(fonkod.generateKey(stdNavn));
			} catch (final Exception e) {
			}
			pnModel.setPrimaerNavn(true);
			pnModel.insert(conn);

			// Indsæt individbegivenhed

			ibModel = new IndividBegivenhedModel();
			ibModel.setIndividId(individId);
			alder = rs.getString("ALDER").replace("?", "");

			if (alder != null && !alder.isBlank()) {
				try {
					ibModel.setAlder(Integer.parseInt(alder.trim()));
				} catch (final NumberFormatException e) {
				}
			}

			ibModel.setKildeId(kildeId);
			ibModel.setBegType("Tilgang");
			fullDate = findFuldDato(rs);
			ibModel.setDato(fullDate);
			fra = rs.getString("FRA");

			if (!fra.isBlank()) {
				ibModel.setNote("fra " + afQ(fra));
			}

			haandtering = rs.getString("HAANDTERING");

			haandtering = haandtering == null || haandtering.isBlank() ? "" : "Håndtering: " + afQ(haandtering) + ", ";
			detaljer = "Kirkebog " + rs.getString("KIRKEBOG") + ", Opslag " + rs.getString("OPSLAG") + "\r\n4 CONT "
					+ haandtering + afQ(rs.getString("BEM"));

			ibModel.setDetaljer(detaljer);
			ibModel.setFoedt(foedt + "-01-01");
			til = rs.getString("TIL");

			if (til.isBlank() || "Blistrup".equals(til)) {
				til = "Blistrup, Holbo, Frederiksborg";
			} else {
				til = fixStedNavn(afQ(fra));
			}

			ibModel.setStedNavn(til);
			ibModel.insert(conn);

			taeller++;

		}

		conn.commit();
		conn.close();
		return taeller;
	}
}
