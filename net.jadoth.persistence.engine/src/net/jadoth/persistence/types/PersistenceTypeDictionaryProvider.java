package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

import java.io.File;

import net.jadoth.persistence.internal.FilePersistenceTypeDictionary;


public interface PersistenceTypeDictionaryProvider
{
	public PersistenceTypeDictionary provideDictionary();


	public static PersistenceTypeDictionaryProvider NewFromFile(
		final File                              file                  ,
		final PersistenceFieldLengthResolver    lengthResolver        ,
		final PersistenceTypeDescriptionBuilder typeDescriptionBuilder
	)
	{
		return new Implementation(
			new PersistenceTypeDictionaryParser.Implementation(lengthResolver, typeDescriptionBuilder),
			new FilePersistenceTypeDictionary(file)
		);
	}

	public static PersistenceTypeDictionary provideDictionary(
		final File                              file                  ,
		final PersistenceFieldLengthResolver    lengthResolver        ,
		final PersistenceTypeDescriptionBuilder typeDescriptionBuilder
	)
	{
		final PersistenceTypeDictionaryProvider typeDictProvider =
			PersistenceTypeDictionaryProvider.NewFromFile(file, lengthResolver, typeDescriptionBuilder)
		;
		final PersistenceTypeDictionary ptd = typeDictProvider.provideDictionary();

		return ptd;
	}


	public final class Caching implements PersistenceTypeDictionaryProvider
	{
		private final     PersistenceTypeDictionaryProvider delegate        ;
		private transient PersistenceTypeDictionary         cachedDictionary;


		public Caching(final PersistenceTypeDictionaryProvider delegate)
		{
			super();
			this.delegate = delegate;
		}

		@Override
		public final PersistenceTypeDictionary provideDictionary()
		{
			synchronized(this.delegate)
			{
				if(this.cachedDictionary == null)
				{
					this.cachedDictionary = this.delegate.provideDictionary();
				}
				return this.cachedDictionary;
			}
		}


	}

	public final class Implementation implements PersistenceTypeDictionaryProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final PersistenceTypeDictionaryParser parser;
		private final PersistenceTypeDictionaryLoader loader;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(
			final PersistenceTypeDictionaryParser parser,
			final PersistenceTypeDictionaryLoader loader
		)
		{
			super();
			this.parser = notNull(parser);
			this.loader = notNull(loader);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public PersistenceTypeDictionary provideDictionary()
		{
			final String typeDictionaryString = this.loader.loadTypeDictionary();
			if(typeDictionaryString == null)
			{
				return PersistenceTypeDictionary.New();
			}
			return this.parser.parse(typeDictionaryString);
		}

	}

}
