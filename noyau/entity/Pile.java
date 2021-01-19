package sample.noyau.entity;

import sample.noyau.exception.ExceptionPileVide;

import java.util.*;

public class Pile {

	private Deque<Object> pile = new ArrayDeque<Object>();

	// empile x au sommet de la pile
	public void empiler(Object x) {
		pile.push(x);
	}

	// retourne l’élément qui est au sommet de la pile en le supprimant de la pile.
	// Elle lance une exception si la pile est vide
	public Object depiler() throws ExceptionPileVide {
		if (this.estVide())
			throw new ExceptionPileVide();
		return pile.pop();
	}

	// retourne true si la pile est vide et false dans le cas contraire
	public boolean estVide() {
		return pile.isEmpty();// ligne 19
	}
}