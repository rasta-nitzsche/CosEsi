package sample.noyau.exception;

public class DateException extends NotifException {
	public static final String msgParDefaut = "Erreur de date";
	public static final String msgDateDebutFin = "La date de début doit être avant la date de fin";
	public static final String msgDateDebutProchain = "La date du prochain prélèvement doit être après la date de début";
	public static final String msgDatePrelevement = "La date de prélèvement est erronée";
	public static final String msgDateRemboursement = "La date de remboursement est erronée";
	public static final String msgDateDemandeDebut = "La date de début doit être après la date de demande";

	public DateException() {
		super(msgParDefaut);
		setDefaultMessage(msgParDefaut);
	}

	public DateException(String msg) {
		super(msg);
		setDefaultMessage(msgParDefaut);
	}
}