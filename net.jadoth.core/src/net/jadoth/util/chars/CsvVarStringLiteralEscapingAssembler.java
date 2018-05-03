package net.jadoth.util.chars;

import static net.jadoth.X.notNull;

import net.jadoth.csv.CsvConfiguration;
import net.jadoth.functional._charProcedure;

public class CsvVarStringLiteralEscapingAssembler implements _charProcedure
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static final CsvVarStringLiteralEscapingAssembler New(final CsvConfiguration csvConfig, final VarString vs)
	{
		return new CsvVarStringLiteralEscapingAssembler(
			notNull(vs)                            ,
			        csvConfig.valueSeparator()     ,
			        csvConfig.literalDelimiter()   ,
			        csvConfig.escaper()            ,
			notNull(csvConfig.valueEscapeHandler())
		);
	}

	public static final CsvVarStringLiteralEscapingAssembler New(final CsvConfiguration csvConfig)
	{
		return New(csvConfig, VarString.New());
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final VarString     vs           ;
	final char          separator    ;
	final char          delimiter    ;
	final char          escaper      ;
	final EscapeHandler escapeHandler;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	CsvVarStringLiteralEscapingAssembler(
		final VarString     vs           ,
		final char          separator    ,
		final char          delimiter    ,
		final char          escaper      ,
		final EscapeHandler escapeHandler
	)
	{
		super();
		this.vs            = vs           ;
		this.separator     = separator    ;
		this.delimiter     = delimiter    ;
		this.escaper       = escaper      ;
		this.escapeHandler = escapeHandler;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void accept(final char c)
	{
		if(this.escapeHandler.needsEscaping(c))
		{
			this.vs.add(this.escaper).add(this.escapeHandler.transformEscapedChar(c));
		}
		else
		{
			this.vs.add(c);
		}
	}

}
