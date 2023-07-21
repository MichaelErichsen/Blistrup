package net.myerichsen.blistrup.models;

/**
 * @author michael
 *
 */
public class IndividBegivenhedsModel extends Begivenhedsmodel {

	private int individId;
	private int alder;
	private String rolle;

	private String foedt;

	/**
	 * @return the alder
	 */
	public int getAlder() {
		return alder;
	}

	/**
	 * @return the foedt
	 */
	public String getFoedt() {
		return foedt;
	}

	/**
	 * @return the individId
	 */
	public int getIndividId() {
		return individId;
	}

	/**
	 * @return the rolle
	 */
	public String getRolle() {
		return rolle;
	}

	/**
	 * @param alder the alder to set
	 */
	public void setAlder(int alder) {
		this.alder = alder;
	}

	/**
	 * @param foedt the foedt to set
	 */
	public void setFoedt(String foedt) {
		this.foedt = foedt;
	}

	/**
	 * @param individId the individId to set
	 */
	public void setIndividId(int individId) {
		this.individId = individId;
	}

	/**
	 * @param rolle the rolle to set
	 */
	public void setRolle(String rolle) {
		this.rolle = rolle;
	}

	@Override
	public String toString() {
		return "IndividBegivenhedsModel [individId=" + individId + ", alder=" + alder + ", "
				+ (rolle != null ? "rolle=" + rolle + ", " : "") + (foedt != null ? "foedt=" + foedt + ", " : "")
				+ "id=" + id + ", " + (begType != null ? "begType=" + begType + ", " : "")
				+ (underType != null ? "underType=" + underType + ", " : "")
				+ (dato != null ? "dato=" + dato + ", " : "") + "kildeId=" + kildeId + ", "
				+ (note != null ? "note=" + note + ", " : "") + (detaljer != null ? "detaljer=" + detaljer + ", " : "")
				+ (blistrupId != null ? "blistrupId=" + blistrupId : "") + "]";
	}

}
