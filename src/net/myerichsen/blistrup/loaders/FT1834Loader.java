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
 * Load en FT 1834 tabel
 *
 * @author Michael Erichsen
 * @version 1. sep. 2023
 *
 */
public class FT1834Loader extends AbstractLoader {
	private static final String[] famcArray = { "Avlsbestyrer, deres B�rn", "B�rn", "deres Barn", "Deres B�rn",
			"deres B�rn Dagleier", "deres B�rn Skomager", "deres B�rn Skomager, Militair", "deres B�rn Skr�der",
			"deres B�rn Skr�derpige", "deres B�rn Snedker", "Deres B�rn, Hugger", "deres B�rn, Skr�der", "Deres Datter",
			"deres S�n", "deres S�n og Avlskarl", "hans B�rn", "hans Datter", "hans S�n", "hendes B�rn",
			"hendes B�rn Snedker", "hendes Datter", "hendes D�ttre", "hendes S�n", "hendes S�n, Millitair",
			"Inderste, Datter af Huusfader", "u�gte B�rn" };
	private static final long FIRST_DATE = -62135773200000L;
	private static final String SELECT1 = "SELECT * FROM FT1834";
	private static final String INSERT1 = "INSERT INTO BLISTRUP.VIDNE (INDIVIDID, ROLLE, FAMILIEBEGIVENHEDID) VALUES (?, ?, ?)";
	private static PreparedStatement statements1;
	private static PreparedStatement statementi1;
	private static boolean primary = true;
	private static int nrIHusstand = 0;

	/**
	 * @return the nrIHusstand
	 */
	public static int getNrIHusstand() {
		return nrIHusstand;
	}

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
			final int taeller = new FT1834Loader().load();
			System.out.println("Har indl�st " + taeller + " folket�llingslinier");
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param nrIHusstand the nrIHusstand to set
	 */
	public static void setNrIHusstand(int nrIHusstand) {
		FT1834Loader.nrIHusstand = nrIHusstand;
	}

	/**
	 * @param primary the primary to set
	 */
	public static void setPrimary(boolean primary) {
		FT1834Loader.primary = primary;
	}

	/**
	 * Inds�t et individ og returner de n�dvendige data
	 *
	 * @param conn
	 * @param rs
	 * @param kildeId
	 * @param familieId
	 * @param nrIHusstand
	 * @return
	 * @throws SQLException
	 */
	private IndividData insertIndividual(Connection conn, ResultSet rs, int kildeId, int familieId, int nrIHusstand)
			throws SQLException {

		/**
		 * Individ
		 */
		final IndividModel iModel = new IndividModel();
		iModel.setKoen(rs.getString("K�N").startsWith("M") ? "M" : "F");
		final String kildeErhverv = rs.getString("KILDEERHVERV");
		if (nrIHusstand == 0 && "Gift".equals(rs.getString("CIVILSTAND"))) {
			iModel.getFams().add(familieId);
			iModel.setPrimary(true);
		} else if (nrIHusstand == 0
				&& ("Enkemand".equals(rs.getString("CIVILSTAND")) || "Enke".equals(rs.getString("CIVILSTAND")))) {
			iModel.getFams().add(familieId);
			iModel.setPrimary(true);
			setPrimary(false);
		} else if (nrIHusstand == 1 && isPrimary()) {
			iModel.getFams().add(familieId);
			iModel.setPrimary(true);
			setPrimary(false);
		} else {
			for (final String string : famcArray) {
				if (string.equals(kildeErhverv) || kildeErhverv.startsWith(string + " ")) {
					iModel.setFamc(familieId);
					break;
				}
			}
		}
		try {
			iModel.setFoedt(Integer.toString(1834 - Integer.parseInt(rs.getString("ALDER"))));
		} catch (final Exception e1) {
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
		int nrIHusstand = 0;
		final Connection conn = connect("APP");
		final int kildeId = insertSource(conn, "1834");
		statements1 = conn.prepareStatement(SELECT1);
		statementi1 = conn.prepareStatement(INSERT1);
		final ResultSet rs = statements1.executeQuery();

		while (rs.next()) {
			/**
			 * N�ste husstand
			 */
			if (!matrNrAdresse.equals(rs.getString("MATR_NR__ADRESSE"))
					|| !hfNr.equals(rs.getString("HUSSTANDS_FAMILIENR_"))) {
				if (!matrNrAdresse.isBlank()) {
					/**
					 * Folket�lling (Familiebegivenhed)
					 */
					fbModel = new FamilieBegivenhedModel();
					fbModel.setFamilieId(familieId);
					fbModel.setBegType("Folket�lling");
					fbModel.setKildeId(kildeId);
					fbModel.setDato(Date.valueOf("1834-02-18"));
					fbModel.setStedNavn(matrNrAdresse + ",,,");
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

				nrIHusstand = 0;
				matrNrAdresse = rs.getString("MATR_NR__ADRESSE");
				hfNr = rs.getString("HUSSTANDS_FAMILIENR_");
				list = new ArrayList<>();
			}

			id = insertIndividual(conn, rs, kildeId, familieId, nrIHusstand);
			id.getStillingIHusstanden();

			if (id.getiModel().isPrimary()) {
				if ("M".equals(id.getiModel().getKoen())) {
					fModel.setFader(id.getId());
					fModel.updateFather();
				} else if ("F".equals(id.getiModel().getKoen())) {
					fModel.setModer(id.getId());
					fModel.updateMother();
				}
			}

			list.add(id);
			count++;
		}

		conn.commit();
		conn.close();
		return count;
	}
}
