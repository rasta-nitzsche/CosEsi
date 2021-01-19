package sample.noyau.service;

import java.io.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import sample.noyau.entity.*;
import sample.noyau.exception.COSException;
import sample.noyau.exception.DateException;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.hibernate.Transaction;

import sample.noyau.util.ExcelUtil;
import sample.noyau.util.HibernateUtil;
import sample.noyau.util.ID;

// classe service pour les demandes de prêts

/**
 * Méthodes : ajouter (à partir d'une demande ou à partir d'un fichier Excel)
 * trouverID modifier supprimer refuser accepter (prêt remboursable) accepter
 * (don) lireDemandes importerDemandesExcel exporterDemandesExcel
 */

// TODO: @throws

public class DemandeService {

	// ajouter une demande à la BDD (ne modifier pas l'employé, il y aura des
	// erreurs dans Hibernate)
	public static void ajouter(Demande demande) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			Transaction tx = session.beginTransaction();
			session.save(demande);
			tx.commit();
			session.close();
		}
	}

	// ajouter une demande à partir d'un fichier Excel
	public static void ajouter(String excelLine) {
		String[] line = excelLine.split(";");
		ID id = new ID();
		TypePret typePret = TypePret.valueOf(line[1].replace('é', 'e').toUpperCase());
		String employeId = line[2].split("/")[1];
		Employe employe = EmployeService.trouverID(employeId);
		DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		LocalDate dateDemande = LocalDate.parse(line[3], format);
		Status status = Status.getStatus(line[4]);
		int pv = Integer.valueOf(line[5]);
		double somme = Double.valueOf(line[6]);
		String desc = (line[7] == " " ? null : line[7]);
		String fournisseur = "";
		if (typePret == TypePret.ELECTROMENAGER)
			fournisseur = (line[8] == " " ? null : line[8]);
		Demande demande = new Demande(id, typePret, employe, dateDemande, status, pv, somme, desc, fournisseur);
		ajouter(demande);
	}

	// ajouter une demande à partir d'un fichier Excel (en spécifiant le type)
	public static void ajouter(TypePret typePret, String excelLine) {
		String[] line = excelLine.split(";");
		ID id = new ID();
		String employeId = line[1].split("/")[1];
		Employe employe = EmployeService.trouverID(employeId);
		DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		LocalDate dateDemande = LocalDate.parse(line[2], format);
		Status status = Status.getStatus(line[3]);
		int pv = Integer.valueOf(line[4]);
		double somme = Double.valueOf(line[5]);
		String desc = line[6];
		String fournisseur;
		if (typePret == TypePret.ELECTROMENAGER)
			fournisseur = line[7];
		else
			fournisseur = null;
		Demande demande = new Demande(id, typePret, employe, dateDemande, status, pv, somme, desc, fournisseur);
		ajouter(demande);
	}

	// trouver une demande de la BDD avec son ID
	public static Demande trouverID(String id) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			return session.get(Demande.class, id);
		}
	}

	// modifier une demande dans la BDD
	public static void modifier(Demande demande) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			Transaction transaction = session.beginTransaction();
			session.update(demande);
			transaction.commit();
			session.close();
		}
	}

	// supprimer une demande de la BDD
	public static void supprimer(Demande demande) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			Transaction tx = session.beginTransaction();
			session.delete(demande);
			tx.commit();
			session.close();
		}
	}

	// refuser une demande de prêt
	public static void refuser(Demande demande, int pv) {
		demande.setStatus(Status.REFUSE);
		demande.setPV(pv);
		modifier(demande);
	}

	// accepter une demande de prêt remboursable
	public static PretRemboursable accepter(Demande demande, int pv, LocalDate dateDebut)
			throws DateException, COSException {
		if (dateDebut != null && dateDebut.isBefore(demande.getDateDemande()))
			throw new DateException(DateException.msgDateDemandeDebut);
		if (COS.getCompte() - demande.getSomme() < COS.getBudgetInit() * COS.getMinCompteRatio())
			throw new COSException(COSException.msgCompte);
		PretRemboursable pret = null;
		switch (demande.getTypePret()) {
		case SOCIAL:
			pret = new PretSocial(demande, pv, dateDebut);
			break;
		case ELECTROMENAGER:
			pret = new PretElectromenager(demande, pv, dateDebut);
			break;
		default:
			return pret;
		}
		PretService.ajouter(pret);
		COS.modifierCompte(-demande.getSomme());
		supprimer(demande);
		return pret;
	}

	// accepter une demande de don
	public static Don accepter(Demande demande, int pV) throws COSException {
		if (COS.getCompte() - demande.getSomme() < COS.getBudgetInit() * COS.getMinCompteRatio())
			throw new COSException(COSException.msgCompte);
		Don don = null;
		if (demande.getTypePret() == TypePret.DON) {
			don = new Don(demande, pV);
			PretService.ajouter(don);
			COS.modifierCompte(-demande.getSomme());
			supprimer(demande);
		}
		return don;
	}

	// retourne une liste de toutes les demandes de la BDD
	public static List<Demande> lireDemandes() {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			Transaction tx = session.beginTransaction();
			List<Demande> demandes = session.createQuery("from sample.noyau.entity.Demande", Demande.class).list();
			tx.commit();
			session.close();
			return demandes;
		}
	}
	
	// importer des demandes de prêt à partir d'un Excel XLSX
	public static void importerDemandesExcel(String path) throws IOException {
		File file = new File(path);
		FileInputStream fis = new FileInputStream(file);
		XSSFWorkbook wb = new XSSFWorkbook(fis);
		XSSFSheet sheet = wb.getSheetAt(0);
		Iterator<Row> itr = sheet.iterator();
		Row row = itr.next();
		while (itr.hasNext()) {
			row = itr.next();
			ID id = new ID();
			TypePret typePret = TypePret.valueOf(row.getCell(1).getStringCellValue().replace('é', 'e').toUpperCase());
			Employe employe = EmployeService.trouverID(row.getCell(3).getStringCellValue());
			Date date = row.getCell(4).getDateCellValue();
			LocalDate dateDemande = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			Status status = Status.getStatus(row.getCell(5).getStringCellValue());
			int pv = (int) row.getCell(6).getNumericCellValue();
			double somme = row.getCell(7).getNumericCellValue();
			String desc = row.getCell(8, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
			String fournisseur = "";
			if (typePret == TypePret.ELECTROMENAGER)
				fournisseur = row.getCell(9, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).getStringCellValue();
			Demande demande = new Demande(id, typePret, employe, dateDemande, status, pv, somme, desc, fournisseur);
			ajouter(demande);
		}
	}

	// importer des demandes de prêt à partir d'un fichier Excel (csv)
	public static void importerDemandesCSV(String path) throws IOException {
		File file = new File(path);
		Reader w = new InputStreamReader(new FileInputStream(file), "UTF8");
		BufferedReader fileReader = new BufferedReader(w);
		String line = fileReader.readLine();
		// Check compatibilité
		if (!line.contains("Type"))
			throw new IOException();
		while ((line = fileReader.readLine()) != null) {
			try {
				ajouter(line);
			} catch (org.hibernate.NonUniqueObjectException e) {
			}
		}
		fileReader.close();
	}

	// importer des demandes de prêt à partir d'un fichier Excel (csv) en spécifiant
	// le type de prêt
	public static void importerDemandesExcel(TypePret typePret, String path, String fileName) throws IOException {
		File file = new File(path + fileName + ".csv");
		Reader w = new InputStreamReader(new FileInputStream(file), "UTF8");
		BufferedReader fileReader = new BufferedReader(w);
		String line = fileReader.readLine();
		// Check compatibilité
		switch (typePret) {
		case ELECTROMENAGER:
			if (!line.contains(
					"Identifiant;Employé/ID;Date de demande;Status;Numéro de PV;Somme demandée;Produit;Fournisseur"))
				throw new IOException();
			break;
		default:
			if (!line.contains("Identifiant;Employé/ID;Date de demande;Status;Numéro de PV;Somme demandée;Motif"))
				throw new IOException();
			break;
		}
		while ((line = fileReader.readLine()) != null) {
			try {
				ajouter(typePret, line);
			} catch (org.hibernate.NonUniqueObjectException e4) {
				e4.printStackTrace();
			}
		}
		fileReader.close();
	}
	
	public static void exporterDemandesExcel(String path) throws IOException {
		List<Demande> demandeList = lireDemandes();
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("Liste des demandes");
		CreationHelper creationHelper = workbook.getCreationHelper();
		CellStyle dateStyle = workbook.createCellStyle();
		dateStyle.setDataFormat(creationHelper.createDataFormat().getFormat("dd-mm-yyyy"));
		List<String> HEADER = List.of("Identifiant", "Type de prêt", "Employé", "ID employé", "Date de demande", "Status",
				"Numéro de PV", "Somme demandée", "Motif/Produit", "Fournisseur");
		List<Object> header = new ArrayList<Object>(HEADER);
		Row row = sheet.createRow(0);
		row.setHeight((short) 450);
		int cellNum = 0;
		for (Object obj : header) {
			row.createCell(cellNum++).setCellValue(obj.toString());
		}
		int rowNum = 1;
		for (Demande demande : demandeList) {
			row = sheet.createRow(rowNum++);
			row.setHeight((short) 450);
			for (cellNum = 0; cellNum < header.size(); cellNum++) {
				row.createCell(cellNum);
			}
			ExcelUtil.setDemandeRow(row, demande);
		}
		for (int i = 0; i < header.size(); i++) {
			sheet.autoSizeColumn(i);
		}
		FileOutputStream out = new FileOutputStream(new File(path));
		workbook.write(out);
		out.close();
	}

	// exporter les demandes de la BDD vers un fichier Excel (csv)
	public static void exporterDemandesCSV(String path) throws IOException {
		List<Demande> demandes = lireDemandes();
		File file = new File(path);
		file.createNewFile();
		Writer w = new OutputStreamWriter(new FileOutputStream(file), "UTF8");
		BufferedWriter fileWriter = new BufferedWriter(w);
		Iterator<Demande> iterator = demandes.iterator();
		fileWriter.write(
				"\uFEFFIdentifiant;Type de prêt;Employé/ID;Date de demande;Status;Numéro de PV;Somme demandée;Motif/Produit;Fournisseur");
		while (iterator.hasNext()) {
			Demande demande = iterator.next();
			fileWriter.newLine();
			fileWriter.write(demande.getExcelLine());
		}
		fileWriter.close();
	}
	

	// exporter les demandes de la BDD vers un fichier Excel (csv) en spécifiant le
	// type de prêt
	public static void exporterDemandesExcel(TypePret typePret, String path) throws IOException {
		List<Demande> demandes = lireDemandes().stream().filter(demande -> (demande.getTypePret() == typePret))
				.collect(Collectors.toList());
		File file = new File(path);
		file.createNewFile();
		Writer w = new OutputStreamWriter(new FileOutputStream(file), "UTF8");
		BufferedWriter fileWriter = new BufferedWriter(w);
		Iterator<Demande> iterator = demandes.iterator();
		if (typePret == TypePret.ELECTROMENAGER)
			fileWriter.write(
					"\uFEFFIdentifiant;Employé/ID;Date de demande;Status;Numéro de PV;Somme demandée;Produit;Fournisseur");
		else
			fileWriter.write("\uFEFFIdentifiant;Employé/ID;Date de demande;Status;Numéro de PV;Somme demandée;Motif");
		while (iterator.hasNext()) {
			Demande demande = iterator.next();
			fileWriter.newLine();
			if (typePret == TypePret.ELECTROMENAGER)
				fileWriter.write(demande.getExcelLineElectromenager());
			else
				fileWriter.write(demande.getExcelLineSocialDon());
		}
		fileWriter.close();
	}

	// TODO: (implement) imprimerDemande()
	public static void imprimerDemande() {
	}
}
