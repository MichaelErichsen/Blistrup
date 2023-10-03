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
 * @version 3. okt. 2023
 *
 */
public class ArveFaesteLoader extends AbstractLoader {
	private static final String SELECT1 = "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'M' ORDER BY PID";

	private static final String INSERT1 = "INSERT INTO INDIVID (KOEN, FOEDT, FAM, SLGT) VALUES (?, ?, ?, ?)";
	private static final String INSERT2 = "INSERT INTO PERSONNAVN (INDIVIDID, STDNAVN, FONETISKNAVN, PRIMAERNAVN) VALUES (?, ?, ?, 'TRUE')";
	private static final String INSERT3 = "INSERT INTO INDIVIDBEGIVENHED (INDIVIDID, BEGTYPE, DATO, ALDER, KILDEID, STEDNAVN, DETALJER) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?)";
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
		ResultSet generatedKeys;
		int individId = 0;
		int taeller = 0;
		String stdnavn = "";
		String stedNavn = "";
		int alder = 0;
		LocalDate localDate;
		StringBuilder sb;
		String matr = "";

		final Connection conn = connect("BLISTRUP");
		final KildeModel kModel = new KildeModel();
		kModel.setKbNr("Arvefæste");
		kModel.setAarInterval("1788-1844");
		final int kildeId = kModel.insert(conn);

		// SELECT1 = "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'M' ORDER BY PID";

		final PreparedStatement statements1 = conn.prepareStatement(SELECT1);
		final PreparedStatement statementi1 = conn.prepareStatement(INSERT1, Statement.RETURN_GENERATED_KEYS);
		final PreparedStatement statementi2 = conn.prepareStatement(INSERT2);
		final PreparedStatement statementi3 = conn.prepareStatement(INSERT3);

		final ResultSet rs1 = statements1.executeQuery();

		while (rs1.next()) {
			// INSERT1 = "INSERT INTO INDIVID (KOEN, FOEDT, FAM, SLGT) VALUES

			stedNavn = afQ(rs1.getString("STEDNAVN"));
			statementi1.setString(1, "m".equals(rs1.getString("SEX")) ? "M" : "F");
			String foedt = getFoedtDoebtDato(rs1);
			statementi1.setString(2, foedt);
			statementi1.setString(3, rs1.getString("FAM"));
			statementi1.setString(4, rs1.getString("SLGT"));
			statementi1.executeUpdate();
			generatedKeys = statementi1.getGeneratedKeys();

			if (generatedKeys.next()) {
				individId = generatedKeys.getInt(1);
			} else {
				individId = 0;
			}

			generatedKeys.close();

			// INSERT2 = "INSERT INTO PERSONNAVN (INDIVIDID, STDNAVN, FONETISKNAVN,
			// PRIMAERNAVN) VALUES (?, ?, ?, 'TRUE')";

			statementi2.setInt(1, individId);
			stdnavn = afQ(rs1.getString("STD_NAVN"));
			statementi2.setString(2, cleanName(stdnavn));
			try {
				statementi2.setString(3, fonkod.generateKey(stdnavn).trim());
			} catch (final Exception e) {
				statementi2.setString(3, "");
			}

			statementi2.executeUpdate();

			// INSERT3 = "INSERT INTO INDIVIDBEGIVENHED (INDIVIDID, BEGTYPE, DATO, ALDER,
			// KILDEID, STEDNAVN, DETALJER) "

			statementi3.setInt(1, individId);
			statementi3.setString(2, "Arvefæste");
			localDate = LocalDate.parse(rs1.getString("DATO"), date8Format);
			statementi3.setString(3, localDate.toString());

			try {
				alder = Integer.parseInt(rs1.getString("AAR")) - Integer.parseInt(rs1.getString("ALDER"));
			} catch (final Exception e) {
				alder = 0;
			}

			statementi3.setString(4, Integer.toString(alder));
			statementi3.setInt(5, kildeId);
			sb = new StringBuilder();
			matr = rs1.getString("MATR_");

			if (matr != null && !matr.isBlank()) {
				sb.append("Matr. " + afQ(matr) + ", ");
			}

			sb.append(afQ(rs1.getString("GAARD")) + ", " + stedNavn + ", Blistrup, Holbo, Frederiksborg, ");
			statementi3.setString(6, sb.toString());
			statementi3.setString(7, "Side " + rs1.getString("SIDE") + ", opslag " + rs1.getString("OPSLAG") + ", "
					+ rs1.getString("CIVILSTAND"));
			statementi3.executeUpdate();

			taeller++;
		}

		conn.commit();
		conn.close();
		return taeller;
	}
}
