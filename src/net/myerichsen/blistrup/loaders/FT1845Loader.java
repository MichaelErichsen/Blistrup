package net.myerichsen.blistrup.loaders;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.myerichsen.blistrup.models.FamilieBegivenhedModel;
import net.myerichsen.blistrup.models.FamilieModel;
import net.myerichsen.blistrup.models.IndividBegivenhedModel;
import net.myerichsen.blistrup.models.IndividModel;
import net.myerichsen.blistrup.models.KildeModel;
import net.myerichsen.blistrup.models.PersonNavneModel;

/**
 * Load en FT 1845 tabel
 *
 * @author Michael Erichsen
 * @version 2. sep. 2023
 *
 */
public class FT1845Loader extends AbstractLoader {
	private static final String[] famsArrayF = { "Gaardmands Enke", "Gaardeierske", "hans Kone",
			"Huusmands Enke og Arbeidskone", "Huusmands Enke og Dagleierske", "Huusmands Enke, lever af sin Jordlod" };
	private static final String[] famcArrayM = { "deres Søn", "deres Søn, som driver Skomager Profession",
			"deres Sønner", "hans Søn", "hans Søn, Arbeidsmand", "hans Søn, Avlsbestyrer", "hans Søn, Skræder",
			"driver Hjulmand-Profession, deres Børn", "ernærer sig som Skræder, deres Børn", "hans Sønner",
			"hendes Søn", "hendes Søn, Arbeidsmand", "hendes Sønner", "bestyrer Parcellen, deres Børn",
			"hendes Børn, ernærer sig som Snedker" };
	private static final String[] famcArrayF = { "Datter og Huusholderske", "deres Datter", "deres Døttre",
			"hans Datter", "hendes Datter" };
	private static final String[] famcArray = { "deres Barn", "deres Børn", "disse ovennævnte ere hendes Børn",
			"hans Børn", "hendes Børn" };
	private static final long FIRST_DATE = -62135773200000L;
	private static final String SELECT1 = "SELECT * FROM FT1845";
	private static final String INSERT1 = "INSERT INTO BLISTRUP.VIDNE (INDIVIDID, ROLLE, FAMILIEBEGIVENHEDID) VALUES (?, ?, ?)";
	private static PreparedStatement statements1;
	private static PreparedStatement statementi1;
	private static boolean primary = true;
	private static int nrIHusstand = 0;

	/**
	 * @return the primary
	 */
	public static boolean isPrimary() {
		return primary;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			final int taeller = new FT1845Loader().load();
			System.out.println("Har indlæst " + taeller + " folketællingslinier");
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param primary the primary to set
	 */
	public static void setPrimary(boolean primary) {
		FT1845Loader.primary = primary;
	}

	/**
	 * Indsæt et individ og returner de nødvendige data
	 *
	 * @param conn
	 * @param rs
	 * @param kildeId
	 * @param familieId
	 * @return
	 * @throws SQLException
	 */

	private IndividData insertIndividual(Connection conn, ResultSet rs, int kildeId, int familieId)
			throws SQLException {
		boolean found = false;

		/**
		 * Individ
		 */
		final IndividModel iModel = new IndividModel();

		final String kildeErhverv = rs.getString("KILDEERHVERV");
		final String koen = rs.getString("KØN");
		iModel.setKoen("M".equals(koen) ? "M" : "F");

		if (nrIHusstand == 0 || nrIHusstand == 1) {
			for (final String string : famsArrayF) {
				if (string.equals(kildeErhverv) || kildeErhverv.startsWith(string + " ")) {
					break;
				}
			}
		} else {
			for (final String string : famcArrayM) {
				if (string.equals(kildeErhverv) || kildeErhverv.startsWith(string + " ")) {
					iModel.setFamc(familieId);
					found = true;
					break;
				}
			}
			if (!found) {
				for (final String string : famcArrayF) {
					if (string.equals(kildeErhverv) || kildeErhverv.startsWith(string + " ")) {
						iModel.setFamc(familieId);
						break;
					}
				}
			}
			if (!found) {
				for (final String string : famcArray) {
					if (string.equals(kildeErhverv) || kildeErhverv.startsWith(string + " ")) {
						iModel.setFamc(familieId);
						break;
					}
				}
			}
		}

		try {
			iModel.setFoedt(Integer.toString(1845 - Integer.parseInt(rs.getString("ALDER"))));
		} catch (final Exception e1) {
		}

		if (nrIHusstand == 0 && "Gift".equals(rs.getString("CIVILSTAND"))) {
			iModel.getFams().add(familieId);
			iModel.setPrimary(true);

		} else if (nrIHusstand == 0 || nrIHusstand == 1 && isPrimary()) {
			iModel.getFams().add(familieId);
			iModel.setPrimary(true);
		}

		final int individId = iModel.insert(conn);
		iModel.setId(individId);

		/**
		 * Individ navn
		 */
		final PersonNavneModel pModel = new PersonNavneModel();
		pModel.setIndividId(individId);
		String stdnavn = rs.getString("KILDENAVN");
		try {
			pModel.setFonetiskNavn(fonkod.generateKey(stdnavn));
		} catch (final Exception e) {
		}
		final String[] parts = stdnavn.split(" ");
		final String efternavn = parts[parts.length - 1];
		pModel.setFornavn(stdnavn.replace(efternavn, "").trim());
		pModel.setEfternavn(efternavn);
		stdnavn = stdnavn.replace(efternavn, "/" + efternavn + "/");
		pModel.setStdnavn(stdnavn);
		pModel.insert(conn);

		/**
		 * Erhverv
		 */
		final IndividBegivenhedModel ibModel = new IndividBegivenhedModel();
		ibModel.setIndividId(individId);
		ibModel.setKildeId(kildeId);
		ibModel.setBegType("Erhverv");
		ibModel.setNote(kildeErhverv);
		ibModel.insert(conn);

		final IndividData id = new IndividData(individId, kildeErhverv, getTableRow(rs), iModel);
		return id;
	}

	/**
	 * Load tabeller
	 *
	 * @return
	 * @throws SQLException
	 */
	private int load() throws SQLException {
		int count = 0;
		String kildeStedNavn = "";
		String matrNrAdresse = "";
		String hfNr = "";
		FamilieModel fModel = null;
		int familieId = 0;
		FamilieBegivenhedModel fbModel;
		IndividData id;
		StringBuilder sb;
		List<IndividData> list = new ArrayList<>();
		int ftId = 0;
		int individId = 0;
		final Connection conn = connect("APP");
		final KildeModel kModel = new KildeModel();
		kModel.setKbNr("Folketælling");
		kModel.setAarInterval("1845");
		final int kildeId = kModel.insert(conn);
		statements1 = conn.prepareStatement(SELECT1);
		statementi1 = conn.prepareStatement(INSERT1);
		final ResultSet rs = statements1.executeQuery();

		while (rs.next()) {
			/**
			 * Næste husstand
			 */
			if (!kildeStedNavn.equals(rs.getString("KILDESTEDNAVN"))
					|| !matrNrAdresse.equals(rs.getString("MATR_NR__ADRESSE"))
					|| !hfNr.equals(rs.getString("HUSSTANDS_FAMILIENR_"))) {
				if (!matrNrAdresse.isBlank()) {
					/**
					 * Folketælling (Familiebegivenhed)
					 */
					fbModel = new FamilieBegivenhedModel();
					fbModel.setFamilieId(familieId);
					fbModel.setBegType("Folketælling");
					fbModel.setKildeId(kildeId);
					fbModel.setDato(Date.valueOf("1845-02-01"));
					fbModel.setStedNavn(matrNrAdresse + "," + kildeStedNavn + ",,,");
					sb = new StringBuilder();
					for (int i = 0; i < list.size() - 1; i++) {
						sb.append(list.get(i).getDetaljer() + "\r\n");
					}
					sb.append(list.get(list.size() - 1).getDetaljer());
					fbModel.setDetaljer(sb.toString());
					ftId = fbModel.insert(conn);

					/**
					 * Vidner til folketælling
					 */
					for (final IndividData individData : list) {
						individId = individData.getId();

						if (individData.getiModel().isPrimary()) {
							continue;
						}

						statementi1.setInt(1, individId);
						statementi1.setString(2, individData.getStillingIHusstanden());
						statementi1.setInt(3, ftId);
						statementi1.executeUpdate();
					}
				}

				/**
				 * Familie
				 */
				fModel = new FamilieModel(conn);
				familieId = fModel.insert();
				fModel.setId(familieId);

				/**
				 * Vielse (Familiebegivenhed)
				 */
				fbModel = new FamilieBegivenhedModel();
				fbModel.setFamilieId(familieId);
				fbModel.setBegType("Vielse");
				fbModel.setDato(new Date(FIRST_DATE));
				fbModel.setKildeId(kildeId);
				fbModel.insert(conn);

				/**
				 * Reset for next family
				 */
				setPrimary(true);
				nrIHusstand = 0;
				kildeStedNavn = rs.getString("KILDESTEDNAVN");
				matrNrAdresse = rs.getString("MATR_NR__ADRESSE");
				hfNr = rs.getString("HUSSTANDS_FAMILIENR_");
				list = new ArrayList<>();
			}

			id = insertIndividual(conn, rs, kildeId, familieId);

			if (isPrimary()) {
				if ("M".equals(id.getiModel().getKoen())) {
					fModel.setFader(id.getId());
					fModel.updateFather();
				} else if ("F".equals(id.getiModel().getKoen())) {
					fModel.setModer(id.getId());
					fModel.updateMother();
					setPrimary(false);
				}
			}

			list.add(id);
			nrIHusstand++;
			count++;
		}

		conn.commit();
		conn.close();
		return count;
	}
}
