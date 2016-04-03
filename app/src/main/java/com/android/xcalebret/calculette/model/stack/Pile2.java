package com.android.xcalebret.calculette.model.stack;

import java.io.Serializable;
import java.util.Stack;

public class Pile2<T> implements PileI<T>, Serializable {

    private Stack<T> stk; // delegated to stack
    private int capacite; // stack capacity

    /**
     * Creation d'une pile.
     *
     * @param taille la taille de la pile, la taille doit etre > 0
     */
    public Pile2(int taille)
    {
        assert taille > 0 : "stack size must be > 0";
        this.stk = new Stack<T>();
        this.capacite = taille;
    }

    public Pile2()
    {
        this(PileI.CAPACITE_PAR_DEFAUT);
    }

    public void empiler(T o) throws PilePleineException
    {
        assert o != null : "null-object not allowed to be stacked";
        if (estPleine())
            throw new PilePleineException(o.toString());
        stk.push(o);
    }

    public T depiler() throws PileVideException
    {
        if (estVide() == true)
            throw new PileVideException();
        return stk.pop();
    }

    public T sommet() throws PileVideException
    {
        if (estVide())
            throw new PileVideException();
        return stk.peek();
    }

    /**
     * Effectue un test de l'�tat de la pile.
     *
     * @return vrai si la pile est vide, faux autrement
     */
    public boolean estVide()
    {
        return stk.empty();
    }

    /**
     * Effectue un test de l'�tat de la pile.
     *
     * @return vrai si la pile est pleine, faux autrement
     */
    public boolean estPleine()
    {
        return capacite == stk.size();
    }

    /**
     * Retourne une repr�sentation en String d'une pile, contenant la
     * repr�sentation en String de chaque �l�ment.
     *
     * @return une repr�sentation en String d'une pile
     */
    public String toString()
    {
        String s = "[";
        for (int i = stk.size() - 1; i >= 0; i--) {
            s = s + stk.elementAt(i);
            if (i > 0)
                s = s + ", ";
        }
        return s + "]";
    }

    /**
     * Retourne la taille de la pile
     *
     * @return cette taille
     */
    public int taille()
    {
        return this.stk.size();
    }

    /**
     * Retourne la taille de la pile
     *
     * @return cette taille
     */
    public int capacite()
    {
        return this.capacite;
    }
}
