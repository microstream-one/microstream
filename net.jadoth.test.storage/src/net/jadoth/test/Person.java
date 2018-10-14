package net.jadoth.test;

import java.util.Date;

public class Person
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	String  firstname, lastname, whatever;
	Date    doB   ;
	int     age   ;
	float   weight;
	double  height;
	long    ssid  ;
	boolean b1, b2;
	short   stuff ;
	char    sex   ;
	byte    bla   ;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public Person()
	{
		super();
	}
		
	public Person(
		final String  firstname,
		final String  lastname ,
		final String  whatever ,
		final Date    doB      ,
		final int     age      ,
		final float   weight   ,
		final double  height   ,
		final char    sex      ,
		final long    ssid     ,
		final boolean b1       ,
		final boolean b2       ,
		final short   stuff    ,
		final byte    bla
	)
	{
		super();
		this.firstname = firstname;
		this.lastname  = lastname ;
		this.whatever  = whatever ;
		this.doB       = doB      ;
		this.age       = age      ;
		this.weight    = weight   ;
		this.height    = height   ;
		this.sex       = sex      ;
		this.ssid      = ssid     ;
		this.b1        = b1       ;
		this.b2        = b2       ;
		this.stuff     = stuff    ;
		this.bla       = bla      ;
	}
	
}
