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
 * Indlæs arvefæster
 *
 * @author Michael Erichsen
 * @version 10. sep. 2023
 *
 */
public class ArveFaesteLoader extends AbstractLoader {
	private static final String SELECT1 = "SELECT AAR, LBNR, STEDNAVN FROM F9PERSONFAMILIEQ WHERE TYPE = 'M'";
	private static final String SELECT2 = "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'M' AND AAR = ? AND LBNR = ? AND STEDNAVN = ?";

	private static final String INSERT1 = "INSERT INTO INDIVID (BLISTRUPID, KOEN, FOEDT, FAM, SLGT) VALUES (?, ?, ?, ?, ?)";
	private static final String INSERT2 = "INSERT INTO PERSONNAVN (INDIVIDID, STDNAVN, FONETISKNAVN, PRIMAERNAVN) VALUES (?, ?, ?, 'TRUE')";
	private static final String INSERT3 = "INSERT INTO INDIVIDBEGIVENHED (INDIVIDID, BEGTYPE, DATO, ALDER, BLISTRUPID, KILDEID, STEDNAVN, DETALJER) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
	private static final DateTimeFormatter date8Format = DateTimeFormatter.ofPattern("yyyyMMdd");

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			final int taeller = new ArveFaesteLoader().load();
			System.out.println("Har indlæst " + taeller + " arvefæstelinier");
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return
	 * @throws SQLException
	 */
	public int load() throws SQLException {
		ResultSet rs1, generatedKeys;
		int individId = 0;
		int taeller = 0;
		String stdnavn = "";
		String koen = "";
		String jordlod = "";
		String blistrupId = "";
		String stedNavn = "";
		int alder = 0;
		LocalDate localDate;
		String aar = "";
		String lbNr = "";

		final Connection conn = connect("BLISTRUP");
		final KildeModel kModel = new KildeModel();
		kModel.setKbNr("Arvefæste");
		kModel.setAarInterval("1788-1844");
		final int kildeId = kModel.insert(conn);

		// SELECT1 = "SELECT DISTINCT BEGIV FROM F9PERSONFAMILIEQ WHERE TYPE = 'H'

		final PreparedStatement statement0 = conn.prepareStatement(SELECT1);
		final PreparedStatement statement1 = conn.prepareStatement(SELECT2);
		final PreparedStatement statement2 = conn.prepareStatement(INSERT1, Statement.RETURN_GENERATED_KEYS);
		final PreparedStatement statement3 = conn.prepareStatement(INSERT2);
		final PreparedStatement statement4 = conn.prepareStatement(INSERT3);

		final ResultSet rs0 = statement0.executeQuery();

		while (rs0.next()) {

			// "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'M' AND AAR = ? AND LBNR = ? AND
			// STEDNAVN = ?";

			aar = rs0.getString("AAR");
			statement1.setString(1, aar);
			lbNr = rs0.getString("LBNR");
			statement1.setString(2, lbNr);
			stedNavn = rs0.getString("STEDNAVN");
			statement1.setString(3, stedNavn);
			rs1 = statement1.executeQuery();

			if (rs1.next()) {
				stedNavn = afQ(stedNavn);

				// "INSERT INTO INDIVID (BLISTRUPID, KOEN, FOEDT, FAM, SLGT) VALUES (?, ?, ?, ?,

				blistrupId = afQ(rs1.getString("BEGIV"));
				statement2.setString(1, blistrupId);
				koen = rs1.getString("SEX");
				statement2.setString(2, "m".equals(koen) ? "M" : "F");
				statement2.setString(3, rs1.getString("FQODT"));
				statement2.setString(4, rs1.getString("FAM"));
				statement2.setString(5, rs1.getString("SLGT"));
				statement2.executeUpdate();
				generatedKeys = statement2.getGeneratedKeys();

				if (generatedKeys.next()) {
					individId = generatedKeys.getInt(1);
				} else {
					individId = 0;
				}

				generatedKeys.close();

				// "INSERT INTO PERSONNAVN (INDIVIDID, STDNAVN, FONETISKNAVN, PRIMAERNAVN)

				statement3.setInt(1, individId);
				stdnavn = afQ(rs1.getString("STD_NAVN"));
				statement3.setString(2, cleanName(stdnavn));
				try {
					statement3.setString(3, fonkod.generateKey(stdnavn).trim());
				} catch (final Exception e) {
					statement3.setString(3, "");
				}

				statement3.executeUpdate();

				// "INSERT INTO INDIVIDBEGIVENHED (INDIVIDID, BEGTYPE, DATO, ALDER, BLISTRUPID,
				// KILDEID, STEDNAVN, DETALJER) "

				statement4.setInt(1, individId);
				statement4.setString(2, "Arvefæste");
				localDate = LocalDate.parse(rs1.getString("DATO"), date8Format);
				statement4.setString(3, localDate.toString());

				try {
					alder = Integer.parseInt(rs1.getString("AAR")) - Integer.parseInt(rs1.getString("ALDER"));
				} catch (final Exception e) {
					alder = 0;
				}

				statement4.setString(4, Integer.toString(alder));
				statement4.setString(5, blistrupId);
				statement4.setInt(6, kildeId);
				jordlod = "Matr. " + afQ(rs1.getString("MATR_")) + ", " + afQ(rs1.getString("GAARD")) + ", " + stedNavn
						+ ", Blistrup, Holbo, Frederiksborg, ";
				statement4.setString(7, jordlod);
				statement4.setString(8, "4 CONT Side " + rs1.getString("SIDE") + ", opslag " + rs1.getString("OPSLAG")
						+ ", " + rs1.getString("CIVILSTAND"));
				statement4.executeUpdate();

				taeller++;

			} else
				System.err.println("Ikke fundet: SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'M' AND AAR = " + aar
						+ " AND LBNR = " + lbNr + " AND STEDNAVN = " + stedNavn);
		}

		statement0.close();
		statement1.close();
		statement2.close();
		statement3.close();
		statement4.close();
		conn.commit();
		conn.close();
		return taeller;
	}
}
