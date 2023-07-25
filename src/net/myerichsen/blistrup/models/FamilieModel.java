package net.myerichsen.blistrup.models;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Erichsen
 * @version 25. jul. 2023
 *
 */
public class FamilieModel {
	private static final String SELECT1 = "SELECT * FROM BLISTRUP.FAMILIE"; // FETCH FIRST 50 ROWS ONLY";
	private static final String SELECT2 = "SELECT * FROM BLISTRUP.PERSONNAVN WHERE INDIVIDID = ?";
	private static final String SELECT3 = "SELECT * FROM BLISTRUP.INDIVID WHERE FAMC = ?";

	public static FamilieModel[] getData(String dbPath) throws SQLException {
		FamilieModel model;
		final List<FamilieModel> liste = new ArrayList<>();
		final Connection conn = DriverManager.getConnection("jdbc:derby:" + dbPath);
		final PreparedStatement statement1 = conn.prepareStatement(SELECT1);
		final PreparedStatement statement2 = conn.prepareStatement(SELECT2);
		final PreparedStatement statement3 = conn.prepareStatement(SELECT3);
		final ResultSet rs1 = statement1.executeQuery();
		ResultSet rs2;
		ResultSet rs3;
		int familieId;
		int husFaderId;
		int husModerId;
		int barnId;
		IndividModel individModel;
		List<IndividModel> boern;

		while (rs1.next()) {
			model = new FamilieModel();
			familieId = rs1.getInt("ID");
			model.setId(familieId);
			husFaderId = rs1.getInt("HUSFADER");
			model.setHusFader(husFaderId);
			husModerId = rs1.getInt("HUSMODER");
			model.setHusModer(husModerId);

			// Husfaders navn
			statement2.setInt(1, husFaderId);
			rs2 = statement2.executeQuery();

			if (rs2.next()) {
				model.setHusFaderNavn(rs2.getString("STDNAVN").trim());
			}

			// Husmoders navn
			statement2.setInt(1, husModerId);
			rs2 = statement2.executeQuery();

			if (rs2.next()) {
				model.setHusModerNavn(rs2.getString("STDNAVN").trim());
			}

			// Børn
			boern = new ArrayList<>();

			statement3.setInt(1, familieId);
			rs3 = statement3.executeQuery();

			while (rs3.next()) {
				individModel = new IndividModel();
				barnId = rs3.getInt("ID");
				individModel.setId(barnId);

				// Barns navn
				statement2.setInt(1, barnId);
				rs2 = statement2.executeQuery();

				if (rs2.next()) {
					individModel.setStdNavn(rs2.getString("STDNAVN").trim());
				}

				boern.add(individModel);
			}

			model.setBoern(boern);

			liste.add(model);
		}

		final FamilieModel[] array = new FamilieModel[liste.size()];

		for (int i = 0; i < liste.size(); i++) {
			array[i] = liste.get(i);
		}

		return array;

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			final FamilieModel[] data = FamilieModel.getData("C:\\Users\\michael\\BlistrupDB");

			for (final FamilieModel familieModel : data) {
				System.out.println(familieModel);
			}
			System.out.println("Færdig!");
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	private int id = 0;
	private int husFader = 0;
	private int husModer = 0;
	private String husFaderNavn = "";
	private String husModerNavn = "";
	private List<IndividModel> boern = new ArrayList<>();

	/**
	 * @return the boern
	 */
	public List<IndividModel> getBoern() {
		return boern;
	}

	/**
	 * @return the husFader
	 */
	public int getHusFader() {
		return husFader;
	}

	/**
	 * @return the husFaderNavn
	 */
	public String getHusFaderNavn() {
		return husFaderNavn;
	}

	/**
	 * @return the husModer
	 */
	public int getHusModer() {
		return husModer;
	}

	/**
	 * @return the husModerNavn
	 */
	public String getHusModerNavn() {
		return husModerNavn;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param boern the boern to set
	 */
	public void setBoern(List<IndividModel> boern) {
		this.boern = boern;
	}

	/**
	 * @param husFader the husFader to set
	 */
	public void setHusFader(int husFader) {
		this.husFader = husFader;
	}

	/**
	 * @param husFaderNavn the husFaderNavn to set
	 */
	public void setHusFaderNavn(String husFaderNavn) {
		this.husFaderNavn = husFaderNavn;
	}

	/**
	 * @param husModer the husModer to set
	 */
	public void setHusModer(int husModer) {
		this.husModer = husModer;
	}

	/**
	 * @param husModerNavn the husModerNavn to set
	 */
	public void setHusModerNavn(String husModerNavn) {
		this.husModerNavn = husModerNavn;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(id);
		builder.append(", ");
		builder.append(husFader);
		builder.append(", ");
		builder.append(husModer);
		builder.append(", ");
		if (husFaderNavn != null) {
			builder.append(husFaderNavn);
			builder.append(", ");
		}
		if (husModerNavn != null) {
			builder.append(husModerNavn);
			builder.append(", ");
		}
		if (boern != null)
			builder.append("\r\nBørn:\r\n");

		for (IndividModel individModel : boern) {
			builder.append(individModel.getId() + ", " + individModel.getStdNavn());
		}
		builder.append(boern);
		return builder.toString();
	}

}
