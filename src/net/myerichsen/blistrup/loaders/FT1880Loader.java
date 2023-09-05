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
 * Load en FT 1880 tabel
 *
 * @author Michael Erichsen
 * @version 3. sep. 2023
 *
 */
public class FT1880Loader extends AbstractLoader {
	private static final String[] famsArrayF = { "Gaardejer Kone. Husmoder", "Hans Hustru", "Hans Kone",
			"Hans Kone, Nyder Fattigunderst�ttelse", "Husmoder og Enke", "Husmoder og Gaardejerinde",
			"Husmoder og Husejerinde", "Husmoder og Lever af sin Jordlod", "Husmoder, Fors�rges af Fattigv�senet",
			"Husmoder, Husejer", "Husmoder, Indsidder", "Husmoder, Kludehandler?",
			"Husmoder, Nyder Fattigunderst�ttelse", "Husmoder, S�n og Moder ern�rer sig tilsammen",
			"Husmoder, Underst�ttes af Fattigv�senet", "Hustru" };
	private static final String[] famcArrayM = { "Deres S�n", "En S�n", "Fors�rges af Husfaderen, Hans S�n",
			"Hendes S�n", "Hendes S�n og er Arbejdskarl ved Agerbruget", "Husfaderens Steds�n", "Husfaderens S�n",
			"Smedesvend og S�n", "S�n", "S�n, Barn", "S�n, Deres Barn", "S�n, S�n og Moder ern�rer sig tilsammen" };
	private static final String[] famcArrayF = { "Datter", "Datter, Barn", "Datter, Deres Barn",
			"Datter, Fors�rges af Fattigv�senet", "Datter, Husmoderens Barn", "Datter, Nyder Fattigunderst�ttelse",
			"Deres Barn, V�vepige", "Deres Datter", "Deres Datter, For�ldrene Behj�lpelige", "Hendes Datter",
			"Husfaderens Datter", "Husmoderens Datter", "L�rer H�egs Datter", "Sypige og Datter", "Sypige, Datter",
			"V�vepige og deres Datter" };
	private static final String[] famcArray = { "Barn", "Barn, Nyder Fattigunderst�ttelse", "Deres Barn",
			"Deres Barn, For�ldrene behj�lpelige", "Deres Barn, Hjulmand", "Deres Barn, Murer",
			"Deres Barn, Nyder Fattighj�lp", "Deres Barn, Snedker", "Deres Barn, Tjener Faderen", "Deres Barn, Urmager",
			"Fors�rges af sin Moder", "Hans Barn", "Hans Barn, Skomager", "Hans Barn, Sypige", "Hendes Barn",
			"Hendes Barn (u�gte)", "Hendes Barn, fors�rges af Fattigv�sent", "Husfaderens Barn", "Husfaderens Stedbarn",
			"Midlertidigt hos Moderen" };
	private static final long FIRST_DATE = -62135773200000L;
	private static final String SELECT1 = "SELECT * FROM FT1880";
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
			final int taeller = new FT1880Loader().load();
			System.out.println("Har indl�st " + taeller + " linier fra 1880 folket�llingen");
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param primary the primary to set
	 */
	public static void setPrimary(boolean primary) {
		FT1880Loader.primary = primary;
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
		boolean found = false;

		/**
		 * Individ
		 */
		final IndividModel iModel = new IndividModel();
		final String koen = rs.getString("K�N");
		iModel.setKoen("M".equals(koen) ? "M" : "F");

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
			iModel.setFoedt(Integer.toString(1880 - Integer.parseInt(rs.getString("ALDER"))));
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
		final String kildeErhverv = rs.getString("KILDEERHVERV");
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
		kModel.setKbNr("Folket�lling");
		kModel.setAarInterval("1880");
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
					fbModel.setDato(Date.valueOf("1880-02-01"));
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
