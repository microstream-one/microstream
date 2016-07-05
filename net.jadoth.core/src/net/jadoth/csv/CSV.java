package net.jadoth.csv;

import java.util.function.Consumer;

import net.jadoth.collections.XIterable;
import net.jadoth.util.chars.EscapeHandler;
import net.jadoth.util.chars.VarString;

public final class CSV
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	static final char             DEFAULT_LINE_SEPERATOR              = '\n';
	// the most reasonable control character for anyone who actually understands how it really works
	static final char             DEFAULT_SEPERATOR                   = '\t';
	static final char             DEFAULT_DELIMITER                   = '"' ;
	static final char             DEFAULT_ESCAPER                     = '\\';
	static final char             DEFAULT_SEGMENT_STARTER             = '{' ;
	static final char             DEFAULT_SEGMENT_TERMINATOR          = '}' ;
	static final char             DEFAULT_HEADER_STARTER              = '(' ;
	static final char             DEFAULT_HEADER_TERMINATOR           = ')' ;
	static final char             DEFAULT_COMMENT_SIGNAL              = '/' ;
	static final char             DEFAULT_COMMENT_SIMPLE_STARTER      = '/' ;
	static final char             DEFAULT_COMMENT_FULL_STARTER        = '*' ;
	static final String           DEFAULT_COMMENT_FULL_TERMINATOR     = "*/";
	static final char             DEFAULT_TERMINATOR                  = 0   ; // null character by default
	static final int              DEFAULT_SKIP_LINE_COUNT             = 0   ;
	static final int              DEFAULT_SKIP_LINE_COUNT_POST_HEADER = 0   ;
	static final int              DEFAULT_TRAILING_LINE_COUNT         = 0   ;
	static final EscapeHandler    DEFAULT_ESCAPE_HANDLER              = new EscapeHandler.Implementation();
	static final CsvConfiguration DEFAULT_CONFIG = new CsvConfiguration.Builder.Implementation().createConfiguration();



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static final CsvConfiguration configurationDefault()
	{
		return DEFAULT_CONFIG;
	}

	public static final CsvConfiguration.Builder configurationBuilder()
	{
		return new CsvConfiguration.Builder.Implementation();
	}

	public static final CsvAssembler.Builder<VarString> rowAssemblerBuilder()
	{
		return CsvAssembler.Builder.Implementation.New();
	}

	public static final <T> void assembleRow(
		final CsvAssembler           assembler   ,
		final CsvRowAssembler<T>     rowAssembler,
		final XIterable<? extends T> row
	)
	{
		row.iterate(new Consumer<T>()
		{
			@Override
			public void accept(final T entity)
			{
				rowAssembler.accept(entity, assembler);
			}
		});
		assembler.completeRow();
	}

	public static final <T> void assembleRows(
		final CsvAssembler           assembler   ,
		final CsvRowAssembler<T>     rowAssembler,
		final XIterable<? extends T> rows
	)
	{
		rows.iterate(new Consumer<T>()
		{
			@Override
			public void accept(final T entity)
			{
				rowAssembler.accept(entity, assembler);
				assembler.completeRow();
			}
		});
		assembler.completeRows();
	}



	private CSV()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}

