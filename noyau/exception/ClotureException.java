package sample.noyau.exception;

public class ClotureException extends NotifException {
	public static final String msgParDefaut = "Erreur de clôture de prêt";
	public static final String msgNonCloture = "Le prêt n'est pas encore clôturé";
	public static final String msgCloture = "Le prêt est clôturé";
	public static final String msgClotureDeja = "Le prêt est déjà clôturé";
	
	public ClotureException() {
		super(msgParDefaut);
		setDefaultMessage(msgParDefaut);
	}

	public ClotureException(String msg) {
		super(msg);
		setDefaultMessage(msgParDefaut);
	}
}
