package net.myerichsen.blistrup.loaders;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import net.myerichsen.blistrup.models.IndividModel;
import net.myerichsen.blistrup.models.KildeModel;
import net.myerichsen.blistrup.util.Fonkod;

/**
 * Abstrakt overklasse for Blistrup loader programmer
 *
 * @author Michael Erichsen
 * @version 7. okt. 2023
 *
 */

public abstract class AbstractLoader {
	protected static class IndividData {
		protected int id = 0;
		protected String stillingIHusstanden = "";
		protected String detaljer;
		protected IndividModel iModel;

		/**
		 * Constructor
		 *
		 * @param id
		 * @param stillingIHusstanden
		 * @param detaljer
		 * @param iModel
		 */
		public IndividData(int id, String stillingIHusstanden, String detaljer, IndividModel iModel) {
			this.id = id;
			this.stillingIHusstanden = stillingIHusstanden;
			this.detaljer = detaljer;
			this.iModel = iModel;
		}

		/**
		 * @return the detaljer
		 */
		public String getDetaljer() {
			return detaljer;
		}

		/**
		 * @return the id
		 */
		public int getId() {
			return id;
		}

		/**
		 * @return the iModel
		 */
		public IndividModel getiModel() {
			return iModel;
		}

		/**
		 * @return the stillingIHusstanden
		 */
		public String getStillingIHusstanden() {
			return stillingIHusstanden;
		}

		/**
		 * @param iModel the iModel to set
		 */
		public void setiModel(IndividModel iModel) {
			this.iModel = iModel;
		}

		/**
		 * @param stillingIHusstanden the stillingIHusstanden to set
		 */
		public void setStillingIHusstanden(String stillingIHusstanden) {
			this.stillingIHusstanden = stillingIHusstanden;
		}

	}

	private static final String SET_SCHEMA = "SET SCHEMA = ?";
	private static final String[] steder = new String[] { "Aggebo", "Alme", "Alume", "Ammendrup", "Annisse",
			"Bakkebjerg", "Bannebjerg", "Blidstrup", "Blistrup", "Esbønderup", "Esrum", "Fjellenstrup", "Frederiksborg",
			"Gilleleje", "Gillemølle", "Græsted", "Græsted_Overdrev", "Haagendrup", "Helsingør", "Hemmingstrup",
			"Hesselbjerg", "Højelt", "Hulerød", "Huseby", "Kolsbæk", "Ludshøj", "Maarum", "Orne", "Paarup",
			"Præstegaard", "Præstegaarden", "Raageleje", "Ramløse", "Smidstrup", "Str.Esbø", "Taagerup", "Tibirke",
			"Udsholt", "Unnerup", "Vejby" };
	protected static final Fonkod fonkod = new Fonkod();

	/**
	 * Oversæt til æ, ø og å
	 *
	 * @param input
	 * @return
	 */
	protected String afQ(String input) {
		return input.replace("QO", "Ø").replace("QE", "Æ").replace("QA", "Å").replace("Qo", "ø").replace("Qe", "æ")
				.replace("Qa", "å").trim();
	}

	/**
	 * Fjern foranstillede cifre og stednavn. Sæt skråstreger omkring sidste del af
	 * et navn
	 *
	 * @param name
	 * @return
	 */
	protected String cleanName(String name) {
		// Fjern stednavne
		for (final String sted : steder) {
			if (name.contains(sted)) {
				name = name.replace(sted, "");
			}
		}

		// Fjern foranstillede cifre
		String[] parts = name.split(",");
		int len = parts.length;

		if (len > 1) {
			if (parts[0].trim().toLowerCase().matches("[0-9]*")) {
				name = name.replace(parts[0] + ",", "");
			}
		}

		name = name.replace(",", "").replace(" af ", " ").replace(" og ", " ").replace(" til ", " ");
		parts = name.split(" ");
		len = parts.length;
		return name.replace(parts[len - 1], "/" + parts[len - 1] + "/");
	}

	/**
	 * Forbind til databasen
	 *
	 * @return conn forbindelse
	 * @throws SQLException
	 */
	protected Connection connect(String schema) throws SQLException {
		final Connection conn = DriverManager.getConnection("jdbc:derby:C:\\Users\\michael\\BlistrupDB");
		conn.setAutoCommit(false);
		final PreparedStatement statement = conn.prepareStatement(SET_SCHEMA);
		statement.setString(1, schema);
		statement.execute();
		return conn;
	}

	/**
	 * @param doebtDato
	 * @return
	 */
	public String dashDato(String doebtDato) {
		doebtDato = doebtDato.substring(0, 4) + "-" + doebtDato.substring(4, 6) + "-" + doebtDato.substring(6, 8);
		return doebtDato;
	}

	/**
	 * Find den fulde dato fra ÅR og DATO kolonnerne
	 *
	 * @param rs
	 * @return
	 * @throws SQLException
	 * @throws NumberFormatException
	 */
	public Date findFuldDato(final ResultSet rs) throws SQLException, NumberFormatException {
		String formatted;
		String dato;
		Date fullDate;
		dato = rs.getString("DATO");
		if (dato != null && !dato.isBlank()) {
			formatted = String.format("%04d", Integer.parseInt(dato.trim()));
		} else {
			formatted = "0101";
		}
		try {
			formatted = rs.getString("ÅR") + "-" + formatted.substring(0, 2) + "-" + formatted.substring(2, 4);
		} catch (final SQLException e1) {
			formatted = rs.getString("AAR") + "-" + formatted.substring(0, 2) + "-" + formatted.substring(2, 4);
		}
		try {
			fullDate = Date.valueOf(formatted);
		} catch (final Exception e) {
			try {
				fullDate = Date.valueOf(rs.getString("ÅR") + "-01-01");
			} catch (final SQLException e1) {
				fullDate = Date.valueOf(rs.getString("AAR") + "-01-01");
			}
		}
		return fullDate;
	}

	/**
	 * @param stedNavn
	 * @return
	 */
	protected String fixStedNavn(String stedNavn) {
		stedNavn = afQ(stedNavn);

		if (stedNavn.contains("Blistrup") || stedNavn.contains("Blidstrup")) {
			stedNavn = stedNavn + ", Holbo, Frederiksborg,";
		} else if (stedNavn.contains("Bakkebjerg") || stedNavn.contains("Hesselbjerg") || stedNavn.contains("Højelt")
				|| stedNavn.contains("Kolsbæk") || stedNavn.contains("Ludshøj") || stedNavn.contains("Rågeleje")
				|| stedNavn.contains("Raageleje") || stedNavn.contains("Smidstrup") || stedNavn.contains("Udsholt")) {
			stedNavn = stedNavn.replace(",", "") + ", Blistrup, Holbo, Frederiksborg,";
		} else {
			stedNavn = stedNavn + ",,,";
		}
		return stedNavn;
	}

	/**
	 * Formatter en tabelrække som tekst for FT 1771
	 *
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	protected String get1771TableRow(ResultSet rs) throws SQLException {
		final ResultSetMetaData rsmd = rs.getMetaData();
		final StringBuilder sb = new StringBuilder();
		final int columnCount = rsmd.getColumnCount();

		for (int i = 1; i < columnCount + 1; i++) {
			if (rs.getString(i) != null) {
				sb.append("4 CONT " + rsmd.getColumnName(i).toLowerCase().trim() + ": ");
				sb.append(rs.getString(i).trim() + "\r\n");
			}
		}

		String streng = sb.toString();
		streng = streng.substring(0, streng.length() - 2);
		return streng;
	}

	/**
	 * @param rs1
	 * @return
	 * @throws SQLException
	 * @throws NumberFormatException
	 */
	public String getFoedtDoebtDato(final ResultSet rs1) throws SQLException, NumberFormatException {
		String foedt = rs1.getString("FQODT");
		if (foedt != null && !foedt.isBlank()) {
			String doebt = rs1.getString("DQOBT");
			if (doebt != null && !doebt.isBlank() && doebt.matches("[0-9]+")) {
				doebt = String.format("%04d", Integer.parseInt(doebt.trim()));
				foedt = foedt + doebt;
			} else {
				foedt = foedt + "-01-01";
			}
		}
		return foedt;
	}

	/**
	 * Formatter en tabelrække som tekst
	 *
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	protected String getTableRow(ResultSet rs) throws SQLException {
		final ResultSetMetaData rsmd = rs.getMetaData();
		final StringBuilder sb = new StringBuilder();
		final int columnCount = rsmd.getColumnCount();

		for (int i = 1; i < columnCount + 1; i++) {
			if (rs.getString(i) != null && !rs.getString(i).isBlank()) {
				sb.append(rs.getString(i).trim() + ", ");
			}
		}
		return "4 CONT " + sb.toString();
	}

	/**
	 * Indsæt kilde
	 *
	 * @param conn
	 * @return
	 *
	 * @throws SQLException
	 */
	protected int insertSource(Connection conn, KildeModel kModel) throws SQLException {
		return kModel.insert(conn);
	}

	/**
	 * Undersøg, om dette er et barn
	 *
	 * @param stillingIHusstanden
	 * @return
	 */
	protected boolean testBoern(String stillingIHusstanden) {
		final String[] parts = stillingIHusstanden.toLowerCase().split(" ");

		for (final String string : parts) {
			if ("barn".equals(string) || "søn".equals(string) || "datter".equals(string)) {
				return true;
			}
		}

		return false;
	}
}
