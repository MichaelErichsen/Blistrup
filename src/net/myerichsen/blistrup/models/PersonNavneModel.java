package net.myerichsen.blistrup.models;

/**
 * @author Michael Erichsen
 * @version 21. jul. 2023
 *
 */
public class PersonNavneModel {
	private int id;
	private String individId;
	private String prefix;
	private String fornavn;
	private String kaelenavn;
	private String efternavnePrefix;
	private String efternavn;
	private String suffix;
	private boolean primaerNavn = false;
	private String fonetiskNavn;

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

	public String getIndividId() {
		return individId;
	}

	public String getKaelenavn() {
		return kaelenavn;
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
		return "PersonNavneModel [id=" + id + ", " + (individId != null ? "individId=" + individId + ", " : "")
				+ (prefix != null ? "prefix=" + prefix + ", " : "")
				+ (fornavn != null ? "fornavn=" + fornavn + ", " : "")
				+ (kaelenavn != null ? "kaelenavn=" + kaelenavn + ", " : "")
				+ (efternavnePrefix != null ? "efternavnePrefix=" + efternavnePrefix + ", " : "")
				+ (efternavn != null ? "efternavn=" + efternavn + ", " : "")
				+ (suffix != null ? "suffix=" + suffix + ", " : "") + "primaerNavn=" + primaerNavn + ", "
				+ (fonetiskNavn != null ? "fonetiskNavn=" + fonetiskNavn : "") + "]";
	}
}
