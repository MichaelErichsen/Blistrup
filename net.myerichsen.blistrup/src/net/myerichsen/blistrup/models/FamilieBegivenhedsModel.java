/**
 *
 */
package net.myerichsen.blistrup.models;

/**
 * @author michael
 *
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

	/**
	 * @return the familieId
	 */
	public int getFamilieId() {
		return familieId;
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
	 * @param familieId the familieId to set
	 */
	public void setFamilieId(int familieId) {
		this.familieId = familieId;
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

	@Override
	public String toString() {
		return "FamilieBegivenhedsModel [familieId=" + familieId + ", husfaderAlder=" + husfaderAlder
				+ ", husmoderAlder=" + husmoderAlder + ", id=" + id + ", "
				+ (begType != null ? "begType=" + begType + ", " : "")
				+ (underType != null ? "underType=" + underType + ", " : "")
				+ (dato != null ? "dato=" + dato + ", " : "") + "kildeId=" + kildeId + ", "
				+ (note != null ? "note=" + note + ", " : "") + (detaljer != null ? "detaljer=" + detaljer + ", " : "")
				+ (blistrupId != null ? "blistrupId=" + blistrupId : "") + "]";
	}

}
