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

import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.functional._charProcedure;

public interface XCsvAssembler
{
	public void addRowValueNull();

	public void addRowValueSimple(byte value);

	public void addRowValueSimple(boolean value);

	public void addRowValueSimple(short value);

	public void addRowValueSimple(char value);

	public void addRowValueSimple(int value);

	public void addRowValueSimple(float value);

	public void addRowValueSimple(long value);

	public void addRowValueSimple(double value);

	public void addRowValueSimple(CharSequence value);
	
	public void addRowValueSimple(Boolean value);

	public void addRowValueDelimited(CharSequence value);

	public void completeRow();

	public void completeRows();



	public final class Default implements XCsvAssembler
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////

		public static final XCsvAssembler.Default New(
			final XCsvConfiguration xcsvConfig           ,
			final VarString         vs                   ,
			final String            valueSeparatorPrefix ,
			final String            valueSeparatorSuffix ,
			final String            lineSeparatorPrefix,
			final String            lineSeparatorSuffix
		)
		{

			return new XCsvAssembler.Default(
				notNull(vs),
				xcsvConfig.literalDelimiter(),
				xcsvConfig.valueSeparator(valueSeparatorPrefix, valueSeparatorSuffix).toCharArray(),
				xcsvConfig.lineSeparator(lineSeparatorPrefix, lineSeparatorSuffix).toCharArray(),
				XCsvVarStringLiteralEscapingAssembler.New(xcsvConfig, vs)
			);
		}

		public static final XCsvAssembler.Default New(
			final XCsvConfiguration xcsvConfig,
			final VarString         vs
		)
		{
			return new XCsvAssembler.Default(
				notNull(vs),
				xcsvConfig.literalDelimiter(),
				new char[]{xcsvConfig.valueSeparator()},
				new char[]{xcsvConfig.lineSeparator()} ,
				XCsvVarStringLiteralEscapingAssembler.New(xcsvConfig, vs)
			);
		}

		public static final XCsvAssembler.Default New(final XCsvConfiguration xcsvConfig)
		{
			return New(xcsvConfig, VarString.New());
		}



		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final VarString      vs            ;
		final char           delimiter     ;
		final char[]         valueSeparator;
		final char[]         lineSeparator ;
		final _charProcedure assembler     ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final VarString      vs            ,
			final char           delimiter     ,
			final char[]         valueSeparator,
			final char[]         lineSeparator ,
			final _charProcedure assembler
		)
		{
			super();
			this.vs             = vs            ;
			this.valueSeparator = valueSeparator;
			this.delimiter      = delimiter     ;
			this.lineSeparator  = lineSeparator ;
			this.assembler      = assembler     ;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		private void separate()
		{
			this.vs.add(this.valueSeparator);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final void addRowValueNull()
		{
			this.separate();
		}

		@Override
		public final void addRowValueSimple(final byte value)
		{
			this.vs.add(value);
			this.separate();
		}

		@Override
		public final void addRowValueSimple(final boolean value)
		{
			this.vs.add(value);
			this.separate();
		}

		@Override
		public final void addRowValueSimple(final short value)
		{
			this.vs.add(value);
			this.separate();
		}

		@Override
		public final void addRowValueSimple(final char value)
		{
			this.vs.add(value);
			this.separate();
		}

		@Override
		public final void addRowValueSimple(final int value)
		{
			this.vs.add(value);
			this.separate();
		}

		@Override
		public final void addRowValueSimple(final float value)
		{
			this.vs.add(value);
			this.separate();
		}

		@Override
		public final void addRowValueSimple(final long value)
		{
			this.vs.add(value);
			this.separate();
		}

		@Override
		public final void addRowValueSimple(final double value)
		{
			this.vs.add(value);
			this.separate();
		}
		
		@Override
		public final void addRowValueSimple(final Boolean value)
		{
			if(value != null)
			{
				this.vs.add(value);
			}
			this.separate();
		}

		@Override
		public final void addRowValueSimple(final CharSequence value)
		{
			if(value != null)
			{
				this.vs.add(value);
			}
			this.separate();
		}

		@Override
		public final void addRowValueDelimited(final CharSequence value)
		{
			if(value != null)
			{
				this.vs.add(this.delimiter);
				XChars.iterate(value, this.assembler);
				this.vs.add(this.delimiter);
			}
			this.separate();
		}

		@Override
		public final void completeRow()
		{
			if(!this.vs.endsWith(this.valueSeparator))
			{
				return; // last record already completed
			}

			// safely delete trailing separator and add record separator
			this.vs.deleteLast(this.valueSeparator.length).add(this.lineSeparator);
		}


		@Override
		public final void completeRows()
		{
			if(!this.vs.endsWith(this.lineSeparator))
			{
				return;
			}
			// safely delete trailing record separator
			this.vs.deleteLast(this.lineSeparator.length);
		}

	}

	
	public static XCsvAssembler.Builder<VarString> Builder()
	{
		return XCsvAssembler.Builder.Default.New();
	}

	public interface Builder<O>
	{
		public XCsvConfiguration configuration();

		public String valueSeperatorPrefix();

		public String valueSeperatorSuffix();

		public String lineSeparatorPrefix();

		public String lineSeparatorSuffix();

		public Builder<O> setConfiguration(XCsvConfiguration configuration);

		public Builder<O> setValueSeperatorPrefix(String prefix);

		public Builder<O> setValueSeperatorSuffix(String suffix);

		public Builder<O> setLineSeparatorPrefix(String prefix);

		public Builder<O> setlLineSeparatorSuffix(String suffix);

		public XCsvAssembler buildRowAssembler(O outputMedium);



		public final class Default implements Builder<VarString>
		{
			///////////////////////////////////////////////////////////////////////////
			// static methods //
			///////////////////

			public static final Default New()
			{
				return new Default(XCSV.configurationDefault(), "", "", "", "");
			}



			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////

			private XCsvConfiguration configuration       ;
			private String            valueSeparatorPrefix;
			private String            valueSeparatorSuffix;
			private String            lineSeparatorPrefix ;
			private String            lineSeparatorSuffix ;



			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////

			Default(
				final XCsvConfiguration configuration       ,
				final String            valueSeparatorPrefix,
				final String            valueSeparatorSuffix,
				final String            lineSeparatorPrefix ,
				final String            lineSeparatorSuffix
			)
			{
				super();
				this.configuration        = notNull(configuration)       ;
				this.valueSeparatorPrefix = notNull(valueSeparatorPrefix);
				this.valueSeparatorSuffix = notNull(valueSeparatorSuffix);
				this.lineSeparatorPrefix  = notNull(lineSeparatorPrefix) ;
				this.lineSeparatorSuffix  = notNull(lineSeparatorSuffix) ;
			}



			///////////////////////////////////////////////////////////////////////////
			// getters //
			////////////

			@Override
			public final XCsvConfiguration configuration()
			{
				return this.configuration;
			}

			@Override
			public final String valueSeperatorPrefix()
			{
				return this.valueSeparatorPrefix;
			}

			@Override
			public final String valueSeperatorSuffix()
			{
				return this.valueSeparatorSuffix;
			}

			@Override
			public final String lineSeparatorPrefix()
			{
				return this.lineSeparatorPrefix;
			}

			@Override
			public final String lineSeparatorSuffix()
			{
				return this.lineSeparatorSuffix;
			}



			///////////////////////////////////////////////////////////////////////////
			// setters //
			////////////

			@Override
			public final Builder<VarString> setConfiguration(final XCsvConfiguration configuration)
			{
				this.configuration = notNull(configuration);
				return this;
			}

			@Override
			public final Builder<VarString> setValueSeperatorPrefix(final String separatorPrefix)
			{
				this.valueSeparatorPrefix = notNull(separatorPrefix);
				return this;
			}

			@Override
			public final Builder<VarString> setValueSeperatorSuffix(final String separatorSuffix)
			{
				this.valueSeparatorSuffix = notNull(separatorSuffix);
				return this;
			}


			@Override
			public final Builder<VarString> setLineSeparatorPrefix(final String separatorPrefix)
			{
				this.lineSeparatorPrefix = notNull(separatorPrefix);
				return this;
			}

			@Override
			public final Builder<VarString> setlLineSeparatorSuffix(final String separatorSuffix)
			{
				this.lineSeparatorSuffix = notNull(separatorSuffix);
				return this;
			}



			///////////////////////////////////////////////////////////////////////////
			// override methods //
			/////////////////////

			@Override
			public XCsvAssembler buildRowAssembler(final VarString vs)
			{
				return XCsvAssembler.Default.New(
					this.configuration,
					vs,
					this.valueSeparatorPrefix,
					this.valueSeparatorSuffix,
					this.lineSeparatorPrefix,
					this.lineSeparatorSuffix
				);
			}

		}

	}

}
