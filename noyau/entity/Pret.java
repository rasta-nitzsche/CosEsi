package sample.noyau.entity;

import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.persistence.*;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import sample.noyau.service.COS;
import sample.noyau.util.ID;
import sample.noyau.util.Notification;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
// TODO: why no discriminator value ?
// TODO: why not abstract ?
public abstract class Pret implements Comparable<Pret> {
	@Id
	private String id; // "num_annee-sociale" (ex. : "1_2020") (utilise ID.toString())
	private int pv; // numéro du PV
	@ManyToOne
	@JoinColumn(name = "employeId", nullable = false) // TODO: true or false (for me there is no pret without employe)
	private Employe employe;
	private LocalDate dateDemande;
	private double somme;
	// Constructors

	public Pret() {

	}

	public Pret(ID id, int pv, Employe employe, LocalDate dateDemande, double somme, TypePret typePret) {
		this.id = id.toString();
		this.pv = pv;
		this.employe = employe;
		this.employe.ajouterPret(this);
		this.dateDemande = dateDemande;
		this.somme = somme;
	}

	public Pret(Demande demande, int pv) {
		this.id = demande.getId();
		this.pv = pv;
		this.employe = demande.getEmploye();
		this.employe.ajouterPret(this);
		this.dateDemande = demande.getDateDemande();
		this.somme = demande.getSomme();
	}

	public void envoyer() throws AddressException, MessagingException {
		String objet = " Prêt (" + id + ")";
		String message = " Infomations sur le prêt: \n" + this;
		Notification.envoyerMessage(COS.getCosEmail(), COS.getPasswordEmail(), this.getEmploye().getEmail(), objet,
				message);
	}

	// Overrided methods

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Pret))
			return false;
		Pret pret = (Pret) o;
		return getId().equals(pret.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId());
	}

	@Override
	public String toString() {
		return "Prêt: " + id + "\n Type: " + TypePret.getType(this) + "\n PV: " + pv + "\n Employé: "
				+ employe.getNomComplet() + "\n DateDemande: " + dateDemande + "\n Somme: " + somme;
	}

	@Override
	public int compareTo(Pret pret) {
		return id.compareTo(pret.getId());
	}

	// Getters & Setters

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getPV() {
		return pv;
	}

	public void setPV(int pv) {
		this.pv = pv;
	}

	public Employe getEmploye() {
		return employe;
	}

	public void setEmploye(Employe employe) {
		this.employe = employe;
		this.employe.ajouterPret(this);
	}

	public LocalDate getDateDemande() {
		return dateDemande;
	}

	public void setDateDemande(LocalDate dateDemande) {
		this.dateDemande = dateDemande;
	}

	public double getSomme() {
		return somme;
	}

	public void setSomme(double somme) {
		this.somme = somme;
	}

	public int getPv() {
		return pv;
	}

	public void setPv(int pv) {
		this.pv = pv;
	}

	public Year getAnneeSociale() {
		return COS.anneeSociale(dateDemande);
	}

	public String getDescription() {
		return "description";
	}

	public String getExcelLine() {
		String idString = id;
		String typePretString = TypePret.getType(this).toString();
		String employeString = employe.getNom() + " " + employe.getPrenom() + " /" + employe.getId();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		String dateString = dateDemande.format(format);
		String pvString = String.valueOf(pv);
		String sommeString = String.valueOf(somme);
		String dateDebutString = " ";
		String dateProchainString = " ";
		String moisRestantsString = " ";
		String sommeRestanteString = " ";
		String motifString = " ";
		String produitString = " ";
		String fournisseurString = " ";
		return String.format("%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s", idString, typePretString, employeString,
				dateString, pvString, sommeString, dateDebutString, dateProchainString, moisRestantsString,
				sommeRestanteString, motifString, produitString, fournisseurString);
	}

	public String getClassName() {
		return getClass().getSimpleName();
	}

	public StringProperty getIdProperty() {
		return new SimpleStringProperty(id);
	}

	public StringProperty getDateProperty() {
		return new SimpleStringProperty(dateDemande.toString());
	}

	public DoubleProperty getSommeProperty() {
		return new SimpleDoubleProperty(somme);
	}

	public StringProperty getTypeProperty() {
		return new SimpleStringProperty("Prêt");
	}

	public StringProperty getSommeRStringProperty() {
		return new SimpleStringProperty("///");
	}
}
