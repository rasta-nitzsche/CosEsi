package sample.noyau.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import sample.noyau.entity.Compte;
import sample.noyau.entity.PretRemboursable;
import sample.noyau.exception.COSException;
import sample.noyau.exception.ClotureException;
import sample.noyau.exception.DateException;
import sample.noyau.exception.RemboursementException;
import sample.noyau.exception.ReportException;
import sample.noyau.util.FTPUtil;
import sample.noyau.util.HibernateUtil;
import sample.noyau.util.ID;
import sample.noyau.util.Notification;

/**
 * Méthodes : ajouterService ajouterFournisseur modifierCompte preleverMensuel
 * sauvegarderBudgetAnnuel anneeSociale anneesArchivees
 */

// Classe service pour la comité des oeuvres sociales (COS)

public class COS {

	/** Compte d'Admin **/
	// n'est pas supprimé et on peut pas changer son droit (est sérialisé)
	// la raison de ça et de corriger le probleme d'interface (modifier/supprimer)
	public static final String Admin = "Admin";

	/** COSHost.dat (Trouvé dans le serveur) **/

	// Est un fichier accédé par tous les utilisateurs
	// Hosté par le serveur
	private static File COSHost = new File("COSHost.dat");
	// Chemin dans le serveur
	public static final String loadPath = "COSHost.dat";

	/** COSFile.dat (Fichier local) **/

	// Est un fichier accédé localement
	private static final File COSFile = new File("COSFile.dat");

	/** Classes entity **/
	private static List<Class> annotatedClasses = new ArrayList<Class>();

	/** Les notifications **/
	public static Set<Notification> notifs = new TreeSet<Notification>();

	/** L'email d'envoi **/
	// Pour envoyer les emails
	private static String cosEmail = "servicecos.esi@gmail.com";
	private static String passwordEmail = "cos2020cos";

	/** Les dates **/
	// L'année sociale se clôture le 24/06 par défaut (changeable)
	private static Month MOIS_CLOTURE = Month.JUNE;
	private static int JOUR_CLOTURE = 24;
	// Le jour de prelevement mensuel
	private static final int JOUR_PRELEVEMENT = 24;

	/** Budget et Compte **/
	// budget de COS de l'année sociale courante (budget initial)
	private static double budgetInit = 0;
	// compte de COS de l'année sociale courante (budget apres les modifications)
	private static double compte = 0;
	// les budgets annuels archivés de la COS
	private static Map<Year, List<Double>> budgetsAnnuels = new HashMap<Year, List<Double>>();
	// Pourcentage du minimun de compte (qui doit rester dans le compte +
	// modifiable)
	private static double MIN_COMPTE_RATIO = 10 / 100;

	/** Services et fournisseurs **/
	private static List<String> servicesList = new ArrayList<String>(); // liste des services des employés
	private static List<String> fournisseursList = new ArrayList<String>(); // liste des fournisseurs de produits
																			// électroménagers

	/** Années Archivées **/
	// les années archivés de la COS
	private static Set<Year> anneesArchives = new HashSet<Year>();

	/** Admin **/
	public static Compte getCompteAdmin() {
		return CompteService.trouverID(Admin);
	}

	/** Les méthodes de gestion de Hibernate **/
	// AnnotatedClasses (Ajouter les classes "entity")
	public static void addAnnotatedClasses() {
		annotatedClasses.add(sample.noyau.entity.Demande.class);
		annotatedClasses.add(sample.noyau.entity.Pret.class);
		annotatedClasses.add(sample.noyau.entity.Employe.class);
		annotatedClasses.add(sample.noyau.entity.Report.class);
		annotatedClasses.add(sample.noyau.entity.Remboursement.class);
		annotatedClasses.add(sample.noyau.entity.PretRemboursable.class);
		annotatedClasses.add(sample.noyau.entity.PretSocial.class);
		annotatedClasses.add(sample.noyau.entity.PretElectromenager.class);
		annotatedClasses.add(sample.noyau.entity.Don.class);
		annotatedClasses.add(sample.noyau.entity.Compte.class);
	}

	public static List<Class> getAnnotatedClasses() {
		return annotatedClasses;
	}

	/** Méthodes de compteCOS et Budget **/
	// ajoute/retranche la somme au compte de la COS (somme peut être +/-)
	public static void modifierCompte(double somme) {
		compte += somme;
		List<Double> budget = new ArrayList<Double>();
		budget.add(0, budgetInit);
		budget.add(1, compte);
		budgetsAnnuels.put(COS.getAnneeSociale(), budget);
	}

	public static double getCompte() {
		return compte;
	}

	public static double getBudgetInit() {
		return budgetInit;
	}

	public static double getMinCompteRatio() {
		return MIN_COMPTE_RATIO;
	}

	/// List<Double>.get(O) == budgetInit et List<Double>.get(1) == budgetRestant
	public static Map<Year, List<Double>> getBudgetsAnnuels() {
		return budgetsAnnuels;
	}

	// retourne le budget annuel (initial et restant) de l'année indiquée
	public static List<Double> getBudgetAnnuel(Year annee) {
		List<Double> budget = new ArrayList<Double>();
		budget.add(0, 0.0); // Budget Initial
		budget.add(1, 0.0); // Budget Restant
		if (!budgetsAnnuels.containsKey(annee))
			return budget;
		return budgetsAnnuels.get(annee);
	}

	/** Méthodes de Pret / Employe **/
	public static List<String> getServicesList() {
		return servicesList;
	}

	public static void setServicesList(List<String> servicesList) {
		COS.servicesList = servicesList;
	}

	public static List<String> getFournisseursList() {
		return fournisseursList;
	}

	public static void setFournisseursList(List<String> fournisseursList) {
		COS.fournisseursList = fournisseursList;
	}

	// TODO: for loop too many queries on DB
	// effectue le prélèvement mensuel de tous les prêts remboursables
	public static void preleverMensuel() {
		List<PretRemboursable> prets = PretService.lirePretsRemboursables();
		for (PretRemboursable pret : prets) {
			try {
				PretService.prelever(pret);
			} catch (ClotureException | RemboursementException | DateException | ReportException e) {
			}
		}
	}

	/** Méthodes de date **/
	// retourne l'année sociale de date
	public static Year anneeSociale(LocalDate date) {
		if (date.compareTo(LocalDate.of(date.getYear(), COS.getJourCloture().getMonth(),
				COS.getJourCloture().getDayOfMonth())) > 0)
			return Year.of(date.getYear() + 1);
		return Year.of(date.getYear());
	}

	// retourne la liste des années sociales archivés
	public static Set<Year> anneesArchivees() {
		return anneesArchives;
	}

	public static MonthDay getJourCloture() {
		return MonthDay.of(MOIS_CLOTURE, JOUR_CLOTURE);
	}

	public static int getJourPrelevement() {
		return JOUR_PRELEVEMENT;
	}

	// retourne l'année sociale courante
	public static Year getAnneeSociale() {
		if (LocalDate.now().compareTo(LocalDate.of(Year.now().getValue(), MOIS_CLOTURE, JOUR_CLOTURE)) > 0)
			return Year.of(Year.now().getValue() + 1);
		return Year.now();
	}

	/** Email **/
	public static String getCosEmail() {
		return cosEmail;
	}

	public static String getPasswordEmail() {
		return passwordEmail;
	}

	/** Méthodes de connexion du COS **/

	public static boolean externConnection() {
		return (FTPUtil.estConnecte() && HibernateUtil.estConnecte());
	}

	/** Méthodes de connexion de compte **/

	public static boolean estConnecte() {
		return (CompteService.compteActuel != null);
	}

	/******************** Sérialisation ***************************/

	// Fichier commun entre les utilisateurs (Dans le serveur)
	public static void serialiserHost() {
		if (COS.externConnection()) {
			try {
				ObjectOutputStream outCOS = new ObjectOutputStream(
						new BufferedOutputStream(new FileOutputStream(COSHost)));
				outCOS.writeObject(MOIS_CLOTURE);
				outCOS.writeObject(JOUR_CLOTURE);
				outCOS.writeObject(MIN_COMPTE_RATIO);
				outCOS.writeObject(budgetInit);
				outCOS.writeObject(compte);
				outCOS.writeObject(servicesList);
				outCOS.writeObject(fournisseursList);
				outCOS.writeObject(budgetsAnnuels);
				outCOS.writeObject(anneesArchives);
				outCOS.writeObject(ID.lastIDsGenerated);
				outCOS.writeObject(cosEmail);
				outCOS.writeObject(passwordEmail);
				outCOS.close();
				FTPUtil.upload(COSHost, loadPath);
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
		}
	}

	public static void serialiser() {
		if (COS.externConnection()) {
			try {
				COSFile.createNewFile();
				ObjectOutputStream outCOS = new ObjectOutputStream(
						new BufferedOutputStream(new FileOutputStream(COSFile)));
				outCOS.writeObject(CompteService.compteActuel);
				outCOS.writeObject(notifs.stream().filter(notif -> !notif.isException())
						.collect(Collectors.toCollection(TreeSet::new)));
				outCOS.writeObject(HibernateUtil.getDatabaseName());
				outCOS.writeObject(HibernateUtil.getHost());
				outCOS.writeObject(HibernateUtil.getPort());
				outCOS.writeObject(HibernateUtil.getSettings());
				outCOS.writeObject(FTPUtil.getServer());
				outCOS.writeObject(FTPUtil.getPort());
				outCOS.writeObject(FTPUtil.getUser());
				outCOS.writeObject(FTPUtil.getPass());
				outCOS.close();
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static void deserialiserHost() {
		if (COSHost.exists()) {
			try {
				if (COS.externConnection() && FTPUtil.checkFileExists(COS.loadPath))
					FTPUtil.download(loadPath, COSHost.getAbsolutePath());
				ObjectInputStream inCOS = new ObjectInputStream(new BufferedInputStream(new FileInputStream(COSHost)));
				MOIS_CLOTURE = (Month) inCOS.readObject();
				JOUR_CLOTURE = (int) inCOS.readObject();
				MIN_COMPTE_RATIO = (double) inCOS.readObject();
				budgetInit = (Double) inCOS.readObject();
				compte = (Double) inCOS.readObject();
				servicesList = (List<String>) inCOS.readObject();
				fournisseursList = (List<String>) inCOS.readObject();
				budgetsAnnuels = (Map<Year, List<Double>>) inCOS.readObject();
				anneesArchives = (Set<Year>) inCOS.readObject();
				ID.lastIDsGenerated = (Map<Year, Integer>) inCOS.readObject();
				cosEmail = (String) inCOS.readObject();
				passwordEmail = (String) inCOS.readObject();
				inCOS.close();
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			} catch (ClassNotFoundException e) {
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static void deserialiser() {
		if (COSFile.exists()) {
			try {
				ObjectInputStream inCOS = new ObjectInputStream(new BufferedInputStream(new FileInputStream(COSFile)));
				CompteService.compteActuel = (Compte) inCOS.readObject();
				notifs = (Set<Notification>) inCOS.readObject();
				HibernateUtil.setDatabaseName((String) inCOS.readObject());
				HibernateUtil.setHost((String) inCOS.readObject());
				HibernateUtil.setPort((String) inCOS.readObject());
				HibernateUtil.setSettings((Properties) inCOS.readObject());
				FTPUtil.setServer((String) inCOS.readObject());
				FTPUtil.setPort((Integer) inCOS.readObject());
				FTPUtil.setUser((String) inCOS.readObject());
				FTPUtil.setPass((String) inCOS.readObject());
				inCOS.close();
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			} catch (ClassNotFoundException e) {
			}
		}
	}

	/******************** Paramètres ***************************/

	/**
	 * Connexion Hibernate HibernateUtil.initialize(host, port, "cos_db", username",
	 * password, mySQLConnectorVersion, COS.getAnnotatedClasses(), "update");
	 **/

	public static void addService(String service) {
		servicesList.add(service);
	}

	public static void removeService(String service) {
		servicesList.remove(service);
	}

	public static void addFournisseur(String fournisseur) {
		fournisseur = fournisseur.toUpperCase();
		fournisseursList.add(fournisseur);
	}

	public static void removeFournisseur(String fournisseur) {
		fournisseur = fournisseur.toUpperCase();
		fournisseursList.remove(fournisseur);
	}

	public static void setCosEmail(String cosEmail) {
		COS.cosEmail = cosEmail;
	}

	public static void setPasswordEmail(String passwordEmail) {
		COS.passwordEmail = passwordEmail;
	}

	public static void setJourCloture(int jour, Month mois) {
		MOIS_CLOTURE = mois;
		JOUR_CLOTURE = jour;
	}

	public static void setMinCompteRatio(double minCompteRatio) {
		MIN_COMPTE_RATIO = minCompteRatio;
	}

	// Faire un nouveau budget annuel
	public static void nouveauBudgetAnnuel(double nouveauBudget) {
		List<Double> budget = new ArrayList<Double>();
		budget.add(0, nouveauBudget); // Budget Initial
		budget.add(1, nouveauBudget); // Budget Restant
		budgetsAnnuels.put(getAnneeSociale(), budget);
		budgetInit = nouveauBudget;
		compte = nouveauBudget;
	}

	// modifier le budget restant
	public static void setCompte(double compte) throws COSException {
		if (compte < COS.getBudgetInit() * COS.getMinCompteRatio())
			throw new COSException(COSException.msgCompteRatio);
		if (compte > budgetInit)
			throw new COSException(COSException.msgCompteBudget);
		COS.compte = compte;
		List<Double> budget = new ArrayList<Double>();
		budget.add(0, budgetInit);
		budget.add(1, compte);
		budgetsAnnuels.put(COS.getAnneeSociale(), budget);
	}

	public static File getCOSHost() {
		return COSHost;
	}

}
