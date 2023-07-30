package net.myerichsen.blistrup.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Class to list contents of a BLISTRUP Derby table.
 *
 * @author Michael Erichsen
 * @version 30. jul. 2023
 *
 */
public class VidneLister {

	/**
	 * MilRollEntryDialog method
	 *
	 * @param args
	 */
	public static void main(String[] args) {

		final VidneLister dbl = new VidneLister();

		try {
			dbl.execute(args);
		} catch (IOException | SQLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Worker method
	 *
	 * @param args
	 * @throws GedcomParserException
	 * @throws IOException
	 * @throws SQLException
	 */
	private void execute(String[] args) throws IOException, SQLException {
		int taeller = 0;
		final String dbURL = "jdbc:derby:C:\\Users\\michael\\BlistrupDB";
		final Connection conn = DriverManager.getConnection(dbURL);
		final PreparedStatement statement = conn
				.prepareStatement("SELECT DISTINCT ROLLE FROM BLISTRUP.VIDNE ORDER BY ROLLE");
		final ResultSet rs = statement.executeQuery();

		while (rs.next()) {
			System.out.println(rs.getString("ROLLE"));
			taeller++;
		}

		conn.close();
		System.out.println(taeller + " distinkte roller fundet");
	}

}