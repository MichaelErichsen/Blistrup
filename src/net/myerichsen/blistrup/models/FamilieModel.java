package net.myerichsen.blistrup.models;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Erichsen
 * @version 30. aug. 2023
 *
 */
public class FamilieModel {
	private static final String SELECT1 = "SELECT * FROM BLISTRUP.FAMILIE";
	private static final String SELECT2 = "SELECT * FROM BLISTRUP.PERSONNAVN WHERE INDIVIDID = ?";
	private static final String SELECT3 = "SELECT * FROM BLISTRUP.INDIVID WHERE FAMC = ?";
	private static final String SELECT4 = "SELECT * FROM BLISTRUP.INDIVID WHERE ID = ?";
	private static final String INSERT1 = "INSERT INTO BLISTRUP.FAMILIE (HUSFADER, HUSMODER) VALUES(?, ?)";
	private static final String UPDATE1 = "UPDATE BLISTRUP.FAMILIE SET HUSFADER = ? WHERE ID = ?";
	private static final String UPDATE2 = "UPDATE BLISTRUP.FAMILIE SET HUSMODER = ? WHERE ID = ?";

	private static PreparedStatement statements2;

	private static PreparedStatement statements3;

	private static PreparedStatement statements4;

	private static PreparedStatement statementi1;
	private static PreparedStatement statementu1;
	private static PreparedStatement statementu2;

	/**
	 * @param statement2
	 * @param statement3
	 * @param statement4
	 * @param rs1
	 * @return
	 * @throws SQLException
	 */
	public static FamilieModel getData(Connection conn, final ResultSet rs1) throws SQLException {
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
		String foedt = "";

		model = new FamilieModel(conn);
		familieId = rs1.getInt("ID");
		model.setId(familieId);
		husFaderId = rs1.getInt("HUSFADER");
		model.setFader(husFaderId);
		husModerId = rs1.getInt("HUSMODER");
		model.setModer(husModerId);

		// Husfaders navn
		// SELECT2 = "SELECT * FROM BLISTRUP.PERSONNAVN WHERE INDIVIDID = ?";

		statements2.setInt(1, husFaderId);
		rs2 = statements2.executeQuery();

		if (rs2.next()) {
			model.setFaderNavn(rs2.getString("STDNAVN").trim());
		}

		// Husfaders fødsel
		// SELECT4 = "SELECT * FROM BLISTRUP.INDIVID WHERE ID = ?";

		statements4.setInt(1, husFaderId);
		rs4 = statements4.executeQuery();

		if (rs4.next()) {
			foedt = rs4.getString("FOEDT");

			if (foedt != null) {
			}

		}

		// Husmoders navn
		// SELECT2 = "SELECT * FROM BLISTRUP.PERSONNAVN WHERE INDIVIDID = ?";

		statements2.setInt(1, husModerId);
		rs2 = statements2.executeQuery();

		if (rs2.next()) {
			model.setModerNavn(rs2.getString("STDNAVN").trim());
		}

		// Husmoders fødsel
		// SELECT4 = "SELECT * FROM BLISTRUP.INDIVID WHERE ID = ?";

		statements4.setInt(1, husModerId);
		rs4 = statements4.executeQuery();

		if (rs4.next()) {
			foedt = rs4.getString("FOEDT");
			if (foedt != null) {
				model.setModerFoedt(foedt.trim());
			}
		}

		// Børn
		boern = new ArrayList<>();

		// SELECT3 = "SELECT * FROM BLISTRUP.INDIVID WHERE FAMC = ?";

		statements3.setInt(1, familieId);
		rs3 = statements3.executeQuery();

		while (rs3.next()) {
			individModel = new IndividModel();
			barnId = rs3.getInt("ID");
			individModel.setId(barnId);

			// Barns navn
			// SELECT2 = "SELECT * FROM BLISTRUP.PERSONNAVN WHERE INDIVIDID = ?";

			statements2.setInt(1, barnId);
			rs2 = statements2.executeQuery();

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
		final PreparedStatement statement = conn.prepareStatement(SELECT1);

		// SELECT1 = "SELECT * FROM BLISTRUP.FAMILIE";

		final ResultSet rs1 = statement.executeQuery();

		while (rs1.next()) {
			model = getData(conn, rs1);
			liste.add(model);
		}

		statement.close();

		final FamilieModel[] array = new FamilieModel[liste.size()];

		for (int i = 0; i < liste.size(); i++) {
			array[i] = liste.get(i);
		}

		return array;

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
	 * Constructor
	 *
	 * @throws SQLException
	 *
	 */
	public FamilieModel(Connection conn) throws SQLException {
		statements2 = conn.prepareStatement(SELECT2);
		statements3 = conn.prepareStatement(SELECT3);
		statements4 = conn.prepareStatement(SELECT4);
		statementi1 = conn.prepareStatement(INSERT1, Statement.RETURN_GENERATED_KEYS);
		statementu1 = conn.prepareStatement(UPDATE1);
		statementu2 = conn.prepareStatement(UPDATE2);
	}

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
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public int insert() throws SQLException {
		int familieId = 0;

		statementi1.setInt(1, fader);
		statementi1.setInt(2, moder);
		statementi1.executeUpdate();

		final ResultSet generatedKeys = statementi1.getGeneratedKeys();

		if (generatedKeys.next()) {
			familieId = generatedKeys.getInt(1);
		} else {
			familieId = 0;
		}
		generatedKeys.close();

		return familieId;

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

	/**
	 * @throws SQLException
	 *
	 */
	public void updateFather() throws SQLException {
		statementu1.setInt(1, fader);
		statementu1.setInt(2, id);
		statementu1.executeUpdate();
	}

	/**
	 * @throws SQLException
	 *
	 */
	public void updateMother() throws SQLException {
		statementu2.setInt(1, moder);
		statementu2.setInt(2, id);
		statementu2.executeUpdate();
	}

}
