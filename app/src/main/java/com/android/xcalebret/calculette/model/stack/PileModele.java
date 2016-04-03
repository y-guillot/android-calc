package com.android.xcalebret.calculette.model.stack;

import java.util.Observable;

public class PileModele<T> extends Observable implements PileI<T> {
    private PileI<T> pile;

    public PileModele(PileI<T> pile)
    {
        this.pile = pile;
    }

    public void empiler(T o) throws PilePleineException
    {
        try {
            pile.empiler(o);
            setChanged();
            notifyObservers(o);
        } catch (PilePleineException e) {
        }
    }

    public T depiler() throws PileVideException
    {
        T o = pile.depiler();
        setChanged();
        notifyObservers(o);
        return o;
    }


    public T sommet() throws PileVideException
    {
        return pile.sommet();
    }

    public int capacite()
    {
        return pile.capacite();
    }

    public int taille()
    {
        return pile.taille();
    }

    public boolean estVide()
    {
        return pile.estVide();
    }

    public boolean estPleine()
    {
        return pile.estPleine();
    }

    public String toString()
    {
        return pile.toString();
    }
}
