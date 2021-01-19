package sample.noyau.entity;

import sample.noyau.service.COS;
import sample.noyau.util.Notification;
import sample.noyau.util.ReportPK;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.persistence.*;

// TODO: (changed) removed attribute id and added @IdClass(ReportPK.class)

@Entity
@IdClass(ReportPK.class) // primary key
public class Report implements Serializable {
	@Id
	@ManyToOne
	@JoinColumn(name = "pretId", nullable = false)
	private PretRemboursable pret;
	@Id
	private LocalDate dateDebut;
	private LocalDate dateFin;
	private String cause;

	// Constructors

	public Report() {
	}

	public Report(PretRemboursable pret, LocalDate dateDebut, LocalDate dateFin, String cause) {
		this.pret = pret;
		this.pret.ajouterReport(this);
		this.dateDebut = dateDebut;
		this.dateFin = dateFin;
		this.cause = cause;
	}

	public void envoyer() throws AddressException, MessagingException {
		if (dateFin == null) {
			String objet = "Prêt clôturé (" + cause + ")";
			String message = " Infomations sur le prêt: " + pret + "\n\n Informations sur la clôture: " + this;
			Notification.envoyerMessage(COS.getCosEmail(), COS.getPasswordEmail(), pret.getEmploye().getEmail(), objet,
					message);
		} else {
			String objet = "Report du prêt (" + cause + ")";
			String message = " Infomations sur le prêt: " + pret + "\n\n Informations sur le report: " + this;
			Notification.envoyerMessage(COS.getCosEmail(), COS.getPasswordEmail(), pret.getEmploye().getEmail(), objet,
					message);
		}
	}

	// Overrided methods

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Report))
			return false;
		Report report = (Report) o;
		return getPret().equals(report.getPret()) && getDateDebut().equals(report.getDateDebut());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getPret(), getDateDebut());
	}

	@Override
	public String toString() {
		if (dateFin == null) {
			return "\n Date de clôture: " + dateDebut + "\n Cause: " + cause;
		} else {
			return "\n Date de début: " + dateDebut + "\n Date de fin: " + dateFin + "\n Cause: " + cause;
		}
	}

	// Getters & Setters

	public PretRemboursable getPret() {
		return pret;
	}

	public void setPret(PretRemboursable pret) {
		this.pret = pret;
		this.pret.ajouterReport(this);
	}

	public LocalDate getDateDebut() {
		return dateDebut;
	}

	public void setDateDebut(LocalDate dateDebut) {
		this.dateDebut = dateDebut;
	}

	public LocalDate getDateFin() {
		return dateFin;
	}

	public void setDateFin(LocalDate dateFin) {
		this.dateFin = dateFin;
	}

	public String getCause() {
		return cause;
	}

	public void setCause(String cause) {
		this.cause = cause;
	}
}
