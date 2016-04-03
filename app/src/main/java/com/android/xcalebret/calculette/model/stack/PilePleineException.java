package com.android.xcalebret.calculette.model.stack;

/**
 * D�crivez votre classe PilePleineException ici.
 *
 * @author (votre nom)
 * @version (un num�ro de version ou une date)
 */
public class PilePleineException extends Exception {

    private final static String msg = "pile pleine : ";

    public PilePleineException()
    {
        super(msg);
    }

    public PilePleineException(String message)
    {
        super(msg + message);
    }
}