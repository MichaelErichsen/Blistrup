package net.myerichsen.blistrup.mergers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.myerichsen.blistrup.loaders.AbstractLoader;

/**
 * Flet individer med samme navn, samme FAM og ingen SLGT i rådataene
 *
 * @author Michael Erichsen
 * @version 6. okt. 2023
 *
 */
// FIXME Finder intet
public class FamMerger extends AbstractLoader {
	/**
	 * @author Michael Erichsen
	 * @version 5. okt. 2023
	 *
	 */
	private static class FamObject {
		int individId = 0;
		String navn = "";

		/**
		 * Constructor
		 *
		 * @param individId2
		 * @param navn2
		 */
		public FamObject(int individId2, String navn2) {
			individId = individId2;
			navn = navn2;
		}

		/**
		 * @return the individId
		 */
		public int getIndividId() {
			return individId;
		}

		/**
		 * @return the navn
		 */
		public String getNavn() {
			return navn;
		}

	}

	private static final String SELECTC = "SELECT COUNT(*) AS CT FROM INDIVID";
	private static final String SELECT01 = "SELECT DISTINCT FAM FROM INDIVID WHERE FAM IS NOT NULL AND TRIM(FAM) <> '' AND (SLGT IS NULL OR TRIM(SLGT) = '') ORDER BY FAM";
	private static final String SELECT02 = "SELECT ID FROM INDIVID WHERE FAM = ? AND (SLGT IS NULL OR TRIM(SLGT) = '')";
	private static final String SELECT03 = "SELECT STDNAVN FROM PERSONNAVN WHERE INDIVIDID = ?";
	private static final String DELETEI = "DELETE FROM INDIVID WHERE ID = ?";
	private static final String UPDATEP = "UPDATE PERSONNAVN SET INDIVIDID = ?, PRIMAERNAVN = 'FALSE' WHERE INDIVIDID = ?";
	private static final String UPDATEB = "UPDATE INDIVIDBEGIVENHED SET INDIVIDID = ? WHERE INDIVIDID = ?";
	private static final String UPDATEV = "UPDATE VIDNE SET INDIVIDID = ? WHERE INDIVIDID = ?";
	private static final String UPDATEF = "UPDATE FAMILIE SET HUSFADER = ? WHERE HUSFADER = ?";
	private static final String UPDATEM = "UPDATE FAMILIE SET HUSMODER = ? WHERE HUSMODER = ?";
	private static PreparedStatement statement01;
	private static PreparedStatement statement02;
	private static PreparedStatement statement03;
	private static PreparedStatement statementsc;
	private static PreparedStatement statementdi;
	private static PreparedStatement statementup;
	private static PreparedStatement statementub;
	private static PreparedStatement statementuv;
	private static PreparedStatement statementuf;
	private static PreparedStatement statementum;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final FamMerger merger = new FamMerger();
		try {
			merger.execute();
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Worker method
	 *
	 * @throws SQLException
	 */
	private void execute() throws SQLException {
		ResultSet rsc, rs01, rs02, rs03;
		int samleIndividId = 0;
		int individId = 0;
		int count = 0;
		List<FamObject> famObjectListe = new ArrayList<>();
		String navn = "";
		String fam = "";

		final Connection conn = connect("BLISTRUP");
		statement01 = conn.prepareStatement(SELECT01);
		statement02 = conn.prepareStatement(SELECT02);
		statement03 = conn.prepareStatement(SELECT03);
		statementsc = conn.prepareStatement(SELECTC);
		statementdi = conn.prepareStatement(DELETEI);
		statementup = conn.prepareStatement(UPDATEP);
		statementub = conn.prepareStatement(UPDATEB);
		statementuv = conn.prepareStatement(UPDATEV);
		statementuf = conn.prepareStatement(UPDATEF);
		statementum = conn.prepareStatement(UPDATEM);

		// Count records
		rsc = statementsc.executeQuery();

		if (rsc.next()) {
			count = rsc.getInt("CT");
			System.out.println("Count: " + count);
		}

		// Find alle tilfælde af FAM uden SLGT, da FAM med SLGT håndteres i et andet
		// program

		// SELECT DISTINCT FAM FROM INDIVID WHERE FAM IS NOT NULL AND TRIM(FAM) <> ''
		// AND (SLGT IS NULL OR TRIM(SLGT) = '') ORDER BY FAM

		rs01 = statement01.executeQuery();

		// For hver FAM

		while (rs01.next()) {
			fam = rs01.getString("FAM");
			samleIndividId = 0;

			// Find alle individer med denne FAM og ingen SLGT
			// SELECT ID FROM INDIVID WHERE FAM = ? AND (SLGT IS NULL OR TRIM(SLGT) = '')

			statement02.setString(1, fam);
			rs02 = statement02.executeQuery();

			// For hvert individ

			while (rs02.next()) {
				individId = rs02.getInt("ID");

				if (samleIndividId == 0) {
					samleIndividId = individId;
				}

				famObjectListe = new ArrayList<>();

				// Find tilhørende navn
				// SELECT STDNAVN FROM PERSONNAVN WHERE INDIVIDID = ?

				statement03.setInt(1, individId);
				rs03 = statement03.executeQuery();

				// Dan liste af objekter

				while (rs03.next()) {
					navn = rs03.getString("STDNAVN");
					famObjectListe.add(new FamObject(individId, navn));
				}

				// Fjern dubletter

				for (int i = 0; i < famObjectListe.size(); i++) {
					for (int j = i + 1; j < famObjectListe.size(); j++) {
						System.out.println("Fam " + fam + ":" + famObjectListe.get(i).getNavn().trim() + " -"
								+ famObjectListe.get(j).getNavn().trim());
						if (famObjectListe.get(i).getNavn().equals(famObjectListe.get(j).getNavn())) {

							final int a = famObjectListe.get(j).getIndividId();

							// Hvis der er mere end ét, samles de øvrige i det førstes individ

							flet(samleIndividId, a);

							conn.commit();

							System.out.println("Sammenlagt " + a + " til " + samleIndividId);
						}
					}
				}
			}
		}

		// Count records
		rsc = statementsc.executeQuery();

		if (rsc.next()) {
			System.out.println("Count before: " + count + ", after: " + rsc.getInt("CT"));
		}

		conn.commit();
		conn.close();

	}

	/**
	 * @param samleIndividId
	 * @param a
	 * @throws SQLException
	 */
	public void flet(int samleIndividId, int a) throws SQLException {
		// For hvert personnavn
		// UPDATEP = "UPDATE PERSONNAVN SET INDIVIDID = ?, PRIMAERNAVN =
		// 'FALSE' WHERE INDIVIDID = ?";
		statementup.setInt(1, samleIndividId);
		statementup.setInt(2, a);
		statementup.executeUpdate();

		// For hver individbegivenhed
		// UPDATEB = "UPDATE INDIVIDBEGIVENHED SET INDIVIDID = ? WHERE
		// INDIVIDID = ?";
		statementub.setInt(1, samleIndividId);
		statementub.setInt(2, a);
		statementub.executeUpdate();

		// For hvert vidne
		// UPDATEV = "UPDATE VIDNE SET INDIVIDID = ? WHERE INDIVIDID = ?";
		statementuv.setInt(1, samleIndividId);
		statementuv.setInt(2, a);
		statementuv.executeUpdate();

		// For hver familie
		// UPDATEF = "UPDATE FAMILIE SET HUSFADER = ? WHERE HUSFADER = ?";
		statementuf.setInt(1, samleIndividId);
		statementuf.setInt(2, a);
		statementuf.executeUpdate();

		// UPDATEM = "UPDATE FAMILIE SET HUSMODER = ? WHERE HUSMODER = ?";
		statementum.setInt(1, samleIndividId);
		statementum.setInt(2, a);
		statementum.executeUpdate();

		// For hvert dubletindivid
		// DELETEI = "DELETE FROM INDIVID WHERE ID = ?";
		statementdi.setInt(1, a);
		statementdi.executeUpdate();
	}

}
