package sample.noyau.entity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

import javax.persistence.*;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import sample.noyau.util.ID;

@Entity
public class PretElectromenager extends PretRemboursable {
	private String produit;
	private String fournisseur;

	// Constructors

	public PretElectromenager() {
	}

	public PretElectromenager(ID id, int pv, Employe employe, LocalDate dateDemande, double somme, TypePret typePret,
			LocalDate dateDebut, String produit, String fournisseur) {
		super(id, pv, employe, dateDemande, somme, typePret, dateDebut);
		this.produit = produit;
		this.fournisseur = fournisseur;
	}

	public PretElectromenager(Demande demande, int pV, LocalDate dateDebut) {
		super(demande, pV, dateDebut);
		this.produit = demande.getDescription();
		this.fournisseur = demande.getFournisseur();
	}

	// Overrided methods

	@Override
	public String toString() {
		return super.toString() + "\n Produit: " + produit + "\n Fournisseur: " + fournisseur;
	}

	// Getters & Setters

	public String getProduit() {
		return produit;
	}

	public void setProduit(String produit) {
		this.produit = produit;
	}

	public String getFournisseur() {
		return fournisseur;
	}

	public void setFournisseur(String fournisseur) {
		this.fournisseur = fournisseur;
	}

	public double getSommeRembourseFrounisser() {
		Iterator<Remboursement> it = getRemboursementsList().iterator();
		Remboursement remboursement;
		double sommeRembouseFrounisseur = 0;
		while (it.hasNext()) {
			remboursement = it.next();
			if (remboursement.getTypeRemboursement().equals(TypeRemboursement.REMBOURSEMENT_FOURNISSEUR))
				sommeRembouseFrounisseur = sommeRembouseFrounisseur + remboursement.getSomme();
		}
		return sommeRembouseFrounisseur;
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
		String motifString = " ";
		String produitString = ((getProduit() == null) ? " " : getProduit());
		String fournisseurString = ((getFournisseur() == null) ? " " : getFournisseur());
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
		return new SimpleStringProperty("Prêt Electroménager");
	}
}
