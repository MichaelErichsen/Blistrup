package net.myerichsen.blistrup.savers;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.myerichsen.blistrup.models.FamilieModel;
import net.myerichsen.blistrup.models.IndividModel;

/**
 * Udskriv Blistrup databasen som GEDCOM
 *
 * @author Michael Erichsen
 * @version 29. aug. 2023
 *
 */
public class GedcomSaver {
	/**
	 * Privat klasse, der repræsenterer en kildehenvisning
	 *
	 * @author Michael Erichsen
	 * @version 27. aug. 2023
	 *
	 */
	private static class SourceReference {
		private String id = "";
		private String title = "";
		private final List<String> aliases = new ArrayList<>();

		/**
		 * @param id
		 */
		public void addAlias(String id) {
			aliases.add(id);
		}

		/**
		 * @return the id
		 */
		public String getId() {
			return id;
		}

		/**
		 * @return the title
		 */
		public String getTitle() {
			return title;
		}

		/**
		 * @param id the id to set
		 */
		public void setId(String id) {
			this.id = id;
		}

		/**
		 * @param title the title to set
		 */
		public void setTitle(String title) {
			this.title = title;
		}
	}

	private static final String SELECTI1 = "SELECT * FROM BLISTRUP.INDIVID";
	private static final String SELECTI2 = "SELECT * FROM BLISTRUP.INDIVIDBEGIVENHED WHERE INDIVIDID = ?";
	private static final String SELECTF1 = "SELECT * FROM BLISTRUP.FAMILIE";
	private static final String SELECTF2 = "SELECT * FROM BLISTRUP.FAMILIEBEGIVENHED WHERE FAMILIEID = ?";
	private static final String SELECTK1 = "SELECT * FROM BLISTRUP.KILDE";
	private static final String SELECTK2 = "SELECT * FROM BLISTRUP.KILDE WHERE ID = ?";
	private static final String SELECTV1 = "SELECT * FROM BLISTRUP.VIDNE WHERE INDIVIDID = ?";
	private static final String SELECTV2 = "SELECT * FROM BLISTRUP.INDIVIDBEGIVENHED WHERE ID = ?";
	private static final String SELECTV3 = "SELECT * FROM BLISTRUP.FAMILIEBEGIVENHED WHERE ID = ?";
	private static final String FOUR_DIGITS = "[0-9]{4}";
	private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US);
	private static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

	private static OutputStreamWriter fw;
	private static final List<SourceReference> referenceList = new ArrayList<>();
	private static PreparedStatement statementi1;
	private static PreparedStatement statementi2;
	private static PreparedStatement statementf1;
	private static PreparedStatement statementf2;
	private static PreparedStatement statementk1;
	private static PreparedStatement statementk2;
	private static PreparedStatement statementv1;
	private static PreparedStatement statementv2;
	private static PreparedStatement statementv3;

	/**
	 * Indgangspunkt
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		args = new String[] { "C:\\Users\\michael\\BlistrupDB",
				"C:\\Users\\michael\\Documents\\The Master Genealogist v9\\Export\\Blistrup.ged", "Alex Hvidberg" };

		try {
			new GedcomSaver().save(args);
		} catch (final Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Forbind til databasen
	 *
	 * @param dbPath
	 * @return
	 * @throws SQLException
	 */
	private Connection connect(String dbPath) throws SQLException {
		final Connection conn = DriverManager.getConnection("jdbc:derby:" + dbPath);
		statementi1 = conn.prepareStatement(SELECTI1);
		statementi2 = conn.prepareStatement(SELECTI2);
		statementf1 = conn.prepareStatement(SELECTF1);
		statementf2 = conn.prepareStatement(SELECTF2);
		statementk1 = conn.prepareStatement(SELECTK1);
		statementk2 = conn.prepareStatement(SELECTK2);
		statementv1 = conn.prepareStatement(SELECTV1);
		statementv2 = conn.prepareStatement(SELECTV2);
		statementv3 = conn.prepareStatement(SELECTV3);
		return conn;

	}

	/**
	 * Find tekst til kilde
	 *
	 * @param kildeId
	 * @return
	 * @throws SQLException
	 */
	private String findTextFromSource(String kildeId) throws SQLException {
		final StringBuilder sb = new StringBuilder();

		statementk2.setInt(1, Integer.parseInt(kildeId));
		final ResultSet rs1 = statementk2.executeQuery();

		if (rs1.next()) {

			if (rs1.getString("KBDEL") != null && !rs1.getString("KBDEL").isBlank()) {
				sb.append("KBDEL " + rs1.getString("KBDEL").trim() + ", ");
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

		}

		return sb.toString();
	}

	/**
	 * Hovedmetode
	 *
	 * @param args Database path, GEDCOM path, Submitter
	 * @throws Exception
	 */
	public void save(String[] args) throws Exception {
		fw = new OutputStreamWriter(new FileOutputStream(args[1]));
		final Connection conn = connect(args[0]);
		writeHeader(args[1], args[2]);
		System.out.println("Udskrevet header");
		writeIndividuals(conn);
		System.out.println("Udskrevet individer");
		writeFamilies(conn);
		System.out.println("Udskrevet familier");
		writeSources();
		System.out.println("Udskrevet kilder");
		conn.close();
		writeTrailer();
		System.out.println("Udskrevet trailer");
		System.out.println("Færdig!");
		fw.flush();
		fw.close();
	}

	/**
	 * Formatter dato til GEDCOM format
	 *
	 * @param rs2
	 * @throws IOException
	 * @throws SQLException
	 */
	public void writeDate(ResultSet rs) throws IOException, SQLException {
		final LocalDate localDate = LocalDate.parse(rs.getString("DATO"));
		final String date = dateFormat.format(localDate).toUpperCase();
		if (!"01 JAN 1970".equals(date)) {
			writeLine("2 DATE " + date);
		}
	}

	/**
	 * Udskriv all familier
	 *
	 * @param conn
	 * @throws SQLException
	 * @throws IOException
	 */
	private void writeFamilies(Connection conn) throws SQLException, IOException {
		FamilieModel model;
		final ResultSet rs1 = statementf1.executeQuery();
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

			writeFamilyEvent(model.getId(), true);

		}

		statementf1.close();
		statementf2.close();
	}

	/**
	 * Udskriv alle familiebegivenheder for en familie
	 *
	 * @param id
	 * @param primary
	 * @throws SQLException
	 * @throws IOException
	 */
	public void writeFamilyEvent(int id, boolean primary) throws SQLException, IOException {
		ResultSet rs2;
		String type;
		String kildeId;
		statementf2.setInt(1, id);
		rs2 = statementf2.executeQuery();

		while (rs2.next()) {
			type = rs2.getString("BEGTYPE").trim();

			if ("Vielse".equals(type)) {
				writeLine("1 MARR");
			} else if ("Folketælling".equals(type)) {
				writeLine("1 CENS");
			}

			if (!primary) {
				writeLine("2 NOTE Vidne");
			}

			writeDate(rs2);
			writeLine("2 PLAC " + rs2.getString("STEDNAVN"));
			kildeId = rs2.getString("KILDEID");
			writeSourceReference(kildeId, rs2.getString("DETALJER"));
		}

	}

	/**
	 * Udskriv GEDCOM header
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
	 * Udskriv alle begivenheder for et individ
	 *
	 * @param id
	 * @param primary
	 * @throws SQLException
	 * @throws IOException
	 */
	public void writeIndividualEvent(int id, boolean primary) throws SQLException, IOException {
		String type;
		String kildeId;

		statementi2.setInt(1, id);
		final ResultSet rs2 = statementi2.executeQuery();

		if (rs2.next()) {
			type = rs2.getString("BEGTYPE").trim();

			if ("Dåb".equals(type)) {
				writeLine("1 CHR");
			} else if ("Konfirmation".equals(type)) {
				writeLine("1 CONF");
			} else if ("Begravelse".equals(type)) {
				writeLine("1 BURI");
			} else if ("Erhverv".equals(type)) {
				writeLine("1 OCCU " + rs2.getString("NOTE"));
				return;
			}

			writeDate(rs2);
			writeLine("2 PLAC " + rs2.getString("STEDNAVN"));
			if (primary) {
				writeLine("2 NOTE " + rs2.getString("NOTE"));
			} else {
				writeLine("2 NOTE Vidne");
			}
			kildeId = rs2.getString("KILDEID");
			writeSourceReference(kildeId, rs2.getString("DETALJER"));

		}
	}

	/**
	 * Udskriv alle individer
	 *
	 * @param conn
	 * @throws SQLException
	 * @throws IOException
	 *
	 */
	private void writeIndividuals(Connection conn) throws SQLException, IOException {
		IndividModel model;
		String sex = "";

		final ResultSet rs1 = statementi1.executeQuery();

		while (rs1.next()) {
			model = IndividModel.getData(conn, rs1);
			writeLine("0 @I" + model.getId() + "@ INDI");
			writeLine("1 NAME " + model.getStdNavn());
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
				if (model.getFoedt().trim().length() > 4) {
					final LocalDate localDate = LocalDate.parse(model.getFoedt().trim());
					writeLine("2 DATE " + dateFormat.format(localDate).toUpperCase());
				} else {
					writeLine("2 DATE " + model.getFoedt());
				}
			}

			writeIndividualEvent(model.getId(), true);
			writeWitnessedEvents(model.getId());

		}

		statementi1.close();
	}

	/**
	 * Udskriv en linie
	 *
	 * @param string
	 * @throws IOException
	 */
	private void writeLine(String string) throws IOException {
		fw.write(string + "\n");
		System.out.println(string);
	}

	/**
	 * Udskriv en kildereference
	 *
	 * @param kildeId
	 * @param detaljer
	 * @throws SQLException
	 * @throws IOException
	 */
	public void writeSourceReference(String kildeId, String detaljer) throws SQLException, IOException {
		final String text = findTextFromSource(kildeId);
		boolean found = false;

		for (final SourceReference sourceReference : referenceList) {
			found = false;

			if (sourceReference.getTitle().contains(text)) {
				sourceReference.addAlias(kildeId);
				kildeId = sourceReference.getId();
				found = true;
				break;
			}

		}

		if (!found) {
			final SourceReference newReference = new SourceReference();
			newReference.setId(kildeId);
			newReference.setTitle(text);
			referenceList.add(newReference);
		}

		writeLine("2 SOUR @S" + kildeId + "@");
		writeLine("3 PAGE " + text + "\r\n4 CONC " + detaljer);
	}

	/**
	 * Udskriv alle kilder
	 *
	 * @param conn
	 * @throws SQLException
	 * @throws IOException
	 */
	private void writeSources() throws SQLException, IOException {
		int kbnr = 0;
		String aarinterval = "";
		int id = 0;
		boolean found = false;

		final ResultSet rs1 = statementk1.executeQuery();
		foreachsource: while (rs1.next()) {
			id = rs1.getInt("ID");
			found = false;

			for (final SourceReference sourceReference : referenceList) {
				if ((id + "").equals(sourceReference.getId())) {
					found = true;
					break;
				}
			}

			if (!found) {
				continue foreachsource;
			}

			writeLine("0 @S" + id + "@ SOUR");

			try {
				kbnr = Integer.parseInt(rs1.getString("KBNR").trim());
			} catch (final Exception e) {
				kbnr = 17;
			}

			aarinterval = rs1.getString("AARINTERVAL").trim();

			if (kbnr < 10 || aarinterval.substring(0, 4).matches(FOUR_DIGITS)) {
				writeLine("1 TITL Kirkebog Blistrup " + aarinterval);
				writeLine("1 ABBR Kirkebog Blistrup " + aarinterval);
			} else {
				writeLine("1 TITL Folketælling Blistrup " + aarinterval);
				writeLine("1 ABBR Folketælling Blistrup " + aarinterval);
			}
		}

	}

	/**
	 * Udskriv GEDCOM trailer
	 *
	 * @throws IOException
	 */
	private void writeTrailer() throws IOException {
		writeLine("0 TRLR");

	}

	/**
	 * Indsæt bevidnede begivenheder for individer
	 *
	 * @param conn
	 * @param individId
	 * @throws SQLException
	 * @throws IOException
	 */
	private void writeWitnessedEvents(int individId) throws SQLException, IOException {
		int begId = 0;
		ResultSet rs2, rs3;

		statementv1.setInt(1, individId);
		final ResultSet rs1 = statementv1.executeQuery();

		while (rs1.next()) {
			begId = rs1.getInt("INDIVIDBEGIVENHEDID");

			if (begId > 0) {
				statementv2.setInt(1, begId);
				rs2 = statementv2.executeQuery();

				if (rs2.next()) {
					writeIndividualEvent(rs2.getInt("INDIVIDID"), false);
				}

			} else {
				begId = rs1.getInt("FAMILIEBEGIVENHEDID");
				if (begId > 0) {
					statementv3.setInt(1, begId);
					rs3 = statementv3.executeQuery();

					if (rs3.next()) {
						writeFamilyEvent(rs3.getInt("INDIVIDID"), false);
					}

				}
			}

		}

	}

}
