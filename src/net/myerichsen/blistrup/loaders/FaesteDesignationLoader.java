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
 * @version 10. sep. 2023
 *
 */
public class FaesteDesignationLoader extends AbstractLoader {
	private static final String SELECT1 = "SELECT  * FROM F9PERSONFAMILIEQ WHERE TYPE = 'K'";
	private static final String INSERT1 = "INSERT INTO INDIVID (KOEN, BLISTRUPID, FAM, SLGT, FOEDT) VALUES (?, ?, ?, ?, ?)";
	private static final String INSERT2 = "INSERT INTO PERSONNAVN (INDIVIDID, STDNAVN, FONETISKNAVN, PRIMAERNAVN) VALUES (?, ?, ?, 'TRUE')";
	private static final String INSERT3 = "INSERT INTO INDIVIDBEGIVENHED (INDIVIDID, BEGTYPE, DATO, BLISTRUPID, KILDEID, STEDNAVN, NOTE, DETALJER) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
	private static final DateTimeFormatter date8Format = DateTimeFormatter.ofPattern("yyyyMMdd");

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			final int taeller = new FaesteDesignationLoader().load();
			System.out.println("Har indlæst " + taeller + " fæstedesignationslinier");
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
		String blistrupId = "";
		String stedNavn = "";
		String matr = "";
		String gaard = "";
		LocalDate localDate;
		String dato = "";
		String til = "";
		String fra = "";

		final Connection conn = connect("BLISTRUP");
		final KildeModel kModel = new KildeModel();
		kModel.setKbNr("Fæstedesignation");
		kModel.setAarInterval("1754-1832");
		final int kildeId = kModel.insert(conn);

		// "SELECT DISTINCT BEGIV FROM F9PERSONFAMILIEQ WHERE TYPE = 'K'

		final PreparedStatement statement0 = conn.prepareStatement(SELECT1);
		final PreparedStatement statement2 = conn.prepareStatement(INSERT1, Statement.RETURN_GENERATED_KEYS);
		final PreparedStatement statement3 = conn.prepareStatement(INSERT2);
		final PreparedStatement statement4 = conn.prepareStatement(INSERT3);

		final ResultSet rs1 = statement0.executeQuery();

		while (rs1.next()) {
			// "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'K'

			blistrupId = afQ(rs1.getString("PID"));
			stedNavn = afQ(rs1.getString("STEDNAVN"));
			gaard = afQ(rs1.getString("GAARD"));

			// "INSERT INTO INDIVID (KOEN, BLISTRUPID, FAM, SLGT, FOEDT)

			statement2.setString(1, "m".equals(rs1.getString("SEX")) ? "M" : "F");
			statement2.setString(2, blistrupId);
			statement2.setString(3, rs1.getString("FAM"));
			statement2.setString(4, rs1.getString("SLGT"));
			statement2.setString(5, rs1.getString("FQODT"));
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

			stdnavn = afQ(rs1.getString("HP"));
			statement3.setString(2, cleanName(stdnavn));
			try {
				statement3.setString(3, fonkod.generateKey(stdnavn).trim());
			} catch (final Exception e) {
				statement3.setString(3, "");
			}

			statement3.executeUpdate();

			// "INSERT INTO INDIVIDBEGIVENHED (INDIVIDID, BEGTYPE, DATO,
			// BLISTRUPID, KILDEID, STEDNAVN, NOTE, DETALJER) "

			statement4.setInt(1, individId);
			statement4.setString(2, "Fæstedesignation");
			try {
				dato = rs1.getString("DATO").replace("0000", "0101");
				localDate = LocalDate.parse(dato, date8Format);
				statement4.setString(3, localDate.toString());
			} catch (final Exception e) {
				statement4.setString(3, "0001-01-01");
			}
			statement4.setString(4, blistrupId);
			statement4.setInt(5, kildeId);
			if (stedNavn.contains("Blistrup")) {
				stedNavn = stedNavn + ", Holbo, Frederiksborg, ";
			} else {
				stedNavn = stedNavn + ", Blistrup, Holbo, Frederiksborg, ";
			}

			matr = afQ(rs1.getString("MATR_"));

			if (matr.isBlank()) {
				jordlod = afQ(gaard) + ", " + stedNavn;
			} else {
				jordlod = "Matr. " + matr + ", " + afQ(gaard) + ", " + stedNavn;
			}
			statement4.setString(6, jordlod);

			til = rs1.getString("TIL");
			fra = rs1.getString("FRA");

			if (!til.isBlank()) {
				statement4.setString(7, rs1.getString("ROLLE") + " " + afQ(til));
			} else if (!fra.isBlank()) {
				statement4.setString(7, rs1.getString("ROLLE") + " " + afQ(fra));
			} else {
				statement4.setString(7, "");
			}

			statement4.setString(8, "4 CONT Side " + rs1.getString("SIDE") + ", opslag " + rs1.getString("OPSLAG")
					+ ", " + rs1.getString("STILLING"));
			statement4.executeUpdate();

			taeller++;

		}

		statement0.close();
		statement2.close();
		statement3.close();
		statement4.close();
		conn.commit();
		conn.close();
		return taeller;
	}
}
