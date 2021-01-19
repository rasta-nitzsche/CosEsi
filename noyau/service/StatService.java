package sample.noyau.service;

import sample.noyau.entity.*;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StatService {
	/*
	 * Statistiques Nombre de prêts total Nombre de prêts selon leur type (don, prêt
	 * électroménager, prêt social) Nombre d'employés ayant au moins un prêt Nombre
	 * de prêts d'un employé Nombre de prêts selon la période de début de
	 * prélèvement (entre dateDebut et dateFin)
	 */

	// nombre de prêts
	public static int nbPrets(List<Pret> pretList) {
		if (pretList == null)
			return 0;

		return pretList.size();
	}

	// nombre de prêts selon le type (don, prêt électroménager, prêt social)
	public static int nbPretsParType(List<Pret> pretList, TypePret typePret) {
		if (pretList == null)
			return 0;

		switch (typePret) {
		case DON:
			return pretList.stream().filter(pret -> Don.class.isAssignableFrom(pret.getClass()))
					.collect(Collectors.toList()).size();
		case ELECTROMENAGER:
			return pretList.stream().filter(pret -> PretElectromenager.class.isAssignableFrom(pret.getClass()))
					.collect(Collectors.toList()).size();
		case SOCIAL:
			return pretList.stream().filter(pret -> PretSocial.class.isAssignableFrom(pret.getClass()))
					.collect(Collectors.toList()).size();
		default:
			return 0; // TODO: or exception maybe ?
		}
	}

	// nombre d'employés ayant au moins un prêt
	public static int nbPretParEmploye(List<Employe> employeList) {
		if (employeList == null)
			return 0;

		return employeList.stream()
				.filter(employe -> employe.getPretsList() != null && !employe.getPretsList().isEmpty())
				.collect(Collectors.toList()).size();
	}

	// nombre de prêts d'un employé
	public static int nbPretParEmploye(Employe employe) {
		if (employe == null)
			return 0;
		if (employe.getPretsList() == null)
			return 0;

		return employe.getPretsList().size();
	}

	// nombre de prêts remboursables avec dates de début de prélèvement entre
	// dateDebut et dateFin
	public static int nbPretsParPeriode(List<Pret> pretList, LocalDate dateDebut, LocalDate dateFin) {
		if (pretList == null)
			return 0;

		// retourner seulement les prêts remboursables à partir de pretList
		List<Pret> pretRemboursableList = pretList.stream()
				.filter(pret -> PretRemboursable.class.isAssignableFrom(pret.getClass())).collect(Collectors.toList());

		return pretRemboursableList.stream()
				.filter(pret -> ((PretRemboursable) pret).getDateDebut().isAfter(dateDebut)
						&& ((PretRemboursable) pret).getDateDebut().isBefore(dateFin))
				.collect(Collectors.toList()).size();
	}

	// nombre de prêts électroménagers par le fournisseur indiqué
	public static int nbPretParFournisseur(List<Pret> pretList, String fournisseur) {
		if (pretList == null || fournisseur == null)
			return 0;

		return pretList.stream()
				.filter(pret -> PretElectromenager.class.isAssignableFrom(pret.getClass())
						&& ((PretElectromenager) pret).getFournisseur().equals(fournisseur))
				.collect(Collectors.toList()).size();
	}

	// nombre de prêts électroménagers par chaque fournisseur (Map<fournisseur,
	// nombre>)
	public static Map<String, Integer> nbPretParFournisseurs(List<Pret> pretList) {
		Map<String, Integer> fournisseursNbPrets = COS.getFournisseursList().stream()
				.collect(Collectors.toMap(fournisseur -> fournisseur, fournisseur -> Integer.valueOf(0)));

		List<Pret> pretElectroList = pretList.stream()
				.filter(pret -> PretElectromenager.class.isAssignableFrom(pret.getClass()))
				.collect(Collectors.toList());

		for (Pret pret : pretElectroList) {
			String fournisseur = ((PretElectromenager) pret).getFournisseur();

			// on assume qu'il n' y a pas de prêt avec un fournisseur différent de ceux dans
			// la liste des fournisseurs
			if (fournisseursNbPrets.containsKey(fournisseur)) {
				fournisseursNbPrets.replace(fournisseur, fournisseursNbPrets.get(fournisseur) + 1);
			}
		}

		return fournisseursNbPrets;
	}

	/*
	 * Tri Prêts selon leur ID Prêts selon leur type Prêts selon les ID des employés
	 * Prêts remboursables selon leur date de début de prélèvement Employés selon
	 * leur ID Employés selon leur nom + Possibilité de trier selon n'importe quel
	 * attribut avec triPrets et triEmployes
	 */

	// trie la liste des prêts selon l'attribut indiqué par le getter (par exemple :
	// Pret::getId)
	// reversed indique si le tri est inversé
	@SuppressWarnings("unchecked")
	public static List<Pret> triPrets(List<Pret> pretList,
			Function<? super PretRemboursable, ? extends Comparable> getter, boolean reversed) {
		if (pretList == null)
			return null;
		if (getter == null)
			return null;

		List<Pret> newList = new ArrayList<Pret>(pretList);
		if (reversed) {
			newList.sort(Comparator.comparing(getter).reversed());
		} else {
			newList.sort((Comparator<Pret>) Comparator.comparing(getter));
		}

		return newList;
	}

	// trie la liste des employés selon l'attribut indiqué par le getter (par
	// exemple : Employe::getId)
	// reversed indique si le tri est inversé
	public static List<Employe> triEmployes(List<Employe> employeList,
			Function<? super Employe, ? extends Comparable> getter, boolean reversed) {
		if (employeList == null)
			return null;
		if (getter == null)
			return null;

		List<Employe> newList = new ArrayList<Employe>(employeList);
		if (reversed) {
			newList.sort(Comparator.comparing(getter).reversed());
		} else {
			newList.sort(Comparator.comparing(getter));
		}

		return newList;
	}

	// Prêts

	// trie la liste des prêts selon leur ID
	public static List<Pret> pretParID(List<Pret> pretList) {
		return triPrets(pretList, Pret::getId, false);
	}

	// trie la liste des prêts selon leur type (don, électroménager et social)
	public static List<Pret> pretParType(List<Pret> pretList) {
		return triPrets(pretList, Pret::getClassName, false);
	}

	// trie la liste des prêts selon les ID des employés
	public static List<Pret> pretsParEmployeID(List<Pret> pretList) {
		return triPrets(pretList, Pret::getEmploye, false);
	}

	// TODO: ajouter les prêts non remboursables dans la liste finale ?
	// trie la liste des prêts remboursables selon les dates de début de prélèvement
	public static List<Pret> pretsParDatePrelevement(List<Pret> pretList) {
		if (pretList == null)
			return null;

		// retourner seulement les prêts remboursables à partir de pretList
		List<Pret> pretRemboursableList = pretList.stream()
				.filter(pret -> PretRemboursable.class.isAssignableFrom(pret.getClass())).collect(Collectors.toList());

		return triPrets(pretRemboursableList, PretRemboursable::getDateDebut, true);
	}

	// Employés

	// trie la liste des employés l'ID
	public static List<Employe> employesParID(List<Employe> employeList) {
		return triEmployes(employeList, Employe::getId, false);
	}

	// trie la liste des employés leurs noms
	public static List<Employe> employesParNom(List<Employe> employeList) {
		return triEmployes(employeList, Employe::getNom, false);
	}

}
