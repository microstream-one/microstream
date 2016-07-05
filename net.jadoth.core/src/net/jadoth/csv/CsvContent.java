package net.jadoth.csv;

import static net.jadoth.Jadoth.keyValue;
import net.jadoth.Jadoth;
import net.jadoth.collections.LimitList;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XImmutableSequence;
import net.jadoth.util.KeyValue;
import net.jadoth.util.chars.StringTable;

public interface CsvContent
{
	public String name();

	public XGettingSequence<? extends KeyValue<String, StringTable>> segments();

	public CsvConfiguration configuration();
	
	
	
	public interface Builder<M>
	{
		public CsvContent build(String name, M medium);
	}



	public final class Implementation implements CsvContent
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods    //
		/////////////////////
		
		public static final Implementation New(
			final String                                                    name         ,
			final XGettingSequence<? extends KeyValue<String, StringTable>> segments     ,
			final CsvConfiguration                                          configuration
		)
		{
			return new Implementation(name, segments, configuration);
		}
		
		public static final Implementation NewTranslated(
			final String                                  name         ,
			final XGettingSequence<? extends StringTable> segments     ,
			final CsvConfiguration                        configuration
		)
		{
			final LimitList<KeyValue<String, StringTable>> translated = new LimitList<>(Jadoth.to_int(segments.size()));
			for(final StringTable table : segments)
			{
				translated.add(keyValue(table.name(), table));
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

		private Implementation(
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
		// override methods //
		/////////////////////

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
