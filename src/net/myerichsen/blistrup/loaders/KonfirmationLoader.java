package net.myerichsen.blistrup.loaders;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import net.myerichsen.blistrup.models.KildeModel;

/**
 * L�s konfirmationsdata fra grundtabellen ind i GEDCOM-tabeller
 *
 * @author Michael Erichsen
 * @version 5. okt. 2023
 *
 */
public class KonfirmationLoader extends AbstractLoader {
	private static final String SELECT1 = "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'B' ORDER BY BEGIV, RX";

	private static final String INSERT1 = "INSERT INTO INDIVID (KOEN, FOEDT, FAM, SLGT) VALUES (?, ?, ?, ?)";
	private static final String INSERT2 = "INSERT INTO PERSONNAVN (INDIVIDID, FORNAVN, EFTERNAVN, PRIMAERNAVN, FONETISKNAVN, STDNAVN) "
			+ "VALUES (?, ?, ?, ?, ?, ?)";
	private static final String INSERT3 = "INSERT INTO INDIVIDBEGIVENHED (INDIVIDID, ALDER, BEGTYPE, DATO, ROLLE, KILDEID, STEDNAVN, BEM) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String INSERT4 = "INSERT INTO VIDNE (INDIVIDID, ROLLE, INDIVIDBEGIVENHEDID) VALUES (?, ?, ?)";
	private static final String INSERT5 = "INSERT INTO FAMILIE (HUSFADER) VALUES(?)";

	private static final String UPDATE1 = "UPDATE INDIVID SET FAMC = ? WHERE ID = ?";
	private static final String UPDATE2 = "UPDATE INDIVIDBEGIVENHED SET DETALJER = ? WHERE ID = ?";
	private static final String UPDATE3 = "UPDATE FAMILIE SET HUSMODER = ? WHERE ID = ?";

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
		int kildeId = 0;
		int individBegivenhedsId = 0;
		int familieId = 0;
		int barnId = 0;
		int taeller = 0;
		StringBuilder sb = new StringBuilder();
		String navn = "";
		String stdnavn = "";
		String rx = "";
		int alder = 0;
		String foedtDato = "";
		String doebtDato = "";
		String konf = "";

		final Connection conn = connect("BLISTRUP");
		final PreparedStatement statements1 = conn.prepareStatement(SELECT1);
		final PreparedStatement statementi1 = conn.prepareStatement(INSERT1, Statement.RETURN_GENERATED_KEYS);
		final PreparedStatement statementi2 = conn.prepareStatement(INSERT2);
		final PreparedStatement statementi3 = conn.prepareStatement(INSERT3, Statement.RETURN_GENERATED_KEYS);
		final PreparedStatement statementi4 = conn.prepareStatement(INSERT4);
		final PreparedStatement statementi5 = conn.prepareStatement(INSERT5, Statement.RETURN_GENERATED_KEYS);
		final PreparedStatement statementu1 = conn.prepareStatement(UPDATE1);
		final PreparedStatement statementu2 = conn.prepareStatement(UPDATE2);
		final PreparedStatement statementu3 = conn.prepareStatement(UPDATE3);

		// SELECT1 = "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'B' ORDER BY BEGIV,
		// RX";
		final ResultSet rs1 = statements1.executeQuery();

		while (rs1.next()) {
			// INSERT1 = "INSERT INTO INDIVID (KOEN, FOEDT, FAM, SLGT) VALUES

			rx = rs1.getString("RX");
			rolle = rs1.getString("ROLLE").trim();
			navn = rs1.getString("NAVN").trim();

			statementi1.setString(1, rs1.getString("SEX").trim());

			if (!rx.equals("1")) {
				statementi1.setString(2, getFoedtDoebtDato(rs1));
			} else {
				statementi1.setString(2, "");
			}

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

			// Barn eller konf
			if ("1".equals(rx)) {
				// UPDATE2 = "UPDATE INDIVIDBEGIVENHED SET DETALJER = ? WHERE ID = ?";

				statementu2.setString(1, afQ(sb.toString()));
				statementu2.setInt(2, individBegivenhedsId);
				statementu2.executeUpdate();

				sb = new StringBuilder();
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
				// ROLLE, KILDEID, STEDNAVN, BEM) "

				statementi3.setInt(1, individId);

				try {
					alder = Integer.parseInt(rs1.getString("AAR")) - Integer.parseInt(rs1.getString("FQODT"));
				} catch (final Exception e1) {
					alder = 0;
				}
				statementi3.setString(2, Integer.toString(alder));
				statementi3.setString(3, "Konfirmation");
				konf = rs1.getString("KONF");

				if (konf != null && !konf.isBlank() && konf.matches("[0-9]+")) {
					konf = dashDato(konf);
					try {
						statementi3.setDate(4, Date.valueOf(konf));
					} catch (final Exception e) {
						statementi3.setDate(4, Date.valueOf(konf.substring(0, 4) + "-01-01"));
					}
				} else {
					statementi3.setDate(4, Date.valueOf(rs1.getString("AAR") + "-01-01"));
				}

				statementi3.setString(5, rolle);
				statementi3.setInt(6, kildeId);
				statementi3.setString(7, fixStedNavn(rs1.getString("STEDNAVN")));
				statementi3.setString(8, afQ(rs1.getString("BEM")));
				statementi3.executeUpdate();
				generatedKeys = statementi3.getGeneratedKeys();

				if (generatedKeys.next()) {
					individBegivenhedsId = generatedKeys.getInt(1);
				} else {
					individBegivenhedsId = 0;
				}
				generatedKeys.close();

				// Inds�t f�dsel
				// INSERT3 = "INSERT INTO INDIVIDBEGIVENHED (INDIVIDID, ALDER, BEGTYPE, DATO,
				// ROLLE, KILDEID, STEDNAVN, BEM
				foedtDato = rs1.getString("FQODTDATO");

				if (foedtDato != null && !foedtDato.isBlank() && foedtDato.matches("[0-9]+")) {
					statementi3.setInt(1, individId);
					statementi3.setString(2, "0");
					statementi3.setString(3, "F�dsel");
					foedtDato = dashDato(foedtDato);
					try {
						statementi3.setDate(4, Date.valueOf(foedtDato));
					} catch (final Exception e) {
						statementi3.setDate(4, Date.valueOf(foedtDato.substring(0, 4) + "-01-01"));
					}
					statementi3.setString(5, "Barn");
					statementi3.setInt(6, kildeId);
					statementi3.setString(7, "");
					statementi3.setString(8, "");
					statementi3.executeUpdate();
				}

				// Inds�t d�b
				// INSERT3 = "INSERT INTO INDIVIDBEGIVENHED (INDIVIDID, ALDER, BEGTYPE, DATO,
				// ROLLE, KILDEID, STEDNAVN, BEM
				doebtDato = rs1.getString("DQOBT");

				if (doebtDato != null && !doebtDato.isBlank() && doebtDato.matches("[0-9]+")) {
					statementi3.setInt(1, individId);
					statementi3.setString(2, "0");
					statementi3.setString(3, "D�b");
					doebtDato = dashDato(doebtDato);
					try {
						statementi3.setDate(4, Date.valueOf(doebtDato));
					} catch (final Exception e) {
						statementi3.setDate(4, Date.valueOf(doebtDato.substring(0, 4) + "-01-01"));
					}
					statementi3.setString(5, "Barn");
					statementi3.setInt(6, kildeId);
					statementi3.setString(7, fixStedNavn(rs1.getString("DQOBTSTED")));
					statementi3.setString(8, "");
					statementi3.executeUpdate();
				}

				// Far eller stedfar
			} else if ("2".equals(rx)) {
				// INSERT4 = "INSERT INTO VIDNE (INDIVIDID, ROLLE, INDIVIDBEGIVENHEDID) VALUES

				statementi4.setInt(1, individId);
				statementi4.setString(2, rolle);
				statementi4.setInt(3, individBegivenhedsId);
				statementi4.executeUpdate();

				// INSERT5 = "INSERT INTO FAMILIE (HUSFADER) VALUES(?)";

				statementi5.setInt(1, individId);
				statementi5.executeUpdate();
				generatedKeys.close();
				generatedKeys = statementi5.getGeneratedKeys();

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

				// mor eller Hans Kone
			} else if ("3".equals(rx)) {
				// INSERT4 = "INSERT INTO VIDNE (INDIVIDID, ROLLE, INDIVIDBEGIVENHEDID) VALUES

				statementi4.setInt(1, individId);
				statementi4.setString(2, rolle);
				statementi4.setInt(3, individBegivenhedsId);
				statementi4.executeUpdate();

				// UPDATE3 = "UPDATE FAMILIE SET HUSMODER = ? WHERE ID = ?";

				statementu3.setInt(1, individId);
				statementu3.setInt(2, familieId);
				statementu3.executeUpdate();

			}
			sb.append(rolle + ": " + navn + "\r\n4 CONT ");

		}

		conn.commit();
		conn.close();
		return taeller;
	}
}
