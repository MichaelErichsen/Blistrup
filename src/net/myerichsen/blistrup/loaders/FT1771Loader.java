package net.myerichsen.blistrup.loaders;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.myerichsen.blistrup.models.FamilieBegivenhedModel;
import net.myerichsen.blistrup.models.FamilieModel;
import net.myerichsen.blistrup.models.IndividBegivenhedModel;
import net.myerichsen.blistrup.models.IndividModel;
import net.myerichsen.blistrup.models.KildeModel;
import net.myerichsen.blistrup.models.PersonNavneModel;

/**
 * Load en FT 1771 tabel
 *
 * @author Michael Erichsen
 * @version 5. sep. 2023
 *
 */
public class FT1771Loader extends AbstractLoader {
	private static final long FIRST_DATE = -62135773200000L;
	private static final String SELECT1 = "SELECT * FROM FT1771";
	private static PreparedStatement statements1;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			final int taeller = new FT1771Loader().load();
			System.out.println("Har indlæst " + taeller + " folketællingslinier");
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Load tabeller
	 *
	 * @param args
	 * @return
	 * @throws SQLException
	 */
	private int load() throws SQLException {
		int count = 0;
		String stdnavn = "";
		IndividModel iModel;
		int foedt = 0;
		int doed = 0;
		String[] decimaltal;
		IndividBegivenhedModel ibModel;
		PersonNavneModel pModel;
		int faderId;
		String[] parts;
		String efternavn;
		int moderId = 0;
		String streng = "";
		FamilieModel fModel;
		int familieId = 0;
		FamilieBegivenhedModel fbModel;

		final Connection conn = connect("APP");

		final KildeModel kModel = new KildeModel();
		kModel.setKbNr("Folketælling");
		kModel.setAarInterval("1771");
		final int kildeId = kModel.insert(conn);

		statements1 = conn.prepareStatement(SELECT1);
		final ResultSet rs = statements1.executeQuery();

		while (rs.next()) {
			/**
			 * Husfader
			 */
			iModel = new IndividModel();
			iModel.setKoen("M");
			if ("Ja".equals(rs.getString("MAND_DØD__JA_NEJ_"))) {
				if (!rs.getString("ENKESTAND_ANTAL_ÅR").isBlank()) {
					decimaltal = rs.getString("ENKESTAND_ANTAL_ÅR").split(",");
					doed = 1771 - Integer.parseInt(decimaltal[0]);
					iModel.setDoed(Integer.toString(doed));
				} else {
					iModel.setDoed("BEF 1771");
				}
			} else {
				foedt = 1771 - Integer.parseInt(rs.getString("MANDENS_ALDER"));
				iModel.setFoedt(Integer.toString(foedt));
			}
			faderId = iModel.insert(conn);

			/**
			 * Faders navn
			 */
			pModel = new PersonNavneModel();
			pModel.setIndividId(faderId);
			stdnavn = rs.getString("MANDENS_NAVN");
			try {
				pModel.setFonetiskNavn(fonkod.generateKey(stdnavn));
			} catch (final Exception e) {
			}
			parts = stdnavn.split(" ");
			efternavn = parts[parts.length - 1];
			pModel.setFornavn(stdnavn.replace(efternavn, "").trim());
			pModel.setEfternavn(efternavn);
			stdnavn = stdnavn.replace(efternavn, "/" + efternavn + "/");
			pModel.setStdnavn(stdnavn);
			pModel.insert(conn);

			/**
			 * Erhverv
			 */
			ibModel = new IndividBegivenhedModel();
			ibModel.setIndividId(faderId);
			ibModel.setKildeId(kildeId);
			ibModel.setBegType("Erhverv");
			ibModel.setNote(rs.getString("MANDENS_ERHVERV"));
			ibModel.insert(conn);

			/**
			 * Husmoder
			 */
			iModel = new IndividModel();
			iModel.setKoen("F");
			streng = rs.getString("HUSTRUS_ALDER");
			if (!streng.isBlank()) {
				foedt = 1771 - Integer.parseInt(streng);
				iModel.setFoedt(Integer.toString(foedt));
			}
			if ("Ja".equals(rs.getString("HUSTRU_DØD__JA_NEJ_"))) {
				iModel.setDoed("BEF 1771");
			}
			moderId = iModel.insert(conn);

			/**
			 * Moders navn
			 */
			pModel = new PersonNavneModel();
			pModel.setIndividId(moderId);
			pModel.setStdnavn("? /?/");
			pModel.setFornavn("?");
			pModel.setEfternavn("?");
			pModel.insert(conn);

			/**
			 * Hustru erhverv (hvis hustrus forsørgelse ikke er tom)
			 */
			streng = rs.getString("HUSTRUS_FORSØRGELSE");
			if (!streng.isBlank()) {
				ibModel = new IndividBegivenhedModel();
				ibModel.setIndividId(moderId);
				ibModel.setKildeId(kildeId);
				ibModel.setBegType("Erhverv");
				ibModel.setNote(streng);
				ibModel.insert(conn);
			}

			/**
			 * Familie
			 */
			fModel = new FamilieModel(conn);
			fModel.setFader(faderId);
			fModel.setModer(moderId);
			familieId = fModel.insert();

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
			 * Folketælling (Familiebegivenhed)
			 */
			fbModel = new FamilieBegivenhedModel();
			fbModel.setFamilieId(familieId);
			fbModel.setBegType("Folketælling");
			fbModel.setKildeId(kildeId);
			fbModel.setDato(Date.valueOf("1771-02-01"));
			fbModel.setStedNavn("Blistrup,,,");
			fbModel.setDetaljer(get1771TableRow(rs));
			fbModel.insert(conn);

			count++;
		}

		conn.commit();
		conn.close();
		return count;
	}
}
