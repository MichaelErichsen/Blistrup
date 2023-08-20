package net.myerichsen.blistrup.savers;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import net.myerichsen.blistrup.models.FamilieModel;
import net.myerichsen.blistrup.models.IndividBegivenhedModel;
import net.myerichsen.blistrup.models.IndividModel;

/**
 * @author Michael Erichsen
 * @version 20. aug. 2023
 *
 */
public class GedcomSaver {
	final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US);
	final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final GedcomSaver gs = new GedcomSaver();

		gs.execute(args);

	}

	private OutputStreamWriter fw;

	/**
	 * Worker method
	 *
	 * @param args
	 */
	private void execute(String[] args) {
		final String filename = "C:\\Users\\michael\\Documents\\The Master Genealogist v9\\Export\\Test.ged";
		final String submitterName = "Michael Erichsen";
		final String dbPath = "C:\\Users\\michael\\BlistrupDB";

		try {
			fw = new OutputStreamWriter(new FileOutputStream(filename));
			writeHeader(filename, submitterName);
			writeIndividuals(dbPath);
			writeFamilies(dbPath);
			writeSources(dbPath);
			writeTrailer();
			fw.flush();
			fw.close();
			System.out.println("Færdig!");
		} catch (final Exception e) {
			e.printStackTrace();
		}
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
	 * @param dbPath
	 * @throws SQLException
	 * @throws IOException
	 */
	private void writeFamilies(String dbPath) throws SQLException, IOException {
		FamilieModel[] array = FamilieModel.getData(dbPath);

		for (FamilieModel model : array) {
			writeLine("0 @F" + model.getId() + "@ FAM");
			if (model.getFader() > 0) {
				writeLine("1 HUSB @I" + model.getFader() + "@");
			}

			if (model.getModer() > 0) {
				writeLine("1 WIFE @I" + model.getModer() + "@");
			}

			for (IndividModel barn : model.getBoern()) {
				writeLine("1 CHIL @I" + barn.getId() + "@");
			}

//			writeLine("1 MARR");
//			writeLine("2 DATE 01 MAY 1921");
//			writeLine("2 PLAC Olsker, Bornholm");
		}
	}

	/**
	 * Write GEDCOM header
	 *
	 * @param filename
	 * @param submitterName
	 * @throws IOException
	 */
	private void writeHeader(String filename, String submitterName) throws IOException {
		final LocalDateTime now = LocalDateTime.now();

		writeLine("0 HEAD");
		writeLine("1 SOUR BlistrupLokalhistorie");
		writeLine("2 VERS 1.0.0");
		writeLine("1 SUBM @SUB1@");
		writeLine("1 GEDC");
		writeLine("2 VERS 5.5");
		writeLine("2 FORM LINEAGE-LINKED");
		writeLine("1 DEST GED55");
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
	 * @param dbPath
	 * @throws SQLException
	 * @throws IOException
	 */
	private void writeIndividuals(String dbPath) throws SQLException, IOException {
		String sex = "";
		final IndividModel[] array = IndividModel.getData(dbPath);

		// For each individual
		for (final IndividModel model : array) {
			writeLine("0 @I" + model.getId() + "@ INDI");
			writeLine("1 NAME " + slashName(model.getStdNavn()));
			sex = model.getKoen().toUpperCase().equals("M") ? "M" : "F";
			writeLine("1 SEX " + sex);

			if (model.getFamc() > 0) {
				writeLine("1 FAMC @F" + model.getFamc() + "@");
			}

			for (Integer aFams : model.getFams()) {
				writeLine("1 FAMS @F" + aFams + "@");
			}

			List<IndividBegivenhedModel> begivenheder = model.getBegivenheder();

			for (IndividBegivenhedModel begivenhed : begivenheder) {
				writeLine("Begivenhed: " + begivenhed.getId() + ", " + begivenhed.getBegType() + ", "
						+ begivenhed.getRolle() + ", " + begivenhed.getDato() + ", " + begivenhed.getDetaljer());
			}
		}
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
	 * @param dbPath
	 */
	private void writeSources(String dbPath) {
//		For each source
//
//		Get ID and strings for a title and abbreviation
//
//		0 @S1@ SOUR
//		1 TITL Kirkebog Blistrup 1698-1797
//		1 ABBR Kirkebog Blistrup 1698-1797
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
