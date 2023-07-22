package net.myerichsen.blistrup.models;

/**
 * @author Michael Erichsen
 * @version 21. jul. 2023
 */
public class FamilieBegivenhedsModel extends Begivenhedsmodel {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	private int familieId;
	private int husfaderAlder;
	private int husmoderAlder;
	private int aar;
	private String rolle;
	private int gom;
	private int brud;
	private String gaard;

	/**
	 * @return the aar
	 */
	public int getAar() {
		return aar;
	}

	/**
	 * @return the brud
	 */
	public int getBrud() {
		return brud;
	}

	/**
	 * @return the familieId
	 */
	public int getFamilieId() {
		return familieId;
	}

	/**
	 * @return the gaard
	 */
	public String getGaard() {
		return gaard;
	}

	/**
	 * @return the gom
	 */
	public int getGom() {
		return gom;
	}

	/**
	 * @return the husfaderAlder
	 */
	public int getHusfaderAlder() {
		return husfaderAlder;
	}

	/**
	 * @return the husmoderAlder
	 */
	public int getHusmoderAlder() {
		return husmoderAlder;
	}

	/**
	 * @return the rolle
	 */
	public String getRolle() {
		return rolle;
	}

	/**
	 * @param aar the aar to set
	 */
	public void setAar(int aar) {
		this.aar = aar;
	}

	/**
	 * @param brud the brud to set
	 */
	public void setBrud(int brud) {
		this.brud = brud;
	}

	/**
	 * @param familieId the familieId to set
	 */
	public void setFamilieId(int familieId) {
		this.familieId = familieId;
	}

	/**
	 * @param gaard the gaard to set
	 */
	public void setGaard(String gaard) {
		this.gaard = gaard;
	}

	/**
	 * @param gom the gom to set
	 */
	public void setGom(int gom) {
		this.gom = gom;
	}

	/**
	 * @param husfaderAlder the husfaderAlder to set
	 */
	public void setHusfaderAlder(int husfaderAlder) {
		this.husfaderAlder = husfaderAlder;
	}

	/**
	 * @param husmoderAlder the husmoderAlder to set
	 */
	public void setHusmoderAlder(int husmoderAlder) {
		this.husmoderAlder = husmoderAlder;
	}

	/**
	 * @param rolle the rolle to set
	 */
	public void setRolle(String rolle) {
		this.rolle = rolle;
	}

	@Override
	public String toString() {
		return familieId + ", " + husfaderAlder + ", " + husmoderAlder + ", " + aar + ", "
				+ (rolle != null ? rolle + ", " : "") + gom + ", " + brud + ", " + (gaard != null ? gaard + ", " : "")
				+ id + ", " + (begType != null ? begType + ", " : "") + (underType != null ? underType + ", " : "")
				+ (dato != null ? dato + ", " : "") + kildeId + ", " + (note != null ? note + ", " : "")
				+ (detaljer != null ? detaljer + ", " : "") + (blistrupId != null ? blistrupId : "");
	}

}
