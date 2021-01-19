package sample.noyau.entity;

public enum TypeCritere {
	// filtrer selon l'ID de l'employ� (params: String idEmploye)
	CRITERE1,
	// somme du pr�t entre sommeMin et sommeMax inclus (params: Pile sommes : int
	// sommeMin, int sommeMax)
	CRITERE2,
	// date de demande du pr�t entre dateMin et dateMax (params: Pile dates :
	// LocalDate dateMin, LocalDate dateMax)
	CRITERE3,
	// filtrer selon le num�ro du PV (params: int numeroPV)
	CRITERE4,
	// filtrer selon les pr�ts remboursables cl�tur�s (ajoute aussi les pr�ts non
	// remboursables) (params: void)
	CRITERE5,
	// filtrer selon le type de pr�t (nom de classe) (params: String nomClasse)
	CRITERE6,
	// filtrer selon le nom de l'employ� (params: String nomEmploye)
	CRITERE7,
	// Pret remboursable clotruers
	CRITERE8
}
