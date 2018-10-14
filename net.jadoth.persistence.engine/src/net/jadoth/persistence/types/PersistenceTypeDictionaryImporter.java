package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

import net.jadoth.collections.types.XGettingSequence;


public interface PersistenceTypeDictionaryImporter
{
	public PersistenceTypeDictionary importTypeDictionary();

	
	
	public static PersistenceTypeDictionaryImporter.Implementation New(
		final PersistenceTypeDictionaryLoader  loader ,
		final PersistenceTypeDictionaryParser  parser ,
		final PersistenceTypeDictionaryBuilder builder
	)
	{
		return new PersistenceTypeDictionaryImporter.Implementation(
			notNull(loader) ,
			notNull(parser) ,
			notNull(builder)
		);
	}

	public final class Implementation implements PersistenceTypeDictionaryImporter
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final PersistenceTypeDictionaryLoader  loader ;
		private final PersistenceTypeDictionaryParser  parser ;
		private final PersistenceTypeDictionaryBuilder builder;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		Implementation(
			final PersistenceTypeDictionaryLoader  loader ,
			final PersistenceTypeDictionaryParser  parser ,
			final PersistenceTypeDictionaryBuilder builder
		)
		{
			super();
			this.loader  = loader ;
			this.parser  = parser ;
			this.builder = builder;
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public PersistenceTypeDictionary importTypeDictionary()
		{
			final String typeDictionaryString =
				this.loader.loadTypeDictionary()
			;
			final XGettingSequence<? extends PersistenceTypeDictionaryEntry> entries =
				this.parser.parseTypeDictionary(typeDictionaryString)
			;
			final PersistenceTypeDictionary typeDictionary =
				this.builder.buildTypeDictionary(entries)
			;
			
			return typeDictionary;
		}

	}
	
	
	
	public static PersistenceTypeDictionaryImporter.Caching Caching(
		final PersistenceTypeDictionaryImporter typeDictionaryImporter
	)
	{
		return new Caching(
			notNull(typeDictionaryImporter)
		);
	}
	
	public final class Caching implements PersistenceTypeDictionaryImporter
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final     PersistenceTypeDictionaryImporter delegate        ;
		private transient PersistenceTypeDictionary         cachedDictionary;


		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Caching(final PersistenceTypeDictionaryImporter delegate)
		{
			super();
			this.delegate = delegate;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final PersistenceTypeDictionary importTypeDictionary()
		{
			synchronized(this.delegate)
			{
				if(this.cachedDictionary == null)
				{
					this.cachedDictionary = this.delegate.importTypeDictionary();
				}
				return this.cachedDictionary;
			}
		}
		
		public final void clear()
		{
			synchronized(this.delegate)
			{
				this.cachedDictionary = null;
			}
		}
		
	}

}
