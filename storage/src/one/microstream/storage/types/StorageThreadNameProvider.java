package one.microstream.storage.types;

import static one.microstream.X.notNull;

@FunctionalInterface
public interface StorageThreadNameProvider
{
	public String provideThreadName(StorageThreadProviding threadProvider, String definedThreadName);
	
	
	
	public static StorageThreadNameProvider NoOp()
	{
		return new NoOp();
	}
	
	public final class NoOp implements StorageThreadNameProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		 NoOp()
		{
			super();
		}
		 
		 
		 
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public String provideThreadName(
			final StorageThreadProviding threadProvider   ,
			final String                 definedThreadName
		)
		{
			return definedThreadName;
		}
		
	}
	
	
	public static StorageThreadNameProvider Prefixer(final String prefix)
	{
		return new Prefixer(
			notNull(prefix)
		);
	}
	
	public final class Prefixer implements StorageThreadNameProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final String prefix;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Prefixer(final String prefix)
		{
			super();
			this.prefix = prefix;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final String provideThreadName(
			final StorageThreadProviding threadProvider   ,
			final String                 definedThreadName
		)
		{
			return this.prefix + definedThreadName;
		}
		
	}
	
}
