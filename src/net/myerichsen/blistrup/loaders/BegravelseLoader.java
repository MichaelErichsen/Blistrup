package net.myerichsen.blistrup.loaders;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.myerichsen.blistrup.models.KildeModel;

/**
 * Indlæs begravelser
 *
 * @author Michael Erichsen
 * @version 5. okt. 2023
 *
 */
public class BegravelseLoader extends AbstractLoader {
	private static final String SELECT1 = "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'D' ORDER BY BEGIV, RX";

	private static final String INSERT1 = "INSERT INTO INDIVID (KOEN, FOEDT, FAM, SLGT) VALUES (?, ?, ?, ?)";
	private static final String INSERT2 = "INSERT INTO PERSONNAVN (INDIVIDID, STDNAVN, FONETISKNAVN, PRIMAERNAVN) VALUES (?, ?, ?, 'TRUE')";
	private static final String INSERT4 = "INSERT INTO INDIVIDBEGIVENHED (INDIVIDID, ALDER, BEGTYPE, DATO, ROLLE, KILDEID, STEDNAVN, BEM, FOEDT) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String INSERT5 = "INSERT INTO VIDNE (INDIVIDID, ROLLE, INDIVIDBEGIVENHEDID) VALUES (?, ?, ?)";
	private static final String INSERT6 = "INSERT INTO FAMILIE (HUSFADER) VALUES(?)";
	private static final String INSERT7 = "INSERT INTO FAMILIE (HUSMODER) VALUES(?)";
	private static final String INSERT8 = "INSERT INTO FAMILIE (HUSFADER, HUSMODER) VALUES(?, ?)";

	private static final String UPDATE1 = "UPDATE INDIVID SET FAMC = ? WHERE ID = ?";
	private static final String UPDATE2 = "UPDATE INDIVIDBEGIVENHED SET DETALJER = ? WHERE ID = ?";
	private static final String UPDATE3 = "UPDATE FAMILIE SET HUSMODER = ? WHERE ID = ?";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			final int taeller = new BegravelseLoader().load();
			System.out.println("Har indlæst " + taeller + " begravelseslinier");
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return
	 * @throws SQLException
	 */
	public int load() throws SQLException {
		String rolle = "";
		ResultSet generatedKeys;
		int individId = 0;
		int kildeId = 0;
		int individBegivenhedsId = 0;
		int husfaderId = 0;
		int husmoderId = 0;
		int familieId = 0;
		int doedId = 0;
		int taeller = 0;
		StringBuilder sb = new StringBuilder();
		String navn = "";
		String stdnavn = "";
		int aefaelleId = 0;
		String koen = "";

		final Connection conn = connect("BLISTRUP");
		final PreparedStatement statements1 = conn.prepareStatement(SELECT1);
		final PreparedStatement statementi1 = conn.prepareStatement(INSERT1, Statement.RETURN_GENERATED_KEYS);
		final PreparedStatement statementi2 = conn.prepareStatement(INSERT2);
		final PreparedStatement statementi4 = conn.prepareStatement(INSERT4, Statement.RETURN_GENERATED_KEYS);
		final PreparedStatement statementi5 = conn.prepareStatement(INSERT5);
		final PreparedStatement statementi6 = conn.prepareStatement(INSERT6, Statement.RETURN_GENERATED_KEYS);
		final PreparedStatement statementi7 = conn.prepareStatement(INSERT7, Statement.RETURN_GENERATED_KEYS);
		final PreparedStatement statementi8 = conn.prepareStatement(INSERT8);
		final PreparedStatement statementu1 = conn.prepareStatement(UPDATE1);
		final PreparedStatement statementu2 = conn.prepareStatement(UPDATE2);
		final PreparedStatement statementu3 = conn.prepareStatement(UPDATE3);

		// SELECT1 = "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'D' ORDER BY PID";

		final ResultSet rs1 = statements1.executeQuery();

		while (rs1.next()) {
			rolle = afQ(rs1.getString("ROLLE"));
			navn = afQ(rs1.getString("NAVN"));

			koen = rs1.getString("SEX").trim();

			// INSERT1 = "INSERT INTO INDIVID (KOEN, FOEDT, FAM, SLGT) VALUES

			statementi1.setString(1, koen);
			statementi1.setString(2, getFoedtDoebtDato(rs1));
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

			taeller++;

			// død, barn, far, mor, æfælle
			if ("død".equals(rolle)) {
				// UPDATE2 = "UPDATE INDIVIDBEGIVENHED SET DETALJER = ? WHERE ID = ?";

				statementu2.setString(1, afQ(sb.toString()));
				statementu2.setInt(2, individBegivenhedsId);
				statementu2.executeUpdate();

				sb = new StringBuilder();
				doedId = individId;

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

				try {
					statementi4.setInt(2, Integer.parseInt(rs1.getString("ALDER").trim()));
				} catch (final Exception e) {
					statementi4.setInt(2, 0);
				}

				statementi4.setString(3, "Begravelse");
				statementi4.setString(4, rs1.getString("AAR").trim() + "-01-01");
				statementi4.setString(5, rolle);
				statementi4.setInt(6, kildeId);
				statementi4.setString(7, fixStedNavn(rs1.getString("STEDNAVN")));
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

				statementi5.setInt(1, husfaderId);
				statementi5.setString(2, rolle);
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
				statementu1.setInt(2, doedId);
				statementu1.executeUpdate();

			} else if ("mor".equals(rolle)) {
				husmoderId = individId;

				// INSERT5 = "INSERT INTO VIDNE (INDIVIDID, ROLLE, INDIVIDBEGIVENHEDID) VALUES

				statementi5.setInt(1, husmoderId);
				statementi5.setString(2, rolle);
				statementi5.setInt(3, individBegivenhedsId);
				statementi5.executeUpdate();

				if (familieId == 0) {

					// INSERT7 = "INSERT INTO FAMILIE (HUSMODER) VALUES(?)";

					statementi7.setInt(1, husfaderId);
					statementi7.executeUpdate();
					generatedKeys.close();
					generatedKeys = statementi7.getGeneratedKeys();

					if (generatedKeys.next()) {
						familieId = generatedKeys.getInt(1);
					} else {
						familieId = 0;
					}
					generatedKeys.close();

					// UPDATE1 = "UPDATE INDIVID SET FAMC = ? WHERE ID = ?";

					statementu1.setInt(1, familieId);
					statementu1.setInt(2, doedId);
					statementu1.executeUpdate();
				} else {

					// UPDATE3 = "UPDATE FAMILIE SET HUSMODER = ? WHERE ID = ?";

					statementu3.setInt(1, husmoderId);
					statementu3.setInt(2, familieId);
					statementu3.executeUpdate();
				}
			} else if ("æfælle".equals(rolle)) {
				aefaelleId = individId;

				// INSERT5 = "INSERT INTO VIDNE (INDIVIDID, ROLLE, INDIVIDBEGIVENHEDID) VALUES

				statementi5.setInt(1, husfaderId);
				statementi5.setString(2, rolle);
				statementi5.setInt(3, individBegivenhedsId);
				statementi5.executeUpdate();

				// INSERT8 = "INSERT INTO FAMILIE (HUSFADER, HUSMODER) VALUES(?, ?)";

				if ("m".equals(koen)) {
					statementi8.setInt(1, aefaelleId);
					statementi8.setInt(2, doedId);
				} else {
					statementi8.setInt(1, doedId);
					statementi8.setInt(2, aefaelleId);
				}

				statementi8.executeUpdate();
			}
			sb.append(rolle + ": " + navn + ", \r\n4 CONT ");
		}

		conn.commit();
		conn.close();
		return taeller;
	}
}
