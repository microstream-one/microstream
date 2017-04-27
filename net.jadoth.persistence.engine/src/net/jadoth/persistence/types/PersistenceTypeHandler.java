package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;
import static net.jadoth.math.JadothMath.positive;

import java.lang.reflect.Field;

import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.functional._longProcedure;
import net.jadoth.memory.objectstate.ObjectStateHandler;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeConsistency;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.SwizzleFunction;
import net.jadoth.swizzling.types.SwizzleStoreLinker;

public interface PersistenceTypeHandler<M, T> extends PersistenceTypeDescription<T>, ObjectStateHandler<T>
{
	// implementing this method in a per-instance handler to be a no-op makes the instance effectively shallow
	public void iterateInstanceReferences(T instance, SwizzleFunction iterator);

	public void iteratePersistedReferences(M medium, _longProcedure iterator);

	// implementing this method in a per-instance handler to be a no-op makes the instc effectively skipped for storing
	public void store(M medium, T instance, long objectId, SwizzleStoreLinker linker);

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

//	public void staticUpdate(M medium, SwizzleBuilder builder);
//	public void staticStore (M medium, PersistenceStorer<M> persister);

	public void validateFields(XGettingSequence<Field> fieldDescriptions);

//	public void validateTypeDefinition(PersistenceTypeDescription typeDescription);

//	public PersistenceTypeDescription<T> typeDescription();

	public XGettingEnum<Field> getAllFields();

//	public XGettingEnum<Field> getStaticFinalFields();
//	public XGettingEnum<Field> getStaticFinalReferenceFields();
//	public XGettingEnum<Field> getStaticFinalPrimitiveFields();
//
//	public XGettingEnum<Field> getStaticMutableFields();
//	public XGettingEnum<Field> getStaticMutableReferenceFields();
//	public XGettingEnum<Field> getStaticMutablePrimitiveFields();
//
//	public XGettingEnum<Field> getStaticAllFields();
//	public XGettingEnum<Field> getStaticAllReferenceFields();
//	public XGettingEnum<Field> getStaticAllPrimitiveFields();



//	public static <T> PersistenceTypeDescription<T> getTypeDefinition(final PersistenceTypeHandler<?, T> input)
//	{
//		return input.typeDescription();
//	}

	public interface Creator<M, T>
	{
		public PersistenceTypeHandler<M, T> createTypeHandler(long typeId);
	}
	
	



	public abstract class AbstractImplementation<M, T>
	implements PersistenceTypeHandler<M, T>, PersistenceTypeDescription.Initializer<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		// basic type swizzling //
		private final Class<T> type;
		
		// these fields are effectively final / immutable: they get only initialized once and are never mutated again.
		
		private long                                               typeId   ;
		private XGettingTable<Long, PersistenceTypeDescription<T>> obsoletes;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected AbstractImplementation(final Class<T> type)
		{
			super();
			this.type = notNull(type);
		}

		protected AbstractImplementation(final Class<T> type, final long tid)
		{
			this(type);
			this.typeId  = positive(tid );
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
		// override methods //
		/////////////////////

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
		public final boolean isLatestPersisted()
		{
			/* (13.04.2017 TM)NOTE:
			 * explicitely defined type handlers at runtime are / must be always the latest persisted.
			 * Otherwise, the type handling infrastructure is inconsistent.
			 */
			return true;
		}
				
		@Override
		public final PersistenceTypeDescription<T> current()
		{
			return this;
		}
		
		@Override
		public final XGettingTable<Long, PersistenceTypeDescription<T>> obsoletes()
		{
			return this.obsoletes;
		}
		
		@Override
		public PersistenceTypeHandler<M, T> initialize(
			final long                                               typeId   ,
			final XGettingTable<Long, PersistenceTypeDescription<T>> obsoletes
		)
		{
			if(this.typeId != 0)
			{
				if(this.typeId != typeId)
				{
					// (26.04.2017 TM)EXCP: proper exception
					throw new RuntimeException(
						"Specified type ID " + typeId + " conflicts with already initalized type ID " + this.typeId
					);
				}
				// fall through
			}
			
			if(this.obsoletes != null)
			{
				if(this.obsoletes != obsoletes)
				{
					// (26.04.2017 TM)EXCP: proper exception
					throw new RuntimeException(
						"Obsolete " + PersistenceTypeDescription.class.getSimpleName()
						+ "s have already been initialized for type ID " + this.typeId
					);
				}
				// fall through
			}
			
			this.typeId    = typeId   ;
			this.obsoletes = obsoletes;
			
			return this;
		}
	
	}

}
