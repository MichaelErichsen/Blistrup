package net.myerichsen.blistrup.mergers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.myerichsen.blistrup.loaders.AbstractLoader;

/**
 * Flet individer med samme SLGT i rådataene
 *
 * @author Michael Erichsen
 * @version 30. sep. 2023
 *
 */

public class SlgtMerger extends AbstractLoader {
	private static final String SELECTI = "SELECT * FROM INDIVID WHERE SLGT IS NOT NULL "
			+ "AND TRIM(SLGT) <> ''  AND SLGT NOT LIKE '?%' ORDER BY SLGT";
	private static final String SELECTC = "SELECT COUNT(*) AS CT FROM INDIVID";
	private static final String DELETEI = "DELETE FROM INDIVID WHERE ID = ?";
	private static final String UPDATEP = "UPDATE PERSONNAVN SET INDIVIDID = ?, PRIMAERNAVN = 'FALSE' WHERE INDIVIDID = ?";
	private static final String UPDATEB = "UPDATE INDIVIDBEGIVENHED SET INDIVIDID = ? WHERE INDIVIDID = ?";
	private static final String UPDATEV = "UPDATE VIDNE SET INDIVIDID = ? WHERE INDIVIDID = ?";
	private static final String UPDATEF = "UPDATE FAMILIE SET HUSFADER = ? WHERE HUSFADER = ?";
	private static final String UPDATEM = "UPDATE FAMILIE SET HUSMODER = ? WHERE HUSMODER = ?";
	private static PreparedStatement statementsi;
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
		final SlgtMerger merger = new SlgtMerger();
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
		ResultSet rsc;
		String slgt = "";
		String senesteSlgt = "";
		int individId = 0;
		int samleIndividId = 0;
		int count = 0;

		final Connection conn = connect("BLISTRUP");
		statementsi = conn.prepareStatement(SELECTI);
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

		// Find hvert slgt-registreret individ
		// SELECTI = "SELECT * FROM INDIVID WHERE SLGT IS NOT NULL AND
		// TRIM(SLGT) <> '' AND SLGT NOT LIKE '?%' ORDER BY SLGT"

		final ResultSet rsi = statementsi.executeQuery();

		while (rsi.next()) {
			slgt = rsi.getString("SLGT");

			// Søg efter dubletter til sammenlægning

			if (!slgt.equals(senesteSlgt)) {
				senesteSlgt = slgt;
				samleIndividId = 0;
				continue;
			}

			individId = rsi.getInt("ID");

			if (samleIndividId == 0) {
				samleIndividId = individId;
			}

			if (samleIndividId == individId) {
				continue;
			}

			// For hvert personnavn
			// UPDATEP = "UPDATE PERSONNAVN SET INDIVIDID = ?, PRIMAERNAVN =
			// 'FALSE' WHERE INDIVIDID = ?";
			statementup.setInt(1, samleIndividId);
			statementup.setInt(2, individId);
			statementup.executeUpdate();

			// For hver individbegivenhed
			// UPDATEB = "UPDATE INDIVIDBEGIVENHED SET INDIVIDID = ? WHERE
			// INDIVIDID = ?";
			statementub.setInt(1, samleIndividId);
			statementub.setInt(2, individId);
			statementub.executeUpdate();

			// For hvert vidne
			// UPDATEV = "UPDATE VIDNE SET INDIVIDID = ? WHERE INDIVIDID = ?";
			statementuv.setInt(1, samleIndividId);
			statementuv.setInt(2, individId);
			statementuv.executeUpdate();

			// For hver familie
			// UPDATEF = "UPDATE FAMILIE SET HUSFADER = ? WHERE HUSFADER = ?";
			statementuf.setInt(1, samleIndividId);
			statementuf.setInt(2, individId);
			statementuf.executeUpdate();

			// UPDATEM = "UPDATE FAMILIE SET HUSMODER = ? WHERE HUSMODER = ?";
			statementum.setInt(1, samleIndividId);
			statementum.setInt(2, individId);
			statementum.executeUpdate();

			// For hvert dubletindivid
			// DELETEI = "DELETE FROM INDIVID WHERE ID = ?";
			statementdi.setInt(1, individId);
			statementdi.executeUpdate();

			conn.commit();

		}

		// Count records
		rsc = statementsc.executeQuery();

		if (rsc.next()) {
			System.out.println("Count before: " + count + ", after: " + rsc.getInt("CT"));
		}
		conn.commit();
		conn.close();

	}

}
