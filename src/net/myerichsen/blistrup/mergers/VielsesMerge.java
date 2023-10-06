package net.myerichsen.blistrup.mergers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.myerichsen.blistrup.loaders.AbstractLoader;

/**
 * Fjern familier med kun et enkelt medlem
 *
 * @author Michael Erichsen
 * @version 1. okt. 2023
 *
 */

public class VielsesMerge extends AbstractLoader {
	private static final String SELECT1 = "SELECT * FROM FAMILIE WHERE HUSFADER IS NULL AND HUSMODER IS NULL ";
	private static final String SELECT2 = "SELECT * FROM FAMILIE WHERE HUSFADER IS NULL OR HUSMODER IS NULL ";
	private static final String SELECT3 = "SELECT COUNT(*) AS CT FROM INDIVID WHERE FAMC = ?";
	private static final String SELECTC = "SELECT COUNT(*) AS CT FROM FAMILIE";
	private static final String UPDATE1 = "UPDATE INDIVID SET FAMC = NULL WHERE FAMC = ?";
	private static final String DELETE1 = "DELETE FROM FAMILIE WHERE ID = ?";

	private static PreparedStatement statements1;
	private static PreparedStatement statements2;
	private static PreparedStatement statements3;
	private static PreparedStatement statementsc;
	private static PreparedStatement statementu1;
	private static PreparedStatement statementd1;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final VielsesMerge merger = new VielsesMerge();
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
		ResultSet rs2, rs3;
		ResultSet rsc;
		int count = 0;
		int familieId = 0;

		final Connection conn = connect("BLISTRUP");
		statements1 = conn.prepareStatement(SELECT1);
		statements2 = conn.prepareStatement(SELECT2);
		statements3 = conn.prepareStatement(SELECT3);
		statementsc = conn.prepareStatement(SELECTC);
		statementu1 = conn.prepareStatement(UPDATE1);
		statementd1 = conn.prepareStatement(DELETE1);

		// Count records
		rsc = statementsc.executeQuery();

		if (rsc.next()) {
			count = rsc.getInt("CT");
			System.out.println("Count: " + count);
		}

		// Hent alle familier uden forældre
		// "SELECT ID FROM FAMILIE WHERE HUSFADER IS NULL AND HUSMODER IS NULL ";
		final ResultSet rs1 = statements1.executeQuery();

		while (rs1.next()) {
			// For alle familier uden forældre fjernes familien fra barnet
			// UPDATE1 = "UPDATE INDIVID SET FAMC TO NULL WHERE FAMC = ?";
			statementu1.setInt(1, rs1.getInt("ID"));
			statementu1.executeUpdate();
			System.out.println("Opdateret barn fra " + familieId);

			// Slet den tomme familie
			// DELETE1 = "DELETE FROM FAMILIE WHERE ID = ?";
			statementd1.setInt(1, familieId);
			statementd1.executeUpdate();
//			System.out.println("Slettet familie " + familieId + " uden forældre");
		}

		// Hent alle familier med højst en forælder
		// SELECT2 = "SELECT ID FROM FAMILIE WHERE HUSFADER IS NULL OR HUSMODER IS NULL

		rs2 = statements2.executeQuery();

		while (rs2.next()) {
			familieId = rs2.getInt("ID");

			// For hver familie med kun en forælder find antallet af børn
			// SELECT3 = "SELECT COUNT(*) AS CT FROM INDIVID WHERE FAMC = ?";
			statements3.setInt(1, familieId);
			rs3 = statements3.executeQuery();

			if (rs3.next()) {
				if (rs3.getInt("CT") == 0) {
					// Hvis der heller ikke er børn slettes familien
					// DELETE1 = "DELETE FROM FAMILIE WHERE ID = ?";
					statementd1.setInt(1, familieId);
					statementd1.executeUpdate();
					System.out.println(
							"Slettet familie " + familieId + " med en forælder og " + rs3.getInt("CT") + " børn");
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

}
