package one.microstream.persistence.types;

/*-
 * #%L
 * microstream-persistence
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

import static one.microstream.X.notNull;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import one.microstream.X;
import one.microstream.collections.EqHashEnum;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XImmutableEnum;
import one.microstream.collections.types.XImmutableSequence;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeConsistency;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import one.microstream.reference.Swizzling;
import one.microstream.reflect.XReflect;

public interface PersistenceTypeHandler<D, T> extends PersistenceTypeDefinition, PersistenceDataTypeHolder<D>
{
	@Override
	public Class<D> dataType();
	
	@Override
	public Class<T> type();
	
	public default boolean isValidEntityType(final Class<? extends T> type)
	{
		/*
		 * Note that type() is validated to never be null prior to type handler instance creation.
		 * Must be super type check instead of simple identity check as some classes must be handleable
		 * as their super types (e.g. local implementation of java.nio.file.Path)
		 */
		return this.type().isAssignableFrom(type);
	}
	
	public default void validateEntityType(final Class<? extends T> type)
	{
		if(this.isValidEntityType(type))
		{
			return;
		}
		
		throw new PersistenceExceptionTypeConsistency(
			"Invalid entity type "+ type  +" for type handler " + this.toTypeIdentifier()
		);
	}
	
	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMember> allMembers();
	
	@Override
	public XGettingEnum<? extends PersistenceTypeDefinitionMember> instanceMembers();
		
	// implementing this method in a per-instance handler to be a no-op makes the instance effectively shallow
	public void iterateInstanceReferences(T instance, PersistenceFunction iterator);

	public void iterateLoadableReferences(D data, PersistenceReferenceLoader iterator);

	// implementing this method in a per-instance handler to be a no-op makes the instc effectively skipped for storing
	public void store(D data, T instance, long objectId, PersistenceStoreHandler<D> handler);

	public T create(D data, PersistenceLoadHandler handler);
	
	// implementing this method in a per-instance handler to be a no-op makes the instc effectively skipped for loading
	public default void initializeState(final D data, final T instance, final PersistenceLoadHandler handler)
	{
		// for non-value-types, initialize is the same as update. Value-types
		this.updateState(data, instance, handler);
	}

	// implementing this method in a per-instance handler to be a no-op makes the instc effectively skipped for loading
	public void updateState(D data, T instance, PersistenceLoadHandler handler);

	/**
	 * Completes an initially built instance after all loaded instances have been built.
	 * E.g. can be used to cause a hash collection to hash all its initially collected entries after their
	 * instances have been built.
	 *
	 * @param data the data target
	 * @param instance the source instance
	 * @param handler the appropriate handler
	 */
	public void complete(D data, T instance, PersistenceLoadHandler handler);

	/* (06.10.2012 TM)XXX: PersistenceDomainTypeHandler<D,T> ?
	 * to bind a generic TypeHandler to a specific registry inside a Domain
	 * specific registry could replace the oidResolver parameter.
	 * But only in an additional overloaded method.
	 * And what about the existing one that still gets called? What if it gets passed another oidresolver?
	 * Maybe solve by a PersistenceDomain-specific Builder? Wouldn't even have to have a new interface, just a class
	 */
	
	public PersistenceTypeHandler<D, T> initialize(long typeId);
	
	/**
	 * Iterates the types of persistent members (e.g. non-transient {@link Field}s).
	 * The same type may occur more than once.
	 * The order in which the types are provided is undefined, i.e. depending on the implementation.
	 * 
	 * @param <C> the logic type
	 * @param logic the iteration logic
	 * @return the given logic
	 */
	public <C extends Consumer<? super Class<?>>> C iterateMemberTypes(C logic);
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// default methods //
	////////////////////

	/*!*\
	 * All default methods must be implemented in
	 * PersistenceLegacyTypeHandler$Wrapper and AbstractBinaryLegacyTypeHandlerTranslating
	 * to prevent bugs!
	\*!*/
	
	public default XGettingEnum<? extends PersistenceTypeDefinitionMember> membersInDeclaredOrder()
	{
		// by default, there is no difference between members (in persisted order) and members in declared order.
		return this.allMembers();
	}
	
	public default XGettingEnum<? extends PersistenceTypeDescriptionMember> storingMembers()
	{
		// "storingMembers" is just an alias for instanceMembers since all instance members get stored.
		return this.instanceMembers();
	}

	public default XGettingEnum<? extends PersistenceTypeDescriptionMember> settingMembers()
	{
		// same as storingMembers except for java.lang.Enum (where name and ordinal may never be overwritten)
		return this.storingMembers();
	}
	
	/**
	 * Guarantees that the {@link PersistenceTypeHandler} implementation is actually viably usable to handle instances.
	 * That is the natural purpose of type handlers, but there are exceptions, like type handlers created for
	 * abstract types or unpersistable types just to have a metadata representation that links a type and a type id.
	 * <p>
	 * See occurances of {@link PersistenceExceptionTypeNotPersistable}.
	 * 
	 * @throws PersistenceExceptionTypeNotPersistable if the handler's type is not persistable
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
	
	// (27.08.2019 TM)TODO: "~Enum~" methods actually belong in a "PersistenceTypeHandlerEnum" subtype. Maybe refactor.
	
	public default Object[] collectEnumConstants()
	{
		throw new UnsupportedOperationException();
	}
	
	public default int getPersistedEnumOrdinal(final D data)
	{
		throw new UnsupportedOperationException();
	}

	/*!*\
	 * All default methods must be implemented in
	 * PersistenceLegacyTypeHandler$Wrapper and AbstractBinaryLegacyTypeHandlerTranslating
	 * to prevent bugs!
	\*!*/
	
	
	public static <T, M> T resolveEnumConstant(final Class<T> type, final int ordinal)
	{
		/*
		 * Required for AIC-like special subclass enums constants:
		 * The instance is actually of type T, but it is stored in a "? super T" array of its parent enum type.
		 */
		final Object enumConstantInstance = XReflect.resolveEnumConstantInstance(type, ordinal);
		
		// compensate the subclass typing hassle
		@SuppressWarnings("unchecked")
		final T enumConstantinstance = (T)enumConstantInstance;
		
		return enumConstantinstance;
	}
	
	
	public abstract class Abstract<D, T> implements PersistenceTypeHandler<D, T>
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////

		public static <D extends PersistenceTypeDefinitionMember> XImmutableEnum<D> validateAndImmure(
			final XGettingSequence<D> members
		)
		{
			if(members == null)
			{
				// members may be null to allow delayed on-demand BinaryField initialization.
				return null;
			}
			
			// note that this is descriptionMember-identity, meaning #identifier
			final EqHashEnum<D> validatedMembers = EqHashEnum.New(
				PersistenceTypeDescriptionMember.identityHashEqualator()
			);
			validatedMembers.addAll(members);
			if(validatedMembers.size() != members.size())
			{
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
		private long typeId = Swizzling.notFoundId();


		
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
		public synchronized PersistenceTypeHandler<D, T> initialize(final long typeId)
		{
			/* note:
			 * Type handlers can have hardcoded typeIds, e.g. for native types like primitive arrays.
			 * As long as the same typeId (originating from the dictionary file) is passed for initialization,
			 * everything is fine.
			 */
			if(Swizzling.isFoundId(this.typeId))
			{
				if(this.typeId == typeId)
				{
					// consistent no-op, abort
					return this;
				}
				
				throw new PersistenceException(
					"Specified type ID " + typeId + " conflicts with already initalized type ID " + this.typeId
				);
			}
			
			this.typeId = typeId;
			
			this.internalInitialize();
			
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
