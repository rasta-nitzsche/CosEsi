package sample.noyau.service;

import java.io.*;
import java.time.LocalDate;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

import sample.noyau.exception.*;

import org.hibernate.NonUniqueObjectException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javafx.collections.FXCollections;
import sample.noyau.entity.*;
import sample.noyau.util.HibernateUtil;
import sample.noyau.util.ID;
import sample.noyau.util.RemboursementPK;
import sample.noyau.util.ReportPK;

// classe service pour tous les types de prêts

/** Méthodes :
 * ajouter
 * supprimer
 * modifier
 * lirePrets
 * lirePretsRemboursables
 * trouverID
 * trouverRemboursementId
 * trouverReportId
 * prelever
 * remboursementAnticipe
 * reporter
 * cloturer
 * archiver
 * desarchiver
 * archiverPrets
 * filtrerPrets
 */

// TODO: @throws
// TODO: not use modifier() here because it results in many queries ? maybe just use it externally anytime it is needed ?

public class PretService {

	/*// Errors in relation OneToMany: verify this in archive
	public static void ajouter(Pret pret){
		EmployeService.modifier(pret.getEmploye());
	}*/

	// ajouter un prêt à la BDD (This is not an old code, this is the correct one)
	public static void ajouter(Pret pret) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		session.save(pret);
		transaction.commit();
		session.close();
	}

	// supprimer un prêt de la BDD
	public static void supprimer(Pret pret) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		session.delete(pret);
		transaction.commit();
		session.close();
	}

	// modifie un prêt dans la BDD
	public static void modifier(Pret pret) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		session.update(pret);
		transaction.commit();
		session.close();
	}

	// retourne une liste de tous les prêts de la BDD
	public static List<Pret> lirePrets() {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		List<Pret> prets = session.createQuery("from sample.noyau.entity.Pret", Pret.class).list();
		transaction.commit();
		session.close();
		return prets;
	}

	// retourne une liste de tous les prêts remboursables de la BDD
	public static List<PretRemboursable> lirePretsRemboursables() {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		List<PretRemboursable> prets = session.createQuery("from sample.noyau.entity.PretRemboursable", PretRemboursable.class).list();
		transaction.commit();
		session.close();
		return prets;
	}

	// retourne une liste de tous les dons de la BDD
	public static List<Don> lireDons() {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		List<Don> dons = session.createQuery("from sample.noyau.entity.Don", Don.class).list();
		transaction.commit();
		session.close();
		return dons;
	}

	public static List<Don> lireDonsAnnee() {
		List<Don> dons = lireDons();
		List<Don> donsAnnee  = new ArrayList<Don>();
		int mois = COS.getJourCloture().getMonthValue();
		int jour = COS.getJourCloture().getDayOfMonth();
		LocalDate datemin ;
		LocalDate datemax ;
		if (LocalDate.now().compareTo(LocalDate.of(LocalDate.now().getYear()  , mois, jour)) >= 0 )
		{
			datemin = LocalDate.of(LocalDate.now().getYear() , mois, jour);
			datemax = LocalDate.of(LocalDate.now().getYear() + 1 , mois, jour);
		}
		else
		{
			datemin = LocalDate.of(LocalDate.now().getYear() -1 , mois, jour);
			datemax = LocalDate.of(LocalDate.now().getYear() , mois, jour);
		}
		Iterator<Don> it = dons.iterator();
		Don don;
		while (it.hasNext())
		{
			don = it.next();
			if ((don.getDateDemande().compareTo(datemin) >= 0 ) && ((don.getDateDemande().compareTo(datemax) < 0 ))) donsAnnee.add(don);
		}

		return donsAnnee;
	}

	public static List<PretRemboursable> lirePretsAnnee() {
		List<PretRemboursable> prets = lirePretsRemboursables();
		List<PretRemboursable> pretsAnnee  = new ArrayList<PretRemboursable>();
		Iterator<PretRemboursable> it = prets.iterator();
		PretRemboursable pret;
		int mois = COS.getJourCloture().getMonthValue();
		int jour = COS.getJourCloture().getDayOfMonth();
		LocalDate datemin ;
		LocalDate datemax ;
		if (LocalDate.now().compareTo(LocalDate.of(LocalDate.now().getYear()  , mois, jour)) >= 0 )
		{
			datemin = LocalDate.of(LocalDate.now().getYear() , mois, jour);
			datemax = LocalDate.of(LocalDate.now().getYear() + 1 , mois, jour);
		}
		else
		{
			datemin = LocalDate.of(LocalDate.now().getYear() -1 , mois, jour);
			datemax = LocalDate.of(LocalDate.now().getYear() , mois, jour);
		}
		while (it.hasNext())
		{
			pret = it.next();
			if (pret.getDateDebut() == null ) pretsAnnee.add(pret); // pret non debuté
			else
			{
				if (pret.getNbMois() != 0) pretsAnnee.add(pret);  //pret en cour
				else
				{
					// Pret remboursé automatiquement
					if (pret.getSommeRestante() == 0 )
					{
						if ((pret.getDernierRemboursement().getDate().compareTo(datemin) >= 0 ) && ((pret.getDernierRemboursement().getDate().compareTo(datemax) < 0 ))) pretsAnnee.add(pret);
					}
					else //Pret cloturé
					{
						if ((pret.getDernierReport().getDateDebut().compareTo(datemin) >= 0 ) && ((pret.getDernierReport().getDateDebut().compareTo(datemax) < 0 ))) pretsAnnee.add(pret);

					}
				}
			}
		}
		return pretsAnnee;
	}

	// trouver un prêt de la BDD avec son ID
	public static Pret trouverID(String id) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Pret pret = session.get(Pret.class, id);
		session.close();
		return pret;
	}

	public static Pret trouverID(ID id) {
		return trouverID(id.toString());
	}

	public static Pret trouverID(int num, Year anneeSociale) {
		return trouverID(new ID(num, anneeSociale).toString());
	}

	// trouver un remboursement de la BDD avec son ID
	// ClassCastException: Pour les prets non remboursables, parce qu'il n y a pas de remboursements pour les prets non remboursables
	public static Remboursement trouverRemboursementId(RemboursementPK remboursementPK)
			throws ClassCastException {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Remboursement remboursement = session.get(Remboursement.class, remboursementPK);
		session.close();
		return remboursement;
	}

	public static Remboursement trouverRemboursementId(String id, LocalDate date, TypeRemboursement typeRemboursement)
			throws ClassCastException {
		return trouverRemboursementId(new RemboursementPK((PretRemboursable) trouverID(id), date, typeRemboursement));
	}

	public static Remboursement trouverRemboursementId(ID id, LocalDate date, TypeRemboursement typeRemboursement)
			throws ClassCastException {
		return trouverRemboursementId(new RemboursementPK((PretRemboursable) trouverID(id), date, typeRemboursement));
	}

	public static Remboursement trouverRemboursementId(PretRemboursable pret, LocalDate date, TypeRemboursement typeRemboursement)
			throws ClassCastException {
		return trouverRemboursementId(new RemboursementPK(pret, date, typeRemboursement));
	}

	// trouver un report de le BDD avec son ID
	// ClassCastException: Pour les prets non remboursables, parce qu'il n y a pas des reports pour les prets non remboursables

	public static Report trouverReportId(ReportPK reportPK)
			throws ClassCastException {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Report report = session.get(Report.class, reportPK);
		session.close();
		return report;
	}

	public static Report trouverReportId(String id,LocalDate date)
			throws ClassCastException {
		return trouverReportId(new ReportPK((PretRemboursable) trouverID(id), date));
	}

	public static Report trouverReportId(ID id,LocalDate date)
			throws ClassCastException {
		return trouverReportId(new ReportPK((PretRemboursable) trouverID(id), date));
	}

	public static Report trouverReportId(PretRemboursable pret,LocalDate date)
			throws ClassCastException {
		return trouverReportId(new ReportPK(pret, date));
	}

	// effectue le prélèvement du prêt remboursable en spécifiant la date de prélèvement et la somme à prélever
	// exceptions : si la date ou la somme ne sont pas conformes, ou si le prêt est déjà clôturé, ou bien
	// s'il existe un report
	public static void prelever(PretRemboursable pret, LocalDate datePrelevement, double sommePrelevee)
			throws ClotureException, RemboursementException, DateException, ReportException {
		if (sommePrelevee > pret.getSommeRestante()) throw new RemboursementException(RemboursementException.msgSommeRemboursee);
		if (!pret.commencePrelevement()) throw new RemboursementException(RemboursementException.msgDebutRemboursement);
	/*	if (datePrelevement.isBefore(pret.getDateDebut()) ||
				datePrelevement.isAfter(pret.getDateProchain()) ||
				datePrelevement.isAfter(LocalDate.now())) throw new DateException(DateException.msgDatePrelevement);
*/		if (pret.estPreleve(datePrelevement)) throw new RemboursementException(RemboursementException.msgDejaPreleve);
		if (pret.estCloture()) throw new ClotureException(ClotureException.msgCloture);
		if (!pret.getReports(datePrelevement).isEmpty()) throw new ReportException(ReportException.msgDejaReporte);

		new Remboursement(pret, datePrelevement, TypeRemboursement.PRELEVEMENT, sommePrelevee);
		pret.setSommeRestante(pret.getSommeRestante() - sommePrelevee);
		COS.modifierCompte(sommePrelevee);
		pret.setNbMois(pret.getNbMois() - 1);
		if (pret.getNbMois() != 0) pret.setDateProchain(datePrelevement.plusMonths(1));
		else pret.setDateProchain(null);

		// TODO Pas la piene de faire datePrelevement.isEqual(dateProchain) on a deja lancé une exception si c'etait a faux
		// if (datePrelevement.isEqual(dateProchain)  && pret.getSommeRestante() != 0) pret.setDateProchain()
		// TODO  Pas la piene probleme traité dans discord
		//if (pret.getNbMois() == 0 || pret.getSommeRestante() == 0) cloturer(pret, pret.getDernierPrelevement().getDate(), CAUSE_FIN);

		modifier(pret);
	}

	// la somme à prélever est la tranche mensuelle
	public static void prelever(PretRemboursable pret, LocalDate datePrelevement)
			throws ClotureException, RemboursementException, DateException, ReportException {
		double sommePrelevee;
		double tranche = pret.getSomme() / PretRemboursable.TRANCHES;
		// TODO : c'est par example il a fait un apport personnel c'est pas toujour la tranche qu'on doit prelever .on doit faire un test bien avant
		if ((pret.getSommeRestante() - tranche) >= 0)  sommePrelevee = tranche;
		else sommePrelevee = pret.getSommeRestante();
		prelever(pret, datePrelevement, sommePrelevee);
	}

	// la somme à prélever est la tranche mensuelle
	// la date de prélèvement est la date prochaine du prêt
	public static void prelever(PretRemboursable pret)
			throws ClotureException, RemboursementException, DateException, ReportException {
		double sommePrelevee;
		double tranche = pret.getSomme() / PretRemboursable.TRANCHES;
		// TODO : c'est par example il a fait un apport personnel c'est pas toujour la tranche qu'on doit prelever .on doit faire un test bien avant
		if ((pret.getSommeRestante() - tranche) >= 0)  sommePrelevee = tranche;
		else sommePrelevee = pret.getSommeRestante();
		prelever(pret, pret.getDateProchain(), sommePrelevee);
	}

	public static void rembourserFourniseur(PretElectromenager pret) throws RemboursementException {
		if (!pret.commencePrelevement())
			throw new RemboursementException(RemboursementException.msgDebutRemboursement);
		if (pret.getSommeRestante() - pret.getSommeRembourseFrounisser() == 0.0)
			throw new RemboursementException(RemboursementException.msgTotalementRembourse);
		if (pret.getDernierRemboursement() == null)
		{
			if ( ! pret.estReporte(pret.getDateDebut())) throw new RemboursementException(RemboursementException.msgNonReportee);
		}
		else
		{
			if ( ! pret.estReporte(pret.getDernierRemboursement().getDate().plusMonths(1)))  throw new RemboursementException(RemboursementException.msgNonReportee);
		}
		double sommeRembourse;
		double tranche = pret.getSomme() / PretRemboursable.TRANCHES;
		if ((pret.getSommeRestante() - tranche) >= 0)
			sommeRembourse = tranche;
		else
			sommeRembourse = pret.getSommeRestante();
		COS.modifierCompte(-sommeRembourse);
		if (pret.getDernierRemboursement() == null)
			new Remboursement(pret, pret.getDateDebut(), TypeRemboursement.REMBOURSEMENT_FOURNISSEUR, sommeRembourse);
		else
			new Remboursement(pret, pret.getDernierRemboursement().getDate().plusMonths(1),
					TypeRemboursement.REMBOURSEMENT_FOURNISSEUR, sommeRembourse);
		modifier(pret);
	}

	// effectue un remboursement anticipé sur un prêt remboursable
	public static void remboursementAnticipe(PretRemboursable pret, LocalDate dateRemboursement, double sommeRemboursee)
			throws ClotureException, RemboursementException {
		if (pret.estCloture()) throw new ClotureException(ClotureException.msgCloture);
		if (sommeRemboursee > pret.getSommeRestante()) throw new RemboursementException(RemboursementException.msgSommeRemboursee);
		if (!pret.commencePrelevement()) throw new RemboursementException(RemboursementException.msgDebutRemboursement);

		Remboursement remboursement = new Remboursement(pret, dateRemboursement, TypeRemboursement.APPORT_EXTERNE, sommeRemboursee);
		pret.setSommeRestante(pret.getSommeRestante() - sommeRemboursee);
		COS.modifierCompte(sommeRemboursee);
		if (pret.getSommeRestante() == 0 ) {
			pret.setNbMois(0);
			pret.setDateProchain(null);

		} else {
			double tranche = pret.getSomme() / PretRemboursable.TRANCHES;
			if (pret.getSommeRestante() % tranche == 0) {
				pret.setNbMois((int) (pret.getSommeRestante() / tranche));
			} else {
				pret.setNbMois((int) (pret.getSommeRestante() / tranche) + 1);
			}
		}

		modifier(pret);
	}

	//Rembourser la totalite
	public static void remboursementAnticipe(PretRemboursable pret, LocalDate dateRemboursement) throws ClotureException, RemboursementException {
		if (pret.estCloture()) throw new ClotureException(ClotureException.msgCloture);
		if (!pret.commencePrelevement()) throw new RemboursementException(RemboursementException.msgDebutRemboursement);

		Remboursement remboursement = new Remboursement(pret, dateRemboursement, TypeRemboursement.APPORT_EXTERNE, pret.getSommeRestante());
		COS.modifierCompte(pret.getSommeRestante());
		pret.setSommeRestante(0);
		pret.setNbMois(0);
		pret.setDateProchain(null);
		modifier(pret);
	}

	// reporter un prélèvement de prêt remboursable jusqu'à dateFin avec sa cause
	public static void reporter(PretRemboursable pret, LocalDate dateDebut, LocalDate dateFin, String cause)
			throws ClotureException, DateException, ReportException, RemboursementException {
		if (pret.estCloture()) throw new ClotureException(ClotureException.msgCloture);
		//	if (!(dateFin == null) && pret.estPreleve(dateDebut, dateFin)) throw new ReportException(ReportException.msgDejaPreleve);
		if (!(dateFin == null) && !pret.commencePrelevement()) throw new RemboursementException(RemboursementException.msgDebutRemboursement);
		if (dateFin != null && dateDebut.isAfter(dateFin)) throw new DateException(DateException.msgDateDebutFin);

		Report report = new Report(pret, dateDebut, dateFin, cause);
		pret.setDateProchain(dateFin.plusMonths(1));

		modifier(pret);
	}

	// clôture un prêt remboursable avec sa cause
	public static void cloturer(PretRemboursable pret,String cause)
			throws ClotureException {
		if (pret.estCloture()) throw new ClotureException(ClotureException.msgCloture);

		pret.setNbMois(0);
		new Report(pret, pret.getDateProchain(), null, cause);

		modifier(pret);
	}

	// archiver un prêt avec son année sociale correspondante
	public static void archiver(Pret pret) throws ClotureException {
		int anneePret = pret.getAnneeSociale().getValue();
		// si le prêt n'est pas remboursable (i.e: don) ou il l'est et il est clôturé
		if (!PretRemboursable.class.isAssignableFrom(pret.getClass()) || ((PretRemboursable) pret).estCloture()) {
			Session session = HibernateUtil.getSessionFactory().openSession();
			Transaction transaction = session.beginTransaction();
			session.createSQLQuery(
					"CREATE DATABASE IF NOT EXISTS 	" + HibernateUtil.getDatabaseName() + "_" + anneePret)
					.executeUpdate();
			transaction.commit();
			session.close();
			session = HibernateUtil.getSessionFactoryArchive(anneePret, COS.getAnnotatedClasses()).openSession();
			transaction = session.beginTransaction();
			Employe employeReel = pret.getEmploye();
			Employe employeFictif = session.get(Employe.class, employeReel.getId());
			if (employeFictif == null) {
				employeFictif = new Employe(employeReel);
				pret.setEmploye(employeFictif);
				employeFictif.getPretsList().add(pret);
				session.save(employeFictif);
			} else {
				try {
					session.save(pret);
				} catch (NonUniqueObjectException e) {
					session.update(pret);
				}
			}
			transaction.commit();
			session.close();
			pret.setEmploye(employeReel);
			supprimer(pret);
			COS.anneesArchivees().add(Year.of(anneePret));
		} else
			throw new ClotureException();
	}

	// désarchiver un prêt
	public static void desarchiver(Pret pret) {
		ajouter(pret);
		COS.addAnnotatedClasses();
		Session session = HibernateUtil
				.getSessionFactoryArchive(pret.getAnneeSociale().getValue(), COS.getAnnotatedClasses()).openSession();
		Transaction transaction = session.beginTransaction();
		session.delete(pret);
		transaction.commit();
		session.close();
	}

	// Archiver tous les prêts cloturés dans leurs années sociales correspondantes
	public static void archiverPrets() {
		List<Pret> prets = lirePrets();
		for (Pret pret : prets) {
			if (!PretRemboursable.class.isAssignableFrom(pret.getClass()) || ((PretRemboursable) pret).estCloture()) {
				try {
					archiver(pret);
				} catch (ClotureException | NonUniqueObjectException e) {
				}
			}
		}
	}

	// Lire l'archive d'une année sociale
	public static List<Pret> lireArchive(int anneeSociale) {
		try (Session session = HibernateUtil.getSessionFactoryArchive(anneeSociale, COS.getAnnotatedClasses())
				.openSession()) {
			Transaction transaction = session.beginTransaction();
			List<Pret> prets = session.createSQLQuery("SELECT * FROM Pret").addEntity(Pret.class).list();
			transaction.commit();
			session.close();
			return prets;
		}
	}

	public static void exporterPretsCSV(List<Pret> pretsList, String path) throws IOException {
		File file = new File(path);
		file.createNewFile();
		Writer w = new OutputStreamWriter(new FileOutputStream(file), "UTF8");
		BufferedWriter fileWriter = new BufferedWriter(w);
		Iterator<Pret> iterator = pretsList.iterator();
		fileWriter.write("\uFEFFIdentifiant;Type de prêt;Employé/ID;Date de demande;Numéro de PV;Somme demandée;Date de début;Prochain Prélèvement;Mois Restants;Somme Restante;Motif;Produit;Fournisseur");
		while (iterator.hasNext()) {
			Pret pret = iterator.next();
			fileWriter.newLine();
			fileWriter.write(pret.getExcelLine());
		}
		fileWriter.close();
	}


	// retourne une liste des prêts filtrée selon les critères indiqués
	public static List<Pret> filtrerPrets(Map<TypeCritere,Object> mapCriteres)
			throws ExceptionPileVide {
		List<Pret> listPretsResulats = new ArrayList<Pret>();
		List<Pret> listPrets = lirePrets();
		TypeCritere cle;
		Object valeur;
		for (Map.Entry<TypeCritere,Object> E : mapCriteres.entrySet() ) {
			cle = E.getKey();
			valeur = E.getValue();
			switch (cle) {
				// filtrer selon l'ID de l'employé
				case CRITERE1:
					String id = (String) valeur;
					listPretsResulats.addAll(listPrets.stream()
							.filter(pret-> (id.equals(pret.getEmploye().getId())))
							.collect(Collectors.toList()));

					break;
				// somme du prêt entre sommeMin et sommeMax inclus
				case CRITERE2:
					Pile pileSomme = (Pile) valeur;
					Double sommeMin = (double) pileSomme.depiler();
					Double sommeMax = (double) pileSomme.depiler();
					listPretsResulats.addAll(listPrets.stream()
							.filter(pret-> (pret.getSomme() >= sommeMin && pret.getSomme() <= sommeMax))
							.collect(Collectors.toList()));

					break;
				// date de demande du prêt entre dateMin et dateMax
				case CRITERE3:
					Pile pileDate = (Pile) valeur;
					LocalDate dateMin = (LocalDate) pileDate.depiler();
					LocalDate  dateMax = (LocalDate) pileDate.depiler();
					listPretsResulats.addAll(listPrets.stream()
							.filter(pret-> (pret.getDateDemande().compareTo(dateMin) >= 0 &&
									pret.getDateDemande().compareTo(dateMax) <= 0))
							.collect(Collectors.toList()));
					break;
				// filtrer selon le numéro du PV
				case CRITERE4:
					int numeroPv = (int) valeur;
					listPretsResulats.addAll(listPrets.stream()
							.filter(pret-> (pret.getPV() == numeroPv ))
							.collect(Collectors.toList()));
					break;
				// filtrer selon les prêts remboursables clôturés (ajoute aussi les prêts non remboursables)
				case CRITERE5:
					listPretsResulats.addAll(listPrets.stream()
							.filter(pret-> (PretRemboursable.class.isAssignableFrom(pret.getClass()) &&
									((PretRemboursable) pret).getNbMois() == 0  && ((PretRemboursable) pret).getSommeRestante() == 0.0 ))
							.collect(Collectors.toList()));
					break;
				// filtrer selon le type de prêt (nom de classe)
				case CRITERE6:
					String nomClass = (String) valeur;
					listPretsResulats.addAll(listPrets.stream()
							.filter(pret-> (pret.getClassName().equals(nomClass)))
							.collect(Collectors.toList()));
					break;
				// filtrer selon le nom de l'employé
				case CRITERE7:
					String nomEmploye = (String) valeur;
					listPretsResulats.addAll(listPrets.stream()
							.filter(pret-> (pret.getEmploye().getNom().equalsIgnoreCase(nomEmploye)))
							.collect(Collectors.toList()));
					break;
				// Affichage des prets cloturer
				case CRITERE8:
					listPretsResulats.addAll(listPrets.stream()
							.filter(pret-> (PretRemboursable.class.isAssignableFrom(pret.getClass()) &&
									((PretRemboursable) pret).getNbMois() == 0  && ((PretRemboursable) pret).getSommeRestante() != 0.0 ))
							.collect(Collectors.toList()));
					break;

			}
			listPrets.clear();
			listPrets.addAll(listPretsResulats);
			listPretsResulats.clear();
		}
		return listPrets;
	}

	public static List<PretRemboursable> pretsEnCours(List<PretRemboursable> list){
		List<PretRemboursable> listPrets = FXCollections.observableArrayList();
		for (PretRemboursable pret : list){
			if (!pret.estCloture()){
				if (LocalDate.now().plusMonths(1).isAfter(pret.getDateProchain()))
					listPrets.add(pret);
				else if (pret instanceof PretElectromenager) {
					if ((pret.getSommeRestante() - ((PretElectromenager) pret).getSommeRembourseFrounisser() != 0.0))
						listPrets.add(pret);
				}
			}
		}
		return listPrets;
	}
}