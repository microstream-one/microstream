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

import one.microstream.chars.EscapeHandler;
import one.microstream.chars.VarString;
import one.microstream.typing.Immutable;
import one.microstream.util.InstanceDispatcher;

public interface XCsvConfiguration
{
	public char valueSeparator();

	public char lineSeparator();

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
	public String lineSeparator(String prefix, String suffix);

	public String buildControlCharactersDefinition(char separator);

	public boolean isControlCharacter(char c);

	/**
	 * @return a potentially cached char array of {@link #commentFullTerminator()} for read-only use.
	 */
	public default char[] commentFullTerminatorArray()
	{
		return this.commentFullTerminator().toCharArray();
	}
	
	public Boolean hasColumnNamesHeader();
	
	public Boolean hasColumnTypesHeader();
	
	public Boolean hasControlCharacterDefinitionHeader();

	
	
	public static XCsvConfiguration New()
	{
		return Builder().buildConfiguration();
	}
	
	public static XCsvConfiguration New(final char valueSeparator)
	{
		return Builder(valueSeparator).buildConfiguration();
	}


	public final class Default implements XCsvConfiguration, Immutable
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
		private final Boolean       hasColumnNamesHeader   ;
		private final Boolean       hasColumnTypesHeader   ;
		private final Boolean       hasMetaCharDefHeader   ;
		private final EscapeHandler contentEscapeHandler   ;
		private final EscapeHandler valueEscapeHandler     ;
		
		private transient char[] commentFullTerminatorArray;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final char          lineSeparator          ,
			final char          terminator             ,
			final char          valueSeparator         ,
			final char          literalDelimiter         ,
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
			final Boolean       hasColumnNamesHeader   ,
			final Boolean       hasColumnTypesHeader   ,
			final Boolean       hasMetaCharDefHeader   ,
			final EscapeHandler contentEscapeHandler
		)
		{
			super();
			this.lineSeparator           = lineSeparator          ;
			this.terminator              = terminator             ;
			this.valueSeparator          = valueSeparator         ;
			this.literalDelimiter        = literalDelimiter       ;
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
			this.hasColumnNamesHeader    = hasColumnNamesHeader   ;
			this.hasColumnTypesHeader    = hasColumnTypesHeader   ;
			this.hasMetaCharDefHeader    = hasMetaCharDefHeader   ;
			
			this.valueEscapeHandler      = new ValueEscapeHandler(contentEscapeHandler, literalDelimiter, valueEscaper);
		}


		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

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
		public final char lineSeparator()
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

		@Override
		public final Boolean hasColumnNamesHeader()
		{
			return this.hasColumnNamesHeader;
		}
		
		@Override
		public final Boolean hasColumnTypesHeader()
		{
			return this.hasColumnTypesHeader;
		}
		
		@Override
		public final Boolean hasControlCharacterDefinitionHeader()
		{
			return this.hasMetaCharDefHeader;
		}

		@Override
		public final String valueSeparator(final String prefix, final String suffix)
		{
			return (prefix != null ? prefix : "") + this.valueSeparator() + (suffix != null ? suffix : "");
		}

		@Override
		public final String lineSeparator(final String prefix, final String suffix)
		{
			return (prefix != null ? prefix : "") + this.lineSeparator() + (suffix != null ? suffix : "");
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
			this.addControlCharacter(vs.add(separator), escapeHandler, this.lineSeparator());

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
				|| c == this.lineSeparator()
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



	
	public static Builder Builder(final char valueSeparator)
	{
		return Builder().setValueSeparator(valueSeparator);
	}
	
	public static Builder Builder()
	{
		return new XCsvConfiguration.Builder.Default();
	}

	public interface Builder extends InstanceDispatcher
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
		
		public Boolean hasColumnNamesHeader();
		
		public Boolean hasColumnTypesHeader();
		
		public Boolean hasControlCharacterDefinitionHeader();

		public EscapeHandler getEscapeHandler();



		public Builder setLineSeparator(char lineSeparator);

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
		
		public Builder setHasColumnNamesHeader(Boolean hasColumnNamesHeader);
		
		public Builder setHasColumnTypesHeader(Boolean hasColumnTypesHeader);
		
		public Builder setHasControlCharacterDefinitionHeader(Boolean hasControlCharacterDefinitionHeader);

		public Builder setEscapeHandler(EscapeHandler escapeHandler);

		public Builder copyFrom(XCsvConfiguration configuration);

		public XCsvConfiguration buildConfiguration();



		public final class Default extends InstanceDispatcher.Default implements Builder
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////

			private char          lineSeparator           = XCSV.DEFAULT_LINE_SEPERATOR             ;
			private char          terminator              = XCSV.DEFAULT_TERMINATOR                 ;
			private char          valueSeparator          = XCSV.DEFAULT_SEPERATOR                  ;
			private char          valueDelimiter          = XCSV.DEFAULT_DELIMITER                  ;
			private char          valueEscaper            = XCSV.DEFAULT_ESCAPER                    ;
			private char          segmentStarter          = XCSV.DEFAULT_SEGMENT_STARTER            ;
			private char          segmentTerminator       = XCSV.DEFAULT_SEGMENT_TERMINATOR         ;
			private char          headerStarter           = XCSV.DEFAULT_HEADER_STARTER             ;
			private char          headerTerminator        = XCSV.DEFAULT_HEADER_TERMINATOR          ;
			private char          commentSignal           = XCSV.DEFAULT_COMMENT_SIGNAL             ;
			private char          commentSimpleStarter    = XCSV.DEFAULT_COMMENT_SIMPLE_STARTER     ;
			private char          commentFullStarter      = XCSV.DEFAULT_COMMENT_FULL_STARTER       ;
			private String        commentFullTerminator   = XCSV.DEFAULT_COMMENT_FULL_TERMINATOR    ;
			private int           skipLineCount           = XCSV.DEFAULT_SKIP_LINE_COUNT            ;
			private int           skipLineCountPostHeader = XCSV.DEFAULT_SKIP_LINE_COUNT_POST_HEADER;
			private int           trailingLineCount       = XCSV.DEFAULT_TRAILING_LINE_COUNT        ;
			private Boolean       hasColumnNamesHeader    = XCSV.DEFAULT_HAS_COLUMN_NAMES_HEADER    ;
			private Boolean       hasColumnTypesHeader    = XCSV.DEFAULT_HAS_COLUMN_TYPES_HEADER    ;
			private Boolean       hasCtrlCharDefHeader    = XCSV.DEFAULT_HAS_CTRLCHAR_DEF_HEADER    ;
			private EscapeHandler escapeHandler           = XCSV.DEFAULT_ESCAPE_HANDLER             ;
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			Default()
			{
				super();
			}

			
			
			///////////////////////////////////////////////////////////////////////////
			// getters //
			////////////

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
			public final Boolean hasColumnNamesHeader()
			{
				return this.hasColumnNamesHeader;
			}
			
			@Override
			public final Boolean hasColumnTypesHeader()
			{
				return this.hasColumnTypesHeader;
			}
			
			@Override
			public final Boolean hasControlCharacterDefinitionHeader()
			{
				return this.hasCtrlCharDefHeader;
			}

			@Override
			public EscapeHandler getEscapeHandler()
			{
				return this.escapeHandler;
			}



			///////////////////////////////////////////////////////////////////////////
			// setters //
			////////////

			@Override
			public Builder.Default setLineSeparator(final char lineSeparator)
			{
				this.lineSeparator = lineSeparator;
				return this;
			}

			@Override
			public Builder.Default setTerminator(final char terminator)
			{
				this.terminator = terminator;
				return this;
			}

			@Override
			public Builder.Default setValueSeparator(final char valueSeparator)
			{
				XCSV.validateValueSeparator(valueSeparator);
				this.valueSeparator = valueSeparator;
				return this;
			}

			@Override
			public Builder.Default setLiteralDelimiter(final char valueDelimiter)
			{
				this.valueDelimiter = valueDelimiter;
				return this;
			}

			@Override
			public Builder.Default setEscaper(final char valueEscaper)
			{
				this.valueEscaper = valueEscaper;
				return this;
			}

			@Override
			public Builder.Default setSegmentStarter(final char segmentStarter)
			{
				this.segmentStarter = segmentStarter;
				return this;
			}

			@Override
			public Builder.Default setSegmentTerminator(final char segmentTerminator)
			{
				this.segmentTerminator = segmentTerminator;
				return this;
			}

			@Override
			public Builder.Default setHeaderStarter(final char headerStarter)
			{
				this.headerStarter = headerStarter;
				return this;
			}

			@Override
			public Builder.Default setHeaderTerminator(final char headerTerminator)
			{
				this.headerTerminator = headerTerminator;
				return this;
			}

			@Override
			public Builder.Default setCommentSignal(final char commentSignal)
			{
				this.commentSignal = commentSignal;
				return this;
			}

			@Override
			public Builder.Default setCommentSimpleStarter(final char commentSimpleStarter)
			{
				this.commentSimpleStarter = commentSimpleStarter;
				return this;
			}

			@Override
			public Builder.Default setCommentFullStarter(final char commentFullStarter)
			{
				this.commentFullStarter = commentFullStarter;
				return this;
			}

			@Override
			public Builder.Default setCommentFullTerminator(final String commentFullTerminator)
			{
				this.commentFullTerminator = commentFullTerminator;
				return this;
			}

			@Override
			public Builder.Default setSkipLineCount(final int skipLineCount)
			{
				this.skipLineCount = skipLineCount;
				return this;
			}

			@Override
			public Builder.Default setPostColumnHeaderSkipLineCount(final int skipLineCountPostHeader)
			{
				this.skipLineCountPostHeader = skipLineCountPostHeader;
				return this;
			}

			@Override
			public Builder setTrailingLineCount(final int trailingLineCount)
			{
				this.trailingLineCount = trailingLineCount;
				return this;
			}
			
			@Override
			public Builder setHasColumnNamesHeader(final Boolean hasColumnNamesHeader)
			{
				this.hasColumnNamesHeader = hasColumnNamesHeader;
				return this;
			}
			
			@Override
			public Builder setHasColumnTypesHeader(final Boolean hasColumnTypesHeader)
			{
				this.hasColumnTypesHeader = hasColumnTypesHeader;
				return this;
			}
			
			@Override
			public Builder setHasControlCharacterDefinitionHeader(final Boolean hasControlCharacterDefinitionHeader)
			{
				this.hasCtrlCharDefHeader = hasControlCharacterDefinitionHeader;
				return this;
			}

			@Override
			public Builder.Default setEscapeHandler(final EscapeHandler escapeHandler)
			{
				this.escapeHandler = escapeHandler;
				return this;
			}

			@Override
			public Builder copyFrom(final XCsvConfiguration configuration)
			{
				this
				.setLineSeparator                (configuration.lineSeparator()                )
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
			public XCsvConfiguration buildConfiguration()
			{
				/* (01.07.2013 TM)FIXME: CsvConfiguration: meta symbol consistency validation
				 * i.e. delimiter not same as separator, etc.
				 */
				return new XCsvConfiguration.Default(
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
					this.hasColumnNamesHeader(),
					this.hasColumnTypesHeader(),
					this.hasControlCharacterDefinitionHeader(),
					this.getEscapeHandler()
				);
			}

		}

	}

}
