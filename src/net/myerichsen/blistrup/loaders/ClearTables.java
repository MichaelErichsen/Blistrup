package net.myerichsen.blistrup.loaders;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Ryd alle tabeller
 *
 * @author Michael Erichsen
 * @version 1. sep. 2023
 *
 */
public class ClearTables {
	private static final String SET_SCHEMA = "SET SCHEMA = 'BLISTRUP'";
	private static final String DELETE1 = "DELETE FROM INDIVID";
	private static final String DELETE2 = "DELETE FROM PERSONNAVN";
	private static final String DELETE3 = "DELETE FROM INDIVIDBEGIVENHED";
	private static final String DELETE4 = "DELETE FROM VIDNE";
	private static final String DELETE5 = "DELETE FROM KILDE";
	private static final String DELETE6 = "DELETE FROM FAMILIE";
	private static final String DELETE7 = "DELETE FROM FAMILIEBEGIVENHED";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			new ClearTables().clear();
			System.out.println("Tabeller er ryddet");
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return
	 * @throws SQLException
	 */
	public void clear() throws SQLException {
		final Connection conn = DriverManager.getConnection("jdbc:derby:C:\\Users\\michael\\BlistrupDB");
		conn.setAutoCommit(false);
		final PreparedStatement statement = conn.prepareStatement(SET_SCHEMA);
		statement.execute();
		conn.prepareStatement(DELETE1).executeUpdate();
		conn.prepareStatement(DELETE2).executeUpdate();
		conn.prepareStatement(DELETE3).executeUpdate();
		conn.prepareStatement(DELETE4).executeUpdate();
		conn.prepareStatement(DELETE5).executeUpdate();
		conn.prepareStatement(DELETE6).executeUpdate();
		conn.prepareStatement(DELETE7).executeUpdate();

		conn.commit();
		conn.close();
	}
}
