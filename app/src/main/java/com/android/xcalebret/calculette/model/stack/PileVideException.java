package com.android.xcalebret.calculette.model.stack;

/**
 * Decrivez votre classe PilePleineException ici.
 *
 * @author (votre nom)
 * @version (un numero de version ou une date)
 */
public class PileVideException extends Exception {

    private final static String msg = "pile vide";

    public PileVideException()
    {
        super(msg);
    }

    public PileVideException(String message)
    {
        super(msg + message);
    }
}