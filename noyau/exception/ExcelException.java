package sample.noyau.exception;

public class ExcelException extends NotifException {
	public static final String msgParDefaut = "Erreur d'importation/exportation Excel";
	public static final String msgExtension = "Le format du fichier Excel n'est pas supporté\n"
			+ "Formats supportés : .xls, .xlsx";
	public static final String msgFormat = "Le format des informations du fichier Excel n'est pas correct";
	public static final String msgNoEmploye = "L'identifiant de l'employé n'existe pas";
	public static final String msgTypePret = "Le type du prêt n'a pas pu être déterminé";

	public ExcelException() {
		super(msgParDefaut);
		setDefaultMessage(msgParDefaut);
	}

	public ExcelException(String msg) {
		super(msg);
		setDefaultMessage(msgParDefaut);
	}
}
