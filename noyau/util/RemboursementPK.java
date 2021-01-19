package sample.noyau.util;

import sample.noyau.entity.Pret;
import sample.noyau.entity.PretRemboursable;
import sample.noyau.entity.TypeRemboursement;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class RemboursementPK implements Serializable {
	private PretRemboursable pret;
	private LocalDate date; // Date de remboursement (prélèvement)
	// Car on peut avoir deux remboursements dans la meme date (pour le cas: Apport
	// externe + Prelevement)
	private TypeRemboursement typeRemboursement;

	// Constructors

	public RemboursementPK() {
	}

	public RemboursementPK(PretRemboursable pret, LocalDate date, TypeRemboursement typeRemboursement) {
		this.pret = pret;
		this.date = date;
		this.typeRemboursement = typeRemboursement;
	}

	// Overrided methods

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof RemboursementPK))
			return false;
		RemboursementPK that = (RemboursementPK) o;
		return date.isEqual(that.date) && pret.equals(that.pret) && typeRemboursement.equals(that.typeRemboursement);
	}

	@Override
	public int hashCode() {
		return Objects.hash(date, pret, typeRemboursement);
	}

	// Getters & Setters

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public Pret getPret() {
		return pret;
	}

	public void setPret(PretRemboursable pret) {
		this.pret = pret;
	}

	public TypeRemboursement getTypeRemboursement() {
		return typeRemboursement;
	}

	public void setTypeRemboursement(TypeRemboursement typeRemboursement) {
		this.typeRemboursement = typeRemboursement;
	}

}
