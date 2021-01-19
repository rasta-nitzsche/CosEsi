package sample.noyau.entity;

public enum TypeRemboursement {
	PRELEVEMENT, APPORT_EXTERNE, REMBOURSEMENT_FOURNISSEUR;

	@Override
	public String toString() {
		switch (this) {
		case PRELEVEMENT:
			return "Prélèvement";
		case APPORT_EXTERNE:
			return "Apport externe";
		case REMBOURSEMENT_FOURNISSEUR:
			return "Remboursement au fournisseur";
		default:
			return name();
		}
	}
}