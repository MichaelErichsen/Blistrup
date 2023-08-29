package net.myerichsen.blistrup.loaders;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Abstrakt overklasse for Blistrup loader programmer
 *
 * @author Michael Erichsen
 * @version 25. aug. 2023
 *
 */

public abstract class AbstractLoader {
	private static final String SET_SCHEMA = "SET SCHEMA = 'BLISTRUP'";
	private static final String[] steder = new String[] { "Aggebo", "Alme", "Alume", "Ammendrup", "Annisse",
			"Bakkebjerg", "Bannebjerg", "Blidstrup", "Blistrup", "Esb�nderup", "Esrum", "Fjellenstrup", "Frederiksborg",
			"Gilleleje", "Gillem�lle", "Gr�sted", "Gr�sted_Overdrev", "Haagendrup", "Helsing�r", "Hemmingstrup",
			"Hesselbjerg", "H�jelt", "Huler�d", "Huseby", "Kolsb�k", "Ludsh�j", "Maarum", "Orne", "Paarup",
			"Pr�stegaard", "Pr�stegaarden", "Raageleje", "Raml�se", "Smidstrup", "Str.Esb�", "Taagerup", "Tibirke",
			"Udsholt", "Unnerup", "Vejby" };

	/**
	 * Overs�t til �, � og �
	 *
	 * @param input
	 * @return
	 */
	protected String afQ(String input) {
		return input.replace("Qo", "�").replace("Qe", "�").replace("Qa", "a").trim();
	}

	/**
	 * Fjern foranstillede cifre og stednavn. S�t skr�streger omkring sidste del af
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
	protected Connection connect() throws SQLException {
		final Connection conn = DriverManager.getConnection("jdbc:derby:C:\\Users\\michael\\BlistrupDB");
		final PreparedStatement statement = conn.prepareStatement(SET_SCHEMA);
		statement.execute();
		return conn;
	}

	/**
	 * Tilf�j kommaer efter stednavn for at passe i stednavnestrukturen
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

}
