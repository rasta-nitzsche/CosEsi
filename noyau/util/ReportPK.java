package sample.noyau.util;

import sample.noyau.entity.PretRemboursable;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

// Primary Key for table "report"
public class ReportPK implements Serializable {

	private PretRemboursable pret;
	private LocalDate dateDebut;

	// Constructors

	public ReportPK() {

	}

	public ReportPK(PretRemboursable pret, LocalDate dateDebut) {
		this.pret = pret;
		this.dateDebut = dateDebut;
	}

	// Overrided methods

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ReportPK))
			return false;
		ReportPK reportPK = (ReportPK) o;
		return getDateDebut().equals(reportPK.getDateDebut()) && getPret().equals(reportPK.getPret());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getDateDebut(), getPret());
	}

	// Getters & Setters

	public LocalDate getDateDebut() {
		return dateDebut;
	}

	public void setDateDebut(LocalDate dateDebut) {
		this.dateDebut = dateDebut;
	}

	public PretRemboursable getPret() {
		return pret;
	}

	public void setPret(PretRemboursable pret) {
		this.pret = pret;
	}
}
