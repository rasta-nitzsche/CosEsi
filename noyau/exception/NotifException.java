package sample.noyau.exception;

public class NotifException extends Exception {
	public static String msgParDefaut = "Type de l'erreur";

	public String getDefaultMessage() {
		return msgParDefaut;
	}

	public void setDefaultMessage(String msg) {
		msgParDefaut = msg;
	}

	public NotifException() {
		super("Description de l'erreur");
	}

	public NotifException(String msg) {
		super(msg);
	}
}
