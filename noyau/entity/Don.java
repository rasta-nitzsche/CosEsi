package sample.noyau.entity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.persistence.*;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import sample.noyau.util.ID;

@Entity
public class Don extends Pret {
	private String motif; // type de don (marriage, naissance, ...)

	// Constructors

	public Don() {
	}

	public Don(ID id, int pv, Employe employe, LocalDate dateDemande, double somme, TypePret typePret, String motif) {
		super(id, pv, employe, dateDemande, somme, typePret);
		this.motif = motif;
	}

	public Don(Demande demande, int pV) {
		super(demande, pV);
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
		String dateDebutString = " ";
		String dateProchainString = " ";
		String moisRestantsString = " ";
		String sommeRestanteString = " ";
		String motifString = ((getMotif() == null) ? " " : getMotif());
		String produitString = " ";
		String fournisseurString = " ";
		return String.format("%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s", idString, typePretString, employeString,
				dateString, pvString, sommeString, dateDebutString, dateProchainString, moisRestantsString,
				sommeRestanteString, motifString, produitString, fournisseurString);
	}

	@Override
	public StringProperty getTypeProperty() {
		return new SimpleStringProperty("Don");
	}

}
