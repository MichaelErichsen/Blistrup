package net.myerichsen.blistrup.loaders;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.myerichsen.blistrup.models.KildeModel;

/**
 * Læs dåbsdata fra grundtabellen ind i GEDCOM-tabeller
 *
 * @author Michael Erichsen
 * @version 17. sep. 2023
 *
 */
public class DaabLoader extends AbstractLoader {
	private static final String SELECT1 = "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'A' ORDER BY BEGIV, RX";

	private static final String INSERT1 = "INSERT INTO INDIVID (KOEN, FOEDT, FAM, SLGT) VALUES (?, ?, ?, ?)";
	private static final String INSERT2 = "INSERT INTO PERSONNAVN (INDIVIDID, FORNAVN, EFTERNAVN, PRIMAERNAVN, FONETISKNAVN, STDNAVN) VALUES (?, ?, ?, ?, ?, ?)";
	private static final String INSERT4 = "INSERT INTO INDIVIDBEGIVENHED (INDIVIDID, ALDER, BEGTYPE, DATO, ROLLE, KILDEID, STEDNAVN, BEM, FOEDT) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String INSERT5 = "INSERT INTO VIDNE (INDIVIDID, ROLLE, INDIVIDBEGIVENHEDID) VALUES (?, ?, ?)";
	private static final String INSERT6 = "INSERT INTO FAMILIE (HUSFADER) VALUES(?)";

	private static final String UPDATE1 = "UPDATE INDIVID SET FAMC = ? WHERE ID = ?";
	private static final String UPDATE2 = "UPDATE INDIVIDBEGIVENHED SET DETALJER = ? WHERE ID = ?";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			final int taeller = new DaabLoader().load();
			System.out.println("Har indlæst " + taeller + " dåbslinier");
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return
	 * @throws SQLException ROWS ONLY";
	 */
	public int load() throws SQLException {
		String rolle = "";
		ResultSet generatedKeys;
		int individId = 0;
		String fornvn = "";
		String efternvn = "";
		String dato = "";
		String mm = "";
		String dd = "";
		int kildeId = 0;
		int individBegivenhedsId = 0;
		int husfaderId;
		int familieId;
		int barnId = 0;
		int taeller = 0;
		StringBuilder sb = new StringBuilder();
//		String navn = "";
		String stdnavn = "";
		String barnenavn = "";
		boolean first = true;

		final Connection conn = connect("BLISTRUP");
		final PreparedStatement statements1 = conn.prepareStatement(SELECT1);
		final PreparedStatement statementi1 = conn.prepareStatement(INSERT1, Statement.RETURN_GENERATED_KEYS);
		final PreparedStatement statementi2 = conn.prepareStatement(INSERT2);
		final PreparedStatement statementi4 = conn.prepareStatement(INSERT4, Statement.RETURN_GENERATED_KEYS);
		final PreparedStatement statementi5 = conn.prepareStatement(INSERT5);
		final PreparedStatement statementi6 = conn.prepareStatement(INSERT6, Statement.RETURN_GENERATED_KEYS);
		final PreparedStatement statementu1 = conn.prepareStatement(UPDATE1);
		final PreparedStatement statementu2 = conn.prepareStatement(UPDATE2);

		// SELECT1 = "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'A' ORDER BY BEGIV,
		// RX";

		final ResultSet rs1 = statements1.executeQuery();

		while (rs1.next()) {
			rolle = rs1.getString("ROLLE").trim();
//			navn = rs1.getString("NAVN").trim();

			// INSERT1 = "INSERT INTO INDIVID (KOEN, FOEDT, FAM, SLGT)

			statementi1.setString(1, rs1.getString("SEX").trim());
			statementi1.setString(2, rs1.getString("FQODT").trim());
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

			// INSERT2 = "INSERT INTO PERSONNAVN (INDIVIDID, FORNAVN, EFTERNAVN,
			// PRIMAERNAVN, FONETISKNAVN, STDNAVN) VALUES (?, ?, ?, ?, ?, ?)";

			statementi2.setInt(1, individId);
			fornvn = afQ(rs1.getString("FORNVN"));
			statementi2.setString(2, fornvn);
			efternvn = afQ(rs1.getString("EFTERNVN"));
			statementi2.setString(3, efternvn);
			statementi2.setString(4, "TRUE");
			stdnavn = afQ(rs1.getString("STD_NAVN"));

			try {
				statementi2.setString(5, fonkod.generateKey(stdnavn).trim());
			} catch (final Exception e) {
				statementi2.setString(5, "");
			}

			statementi2.setString(6, cleanName(stdnavn));
			statementi2.executeUpdate();

			taeller++;

			if ("barn".equals(rolle)) {
				if (!first) {
					// UPDATE2 = "UPDATE INDIVIDBEGIVENHED SET DETALJER = ? WHERE ID = ?";

					statementu2.setString(1, afQ(sb.toString()));
					statementu2.setInt(2, individBegivenhedsId);
					statementu2.executeUpdate();
				} else {
					first = false;
				}

				sb = new StringBuilder();
				barnId = individId;
				barnenavn = stdnavn;

				final KildeModel kModel = new KildeModel();
				kModel.setKbNr(rs1.getString("KBNR").trim());
				kModel.setAarInterval(rs1.getString("KILDE").trim());
				kModel.setKbDel(rs1.getString("KBDEL").trim());
				kModel.setTifNr(rs1.getString("TIFNR").trim());
				kModel.setOpslag(rs1.getString("OPSLAG").trim());
				kModel.setOpNr(rs1.getString("OPNR").trim());
				kildeId = kModel.insert(conn);

				// INSERT4 = "INSERT INTO INDIVIDBEGIVENHED (INDIVIDID, ALDER, BEGTYPE, DATO,
				// ROLLE, KILDEID, STEDNAVN, BEM, FOEDT) "

				statementi4.setInt(1, individId);
				statementi4.setInt(2, 0);
				statementi4.setString(3, "Dåb");

				try {
					dato = rs1.getString("FQODTDATO").trim();
					mm = dato.substring(4, 6);
					dd = dato.substring(6, 8);
					statementi4.setString(4, dato.substring(0, 4) + "-" + ("00".equals(mm) ? "01" : mm) + "-"
							+ ("00".equals(dd) ? "01" : dd));
				} catch (final Exception e) {
					statementi4.setString(4, "0001-01-01");
				}

				statementi4.setString(5, rolle);
				statementi4.setInt(6, kildeId);
				statementi4.setString(7, fixStedNavn(afQ(rs1.getString("STEDNAVN"))));
				statementi4.setString(8, afQ(rs1.getString("BEM")));
				statementi4.setString(9, rs1.getString("FQODT").trim());
				statementi4.executeUpdate();
				generatedKeys = statementi4.getGeneratedKeys();

				if (generatedKeys.next()) {
					individBegivenhedsId = generatedKeys.getInt(1);
				} else {
					individBegivenhedsId = 0;
				}
				generatedKeys.close();
			} else if ("far".equals(rolle)) {
				husfaderId = individId;

				// INSERT5 = "INSERT INTO VIDNE (INDIVIDID, ROLLE, INDIVIDBEGIVENHEDID) VALUES

				statementi5.setInt(1, individId);
				statementi5.setString(2, rs1.getString("ROLLE").trim());
				statementi5.setInt(3, individBegivenhedsId);
				statementi5.executeUpdate();

				// INSERT6 = "INSERT INTO FAMILIE (HUSFADER) VALUES(?)";

				statementi6.setInt(1, husfaderId);
				statementi6.executeUpdate();
				generatedKeys.close();
				generatedKeys = statementi6.getGeneratedKeys();

				if (generatedKeys.next()) {
					familieId = generatedKeys.getInt(1);
				} else {
					familieId = 0;
				}
				generatedKeys.close();

				// UPDATE1 = "UPDATE INDIVID SET FAMC = ? WHERE ID = ?";

				statementu1.setInt(1, familieId);
				statementu1.setInt(2, barnId);
				statementu1.executeUpdate();
			} else {
				// "gud" or "f1", "f2, etc.
				// INSERT5 = "INSERT INTO VIDNE (INDIVIDID, ROLLE, INDIVIDBEGIVENHEDID) VALUES

				statementi5.setInt(1, individId);
				statementi5.setString(2, rs1.getString("ROLLE").trim() + " for " + barnenavn);
				statementi5.setInt(3, individBegivenhedsId);
				statementi5.executeUpdate();
			}
			sb.append(rolle + ": " + stdnavn + ", \r\n4 CONT ");
		}

		conn.commit();
		conn.close();
		return taeller;
	}
}
