package net.myerichsen.blistrup.models;

/**
 * Personnavne
 *
 * @author Michael Erichsen
 * @version 26. jul. 2023
 *
 */
public class PersonNavneModel {
	private int id = 0;
	private String individId = "";
	private String prefix = "";
	private String fornavn = "";
	private String kaelenavn = "";
	private String efternavnePrefix = "";
	private String efternavn = "";
	private String suffix = "";
	private boolean primaerNavn = false;
	private String fonetiskNavn = "";
	private String noter = "";

	/**
	 * @return the efternavn
	 */
	public String getEfternavn() {
		return efternavn;
	}

	/**
	 * @return
	 */
	public String getEfternavnePrefix() {
		return efternavnePrefix;
	}

	/**
	 * @return
	 */
	public String getEternavn() {
		return efternavn;
	}

	public String getFonetiskNavn() {
		return fonetiskNavn;
	}

	public String getFornavn() {
		return fornavn;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	public String getIndividId() {
		return individId;
	}

	public String getKaelenavn() {
		return kaelenavn;
	}

	/**
	 * @return the noter
	 */
	public String getNoter() {
		return noter;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public boolean isPrimaerNavn() {
		return primaerNavn;
	}

	/**
	 * @param efternavn the efternavn to set
	 */
	public void setEfternavn(String efternavn) {
		this.efternavn = efternavn;
	}

	public void setEfternavnePrefix(String efternavnePrefix) {
		this.efternavnePrefix = efternavnePrefix;
	}

	public void setEternavn(String eternavn) {
		this.efternavn = eternavn;
	}

	public void setFonetiskNavn(String fonetiskNavn) {
		this.fonetiskNavn = fonetiskNavn;
	}

	public void setFornavn(String fornavn) {
		this.fornavn = fornavn;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	public void setIndividId(String individId) {
		this.individId = individId;
	}

	public void setKaelenavn(String kaelenavn) {
		this.kaelenavn = kaelenavn;
	}

	/**
	 * @param noter the noter to set
	 */
	public void setNoter(String noter) {
		this.noter = noter;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void setPrimaerNavn(boolean primaerNavn) {
		this.primaerNavn = primaerNavn;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	@Override
	public String toString() {
		return id + ", " + (individId != null ? individId + ", " : "") + (prefix != null ? prefix + ", " : "")
				+ (fornavn != null ? fornavn + ", " : "") + (kaelenavn != null ? kaelenavn + ", " : "")
				+ (efternavnePrefix != null ? efternavnePrefix + ", " : "")
				+ (efternavn != null ? efternavn + ", " : "") + (suffix != null ? suffix + ", " : "") + primaerNavn
				+ ", " + (fonetiskNavn != null ? fonetiskNavn + ", " : "") + (noter != null ? noter : "");
	}
}
