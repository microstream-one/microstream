package one.microstream.util.xcsv;

/*-
 * #%L
 * microstream-base
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

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
