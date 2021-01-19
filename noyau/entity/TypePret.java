package sample.noyau.entity;

public enum TypePret {
	SOCIAL, ELECTROMENAGER, DON;

	@Override
	public String toString() {
		switch (this) {
		case ELECTROMENAGER:
			return "électroménager";
		default:
			return name().toLowerCase();
		}
	}

	public static TypePret getType(Pret pret) {
		switch (pret.getClass().getSimpleName()) {
		case "PretElectromenager":
			return ELECTROMENAGER;
		case "PretSocial":
			return SOCIAL;
		case "Don":
			return DON;
		default:
			return null;
		}
	}
}
