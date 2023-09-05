package net.myerichsen.blistrup.loaders;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import net.myerichsen.blistrup.models.KildeModel;

/**
 * Indlæs begravelser
 *
 * @author Michael Erichsen
 * @version 30. aug. 2023
 *
 */
public class BegravelseLoader extends AbstractLoader {
	private static final String SELECT1 = "SELECT DISTINCT BEGIV FROM F9PERSONFAMILIEQ WHERE TYPE = 'D'";
	private static final String SELECT2 = "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'D' AND BEGIV = ? ORDER BY PID";

	private static final String INSERT1 = "INSERT INTO INDIVID (KOEN, BLISTRUPID, FOEDT) VALUES (?, ?, ?)";
	private static final String INSERT2 = "INSERT INTO PERSONNAVN (INDIVIDID, STDNAVN, FONETISKNAVN, PRIMAERNAVN) VALUES (?, ?, ?, 'TRUE')";
	private static final String INSERT4 = "INSERT INTO INDIVIDBEGIVENHED (INDIVIDID, ALDER, BEGTYPE, DATO, NOTE, ROLLE, BLISTRUPID, KILDEID, STEDNAVN, BEM, FOEDT) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
		final Connection conn = connect("BLISTRUP");
		final List<String> blistrupIdListe = new ArrayList<>();
		String rolle = "";
		PreparedStatement statement2;
		ResultSet generatedKeys;
		int individId = 0;
		int kildeId = 0;
		int individBegivenhedsId = 0;
		int husfaderId = 0;
		int husmoderId = 0;
		int familieId = 0;
		int doedId = 0;
		int taeller = 0;
		StringBuilder sb;
		String navn = "";
		String fader = "";
		String moder = "";
		String faelle = "";
		String stdnavn = "";
		int aefaelleId = 0;
		String koen = "";

		// SELECT1 = "SELECT DISTINCT BEGIV FROM F9PERSONFAMILIEQ WHERE TYPE = 'D'

		PreparedStatement statement1 = conn.prepareStatement(SELECT1);
		ResultSet rs1 = statement1.executeQuery();

		while (rs1.next()) {
			blistrupIdListe.add(rs1.getString("BEGIV"));
		}

		for (final String blistrupId : blistrupIdListe) {
			sb = new StringBuilder();

			// SELECT2 = "SELECT * FROM F9PERSONFAMILIEQ WHERE TYPE = 'D' AND BEGIV = ?
			// ORDER BY PID";

			statement1 = conn.prepareStatement(SELECT2);
			statement1.setString(1, blistrupId);
			rs1 = statement1.executeQuery();

			while (rs1.next()) {
				fader = "";
				moder = "";
				rolle = afQ(rs1.getString("ROLLE"));
				navn = afQ(rs1.getString("NAVN"));
				sb.append(rolle + ": " + navn + ", \r\n4 CONT ");
				koen = rs1.getString("SEX").trim();

				// INSERT1 = "INSERT INTO INDIVID (KOEN, BLISTRUPID, FOEDT) VALUES (?, ?, ?)";

				statement2 = conn.prepareStatement(INSERT1, Statement.RETURN_GENERATED_KEYS);
				statement2.setString(1, koen);
				statement2.setString(2, rs1.getString("PID").trim());
				statement2.setString(3, rs1.getString("FQODT").trim());
				statement2.executeUpdate();
				generatedKeys = statement2.getGeneratedKeys();

				if (generatedKeys.next()) {
					individId = generatedKeys.getInt(1);
				} else {
					individId = 0;
				}

				generatedKeys.close();

				// INSERT2 = "INSERT INTO PERSONNAVN (INDIVIDID, STDNAVN, FONETISKNAVN,
				// PRIMAERNAVN) VALUES

				statement2 = conn.prepareStatement(INSERT2);
				statement2.setInt(1, individId);

				stdnavn = afQ(rs1.getString("STD_NAVN"));
				statement2.setString(2, cleanName(stdnavn));
				try {
					statement2.setString(3, fonkod.generateKey(stdnavn).trim());
				} catch (final Exception e) {
					statement2.setString(3, "");
				}

				statement2.executeUpdate();

				taeller++;

				// død, barn, far, mor, æfælle
				if ("død".equals(rolle)) {
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
					// NOTE, ROLLE, BLISTRUPID, KILDEID, STEDNAVN, BEM, FOEDT) "

					statement2 = conn.prepareStatement(INSERT4, Statement.RETURN_GENERATED_KEYS);
					statement2.setInt(1, individId);

					try {
						statement2.setInt(2, Integer.parseInt(rs1.getString("ALDER").trim()));
					} catch (final Exception e) {
						statement2.setInt(2, 0);
					}

					statement2.setString(3, "Begravelse");
					statement2.setString(4, rs1.getString("AAR").trim() + "-01-01");
					fader = afQ(rs1.getString("FADER"));
					fader = fader.length() > 0 ? "Fader: " + fader : "";
					moder = afQ(rs1.getString("MODER"));
					moder = moder.length() > 0 ? "Moder: " + moder : "";
					faelle = afQ(rs1.getString("FQELLE"));
					faelle = faelle.length() > 0 ? "Ægtefælle: " + faelle : "";
					statement2.setString(5, (fader + " " + moder + " " + faelle).trim());
					statement2.setString(6, rolle);
					statement2.setString(7, afQ(rs1.getString("BEGIV")));
					statement2.setInt(8, kildeId);
					statement2.setString(9, formatPlaceName(afQ(rs1.getString("STEDNAVN"))));
					statement2.setString(10, afQ(rs1.getString("BEM")));
					statement2.setString(11, rs1.getString("FQODT").trim());
					statement2.executeUpdate();
					generatedKeys = statement2.getGeneratedKeys();

					if (generatedKeys.next()) {
						individBegivenhedsId = generatedKeys.getInt(1);
					} else {
						individBegivenhedsId = 0;
					}
					generatedKeys.close();

				} else if ("far".equals(rolle)) {
					husfaderId = individId;

					// INSERT5 = "INSERT INTO VIDNE (INDIVIDID, ROLLE, INDIVIDBEGIVENHEDID) VALUES

					statement2.close();
					statement2 = conn.prepareStatement(INSERT5);
					statement2.setInt(1, husfaderId);
					statement2.setString(2, rolle);
					statement2.setInt(3, individBegivenhedsId);
					statement2.executeUpdate();

					// INSERT6 = "INSERT INTO FAMILIE (HUSFADER) VALUES(?)";

					statement2 = conn.prepareStatement(INSERT6, Statement.RETURN_GENERATED_KEYS);
					statement2.setInt(1, husfaderId);
					statement2.executeUpdate();
					generatedKeys.close();
					generatedKeys = statement2.getGeneratedKeys();

					if (generatedKeys.next()) {
						familieId = generatedKeys.getInt(1);
					} else {
						familieId = 0;
					}
					generatedKeys.close();

					// UPDATE1 = "UPDATE INDIVID SET FAMC = ? WHERE ID = ?";

					statement2 = conn.prepareStatement(UPDATE1);
					statement2.setInt(1, familieId);
					statement2.setInt(2, doedId);
					statement2.executeUpdate();

				} else if ("mor".equals(rolle)) {
					husmoderId = individId;

					// INSERT5 = "INSERT INTO VIDNE (INDIVIDID, ROLLE, INDIVIDBEGIVENHEDID) VALUES

					statement2.close();
					statement2 = conn.prepareStatement(INSERT5);
					statement2.setInt(1, husmoderId);
					statement2.setString(2, rolle);
					statement2.setInt(3, individBegivenhedsId);
					statement2.executeUpdate();

					if (familieId == 0) {

						// INSERT7 = "INSERT INTO FAMILIE (HUSMODER) VALUES(?)";

						statement2 = conn.prepareStatement(INSERT7, Statement.RETURN_GENERATED_KEYS);
						statement2.setInt(1, husfaderId);
						statement2.executeUpdate();
						generatedKeys.close();
						generatedKeys = statement2.getGeneratedKeys();

						if (generatedKeys.next()) {
							familieId = generatedKeys.getInt(1);
						} else {
							familieId = 0;
						}
						generatedKeys.close();

						// UPDATE1 = "UPDATE INDIVID SET FAMC = ? WHERE ID = ?";

						statement2 = conn.prepareStatement(UPDATE1);
						statement2.setInt(1, familieId);
						statement2.setInt(2, doedId);
					} else {

						// UPDATE3 = "UPDATE FAMILIE SET HUSMODER = ? WHERE ID = ?";

						statement2.close();
						statement2 = conn.prepareStatement(UPDATE3);
						statement2.setInt(1, husmoderId);
						statement2.setInt(2, familieId);
					}
					statement2.executeUpdate();

				} else if ("æfælle".equals(rolle)) {
					aefaelleId = individId;

					// INSERT5 = "INSERT INTO VIDNE (INDIVIDID, ROLLE, INDIVIDBEGIVENHEDID) VALUES

					statement2.close();
					statement2 = conn.prepareStatement(INSERT5);
					statement2.setInt(1, husfaderId);
					statement2.setString(2, rolle);
					statement2.setInt(3, individBegivenhedsId);
					statement2.executeUpdate();

					// INSERT8 = "INSERT INTO FAMILIE (HUSFADER, HUSMODER) VALUES(?, ?)";

					statement2 = conn.prepareStatement(INSERT8);

					if ("m".equals(koen)) {
						statement2.setInt(1, aefaelleId);
						statement2.setInt(2, doedId);
					} else {
						statement2.setInt(1, doedId);
						statement2.setInt(2, aefaelleId);
					}

					statement2.executeUpdate();
				}
			}

			// UPDATE2 = "UPDATE INDIVIDBEGIVENHED SET DETALJER = ? WHERE ID = ?";

			statement2 = conn.prepareStatement(UPDATE2);
			statement2.setString(1, afQ(sb.toString()));
			statement2.setInt(2, individBegivenhedsId);
			statement2.executeUpdate();
			statement2.close();
		}

		conn.commit();
		conn.close();
		return taeller;
	}
}
