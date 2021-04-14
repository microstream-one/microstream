package one.microstream.util.xcsv;

import static one.microstream.X.notNull;

import one.microstream.chars.EscapeHandler;
import one.microstream.chars.VarString;
import one.microstream.functional._charProcedure;

public class XCsvVarStringLiteralEscapingAssembler implements _charProcedure
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static final XCsvVarStringLiteralEscapingAssembler New(final XCsvConfiguration csvConfig, final VarString vs)
	{
		return new XCsvVarStringLiteralEscapingAssembler(
			notNull(vs)                            ,
			        csvConfig.valueSeparator()     ,
			        csvConfig.literalDelimiter()   ,
			        csvConfig.escaper()            ,
			notNull(csvConfig.valueEscapeHandler())
		);
	}

	public static final XCsvVarStringLiteralEscapingAssembler New(final XCsvConfiguration csvConfig)
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

	XCsvVarStringLiteralEscapingAssembler(
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
