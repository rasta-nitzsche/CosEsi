package sample.noyau.entity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.persistence.*;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import sample.noyau.util.ID;

@Entity
public class PretSocial extends PretRemboursable {
	private String motif; // type du prêt social (AADL, voiture, ...)

	// Constructors

	public PretSocial() {
	}

	public PretSocial(ID id, int pv, Employe employe, LocalDate dateDemande, double somme, TypePret typePret,
			LocalDate dateDebut, String motif) {
		super(id, pv, employe, dateDemande, somme, typePret, dateDebut);
		this.motif = motif;
	}

	public PretSocial(Demande demande, int pV, LocalDate dateDebut) {
		super(demande, pV, dateDebut);
		this.motif = demande.getDescription();
	}

	// Overrided methods

	@Override
	public String toString() {
		return super.toString() + "\n Motif: " + motif;
	}

	// Getters & Setters

	public String getMotif() {
		return motif;
	}

	public void setMotif(String motif) {
		this.motif = motif;
	}

	@Override
	public String getExcelLine() {
		String idString = getId();
		String typePretString = TypePret.getType(this).toString();
		String employeString = getEmploye().getNom() + " " + getEmploye().getPrenom() + " /" + getEmploye().getId();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		String dateString = getDateDemande().format(format);
		String pvString = String.valueOf(getPv());
		String sommeString = String.valueOf(getSomme());
		String dateDebutString = ((getDateDebut() == null) ? " " : getDateDebut().toString());
		String dateProchainString = ((getDateProchain() == null) ? " " : getDateProchain().toString());
		String moisRestantsString = Integer.toString(getNbMois());
		String sommeRestanteString = Double.toString(getSommeRestante());
		String motifString = ((getMotif() == null) ? " " : getMotif());
		String produitString = " ";
		String fournisseurString = " ";
		String moisString = "";
		int months = 0;
		if (getDateDebut() != null) {
			if (getDateProchain() == null) {
				if (getDernierReport() != null)
					months = getDateDebut().until(getDernierReport().getDateDebut()).getMonths();
			}
			else
				months = getDateDebut().until(getDateProchain()).getMonths();
			LocalDate date = getDateDebut();
			for (int i = 0; i < months; ++i) {
				if (estRembourse(date))
					moisString += ";" + "Remboursement (" + date + ")";
				if (estReporte(date))
					moisString += ";" + "Report (" + date + ")";
				date = date.plusMonths(1);
			}
		}
		return String.format("%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s%s", idString, typePretString, employeString,
				dateString, pvString, sommeString, dateDebutString, dateProchainString, moisRestantsString,
				sommeRestanteString, motifString, produitString, fournisseurString, moisString);
	}

	@Override
	public StringProperty getTypeProperty() {
		return new SimpleStringProperty("Prêt social");
	}

}
