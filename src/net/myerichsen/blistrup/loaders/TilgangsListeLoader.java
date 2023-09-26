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
 * @version 26. sep. 2023
 *
 */

public class TilgangsListeLoader extends AbstractLoader {
	private static final String SELECT1 = "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'T' ORDER BY AAR, LBNR";

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
		String fra = "";

		final Connection conn = connect("BLISTRUP");
		final KildeModel kModel = new KildeModel();
		kModel.setKbNr("Tilgangsliste");
		kModel.setAarInterval("1815-75");
		final int kildeId = kModel.insert(conn);

		final PreparedStatement statements1 = conn.prepareStatement(SELECT1);

		// Hent en linie
		// SELECT1 = "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'T' ORDER BY AAR,
		// LBNR"

		final ResultSet rs = statements1.executeQuery();

		while (rs.next()) {
			// Indsæt person

			iModel = new IndividModel();
			iModel.setFoedt(rs.getString("FQODT"));
			iModel.setFam(rs.getString("FAM"));
			iModel.setSlgt(rs.getString("SLGT"));
			iModel.setKoen("M".equals(rs.getString("SEX").toUpperCase()) ? "M" : "F");
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
			alder = rs.getString("ALDER");

			if (alder != null && !alder.isBlank()) {
				ibModel.setAlder(Integer.parseInt(alder.trim()));
			}

			ibModel.setKildeId(kildeId);
			ibModel.setBegType("Tilgang");
			ibModel.setDato(Date.valueOf(rs.getString("AAR") + "-01-01"));
			fra = rs.getString("FRA");

			if (fra.isBlank()) {
				fra = rs.getString("STEDNAVN");
			}

			ibModel.setNote("fra " + (fra.isBlank() ? "Blistrup" : fra));
			detaljer = "Kbnr " + rs.getString("KBNR") + ", " + rs.getString("KILDE") + ", kbdel "
					+ rs.getString("KBDEL") + ", opslag " + rs.getString("OPSLAG") + ", " + afQ(rs.getString("BEM"));
			ibModel.setDetaljer(detaljer);
			ibModel.setFoedt(rs.getString("FQODT") + "-01-01");
			ibModel.setStedNavn(fixStedNavn(afQ(rs.getString("TIL"))));
			ibModel.insert(conn);

			taeller++;

		}

		conn.commit();
		conn.close();
		return taeller;
	}
}
