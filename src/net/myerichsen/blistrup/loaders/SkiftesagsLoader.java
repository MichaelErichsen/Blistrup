package net.myerichsen.blistrup.loaders;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.myerichsen.blistrup.models.FamilieModel;
import net.myerichsen.blistrup.models.IndividBegivenhedModel;
import net.myerichsen.blistrup.models.IndividModel;
import net.myerichsen.blistrup.models.KildeModel;
import net.myerichsen.blistrup.models.PersonNavneModel;

/**
 * Indl�s skifteregistre
 *
 * @author Michael Erichsen
 * @version 29. sep. 2023
 *
 */

public class SkiftesagsLoader extends AbstractLoader {
	private static final String SELECT1 = "SELECT * FROM APP.SKIFTEREGISTER ORDER BY �R, EFTERNAVN, FORNAVN ";
	private static final String INSERT = "INSERT INTO VIDNE (INDIVIDID, ROLLE, INDIVIDBEGIVENHEDID) VALUES (?, ?, ?)";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			new ClearTables().clear();
			final int taeller = new SkiftesagsLoader().load();
			System.out.println("Har indl�st " + taeller + " skifter");
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return
	 * @throws SQLException
	 */
	public int load() throws SQLException {
		int individId = 0, relIndividId = 0, familieId = 0;
		int taeller = 0;
		String sekundaerNavn = "";
		String detaljer = "";
		IndividModel iModel, relIModel;
		PersonNavneModel pnModel, relPnModel;
		IndividBegivenhedModel ibModel;
		String bem = "";
		FamilieModel fModel;
		String relation = "";
		String erhverv = "";
		String relnavn = "";
		String relerhverv = "";
		int ibModelId = 0;
		String formatted = "";
		String dato = "";
		String primaerNavn = "";

		final Connection conn = connect("BLISTRUP");
		final KildeModel kModel = new KildeModel();
		kModel.setKbNr("Skiftesager");
		kModel.setAarInterval("1640-1833");
		final int kildeId = kModel.insert(conn);

		final PreparedStatement statements1 = conn.prepareStatement(SELECT1);
		final PreparedStatement statementi1 = conn.prepareStatement(INSERT);

		// Hent en linie
		// SELECT1 = "SELECT * FROM APP.SKIFTEREGISTER ORDER BY AAR, EFTERNAVN,
		// FORNAVN";

		final ResultSet rs = statements1.executeQuery();

		while (rs.next()) {
			erhverv = rs.getString("ERHVERV");
			relation = rs.getString("RELATION");
			relnavn = rs.getString("RELNAVN");
			relerhverv = rs.getString("RELERHV");
			relIModel = null;

			// Inds�t prim�r person

			iModel = new IndividModel();
			relIModel = new IndividModel();

			if (relation.startsWith("g.m") && !relnavn.isBlank()) {
				if (erhverv.isBlank()) {
					iModel.setKoen("F");
					relIModel.setKoen("M");
				} else {
					iModel.setKoen("M");
					relIModel.setKoen("F");
				}
			} else if ("enkemand".equals(relation) || "hs E".equals(relation)) {
				iModel.setKoen("M");
				relIModel.setKoen("F");
			} else if ("enke efter".equals(relation) || "hs H".equals(relation)) {
				iModel.setKoen("F");
				relIModel.setKoen("M");
			}

			individId = iModel.insert(conn);

			// Inds�t prim�r persons navn

			pnModel = new PersonNavneModel();
			pnModel.setIndividId(individId);
			primaerNavn = afQ(rs.getString("NAVN"));
			pnModel.setStdnavn(cleanName(primaerNavn));
			try {
				pnModel.setFonetiskNavn(fonkod.generateKey(primaerNavn));
			} catch (final Exception e) {
			}
			pnModel.setPrimaerNavn(true);
			pnModel.insert(conn);

			// Inds�t erhverv som individbegivenhed for prim�r person hvis ikke blank

			if (erhverv != null && !erhverv.isBlank()) {
				ibModel = new IndividBegivenhedModel();
				ibModel.setIndividId(individId);
				ibModel.setKildeId(kildeId);
				ibModel.setBegType("Erhverv");
				ibModel.setNote(afQ(erhverv));
				bem = afQ(rs.getString("BEM"));
				if (bem != null && !bem.isBlank()) {
					ibModel.setBem(bem);
				}
				ibModel.insert(conn);
			}

			// Inds�t sekund�r person hvis ikke relation blank og relnavn ikke blank

			if (!relnavn.isBlank()) {
				relIndividId = relIModel.insert(conn);

				// Inds�t sekund�r persons navn hvis ikke relation blank og relnavn ikke blank

				relPnModel = new PersonNavneModel();
				relPnModel.setIndividId(relIndividId);
				sekundaerNavn = afQ(rs.getString("RELNAVN"));
				relPnModel.setStdnavn(cleanName(sekundaerNavn));
				try {
					relPnModel.setFonetiskNavn(fonkod.generateKey(sekundaerNavn));
				} catch (final Exception e) {
				}
				relPnModel.setPrimaerNavn(true);
				relPnModel.insert(conn);

				// Inds�t erhverv som individbegivenhed for sekund�r person hvis ikke blank

				if (relerhverv != null && !relerhverv.isBlank()) {
					ibModel = new IndividBegivenhedModel();
					ibModel.setIndividId(relIndividId);
					ibModel.setKildeId(kildeId);
					ibModel.setBegType("Erhverv");
					ibModel.setNote(relerhverv);
					bem = afQ(rs.getString("BEM"));
					if (bem != null && !bem.isBlank()) {
						ibModel.setBem(bem);
					}
					ibModel.insert(conn);
				}

				// Inds�t familie

				fModel = new FamilieModel(conn);
				if (relation.startsWith("g.m")) {
					if (erhverv.isBlank()) {
						fModel.setFader(relIndividId);
						fModel.setModer(individId);
					} else {
						fModel.setFader(individId);
						fModel.setModer(relIndividId);
					}
				} else if ("fader".equals(relation) || "far".equals(relation)) {
					fModel.setFader(relIndividId);
				} else if ("enkemand".equals(relation) || "hs E".equals(relation)) {
					fModel.setFader(individId);
				} else if ("enke efter".equals(relation) || "hs H".equals(relation)) {
					fModel.setModer(individId);
				}
				familieId = fModel.insert();

				// Opdater FAMC for prim�r person hvis barn i familien

				if ("fader".equals(relation) || "far".equals(relation)) {
					iModel.updateFamc(conn, familieId);
				} else if (relIModel != null && ("fader".equals(relation) || "far".equals(relation))) {
					relIModel.updateFamc(conn, familieId);
				}
			}

			// Inds�t skifte som individbegivenhed for prim�r person

			ibModel = new IndividBegivenhedModel();
			ibModel.setIndividId(individId);
			ibModel.setKildeId(kildeId);
			ibModel.setBegType("Skifte");
			dato = rs.getString("DATO");
			if (dato != null && !dato.isBlank()) {
				formatted = String.format("%04d", Integer.parseInt(dato.trim()));
			} else {
				formatted = "0101";
			}
			formatted = rs.getString("�R") + "-" + formatted.substring(0, 2) + "-" + formatted.substring(2, 4);
			try {
				ibModel.setDato(Date.valueOf(formatted));
			} catch (final Exception e) {
				ibModel.setDato(Date.valueOf(rs.getString("�R") + "-01-01"));
			}
			ibModel.setStedNavn(rs.getString("STEDNAVN") + ", " + rs.getString("SOGN") + ", " + rs.getString("HERRED")
					+ ", Frederiksborg");
			detaljer = "Kilde " + rs.getString("KILDE").trim() + ", Opslag " + rs.getString("OPSLAG").trim() + "\r\n"
					+ getTableRow(rs);
			ibModel.setDetaljer(detaljer);
			ibModel.setNote("var n�vnt i skiftet efter " + primaerNavn);
			ibModelId = ibModel.insert(conn);

			// Inds�t sekund�r person som vidne
			// INSERT = "INSERT INTO VIDNE (INDIVIDID, ROLLE, INDIVIDBEGIVENHEDID)

			if (!relnavn.isBlank()) {
				statementi1.setInt(1, relIndividId);
				statementi1.setString(2, "Relation: " + relation);
				statementi1.setInt(3, ibModelId);
				statementi1.executeUpdate();
			}

			taeller++;

		}

		conn.commit();
		conn.close();
		return taeller;
	}
}
