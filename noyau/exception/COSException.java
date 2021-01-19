package sample.noyau.exception;

import sample.noyau.service.COS;

public class COSException extends NotifException {
	public static final String msgParDefaut = "Erreur dans le service COS";
	public static final String msgCompte = "Vous ne pouvez plus prélever du compte";
	public static final String msgAnneeArchive = "L'année n'existe pas dans l'archive";
	public static final String msgCompteBudget = "Le budget restant doit être inférieur au budget initial de cette année";
	public static final String msgCompteRatio = "Le budget restant est érronée doit être supérieur au"
			+ (int) Math.round((COS.getMinCompteRatio() * 100)) + "% du budget initial de cette année";

	public COSException() {
		super(msgParDefaut);
		setDefaultMessage(msgParDefaut);
	}

	public COSException(String msg) {
		super(msg);
		setDefaultMessage(msgParDefaut);
	}
}
