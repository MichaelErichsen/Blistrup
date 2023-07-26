package net.myerichsen.blistrup.models;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Erichsen
 * @version 26. jul. 2023
 */
public class IndividModel {
	private static final String SELECT1 = "SELECT * FROM BLISTRUP.INDIVID FETCH FIRST 200 ROWS ONLY";
	private static final String SELECT2 = "SELECT * FROM BLISTRUP.PERSONNAVN WHERE INDIVIDID = ?";
	private static final String SELECT3 = "SELECT * FROM BLISTRUP.FAMILIE WHERE HUSFADER = ? OR HUSMODER = ?";

	/**
	 * @return
	 * @throws SQLException
	 */
	public static IndividModel[] getData(String dbPath) throws SQLException {
		IndividModel model;
		final List<IndividModel> liste = new ArrayList<>();
		final Connection conn = DriverManager.getConnection("jdbc:derby:" + dbPath);
		final PreparedStatement statement1 = conn.prepareStatement(SELECT1);
		final PreparedStatement statement2 = conn.prepareStatement(SELECT2);
		PreparedStatement statement3;
		final ResultSet rs1 = statement1.executeQuery();
		ResultSet rs2, rs3;
		int id = 0;
		int husfader = 0;
		int husmoder = 0;
		String koen = "";

		while (rs1.next()) {
			id = rs1.getInt("ID");
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
					model.setBlistrupId(rs1.getString("BLISTRUPID").trim());
					model.setFamc(rs1.getInt("FAMC"));
					model.setStdNavn(rs2.getString("STDNAVN").trim());
					model.setFonetiskNavn(rs2.getString("FONETISKNAVN").trim());

					statement3 = conn.prepareStatement(SELECT3);
					statement3.setInt(1, id);
					statement3.setInt(2, id);
					rs3 = statement3.executeQuery();

					if (rs3.next()) {
						model.getFams().add(rs3.getInt("ID"));
						husfader = rs3.getInt("HUSFADER");
						husmoder = rs3.getInt("HUSMODER");
					}

					if ("k".equals(koen) && husfader > 0) {
						statement3 = conn.prepareStatement(SELECT2);
						statement3.setInt(1, husfader);
						rs3 = statement3.executeQuery();

						if (rs3.next()) {
							model.getSpouseNames().add(rs3.getString("STDNAVN").trim());
						}
					}

					if ("m".equals(koen) && husmoder > 0) {
						statement3 = conn.prepareStatement(SELECT2);
						statement3.setInt(1, husmoder);
						rs3 = statement3.executeQuery();

						if (rs3.next()) {
							model.getSpouseNames().add(rs3.getString("STDNAVN").trim());
						}
					}

					liste.add(model);
				}
			}
		}
		final IndividModel[] array = new IndividModel[liste.size()];

		for (int i = 0; i < liste.size(); i++) {
			array[i] = liste.get(i);
		}

		return array;
	}

	private int id = 0;
	private int famc = 0;
	private String koen = "";
	private String BlistrupId = "";
	private List<PersonNavneModel> personNavneListe = new ArrayList<>();
	private String fonetiskNavn = "";
	private String stdNavn = "";
	private List<Integer> fams = new ArrayList<>();
	private List<String> spouseNames = new ArrayList<>();
	private List<IndividBegivenhedModel> begivenheder = new ArrayList<>();
	private String noter = "";
	private Date foedt;
	private Date doebt;
	private Date doed;
	private Date begravet;

	/**
	 * @return the begivenheder
	 */
	public List<IndividBegivenhedModel> getBegivenheder() {
		return begivenheder;
	}

	/**
	 * @return the begravet
	 */
	public Date getBegravet() {
		return begravet;
	}

	/**
	 * @return the blistrupId
	 */
	public String getBlistrupId() {
		return BlistrupId;
	}

	/**
	 * @return the doebt
	 */
	public Date getDoebt() {
		return doebt;
	}

	/**
	 * @return the doed
	 */
	public Date getDoed() {
		return doed;
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
	public Date getFoedt() {
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
	 * @param begivenheder the begivenheder to set
	 */
	public void setBegivenheder(List<IndividBegivenhedModel> begivenheder) {
		this.begivenheder = begivenheder;
	}

	/**
	 * @param begravet the begravet to set
	 */
	public void setBegravet(Date begravet) {
		this.begravet = begravet;
	}

	/**
	 * @param blistrupId the blistrupId to set
	 */
	public void setBlistrupId(String blistrupId) {
		BlistrupId = blistrupId;
	}

	/**
	 * @param doebt the doebt to set
	 */
	public void setDoebt(Date doebt) {
		this.doebt = doebt;
	}

	/**
	 * @param doed the doed to set
	 */
	public void setDoed(Date doed) {
		this.doed = doed;
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
	public void setFoedt(Date foedt) {
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
			builder.append("\r\nÆgtefæller:\r\n");

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
		}
		return builder.toString();
	}

}
