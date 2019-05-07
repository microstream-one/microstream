package one.microstream.util.csv;

import one.microstream.X;
import one.microstream.chars.StringTable;
import one.microstream.collections.LimitList;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XImmutableSequence;
import one.microstream.typing.KeyValue;
import one.microstream.typing.XTypes;


public interface CsvContent
{
	public String name();

	public XGettingSequence<? extends KeyValue<String, StringTable>> segments();

	public CsvConfiguration configuration();
	
	
	
	public interface Builder<M>
	{
		public CsvContent build(String name, M medium);
	}



	public final class Default implements CsvContent
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		//////////////////
		
		public static final Default New(
			final String                                                    name         ,
			final XGettingSequence<? extends KeyValue<String, StringTable>> segments     ,
			final CsvConfiguration                                          configuration
		)
		{
			return new Default(name, segments, configuration);
		}
		
		public static final Default NewTranslated(
			final String                                  name         ,
			final XGettingSequence<? extends StringTable> segments     ,
			final CsvConfiguration                        configuration
		)
		{
			final LimitList<KeyValue<String, StringTable>> translated = new LimitList<>(XTypes.to_int(segments.size()));
			for(final StringTable table : segments)
			{
				translated.add(X.KeyValue(table.name(), table));
			}
			return New(name, translated, configuration);
		}
		
		
		
		////////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final String                                                      name         ;
		final XImmutableSequence<? extends KeyValue<String, StringTable>> segments     ;
		final CsvConfiguration                                            configuration;



		////////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		private Default(
			final String                                                    name         ,
			final XGettingSequence<? extends KeyValue<String, StringTable>> segments     ,
			final CsvConfiguration                                          configuration
		)
		{
			super();
			this.name          = name             ;
			this.segments      = segments.immure();
			this.configuration = configuration    ;
		}



		////////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final String name()
		{
			return this.name;
		}

		@Override
		public final XGettingSequence<? extends KeyValue<String, StringTable>> segments()
		{
			return this.segments;
		}

		@Override
		public final CsvConfiguration configuration()
		{
			return this.configuration;
		}

	}

}
