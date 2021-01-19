package sample.noyau.util;

import sample.noyau.entity.*;
import sample.noyau.exception.ExcelException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import sample.noyau.service.COS;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class ExcelUtil {
	private static final String CSV_EXTENSION = "csv";
	private static final String XLS_EXTENSION = "xls";
	private static final String XLSX_EXTENSION = "xlsx";
	private static final String SHEET_NAME = "Suivi des prêts";
	private static final List<String> HEADER = List.of("ID", "ID Employé", "Nom", "Prénom", "Service", "N° PV",
			"Type Prêt", "Motif", "Fournisseur", "Produit", "Montant", "Date demande", "Tranche par mois",
			"Reste à rembourser", "Clôturé");
	private static final String DATE_FORMAT = "mmm-yy";
	private static final String DATE_DEMANDE_FORMAT = "dd-mm-yyyy";
	private static final short ROW_HEIGHT = 450;

	// retourne l'extension du fichier
	// une exception est levée si le fichier n'a pas d'extension Excel
	public static String getExcelExtension(String filename) throws ExcelException {
		if (filename.matches("^.*\\." + XLS_EXTENSION + "$")) {
			return XLS_EXTENSION;
		} else if (filename.matches("^.*\\." + XLSX_EXTENSION + "$")) {
			return XLSX_EXTENSION;
		} else if (filename.matches("^.*\\." + CSV_EXTENSION + "$")) {
			return CSV_EXTENSION;
		} else {
			throw new ExcelException(ExcelException.msgExtension);
		}
	}

	// ----------------Importer----------------

	/**
	 * Champs ID ID Employé Nom Prénom Service N° PV Type Prêt Motif Fournisseur
	 * Produit Montant Date demande du prêt Tranche par mois Reste à rembourser
	 * Montants par mois (12) (+ Reports)
	 */

	// -retourne une liste de prêts à partir d'un fichier Excel
	// -si l'extension du fichier n'est pas celle d'un fichier Excel une exception
	// est levée
	// -employeList est la liste de tous les employés, si l'ID de l'employé n'est
	// pas trouvé dans
	// cette liste une exception est levée
	// -pretList est la liste de tous les prêts, elle est utilisée pour éviter les
	// doublons
	// -une exception est levée si le format des champs du fichier n'est pas adéquat
	public static List<Pret> importerPretsNouveauFormat(String filePath, List<Employe> employeList, List<Pret> pretList)
			throws IOException, ExcelException {
		File file = new File(filePath);
		String extension = getExcelExtension(file.getName());
		FileInputStream in = new FileInputStream(file);

		Workbook workbook = null;
		switch (extension) {
			case XLS_EXTENSION:
				workbook = new HSSFWorkbook(in);
				break;
			case XLSX_EXTENSION:
				workbook = new XSSFWorkbook(in);
		}

		Sheet sheet = workbook.getSheetAt(0);

		List<Pret> newPretList = new ArrayList<Pret>();

		int lastRowNum = sheet.getLastRowNum();
		// start from row n°1 to skip header
		for (int i = 1; i <= lastRowNum; i++) {
			newPretList.add(getRowPret(sheet.getRow(i), employeList));
		}

		newPretList.removeAll(pretList);
		newPretList.remove(null);

		return newPretList;
	}

	// -retourne un prêt à partir d'une ligne Excel
	// -employeList est la liste de tous les employés, si l'ID de l'employé n'est
	// pas trouvé dans
	// cette liste une exception est levée
	// -une exception est levée si le format des champs du fichier n'est pas adéquat
	private static Pret getRowPret(Row row, List<Employe> employeList) throws ExcelException {
		if (row == null)
			return null;

		Pret pret;
		Employe employe;
		ID id;
		String idEmploye, type, motif, fournisseur, produit;
		int numPV;
		double montant, reste;
		LocalDate dateDemande, dateDebut = null, dateProchain = null;
		boolean estCloture;
		double[] montantsParMois = new double[12];
		String[] causes = new String[12];

		Cell cell;

		try {
			int offset = HEADER.size();
			Row header = row.getSheet().getRow(0);
			LocalDate firstDate = new java.sql.Date(header.getCell(offset).getDateCellValue().getTime())
					.toLocalDate();
			int j = 0;

			// ID
			id = new ID(row.getCell(j++).getStringCellValue());

			// ID Employé
			idEmploye = row.getCell(j++).getStringCellValue();
			try {
				employe = employeList.stream().filter(employe1 -> idEmploye.equals(employe1.getId()))
						.collect(Collectors.toList()).get(0);
			} catch (IndexOutOfBoundsException e) {
				throw new ExcelException(ExcelException.msgNoEmploye + " (ID : " + idEmploye + ")");
			}

			// Nom
			row.getCell(j++);
			// Prénom
			row.getCell(j++);
			// Service
			row.getCell(j++);

			// N° PV
			numPV = Integer.parseInt(row.getCell(j++).getStringCellValue());

			// Type Prêt
			type = row.getCell(j++).getStringCellValue().toLowerCase();

			// Motif
			cell = row.getCell(j++);
			motif = (cell == null) ? null : cell.getStringCellValue();

			// Fournisseur
			cell = row.getCell(j++);
			fournisseur = (cell == null) ? null : cell.getStringCellValue();

			// Produit
			cell = row.getCell(j++);
			produit = (cell == null) ? null : cell.getStringCellValue();

			// Montant
			montant = Double.parseDouble(row.getCell(j++).getStringCellValue());

			// Date demande
			dateDemande = new java.sql.Date(row.getCell(j++).getDateCellValue().getTime()).toLocalDate();

			// Tranche
			row.getCell(j++);

			// Reste
			reste = Double.parseDouble(row.getCell(j++).getStringCellValue());

			// Est clôturé
			estCloture = row.getCell(j++).getStringCellValue().toLowerCase().contains("oui");

			// Montants par mois + Reports (12)
			for (int i = 0; i < 12; i++) {
				try {
					montantsParMois[i] = Double.parseDouble(row.getCell(j + i).getStringCellValue());
				} catch (NumberFormatException e) {
					causes[i] = row.getCell(j + i).getStringCellValue();
				}
			}

			int index = -1, index2 = -1;
			for (int i = 0; i < 12; i++) {
				if (index == -1 && montantsParMois[i] != 0) {
					index = i;
				}
				if (index2 == -1 && (montantsParMois[11 - i] != 0 || causes[11 - i] != null )) {
					index2 = 11 - i;
				}
				if (index != -1 && index2 != -1)
					break;
			}

			if (index != -1) {
				dateDebut = firstDate.plusMonths(index);
			} else {
				int month = COS.getJourCloture().getMonthValue();
				int day = COS.getJourCloture().getDayOfMonth();

				dateDebut = LocalDate.of(COS.getAnneeSociale().getValue(), month, day).plusMonths(1);
				dateProchain = LocalDate.of(COS.getAnneeSociale().getValue(), month, day).plusMonths(1);
			}

			if (index2 != -1) {
				dateProchain = firstDate.plusMonths(index2 + 1);
				while (dateProchain.isBefore(LocalDate.now())) {
					dateProchain = dateProchain.plusMonths(1);
				}
			}

			if (type.matches("^(.*)social(.*)$")) {
				pret = new PretSocial(id, numPV, employe, dateDemande, montant, TypePret.SOCIAL, dateDebut, motif);
			} else if (type.matches("^(.*)ménager(.*)$") || type.matches("^(.*)menager(.*)$")) {
				pret = new PretElectromenager(id, numPV, employe, dateDemande, montant, TypePret.ELECTROMENAGER,
						dateDebut, produit, fournisseur);
			} else if (type.matches("^(.*)don(.*)$")) {
				pret = new Don(id, numPV, employe, dateDemande, montant, TypePret.DON, motif);
			} else {
				throw new ExcelException(ExcelException.msgTypePret);
			}

			if (pret instanceof PretRemboursable) {
				double tranche = pret.getSomme() / PretRemboursable.TRANCHES;

				if (reste % tranche == 0) {
					((PretRemboursable) pret).setNbMois((int) (reste / tranche));
				} else {
					((PretRemboursable) pret).setNbMois((int) (reste / tranche) + 1);
				}
				((PretRemboursable) pret).setDateProchain(dateProchain);
				((PretRemboursable) pret).setSommeRestante(reste);

				if (index != -1) {
					for (int i = index; i <= index2; i++) {
						if (montantsParMois[i] != 0) {
							LocalDate datePrelevement = firstDate.plusMonths(i);
							new Remboursement(((PretRemboursable) pret), datePrelevement, TypeRemboursement.PRELEVEMENT,
									montantsParMois[i]);
						}
					}
				}
				int i = 0;
				while (i < 11) {
					if (causes[i] != null) {
						LocalDate dateDebutReport = firstDate.plusMonths(i);
						while (i < 11 && causes[i].equals(causes[i+1])) {
							i++;
						}
						LocalDate dateFinReport = (i == 12) ? firstDate.plusMonths(11) : firstDate.plusMonths(i);
						new Report(((PretRemboursable) pret), dateDebutReport, dateFinReport, causes[i]);
					}
					i++;
				}
				if (estCloture) {
					((PretRemboursable) pret).setNbMois(0);
					((PretRemboursable) pret).setDateProchain(null);
					Report lastReport = ((PretRemboursable) pret).getDernierReport();
					if (lastReport.getDateFin().equals(firstDate.plusMonths(11))) {
						lastReport.setDateFin(null);
					}
				}
			}

			return pret;
		} catch (Exception e) {
			if (e instanceof ExcelException) {
				throw e;
			}
			else {
				throw new ExcelException(ExcelException.msgFormat);
			}
		}
	}

	// ----------------Exporter----------------

	/**
	 * Champs ID ID Employé Nom Prénom Service N° PV Type Prêt Motif Fournisseur
	 * Produit Montant Date demande du prêt Tranche par mois Reste à rembourser
	 * Montants par mois (12) (+ Reports)
	 */

	// remplit un fichier Excel avec les informations de la liste des prêts
	public static void exporterPrets(List<Pret> pretList, String path) throws IOException {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet(SHEET_NAME);

		CreationHelper creationHelper = workbook.getCreationHelper();
		CellStyle dateStyle = workbook.createCellStyle();
		dateStyle.setDataFormat(creationHelper.createDataFormat().getFormat(DATE_FORMAT));

		List<Object> header = new ArrayList<Object>(HEADER);
		int year = COS.getAnneeSociale().getValue() - 1;
		int mois = COS.getJourCloture().getMonthValue() + 1;
		int day = COS.getJourPrelevement();
		LocalDate date = LocalDate.of(year, mois, day);
		for (int i = 0; i < 12; i++) {
			header.add(date);
			date = date.plusMonths(1);
		}

		Row row = sheet.createRow(0);
		row.setHeight(ROW_HEIGHT);
		int cellNum = 0;
		for (Object obj : header) {
			Cell cell = row.createCell(cellNum++);

			if (obj instanceof LocalDate) {
				cell.setCellStyle(dateStyle);
				cell.setCellValue(java.sql.Date.valueOf((LocalDate) obj));
			} else {
				cell.setCellValue(obj.toString());
			}
		}

		int rowNum = 1;
		for (Pret pret : pretList) {
			row = sheet.createRow(rowNum++);
			row.setHeight(ROW_HEIGHT);

			for (cellNum = 0; cellNum < header.size(); cellNum++) {
				row.createCell(cellNum);
			}

			for (cellNum = 0; cellNum < 12; cellNum++) {
				row.getCell(cellNum + HEADER.size()).setCellValue("0.0");
			}

			setPretRow(row, pret);
		}

		// Iterate through all columns and auto size them
		for (int i = 0; i < header.size(); i++) {
			sheet.autoSizeColumn(i);
		}

		FileOutputStream out = new FileOutputStream(new File(path));
		workbook.write(out);
		out.close();
	}

	// remplit une ligne d'Excel avec les informations du prêt
	private static void setPretRow(Row row, Pret pret) {
		if (row == null || pret == null)
			return;

		CreationHelper creationHelper = row.getSheet().getWorkbook().getCreationHelper();
		CellStyle dateStyle = row.getSheet().getWorkbook().createCellStyle();
		dateStyle.setDataFormat(creationHelper.createDataFormat().getFormat(DATE_DEMANDE_FORMAT));

		Employe employe = pret.getEmploye();

		Iterator<Cell> cellIterator = row.cellIterator();
		Cell cell;

		String type = null, motif = null, fournisseur = null, produit = null, tranche = null, reste = null, cloture = null;
		List<Remboursement> remboursementList = null;
		List<Report> reportList = null;

		LocalDate debutAnnee = LocalDate.of(COS.getAnneeSociale().getValue() - 1, COS.getJourCloture().getMonthValue(),
				COS.getJourCloture().getDayOfMonth() + 1);
		LocalDate finAnnee = debutAnnee.plusYears(1).plusDays(1);
		int monthCloture = COS.getJourCloture().getMonthValue();

		// ID
		cellIterator.next().setCellValue(pret.getId());

		// ID Employé
		cellIterator.next().setCellValue(employe.getId());

		// Nom
		cellIterator.next().setCellValue(employe.getNom());
		// Prénom
		cellIterator.next().setCellValue(employe.getPrenom());

		// Service
		cellIterator.next().setCellValue(employe.getService());

		// N° PV
		cellIterator.next().setCellValue(Integer.toString(pret.getPV()));

		if (pret instanceof PretRemboursable) {
			tranche = Double.valueOf(pret.getSomme() / PretRemboursable.getTRANCHES()).toString();
			reste = Double.valueOf(((PretRemboursable) pret).getSommeRestante()).toString();
			cloture = (((PretRemboursable) pret).estCloture()) ? "oui" : "";
			remboursementList = ((PretRemboursable) pret).getPrelevements(debutAnnee, finAnnee);
			reportList = (((PretRemboursable) pret)).getReportsList().stream()
					.filter(report -> report.getDateDebut() != null &&
							report.getDateDebut().compareTo(debutAnnee) >= 0)
					.collect(Collectors.toList());
			if (pret instanceof PretSocial) {
				type = "Social";
				motif = ((PretSocial) pret).getMotif();
			} else { // PretElectromenager
				type = "Électroménager";
				fournisseur = ((PretElectromenager) pret).getFournisseur();
				produit = ((PretElectromenager) pret).getProduit();
			}
		} else { // Don
			type = "Don";
			motif = ((Don) pret).getMotif();
			tranche = "0.0";
			reste = "0.0";
		}

		// Type Prêt
		cellIterator.next().setCellValue(type);

		// Motif
		cellIterator.next().setCellValue(motif);
		// Fournisseur
		cellIterator.next().setCellValue(fournisseur);
		// Produit
		cellIterator.next().setCellValue(produit);

		// Montant
		cellIterator.next().setCellValue(Double.toString(pret.getSomme()));

		// Date demande
		cell = cellIterator.next();
		cell.setCellValue(java.sql.Date.valueOf(pret.getDateDemande()));
		cell.setCellStyle(dateStyle);

		// Tranche par mois
		cellIterator.next().setCellValue(tranche);

		// Reste à rembourser
		cellIterator.next().setCellValue(reste);

		// Est clôturé
		cellIterator.next().setCellValue(cloture);

		int offset = HEADER.size() - 1;

		// Remboursements mensuels
		if (remboursementList != null) {
			for (Remboursement remboursement : remboursementList) {
				int month = remboursement.getDate().getMonthValue();

				int index = (month > monthCloture) ? offset + (month - monthCloture)
						: offset + (month - monthCloture) + 12;

				cell = row.getCell(index);

				cell.setCellValue(
						Double.toString(Double.parseDouble(cell.getStringCellValue()) + remboursement.getSomme()));
			}
		}
		// Reports
		if (reportList != null) {
			for (Report report : reportList) {
				LocalDate date = report.getDateDebut();
				LocalDate dateFin = report.getDateFin();
				int month, index;

				if (dateFin == null || report.getDateFin().compareTo(finAnnee) >= 0) {
					month = date.getMonthValue();
					index = (month > monthCloture) ? offset + (month - monthCloture)
							: offset + (month - monthCloture) + 12;
					for (int i = index; i < row.getSheet().getRow(0).getLastCellNum(); i++) {
						cell = row.getCell(i);
						cell.setCellValue(report.getCause());
					}
				} else {
					while (date.compareTo(dateFin) <= 0) {
						month = date.getMonthValue();
						index = (month > monthCloture) ? offset + (month - monthCloture)
								: offset + (month - monthCloture) + 12;

						cell = row.getCell(index);
						cell.setCellValue(report.getCause());

						date = date.plusMonths(1);
					}
				}
			}
		}
	}

	public static void setDemandeRow(Row row, Demande demande) {
		if (row == null || demande == null)
			return;

		CreationHelper creationHelper = row.getSheet().getWorkbook().getCreationHelper();
		CellStyle dateStyle = row.getSheet().getWorkbook().createCellStyle();
		dateStyle.setDataFormat(creationHelper.createDataFormat().getFormat(DATE_DEMANDE_FORMAT));
		Iterator<Cell> cellIterator = row.cellIterator();
		Cell cell;
		cellIterator.next().setCellValue(demande.getId());
		cellIterator.next().setCellValue(demande.getTypePret().toString());
		cellIterator.next().setCellValue(demande.getEmploye().getNomComplet());
		cellIterator.next().setCellValue(demande.getEmploye().getId());
		cell = cellIterator.next();
		cell.setCellValue(java.sql.Date.valueOf(demande.getDateDemande()));
		cell.setCellStyle(dateStyle);
		cellIterator.next().setCellValue(demande.getStatus().toString());
		cellIterator.next().setCellValue(demande.getPV());
		cellIterator.next().setCellValue(demande.getSomme());
		cellIterator.next().setCellValue(demande.getDescription());
		cellIterator.next().setCellValue(demande.getFournisseur());
	}


}
