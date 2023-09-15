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
 * Load en FT 1840 tabel
 *
 * @author Michael Erichsen
 * @version 15. sep. 2023
 *
 */
public class FT1840Loader extends AbstractLoader {
	private static final String[] famsArrayF = { "hans Kone", "Huuskone,	lever af sin Jordlod",
			"Huuskone, lever af Spind", "Huusmands Enke", "Huusmands Enke, lever	af sin Jordlod",
			"Huusmands-Enke, lever af sin Jordlod" };
	private static final String[] famcArrayM = { "deres S�n", "deres S�n, Skr�der", "deres S�nner",
			"Enkens S�n, Arbeidsmand", "Enkens S�nner", "hans S�n", "hans S�nner", "hendes S�n", "Konens S�n",
			"Mandens S�nner" };
	private static final String[] famcArrayF = { "deres Datter", "Deres D�ttre", "Enkens D�ttre" };
	private static final String[] famcArray = { "B�rn", "B�rn der fors�rges af Gaarden",
			"B�rn der fors�rges af Gaarden, Hjulmand", "deres Barn", "Deres B�rn", "Deres B�rnparcellist",
			"Dyrker Jorden hos For�ldrene, deres Barn", "hendes Barn", "hendes B�rn", "Skr�der, deres Barn",
			"Snedker, hendes Barn", "V�verske, deres Barn" };
	private static final long FIRST_DATE = -62135773200000L;
	private static final String SELECT1 = "SELECT * FROM FT1840";
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
			final int taeller = new FT1840Loader().load();
			System.out.println("Har indl�st " + taeller + " folket�llingslinier");
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param primary the primary to set
	 */
	public static void setPrimary(boolean primary) {
		FT1840Loader.primary = primary;
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
	private IndividData insertIndividual(Connection conn, ResultSet rs, int kildeId, int familieId)
			throws SQLException {
		String koen = "M";
		boolean found = false;

		/**
		 * Individ
		 */
		final IndividModel iModel = new IndividModel();

		final String kildeErhverv = rs.getString("KILDEERHVERV");

		if (nrIHusstand == 0 || nrIHusstand == 1) {
			for (final String string : famsArrayF) {
				if (string.equals(kildeErhverv) || kildeErhverv.startsWith(string + " ")) {
					koen = "F";
					break;
				}
			}
			iModel.setKoen(koen);
		} else {
			for (final String string : famcArrayM) {
				if (string.equals(kildeErhverv) || kildeErhverv.startsWith(string + " ")) {
					iModel.setKoen("M");
					iModel.setFamc(familieId);
					found = true;
					break;
				}
			}
			if (!found) {
				for (final String string : famcArrayF) {
					if (string.equals(kildeErhverv) || kildeErhverv.startsWith(string + " ")) {
						iModel.setKoen("F");
						iModel.setFamc(familieId);
						break;
					}
				}
			}
			if (!found) {
				for (final String string : famcArray) {
					if (string.equals(kildeErhverv) || kildeErhverv.startsWith(string + " ")) {
						iModel.setKoen("?");
						iModel.setFamc(familieId);
						break;
					}
				}
			}
			if (!found) {
				iModel.setKoen("?");
			}
		}

		try {
			iModel.setFoedt(Integer.toString(1840 - Integer.parseInt(rs.getString("ALDER"))));
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
	public int load() throws SQLException {
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
		kModel.setKbNr("Folket�lling");
		kModel.setAarInterval("1840");
		final int kildeId = kModel.insert(conn);
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
					fbModel.setDato(Date.valueOf("1840-02-01"));
					fbModel.setStedNavn(fixStedNavn(matrNrAdresse + "," + kildeStedNavn));
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
