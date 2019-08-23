package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import one.microstream.X;
import one.microstream.collections.EqHashEnum;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XImmutableEnum;
import one.microstream.collections.types.XImmutableSequence;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeConsistency;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeNotPersistable;

public interface PersistenceTypeHandler<M, T> extends PersistenceTypeDefinition
{
	@Override
	public Class<T> type();
	
	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMember> allMembers();
	
	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMember> instanceMembers();
	
	public boolean hasInstanceReferences();
	
	// implementing this method in a per-instance handler to be a no-op makes the instance effectively shallow
	public void iterateInstanceReferences(T instance, PersistenceFunction iterator);

	public void iterateLoadableReferences(M medium, PersistenceObjectIdAcceptor iterator);

	// implementing this method in a per-instance handler to be a no-op makes the instc effectively skipped for storing
	public void store(M medium, T instance, long objectId, PersistenceStoreHandler handler);

	public T create(M medium, PersistenceObjectIdResolver idResolver);

	// implementing this method in a per-instance handler to be a no-op makes the instc effectively skipped for loading
	public void update(M medium, T instance, PersistenceObjectIdResolver idResolver);

	/**
	 * Completes an initially built instance after all loaded instances have been built.
	 * E.g. can be used to cause a hash collection to hash all its initially collected entries after their
	 * instances have been built.
	 *
	 * @param medium
	 * @param instance
	 * @param handler
	 */
	public void complete(M medium, T instance, PersistenceObjectIdResolver idResolver);

	/* (06.10.2012)XXX: PersistenceDomainTypeHandler<M,T> ?
	 * to bind a generic TypeHandler to a specific registry inside a Domain
	 * specific registry could replace the oidResolver parameter.
	 * But only in an additional overloaded method.
	 * And what about the existing one that still gets called? What if it gets passed another oidresolver?
	 * Maybe solve by a PersistenceDomain-specific Builder? Wouldn't even have to have a new interface, just a class
	 */
	
	public PersistenceTypeHandler<M, T> initialize(long typeId);
	
	/**
	 * Iterates the types of persistent members (e.g. non-transient {@link Field}s).
	 * The same type may occur more than once.
	 * The order in which the types are provided is undefined, i.e. depending on the implementation.
	 * 
	 * @param logic
	 */
	public <C extends Consumer<? super Class<?>>> C iterateMemberTypes(C logic);
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// default methods //
	////////////////////
	
	//!\\ all new default methods must be implemented in PersistenceLegacyTypeHandler$Wrapper to prevent bugs!
	
	public default XGettingEnum<? extends PersistenceTypeDefinitionMember> membersInDeclaredOrder()
	{
		// by default, there is no difference between members (in persisted order) and members in declared order.
		return this.allMembers();
	}
	
	/**
	 * Guarantees that the {@link PersistenceTypeHandler} implementation is actually viably usable to handle instances.
	 * That is the natural purpose of type handlers, but there are exceptions, like type handlers created for
	 * abstract types or unpersistable types just to have a metadata representation that links a type and a type id.
	 * <p>
	 * See occurances of {@link PersistenceExceptionTypeNotPersistable}.
	 * 
	 * @throws PersistenceExceptionTypeNotPersistable
	 * 
	 * @see PersistenceExceptionTypeNotPersistable
	 */
	public default void guaranteeSpecificInstanceViablity() throws PersistenceExceptionTypeNotPersistable
	{
		/*
		 * no-op by default, meaning the handler is viable to be used with instances
		 * not checking #isSpecificInstanceViable is intentional because this method gets called for every
		 * encountered instance and therefore should not execute any logic if no exception is thrown.
		 */
	}
	
	public default boolean isSpecificInstanceViable()
	{
		// true for virtually all handlers except a special-cased "unpersistable" and "abstract" handler.
		return true;
	}
	
	public default void guaranteeSubTypeInstanceViablity() throws PersistenceExceptionTypeNotPersistable
	{
		/*
		 * no-op by default, meaning the handler is viable to be used with instances
		 * not checking #isSubTypeInstanceViable is intentional because this method gets called for every
		 * encountered instance and therefore should not execute any logic if no exception is thrown.
		 */
	}
	
	public default boolean isSubTypeInstanceViable()
	{
		// true for virtually all handlers except a special-cased "unpersistable" handler.
		return true;
	}
	
	public default Object[] collectEnumConstants()
	{
		// (14.08.2019 TM)EXCP: proper exception
		throw new UnsupportedOperationException();
	}

	//!\\ all new default methods must be implemented in PersistenceLegacyTypeHandler$Wrapper to prevent bugs!
	
	
	public abstract class Abstract<M, T> implements PersistenceTypeHandler<M, T>
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////

		public static <M extends PersistenceTypeDefinitionMember> XImmutableEnum<M> validateAndImmure(
			final XGettingSequence<M> members
		)
		{
			// note that this is descriptionMember-identity, meaning #identifier
			final EqHashEnum<M> validatedMembers = EqHashEnum.New(
				PersistenceTypeDescriptionMember.identityHashEqualator()
			);
			validatedMembers.addAll(members);
			if(validatedMembers.size() != members.size())
			{
				// (07.09.2018 TM)EXCP: proper exception
				throw new PersistenceExceptionTypeConsistency("Duplicate member descriptions.");
			}
			
			return validatedMembers.immure();
		}
		
		public static final PersistenceTypeDefinitionMemberFieldReflective declaredField(
			final Field                          field         ,
			final PersistenceFieldLengthResolver lengthResolver
		)
		{
			return PersistenceTypeDefinitionMemberFieldReflective.New(
				field                                              ,
				lengthResolver.resolveMinimumLengthFromField(field),
				lengthResolver.resolveMaximumLengthFromField(field)
			);
		}

		public static final XImmutableSequence<PersistenceTypeDescriptionMemberFieldReflective> declaredFields(
			final PersistenceTypeDescriptionMemberFieldReflective... declaredFields
		)
		{
			return X.ConstList(declaredFields);
		}

		protected static final String deriveTypeName(final Class<?> type)
		{
			// to centralized logic accross child classes
			return Persistence.derivePersistentTypeName(type);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		// basic type swizzling //
		private final Class<T> type;
		
		// differs from Class#getName to properly identify synthetic classes instead using of those "$1,2,3..." names.
		private final String typeName;
		
		// effectively final / immutable: gets only initialized once later on and is never mutated again. initially 0.
		private long           typeId = Persistence.nullId();


		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Abstract(final Class<T> type)
		{
			this(type, type.getName());
		}

		protected Abstract(final Class<T> type, final String typeName)
		{
			super();
			this.type     = notNull(type)    ;
			this.typeName = notNull(typeName);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		protected final void validateInstance(final T instance)
		{
			if(this.type.isInstance(instance))
			{
				return;
			}
			throw new PersistenceExceptionTypeConsistency();
		}

		@Override
		public final Class<T> type()
		{
			return this.type;
		}

		@Override
		public final long typeId()
		{
			return this.typeId;
		}

		@Override
		public final String typeName()
		{
			return this.typeName;
		}
		
		protected void internalInitialize()
		{
			/*
			 * Empty by itself, this method is required to have a convenient entry point for executing
			 * logic in sub classes only once for initialization, but after the constructor chain has been completed.
			 * For example:
			 * Collecting BinaryField instances accross the class hiararchy and initializing their binary offsets
			 * afterwards. Trying to do that in the constructors directly would cause some fields to be null.
			 */
		}
		
		@Override
		public synchronized PersistenceTypeHandler<M, T> initialize(final long typeId)
		{
			/* note:
			 * Type handlers can have hardcoded typeIds, e.g. for native types like primitive arrays.
			 * As long as the same typeId (originating from the dictionary file) is passed for initialization,
			 * everything is fine.
			 */
			if(this.typeId != Persistence.nullId())
			{
				if(this.typeId == typeId)
				{
					// consistent no-op, abort
					return this;
				}
				
				// (26.04.2017 TM)EXCP: proper exception
				throw new IllegalArgumentException(
					"Specified type ID " + typeId + " conflicts with already initalized type ID " + this.typeId
				);
			}
			
			this.typeId = typeId;
			
			// (07.05.2019 TM)FIXME: priv#88: reactivate upon resume.
//			this.internalInitialize();
			
			// by default, implementations are assumed to be (effectively) immutable and thus can return themselves.
			return this;
		}
		
		@Override
		public final String toString()
		{
			return this.toRuntimeTypeIdentifier();
		}

	}

}
