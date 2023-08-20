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
 * @version 20. aug. 2023
 *
 */
public class RaaDataModel {
	private static final String SELECTTYP = "SELECT * FROM BLISTRUP.F9PERSONFAMILIEQ "
			+ "WHERE TYPE = 'D' ORDER BY BEGIV, PID";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			RaaDataModel.select("C:\\Users\\michael\\BlistrupDB");
		} catch (final SQLException e) {
			e.printStackTrace();
		}

	}

	public static void select(String dbPath) throws SQLException {
		RaaDataModel model = null;
		final List<RaaDataModel> liste = new ArrayList<>();
		final Connection conn = DriverManager.getConnection("jdbc:derby:" + dbPath);
		final PreparedStatement statement = conn.prepareStatement(SELECTTYP);
		final ResultSet rs = statement.executeQuery();

		while (rs.next()) {
			model = new RaaDataModel();
			model.setCol0(rs.getString(1));
			model.setBegiv(rs.getString(2));
			model.setPid(rs.getString(3));
			model.setType(rs.getString(4));
			model.setAar(rs.getString(5));
			model.setLbnr(rs.getString(6));
			model.setRolle(rs.getString(7));
			model.setRx(rs.getString(8));
			model.setHp(rs.getString(9));
			model.setHpnr(rs.getString(10));
			model.setStd_navn(rs.getString(11));
			model.setPnyt(rs.getString(12));
			model.setPersfil(rs.getString(13));
			model.setBegivfil(rs.getString(14));
			model.setFader(rs.getString(15));
			model.setFpnr(rs.getString(16));
			model.setModer(rs.getString(17));
			model.setMpnr(rs.getString(18));
			model.setFaelle(rs.getString(19));
			model.setFlpnr(rs.getString(20));
			model.setNavn(rs.getString(21));
			model.setKbnr(rs.getString(22));
			model.setKilde(rs.getString(23));
			model.setKbdel(rs.getString(24));
			model.setTifnr(rs.getString(25));
			model.setSide(rs.getString(26));
			model.setOpslag(rs.getString(27));
			model.setOpnr(rs.getString(28));
			model.setDato(rs.getString(29));
			model.setRef(rs.getString(30));
			model.setFornvn(rs.getString(31));
			model.setEfternvn(rs.getString(32));
			model.setSex(rs.getString(33));
			model.setCivilstand(rs.getString(34));
			model.setErhverv(rs.getString(35));
			model.setAlder(rs.getString(36));
			model.setFoedt(rs.getString(37));
			model.setKval(rs.getString(38));
			model.setFoedested(rs.getString(39));
			model.setHm_(rs.getString(40));
			model.setFoedtdato(rs.getString(41));
			model.setHjdoebt(rs.getString(42));
			model.setDoebtsted(rs.getString(43));
			model.setDoebt(rs.getString(44));
			model.setKonf(rs.getString(45));
			model.setViet(rs.getString(46));
			model.setDoed(rs.getString(47));
			model.setBegr(rs.getString(48));
			model.setFfnvn(rs.getString(49));
			model.setFenvn(rs.getString(50));
			model.setFerhv(rs.getString(51));
			model.setMfnvn(rs.getString(52));
			model.setMenvn(rs.getString(53));
			model.setMalder(rs.getString(54));
			model.setFlfnvn(rs.getString(55));
			model.setFlenvn(rs.getString(56));
			model.setFlerhv(rs.getString(57));
			model.setFlkoen(rs.getString(58));
			model.setFlalder(rs.getString(59));
			model.setFlsted(rs.getString(60));
			model.setTedkod(rs.getString(61));
			model.setStednavn(rs.getString(62));
			model.setSkoledistr(rs.getString(63));
			model.setGaard(rs.getString(64));
			model.setFam0(rs.getString(65));
			model.setSlgt0(rs.getString(66));
			model.setFam(rs.getString(67));
			model.setSlgt(rs.getString(68));
			model.setSlgt00(rs.getString(69));
			model.setSlgt1(rs.getString(70));
			model.setSlgt2(rs.getString(71));
			model.setFam1(rs.getString(72));
			model.setFam2(rs.getString(73));
			model.setHusstand(rs.getString(74));
			model.setMatr_(rs.getString(75));
			model.setMatr1(rs.getString(76));
			model.setRulle(rs.getString(77));
			model.setTitel0(rs.getString(78));
			model.setRolle0(rs.getString(79));
			model.setInit0(rs.getString(80));
			model.setTilg_ref(rs.getString(81));
			model.setAfg_ref(rs.getString(82));
			model.setFra(rs.getString(83));
			model.setTil(rs.getString(84));
			model.setBem(rs.getString(85));
			model.setStilling(rs.getString(86));
			model.setAntfam(rs.getString(87));
			model.setTro(rs.getString(88));
			model.setHandicap(rs.getString(89));
			model.setFlyttet(rs.getString(90));
			model.setGift(rs.getString(91));
			model.setLevb(rs.getString(92));
			model.setDoedeb(rs.getString(93));
			model.setErhvervssted(rs.getString(94));
			model.setSidsteophold(rs.getString(95));
			model.setKildekommentar(rs.getString(96));
			liste.add(model);
		}

		statement.close();

		for (final RaaDataModel raaDataModel : liste) {
			System.out.println(raaDataModel);
		}
	}

	private String col0;
	private String begiv;
	private String pid;
	private String type;
	private String aar;
	private String lbnr;
	private String rolle;
	private String rx;
	private String hp;
	private String hpnr;
	private String std_navn;
	private String pnyt;
	private String persfil;
	private String begivfil;
	private String fader;
	private String fpnr;
	private String moder;
	private String mpnr;
	private String faelle;
	private String flpnr;
	private String navn;
	private String kbnr;
	private String kilde;
	private String kbdel;
	private String tifnr;
	private String side;
	private String opslag;
	private String opnr;
	private String dato;
	private String ref;
	private String fornvn;
	private String efternvn;
	private String sex;
	private String civilstand;
	private String erhverv;
	private String alder;
	private String foedt;
	private String kval;
	private String foedested;
	private String hm_;
	private String foedtdato;
	private String hjdoebt;
	private String doebtsted;
	private String doebt;
	private String konf;
	private String viet;
	private String doed;
	private String begr;
	private String ffnvn;
	private String fenvn;
	private String ferhv;
	private String mfnvn;
	private String menvn;
	private String malder;
	private String flfnvn;
	private String flenvn;
	private String flerhv;
	private String flkoen;
	private String flalder;
	private String flsted;
	private String tedkod;
	private String stednavn;
	private String skoledistr;
	private String gaard;
	private String fam0;
	private String slgt0;
	private String fam;
	private String slgt;
	private String slgt00;
	private String slgt1;
	private String slgt2;
	private String fam1;
	private String fam2;
	private String husstand;
	private String matr_;
	private String matr1;
	private String rulle;
	private String titel0;
	private String rolle0;
	private String init0;
	private String tilg_ref;
	private String afg_ref;
	private String fra;
	private String til;
	private String bem;
	private String stilling;
	private String antfam;
	private String tro;
	private String handicap;
	private String flyttet;
	private String gift;
	private String levb;
	private String doedeb;
	private String erhvervssted;
	private String sidsteophold;
	private String kildekommentar;

	private String slut;

	/**
	 * @return the aar
	 */
	public String getAar() {
		return aar;
	}

	/**
	 * @return the afg_ref
	 */
	public String getAfg_ref() {
		return afg_ref;
	}

	/**
	 * @return the alder
	 */
	public String getAlder() {
		return alder;
	}

	/**
	 * @return the antfam
	 */
	public String getAntfam() {
		return antfam;
	}

	/**
	 * @return the begiv
	 */
	public String getBegiv() {
		return begiv;
	}

	/**
	 * @return the begivfil
	 */
	public String getBegivfil() {
		return begivfil;
	}

	/**
	 * @return the begr
	 */
	public String getBegr() {
		return begr;
	}

	/**
	 * @return the bem
	 */
	public String getBem() {
		return bem;
	}

	/**
	 * @return the civilstand
	 */
	public String getCivilstand() {
		return civilstand;
	}

	/**
	 * @return the col0
	 */
	public String getCol0() {
		return col0;
	}

	/**
	 * @return the dato
	 */
	public String getDato() {
		return dato;
	}

	/**
	 * @return the doebt
	 */
	public String getDoebt() {
		return doebt;
	}

	/**
	 * @return the doebtsted
	 */
	public String getDoebtsted() {
		return doebtsted;
	}

	/**
	 * @return the doed
	 */
	public String getDoed() {
		return doed;
	}

	/**
	 * @return the doedeb
	 */
	public String getDoedeb() {
		return doedeb;
	}

	/**
	 * @return the efternvn
	 */
	public String getEfternvn() {
		return efternvn;
	}

	/**
	 * @return the erhverv
	 */
	public String getErhverv() {
		return erhverv;
	}

	/**
	 * @return the erhvervssted
	 */
	public String getErhvervssted() {
		return erhvervssted;
	}

	/**
	 * @return the fader
	 */
	public String getFader() {
		return fader;
	}

	/**
	 * @return the faelle
	 */
	public String getFaelle() {
		return faelle;
	}

	/**
	 * @return the fam
	 */
	public String getFam() {
		return fam;
	}

	/**
	 * @return the fam0
	 */
	public String getFam0() {
		return fam0;
	}

	/**
	 * @return the fam1
	 */
	public String getFam1() {
		return fam1;
	}

	/**
	 * @return the fam2
	 */
	public String getFam2() {
		return fam2;
	}

	/**
	 * @return the fenvn
	 */
	public String getFenvn() {
		return fenvn;
	}

	/**
	 * @return the ferhv
	 */
	public String getFerhv() {
		return ferhv;
	}

	/**
	 * @return the ffnvn
	 */
	public String getFfnvn() {
		return ffnvn;
	}

	/**
	 * @return the flalder
	 */
	public String getFlalder() {
		return flalder;
	}

	/**
	 * @return the flenvn
	 */
	public String getFlenvn() {
		return flenvn;
	}

	/**
	 * @return the flerhv
	 */
	public String getFlerhv() {
		return flerhv;
	}

	/**
	 * @return the flfnvn
	 */
	public String getFlfnvn() {
		return flfnvn;
	}

	/**
	 * @return the flkoen
	 */
	public String getFlkoen() {
		return flkoen;
	}

	/**
	 * @return the flpnr
	 */
	public String getFlpnr() {
		return flpnr;
	}

	/**
	 * @return the flsted
	 */
	public String getFlsted() {
		return flsted;
	}

	/**
	 * @return the flyttet
	 */
	public String getFlyttet() {
		return flyttet;
	}

	/**
	 * @return the foedested
	 */
	public String getFoedested() {
		return foedested;
	}

	/**
	 * @return the foedt
	 */
	public String getFoedt() {
		return foedt;
	}

	/**
	 * @return the foedtdato
	 */
	public String getFoedtdato() {
		return foedtdato;
	}

	/**
	 * @return the fornvn
	 */
	public String getFornvn() {
		return fornvn;
	}

	/**
	 * @return the fpnr
	 */
	public String getFpnr() {
		return fpnr;
	}

	/**
	 * @return the fra
	 */
	public String getFra() {
		return fra;
	}

	/**
	 * @return the gaard
	 */
	public String getGaard() {
		return gaard;
	}

	/**
	 * @return the gift
	 */
	public String getGift() {
		return gift;
	}

	/**
	 * @return the handicap
	 */
	public String getHandicap() {
		return handicap;
	}

	/**
	 * @return the hjdoebt
	 */
	public String getHjdoebt() {
		return hjdoebt;
	}

	/**
	 * @return the hm_
	 */
	public String getHm_() {
		return hm_;
	}

	/**
	 * @return the hp
	 */
	public String getHp() {
		return hp;
	}

	/**
	 * @return the hpnr
	 */
	public String getHpnr() {
		return hpnr;
	}

	/**
	 * @return the husstand
	 */
	public String getHusstand() {
		return husstand;
	}

	/**
	 * @return the init0
	 */
	public String getInit0() {
		return init0;
	}

	/**
	 * @return the kbdel
	 */
	public String getKbdel() {
		return kbdel;
	}

	/**
	 * @return the kbnr
	 */
	public String getKbnr() {
		return kbnr;
	}

	/**
	 * @return the kilde
	 */
	public String getKilde() {
		return kilde;
	}

	/**
	 * @return the kildekommentar
	 */
	public String getKildekommentar() {
		return kildekommentar;
	}

	/**
	 * @return the konf
	 */
	public String getKonf() {
		return konf;
	}

	/**
	 * @return the kval
	 */
	public String getKval() {
		return kval;
	}

	/**
	 * @return the lbnr
	 */
	public String getLbnr() {
		return lbnr;
	}

	/**
	 * @return the levb
	 */
	public String getLevb() {
		return levb;
	}

	/**
	 * @return the malder
	 */
	public String getMalder() {
		return malder;
	}

	/**
	 * @return the matr_
	 */
	public String getMatr_() {
		return matr_;
	}

	/**
	 * @return the matr1
	 */
	public String getMatr1() {
		return matr1;
	}

	/**
	 * @return the menvn
	 */
	public String getMenvn() {
		return menvn;
	}

	/**
	 * @return the mfnvn
	 */
	public String getMfnvn() {
		return mfnvn;
	}

	/**
	 * @return the moder
	 */
	public String getModer() {
		return moder;
	}

	/**
	 * @return the mpnr
	 */
	public String getMpnr() {
		return mpnr;
	}

	/**
	 * @return the navn
	 */
	public String getNavn() {
		return navn;
	}

	/**
	 * @return the opnr
	 */
	public String getOpnr() {
		return opnr;
	}

	/**
	 * @return the opslag
	 */
	public String getOpslag() {
		return opslag;
	}

	/**
	 * @return the persfil
	 */
	public String getPersfil() {
		return persfil;
	}

	/**
	 * @return the pid
	 */
	public String getPid() {
		return pid;
	}

	/**
	 * @return the pnyt
	 */
	public String getPnyt() {
		return pnyt;
	}

	/**
	 * @return the ref
	 */
	public String getRef() {
		return ref;
	}

	/**
	 * @return the rolle
	 */
	public String getRolle() {
		return rolle;
	}

	/**
	 * @return the rolle0
	 */
	public String getRolle0() {
		return rolle0;
	}

	/**
	 * @return the rulle
	 */
	public String getRulle() {
		return rulle;
	}

	/**
	 * @return the rx
	 */
	public String getRx() {
		return rx;
	}

	/**
	 * @return the sex
	 */
	public String getSex() {
		return sex;
	}

	/**
	 * @return the side
	 */
	public String getSide() {
		return side;
	}

	/**
	 * @return the sidsteophold
	 */
	public String getSidsteophold() {
		return sidsteophold;
	}

	/**
	 * @return the skoledistr
	 */
	public String getSkoledistr() {
		return skoledistr;
	}

	/**
	 * @return the slgt
	 */
	public String getSlgt() {
		return slgt;
	}

	/**
	 * @return the slgt0
	 */
	public String getSlgt0() {
		return slgt0;
	}

	/**
	 * @return the slgt00
	 */
	public String getSlgt00() {
		return slgt00;
	}

	/**
	 * @return the slgt1
	 */
	public String getSlgt1() {
		return slgt1;
	}

	/**
	 * @return the slgt2
	 */
	public String getSlgt2() {
		return slgt2;
	}

	/**
	 * @return the slut
	 */
	public String getSlut() {
		return slut;
	}

	/**
	 * @return the std_navn
	 */
	public String getStd_navn() {
		return std_navn;
	}

	/**
	 * @return the stednavn
	 */
	public String getStednavn() {
		return stednavn;
	}

	/**
	 * @return the stilling
	 */
	public String getStilling() {
		return stilling;
	}

	/**
	 * @return the tedkod
	 */
	public String getTedkod() {
		return tedkod;
	}

	/**
	 * @return the tifnr
	 */
	public String getTifnr() {
		return tifnr;
	}

	/**
	 * @return the til
	 */
	public String getTil() {
		return til;
	}

	/**
	 * @return the tilg_ref
	 */
	public String getTilg_ref() {
		return tilg_ref;
	}

	/**
	 * @return the titel0
	 */
	public String getTitel0() {
		return titel0;
	}

	/**
	 * @return the tro
	 */
	public String getTro() {
		return tro;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return the viet
	 */
	public String getViet() {
		return viet;
	}

	/**
	 * @param aar the aar to set
	 */
	public void setAar(String aar) {
		this.aar = aar;
	}

	/**
	 * @param afg_ref the afg_ref to set
	 */
	public void setAfg_ref(String afg_ref) {
		this.afg_ref = afg_ref;
	}

	/**
	 * @param alder the alder to set
	 */
	public void setAlder(String alder) {
		this.alder = alder;
	}

	/**
	 * @param antfam the antfam to set
	 */
	public void setAntfam(String antfam) {
		this.antfam = antfam;
	}

	/**
	 * @param begiv the begiv to set
	 */
	public void setBegiv(String begiv) {
		this.begiv = begiv;
	}

	/**
	 * @param begivfil the begivfil to set
	 */
	public void setBegivfil(String begivfil) {
		this.begivfil = begivfil;
	}

	/**
	 * @param begr the begr to set
	 */
	public void setBegr(String begr) {
		this.begr = begr;
	}

	/**
	 * @param bem the bem to set
	 */
	public void setBem(String bem) {
		this.bem = bem;
	}

	/**
	 * @param civilstand the civilstand to set
	 */
	public void setCivilstand(String civilstand) {
		this.civilstand = civilstand;
	}

	/**
	 * @param col0 the col0 to set
	 */
	public void setCol0(String col0) {
		this.col0 = col0;
	}

	/**
	 * @param dato the dato to set
	 */
	public void setDato(String dato) {
		this.dato = dato;
	}

	/**
	 * @param doebt the doebt to set
	 */
	public void setDoebt(String doebt) {
		this.doebt = doebt;
	}

	/**
	 * @param doebtsted the doebtsted to set
	 */
	public void setDoebtsted(String doebtsted) {
		this.doebtsted = doebtsted;
	}

	/**
	 * @param doed the doed to set
	 */
	public void setDoed(String doed) {
		this.doed = doed;
	}

	/**
	 * @param doedeb the doedeb to set
	 */
	public void setDoedeb(String doedeb) {
		this.doedeb = doedeb;
	}

	/**
	 * @param efternvn the efternvn to set
	 */
	public void setEfternvn(String efternvn) {
		this.efternvn = efternvn;
	}

	/**
	 * @param erhverv the erhverv to set
	 */
	public void setErhverv(String erhverv) {
		this.erhverv = erhverv;
	}

	/**
	 * @param erhvervssted the erhvervssted to set
	 */
	public void setErhvervssted(String erhvervssted) {
		this.erhvervssted = erhvervssted;
	}

	/**
	 * @param fader the fader to set
	 */
	public void setFader(String fader) {
		this.fader = fader;
	}

	/**
	 * @param faelle the faelle to set
	 */
	public void setFaelle(String faelle) {
		this.faelle = faelle;
	}

	/**
	 * @param fam the fam to set
	 */
	public void setFam(String fam) {
		this.fam = fam;
	}

	/**
	 * @param fam0 the fam0 to set
	 */
	public void setFam0(String fam0) {
		this.fam0 = fam0;
	}

	/**
	 * @param fam1 the fam1 to set
	 */
	public void setFam1(String fam1) {
		this.fam1 = fam1;
	}

	/**
	 * @param fam2 the fam2 to set
	 */
	public void setFam2(String fam2) {
		this.fam2 = fam2;
	}

	/**
	 * @param fenvn the fenvn to set
	 */
	public void setFenvn(String fenvn) {
		this.fenvn = fenvn;
	}

	/**
	 * @param ferhv the ferhv to set
	 */
	public void setFerhv(String ferhv) {
		this.ferhv = ferhv;
	}

	/**
	 * @param ffnvn the ffnvn to set
	 */
	public void setFfnvn(String ffnvn) {
		this.ffnvn = ffnvn;
	}

	/**
	 * @param flalder the flalder to set
	 */
	public void setFlalder(String flalder) {
		this.flalder = flalder;
	}

	/**
	 * @param flenvn the flenvn to set
	 */
	public void setFlenvn(String flenvn) {
		this.flenvn = flenvn;
	}

	/**
	 * @param flerhv the flerhv to set
	 */
	public void setFlerhv(String flerhv) {
		this.flerhv = flerhv;
	}

	/**
	 * @param flfnvn the flfnvn to set
	 */
	public void setFlfnvn(String flfnvn) {
		this.flfnvn = flfnvn;
	}

	/**
	 * @param flkoen the flkoen to set
	 */
	public void setFlkoen(String flkoen) {
		this.flkoen = flkoen;
	}

	/**
	 * @param flpnr the flpnr to set
	 */
	public void setFlpnr(String flpnr) {
		this.flpnr = flpnr;
	}

	/**
	 * @param flsted the flsted to set
	 */
	public void setFlsted(String flsted) {
		this.flsted = flsted;
	}

	/**
	 * @param flyttet the flyttet to set
	 */
	public void setFlyttet(String flyttet) {
		this.flyttet = flyttet;
	}

	/**
	 * @param foedested the foedested to set
	 */
	public void setFoedested(String foedested) {
		this.foedested = foedested;
	}

	/**
	 * @param foedt the foedt to set
	 */
	public void setFoedt(String foedt) {
		this.foedt = foedt;
	}

	/**
	 * @param foedtdato the foedtdato to set
	 */
	public void setFoedtdato(String foedtdato) {
		this.foedtdato = foedtdato;
	}

	/**
	 * @param fornvn the fornvn to set
	 */
	public void setFornvn(String fornvn) {
		this.fornvn = fornvn;
	}

	/**
	 * @param fpnr the fpnr to set
	 */
	public void setFpnr(String fpnr) {
		this.fpnr = fpnr;
	}

	/**
	 * @param fra the fra to set
	 */
	public void setFra(String fra) {
		this.fra = fra;
	}

	/**
	 * @param gaard the gaard to set
	 */
	public void setGaard(String gaard) {
		this.gaard = gaard;
	}

	/**
	 * @param gift the gift to set
	 */
	public void setGift(String gift) {
		this.gift = gift;
	}

	/**
	 * @param handicap the handicap to set
	 */
	public void setHandicap(String handicap) {
		this.handicap = handicap;
	}

	/**
	 * @param hjdoebt the hjdoebt to set
	 */
	public void setHjdoebt(String hjdoebt) {
		this.hjdoebt = hjdoebt;
	}

	/**
	 * @param hm_ the hm_ to set
	 */
	public void setHm_(String hm_) {
		this.hm_ = hm_;
	}

	/**
	 * @param hp the hp to set
	 */
	public void setHp(String hp) {
		this.hp = hp;
	}

	/**
	 * @param hpnr the hpnr to set
	 */
	public void setHpnr(String hpnr) {
		this.hpnr = hpnr;
	}

	/**
	 * @param husstand the husstand to set
	 */
	public void setHusstand(String husstand) {
		this.husstand = husstand;
	}

	/**
	 * @param init0 the init0 to set
	 */
	public void setInit0(String init0) {
		this.init0 = init0;
	}

	/**
	 * @param kbdel the kbdel to set
	 */
	public void setKbdel(String kbdel) {
		this.kbdel = kbdel;
	}

	/**
	 * @param kbnr the kbnr to set
	 */
	public void setKbnr(String kbnr) {
		this.kbnr = kbnr;
	}

	/**
	 * @param kilde the kilde to set
	 */
	public void setKilde(String kilde) {
		this.kilde = kilde;
	}

	/**
	 * @param kildekommentar the kildekommentar to set
	 */
	public void setKildekommentar(String kildekommentar) {
		this.kildekommentar = kildekommentar;
	}

	/**
	 * @param konf the konf to set
	 */
	public void setKonf(String konf) {
		this.konf = konf;
	}

	/**
	 * @param kval the kval to set
	 */
	public void setKval(String kval) {
		this.kval = kval;
	}

	/**
	 * @param lbnr the lbnr to set
	 */
	public void setLbnr(String lbnr) {
		this.lbnr = lbnr;
	}

	/**
	 * @param levb the levb to set
	 */
	public void setLevb(String levb) {
		this.levb = levb;
	}

	/**
	 * @param malder the malder to set
	 */
	public void setMalder(String malder) {
		this.malder = malder;
	}

	/**
	 * @param matr_ the matr_ to set
	 */
	public void setMatr_(String matr_) {
		this.matr_ = matr_;
	}

	/**
	 * @param matr1 the matr1 to set
	 */
	public void setMatr1(String matr1) {
		this.matr1 = matr1;
	}

	/**
	 * @param menvn the menvn to set
	 */
	public void setMenvn(String menvn) {
		this.menvn = menvn;
	}

	/**
	 * @param mfnvn the mfnvn to set
	 */
	public void setMfnvn(String mfnvn) {
		this.mfnvn = mfnvn;
	}

	/**
	 * @param moder the moder to set
	 */
	public void setModer(String moder) {
		this.moder = moder;
	}

	/**
	 * @param mpnr the mpnr to set
	 */
	public void setMpnr(String mpnr) {
		this.mpnr = mpnr;
	}

	/**
	 * @param navn the navn to set
	 */
	public void setNavn(String navn) {
		this.navn = navn;
	}

	/**
	 * @param opnr the opnr to set
	 */
	public void setOpnr(String opnr) {
		this.opnr = opnr;
	}

	/**
	 * @param opslag the opslag to set
	 */
	public void setOpslag(String opslag) {
		this.opslag = opslag;
	}

	/**
	 * @param persfil the persfil to set
	 */
	public void setPersfil(String persfil) {
		this.persfil = persfil;
	}

	/**
	 * @param pid the pid to set
	 */
	public void setPid(String pid) {
		this.pid = pid;
	}

	/**
	 * @param pnyt the pnyt to set
	 */
	public void setPnyt(String pnyt) {
		this.pnyt = pnyt;
	}

	/**
	 * @param ref the ref to set
	 */
	public void setRef(String ref) {
		this.ref = ref;
	}

	/**
	 * @param rolle the rolle to set
	 */
	public void setRolle(String rolle) {
		this.rolle = rolle;
	}

	/**
	 * @param rolle0 the rolle0 to set
	 */
	public void setRolle0(String rolle0) {
		this.rolle0 = rolle0;
	}

	/**
	 * @param rulle the rulle to set
	 */
	public void setRulle(String rulle) {
		this.rulle = rulle;
	}

	/**
	 * @param rx the rx to set
	 */
	public void setRx(String rx) {
		this.rx = rx;
	}

	/**
	 * @param sex the sex to set
	 */
	public void setSex(String sex) {
		this.sex = sex;
	}

	/**
	 * @param side the side to set
	 */
	public void setSide(String side) {
		this.side = side;
	}

	/**
	 * @param sidsteophold the sidsteophold to set
	 */
	public void setSidsteophold(String sidsteophold) {
		this.sidsteophold = sidsteophold;
	}

	/**
	 * @param skoledistr the skoledistr to set
	 */
	public void setSkoledistr(String skoledistr) {
		this.skoledistr = skoledistr;
	}

	/**
	 * @param slgt the slgt to set
	 */
	public void setSlgt(String slgt) {
		this.slgt = slgt;
	}

	/**
	 * @param slgt0 the slgt0 to set
	 */
	public void setSlgt0(String slgt0) {
		this.slgt0 = slgt0;
	}

	/**
	 * @param slgt00 the slgt00 to set
	 */
	public void setSlgt00(String slgt00) {
		this.slgt00 = slgt00;
	}

	/**
	 * @param slgt1 the slgt1 to set
	 */
	public void setSlgt1(String slgt1) {
		this.slgt1 = slgt1;
	}

	/**
	 * @param slgt2 the slgt2 to set
	 */
	public void setSlgt2(String slgt2) {
		this.slgt2 = slgt2;
	}

	/**
	 * @param slut the slut to set
	 */
	public void setSlut(String slut) {
		this.slut = slut;
	}

	/**
	 * @param std_navn the std_navn to set
	 */
	public void setStd_navn(String std_navn) {
		this.std_navn = std_navn;
	}

	/**
	 * @param stednavn the stednavn to set
	 */
	public void setStednavn(String stednavn) {
		this.stednavn = stednavn;
	}

	/**
	 * @param stilling the stilling to set
	 */
	public void setStilling(String stilling) {
		this.stilling = stilling;
	}

	/**
	 * @param tedkod the tedkod to set
	 */
	public void setTedkod(String tedkod) {
		this.tedkod = tedkod;
	}

	/**
	 * @param tifnr the tifnr to set
	 */
	public void setTifnr(String tifnr) {
		this.tifnr = tifnr;
	}

	/**
	 * @param til the til to set
	 */
	public void setTil(String til) {
		this.til = til;
	}

	/**
	 * @param tilg_ref the tilg_ref to set
	 */
	public void setTilg_ref(String tilg_ref) {
		this.tilg_ref = tilg_ref;
	}

	/**
	 * @param titel0 the titel0 to set
	 */
	public void setTitel0(String titel0) {
		this.titel0 = titel0;
	}

	/**
	 * @param tro the tro to set
	 */
	public void setTro(String tro) {
		this.tro = tro;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @param viet the viet to set
	 */
	public void setViet(String viet) {
		this.viet = viet;
	}

	@Override
	public String toString() {
		String s = (col0 != null ? col0 + ", " : "") + (begiv != null ? begiv + ", " : "")
				+ (pid != null ? pid + ", " : "") + (type != null ? type + ", " : "") + (aar != null ? aar + ", " : "")
				+ (lbnr != null ? lbnr + ", " : "") + (rolle != null ? rolle + ", " : "")
				+ (rx != null ? rx + ", " : "") + (hp != null ? hp + ", " : "") + (hpnr != null ? hpnr + ", " : "")
				+ (std_navn != null ? std_navn + ", " : "") + (pnyt != null ? pnyt + ", " : "")
				+ (persfil != null ? persfil + ", " : "") + (begivfil != null ? begivfil + ", " : "")
				+ (fader != null ? fader + ", " : "") + (fpnr != null ? fpnr + ", " : "")
				+ (moder != null ? moder + ", " : "") + (mpnr != null ? mpnr + ", " : "")
				+ (faelle != null ? faelle + ", " : "") + (flpnr != null ? flpnr + ", " : "")
				+ (navn != null ? navn + ", " : "") + (kbnr != null ? kbnr + ", " : "")
				+ (kilde != null ? kilde + ", " : "") + (kbdel != null ? kbdel + ", " : "")
				+ (tifnr != null ? tifnr + ", " : "") + (side != null ? side + ", " : "")
				+ (opslag != null ? opslag + ", " : "") + (opnr != null ? opnr + ", " : "")
				+ (dato != null ? dato + ", " : "") + (ref != null ? ref + ", " : "")
				+ (fornvn != null ? fornvn + ", " : "") + (efternvn != null ? efternvn + ", " : "")
				+ (sex != null ? sex + ", " : "") + (civilstand != null ? civilstand + ", " : "")
				+ (erhverv != null ? erhverv + ", " : "") + (alder != null ? alder + ", " : "")
				+ (foedt != null ? foedt + ", " : "") + (kval != null ? kval + ", " : "")
				+ (foedested != null ? foedested + ", " : "") + (hm_ != null ? hm_ + ", " : "")
				+ (foedtdato != null ? foedtdato + ", " : "") + (hjdoebt != null ? hjdoebt + ", " : "")
				+ (doebtsted != null ? doebtsted + ", " : "") + (doebt != null ? doebt + ", " : "")
				+ (konf != null ? konf + ", " : "") + (viet != null ? viet + ", " : "")
				+ (doed != null ? doed + ", " : "") + (begr != null ? begr + ", " : "")
				+ (ffnvn != null ? ffnvn + ", " : "") + (fenvn != null ? fenvn + ", " : "")
				+ (ferhv != null ? ferhv + ", " : "") + (mfnvn != null ? mfnvn + ", " : "")
				+ (menvn != null ? menvn + ", " : "") + (malder != null ? malder + ", " : "")
				+ (flfnvn != null ? flfnvn + ", " : "") + (flenvn != null ? flenvn + ", " : "")
				+ (flerhv != null ? flerhv + ", " : "") + (flkoen != null ? flkoen + ", " : "")
				+ (flalder != null ? flalder + ", " : "") + (flsted != null ? flsted + ", " : "")
				+ (tedkod != null ? tedkod + ", " : "") + (stednavn != null ? stednavn + ", " : "")
				+ (skoledistr != null ? skoledistr + ", " : "") + (gaard != null ? gaard + ", " : "")
				+ (fam0 != null ? fam0 + ", " : "") + (slgt0 != null ? slgt0 + ", " : "")
				+ (fam != null ? fam + ", " : "") + (slgt != null ? slgt + ", " : "")
				+ (slgt00 != null ? slgt00 + ", " : "") + (slgt1 != null ? slgt1 + ", " : "")
				+ (slgt2 != null ? slgt2 + ", " : "") + (fam1 != null ? fam1 + ", " : "")
				+ (fam2 != null ? fam2 + ", " : "") + (husstand != null ? husstand + ", " : "")
				+ (matr_ != null ? matr_ + ", " : "") + (matr1 != null ? matr1 + ", " : "")
				+ (rulle != null ? rulle + ", " : "") + (titel0 != null ? titel0 + ", " : "")
				+ (rolle0 != null ? rolle0 + ", " : "") + (init0 != null ? init0 + ", " : "")
				+ (tilg_ref != null ? tilg_ref + ", " : "") + (afg_ref != null ? afg_ref + ", " : "")
				+ (fra != null ? fra + ", " : "") + (til != null ? til + ", " : "") + (bem != null ? bem + ", " : "")
				+ (stilling != null ? stilling + ", " : "") + (antfam != null ? antfam + ", " : "")
				+ (tro != null ? tro + ", " : "") + (handicap != null ? handicap + ", " : "")
				+ (flyttet != null ? flyttet + ", " : "") + (gift != null ? gift + ", " : "")
				+ (levb != null ? levb + ", " : "") + (doedeb != null ? doedeb + ", " : "")
				+ (erhvervssted != null ? erhvervssted + ", " : "") + (sidsteophold != null ? sidsteophold + ", " : "")
				+ (kildekommentar != null ? kildekommentar + ", " : "") + (slut != null ? slut : "");
		for (int i = 0; i < 10; i++) {
			s = s.replace(", ,", ",");
		}

		return s.replace("Qo", "ø").replace("Qe", "æ").replace("Qa", "a");
	}

}
