package net.jadoth.csv;

import static net.jadoth.Jadoth.notNull;
import net.jadoth.util.AbstractInstanceDispatcher;
import net.jadoth.util.Immutable;
import net.jadoth.util.chars.EscapeHandler;
import net.jadoth.util.chars.VarString;

public interface CsvConfiguration
{
	public char valueSeparator();

	public char recordSeparator();

	public char literalDelimiter();

	public char escaper();

	public char terminator();

	public char segmentStarter();

	public char segmentTerminator();

	public char headerStarter();

	public char headerTerminator();

	public char commentSignal();

	public char commentSimpleStarter();

	public char commentFullStarter();

	public String commentFullTerminator();

	public int skipLineCount();

	public int postColumnHeaderSkipLineCount();

	public int trailingLineCount();

	public EscapeHandler escapeHandler();

	public EscapeHandler valueEscapeHandler();

	// for assembling purposes
	public String valueSeparator(String prefix, String suffix);

	// for assembling purposes
	public String recordSeparator(String prefix, String suffix);

	public String buildControlCharactersDefinition(char separator);

	public boolean isControlCharacter(char c);

	public default char lineSeparator()
	{
		// (22.11.2014)TODO: make line separator configurable
		return '\n';
	}

	/**
	 * Returns a potentially cached char array of {@link #commentFullTerminator()} for read-only use.
	 * @return
	 */
	public default char[] commentFullTerminatorArray()
	{
		return this.commentFullTerminator().toCharArray();
	}



	public final class Implementation implements CsvConfiguration, Immutable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final char          lineSeparator          ;
		private final char          terminator             ;
		private final char          valueSeparator         ;
		private final char          literalDelimiter       ;
		private final char          valueEscaper           ;
		private final char          segmentStarter         ;
		private final char          segmentTerminator      ;
		private final char          headerStarter          ;
		private final char          headerTerminator       ;
		private final char          commentSignal          ;
		private final char          commentSimpleStarter   ;
		private final char          commentFullStarter     ;
		private final String        commentFullTerminator  ;
		private final int           skipLineCount          ;
		private final int           skipLineCountPostHeader;
		private final int           trailingLineCount      ;
		private final EscapeHandler contentEscapeHandler   ;
		private final EscapeHandler valueEscapeHandler     ;
//		private final boolean       parseHeader            ;

		private transient char[]    commentFullTerminatorArray;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(
			final char          lineSeparator          ,
			final char          terminator             ,
			final char          valueSeparator         ,
			final char          valueDelimiter         ,
			final char          valueEscaper           ,
			final char          segmentStarter         ,
			final char          segmentTerminator      ,
			final char          headerStarter          ,
			final char          headerTerminator       ,
			final char          commentSignal          ,
			final char          commentSimpleStarter   ,
			final char          commentFullStarter     ,
			final String        commentFullTerminator  ,
			final int           skipLineCount          ,
			final int           skipLineCountPostHeader,
			final int           trailingLineCount      ,
			final EscapeHandler contentEscapeHandler
		)
		{
			super();
			this.lineSeparator           = lineSeparator          ;
			this.terminator              = terminator             ;
			this.valueSeparator          = valueSeparator         ;
			this.literalDelimiter          = valueDelimiter         ;
			this.valueEscaper            = valueEscaper           ;
			this.segmentStarter          = segmentStarter         ;
			this.segmentTerminator       = segmentTerminator      ;
			this.headerStarter           = headerStarter          ;
			this.headerTerminator        = headerTerminator       ;
			this.commentSignal           = commentSignal          ;
			this.commentSimpleStarter    = commentSimpleStarter   ;
			this.commentFullStarter      = commentFullStarter     ;
			this.commentFullTerminator   = commentFullTerminator  ;
			this.skipLineCount           = skipLineCount          ;
			this.skipLineCountPostHeader = skipLineCountPostHeader;
			this.trailingLineCount       = trailingLineCount      ;
			this.contentEscapeHandler    = contentEscapeHandler   ;
			this.valueEscapeHandler      = new ValueEscapeHandler(contentEscapeHandler, valueDelimiter, valueEscaper);
//			this.parseHeader             = parseHeader            ;
		}


		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public final char[] commentFullTerminatorArray()
		{
			if(this.commentFullTerminatorArray == null)
			{
				this.commentFullTerminatorArray = this.commentFullTerminator().toCharArray();
			}
			return this.commentFullTerminatorArray;
		}

		@Override
		public final char recordSeparator()
		{
			return this.lineSeparator;
		}

		@Override
		public final char terminator()
		{
			return this.terminator;
		}

		@Override
		public final char segmentStarter()
		{
			return this.segmentStarter;
		}

		@Override
		public final char segmentTerminator()
		{
			return this.segmentTerminator;
		}

		@Override
		public final char headerStarter()
		{
			return this.headerStarter;
		}

		@Override
		public final char headerTerminator()
		{
			return this.headerTerminator;
		}

		@Override
		public final char commentSignal()
		{
			return this.commentSignal;
		}

		@Override
		public final char commentSimpleStarter()
		{
			return this.commentSimpleStarter;
		}

		@Override
		public final char commentFullStarter()
		{
			return this.commentFullStarter;
		}

		@Override
		public final String commentFullTerminator()
		{
			return this.commentFullTerminator;
		}

		@Override
		public final char valueSeparator()
		{
			return this.valueSeparator;
		}

		@Override
		public final char literalDelimiter()
		{
			return this.literalDelimiter;
		}

		@Override
		public final char escaper()
		{
			return this.valueEscaper;
		}

		@Override
		public final int skipLineCount()
		{
			return this.skipLineCount;
		}

		@Override
		public final int postColumnHeaderSkipLineCount()
		{
			return this.skipLineCountPostHeader;
		}

		@Override
		public final int trailingLineCount()
		{
			return this.trailingLineCount;
		}

		@Override
		public final EscapeHandler escapeHandler()
		{
			return this.contentEscapeHandler;
		}

		@Override
		public final EscapeHandler valueEscapeHandler()
		{
			return this.valueEscapeHandler;
		}

//		@Override
//		public final boolean parseHeader()
//		{
//			return this.parseHeader;
//		}

		@Override
		public final String valueSeparator(final String prefix, final String suffix)
		{
			return (prefix != null ? prefix : "") + this.valueSeparator() + (suffix != null ? suffix : "");
		}

		@Override
		public final String recordSeparator(final String prefix, final String suffix)
		{
			return (prefix != null ? prefix : "") + this.recordSeparator() + (suffix != null ? suffix : "");
		}

		@Override
		public final String buildControlCharactersDefinition(final char separator)
		{
			final VarString     vs            = VarString.New(32);
			final EscapeHandler escapeHandler = this.escapeHandler();

			// escaper itself and literal delimiter do not have to be escaped at definition site
			vs.add(separator).add(this.escaper());
			vs.add(separator).add(this.literalDelimiter());

			this.addControlCharacter(vs.add(separator), escapeHandler, this.valueSeparator());
			this.addControlCharacter(vs.add(separator), escapeHandler, this.recordSeparator());

			// choosing a character that has to be escaped for any of these would be pretty insane
			vs
			.add(separator).add(this.segmentStarter())
			.add(separator).add(this.segmentTerminator())
			.add(separator).add(this.headerStarter())
			.add(separator).add(this.headerTerminator())
			.add(separator).add(this.commentSignal())
			.add(separator).add(this.commentSimpleStarter())
			.add(separator).add(this.commentFullStarter())
			.add(separator).add(this.commentFullTerminator())
			;

			return vs.toString();
		}

		private void addControlCharacter(
			final VarString     vs              ,
			final EscapeHandler escapeHandler   ,
			final char          controlCharacter
		)
		{
			if(escapeHandler.needsEscaping(controlCharacter))
			{
				vs.add(this.escaper()).add(escapeHandler.transformEscapedChar(controlCharacter));
			}
			else
			{
				vs.add(controlCharacter);
			}
		}

		@Override
		public final boolean isControlCharacter(final char c)
		{
			return c == this.escaper()
				|| c == this.literalDelimiter()
				|| c == this.valueSeparator()
				|| c == this.recordSeparator()
				|| c == this.segmentStarter()
				|| c == this.segmentTerminator()
				|| c == this.headerStarter()
				|| c == this.headerTerminator()
				|| c == this.commentSignal()
				|| c == this.commentSimpleStarter()
				|| c == this.commentFullStarter()
			;
		}



		static final class ValueEscapeHandler implements EscapeHandler
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////

			private final EscapeHandler contentEscapeHandler;
			private final char          delimiter;
			private final char          escaper;



			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////

			public ValueEscapeHandler(
				final EscapeHandler contentEscapeHandler,
				final char          delimiter           ,
				final char          escaper
			)
			{
				super();
				this.contentEscapeHandler = contentEscapeHandler;
				this.delimiter            = delimiter           ;
				this.escaper              = escaper             ;
			}



			///////////////////////////////////////////////////////////////////////////
			// override methods //
			/////////////////////

			@Override
			public final void handleEscapedChar(final char escapedChar, final VarString literalBuilder)
			{
				// simply relay to content escape handler, no special meta symbol logic required
				this.contentEscapeHandler.handleEscapedChar(escapedChar, literalBuilder);
			}

			@Override
			public final boolean needsEscaping(final char chr)
			{
				// additional check for configuration-level meta symbol that must be escaped as well
				return chr == this.delimiter || chr == this.escaper || this.contentEscapeHandler.needsEscaping(chr);
			}

			@Override
			public final char transformEscapedChar(final char chr)
			{
				// meta chars at this level don't have to be transformed, only escaped. So just pass through
				return this.contentEscapeHandler.transformEscapedChar(chr);
			}

			@Override
			public final char unescape(final char chr)
			{
				return this.contentEscapeHandler.unescape(chr);
			}

		}

	}



	public interface Builder
	{
		public char getLineSeparator();

		public char getTerminator();

		public char getValueSeparator();

		public char getValueDelimiter();

		public char getValueEscaper();

		public char getSegmentStarter();

		public char getSegmentTerminator();

		public char getHeaderStarter();

		public char getHeaderTerminator();

		public char getCommentSignal();

		public char getCommentSimpleStarter();

		public char getCommentFullStarter();

		public String getCommentFullTerminator();

		public int getSkipLineCount();

		public int getSkipLineCountPostHeader();

		public int getTrailingLineCount();

		public EscapeHandler getEscapeHandler();



		public Builder setRecordSeparator(char lineSeparator);

		public Builder setTerminator(char terminator);

		public Builder setValueSeparator(char valueSeparator);

		public Builder setLiteralDelimiter(char valueDelimiter);

		public Builder setEscaper(char valueEscaper);

		public Builder setSegmentStarter(char segmentStarter);

		public Builder setSegmentTerminator(char segmentTerminator);

		public Builder setHeaderStarter(char headerStarter);

		public Builder setHeaderTerminator(char headerTerminator);

		public Builder setCommentSignal(char commentSignal);

		public Builder setCommentSimpleStarter(char commentSimpleStarter);

		public Builder setCommentFullStarter(char commentFullStarter);

		public Builder setCommentFullTerminator(String commentFullTerminator);

		public Builder setSkipLineCount(int skipLineCount);

		public Builder setPostColumnHeaderSkipLineCount(int skipLineCountPostHeader);

		public Builder setTrailingLineCount(int skipLineCountTrailing);

		public Builder setEscapeHandler(EscapeHandler escapeHandler);

		public Builder copyFrom(CsvConfiguration configuration);

		public CsvConfiguration createConfiguration();



		public final class Implementation extends AbstractInstanceDispatcher implements Builder
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////

			private char          lineSeparator           = CSV.DEFAULT_LINE_SEPERATOR             ;
			private char          terminator              = CSV.DEFAULT_TERMINATOR                 ;
			private char          valueSeparator          = CSV.DEFAULT_SEPERATOR                  ;
			private char          valueDelimiter          = CSV.DEFAULT_DELIMITER                  ;
			private char          valueEscaper            = CSV.DEFAULT_ESCAPER                    ;
			private char          segmentStarter          = CSV.DEFAULT_SEGMENT_STARTER            ;
			private char          segmentTerminator       = CSV.DEFAULT_SEGMENT_TERMINATOR         ;
			private char          headerStarter           = CSV.DEFAULT_HEADER_STARTER             ;
			private char          headerTerminator        = CSV.DEFAULT_HEADER_TERMINATOR          ;
			private char          commentSignal           = CSV.DEFAULT_COMMENT_SIGNAL             ;
			private char          commentSimpleStarter    = CSV.DEFAULT_COMMENT_SIMPLE_STARTER     ;
			private char          commentFullStarter      = CSV.DEFAULT_COMMENT_FULL_STARTER       ;
			private String        commentFullTerminator   = CSV.DEFAULT_COMMENT_FULL_TERMINATOR    ;
			private int           skipLineCount           = CSV.DEFAULT_SKIP_LINE_COUNT            ;
			private int           skipLineCountPostHeader = CSV.DEFAULT_SKIP_LINE_COUNT_POST_HEADER;
			private int           trailingLineCount       = CSV.DEFAULT_TRAILING_LINE_COUNT        ;
			private EscapeHandler escapeHandler           = CSV.DEFAULT_ESCAPE_HANDLER             ;



			///////////////////////////////////////////////////////////////////////////
			// declared methods //
			/////////////////////

			protected final void internalSetLineSeparator(final char lineSeparator)
			{
				this.lineSeparator = lineSeparator;
			}

			protected final void internalSetTerminator(final char terminator)
			{
				this.terminator = terminator;
			}

			protected final void internalSetValueSeparator(final char valueSeparator)
			{
				this.valueSeparator = valueSeparator;
			}

			protected final void internalSetValueDelimiter(final char valueDelimiter)
			{
				this.valueDelimiter = valueDelimiter;
			}

			protected final void internalSetValueEscaper(final char valueEscaper)
			{
				this.valueEscaper = valueEscaper;
			}

			protected final void internalSetSegmentStarter(final char segmentStarter)
			{
				this.segmentStarter = segmentStarter;
			}

			protected final void internalSetSegmentTerminator(final char segmentTerminator)
			{
				this.segmentTerminator = segmentTerminator;
			}

			protected final void internalSetHeaderStarter(final char headerStarter)
			{
				this.headerStarter = headerStarter;
			}

			protected final void internalSetHeaderTerminator(final char headerTerminator)
			{
				this.headerTerminator = headerTerminator;
			}

			protected final void internalSetCommentSignal(final char commentSignal)
			{
				this.commentSignal = commentSignal;
			}

			protected final void internalSetCommentSimpleStarter(final char commentSimpleStarter)
			{
				this.commentSimpleStarter = commentSimpleStarter;
			}

			protected final void internalSetCommentFullStarter(final char commentFullStarter)
			{
				this.commentFullStarter = commentFullStarter;
			}

			protected final void internalSetCommentFullTerminator(final String commentFullTerminator)
			{
				this.commentFullTerminator = notNull(commentFullTerminator);
			}

			protected final void internalSetSkipLineCount(final int skipLineCount)
			{
				this.skipLineCount = skipLineCount;
			}

			protected final void internalSetSkipLineCountPostHeader(final int skipLineCountPostHeader)
			{
				this.skipLineCountPostHeader = skipLineCountPostHeader;
			}

			protected final void internalSetTrailingLineCount(final int trailingLineCount)
			{
				this.trailingLineCount = trailingLineCount;
			}

			protected final void internalSetEscapeHandler(final EscapeHandler escapeHandler)
			{
				this.escapeHandler = escapeHandler;
			}



			///////////////////////////////////////////////////////////////////////////
			// getters          //
			/////////////////////

			@Override
			public char getLineSeparator()
			{
				return this.lineSeparator;
			}

			@Override
			public char getTerminator()
			{
				return this.terminator;
			}

			@Override
			public char getValueSeparator()
			{
				return this.valueSeparator;
			}

			@Override
			public char getValueDelimiter()
			{
				return this.valueDelimiter;
			}

			@Override
			public char getValueEscaper()
			{
				return this.valueEscaper;
			}

			@Override
			public char getSegmentStarter()
			{
				return this.segmentStarter;
			}

			@Override
			public char getSegmentTerminator()
			{
				return this.segmentTerminator;
			}

			@Override
			public char getHeaderStarter()
			{
				return this.headerStarter;
			}

			@Override
			public char getHeaderTerminator()
			{
				return this.headerTerminator;
			}

			@Override
			public char getCommentSignal()
			{
				return this.commentSignal;
			}

			@Override
			public char getCommentSimpleStarter()
			{
				return this.commentSimpleStarter;
			}

			@Override
			public char getCommentFullStarter()
			{
				return this.commentFullStarter;
			}

			@Override
			public String getCommentFullTerminator()
			{
				return this.commentFullTerminator;
			}

			@Override
			public int getSkipLineCount()
			{
				return this.skipLineCount;
			}

			@Override
			public int getSkipLineCountPostHeader()
			{
				return this.skipLineCountPostHeader;
			}

			@Override
			public int getTrailingLineCount()
			{
				return this.trailingLineCount;
			}

			@Override
			public EscapeHandler getEscapeHandler()
			{
				return this.escapeHandler;
			}



			///////////////////////////////////////////////////////////////////////////
			// setters          //
			/////////////////////

			@Override
			public Builder.Implementation setRecordSeparator(final char lineSeparator)
			{
				this.internalSetLineSeparator(lineSeparator);
				return this;
			}

			@Override
			public Builder.Implementation setTerminator(final char terminator)
			{
				this.internalSetTerminator(terminator);
				return this;
			}

			@Override
			public Builder.Implementation setValueSeparator(final char valueSeparator)
			{
				this.internalSetValueSeparator(valueSeparator);
				return this;
			}

			@Override
			public Builder.Implementation setLiteralDelimiter(final char valueDelimiter)
			{
				this.internalSetValueDelimiter(valueDelimiter);
				return this;
			}

			@Override
			public Builder.Implementation setEscaper(final char valueEscaper)
			{
				this.internalSetValueEscaper(valueEscaper);
				return this;
			}

			@Override
			public Builder.Implementation.Implementation setSegmentStarter(final char segmentStarter)
			{
				this.internalSetSegmentStarter(segmentStarter);
				return this;
			}

			@Override
			public Builder.Implementation.Implementation setSegmentTerminator(final char segmentTerminator)
			{
				this.internalSetSegmentTerminator(segmentTerminator);
				return this;
			}

			@Override
			public Builder.Implementation.Implementation setHeaderStarter(final char headerStarter)
			{
				this.internalSetHeaderStarter(headerStarter);
				return this;
			}

			@Override
			public Builder.Implementation.Implementation setHeaderTerminator(final char headerTerminator)
			{
				this.internalSetHeaderTerminator(headerTerminator);
				return this;
			}

			@Override
			public Builder.Implementation.Implementation setCommentSignal(final char commentSignal)
			{
				this.internalSetCommentSignal(commentSignal);
				return this;
			}

			@Override
			public Builder.Implementation.Implementation setCommentSimpleStarter(final char commentSimpleStarter)
			{
				this.internalSetCommentSimpleStarter(commentSimpleStarter);
				return this;
			}

			@Override
			public Builder.Implementation.Implementation setCommentFullStarter(final char commentFullStarter)
			{
				this.internalSetCommentFullStarter(commentFullStarter);
				return this;
			}

			@Override
			public Builder.Implementation.Implementation setCommentFullTerminator(final String commentFullTerminator)
			{
				this.internalSetCommentFullTerminator(commentFullTerminator);
				return this;
			}

			@Override
			public Builder.Implementation setSkipLineCount(final int skipLineCount)
			{
				this.internalSetSkipLineCount(skipLineCount);
				return this;
			}

			@Override
			public Builder.Implementation setPostColumnHeaderSkipLineCount(final int skipLineCountPostHeader)
			{
				this.internalSetSkipLineCountPostHeader(skipLineCountPostHeader);
				return this;
			}

			@Override
			public Builder setTrailingLineCount(final int skipLineCountTrailing)
			{
				this.internalSetTrailingLineCount(skipLineCountTrailing);
				return this;
			}

			@Override
			public Builder.Implementation setEscapeHandler(final EscapeHandler escapeHandler)
			{
				this.internalSetEscapeHandler(escapeHandler);
				return this;
			}

			@Override
			public Builder copyFrom(final CsvConfiguration configuration)
			{
				this
				.setRecordSeparator              (configuration.recordSeparator()              )
				.setTerminator                   (configuration.terminator()                   )
				.setValueSeparator               (configuration.valueSeparator()               )
				.setLiteralDelimiter             (configuration.literalDelimiter()             )
				.setEscaper                      (configuration.escaper()                      )
				.setSegmentStarter               (configuration.segmentStarter()               )
				.setSegmentTerminator            (configuration.segmentTerminator()            )
				.setHeaderStarter                (configuration.headerStarter()                )
				.setHeaderTerminator             (configuration.headerTerminator()             )
				.setCommentSignal                (configuration.commentSignal()                )
				.setCommentSimpleStarter         (configuration.commentSimpleStarter()         )
				.setCommentFullStarter           (configuration.commentFullStarter()           )
				.setCommentFullTerminator        (configuration.commentFullTerminator()        )
				.setSkipLineCount                (configuration.skipLineCount()                )
				.setPostColumnHeaderSkipLineCount(configuration.postColumnHeaderSkipLineCount())
				.setTrailingLineCount            (configuration.trailingLineCount()            )
				.setEscapeHandler                (configuration.escapeHandler()                )
				;
				return this;
			}

			@Override
			public CsvConfiguration createConfiguration()
			{
				/* (01.07.2013 TM)FIXME: CsvConfiguration: meta symbol consistency validation
				 * i.e. delimiter not same as separator, etc.
				 */
				return new CsvConfiguration.Implementation(
					this.getLineSeparator(),
					this.getTerminator(),
					this.getValueSeparator(),
					this.getValueDelimiter(),
					this.getValueEscaper(),
					this.getSegmentStarter(),
					this.getSegmentTerminator(),
					this.getHeaderStarter(),
					this.getHeaderTerminator(),
					this.getCommentSignal(),
					this.getCommentSimpleStarter(),
					this.getCommentFullStarter(),
					this.getCommentFullTerminator(),
					this.getSkipLineCount(),
					this.getSkipLineCountPostHeader(),
					this.getTrailingLineCount(),
					this.getEscapeHandler()
				);
			}

		}

	}

}
