package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;

import java.lang.reflect.Field;

import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.functional._longProcedure;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeConsistency;
import net.jadoth.swizzling.types.PersistenceStoreFunction;
import net.jadoth.swizzling.types.Swizzle;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.SwizzleFunction;

public interface PersistenceTypeHandler<M, T> extends PersistenceTypeDefinition<T>
{
	public XGettingEnum<Field> getInstanceFields();

	public XGettingEnum<Field> getInstancePrimitiveFields();

	public XGettingEnum<Field> getInstanceReferenceFields();

	public boolean hasInstanceReferences();
	
	// implementing this method in a per-instance handler to be a no-op makes the instance effectively shallow
	public void iterateInstanceReferences(T instance, SwizzleFunction iterator);

	public void iteratePersistedReferences(M medium, _longProcedure iterator);

	// implementing this method in a per-instance handler to be a no-op makes the instc effectively skipped for storing
	public void store(M medium, T instance, long objectId, PersistenceStoreFunction linker);

	public T    create(M medium);

	// implementing this method in a per-instance handler to be a no-op makes the instc effectively skipped for loading
	public void update(M medium, T instance, SwizzleBuildLinker builder);

	/**
	 * Completes an initially built instance after all loaded instances have been built.
	 * E.g. can be used to cause a hash collection to hash all its initially collected entries after their
	 * instances have been built.
	 *
	 * @param medium
	 * @param instance
	 * @param builder
	 */
	public void complete(M medium, T instance, SwizzleBuildLinker builder);

	/* (09.12.2012)XXX: PersistenceTypeHandler#isEqualPersistentState(M medium, T instc, ObjectIdResolving oidResolver);
	 * additionally, with validation using it
	 */
//	public void validatePersistentState(M medium, T instance, SwizzleObjectIdResolving oidResolver);

	/* (06.10.2012)XXX: PersistenceDomainTypeHandler<M,T> ?
	 * to bind a generic TypeHandler to a specific registry inside a Domain
	 * specific registry could replace the oidResolver parameter.
	 * But only in an additional overloaded method.
	 * And what about the existing one that still gets called? What if it gets passed another oidresolver?
	 * Maybe solve by a PersistenceDomain-specific Builder? Wouldn't even have to have a new interface, just a class
	 */

	public void validateFields(XGettingSequence<Field> fieldDescriptions);
	
	public PersistenceTypeHandler<M, T> initializeTypeId(long typeId);
	
	
	
	public abstract class AbstractImplementation<M, T> implements PersistenceTypeHandler<M, T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		// basic type swizzling //
		private final Class<T> type;
		
		// effectively final / immutable: gets only initialized once later on and is never mutated again. initially 0.
		private long           typeId = Swizzle.nullId();


		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected AbstractImplementation(final Class<T> type)
		{
			super();
			this.type = notNull(type);
		}


		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		protected final void validateInstance(final T instance)
		{
			if(this.type.isInstance(instance))
			{
				return;
			}
			throw new PersistenceExceptionTypeConsistency();
		}

//		protected final void validateBasicTypeDefinition(final PersistenceTypeDescription typeDefinition)
//		{
//			if(this.tid == typeDefinition.typeId() && this.type.getName().equals(typeDefinition.typeName()))
//			{
//				return; // validation successful
//			}
//			throw new RuntimeException(); // (18.03.2013)EXCP: proper exception
//		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

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
				throw new RuntimeException(
					"Specified type ID " + typeId + " conflicts with already initalized type ID " + this.typeId
				);
			}
			
			this.typeId = typeId;
			
			// by default, implementations are assumed to be (effectively) immutable and thus can return themselves.
			return this;
		}

	}

}
