package sample.noyau.entity;

public enum Status {
	EN_COURS, REFUSE;

	@Override
	public String toString() {
		switch (this) {
		case EN_COURS:
			return "en cours";
		case REFUSE:
			return "refusé";
		default:
			return name();
		}
	}

	public static Status getStatus(String status) {
		switch (status) {
		case "en cours":
			return EN_COURS;
		case "refusé":
			return REFUSE;
		default:
			return null;
		}
	}
}
