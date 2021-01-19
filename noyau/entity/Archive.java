package sample.noyau.entity;

import javafx.beans.property.*;

public class Archive {

	private int annee, nbPsociaux, nbPelectro, nbDons, total;
	private double budgetInit;

	public Archive(int annee, int nbPsociaux, int nbPelectro, int nbDons, int total, double budgetInit) {
		this.annee = annee;
		this.nbPsociaux = nbPsociaux;
		this.nbPelectro = nbPelectro;
		this.nbDons = nbDons;
		this.total = total;
		this.budgetInit = budgetInit;
	}

	public int getAnnee() {
		return annee;
	}

	public IntegerProperty getanneeProperty() {
		return new SimpleIntegerProperty(annee);
	}

	public IntegerProperty getnbPsociauxProperty() {
		return new SimpleIntegerProperty(nbPsociaux);
	}

	public IntegerProperty getnbPelectroProperty() {
		return new SimpleIntegerProperty(nbPelectro);
	}

	public IntegerProperty getDonsProperty() {
		return new SimpleIntegerProperty(nbDons);
	}

	public IntegerProperty getTotalProperty() {
		return new SimpleIntegerProperty(total);
	}

	public DoubleProperty getBudgetProperty() {
		return new SimpleDoubleProperty(budgetInit);
	}
}
