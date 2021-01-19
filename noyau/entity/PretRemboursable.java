package sample.noyau.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.*;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import sample.noyau.util.ID;

@Entity
// TODO: why no discriminator value ?
public abstract class PretRemboursable extends Pret {
	// nombre de tranches pour le remboursement (10 par défaut)
	public static final int TRANCHES = 10;

	private LocalDate dateDebut;
	private LocalDate dateProchain;
	private double sommeRestante; // somme restante à prélever
	private int nbMois; // nombre de mois restants à prélever

	// TODO: (changed) List to Set for Report and Remboursement

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER, mappedBy = "pret")
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Report> reportsList = new ArrayList<Report>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER, mappedBy = "pret")
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Remboursement> remboursementsList = new ArrayList<Remboursement>();

	// Constructors

	public PretRemboursable() {
	}

	// Cas normal sans remboursement anticipé au début
	public PretRemboursable(ID id, int pV, Employe employe, LocalDate dateDemande, double somme, TypePret typePret,
			LocalDate dateDebut) {
		super(id, pV, employe, dateDemande, somme, typePret);
		this.dateDebut = dateDebut;
		this.dateProchain = dateDebut;
		this.sommeRestante = somme;
		this.nbMois = TRANCHES;
	}

	public PretRemboursable(Demande demande, int pV, LocalDate dateDebut) {
		super(demande, pV);
		this.dateDebut = dateDebut;
		this.dateProchain = dateDebut;
		this.sommeRestante = demande.getSomme();
		this.nbMois = TRANCHES;
	}

	// PretRemboursable methods

	// retourne si le prélèvement a commencé pour ce prêt
	public boolean verifPrelevement() {
		return (dateDebut != null);
	}

	public void ajouterRemboursement(Remboursement remboursement) {
		remboursementsList.add(remboursement);
	}

	public void ajouterReport(Report report) {
		reportsList.add(report);
	}

	public void supprimerRemboursement(Remboursement remboursement) {
		remboursementsList.remove(remboursement);
	}

	public void supprimerReport(Report report) {
		reportsList.remove(report);
	}

	// +/- nbMois
	public void modifierNbMois(int nbMois) {
		this.nbMois += nbMois;
	}

	// +/- sommeRestante
	public void modifierSommeRestante(double somme) {
		sommeRestante += somme;
	}

	// Overrided methods

	@Override
	public String toString() {
		return super.toString() + "\n Date du premier prélèvement: " + dateDebut + "\n Date du prochain prélèvement: "
				+ dateProchain + "\n Somme Restante: " + sommeRestante + "\n Nombre de mois restants: " + nbMois;
	}

	// Getters & Setters

	public LocalDate getDateDebut() {
		return dateDebut;
	}

	public void setDateDebut(LocalDate dateDebut) {
		this.dateDebut = dateDebut;
	}

	public LocalDate getDateProchain() {
		return dateProchain;
	}

	public void setDateProchain(LocalDate dateProchain) {
		this.dateProchain = dateProchain;
	}

	public static int getTRANCHES() {
		return TRANCHES;
	}

	public List<Report> getReportsList() {
		return reportsList;
	}

	public void setReportsList(List<Report> reportsList) {
		this.reportsList = reportsList;
	}

	public List<Remboursement> getRemboursementsList() {
		return remboursementsList;
	}

	public void setRemboursementsList(List<Remboursement> remboursementsList) {
		this.remboursementsList = remboursementsList;
	}

	public double getSommeRestante() {
		return sommeRestante;
	}

	public void setSommeRestante(double sommeRestante) {
		this.sommeRestante = sommeRestante;
	}

	public int getNbMois() {
		return nbMois;
	}

	public void setNbMois(int nbMois) {
		this.nbMois = nbMois;
	}

	// retourne si le prélèvement a commencé pour ce prêt
	public boolean commencePrelevement() {
		return (dateDebut != null);
	}

	// retourne s'il y a eu prélèvement à datePrelevement
	public boolean estPreleve(LocalDate datePrelevement) {
		List<Remboursement> prelevementsList = remboursementsList.stream()
				.filter(remboursement -> (remboursement.getTypeRemboursement() == TypeRemboursement.PRELEVEMENT
						&& remboursement.getDate().isEqual(datePrelevement)))
				.collect(Collectors.toList());
		return (!prelevementsList.isEmpty());
	}

	// retourne s'il y a eu remboursement à dateRemboursement
	public boolean estRembourse(LocalDate dateRemboursement) {
		List<Remboursement> remboursementsList = this.remboursementsList.stream()
				.filter(remboursement -> remboursement.getDate().isEqual(dateRemboursement))
				.collect(Collectors.toList());
		return (!remboursementsList.isEmpty());
	}

	// retourne s'il y a eu prélèvement entre dateDebut et dateFin
	public boolean estPreleve(LocalDate dateDebut, LocalDate dateFin) {
		return (!getPrelevements(dateDebut, dateFin).isEmpty());
	}

	// retourne s'il y a report durant dateReport
	public boolean estReporte(LocalDate dateReport) {
		return (!getReports(dateReport).isEmpty());
	}

	// retourne les prélèvements du prêt entre se situant entre dateDebut et dateFin
	public List<Remboursement> getPrelevements(LocalDate dateDebut, LocalDate dateFin) {
		return remboursementsList.stream()
				.filter(remboursement -> (remboursement.getTypeRemboursement() == TypeRemboursement.PRELEVEMENT)
						&& remboursement.getDate().compareTo(dateDebut) >= 0
						&& remboursement.getDate().compareTo(dateFin) < 0)
				.collect(Collectors.toList());
	}

	// retourne les reports du prêt se situant durant dateReport
	public List<Report> getReports(LocalDate dateReport) {
		return reportsList.stream().filter(report -> (dateReport.compareTo(report.getDateDebut()) > 0
				&& dateReport.compareTo(((report.getDateFin() == null) ? LocalDate.MIN : report.getDateFin())) <= 0))
				.collect(Collectors.toList());
	}

	// retourne si le prêt est clôturé
	public boolean estCloture() {
		// TODO : il faut changer cette methode apres ce que j'ai modifier dans pret
		// service
		return (nbMois == 0);
		// return reportsList.stream().filter(report ->
		// report.getDateFin().isEqual(COS.MAX_DATE)).findFirst().isPresent();
	}

	public Remboursement getDernierRemboursement() {
		if (remboursementsList.size() == 0)
			return null;
		return remboursementsList.get(remboursementsList.size() - 1);
	}

	public Report getDernierReport() {
		if (reportsList.size() == 0)
			return null;
		return reportsList.get(reportsList.size() - 1);
	}

	public DoubleProperty getSommeRProperty() {
		return new SimpleDoubleProperty(sommeRestante);
	}

	// TODO: duplication??
	@Override
	public StringProperty getSommeRStringProperty() {
		return new SimpleStringProperty(sommeRestante + "");
	}

	@Override
	public StringProperty getTypeProperty() {
		return new SimpleStringProperty("Prêt Remboursable");
	}

}
