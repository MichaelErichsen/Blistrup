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
 * Load en FT 1787 tabel
 *
 * @author Michael Erichsen
 * @version 31. aug. 2023
 *
 */
public class FT1787Loader extends AbstractLoader {
	private static final long FIRST_DATE = -62135773200000L;
	private static final String SELECT1 = "SELECT * FROM FT1787";
	private static final String INSERT1 = "INSERT INTO BLISTRUP.VIDNE (INDIVIDID, ROLLE, FAMILIEBEGIVENHEDID) VALUES (?, ?, ?)";
	private static PreparedStatement statements1;
	private static PreparedStatement statementi1;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			final int taeller = new FT1787Loader().load();
			System.out.println("Har indlæst " + taeller + " folketællingslinier");
		} catch (final SQLException e) {
			e.printStackTrace();
		}
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
		/**
		 * Individ
		 */
		final IndividModel iModel = new IndividModel();
		final String stillingIHusstanden = rs.getString("STILLING_I_HUSSTANDEN");

		if ("Manden".equals(stillingIHusstanden) || stillingIHusstanden.startsWith("Manden ")) {
			iModel.setKoen("M");
			iModel.getFams().add(familieId);
		} else if ("Konen".equals(stillingIHusstanden) || stillingIHusstanden.startsWith("Konen ")) {
			iModel.setKoen("F");
			iModel.getFams().add(familieId);
		} else if ("Barn".equals(stillingIHusstanden) || stillingIHusstanden.startsWith("Barn ")) {
			iModel.setKoen("?");
			iModel.setFamc(familieId);
		} else if ("Søn".equals(stillingIHusstanden) || stillingIHusstanden.startsWith("Søn ")) {
			iModel.setKoen("M");
			iModel.setFamc(familieId);
		} else if ("Datter".equals(stillingIHusstanden) || stillingIHusstanden.startsWith("Datter ")) {
			iModel.setKoen("F");
			iModel.setFamc(familieId);
		} else if (stillingIHusstanden.toLowerCase().contains("dreng")) {
			iModel.setKoen("M");
		} else if (stillingIHusstanden.toLowerCase().contains("pige")) {
			iModel.setKoen("F");
		}

		iModel.setFoedt(Integer.toString(1787 - Integer.parseInt(rs.getString("ALDER"))));
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
		ibModel.setNote(rs.getString("KILDEERHVERV"));
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
		String hfNr = "";
		FamilieModel fModel = null;
		int familieId = 0;
		FamilieBegivenhedModel fbModel;
		IndividData id;
		StringBuilder sb;
		String stillingIHusstanden = "";
		List<IndividData> list = new ArrayList<>();
		int ftId = 0;
		int individId = 0;

		final Connection conn = connect("APP");
		final int kildeId = insertSource(conn, "1787");
		statements1 = conn.prepareStatement(SELECT1);
		statementi1 = conn.prepareStatement(INSERT1);
		final ResultSet rs = statements1.executeQuery();

		while (rs.next()) {
			/**
			 * Næste husstand
			 */
			if (!kildeStedNavn.equals(rs.getString("KILDESTEDNAVN"))
					|| !hfNr.equals(rs.getString("HUSSTANDS_FAMILIENR_"))) {
				if (!kildeStedNavn.isBlank()) {
					/**
					 * Folketælling (Familiebegivenhed)
					 */
					fbModel = new FamilieBegivenhedModel();
					fbModel.setFamilieId(familieId);
					fbModel.setBegType("Folketælling");
					fbModel.setKildeId(kildeId);
					fbModel.setDato(Date.valueOf("1787-07-01"));
					fbModel.setStedNavn(rs.getString("KILDESTEDNAVN") + ",,,");
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
					for (IndividData individData : list) {
						individId = individData.getId();

						if (individId == fModel.getFader() || individId == fModel.getModer()) {
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

				kildeStedNavn = rs.getString("KILDESTEDNAVN");
				hfNr = rs.getString("HUSSTANDS_FAMILIENR_");
				list = new ArrayList<>();
			}

			id = insertIndividual(conn, rs, kildeId, familieId);
			stillingIHusstanden = id.getStillingIHusstanden();

			if ("Manden".equals(stillingIHusstanden) || stillingIHusstanden.startsWith("Manden ")) {
				fModel.setFader(id.getId());
				fModel.updateFather();
			} else if ("Konen".equals(stillingIHusstanden) || id.getStillingIHusstanden().startsWith("Konen ")) {
				fModel.setModer(id.getId());
				fModel.updateMother();
			}

			list.add(id);
			count++;
		}

		conn.commit();
		conn.close();
		return count;
	}
}
