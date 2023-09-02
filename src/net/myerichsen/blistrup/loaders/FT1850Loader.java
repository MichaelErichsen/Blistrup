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
 * Load en FT 1850 tabel
 *
 * @author Michael Erichsen
 * @version 2. sep. 2023
 *
 */
public class FT1850Loader extends AbstractLoader {
	private static final String[] famsArrayF = { "Hans Hustru", "Husmoder" };
	private static final String[] famcArrayM = { "Deres s�n, arbejdsmand", "Deres s�n, frav�rende",
			"Deres s�n, hj�lper faderen", "Deres s�n, skr�der", "Deres s�n, student", "Hans s�n", "Hendes s�n",
			"Hendes s�n, skr�derl�rling", "Skr�der, hendes s�n" };
	private static final String[] famcArrayF = { "Deres datter", "Hans datter", "Hendes datter" };
	private static final String[] famcArray = { "Deres barn", "Hans barn", "Hendes barn", "Hendes u�gte barn" };
	private static final long FIRST_DATE = -62135773200000L;
	private static final String SELECT1 = "SELECT * FROM FT1850";
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
			final int taeller = new FT1850Loader().load();
			System.out.println("Har indl�st " + taeller + " linier fra 1850 folket�llingen");
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param primary the primary to set
	 */
	public static void setPrimary(boolean primary) {
		FT1850Loader.primary = primary;
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
//	Kildeerhverv
//	Kildef�dested
//	Kildehenvisning

	private IndividData insertIndividual(Connection conn, ResultSet rs, int kildeId, int familieId)
			throws SQLException {
		boolean found = false;

		/**
		 * Individ
		 */
		final IndividModel iModel = new IndividModel();
		final String koen = rs.getString("K�N");
		iModel.setKoen("M".equals(koen) ? "M" : "F");

		final String kildeErhverv = rs.getString("KILDEERHVERV");

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
			iModel.setFoedt(Integer.toString(1850 - Integer.parseInt(rs.getString("ALDER"))));
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
		final int kildeId = insertSource(conn, "1850");
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
					fbModel.setDato(Date.valueOf("1850-02-01"));
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
