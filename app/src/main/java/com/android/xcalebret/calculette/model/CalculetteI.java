package com.android.xcalebret.calculette.model;


public interface CalculetteI {

	 // operations
	 void enter(int i) throws CalculetteException;
	 
	 void add() throws CalculetteException;
	 void sub() throws CalculetteException;
	 void div() throws CalculetteException;
	 void mul() throws CalculetteException;
	 
	 void clear();
	 int pop()  throws CalculetteException; 
	 
	 // interrogations
	 int result() throws CalculetteException;
	 boolean isEmpty();
	 boolean isFull();
	 int size();
	 int capacity();

}
