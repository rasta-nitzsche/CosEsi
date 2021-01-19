package sample.noyau.entity;

import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import javafx.beans.property.*;
import sample.noyau.service.COS;

import sample.noyau.util.ID;

@Entity
public class Demande {
	@Id
	private String id; // "num_annee-sociale" (ex. : "1_2020") (utilise ID.toString())
	private TypePret typePret; // prêt social, électroménager, don, ...
	@ManyToOne
	@JoinColumn(name = "employeId", nullable = false)
	private Employe employe;
	private LocalDate dateDemande;
	private Status status; // en cours ou refusé
	// TODO: (changed) pV to pv
	// numéro du PV (pour la demande refusée)
	// prends la valeur 0 pour une demande en cours
	private int pv;
	private double somme;
	private String description; // motif pour le prêt social et le don, et produit pour le prêt électroménager
	private String fournisseur; // pour le prêt électroménager

	// Constructors

	public Demande() {
	}

	public Demande(ID id, TypePret typePret, Employe employe, LocalDate dateDemande, Status status, int pv,
			double somme, String description, String fournisseur) {
		this.id = id.toString();
		this.typePret = typePret;
		employe.getDemandesList().add(this);
		this.employe = employe;
		this.dateDemande = dateDemande;
		this.status = status;
		this.pv = pv;
		this.somme = somme;
		this.description = description;
		this.fournisseur = fournisseur;
	}

	public Demande(ID id, TypePret typePret, Employe employe, LocalDate dateDemande, Status status, double somme,
			String description, String fournisseur) {
		this.id = id.toString();
		this.typePret = typePret;
		employe.getDemandesList().add(this);
		this.employe = employe;
		this.dateDemande = dateDemande;
		this.status = status;
		this.pv = pv;
		this.somme = somme;
		this.description = description;
		this.fournisseur = fournisseur;
	}

	public Demande(TypePret typePret, Employe employe, LocalDate dateDemande, double somme, String description,
			String fournisseur) {
		this.id = new ID().toString();
		this.typePret = typePret;
		employe.getDemandesList().add(this);
		this.employe = employe;
		this.dateDemande = dateDemande;
		this.status = Status.EN_COURS;
		this.somme = somme;
		this.description = description;
		this.fournisseur = fournisseur;
	}

	/** Pret Electromenager. */
	public Demande(Employe employe, LocalDate dateDemande, double somme, String produit, String fournisseur) {
		this.id = new ID(dateDemande).toString();
		this.typePret = TypePret.ELECTROMENAGER;
		employe.getDemandesList().add(this);
		this.employe = employe;
		this.dateDemande = dateDemande;
		this.status = Status.EN_COURS;
		this.somme = somme;
		this.description = produit;
		this.fournisseur = fournisseur;
	}

	/** Pour les prets social ou les dons. */
	public Demande(TypePret typePret, Employe employe, LocalDate dateDemande, double somme, String motif) {
		this.id = new ID(dateDemande).toString();
		this.typePret = typePret;
		employe.getDemandesList().add(this);
		this.employe = employe;
		this.dateDemande = dateDemande;
		this.status = Status.EN_COURS;
		this.somme = somme;
		this.description = motif;
		this.fournisseur = null;
	}

	// Overrided methods

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Demande))
			return false;
		Demande demande = (Demande) o;
		return getId().equals(demande.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId());
	}

	@Override
	public String toString() {
		return "Demande: " + id + "\n Type: " + typePret + "\n PV: " + pv + "\n Status:" + status + "\n Employé: "
				+ employe.getNomComplet() + "\n DateDemande: " + dateDemande + "\n Somme: " + somme + "\n Description:"
				+ description;
	}

	// Getters & Setters

	public TypePret getTypePret() {
		return typePret;
	}

	public void setTypePret(TypePret typePret) {
		this.typePret = typePret;
	}

	public Employe getEmploye() {
		return employe;
	}

	public void setEmploye(Employe employe) {
		this.employe = employe;
	}

	public LocalDate getDateDemande() {
		return dateDemande;
	}

	public void setDateDemande(LocalDate dateDemande) {
		this.dateDemande = dateDemande;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public int getPV() {
		return pv;
	}

	public void setPV(int pv) {
		this.pv = pv;
	}

	public double getSomme() {
		return somme;
	}

	public void setSomme(double somme) {
		this.somme = somme;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFournisseur() {
		return fournisseur;
	}

	public void setFournisseur(String fournisseur) {
		this.fournisseur = fournisseur;
	}

	public String getId() {
		return id;
	}

	public Year getAnneeSociale() {
		return COS.anneeSociale(dateDemande);
	}

	// retourne le format Excel de la demande
	public String getExcelLine() {
		String idString = id;
		String typePretString = typePret.toString();
		String employeString = employe.getNom() + " " + employe.getPrenom() + " /" + employe.getId();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		String dateString = dateDemande.format(format);
		String statusString = status.toString();
		String pvString = String.valueOf(pv);
		String sommeString = String.valueOf(somme);
		String descString = ((description == null) ? " " : description);
		String fournisseurString = ((fournisseur == null) ? " " : fournisseur);
		return String.format("%s;%s;%s;%s;%s;%s;%s;%s;%s", idString, typePretString, employeString, dateString,
				statusString, pvString, sommeString, descString, fournisseurString);
	}

	// retourne le format Excel de la demande (de type prêt social ou don)
	public String getExcelLineSocialDon() {
		String idString = id;
		String employeString = employe.getNom() + " " + employe.getPrenom() + " /" + employe.getId();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		String dateString = dateDemande.format(format);
		String statusString = status.toString();
		String pvString = String.valueOf(pv);
		String sommeString = String.valueOf(somme);
		String descString = ((description == null) ? " " : description);
		return String.format("%s;%s;%s;%s;%s;%s;%s", idString, employeString, dateString, statusString, pvString,
				sommeString, descString);
	}

	// retourne le format Excel de la demande (de type prêt électroménager)
	public String getExcelLineElectromenager() {
		String idString = id;
		String employeString = employe.getNom() + " " + employe.getPrenom() + " /" + employe.getId();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		String dateString = dateDemande.format(format);
		String statusString = status.toString();
		String pvString = String.valueOf(pv);
		String sommeString = String.valueOf(somme);
		String descString = ((description == null) ? " " : description);
		String fournisseurString = ((fournisseur == null) ? " " : fournisseur);
		return String.format("%s;%s;%s;%s;%s;%s;%s;%s", idString, employeString, dateString, statusString, pvString,
				sommeString, descString, fournisseurString);
	}

	public SimpleStringProperty getDescriptionProperty() {
		return new SimpleStringProperty(description);
	}

	public SimpleStringProperty getTypeProperty() {
		return new SimpleStringProperty(typePret.toString());
	}

	public SimpleStringProperty getDateProperty() {
		return new SimpleStringProperty(dateDemande.toString());
	}

	public DoubleProperty getSommeProperty() {
		return new SimpleDoubleProperty(somme);
	}

	public IntegerProperty getPVProperty() {
		return new SimpleIntegerProperty(pv);
	}

	public SimpleStringProperty getFournisseurProperty() {
		return new SimpleStringProperty(fournisseur);
	}

}
