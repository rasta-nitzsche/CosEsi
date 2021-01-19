package sample.noyau.entity;

import sample.noyau.service.COS;
import sample.noyau.util.Notification;
import sample.noyau.util.RemboursementPK;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.persistence.*;

// TODO: (changed) removed attribute id and added @IdClass(RemboursementPK.class)

@Entity
@IdClass(RemboursementPK.class)
public class Remboursement implements Serializable {
	@Id
	@ManyToOne
	@JoinColumn(name = "pretId", nullable = false)
	private PretRemboursable pret;
	@Id
	private LocalDate date; // Date de remboursement (prélèvement)
	private TypeRemboursement typeRemboursement; // prélèvement ou apport externe
	private double somme;

// Constructors

	public Remboursement() {
	}

	public Remboursement(PretRemboursable pret, LocalDate date, TypeRemboursement typeRemboursement, double somme) {
		this.pret = pret;
		this.pret.ajouterRemboursement(this);
		this.date = date;
		this.typeRemboursement = typeRemboursement;
		this.somme = somme;
	}

	public void envoyer() throws AddressException, MessagingException {
		String objet = "Remboursement du prêt (" + date + ")";
		String message = " Infomations sur le prêt: " + pret + "\n\n Informations sur le remboursement: " + this;
		Notification.envoyerMessage(COS.getCosEmail(), COS.getPasswordEmail(), pret.getEmploye().getEmail(), objet,
				message);
	}

	// Overrided methods

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Remboursement))
			return false;
		Remboursement that = (Remboursement) o;
		return pret.equals(that.pret) && date.equals(that.date);
	}

	@Override
	public int hashCode() {
		return Objects.hash(pret, date);
	}

	@Override
	public String toString() {
		return "\n Date: " + date + "\n Type de remboursement: " + typeRemboursement + "\n Somme: " + somme;
	}

	// Getters & Setters

	public PretRemboursable getPret() {
		return pret;
	}

	public void setPret(PretRemboursable pret) {
		this.pret = pret;
		this.pret.ajouterRemboursement(this);
	}

	public void supprimerPret() {
		this.pret = null;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public TypeRemboursement getTypeRemboursement() {
		return typeRemboursement;
	}

	public void setTypeRemboursement(TypeRemboursement typeRemboursement) {
		this.typeRemboursement = typeRemboursement;
	}

	public double getSomme() {
		return somme;
	}

	public void setSomme(double somme) {
		this.somme = somme;
	}
}
