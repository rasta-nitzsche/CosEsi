package sample.noyau.util;

import java.time.LocalDate;
import java.time.Year;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Id;
import javax.persistence.Transient;

import sample.noyau.entity.Demande;
import sample.noyau.entity.Pret;
import sample.noyau.service.COS;
import sample.noyau.service.DemandeService;
import sample.noyau.service.PretService;

import javax.persistence.Embeddable;

@Embeddable
public class ID implements Comparable<ID> {
	@Id
	private int num;
	private Year annee;
	@Transient // To serialize (Chaque annee sociale a le nombre de ses demandes)
	public static Map<Year, Integer> lastIDsGenerated = new HashMap<Year, Integer>();

	/** give the current social year */
	public ID() {
		Integer nbIDGenerated = lastIDsGenerated.get(COS.getAnneeSociale());
		annee = COS.getAnneeSociale();
		if (nbIDGenerated == null)
			num = 1;
		else {
			num = nbIDGenerated + 1;
			while (DemandeService.trouverID((new ID(num, annee)).toString()) != null
					|| PretService.trouverID((new ID(num, annee)).toString()) != null) 
				++num;
		}
		lastIDsGenerated.put(COS.getAnneeSociale(), num);
	}

	public ID(LocalDate date) {
		Year anneeSociale = COS.anneeSociale(date);
		Integer nbIDGenerated = lastIDsGenerated.get(anneeSociale);
		annee = anneeSociale;
		if (nbIDGenerated == null)
			num = 1;
		else {
			num = nbIDGenerated + 1;
			while (DemandeService.trouverID((new ID(num, annee)).toString()) != null
					|| PretService.trouverID((new ID(num, annee)).toString()) != null) 
				++num;
		}
		lastIDsGenerated.put(anneeSociale, num);
	}

	public ID(Demande demande) {
		Year anneeSociale = demande.getAnneeSociale();
		Integer nbIDGenerated = lastIDsGenerated.get(anneeSociale);
		annee = anneeSociale;
		if (nbIDGenerated == null)
			num = 1;
		else {
			num = nbIDGenerated + 1;
			while (DemandeService.trouverID((new ID(num, annee)).toString()) != null
					|| PretService.trouverID((new ID(num, annee)).toString()) != null) 
				++num;
		}
		lastIDsGenerated.put(anneeSociale, num);
	}

	public ID(Pret pret) {
		Year anneeSociale = pret.getAnneeSociale();
		Integer nbIDGenerated = lastIDsGenerated.get(anneeSociale);
		annee = anneeSociale;
		if (nbIDGenerated == null)
			num = 1;
		else {
			num = nbIDGenerated + 1;
			while (DemandeService.trouverID((new ID(num, annee)).toString()) != null
					|| PretService.trouverID((new ID(num, annee)).toString()) != null) 
				++num;
		}
		lastIDsGenerated.put(anneeSociale, num);
	}

	public ID(int num, Year annee) {
		this.num = num;
		this.annee = annee;
	}

	public ID(String id) {
		num = Integer.valueOf(id.split("_")[0]);
		annee = Year.of(Integer.valueOf(id.split("_")[1]));
	}

	public int getNum() {
		return num;
	}

	public Year getAnnee() {
		return annee;
	}

	@Override
	public String toString() {
		return Integer.toString(num) + "_" + annee;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((annee == null) ? 0 : annee.hashCode());
		result = prime * result + num;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ID other = (ID) obj;
		if (annee == null) {
			if (other.annee != null)
				return false;
		} else if (!annee.equals(other.annee))
			return false;
		if (num != other.num)
			return false;
		return true;
	}

	public int intValue() {
		return num * 10000 + annee.getValue();
	}

	public int getValue() {
		return num * 10000 + annee.getValue();
	}

	@Override
	public int compareTo(ID id) {
		return this.getValue() - id.getValue();
	}

}
