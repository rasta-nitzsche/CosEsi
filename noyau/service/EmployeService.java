package sample.noyau.service;

import javafx.stage.FileChooser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import sample.Main;
import sample.noyau.exception.ClotureException;

import org.hibernate.Session;
import org.hibernate.Transaction;

import sample.noyau.entity.*;
import sample.noyau.util.HibernateUtil;
import sample.noyau.util.Notification;
import tray.notification.NotificationType;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.persistence.Query;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/**
 * Méthodes : ajouter ajouter (liste d'employés) supprimer trouverID modifier
 * lireEmployes ajouterPret cloturerPrets modifierService pretsEmploye
 * demandesEmploye
 */

// Classe service pour les employés

public class EmployeService {

	// ajouter un employé à la BDD
	public static void ajouter(Employe employe) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		session.save(employe);
		transaction.commit();
		session.close();
	}

	// TODO: for loop many queries
	// ajouter une liste d'employés à la BDD
	public static void ajouter(List<Employe> employes) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		for (Employe employe : employes)
			session.save(employe);
		transaction.commit();
		session.close();
	}

	// supprimer un employé de la BDD
	public static void supprimer(Employe employe) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		session.delete(employe);
		transaction.commit();
		session.close();
	}

	// trouver un employé de la BDD avec son ID
	public static Employe trouverID(String id) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Employe employe = session.get(Employe.class, id);
		session.close();
		return employe;
	}

	// modifier un employé de la BDD
	public static void modifier(Employe employe) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		session.update(employe);
		transaction.commit();
		session.close();
	}

	// ajouter un prêt avec son employé à la BDD
	public static void ajouterPret(Employe employe, Pret pret) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		pret.setEmploye(employe);
		employe.getPretsList().add(pret);
		modifier(employe);
	}

	// TODO: postponed (salaire employé)
//	public void modifierSalaireBase() {
//
//    }
//
//    public void initialiserSalaire() {
//
//    }
//
//    public void modifierSalaire() {
//
//    }
//
//    public void verifierSeuil() {
//
//    }

	// TODO: for loop too many queries on database
	// parcoure tous les prêts remboursables de l'employé en les clôturant avec la
	// même cause
	public void cloturerPrets(Employe employe, String cause) throws ClotureException {
		// retourne seulement les prêts remboursables de l'employé
		List<Pret> pretList = employe.getPretsList().stream()
				.filter(pret -> PretRemboursable.class.isAssignableFrom(pret.getClass())).collect(Collectors.toList());

		for (Pret pret : pretList) {
			PretService.cloturer((PretRemboursable) pret, cause);
		}
	}

	// modifie le service occupé par l'employé
	public void modifierService(Employe employe, String service) {
		employe.setService(service);
		modifier(employe);
	}

	// retourne la liste des prêts de l'employé
	public List<Pret> pretsEmploye(Employe employe) {
		return employe.getPretsList();
	}

	// retourne la liste des demandes de prêts de l'employé
	public List<Demande> demandesEmploye(Employe employe) {
		return employe.getDemandesList();
	}

	public static Employe lireEmploye(String nomEtPrenom) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			String[] strings = nomEtPrenom.split(" ");
			String nom = strings[0];
			String prenom = strings[1];
			for (int i = 2; i < strings.length; i++) {
				prenom = prenom.concat(" " + strings[i]);
			}
			Query query = session
					.createQuery("from sample.noyau.entity.Employe as e where e.prenom = :prenom and e.nom = :nom");
			query.setParameter("prenom", prenom);
			query.setParameter("nom", nom);
			return (Employe) query.getSingleResult();
		}
	}

	public static ObservableList<Employe> lireEmploye() {
		return FXCollections.observableArrayList(lireEmployes());
	}

	// retourne la liste de tous les employés de la BDD
	public static List<Employe> lireEmployes() {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction transaction = session.beginTransaction();
		List<Employe> employes = session.createSQLQuery("SELECT * FROM Employe").addEntity(Employe.class).list();
		transaction.commit();
		session.close();
		return employes;
	}

	public static List<Employe> filtrerEmployes(Map<TypeCritere, Object> mapCriteres) {
		List<Employe> listEmployesResulats = new ArrayList<Employe>();
		List<Employe> listEmployes = lireEmployes();
		TypeCritere cle;
		Object valeur;
		for (Map.Entry<TypeCritere, Object> E : mapCriteres.entrySet()) {
			cle = E.getKey();
			valeur = E.getValue();
			switch (cle) {
				case CRITERE1:
					String id = (String) valeur;
					listEmployesResulats.addAll(listEmployes.stream().filter(employe -> (employe.getId().equals(id)))
							.collect(Collectors.toList()));

					break;
				case CRITERE2:
					String nom = (String) valeur;
					listEmployesResulats.addAll(listEmployes.stream()
							.filter(employe -> (employe.getNom().equalsIgnoreCase(nom))).collect(Collectors.toList()));

					break;
				// date de demande du prêt entre dateMin et dateMax
				case CRITERE3:
					String prenom = (String) valeur;
					listEmployesResulats.addAll(listEmployes.stream()
							.filter(employe -> (employe.getPrenom().equals(prenom))).collect(Collectors.toList()));
					break;
			}
			listEmployes.clear();
			listEmployes.addAll(listEmployesResulats);
			listEmployesResulats.clear();
		}

		return listEmployes;
	}

	// importer les employes
	public static void lireEmployesExcel(String path) throws IOException {
		File file = new File(path);
		FileInputStream fis = new FileInputStream(file);
		XSSFWorkbook wb = new XSSFWorkbook(fis);
		XSSFSheet sheet = wb.getSheetAt(0);
		Iterator<Row> itr = sheet.iterator();
		while (itr.hasNext()) {
			Row row = itr.next();
			String id = Integer.toString((int) row.getCell(0).getNumericCellValue());
			String code = Integer.toString((int) row.getCell(1).getNumericCellValue());
			String numeroSS = row.getCell(2).getStringCellValue();
			String nom = row.getCell(3).getStringCellValue();
			String prenom = row.getCell(4).getStringCellValue();
			Date date = row.getCell(5).getDateCellValue();
			LocalDate dateNaissance = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			String grade = row.getCell(6).getStringCellValue();
			Date date1 = row.getCell(7, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getDateCellValue();
			LocalDate datePremEm = date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			String situationFamiliale = row.getCell(8, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
			String service = row.getCell(9, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
			String numeroCCP = row.getCell(10).getStringCellValue();
			String email = row.getCell(11, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
			Employe employe = new Employe(id, code, nom, prenom, dateNaissance, datePremEm, grade, situationFamiliale,
					email, service, numeroSS, numeroCCP);
			EmployeService.ajouter(employe);
		}
	}

	public static List<Employe> employesEnCours(List<Employe> employes) {
		List<Employe> employesEnCours = FXCollections.observableArrayList();

		for (Employe employe : employes) {
			if (PretService.pretsEnCours(employe.getPretsRemboursables()).size() > 0)
				employesEnCours.add(employe);
		}
		return employesEnCours;
	}

	public static void printPDF(List<Employe> list) {
		try {
			PDDocument document = new PDDocument();

			PDPage blankPage = new PDPage();

			// Adding the blank page to the document
			document.addPage(blankPage);

			PDPageContentStream contentStream = new PDPageContentStream(document, blankPage);
			contentStream.beginText();
			contentStream.setFont(PDType1Font.HELVETICA, 24);
			contentStream.setLeading(32f);
			contentStream.newLineAtOffset(100, 725);
			contentStream.showText("Liste des employés ayant un prêt en cours");
			contentStream.newLine();

			contentStream.setFont(PDType1Font.HELVETICA, 18);
			contentStream.setLeading(17f);
			// contentStream.newLineAtOffset(25, 550);

			int number = 0;
			for (Employe employe : list) {
				number++;
				contentStream.showText("  " + employe.getId() + "    " + employe.getNom() + "   " + employe.getPrenom()
						+ "   " + employe.getMontantRembourse() + "  DA");
				if (number < 35)
					contentStream.newLine();
				else {
					contentStream.endText();
					contentStream.close();

					number = 0;
					blankPage = new PDPage();
					document.addPage(blankPage);

					contentStream = new PDPageContentStream(document, blankPage);
					contentStream.beginText();
					contentStream.setFont(PDType1Font.HELVETICA, 18);
					contentStream.setLeading(17f);
					contentStream.newLineAtOffset(100, 725);
				}
			}

			contentStream.endText();
			contentStream.close();

			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Sauvgarder la liste des employés");
			fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
			fileChooser.setInitialFileName("employesListe.pdf");
			FileChooser.ExtensionFilter pdfExtensionFilter = new FileChooser.ExtensionFilter("PDF", "*.pdf");
			fileChooser.getExtensionFilters().add(pdfExtensionFilter);
			fileChooser.setSelectedExtensionFilter(pdfExtensionFilter);
			File file = fileChooser.showSaveDialog(Main.getPrimaryStage());
			if (file != null) {
				try {
					document.save(file);
				} catch (Exception e) {
					(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
				}
			}

			// Closing the document
			document.close();
		} catch (Exception e) {
			(new Notification(e)).ajouterTrayNotif(NotificationType.ERROR);
		}
	}
}
