package sample.noyau.entity;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import sample.noyau.exception.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import sample.noyau.service.COS;
import sample.noyau.service.PretService;
import sample.noyau.util.Notification;
import tray.notification.NotificationType;

@Entity
public class Employe implements Comparable<Employe> {
	@Id
	private String id;
	private String code;
	private String nom;
	private String prenom;
	private LocalDate dateNaissance;
	private LocalDate datePremEm;
	private String grade;
	private String situationFamiliale;
	private String email;
	private String service; // service occupé par l'employé
	private String numeroSS; // numéro de sécurité social
	private String numeroCCP; // numéro CCP
	// TODO: postponed (salaire employé)
	// private double salaire;
	// private double salaireApresPrelevement;

	// TODO: (changed) List to Set for Prêt and Demande

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER, mappedBy = "employe")
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Pret> pretsList = new ArrayList<Pret>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER, mappedBy = "employe")
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Demande> demandesList = new ArrayList<Demande>();

	// Constructors

	public Employe() {
	}

	public Employe(String id, String nom, String prenom, LocalDate dateNaissance, String service, String numeroSS,
				   String numeroCCP, List<Pret> pretsList, List<Demande> demandesList) {
		this.id = id;
		this.nom = nom;
		this.prenom = prenom;
		this.dateNaissance = dateNaissance;
		this.service = service;
		this.numeroSS = numeroSS;
		this.numeroCCP = numeroCCP;
//		this.salaire = salaire;
//		this.salaireApresPrelevement = salaire;
		this.pretsList = pretsList;
		this.demandesList = demandesList;
		if (!COS.getServicesList().contains(service))
			COS.addService(service);
	}

	public Employe(String id, String nom, String prenom, LocalDate dateNaissance, String service, String numeroSS,
				   String numeroCCP) {
		this.id = id;
		this.nom = nom;
		this.prenom = prenom;
		this.dateNaissance = dateNaissance;
		this.service = service;
		this.numeroSS = numeroSS;
		this.numeroCCP = numeroCCP;
//		this.salaire = salaire;
//		salaireApresPrelevement = salaire;
		if (!COS.getServicesList().contains(service))
			COS.addService(service);
	}

	public Employe(String id, String code, String nom, String prenom, LocalDate dateNaissance, LocalDate datePremEm,
				   String grade, String situationFamiliale, String email, String service, String numeroSS, String numeroCCP) {
		this.id = id;
		this.code = code;
		this.nom = nom;
		this.prenom = prenom;
		this.dateNaissance = dateNaissance;
		this.datePremEm = datePremEm;
		this.grade = grade;
		this.situationFamiliale = situationFamiliale;
		this.email = email;
		this.service = service;
		this.numeroSS = numeroSS;
		this.numeroCCP = numeroCCP;
//		this.salaire = salaire;
//		salaireApresPrelevement = salaire;
		if (!COS.getServicesList().contains(service))
			COS.addService(service);
	}

	// importante pour archiver
	public Employe(Employe employe) {
		this.id = employe.getId();
		this.code = employe.getCode();
		this.nom = employe.getNom();
		this.prenom = employe.getPrenom();
		this.dateNaissance = employe.getDateNaissance();
		this.datePremEm = employe.getDatePremEm();
		this.grade = employe.getGrade();
		this.situationFamiliale = employe.getSituationFamiliale();
		this.email = employe.getEmail();
		this.service = employe.getService();
		this.numeroSS = employe.getNumeroSS();
		this.numeroCCP = employe.getNumeroCCP();
	}

	// Employe methods

	public void ajouterPret(Pret pret) {
		pretsList.add(pret);
	}

	public void ajouterDemande(Demande demande) {
		demandesList.add(demande);
	}

	public void supprimerPret(Pret pret) {
		pretsList.remove(pret);
	}

	public void supprimerDemande(Demande demande) {
		demandesList.remove(demande);
	}

	// Overrided methods

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Employe))
			return false;
		Employe employe = (Employe) o;
		return getId().equals(employe.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId());
	}

	@Override
	public String toString() {
		return (id + "- " + nom + " " + prenom);
	}

	public String getNomComplet() {
		return (nom + " " + prenom);
	}

	@Override
	public int compareTo(Employe employe) {
		return id.compareTo(employe.getId());
	}

	// Getters & Setters

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public String getPrenom() {
		return prenom;
	}

	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}

	public LocalDate getDateNaissance() {
		return dateNaissance;
	}

	public void setDateNaissance(LocalDate dateNaissance) {
		this.dateNaissance = dateNaissance;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getNumeroSS() {
		return numeroSS;
	}

	public void setNumeroSS(String numeroSS) {
		this.numeroSS = numeroSS;
	}

	public String getNumeroCCP() {
		return numeroCCP;
	}

	public void setNumeroCCP(String numeroCCP) {
		this.numeroCCP = numeroCCP;
	}

//	public double getSalaire() {
//		return salaire;
//	}
//
//	public void setSalaire(double salaire) {
//		this.salaire = salaire;
//	}
//
//	public double getSalaireApresPrelevement() {
//		return salaireApresPrelevement;
//	}
//
//	public void setSalaireApresPrelevement(double salaireApresPrelevement) {
//		this.salaireApresPrelevement = salaireApresPrelevement;
//	}

	public List<Pret> getPretsList() {
		return pretsList;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public LocalDate getDatePremEm() {
		return datePremEm;
	}

	public void setDatePremEm(LocalDate datePremEm) {
		this.datePremEm = datePremEm;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public String getSituationFamiliale() {
		return situationFamiliale;
	}

	public void setSituationFamiliale(String situationFamiliale) {
		this.situationFamiliale = situationFamiliale;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPretsList(List<Pret> pretsList) {
		this.pretsList = pretsList;
	}

	public List<Demande> getDemandesList() {
		return demandesList;
	}

	public void setDemandesList(List<Demande> demandesList) {
		this.demandesList = demandesList;
	}

	public List<PretRemboursable> getPretsRemboursables() {
		List<?> prets = pretsList;
		return prets.stream().filter(pret -> pret instanceof PretRemboursable).map(PretRemboursable.class::cast)
				.collect(Collectors.toList());
	}

	public double getMontantRembourse() {
		List<PretRemboursable> pretRemboursableList = PretService.pretsEnCours(getPretsRemboursables());
		double montant = 0;
		for (PretRemboursable pret : pretRemboursableList){
			montant += pret.getSomme()/PretRemboursable.getTRANCHES();
		}
		return montant;
	}

	// TODO: for loop too many queries on database
	public void cloturerPrets(String cause) {
		// retourne seulement les prêts remboursables de l'employé
		List<Pret> pretsRemboursablesList = pretsList.stream()
				.filter(pret -> PretRemboursable.class.isAssignableFrom(pret.getClass())).collect(Collectors.toList());

		for (Pret pret : pretsRemboursablesList) {
			try {
				PretService.cloturer((PretRemboursable) pret, cause);
			} catch (NotifException e) {
				Notification notif = new Notification(e);
				notif.setTitre("Prêt (" + pret.getId() + ") : " + notif.getTitre());
				(notif).ajouterTrayNotif(NotificationType.ERROR);
			}
		}
	}

	// TODO: for loop too many queries on database
	public void reporterPrets(LocalDate dateFin, String cause) throws DateException {
		List<Pret> pretsRemboursablesList = pretsList.stream()
				.filter(pret -> PretRemboursable.class.isAssignableFrom(pret.getClass())).collect(Collectors.toList());
		for (Pret pret : pretsRemboursablesList) {
			try {
				PretService.reporter((PretRemboursable) pret,
						((PretRemboursable) pret).getDateProchain().minusMonths(1), dateFin, cause);
			} catch (NotifException e) {
				Notification notif = new Notification(e);
				notif.setTitre("Prêt (" + pret.getId() + ") : " + notif.getTitre());
				(notif).ajouterTrayNotif(NotificationType.ERROR);
			}
		}
	}

	// TODO: for loop too many queries on database
	public void reporterPrets(LocalDate dateDebut, LocalDate dateFin, String cause) throws DateException {
		List<Pret> pretsRemboursablesList = pretsList.stream()
				.filter(pret -> PretRemboursable.class.isAssignableFrom(pret.getClass())).collect(Collectors.toList());
		for (Pret pret : pretsRemboursablesList) {
			try {
				PretService.reporter((PretRemboursable) pret, dateDebut, dateFin, cause);
			} catch (NotifException e) {
				Notification notif = new Notification(e);
				notif.setTitre("Prêt (" + pret.getId() + ") : " + notif.getTitre());
				(notif).ajouterTrayNotif(NotificationType.ERROR);
			}
		}
	}

	// TODO: for loop too many queries on database
	public void preleverPrets() {
		List<Pret> pretsRemboursablesList = pretsList.stream()
				.filter(pret -> PretRemboursable.class.isAssignableFrom(pret.getClass())).collect(Collectors.toList());

		for (Pret pret : pretsRemboursablesList) {
			try {
				PretService.prelever((PretRemboursable) pret);
			} catch (NotifException e) {
				Notification notif = new Notification(e);
				notif.setTitre("Prêt (" + pret.getId() + ") : " + notif.getTitre());
				(notif).ajouterTrayNotif(NotificationType.ERROR);
			}
		}
	}

	// TODO: for loop too many queries on database
	public void preleverPrets(LocalDate datePrelevement) {
		List<Pret> pretsRemboursablesList = pretsList.stream()
				.filter(pret -> PretRemboursable.class.isAssignableFrom(pret.getClass())).collect(Collectors.toList());
		for (Pret pret : pretsRemboursablesList) {
			try {
				PretService.prelever((PretRemboursable) pret, datePrelevement);
			} catch (NotifException e) {
				Notification notif = new Notification(e);
				notif.setTitre("Prêt (" + pret.getId() + ") : " + notif.getTitre());
				(notif).ajouterTrayNotif(NotificationType.ERROR);
			}
		}
	}

	public StringProperty getNomProperty() {
		return new SimpleStringProperty(nom);
	}

	public StringProperty getPrenomProperty() {
		return new SimpleStringProperty(prenom);
	}

	public StringProperty getServiceProperty() {
		return new SimpleStringProperty(service);
	}

	public StringProperty getDateProperty() {
		return new SimpleStringProperty(dateNaissance.toString());
	}

	public StringProperty getIdProperty() {
		return new SimpleStringProperty(id);
	}

}
