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
 * Indlæs afgangslister
 *
 * @author Michael Erichsen
 * @version 30. sep. 2023
 *
 */

public class AfgangsListeLoader extends AbstractLoader {
	private static final String SELECT1 = "SELECT * FROM APP.TILAFGANGSLISTE WHERE Ï__TYPE = 'U' ORDER BY AAR, NR";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			final int taeller = new AfgangsListeLoader().load();
			System.out.println("Har indlæst " + taeller + " afgange");
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
		Date fullDate;

		final Connection conn = connect("BLISTRUP");
		final KildeModel kModel = new KildeModel();
		kModel.setKbNr("Afgangsliste");
		kModel.setAarInterval("1816-76");
		final int kildeId = kModel.insert(conn);

		final PreparedStatement statements1 = conn.prepareStatement(SELECT1);

		// Hent en linie
		// SELECT1 = "SELECT * FROM APP.TILAFGANGSLISTEILIEQ WHERE TYPE = 'U' ORDER BY
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
				ibModel.setAlder(Integer.parseInt(alder.trim()));
			}

			ibModel.setKildeId(kildeId);
			ibModel.setBegType("Afgang");
			fullDate = findFuldDato(rs);
			ibModel.setDato(fullDate);
			fra = rs.getString("FRA");

			if (fra.isBlank() || "Blistrup".equals(fra)) {
				fra = "fra Blistrup";
			} else {
				fra = "fra " + afQ(fra) + ", Blistrup";
			}

			ibModel.setNote(fra);

			haandtering = rs.getString("HAANDTERING");

			haandtering = haandtering == null || haandtering.isBlank() ? "" : "Håndtering: " + afQ(haandtering) + ", ";
			detaljer = "Kirkebog " + rs.getString("KIRKEBOG") + ", Opslag " + rs.getString("OPSLAG") + "\r\n4 CONT "
					+ haandtering + afQ(rs.getString("BEM"));

			ibModel.setDetaljer(detaljer);
			ibModel.setFoedt(foedt + "-01-01");
			ibModel.setStedNavn(afQ(rs.getString("TIL")));
			ibModel.insert(conn);

			taeller++;

		}

		conn.commit();
		conn.close();
		return taeller;
	}
}
