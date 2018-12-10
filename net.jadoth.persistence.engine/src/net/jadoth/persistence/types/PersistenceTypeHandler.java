package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import net.jadoth.X;
import net.jadoth.collections.EqHashEnum;
import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XImmutableEnum;
import net.jadoth.collections.types.XImmutableSequence;
import net.jadoth.functional._longProcedure;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeConsistency;

public interface PersistenceTypeHandler<M, T> extends PersistenceTypeDefinition
{
	@Override
	public Class<T> type();
	
	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMember> members();
	
	public default XGettingEnum<? extends PersistenceTypeDefinitionMember> membersInDeclaredOrder()
	{
		// by default, there is no difference between members (in persisted order) and members in declared order.
		return this.members();
	}
	
	public boolean hasInstanceReferences();
	
	// implementing this method in a per-instance handler to be a no-op makes the instance effectively shallow
	public void iterateInstanceReferences(T instance, PersistenceFunction iterator);

	public void iteratePersistedReferences(M medium, _longProcedure iterator);

	// implementing this method in a per-instance handler to be a no-op makes the instc effectively skipped for storing
	public void store(M medium, T instance, long objectId, PersistenceStoreHandler handler);

	public T    create(M medium);

	// implementing this method in a per-instance handler to be a no-op makes the instc effectively skipped for loading
	public void update(M medium, T instance, PersistenceLoadHandler builder);

	/**
	 * Completes an initially built instance after all loaded instances have been built.
	 * E.g. can be used to cause a hash collection to hash all its initially collected entries after their
	 * instances have been built.
	 *
	 * @param medium
	 * @param instance
	 * @param builder
	 */
	public void complete(M medium, T instance, PersistenceLoadHandler builder);

	/* (06.10.2012)XXX: PersistenceDomainTypeHandler<M,T> ?
	 * to bind a generic TypeHandler to a specific registry inside a Domain
	 * specific registry could replace the oidResolver parameter.
	 * But only in an additional overloaded method.
	 * And what about the existing one that still gets called? What if it gets passed another oidresolver?
	 * Maybe solve by a PersistenceDomain-specific Builder? Wouldn't even have to have a new interface, just a class
	 */
	
	public PersistenceTypeHandler<M, T> initializeTypeId(long typeId);
	
	/**
	 * Iterates the types of persistent members (e.g. non-transient {@link Field}s).
	 * The same type may occur more than once.
	 * The order in which the types are provided is undefined, i.e. depending on the implementation.
	 * 
	 * @param logic
	 * @return
	 */
	public <C extends Consumer<? super Class<?>>> C iterateMemberTypes(C logic);
	
	
	
	public abstract class AbstractImplementation<M, T> implements PersistenceTypeHandler<M, T>
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////

		public static <M extends PersistenceTypeDefinitionMember> XImmutableEnum<M> validateAndImmure(
			final XGettingSequence<M> members
		)
		{
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
		
		public static final PersistenceTypeDefinitionMemberField declaredField(
			final Field                          field         ,
			final PersistenceFieldLengthResolver lengthResolver
		)
		{
			return PersistenceTypeDefinitionMemberField.New(
				field                                              ,
				lengthResolver.resolveMinimumLengthFromField(field),
				lengthResolver.resolveMaximumLengthFromField(field)
			);
		}

		public static final XImmutableSequence<PersistenceTypeDescriptionMemberField> declaredFields(
			final PersistenceTypeDescriptionMemberField... declaredFields
		)
		{
			return X.ConstList(declaredFields);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		// basic type swizzling //
		private final Class<T> type;
		
		// effectively final / immutable: gets only initialized once later on and is never mutated again. initially 0.
		private long           typeId = Persistence.nullId();


		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected AbstractImplementation(final Class<T> type)
		{
			super();
			this.type = notNull(type);
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
			return this.type.getName();
		}
		
		@Override
		public synchronized PersistenceTypeHandler<M, T> initializeTypeId(final long typeId)
		{
			/* note:
			 * Type handlers can have hardcoded typeIds, e.g. for native types like primitive arrays.
			 * As long as the same typeId (originating from the dictionary file) is passed for initialization,
			 * everything is fine.
			 */
			if(this.typeId != 0)
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
			
			// by default, implementations are assumed to be (effectively) immutable and thus can return themselves.
			return this;
		}

	}

}
