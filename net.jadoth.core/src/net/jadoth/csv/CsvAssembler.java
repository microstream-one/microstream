package net.jadoth.csv;

import static net.jadoth.X.notNull;

import net.jadoth.functional._charProcedure;
import net.jadoth.util.chars.CsvVarStringLiteralEscapingAssembler;
import net.jadoth.util.chars.JadothChars;
import net.jadoth.util.chars.VarString;

public interface CsvAssembler
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



	public final class Implementation implements CsvAssembler
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////

		public static final CsvAssembler.Implementation New(
			final CsvConfiguration csvConfig            ,
			final VarString        vs                   ,
			final String           valueSeparatorPrefix ,
			final String           valueSeparatorSuffix ,
			final String           recordSeparatorPrefix,
			final String           recordSeparatorSuffix
		)
		{

			return new CsvAssembler.Implementation(
				notNull(vs),
				csvConfig.literalDelimiter(),
				csvConfig.valueSeparator(valueSeparatorPrefix, valueSeparatorSuffix).toCharArray(),
				csvConfig.recordSeparator(recordSeparatorPrefix, recordSeparatorSuffix).toCharArray(),
				CsvVarStringLiteralEscapingAssembler.New(csvConfig, vs)
			);
		}

		public static final CsvAssembler.Implementation New(final CsvConfiguration csvConfig, final VarString vs)
		{
			return new CsvAssembler.Implementation(
				notNull(vs),
				csvConfig.literalDelimiter(),
				new char[]{csvConfig.valueSeparator()},
				new char[]{csvConfig.recordSeparator()} ,
				CsvVarStringLiteralEscapingAssembler.New(csvConfig, vs)
			);
		}

		public static final CsvAssembler.Implementation New(final CsvConfiguration csvConfig)
		{
			return New(csvConfig, VarString.New());
		}



		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final VarString        vs             ;
		final char             delimiter      ;
		final char[]           valueSeparator ;
		final char[]           recordSeparator;
		final _charProcedure   assembler      ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(
			final VarString        vs             ,
			final char             delimiter      ,
			final char[]           valueSeparator ,
			final char[]           recordSeparator,
			final _charProcedure   assembler
		)
		{
			super();
			this.vs              = vs             ;
			this.valueSeparator  = valueSeparator ;
			this.delimiter       = delimiter      ;
			this.recordSeparator = recordSeparator;
			this.assembler       = assembler      ;
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
				JadothChars.iterate(value, this.assembler);
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
			this.vs.deleteLast(this.valueSeparator.length).add(this.recordSeparator);
		}


		@Override
		public final void completeRows()
		{
			if(!this.vs.endsWith(this.recordSeparator))
			{
				return;
			}
			// safely delete trailing record separator
			this.vs.deleteLast(this.recordSeparator.length);
		}

	}


	public interface Builder<O>
	{
		public CsvConfiguration configuration  ();

		public String valueSeperatorPrefix();

		public String valueSeperatorSuffix();

		public String recordSeperatorPrefix();

		public String recordSeperatorSuffix();

		public Builder<O> setConfiguration(CsvConfiguration configuration);

		public Builder<O> setValueSeperatorPrefix(String separatorPrefix);

		public Builder<O> setValueSeperatorSuffix(String separatorSuffix);

		public Builder<O> setRecordSeperatorPrefix(String separatorPrefix);

		public Builder<O> setRecordSeperatorSuffix(String separatorSuffix);

		public CsvAssembler buildRowAssembler(O outputMedium);



		public final class Implementation implements Builder<VarString>
		{
			///////////////////////////////////////////////////////////////////////////
			// static methods //
			///////////////////

			public static final Implementation New()
			{
				return new Implementation(CSV.configurationDefault(), "", "", "", "");
			}



			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////

			private CsvConfiguration configuration        ;
			private String           valueSeparatorPrefix ;
			private String           valueSeparatorSuffix ;
			private String           recordSeparatorPrefix;
			private String           recordSeparatorSuffix;



			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////

			Implementation(
				final CsvConfiguration configuration        ,
				final String           valueSeparatorPrefix ,
				final String           valueSeparatorSuffix ,
				final String           recordSeparatorPrefix,
				final String           recordSeparatorSuffix
			)
			{
				super();
				this.configuration         = notNull(configuration)        ;
				this.valueSeparatorPrefix  = notNull(valueSeparatorPrefix );
				this.valueSeparatorSuffix  = notNull(valueSeparatorSuffix );
				this.recordSeparatorPrefix = notNull(recordSeparatorPrefix);
				this.recordSeparatorSuffix = notNull(recordSeparatorSuffix);
			}



			///////////////////////////////////////////////////////////////////////////
			// getters          //
			/////////////////////

			@Override
			public final CsvConfiguration configuration()
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
			public final String recordSeperatorPrefix()
			{
				return this.recordSeparatorPrefix;
			}

			@Override
			public final String recordSeperatorSuffix()
			{
				return this.recordSeparatorSuffix;
			}



			///////////////////////////////////////////////////////////////////////////
			// setters          //
			/////////////////////

			@Override
			public final Builder<VarString> setConfiguration(final CsvConfiguration configuration)
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
			public final Builder<VarString> setRecordSeperatorPrefix(final String separatorPrefix)
			{
				this.recordSeparatorPrefix = notNull(separatorPrefix);
				return this;
			}

			@Override
			public final Builder<VarString> setRecordSeperatorSuffix(final String separatorSuffix)
			{
				this.recordSeparatorSuffix = notNull(separatorSuffix);
				return this;
			}



			///////////////////////////////////////////////////////////////////////////
			// override methods //
			/////////////////////

			@Override
			public CsvAssembler buildRowAssembler(final VarString vs)
			{
				return CsvAssembler.Implementation.New(
					this.configuration,
					vs,
					this.valueSeparatorPrefix,
					this.valueSeparatorSuffix,
					this.recordSeparatorPrefix,
					this.recordSeparatorSuffix
				);
			}

		}

	}

}
