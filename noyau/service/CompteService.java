package sample.noyau.service;

import sample.noyau.entity.Compte;
import sample.noyau.entity.Droit;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import sample.noyau.util.HibernateUtil;

import javax.xml.bind.DatatypeConverter;
import java.security.*;
import java.util.List;

public class CompteService {

	public static Compte compteActuel = null;

	public static void ajouter(String mdp, Compte compte) {
		if (compteActuel.getMdp().equals(mdp)) {
			try (Session session = HibernateUtil.getSessionFactory().openSession()) {
				Transaction tx = session.beginTransaction();
				session.save(compte);
				tx.commit();
			}
		}
	}

	private static void modifier(Compte compte) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			Transaction tx = session.beginTransaction();
			session.update(compte);
			tx.commit();
		}
	}

	public static void supprimer(String mdp, Compte compte) {
		if (compteActuel.getMdp().equals(mdp)) {
			try (Session session = HibernateUtil.getSessionFactory().openSession()) {
				Transaction tx = session.beginTransaction();
				session.delete(compte);
				tx.commit();
			}
		}
	}

	public static Compte trouverID(String nom) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Compte compte = session.get(Compte.class, nom);
		session.close();
		return compte;
	}

	// il faut tester si == null !!!!

	public static List<Compte> lireComptes() {
		return HibernateUtil.getSessionFactory().openSession().createQuery("from Compte", Compte.class).list();
	}

	public static String crypter(String string) {
		String s = string;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(s.getBytes());
			byte[] digest = md.digest();
			s = DatatypeConverter.printHexBinary(digest).toUpperCase();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return s;
	}

	public static Droit authentifier(String nom, String mdp) {
		Query<Compte> query = HibernateUtil.getSessionFactory().openSession()
				.createQuery("from Compte as c where c.nom = :nom", Compte.class);
		query.setParameter("nom", nom);
		for (Compte c : query.list()) {
			if (CompteService.crypter(mdp).equals(c.getMdp()) && nom.equals(c.getNom())) {
				compteActuel = c;
				return c.getDroit();
			}
		}
		return Droit.REFUSE;
	}

	public static void entrer() {
		if (compteActuel != null) {
			compteActuel.setConnexion(true);
			if (trouverID(compteActuel.getNom()) != null)
				modifier(compteActuel);
		}
	}

	public static void sortir() {
		if (compteActuel != null) {
			compteActuel.setConnexion(false);
			if (trouverID(compteActuel.getNom()) != null)
				modifier(compteActuel);
		}
	}

	public static void modifierMdp(String mdp, String nouveauMdp) {
		if (compteActuel.getMdp().equals(mdp)) {
			compteActuel.setMdp(crypter(nouveauMdp));
			modifier(compteActuel);
		}
	}

	public static void modifierMdp(String mdp, Compte compte, String nouveauMdp) {
		if (compteActuel.getMdp().equals(mdp)) {
			compte.setMdp(crypter(nouveauMdp));
			modifier(compte);
		}
	}

	/*
	 * public static boolean modifierNom(String mdp, String nouveauNom) {
	 * if(compteActuel.getMdp().equals(crypter(mdp))) {
	 * compteActuel.setNom(nouveauNom); modifier(compteActuel); return true; }
	 * return false; }
	 */
	public static boolean modifierDroit(String mdp, Compte compte, Droit droit) {
		if (compteActuel.getMdp().equals(mdp)) {
			compte.setDroit(droit);
			modifier(compte);
			return true;
		}
		return false;
	}

	public static Compte superUserConnecte() {
		return lireComptes().stream()
				.filter(compte -> (compte.estConnecte() && compte.getDroit() == Droit.SUPERUTILISATEUR))
				.findAny().orElse(null);
	}

	// Si un super utilisateur est connecte. On peut pas modifier si on est des
	// super utilisateurs ou bien erreur de sync
	public static boolean verifModif() {
		if (! compteActuel.equals(superUserConnecte())) {
			if (superUserConnecte() == null) return true;
			if (compteActuel.getDroit() == Droit.SUPERUTILISATEUR) {
				return false;
			} else
				return true;
		} else
			return true;
	}
}
