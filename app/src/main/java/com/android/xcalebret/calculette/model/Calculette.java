package com.android.xcalebret.calculette.model;

import com.android.xcalebret.calculette.model.stack.*;

import java.io.Serializable;
import java.util.Observable;

public class Calculette extends Observable implements CalculetteI, Serializable {

    private PileI<Integer> pile;

    public Calculette()
    {
        this.pile = new Pile2<Integer>(PileI.CAPACITE_PAR_DEFAUT);
    }

    public Calculette(PileI<Integer> pile)
    {
        this.pile = pile;
    }

    public void enter(int i) throws CalculetteException
    {
        try {
            pile.empiler(i);
            notifyObs(i);
        } catch (PilePleineException e) {
            throw new CalculetteException(e.getMessage());
        }
    }

    public int result() throws CalculetteException
    {
        try {
            return pile.sommet();
        } catch (PileVideException e) {
            throw new CalculetteException(e.getMessage());
        }
    }

    public int pop() throws CalculetteException
    {
        try {
            int poped = pile.depiler();
            notifyObs(null);
            return poped;
        } catch (PileVideException e) {
            throw new CalculetteException(e.getMessage());
        }
    }

    public void add() throws CalculetteException
    {
        try {
            isOperationAllowed(); // 2 integers required in stack to operate
            int op2 = pile.depiler();
            int op1 = pile.depiler();
            pile.empiler(op1 + op2);
            notifyObs(null);
        } catch (PilePleineException | PileVideException e) {
            throw new CalculetteException(e.getMessage());
        }
    }

    public void sub() throws CalculetteException
    {
        try {
            isOperationAllowed(); // 2 integers required in stack to operate
            int op2 = pile.depiler();
            int op1 = pile.depiler();
            pile.empiler(op1 - op2);
            notifyObs(null);
        } catch (PilePleineException | PileVideException e) {
            throw new CalculetteException(e.getMessage());
        }
    }

    public void div() throws CalculetteException
    {
        try {
            isOperationAllowed(); // 2 integers required in stack to operate
            if (pile.sommet() == 0) // peek only
                throw new CalculetteException("Division by zero !");
            int op2 = pile.depiler();
            int op1 = pile.depiler();
            pile.empiler(op1 / op2);
            notifyObs(null);
        } catch (PilePleineException | PileVideException e) {
            throw new CalculetteException(e.getMessage());
        }
    }

    public void mul() throws CalculetteException
    {
        try {
            isOperationAllowed(); // 2 integers required in stack to operate
            int op2 = pile.depiler();
            int op1 = pile.depiler();
            pile.empiler(op1 * op2);
            notifyObs(null);
        } catch (PilePleineException | PileVideException e) {
            throw new CalculetteException(e.getMessage());
        }
    }

    public void clear()
    {
        while (!pile.estVide()) {
            try {
                pile.depiler();
            } catch (PileVideException e) { /* void */ }
        }
        notifyObs(null);
    }

    public String toString()
    {
        return pile.toString();
    }

    public boolean isEmpty()
    {
        return pile.estVide();
    }

    public boolean isFull()
    {
        return pile.estPleine();
    }

    public int size()
    {
        return pile.taille();
    }

    public int capacity()
    {
        return pile.capacite();
    }

    private boolean isOperationAllowed() throws CalculetteException
    {
        if (pile.taille() < 2)
            throw new CalculetteException("Not enough parameters in stack");
        return true;
    }

    private void notifyObs(Object data)
    {
        setChanged();
        notifyObservers(data);
    }
}