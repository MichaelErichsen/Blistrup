package net.myerichsen.blistrup.loaders;

import java.sql.Connection;
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
 * @version 9. sep. 2023
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
	 * Tilføj kommaer efter stednavn for at passe i stednavnestrukturen
	 *
	 * @param placeName
	 * @return
	 */
	protected String formatPlaceName(String placeName) {
		final String[] parts = placeName.split(",");

		switch (parts.length) {
		case 1: {
			placeName = placeName + ",,,";
			break;
		}
		case 2: {
			placeName = placeName + ",,";
			break;
		}
		case 3: {
			placeName = placeName + ",";
			break;
		}
		}
		return placeName;

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

//	/**
//	 * Indsæt folketællingens kilde
//	 *
//	 * @param conn
//	 * @return
//	 *
//	 * @throws SQLException
//	 */
//	protected int insertSource(Connection conn, String aar) throws SQLException {
//		final KildeModel kModel = new KildeModel();
//		kModel.setKbNr("0");
//		kModel.setAarInterval(aar);
//		return kModel.insert(conn);
//	}

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
