package net.myerichsen.blistrup.mergers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import net.myerichsen.blistrup.loaders.AbstractLoader;

/**
 * Flet identiske navnevarianter med samme individid
 *
 * @author Michael Erichsen
 * @version 1. okt. 2023
 *
 */

public class NavneMerger extends AbstractLoader {
	private static final String SELECT1 = "SELECT * FROM INDIVID ORDER BY ID";
	private static final String SELECT2 = "SELECT * FROM PERSONNAVN WHERE INDIVIDID = ?";
	private static final String SELECTC = "SELECT COUNT(*) AS CT FROM PERSONNAVN";
	private static final String DELETE1 = "DELETE FROM PERSONNAVN WHERE INDIVIDID = ? AND STDNAVN = ?";

	private static PreparedStatement statements1;
	private static PreparedStatement statements2;
	private static PreparedStatement statementsc;
	private static PreparedStatement statementd1;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final NavneMerger merger = new NavneMerger();
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
		ResultSet rs2;
		ResultSet rsc;
		int individId = 0;
		String navn = "";
		Set<String> navne = new HashSet<String>();
		boolean addSucceeded = false;
		int count = 0;

		final Connection conn = connect("BLISTRUP");
		statements1 = conn.prepareStatement(SELECT1);
		statements2 = conn.prepareStatement(SELECT2);
		statementsc = conn.prepareStatement(SELECTC);
		statementd1 = conn.prepareStatement(DELETE1);

		// Count records
		rsc = statementsc.executeQuery();

		if (rsc.next()) {
			count = rsc.getInt("CT");
			System.out.println("Count: " + count);
		}

		// Hent alle individer ordnet efter individid
		// SELECTI = "SELECT * FROM INDIVID ORDER BY INDIVIDID";

		final ResultSet rs1 = statements1.executeQuery();

		while (rs1.next()) {
			navne = new HashSet<>();
			individId = rs1.getInt("ID");

			// For hvert individ
			// SELECT2 = "SELECT * FROM PERSONNAVN WHERE INDIVIDID = ?
			statements2.setInt(1, individId);
			rs2 = statements2.executeQuery();

			while (rs2.next()) {
				navn = rs2.getString("STDNAVN");
				addSucceeded = navne.add(navn);

				if (!addSucceeded) {
					// Slet dubletnavnet
					// DELETE1 = "DELETE FROM PERSONNAVN WHERE INDIVIDID = ? AND STDNAVN = ?";
					statementd1.setInt(1, individId);
					statementd1.setString(2, navn);
					statementd1.executeUpdate();
					System.out.println("Slettet id " + individId + ", " + navn);
				}
			}
		}

		// Count records
		rsc = statementsc.executeQuery();

		if (rsc.next())

		{
			System.out.println("Count before: " + count + ", after: " + rsc.getInt("CT"));
		}

		conn.commit();
		conn.close();
	}

}
