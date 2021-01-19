package sample.noyau.exception;

public class ReportException extends NotifException {
	public static final String msgParDefaut = "Erreur de report de prêt";
	public static final String msgNonTrouve = "Aucun report trouvé pour ce prêt durant cette date";
	public static final String msgDejaPreleve = "Vous ne pouvez pas reporter un prêt qui a déjà été prélevé";
	public static final String msgDejaReporte = "Le prélèvement du prêt est déjà reporté";

	public ReportException() {
		super(msgParDefaut);
		setDefaultMessage(msgParDefaut);
	}

	public ReportException(String msg) {
		super(msg);
		setDefaultMessage(msgParDefaut);
	}
}