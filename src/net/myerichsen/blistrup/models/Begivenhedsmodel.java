package net.myerichsen.blistrup.models;

import java.sql.Date;

/**
 * @author Michael Erichsen
 * @version 30. aug. 2023
 */
public abstract class Begivenhedsmodel {
	protected int id = 0;
	protected String begType = "";
	protected String underType = "";
	protected Date dato;
	protected int kildeId = 0;
	protected String note = "";
	protected String detaljer = "";
	protected String blistrupId = "";
	protected String stedNavn = "";

	/**
	 * @return the begType
	 */
	public String getBegType() {
		return begType;
	}

	/**
	 * @return the blistrupId
	 */
	public String getBlistrupId() {
		return blistrupId;
	}

	/**
	 * @return the dato
	 */
	public Date getDato() {
		return dato;
	}

	/**
	 * @return the detaljer
	 */
	public String getDetaljer() {
		return detaljer;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the kildeId
	 */
	public int getKildeId() {
		return kildeId;
	}

	/**
	 * @return the note
	 */
	public String getNote() {
		return note;
	}

	/**
	 * @return the stedNavn
	 */
	public String getStedNavn() {
		return stedNavn;
	}

	/**
	 * @return the underType
	 */
	public String getUnderType() {
		return underType;
	}

	/**
	 * @param begType the begType to set
	 */
	public void setBegType(String begType) {
		this.begType = begType;
	}

	/**
	 * @param blistrupId the blistrupId to set
	 */
	public void setBlistrupId(String blistrupId) {
		this.blistrupId = blistrupId;
	}

	/**
	 * @param dato the dato to set
	 */
	public void setDato(Date dato) {
		this.dato = dato;
	}

	/**
	 * @param detaljer the detaljer to set
	 */
	public void setDetaljer(String detaljer) {
		this.detaljer = detaljer;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @param kildeId the kildeId to set
	 */
	public void setKildeId(int kildeId) {
		this.kildeId = kildeId;
	}

	/**
	 * @param note the note to set
	 */
	public void setNote(String note) {
		this.note = note;
	}

	/**
	 * @param stedNavn the stedNavn to set
	 */
	public void setStedNavn(String stedNavn) {
		this.stedNavn = stedNavn;
	}

	/**
	 * @param underType the underType to set
	 */
	public void setUnderType(String underType) {
		this.underType = underType;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append(id);
		builder.append(", ");
		if (begType != null) {
			builder.append(begType);
			builder.append(", ");
		}
		if (underType != null) {
			builder.append(underType);
			builder.append(", ");
		}
		if (dato != null) {
			builder.append(dato);
			builder.append(", ");
		}
		builder.append(kildeId);
		builder.append(", ");
		if (note != null) {
			builder.append(note);
			builder.append(", ");
		}
		if (detaljer != null) {
			builder.append(detaljer);
			builder.append(", ");
		}
		if (blistrupId != null) {
			builder.append(blistrupId);
			builder.append(", ");
		}
		if (stedNavn != null) {
			builder.append(stedNavn);
		}
		return builder.toString();
	}

}
