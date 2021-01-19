package sample.noyau.entity;

import javafx.beans.property.SimpleStringProperty;
import sample.noyau.service.CompteService;

import java.io.Serializable;

import javax.persistence.*;

@Entity
public class Compte implements Serializable {

	@Id // ID changed
	@Column(name = "nom")
	private String nom;
	@Column(name = "mdp")
	private String mdp; // mot de passe
	@Column(name = "etat")
	private sample.noyau.entity.Droit droit;
	@Column(name = "connexion", nullable = false)
	private boolean connecte = false;

	public Compte() {
	}

	public Compte(String nom, String mdp, Droit droit) {
		this.nom = nom;
		this.mdp = CompteService.crypter(mdp);
		this.droit = droit;
		connecte = false;
	}
	
	// les setters et getters

	public String getMdp() {
		return mdp;
	}

	public void setMdp(String mdp) {
		this.mdp = mdp;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public Droit getDroit() {
		return droit;
	}

	public void setDroit(Droit droit) {
		this.droit = droit;
	}

	public boolean estConnecte() {
		return connecte;
	}

	public void setConnexion(boolean connecte) {
		this.connecte = connecte;
	}

	public SimpleStringProperty getNomProperty() {
		return new SimpleStringProperty(nom);
	}

	public SimpleStringProperty getDroitProperty() {
		return new SimpleStringProperty(droit.toString());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Compte other = (Compte) obj;
		if (nom == null) {
			if (other.nom != null)
				return false;
		} else if (!nom.equals(other.nom))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Compte [nom=" + nom + ", droit=" + droit + ", connecte=" + connecte + "]";
	}

}
