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
import net.myerichsen.blistrup.models.PersonNavneModel;

/**
 * Load en FT 1860 tabel
 *
 * @author Michael Erichsen
 * @version 2. sep. 2023
 *
 */
public class FT1860Loader extends AbstractLoader {
	private static final String[] famsArrayF = { "Hans Kone", "Husmoder. Lever af lodden",
			"Husmoder. Lever af sin Jord", "Husmoder. Lever af sin jordlod" };
	private static final String[] famcArrayM = { "Deres S�n", "Hans S�n", "Hendes Steds�n. Snedker", "Hendes S�n",
			"Hendes S�n. Daglejer", "Hendes S�n. Tr�skomand" };
	private static final String[] famcArrayF = { "Deres Datter", "Deres Datter. V�verpige", "Hans Datter",
			"Hendes Datter", "Hendes Datter. Sypige", "Hendes Datter. V�verske" };
	private static final String[] famcArray = { "Deres Barn", "Deres Barn. Skomager", "Deres Barn. Stenhugger",
			"Deres Barn. Sypige", "Deres Barn. V�verske", "Gaardejers Stedbarn", "Hans Barn", "Hans Barn. Skr�dder",
			"Hendes Barn", "Hendes Barn. Daglejer", "Hendes Barn. V�verske", "Husfaders Stedbarn" };
	private static final long FIRST_DATE = -62135773200000L;
	private static final String SELECT1 = "SELECT * FROM FT1860";
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
			final int taeller = new FT1860Loader().load();
			System.out.println("Har indl�st " + taeller + " linier fra 1860 folket�llingen");
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param primary the primary to set
	 */
	public static void setPrimary(boolean primary) {
		FT1860Loader.primary = primary;
	}

	/**
	 * Inds�t et individ og returner de n�dvendige data
	 *
	 * @param conn
	 * @param rs
	 * @param kildeId
	 * @param familieId
	 * @return
	 * @throws SQLException
	 */

//	Kildestednavn
//	Husstands/familienr.
//	Matr.nr./Adresse
//	Kildenavn
//	K�n
//	Alder
//	Civilstand
//	Kildeerhverv (2) (Hvis (1) blank
//	NB Stilling_i_husstanden (1)
//	Kildef�dested
//	Kildekommentar

//	Kildestednavn
//	Husstands/familienr.
//	Matr.nr./Adresse
//	Kildenavn
//	K�n
//	Alder
//	Civilstand
//	Stilling_i_husstanden
//	Kildef�dested

	private IndividData insertIndividual(Connection conn, ResultSet rs, int kildeId, int familieId)
			throws SQLException {
		boolean found = false;

		/**
		 * Individ
		 */
		final IndividModel iModel = new IndividModel();
		final String koen = rs.getString("K�N");
		iModel.setKoen("Mand".equals(koen) ? "M" : "F");

		final String stillingIHusstanden = rs.getString("STILLING_I_HUSSTANDEN");

		if (nrIHusstand == 0 || nrIHusstand == 1) {
			for (final String string : famsArrayF) {
				if (string.equals(stillingIHusstanden) || stillingIHusstanden.startsWith(string + " ")) {
					break;
				}
			}
		} else {
			for (final String string : famcArrayM) {
				if (string.equals(stillingIHusstanden) || stillingIHusstanden.startsWith(string + " ")) {
					iModel.setFamc(familieId);
					found = true;
					break;
				}
			}
			if (!found) {
				for (final String string : famcArrayF) {
					if (string.equals(stillingIHusstanden) || stillingIHusstanden.startsWith(string + " ")) {
						iModel.setFamc(familieId);
						break;
					}
				}
			}
			if (!found) {
				for (final String string : famcArray) {
					if (string.equals(stillingIHusstanden) || stillingIHusstanden.startsWith(string + " ")) {
						iModel.setFamc(familieId);
						break;
					}
				}
			}
		}

		try {
			iModel.setFoedt(Integer.toString(1860 - Integer.parseInt(rs.getString("ALDER"))));
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
		ibModel.setNote(stillingIHusstanden);
		ibModel.insert(conn);

		final IndividData id = new IndividData(individId, stillingIHusstanden, getTableRow(rs), iModel);
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
		final int kildeId = insertSource(conn, "1860");
		statements1 = conn.prepareStatement(SELECT1);
		statementi1 = conn.prepareStatement(INSERT1);
		final ResultSet rs = statements1.executeQuery();

		while (rs.next()) {
			/**
			 * N�ste husstand
			 */
			if (!kildeStedNavn.equals(rs.getString("KILDESTEDNAVN"))
					|| !matrNrAdresse.equals(rs.getString("MATR_NR__ADRESSE"))
					|| !hfNr.equals(rs.getString("HUSSTANDS_FAMILIENR_"))) {
				if (!matrNrAdresse.isBlank()) {
					/**
					 * Folket�lling (Familiebegivenhed)
					 */
					fbModel = new FamilieBegivenhedModel();
					fbModel.setFamilieId(familieId);
					fbModel.setBegType("Folket�lling");
					fbModel.setKildeId(kildeId);
					fbModel.setDato(Date.valueOf("1860-02-01"));
					fbModel.setStedNavn(matrNrAdresse + "," + kildeStedNavn + ",,,");
					sb = new StringBuilder();
					for (int i = 0; i < list.size() - 1; i++) {
						sb.append(list.get(i).getDetaljer() + "\r\n");
					}
					sb.append(list.get(list.size() - 1).getDetaljer());
					fbModel.setDetaljer(sb.toString());
					ftId = fbModel.insert(conn);

					/**
					 * Vidner til folket�lling
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