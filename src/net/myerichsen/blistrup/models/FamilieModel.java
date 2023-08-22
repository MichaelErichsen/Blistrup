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
 * @version 22. aug. 2023
 *
 */
public class FamilieModel {
	private static final String SELECT1 = "SELECT * FROM BLISTRUP.FAMILIE";
	private static final String SELECT2 = "SELECT * FROM BLISTRUP.PERSONNAVN WHERE INDIVIDID = ?";
	private static final String SELECT3 = "SELECT * FROM BLISTRUP.INDIVID WHERE FAMC = ?";
	private static final String SELECT4 = "SELECT * FROM BLISTRUP.INDIVID WHERE ID = ?";

	/**
	 * @param statement2
	 * @param statement3
	 * @param statement4
	 * @param rs1
	 * @return
	 * @throws SQLException
	 */
	public static FamilieModel getData(Connection conn, final ResultSet rs1) throws SQLException {
		final PreparedStatement statement2 = conn.prepareStatement(SELECT2);
		final PreparedStatement statement3 = conn.prepareStatement(SELECT3);
		final PreparedStatement statement4 = conn.prepareStatement(SELECT4);

		FamilieModel model;
		ResultSet rs2;
		ResultSet rs3;
		ResultSet rs4;
		int familieId;
		int husFaderId;
		int husModerId;
		int barnId;
		IndividModel individModel;
		List<IndividModel> boern;
		model = new FamilieModel();
		familieId = rs1.getInt("ID");
		model.setId(familieId);
		husFaderId = rs1.getInt("HUSFADER");
		model.setFader(husFaderId);
		husModerId = rs1.getInt("HUSMODER");
		model.setModer(husModerId);

		// Husfaders navn
		// SELECT2 = "SELECT * FROM BLISTRUP.PERSONNAVN WHERE INDIVIDID = ?";

		statement2.setInt(1, husFaderId);
		rs2 = statement2.executeQuery();

		if (rs2.next()) {
			model.setFaderNavn(rs2.getString("STDNAVN").trim());
		}

		// Husfaders fødsel
		// SELECT4 = "SELECT * FROM BLISTRUP.INDIVID WHERE ID = ?";

		statement4.setInt(1, husFaderId);
		rs4 = statement4.executeQuery();

		if (rs4.next()) {
			model.setFaderFoedt(rs4.getString("FOEDT").trim());
		}

		// Husmoders navn
		// SELECT2 = "SELECT * FROM BLISTRUP.PERSONNAVN WHERE INDIVIDID = ?";

		statement2.setInt(1, husModerId);
		rs2 = statement2.executeQuery();

		if (rs2.next()) {
			model.setModerNavn(rs2.getString("STDNAVN").trim());
		}

		// Husmoders fødsel
		// SELECT4 = "SELECT * FROM BLISTRUP.INDIVID WHERE ID = ?";

		statement4.setInt(1, husModerId);
		rs4 = statement4.executeQuery();

		if (rs4.next()) {
			model.setModerFoedt(rs4.getString("FOEDT").trim());
		}

		// Børn
		boern = new ArrayList<>();

		// SELECT3 = "SELECT * FROM BLISTRUP.INDIVID WHERE FAMC = ?";

		statement3.setInt(1, familieId);
		rs3 = statement3.executeQuery();

		while (rs3.next()) {
			individModel = new IndividModel();
			barnId = rs3.getInt("ID");
			individModel.setId(barnId);

			// Barns navn
			// SELECT2 = "SELECT * FROM BLISTRUP.PERSONNAVN WHERE INDIVIDID = ?";

			statement2.setInt(1, barnId);
			rs2 = statement2.executeQuery();

			if (rs2.next()) {
				individModel.setStdNavn(rs2.getString("STDNAVN").trim());
			}

			boern.add(individModel);
		}

		model.setBoern(boern);
		return model;
	}

	public static FamilieModel[] getData(String dbPath) throws SQLException {
		FamilieModel model;
		final List<FamilieModel> liste = new ArrayList<>();
		final Connection conn = DriverManager.getConnection("jdbc:derby:" + dbPath);
		final PreparedStatement statement1 = conn.prepareStatement(SELECT1);
//		final PreparedStatement statement2 = conn.prepareStatement(SELECT2);
//		final PreparedStatement statement3 = conn.prepareStatement(SELECT3);
//		final PreparedStatement statement4 = conn.prepareStatement(SELECT4);

//		new ArrayList<>();

		// SELECT1 = "SELECT * FROM BLISTRUP.FAMILIE";

		final ResultSet rs1 = statement1.executeQuery();

		while (rs1.next()) {
			model = getData(conn, rs1);
			liste.add(model);
		}

		statement1.close();
//		statement2.close();
//		statement3.close();
//		statement4.close();

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
	private int fader = 0;
	private int moder = 0;
	private String faderNavn = "";
	private String moderNavn = "";
	private List<IndividModel> boern = new ArrayList<>();
	private List<FamilieBegivenhedModel> begivenheder = new ArrayList<>();
	private String noter = "";
	private String faderFoedt = "";
	private String moderFoedt = "";

	/**
	 * @return the begivenheder
	 */
	public List<FamilieBegivenhedModel> getBegivenheder() {
		return begivenheder;
	}

	/**
	 * @return the boern
	 */
	public List<IndividModel> getBoern() {
		return boern;
	}

	/**
	 * @return the fader
	 */
	public int getFader() {
		return fader;
	}

	/**
	 * @return the faderFoedt
	 */
	public String getFaderFoedt() {
		return faderFoedt;
	}

	/**
	 * @return the faderNavn
	 */
	public String getFaderNavn() {
		return faderNavn;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the moder
	 */
	public int getModer() {
		return moder;
	}

	/**
	 * @return the moderFoedt
	 */
	public String getModerFoedt() {
		return moderFoedt;
	}

	/**
	 * @return the moderNavn
	 */
	public String getModerNavn() {
		return moderNavn;
	}

	/**
	 * @return the noter
	 */
	public String getNoter() {
		return noter;
	}

	/**
	 * @param begivenheder the begivenheder to set
	 */
	public void setBegivenheder(List<FamilieBegivenhedModel> begivenheder) {
		this.begivenheder = begivenheder;
	}

	/**
	 * @param boern the boern to set
	 */
	public void setBoern(List<IndividModel> boern) {
		this.boern = boern;
	}

	/**
	 * @param fader the fader to set
	 */
	public void setFader(int fader) {
		this.fader = fader;
	}

	/**
	 * @param faderFoedt the faderFoedt to set
	 */
	public void setFaderFoedt(String faderFoedt) {
		this.faderFoedt = faderFoedt;
	}

	/**
	 * @param faderNavn the faderNavn to set
	 */
	public void setFaderNavn(String faderNavn) {
		this.faderNavn = faderNavn;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @param moder the moder to set
	 */
	public void setModer(int moder) {
		this.moder = moder;
	}

	/**
	 * @param moderFoedt the moderFoedt to set
	 */
	public void setModerFoedt(String moderFoedt) {
		this.moderFoedt = moderFoedt;
	}

	/**
	 * @param moderNavn the moderNavn to set
	 */
	public void setModerNavn(String moderNavn) {
		this.moderNavn = moderNavn;
	}

	/**
	 * @param noter the noter to set
	 */
	public void setNoter(String noter) {
		this.noter = noter;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append(id);
		builder.append(", \r\n");
		builder.append(fader);
		builder.append(", ");
		if (faderNavn != null) {
			builder.append(faderNavn);
			builder.append(", ");
		}
		if (faderFoedt != null) {
			builder.append(faderFoedt);
			builder.append(", ");
		}
		builder.append("\r\n" + moder);
		builder.append(", ");
		if (moderNavn != null) {
			builder.append(moderNavn);
			builder.append(", ");
		}
		if (moderFoedt != null) {
			builder.append(moderFoedt);
			builder.append(", ");
		}
		if (boern.size() > 0) {
			builder.append("\r\nBørn:\r\n");

			for (final IndividModel barn : boern) {
				builder.append(barn.toString());
				builder.append(", ");
			}

		}
		if (begivenheder.size() > 0) {
			builder.append("\r\nBegivenheder:\r\n");

			for (final FamilieBegivenhedModel begivenhed : begivenheder) {
				builder.append(begivenhed.toString());
				builder.append(", ");
			}

		}
		if (noter != null) {
			builder.append(noter);
		}
		return builder.toString();
	}

}
