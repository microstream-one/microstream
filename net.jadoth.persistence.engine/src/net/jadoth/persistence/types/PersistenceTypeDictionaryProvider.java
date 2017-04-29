package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

import net.jadoth.collections.types.XGettingSequence;


public interface PersistenceTypeDictionaryProvider
{
	public PersistenceTypeDictionary provideTypeDictionary();

	
	
	public static PersistenceTypeDictionaryProvider.Implementation New(
		final PersistenceTypeDictionaryLoader  loader ,
		final PersistenceTypeDictionaryParser  parser ,
		final PersistenceTypeDictionaryBuilder builder
	)
	{
		return new PersistenceTypeDictionaryProvider.Implementation(
			notNull(loader) ,
			notNull(parser) ,
			notNull(builder)
		);
	}

	public final class Implementation implements PersistenceTypeDictionaryProvider
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
		public PersistenceTypeDictionary provideTypeDictionary()
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
	
	
	
	public static PersistenceTypeDictionaryProvider.Caching Caching(
		final PersistenceTypeDictionaryProvider typeDictionaryProvider
	)
	{
		return new Caching(
			notNull(typeDictionaryProvider)
		);
	}
	
	public final class Caching implements PersistenceTypeDictionaryProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final     PersistenceTypeDictionaryProvider delegate        ;
		private transient PersistenceTypeDictionary         cachedDictionary;


		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Caching(final PersistenceTypeDictionaryProvider delegate)
		{
			super();
			this.delegate = delegate;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final PersistenceTypeDictionary provideTypeDictionary()
		{
			synchronized(this.delegate)
			{
				if(this.cachedDictionary == null)
				{
					this.cachedDictionary = this.delegate.provideTypeDictionary();
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
