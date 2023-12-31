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
 * @version 29. sep. 2023
 */
public class IndividModel {
	private static final String SELECT1 = "SELECT * FROM BLISTRUP.INDIVID";
	private static final String SELECT2 = "SELECT * FROM BLISTRUP.PERSONNAVN WHERE INDIVIDID = ?";
	private static final String SELECT3 = "SELECT * FROM BLISTRUP.FAMILIE WHERE HUSFADER = ? OR HUSMODER = ?";
	private static final String SELECT4 = "SELECT * FROM BLISTRUP.PERSONNAVN WHERE INDIVIDID = ?";
	private static final String INSERT1 = "INSERT INTO BLISTRUP.INDIVID (KOEN, FOEDT, FAMC) VALUES (?, ?, ?)";
	private static final String UPDATEFAMC = "UPDATE BLISTRUP.INDIVID SET FAMC = ? WHERE ID = ?";

	/**
	 * Get a model from an SQL result set
	 *
	 * @return
	 * @throws SQLException
	 */
	public static IndividModel getData(Connection conn, ResultSet rs1) throws SQLException {
		IndividModel model = new IndividModel();
		final PreparedStatement statement2 = conn.prepareStatement(SELECT2);
		final PreparedStatement statement3 = conn.prepareStatement(SELECT3);
		final PreparedStatement statement4 = conn.prepareStatement(SELECT4);

		ResultSet rs2, rs3, rs4;
		int husfader = 0;
		int husmoder = 0;
		String koen = "";

		final int id = rs1.getInt("ID");
		statement2.setInt(1, id);
		rs2 = statement2.executeQuery();

		while (rs2.next()) {
			if ("TRUE".equals(rs2.getString("PRIMAERNAVN").trim())) {
				husfader = 0;
				husmoder = 0;

				model = new IndividModel();
				model.setId(id);
				koen = rs1.getString("KOEN").trim();
				model.setKoen(koen);
				try {
					model.setBlistrupId(rs1.getString("BLISTRUPID").trim());
				} catch (final Exception e) {
				}
				model.setFamc(rs1.getInt("FAMC"));
				try {
					model.setFoedt(rs1.getString("FOEDT").trim());
				} catch (final Exception e) {
				}

				model.setFam(rs1.getString("FAM"));
				model.setSlgt(rs1.getString("SLGT"));

				model.setStdNavn(rs2.getString("STDNAVN").trim());
				model.setFonetiskNavn(rs2.getString("FONETISKNAVN").trim());

				statement3.setInt(1, id);
				statement3.setInt(2, id);
				rs3 = statement3.executeQuery();

				if (rs3.next()) {
					model.getFams().add(rs3.getInt("ID"));
					husfader = rs3.getInt("HUSFADER");
					husmoder = rs3.getInt("HUSMODER");
				}

				if ("k".equals(koen) && husfader > 0) {
					statement4.setInt(1, husfader);
					rs4 = statement4.executeQuery();

					if (rs4.next()) {
						model.getSpouseNames().add(rs4.getString("STDNAVN").trim());
					}
				}

				if ("m".equals(koen) && husmoder > 0) {
					statement4.setInt(1, husmoder);
					rs4 = statement4.executeQuery();

					if (rs4.next()) {
						model.getSpouseNames().add(rs4.getString("STDNAVN").trim());
					}
				}

			}
		}

		statement2.close();
		statement3.close();
		statement4.close();
		return model;
	}

	/**
	 * Get array of models
	 *
	 * @return
	 * @throws SQLException
	 */
	public static IndividModel[] getData(String dbPath) throws SQLException {
		IndividModel model;
		final List<IndividModel> liste = new ArrayList<>();
		final Connection conn = DriverManager.getConnection("jdbc:derby:" + dbPath);
		final PreparedStatement statement1 = conn.prepareStatement(SELECT1);
		final ResultSet rs1 = statement1.executeQuery();

		while (rs1.next()) {
			model = getData(conn, rs1);
			liste.add(model);
		}

		statement1.close();

		final IndividModel[] array = new IndividModel[liste.size()];

		for (int i = 0; i < liste.size(); i++) {
			array[i] = liste.get(i);
		}

		return array;
	}

	private int id = 0;
	private int famc = 0;
	private String koen = "?";
	private String BlistrupId = "";
	private List<PersonNavneModel> personNavneListe = new ArrayList<>();
	private String fonetiskNavn = "";
	private String stdNavn = "";
	private List<Integer> fams = new ArrayList<>();
	private List<String> spouseNames = new ArrayList<>();
	private List<IndividBegivenhedModel> begivenheder = new ArrayList<>();
	private String noter = "";
	private String foedt;
	private String doebt;
	private String doed;
	private String begravet;
	private String fam = "";
	private String slgt = "";
	private boolean primary = false;
	private String detaljer = "";

	/**
	 * @return the begivenheder
	 */
	public List<IndividBegivenhedModel> getBegivenheder() {
		return begivenheder;
	}

	/**
	 * @return the begravet
	 */
	public String getBegravet() {
		return begravet;
	}

	/**
	 * @return the blistrupId
	 */
	public String getBlistrupId() {
		return BlistrupId;
	}

	/**
	 * @return the detaljer
	 */
	public String getDetaljer() {
		return detaljer;
	}

	/**
	 * @return the doebt
	 */
	public String getDoebt() {
		return doebt;
	}

	/**
	 * @return the doed
	 */
	public String getDoed() {
		return doed;
	}

	/**
	 * @return the fam
	 */
	public String getFam() {
		return fam;
	}

	/**
	 * @return the famc
	 */
	public int getFamc() {
		return famc;
	}

	/**
	 * @return the fams
	 */
	public List<Integer> getFams() {
		return fams;
	}

	/**
	 * @return the foedt
	 */
	public String getFoedt() {
		return foedt;
	}

	/**
	 * @return the fonetiskNavn
	 */
	public String getFonetiskNavn() {
		return fonetiskNavn;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the koen
	 */
	public String getKoen() {
		return koen;
	}

	/**
	 * @return the noter
	 */
	public String getNoter() {
		return noter;
	}

	/**
	 * @return the personNavneListe
	 */
	public List<PersonNavneModel> getPersonNavneListe() {
		return personNavneListe;
	}

	/**
	 * @return the slgt
	 */
	public String getSlgt() {
		return slgt;
	}

	/**
	 * @return the spouseNames
	 */
	public List<String> getSpouseNames() {
		return spouseNames;
	}

	/**
	 * @return the stdNavn
	 */
	public String getStdNavn() {
		return stdNavn;
	}

	/**
	 * Inds�t et individ
	 *
	 * @param conn
	 * @throws SQLException
	 */
	public int insert(Connection conn) throws SQLException {
		int individId = 0;

		final PreparedStatement statement = conn.prepareStatement(INSERT1, Statement.RETURN_GENERATED_KEYS);
		statement.setString(1, koen);
		statement.setString(2, foedt);
		statement.setInt(3, famc);
		statement.executeUpdate();

		final ResultSet generatedKeys = statement.getGeneratedKeys();

		if (generatedKeys.next()) {
			individId = generatedKeys.getInt(1);
		} else {
			individId = 0;
		}
		generatedKeys.close();

		return individId;
	}

	/**
	 * @return the primary
	 */
	public boolean isPrimary() {
		return primary;
	}

	/**
	 * @param begivenheder the begivenheder to set
	 */
	public void setBegivenheder(List<IndividBegivenhedModel> begivenheder) {
		this.begivenheder = begivenheder;
	}

	/**
	 * @param begravet the begravet to set
	 */
	public void setBegravet(String begravet) {
		this.begravet = begravet;
	}

	/**
	 * @param blistrupId the blistrupId to set
	 */
	public void setBlistrupId(String blistrupId) {
		BlistrupId = blistrupId;
	}

	/**
	 * @param detaljer the detaljer to set
	 */
	public void setDetaljer(String detaljer) {
		this.detaljer = detaljer;
	}

	/**
	 * @param doebt the doebt to set
	 */
	public void setDoebt(String doebt) {
		this.doebt = doebt;
	}

	/**
	 * @param doed the doed to set
	 */
	public void setDoed(String doed) {
		this.doed = doed;
	}

	/**
	 * @param fam the fam to set
	 */
	public void setFam(String fam) {
		this.fam = fam;
	}

	/**
	 * @param i the famc to set
	 */
	public void setFamc(int i) {
		this.famc = i;
	}

	/**
	 * @param fams the fams to set
	 */
	public void setFams(List<Integer> fams) {
		this.fams = fams;
	}

	/**
	 * @param foedt the foedt to set
	 */
	public void setFoedt(String foedt) {
		this.foedt = foedt;
	}

	/**
	 * @param fonetiskNavn the fonetiskNavn to set
	 */
	public void setFonetiskNavn(String fonetiskNavn) {
		this.fonetiskNavn = fonetiskNavn;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @param koen the koen to set
	 */
	public void setKoen(String koen) {
		this.koen = koen;
	}

	/**
	 * @param noter the noter to set
	 */
	public void setNoter(String noter) {
		this.noter = noter;
	}

	/**
	 * @param personNavneListe the personNavneListe to set
	 */
	public void setPersonNavneListe(List<PersonNavneModel> personNavneListe) {
		this.personNavneListe = personNavneListe;
	}

	/**
	 * @param primary the primary to set
	 */
	public void setPrimary(boolean primary) {
		this.primary = primary;
	}

	/**
	 * @param slgt the slgt to set
	 */
	public void setSlgt(String slgt) {
		this.slgt = slgt;
	}

	/**
	 * @param spouseNames the spouseNames to set
	 */
	public void setSpouseNames(List<String> spouseNames) {
		this.spouseNames = spouseNames;
	}

	/**
	 * @param stdNavn the stdNavn to set
	 */
	public void setStdNavn(String stdNavn) {
		this.stdNavn = stdNavn;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append(id);
		builder.append(", ");
		builder.append(famc);
		builder.append(", ");
		if (koen != null) {
			builder.append(koen);
			builder.append(", ");
		}
		if (BlistrupId != null) {
			builder.append(BlistrupId);
			builder.append(", ");
		}
		if (personNavneListe.size() > 0) {
			for (final PersonNavneModel personNavn : personNavneListe) {
				builder.append(personNavn.toString());
				builder.append(", ");
			}
		}
		if (fonetiskNavn != null) {
			builder.append(fonetiskNavn);
			builder.append(", ");
		}
		if (stdNavn != null) {
			builder.append(stdNavn);
			builder.append(", ");
		}
		if (fams.size() > 0) {
			for (final Integer sFamilie : fams) {
				builder.append(sFamilie);
				builder.append(", ");
			}

		}
		if (spouseNames.size() > 0) {
			builder.append("\r\n�gtef�ller:\r\n");

			for (final String sNavn : spouseNames) {
				builder.append(sNavn);
				builder.append(", ");
			}

		}
		if (begivenheder.size() > 0) {
			builder.append("\r\nBegivenheder:\r\n");

			for (final IndividBegivenhedModel begivenhed : begivenheder) {
				builder.append(begivenhed.toString());
				builder.append(", ");
			}

		}
		if (noter != null) {
			builder.append(noter);
			builder.append(", ");
		}
		if (foedt != null) {
			builder.append(foedt);
			builder.append(", ");
		}
		if (doebt != null) {
			builder.append(doebt);
			builder.append(", ");
		}
		if (doed != null) {
			builder.append(doed);
			builder.append(", ");
		}
		if (begravet != null) {
			builder.append(begravet);
			builder.append(", ");
		}
		if (fam != null) {
			builder.append(fam);
			builder.append(", ");
		}
		if (slgt != null) {
			builder.append(slgt);
			builder.append(", ");
		}
		builder.append("Primary: " + isPrimary());
		return builder.toString();
	}

	/**
	 * Opdater familie i hvilken personen er barn
	 *
	 * @param conn
	 * @param familieId
	 * @return
	 * @throws SQLException
	 */
	public void updateFamc(Connection conn, int familieId) throws SQLException {
		final PreparedStatement statement = conn.prepareStatement(UPDATEFAMC);
		statement.setInt(1, familieId);
		statement.setInt(2, id);
		statement.executeUpdate();
	}

}
