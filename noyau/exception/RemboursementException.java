package sample.noyau.exception;

public class RemboursementException extends NotifException {
	public static final String msgParDefaut = "Erreur de remboursement";
	public static final String msgDebutPrelevement = "Le prélèvement n'est pas accepté pour ce prêt, il faut définir la date du premier prélèvement";
	public static final String msgDejaPreleve = "Le prélèvement a déjà été effectué, durant cette date";
	public static final String msgDejaRembourse = "Le remboursement a déjà été effectué durant cette date";
	public static final String msgRemboursementNonTrouve = "Aucun remboursement trouvé pour cette date";
	public static final String msgDebutRemboursement = "Le remboursement n'est pas accepté pour ce prêt, il faut définir la date du premier prélèvement";
	public static final String msgFinRemboursement = "Le prêt a déjà été remboursé complètement";
	public static final String msgSommeRemboursee = "La somme est plus grande que la somme restante à rembourser";
	public static final String msgTotalementRembourse = "Le prêt est déjà remboursé au fournisseur";
	public static final String msgNonReportee = "Le prêt est n'est pas reporté";

	public RemboursementException() {
		super(msgParDefaut);
		setDefaultMessage(msgParDefaut);
	}

	public RemboursementException(String msg) {
		super(msg);
		setDefaultMessage(msgParDefaut);
	}
}
