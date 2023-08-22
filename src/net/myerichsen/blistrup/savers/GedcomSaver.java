package net.myerichsen.blistrup.savers;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import net.myerichsen.blistrup.models.FamilieModel;
import net.myerichsen.blistrup.models.IndividModel;

/**
 * @author Michael Erichsen
 * @version 22. aug. 2023
 *
 */
public class GedcomSaver {
	private static final String SELECTI1 = "SELECT * FROM BLISTRUP.INDIVID";
	private static final String SELECTI2 = "SELECT * FROM BLISTRUP.INDIVIDBEGIVENHED WHERE ID = ?";
	private static final String SELECTF1 = "SELECT * FROM BLISTRUP.FAMILIE";
	private static final String SELECTF2 = "SELECT * FROM BLISTRUP.FAMILIEBEGIVENHED WHERE ID = ?";
	private static final String SELECTK1 = "SELECT * FROM BLISTRUP.KILDE";

	private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US);
	private static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
	private static OutputStreamWriter fw;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		args = new String[] { "C:\\Users\\michael\\BlistrupDB",
				"C:\\Users\\michael\\Documents\\The Master Genealogist v9\\Export\\Blistrup.ged", "Alex Hvidberg" };

		try {
			new GedcomSaver().save(args);
			System.out.println("Færdig!");
		} catch (final Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * @param dbPath
	 * @return
	 * @throws SQLException
	 */
	private Connection connect(String dbPath) throws SQLException {
		final Connection conn = DriverManager.getConnection("jdbc:derby:" + dbPath);
		return conn;

	}

	/**
	 * Worker method
	 *
	 * @param args
	 * @throws Exception
	 */
	public void save(String[] args) throws Exception {
		fw = new OutputStreamWriter(new FileOutputStream(args[1]));
		final Connection conn = connect(args[0]);
		writeHeader(args[1], args[2]);
		writeIndividuals(conn);
		writeFamilies(conn);
		writeSources(conn);
		writeTrailer();
		fw.flush();
		fw.close();
	}

	/**
	 * Put slashes around last part of a standard name
	 *
	 * @param name
	 * @return
	 */
	private String slashName(String name) {
		final String[] parts = name.split(" ");
		return name.replace(parts[parts.length - 1], "/" + parts[parts.length - 1] + "/");
	}

	/**
	 * Write all families
	 *
	 * @param conn
	 * @throws SQLException
	 * @throws IOException
	 */
	private void writeFamilies(Connection conn) throws SQLException, IOException {
		FamilieModel model;
		final PreparedStatement statement1 = conn.prepareStatement(SELECTF1);
		final PreparedStatement statement2 = conn.prepareStatement(SELECTF2);
		final ResultSet rs1 = statement1.executeQuery();
		ResultSet rs2;
		String type = "";

		while (rs1.next()) {
			model = FamilieModel.getData(conn, rs1);
			writeLine("0 @F" + model.getId() + "@ FAM");
			if (model.getFader() > 0) {
				writeLine("1 HUSB @I" + model.getFader() + "@");
			}

			if (model.getModer() > 0) {
				writeLine("1 WIFE @I" + model.getModer() + "@");
			}

			for (final IndividModel barn : model.getBoern()) {
				writeLine("1 CHIL @I" + barn.getId() + "@");
			}

			statement2.setInt(1, model.getId());
			rs2 = statement2.executeQuery();

			if (rs2.next()) {
				type = rs2.getString("BEGTYPE");

				if ("Vielse".equals(type)) {
					writeLine("1 MARR");
					writeLine("2 DATE " + rs2.getString("DATO"));
					writeLine("2 PLAC Blistrup, Holbo, Frederiksborg");
				} else if ("Folketælling".equals(type)) {
					writeLine("1 CENS");
					writeLine("2 DATE " + rs2.getString("DATO"));
					writeLine("2 PLAC " + rs2.getString("STEDNAVN"));
					writeLine("2 SOUR @S" + rs2.getString("KILDEID") + "@");
				}

			}

		}

		statement1.close();
		statement2.close();
	}

	/**
	 * Write GEDCOM header
	 *
	 * @param filename
	 * @param submitterName
	 * @throws IOException
	 */
	private void writeHeader(String filename, String submitterName) throws IOException {
		writeLine("0 HEAD");
		writeLine("1 SOUR BlistrupLokalhistorie");
		writeLine("2 VERS 1.0.0");
		writeLine("1 SUBM @SUB1@");
		writeLine("1 GEDC");
		writeLine("2 VERS 5.5");
		writeLine("2 FORM LINEAGE-LINKED");
		writeLine("1 DEST GED55");

		final LocalDateTime now = LocalDateTime.now();

		writeLine("1 DATE " + dateFormat.format(now).toUpperCase());
		writeLine("2 TIME " + timeFormat.format(now));
		writeLine("1 CHAR ANSEL");
		writeLine("1 FILE " + filename);
		writeLine("0 @SUB1@ SUBM");
		writeLine("1 NAME " + submitterName);
	}

	/**
	 * Write all individuals
	 *
	 * @param conn
	 *
	 * @throws SQLException
	 * @throws IOException
	 *
	 */
	private void writeIndividuals(Connection conn) throws SQLException, IOException {
		IndividModel model;
		String sex = "";
		final PreparedStatement statement1 = conn.prepareStatement(SELECTI1);
		final PreparedStatement statement2 = conn.prepareStatement(SELECTI2);
		final ResultSet rs1 = statement1.executeQuery();
		ResultSet rs2;
		String type = "";

		// TODO Handle name ", /<landsby>/"
		// TODO Konfirmation
		// TODO Begravelse

		while (rs1.next()) {
			model = IndividModel.getData(conn, rs1);
			writeLine("0 @I" + model.getId() + "@ INDI");
			writeLine("1 NAME " + slashName(model.getStdNavn()));
			sex = "M".equals(model.getKoen().toUpperCase()) ? "M" : "F";
			writeLine("1 SEX " + sex);

			if (model.getFamc() > 0) {
				writeLine("1 FAMC @F" + model.getFamc() + "@");
			}

			for (final Integer aFams : model.getFams()) {
				writeLine("1 FAMS @F" + aFams + "@");
			}

			if (model.getFoedt() != null && !model.getFoedt().isBlank()) {
				writeLine("1 BIRT");
				writeLine("2 DATE " + model.getFoedt().trim());
			}

			statement2.setInt(1, model.getId());
			rs2 = statement2.executeQuery();

			if (rs2.next()) {
				type = rs2.getString("BEGTYPE");

				if ("Dåb".equals(type)) {
					writeLine("1 MARR");
					writeLine("2 DATE " + rs2.getString("DATO"));
					writeLine("2 PLAC " + rs2.getString("STEDNAVN"));
					writeLine("2 SOUR @S" + rs2.getString("KILDEID") + "@");
				} else if ("Konfirmation".equals(type)) {
					writeLine("1 CONF");
					writeLine("2 DATE " + rs2.getString("DATO"));
					writeLine("2 PLAC " + rs2.getString("STEDNAVN"));
					writeLine("2 SOUR @S" + rs2.getString("KILDEID") + "@");
				} else if ("Begravelse".equals(type)) {
					writeLine("1 BURI");
					writeLine("2 DATE " + rs2.getString("DATO"));
					writeLine("2 PLAC " + rs2.getString("STEDNAVN"));
					writeLine("2 SOUR @S" + rs2.getString("KILDEID") + "@");
				}

			}

		}

		statement1.close();
	}

	/**
	 * @param string
	 * @throws IOException
	 */
	private void writeLine(String string) throws IOException {
		fw.write(string + "\n");
		System.out.println(string);
	}

	/**
	 * Write all sources
	 *
	 * @param conn
	 * @throws SQLException
	 * @throws IOException
	 */
	private void writeSources(Connection conn) throws SQLException, IOException {
		final PreparedStatement statement1 = conn.prepareStatement(SELECTK1);
		final ResultSet rs1 = statement1.executeQuery();
		StringBuilder sb;

		while (rs1.next()) {
			writeLine("0 @S" + rs1.getInt("ID") + "@ SOUR");
			sb = new StringBuilder();

			if (rs1.getString("KBNR") != null && !rs1.getString("KBNR").isBlank()) {
				sb.append("KBNR " + rs1.getString("KBNR").trim() + ", ");
			}

			if (rs1.getString("AARINTERVAL") != null && !rs1.getString("AARINTERVAL").isBlank()) {
				sb.append("AARINTERVAL " + rs1.getString("AARINTERVAL").trim() + ", ");
			}

			if (rs1.getString("KBDEL") != null && !rs1.getString("KBDEL").isBlank()) {
				sb.append("KBDEL " + rs1.getString("KBDEL") + ", ");
			}

			if (rs1.getString("TIFNR") != null && !rs1.getString("TIFNR").isBlank()) {
				sb.append("TIFNR " + rs1.getString("TIFNR").trim() + ", ");
			}

			if (rs1.getString("SIDE") != null && !rs1.getString("SIDE").isBlank()) {
				sb.append("SIDE " + rs1.getString("SIDE").trim() + ", ");
			}

			if (rs1.getString("OPSLAG") != null && !rs1.getString("OPSLAG").isBlank()) {
				sb.append("OPSLAG " + rs1.getString("OPSLAG").trim() + ", ");
			}

			writeLine("1 TITL " + sb.toString());
			writeLine("1 ABBR " + sb.toString());
		}

	}

	/**
	 * Write GEDCOM trailer
	 *
	 * @throws IOException
	 */
	private void writeTrailer() throws IOException {
		writeLine("0 TRLR");

	}

}
