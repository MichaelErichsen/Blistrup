package net.myerichsen.blistrup.loaders;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import net.myerichsen.blistrup.models.KildeModel;

/**
 * Læs vielsesdata fra grundtabellen ind i GEDCOM-tabeller
 *
 * @author Michael Erichsen
 * @version 1. okt. 2023
 *
 */

public class VielseLoader extends AbstractLoader {
	private static final String SELECT1 = "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'C' ORDER BY BEGIV, RX";

	private static final String INSERT1 = "INSERT INTO INDIVID (KOEN, BLISTRUPID, FOEDT, FAM, SLGT) VALUES (?, ?, ?, ?, ?)";
	private static final String INSERT2 = "INSERT INTO PERSONNAVN (INDIVIDID, FORNAVN, EFTERNAVN, PRIMAERNAVN, FONETISKNAVN, STDNAVN) VALUES (?, ?, ?, ?, ?, ?)";
	private static final String INSERT3 = "INSERT INTO FAMILIE (HUSFADER) VALUES(?)";
	private static final String INSERT4 = "INSERT INTO VIDNE (INDIVIDID, ROLLE, FAMILIEBEGIVENHEDID) VALUES (?, ?, ?)";
	private static final String INSERT5 = "INSERT INTO FAMILIEBEGIVENHED (FAMILIEID, BEGTYPE, DATO, BLISTRUPID, KILDEID, STEDNAVN, BEM) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?)";

	private static final String UPDATE1 = "UPDATE INDIVID SET FAMC = ? WHERE ID = ?";
	private static final String UPDATE2 = "UPDATE FAMILIE SET HUSMODER = ? WHERE ID = ?";
	private static final String UPDATE3 = "UPDATE FAMILIEBEGIVENHED SET DETALJER = ? WHERE ID = ?";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			final int taeller = new VielseLoader().load();
			System.out.println("Har indlæst " + taeller + " vielseslinier");
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public int load() throws Exception {
		ResultSet generatedKeys = null;
		String aar = "";
		String fader = "";
//		String navn = "";
		String rolle = "";
		String rx = "";
		String stdnavn = "";
		int brud = 0;
		int faderFamilieId = 0;
		int faderId = 0;
		int familieBegivenhedId = 0;
		int familieId = 0;
		int gom = 0;
		int individId = 0;
		int taeller = 0;
		StringBuilder sb = new StringBuilder();

		final Connection conn = connect("BLISTRUP");
		final PreparedStatement statements1 = conn.prepareStatement(SELECT1);
		final PreparedStatement statementi1 = conn.prepareStatement(INSERT1, Statement.RETURN_GENERATED_KEYS);
		final PreparedStatement statementi2 = conn.prepareStatement(INSERT2);
		final PreparedStatement statementi3 = conn.prepareStatement(INSERT3, Statement.RETURN_GENERATED_KEYS);
		final PreparedStatement statementi4 = conn.prepareStatement(INSERT4, Statement.RETURN_GENERATED_KEYS);
		final PreparedStatement statementi5 = conn.prepareStatement(INSERT5, Statement.RETURN_GENERATED_KEYS);
		final PreparedStatement statementu1 = conn.prepareStatement(UPDATE1);
		final PreparedStatement statementu2 = conn.prepareStatement(UPDATE2);
		final PreparedStatement statementu3 = conn.prepareStatement(UPDATE3);

		// SELECT1 = "SELECT DISTINCT BEGIV FROM F9PERSONFAMILIEQ WHERE TYPE = 'C' ORDER
		// BY BEGIV, RX";

		final ResultSet rs1 = statements1.executeQuery();

		while (rs1.next()) {
			rx = rs1.getString("RX").trim();
			rolle = rs1.getString("ROLLE").trim();
			fader = rs1.getString("FADER");

			// INSERT1 = "INSERT INTO INDIVID (KOEN, BLISTRUPID, FOEDT, FAM, SLGT) VALUES

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
			statementi2.setString(3, afQ(rs1.getString("EFTERNVN")));
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

			// gom
			if ("1".equals(rx)) {
				if (!sb.toString().isBlank()) {
					// UPDATE3 = "UPDATE FAMILIEBEGIVENHED SET DETALJER = ? WHERE ID = ?";

					statementu3.setString(1, sb.toString());
					statementu3.setInt(2, familieBegivenhedId);
					statementu3.executeUpdate();
				}

				sb = new StringBuilder();
				gom = individId;

				final KildeModel kModel = new KildeModel();
				kModel.setKbNr(rs1.getString("KBNR").trim());
				kModel.setAarInterval(rs1.getString("KILDE").trim());
				kModel.setKbDel(rs1.getString("KBDEL").trim());
				kModel.setTifNr(rs1.getString("TIFNR").trim());
				kModel.setOpslag(rs1.getString("OPSLAG").trim());
				kModel.setOpNr(rs1.getString("OPNR").trim());
				final int kildeId = kModel.insert(conn);

				// INSERT3 = "INSERT INTO FAMILIE (HUSFADER) VALUES(?)";";

				statementi3.setInt(1, gom);
				statementi3.executeUpdate();
				generatedKeys = statementi3.getGeneratedKeys();

				if (generatedKeys.next()) {
					familieId = generatedKeys.getInt(1);
				} else {
					familieId = 0;
				}
				generatedKeys.close();

				if (fader != null && !fader.isBlank()) {
					// INSERT1 = "INSERT INTO INDIVID (KOEN, BLISTRUPID, FOEDT, FAM, SLGT) VALUES

					statementi1.setString(1, rs1.getString("SEX").trim());
					statementi1.setString(2, rs1.getString("PID").trim());
					statementi1.setString(3, rs1.getString("FQODT").trim());
					statementi1.setString(4, rs1.getString("FAM"));
					statementi1.setString(5, rs1.getString("SLGT"));
					statementi1.executeUpdate();
					generatedKeys = statementi1.getGeneratedKeys();

					if (generatedKeys.next()) {
						faderId = generatedKeys.getInt(1);
					} else {
						faderId = 0;
					}
					generatedKeys.close();

					// INSERT3 = "INSERT INTO FAMILIE (HUSFADER) VALUES(?)";

					statementi3.setInt(1, faderId);
					statementi3.executeUpdate();
					generatedKeys = statementi3.getGeneratedKeys();

					if (generatedKeys.next()) {
						faderFamilieId = generatedKeys.getInt(1);
					} else {
						faderFamilieId = 0;
					}
					generatedKeys.close();

					// UPDATE1 = "UPDATE INDIVID SET FAMC = ? WHERE ID = ?";

					statementu1.setInt(1, faderFamilieId);
					statementu1.setInt(2, faderId);
					statementu1.executeUpdate();
				}

				// INSERT5 = "INSERT INTO FAMILIEBEGIVENHED (FAMILIEID, BEGTYPE, DATO,
				// BLISTRUPID, KILDEID, STEDNAVN, BEM) "

				statementi5.setInt(1, familieId);
				statementi5.setString(2, "Vielse");

				try {
					aar = rs1.getString("AAR").trim();
					statementi5.setString(3, aar + "-01-01");
				} catch (final Exception e) {
					statementi5.setString(3, "0001-01-01");
				}

				statementi5.setString(4, afQ(rs1.getString("PID")));
				statementi5.setInt(5, kildeId);
				statementi5.setString(6, "Blistrup, Holbo, Frederiksborg,");
				statementi5.setString(7, afQ(rs1.getString("BEM")));
				statementi5.executeUpdate();
				generatedKeys = statementi5.getGeneratedKeys();

				if (generatedKeys.next()) {
					familieBegivenhedId = generatedKeys.getInt(1);
				} else {
					familieBegivenhedId = 0;
				}
				generatedKeys.close();

				// brud
			} else if ("2".equals(rx)) {
				brud = individId;

				// UPDATE2 = "UPDATE FAMILIE SET HUSMODER = ? WHERE ID = ?"

				statementu2.setInt(1, brud);
				statementu2.setInt(2, familieId);
				statementu2.executeUpdate();

				if (fader != null && !fader.isBlank()) {
					// INSERT1 = "INSERT INTO INDIVID (KOEN, BLISTRUPID, FOEDT, FAM, SLGT) VALUES

					statementi1.setString(1, rs1.getString("SEX").trim());
					statementi1.setString(2, rs1.getString("PID").trim());
					statementi1.setString(3, rs1.getString("FQODT").trim());
					statementi1.setString(4, rs1.getString("FAM"));
					statementi1.setString(5, rs1.getString("SLGT"));
					statementi1.executeUpdate();
					generatedKeys.close();
					generatedKeys = statementi1.getGeneratedKeys();

					if (generatedKeys.next()) {
						faderId = generatedKeys.getInt(1);
					} else {
						faderId = 0;
					}
					generatedKeys.close();

					// INSERT3 = "INSERT INTO FAMILIE (HUSFADER) VALUES(?)";

					statementi3.setInt(1, faderId);
					statementi3.executeUpdate();
					generatedKeys = statementi3.getGeneratedKeys();

					if (generatedKeys.next()) {
						faderFamilieId = generatedKeys.getInt(1);
					} else {
						faderFamilieId = 0;
					}
					generatedKeys.close();

					// UPDATE1 = "UPDATE INDIVID SET FAMC = ? WHERE ID = ?";

					statementu1.setInt(1, faderFamilieId);
					statementu1.setInt(2, faderId);
					statementu1.executeUpdate();
				}
			} else {
				// forl1 eller forl2
				// INSERT4 = "INSERT INTO VIDNE (INDIVIDID, ROLLE, FAMILIEBEGIVENHEDID) VALUES

				statementi4.setInt(1, individId);
				statementi4.setString(2, rolle);
				statementi4.setInt(3, familieBegivenhedId);
				statementi4.executeUpdate();
			}
			sb.append(rolle + ": " + stdnavn + "\r\n4 CONT ");
		}

		conn.commit();
		conn.close();
		return taeller;
	}
}
