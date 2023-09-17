package net.myerichsen.blistrup.loaders;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import net.myerichsen.blistrup.models.KildeModel;

/**
 * Indlæs fæstedesignationer
 *
 * @author Michael Erichsen
 * @version 17. sep. 2023
 *
 */

public class RealRegisterLoader extends AbstractLoader {
	private static final String SELECT1 = "SELECT  * FROM F9PERSONFAMILIEQ WHERE TYPE = 'N' ORDER BY BEGIV";
	private static final String INSERT1 = "INSERT INTO INDIVID (KOEN, FAM, SLGT, FOEDT) VALUES (?, ?, ?, ?)";
	private static final String INSERT2 = "INSERT INTO PERSONNAVN (INDIVIDID, STDNAVN, FONETISKNAVN, PRIMAERNAVN) VALUES (?, ?, ?, 'TRUE')";
	private static final String INSERT3 = "INSERT INTO INDIVIDBEGIVENHED (INDIVIDID, BEGTYPE, DATO, KILDEID, STEDNAVN, NOTE, DETALJER) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?)";
	private static final DateTimeFormatter date8Format = DateTimeFormatter.ofPattern("yyyyMMdd");

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			final int taeller = new RealRegisterLoader().load();
			System.out.println("Har indlæst " + taeller + " realregisterlinier");
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return
	 * @throws SQLException
	 */
	public int load() throws SQLException {
		ResultSet generatedKeys;
		int individId = 0;
		int taeller = 0;
		String stdnavn = "";
		String jordlod = "";
		String stedNavn = "";
		String matr = "";
		String gaard = "";
		LocalDate localDate;
		String dato = "";
		String til = "";
		String fra = "";

		final Connection conn = connect("BLISTRUP");
		final KildeModel kModel = new KildeModel();
		kModel.setKbNr("Realregister");
		kModel.setAarInterval("1795-1914");
		final int kildeId = kModel.insert(conn);

		final PreparedStatement statement0 = conn.prepareStatement(SELECT1);
		final PreparedStatement statement2 = conn.prepareStatement(INSERT1, Statement.RETURN_GENERATED_KEYS);
		final PreparedStatement statement3 = conn.prepareStatement(INSERT2);
		final PreparedStatement statement4 = conn.prepareStatement(INSERT3);

		// SELECT1 = "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'N' ORDER BY PID";

		final ResultSet rs1 = statement0.executeQuery();

		while (rs1.next()) {
			// "INSERT INTO INDIVID (KOEN, FAM, SLGT, FOEDT)

			statement2.setString(1, "m".equals(rs1.getString("SEX")) ? "M" : "F");
			statement2.setString(2, rs1.getString("FAM"));
			statement2.setString(3, rs1.getString("SLGT"));
			statement2.setString(4, rs1.getString("FQODT"));
			statement2.executeUpdate();
			generatedKeys = statement2.getGeneratedKeys();

			if (generatedKeys.next()) {
				individId = generatedKeys.getInt(1);
			} else {
				individId = 0;
			}

			generatedKeys.close();

			// "INSERT INTO PERSONNAVN (INDIVIDID, STDNAVN, FONETISKNAVN,
			// PRIMAERNAVN) VALUES

			statement3.setInt(1, individId);
			stdnavn = afQ(rs1.getString("STD_NAVN"));
			statement3.setString(2, cleanName(stdnavn));

			try {
				statement3.setString(3, fonkod.generateKey(stdnavn).trim());
			} catch (final Exception e) {
				statement3.setString(3, "");
			}

			statement3.executeUpdate();

			// "INSERT INTO INDIVIDBEGIVENHED (INDIVIDID, BEGTYPE, DATO,
			// KILDEID, STEDNAVN, NOTE, DETALJER) "

			statement4.setInt(1, individId);
			statement4.setString(2, "Realregister");

			try {
				dato = rs1.getString("DATO").replace("0000", "0101");
				localDate = LocalDate.parse(dato, date8Format);
				statement4.setString(3, localDate.toString());
			} catch (final Exception e) {
				statement4.setString(3, "0001-01-01");
			}

			statement4.setInt(4, kildeId);
			stedNavn = fixStedNavn(afQ(rs1.getString("STEDNAVN")));
			gaard = afQ(rs1.getString("GAARD"));
			matr = afQ(rs1.getString("MATR_"));

			if (matr.isBlank()) {
				jordlod = gaard + ", " + stedNavn;
			} else {
				jordlod = "Matr. " + matr + ", " + gaard + ", " + stedNavn;
			}

			statement4.setString(5, jordlod);
			til = rs1.getString("TIL");
			fra = rs1.getString("FRA");

			if (!til.isBlank()) {
				statement4.setString(6, rs1.getString("ROLLE") + " " + afQ(til));
			} else if (!fra.isBlank()) {
				statement4.setString(6, rs1.getString("ROLLE") + " " + afQ(fra));
			} else {
				statement4.setString(6, "");
			}

			statement4.setString(7,
					"4 CONT Side " + rs1.getString("SIDE") + ", opslag " + rs1.getString("OPSLAG") + ", "
							+ afQ(rs1.getString("STILLING")) + ", " + rs1.getString("CIVILSTAND") + ", "
							+ afQ(rs1.getString("ERHVERV")));
			statement4.executeUpdate();

			taeller++;

		}

		conn.commit();
		conn.close();
		return taeller;
	}
}