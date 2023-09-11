package net.myerichsen.blistrup.loaders;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.myerichsen.blistrup.models.KildeModel;

/**
 * L�s konfirmationsdata fra grundtabellen ind i GEDCOM-tabeller
 *
 * @author Michael Erichsen
 * @version 11. sep. 2023
 *
 */
public class KonfirmationLoader extends AbstractLoader {
	private static final String SELECT1 = "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'B'";

	private static final String INSERT1 = "INSERT INTO INDIVID (KOEN, BLISTRUPID, FOEDT, FAM, SLGT) VALUES (?, ?, ?, ?, ?)";
	private static final String INSERT2 = "INSERT INTO PERSONNAVN (INDIVIDID, FORNAVN, EFTERNAVN, PRIMAERNAVN, FONETISKNAVN, STDNAVN) VALUES (?, ?, ?, ?, ?, ?)";
	private static final String INSERT3 = "INSERT INTO INDIVIDBEGIVENHED (INDIVIDID, ALDER, BEGTYPE, DATO, NOTE, ROLLE, BLISTRUPID, KILDEID, STEDNAVN, BEM) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String INSERT4 = "INSERT INTO VIDNE (INDIVIDID, ROLLE, INDIVIDBEGIVENHEDID) VALUES (?, ?, ?)";
	private static final String INSERT5 = "INSERT INTO FAMILIE (HUSFADER) VALUES(?)";
	private static final String INSERT6 = "INSERT INTO FAMILIE (HUSMODER) VALUES(?)";

	private static final String UPDATE1 = "UPDATE INDIVID SET FAMC = ? WHERE ID = ?";
	private static final String UPDATE2 = "UPDATE INDIVIDBEGIVENHED SET DETALJER = ? WHERE ID = ?";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			final int taeller = new KonfirmationLoader().load();
			System.out.println("Har indl�st " + taeller + " konfirmationslinier");
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
		String dato = "";
		String mm = "";
		String dd = "";
		int kildeId = 0;
		int individBegivenhedsId = 0;
		int husfaderId = 0;
		int husmoderId = 0;
		int familieId = 0;
		int barnId = 0;
		int taeller = 0;
		StringBuilder sb;
		String navn = "";
		String fader = "";
		String moder = "";
		String stdnavn = "";

		final Connection conn = connect("BLISTRUP");
		PreparedStatement statements1 = conn.prepareStatement(SELECT1);
		PreparedStatement statementi1 = conn.prepareStatement(INSERT1, Statement.RETURN_GENERATED_KEYS);
		PreparedStatement statementi2 = conn.prepareStatement(INSERT2);
		PreparedStatement statementi3 = conn.prepareStatement(INSERT3, Statement.RETURN_GENERATED_KEYS);
		PreparedStatement statementi4 = conn.prepareStatement(INSERT4);
		PreparedStatement statementi5 = conn.prepareStatement(INSERT5);
		PreparedStatement statementi6 = conn.prepareStatement(INSERT6);
		PreparedStatement statementu1 = conn.prepareStatement(UPDATE1);
		PreparedStatement statementu2 = conn.prepareStatement(UPDATE2);

		ResultSet rs1 = statements1.executeQuery();

		while (rs1.next()) {
			// INSERT1 = "INSERT INTO INDIVID (KOEN, BLISTRUPID, FOEDT, FAM, SLGT) VALUES

			sb = new StringBuilder();
			rolle = rs1.getString("ROLLE").trim();
			navn = rs1.getString("NAVN").trim();
			sb.append(rolle + ": " + navn + "\r\n4 CONT ");

			statementi1.setString(1, rs1.getString("SEX").trim());
			statementi1.setString(2, rs1.getString("PID").trim());
			statementi1.setString(3, rs1.getString("FQODT").trim());
			statementi1.setString(4, rs1.getString("FAM"));
			statementi1.setString(5, rs1.getString("SLGT"));
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
			statementi2.setString(2, afQ(rs1.getString("FORNVN")));
			statementi3.setString(3, afQ(rs1.getString("EFTERNVN")));
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
				barnId = individId;

				final KildeModel kModel = new KildeModel();
				kModel.setKbNr(rs1.getString("KBNR").trim());
				kModel.setAarInterval(rs1.getString("KILDE").trim());
				kModel.setKbDel(rs1.getString("KBDEL").trim());
				kModel.setTifNr(rs1.getString("TIFNR").trim());
				kModel.setOpslag(rs1.getString("OPSLAG").trim());
				kModel.setOpNr(rs1.getString("OPNR").trim());
				kildeId = kModel.insert(conn);

				// INSERT3 = "INSERT INTO INDIVIDBEGIVENHED (INDIVIDID, ALDER, BEGTYPE, DATO,
				// NOTE, ROLLE, BLISTRUPID, KILDEID, STEDNAVN, BEM) "

				statementi3.setInt(1, individId);
				statementi3.setString(2, "0");
				statementi3.setString(3, "Konfirmation");

				try {
					dato = rs1.getString("FQODTDATO").trim();
					mm = dato.substring(4, 6);
					dd = dato.substring(6, 8);
					statementi3.setString(4, dato.substring(0, 4) + "-" + ("00".equals(mm) ? "01" : mm) + "-"
							+ ("00".equals(dd) ? "01" : dd));
				} catch (final Exception e) {
					statementi3.setString(4, "0001-01-01");
				}

				fader = afQ(rs1.getString("FADER"));
				fader = fader.length() > 0 ? "Fader: " + fader : "";
				moder = afQ(rs1.getString("MODER"));
				moder = moder.length() > 0 ? "Moder: " + moder : "";
				statementi3.setString(5, (fader + " " + moder).trim());
				statementi3.setString(6, rolle);
				statementi3.setString(7, afQ(rs1.getString("BEGIV")));
				statementi3.setInt(8, kildeId);
				statementi3.setString(9, formatPlaceName(afQ(rs1.getString("STEDNAVN"))));
				statementi3.setString(10, afQ(rs1.getString("BEM")));
				statementi3.executeUpdate();
				generatedKeys = statementi3.getGeneratedKeys();

				if (generatedKeys.next()) {
					individBegivenhedsId = generatedKeys.getInt(1);
				} else {
					individBegivenhedsId = 0;
				}
				generatedKeys.close();
			} else {
				if ("far".equals(rolle) || "stedfar".equals(rolle)) {
					// INSERT4 = "INSERT INTO VIDNE (INDIVIDID, ROLLE, INDIVIDBEGIVENHEDID) VALUES

					statementi4.setInt(1, individId);
					statementi4.setString(2, rs1.getString("ROLLE").trim());
					statementi4.setInt(3, individBegivenhedsId);
					statementi4.executeUpdate();
					generatedKeys.close();
					generatedKeys = statementi4.getGeneratedKeys();

					if (generatedKeys.next()) {
						husfaderId = generatedKeys.getInt(1);
					} else {
						husfaderId = 0;
					}
					generatedKeys.close();

					// INSERT5 = "INSERT INTO FAMILIE (HUSFADER) VALUES(?)";

					statementi5.setInt(1, husfaderId);
					statementi5.executeUpdate();
					generatedKeys = statementi5.getGeneratedKeys();

					if (generatedKeys.next()) {
						familieId = generatedKeys.getInt(1);
					} else {
						familieId = 0;
					}
					generatedKeys.close();

					// UPDATE1 = "UPDATE INDIVID SET FAMC = ? WHERE ID = ?";

					statementu1 = conn.prepareStatement(UPDATE1);
					statementu1.setInt(1, familieId);
					statementu1.setInt(2, barnId);
					statementu1.executeUpdate();
				} else if ("mor".equals(rolle)) {
					// INSERT4 = "INSERT INTO VIDNE (INDIVIDID, ROLLE, INDIVIDBEGIVENHEDID) VALUES

					statementi4.setInt(1, individId);
					statementi4.setString(2, rs1.getString("ROLLE").trim());
					statementi4.setInt(3, individBegivenhedsId);
					statementi4.executeUpdate();
					generatedKeys.close();
					generatedKeys = statementi4.getGeneratedKeys();

					if (generatedKeys.next()) {
						husmoderId = generatedKeys.getInt(1);
					} else {
						husmoderId = 0;
					}
					generatedKeys.close();

					// INSERT6 = "INSERT INTO FAMILIE (HUSMODER) VALUES(?)";

					statementi6.setInt(1, husmoderId);
					statementi6.executeUpdate();
					generatedKeys = statementi6.getGeneratedKeys();

					if (generatedKeys.next()) {
						familieId = generatedKeys.getInt(1);
					} else {
						familieId = 0;
					}
					generatedKeys.close();

					// UPDATE1 = "UPDATE INDIVID SET FAMC = ? WHERE ID = ?";

					statementu1 = conn.prepareStatement(UPDATE1);
					statementu1.setInt(1, familieId);
					statementu1.setInt(2, barnId);
					statementu1.executeUpdate();
				} else {
					// INSERT4 = "INSERT INTO VIDNE (INDIVIDID, ROLLE, INDIVIDBEGIVENHEDID) VALUES

					statementi4.setInt(1, individId);
					statementi4.setString(2, rs1.getString("ROLLE").trim());
					statementi4.setInt(3, individBegivenhedsId);
				}
			}

			// UPDATE2 = "UPDATE INDIVIDBEGIVENHED SET DETALJER = ? WHERE ID = ?";

			statementu2.setString(1, afQ(sb.toString()));
			statementu2.setInt(2, individBegivenhedsId);
			statementu2.executeUpdate();
			statementu2.close();
		}

		statements1.close();
		statementi1.close();
		statementi2.close();
		statementi3.close();
		statementi4.close();
		statementi5.close();
		statementi6.close();
		statementu1.close();
		statementu2.close();
		conn.commit();
		conn.close();
		return taeller;
	}
}
