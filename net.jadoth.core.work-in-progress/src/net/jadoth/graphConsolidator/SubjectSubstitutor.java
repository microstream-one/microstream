package net.jadoth.graphConsolidator;

import net.jadoth.collections.EqHashEnum;
import net.jadoth.hash.HashEqualator;

public interface SubjectSubstitutor<E>
{
	public E substitute(E subject);



	public static <E> SubjectSubstitutor<E> New(final HashEqualator<? super E> equality)
	{
		return new Implementation<>(EqHashEnum.New(equality));
	}

	public final class Implementation<E> implements SubjectSubstitutor<E>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final EqHashEnum<E> uniqueSubjects;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(final EqHashEnum<E> uniqueSubjects)
		{
			super();
			this.uniqueSubjects = uniqueSubjects;
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public synchronized E substitute(final E subject)
		{
			return this.uniqueSubjects.replace(subject);
		}

	}

}



/* (09.04.2016)TODO: subject substitution


public boolean excludeEntityType(Class<?> entityType);

public EntityGraphTraverserFactory excludeEntityTypes(Class<?>... entityTypes);

public EntityGraphTraverserFactory excludeEntityTypes(Iterable<Class<?>> entityTypes);


public boolean registerSubjectType(Class<?> subjectType);

public EntityGraphTraverserFactory registerSubjectTypes(Class<?>... subjectTypes);

public EntityGraphTraverserFactory registerSubjectTypes(Iterable<Class<?>> subjectTypes);


public <E> boolean registerSubjectTypeEqualator(Class<E> subjectType, HashEqualator<? super E> equalator);

public EntityGraphTraverserFactory registerSubjectTypeEqualators(XGettingCollection<? extends KeyValue<Class<?>, ? extends HashEqualator<?>>> mapping);



	public static XGettingEnum<Class<?>> defaultSubjectTypes()
{
	return HashEnum.New(
		Byte.class      ,
		Boolean.class   ,
		Short.class     ,
		Character.class ,
		Integer.class   ,
		Float.class     ,
		Long.class      ,
		Double.class    ,
		String.class    ,
		BigInteger.class,
		BigDecimal.class
	);
}

public static XGettingEnum<Class<?>> defaultUnhandleableEntityTypes()
{
	return HashEnum.New(
		Thread.class
	);
}


public static SubjectEqualityProvider defaultSubjectEqualityProvider()
{
	return new SubjectEqualityProvider.Implementation();
}



protected synchronized XGettingMap<Class<?>, ? extends HashEqualator<?>> provideSubjectTypeHashEqualatorMapping()
{
	final SubjectEqualityProvider               subjectEqualityProvider = this.provideSubjectEqualityProvider();
	final HashTable<Class<?>, HashEqualator<?>> allHashEqualators       = HashTable.New(this.hashEqualators) ;

	for(final Class<?> subjectType : this.subjectTypes)
	{
		allHashEqualators.add(subjectType, subjectEqualityProvider.provideSubjectEquality(subjectType));
	}

	return allHashEqualators;
}





protected synchronized Predicate<? super Class<?>> provideHandleableEntityTypeSelector()
{
	if(this.handleableEntityTypeSelector != null)
	{
		return this.handleableEntityTypeSelector;
	}

	final HashEnum<Class<?>> excludedEntityTypes = HashEnum.New(this.excludedEntityTypes);

	if(excludedEntityTypes.isEmpty())
	{
		excludedEntityTypes.addAll(EntityGraphTraverser.defaultUnhandleableEntityTypes());
		excludedEntityTypes.addAll(EntityGraphTraverser.defaultSubjectTypes());
	}

	return new NonExcludedEntityTypePredicate(excludedEntityTypes);
}


	public final class NonExcludedEntityTypePredicate implements Predicate<Class<?>>
{
	final HashEnum<Class<?>> excludedEntityTypes;

	NonExcludedEntityTypePredicate(final HashEnum<Class<?>> excludedEntityTypes)
	{
		super();
		this.excludedEntityTypes = excludedEntityTypes;
	}
	@Override
	public boolean test(final Class<?> type)
	{
		return !this.excludedEntityTypes.contains(type);
	}

}



public interface SubjectEqualityProvider
{
public <S> HashEqualator<S> provideSubjectEquality(Class<S> subjectType);


public final class Implementation implements SubjectEqualityProvider
{
	@Override
	public <V> HashEqualator<V> provideSubjectEquality(final Class<V> subjectType)
	{
		return JadothHash.hashEqualityValue();
	}
}

}

*/