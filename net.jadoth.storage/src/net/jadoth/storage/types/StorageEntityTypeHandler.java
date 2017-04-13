package net.jadoth.storage.types;

import static net.jadoth.Jadoth.notNull;

import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.functional._longProcedure;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.binary.types.BinaryReferenceTraverser;
import net.jadoth.persistence.types.PersistenceTypeDescription;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMember;

public interface StorageEntityTypeHandler<T> extends PersistenceTypeDescription<T>
{
	public long simpleReferenceCount();

	public void iterateReferences(long entityCacheAddress, _longProcedure procedure);

	public void validateEntity(long length, long typeId, long objectId);

	public boolean isValidEntityGuaranteedType(long length, long objectId);

	public void validateEntityGuaranteedType(long length, long objectId);

	public long minimumLength();

	public long maximumLength();

	@Override
	public default boolean hasPersistedVariableLength()
	{
		return this.minimumLength() != this.maximumLength();
	}

	
	
	public static <T> StorageEntityTypeHandler.Implementation<T> New(
		final PersistenceTypeDescription<T> typeDescription
	)
	{
		return new StorageEntityTypeHandler.Implementation<>(
			notNull(typeDescription)
		);
	}


	public final class Implementation<T> implements StorageEntityTypeHandler<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods    //
		/////////////////////

		static long calculateMinimumEntityLength(final PersistenceTypeDescription<?> typeDescription)
		{
			long minimumEntityLength = BinaryPersistence.entityHeaderLength();

			for(final PersistenceTypeDescriptionMember e : typeDescription.members())
			{
				// minimum length is assumed to never be max long
				minimumEntityLength += e.persistentMinimumLength();
			}

			return minimumEntityLength;
		}

		static long calculateMaximumEntityLength(final PersistenceTypeDescription<?> typeDescription)
		{
			long maximumEntityLength = BinaryPersistence.entityHeaderLength();

			// checks for overflow due to max long (="unlimited") member max length, returns max long in such cases.
			for(final PersistenceTypeDescriptionMember e : typeDescription.members())
			{
				final long maxMemberLength = e.persistentMaximumLength();
				if(Long.MAX_VALUE - maximumEntityLength < maxMemberLength)
				{
					return Long.MAX_VALUE;
				}
				maximumEntityLength += maxMemberLength;
			}

			return maximumEntityLength;
		}



		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final PersistenceTypeDescription<T> typeDescription     ;
		private final BinaryReferenceTraverser[]    referenceTraversers ;
		private final int                           simpleReferenceCount;
		private final long                          simpleReferenceRange;
		private final long                          minimumEntityLength ;
		private final long                          maximumEntityLength ;
		private final boolean                       hasReferences       ;
		private final boolean                       isPrimitive         ;
		private final boolean                       hasVariableLength   ;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		Implementation(final PersistenceTypeDescription<T> typeDescription)
		{
			super();

			final BinaryReferenceTraverser[] referenceTraversers =
				BinaryReferenceTraverser.Static.deriveReferenceTraversers(typeDescription.members())
			;
			this.typeDescription      = typeDescription;
			this.isPrimitive          = typeDescription.isPrimitiveType();
			this.hasReferences        = typeDescription.hasPersistedReferences();
			this.simpleReferenceCount = BinaryReferenceTraverser.Static.calculateSimpleReferenceCount(referenceTraversers);
			this.simpleReferenceRange = this.simpleReferenceCount * BinaryPersistence.oidLength();
			this.referenceTraversers  = BinaryReferenceTraverser.Static.cropToReferences(referenceTraversers);
			this.minimumEntityLength  = calculateMinimumEntityLength(typeDescription);
			this.maximumEntityLength  = calculateMaximumEntityLength(typeDescription);
			this.hasVariableLength    = this.minimumEntityLength != this.maximumEntityLength;
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public final long typeId()
		{
			return this.typeDescription.typeId();
		}

		@Override
		public final String typeName()
		{
			return this.typeDescription.typeName();
		}
		
		@Override
		public final Class<T> type()
		{
			return this.typeDescription.type();
		}
		
		@Override
		public final boolean isObsolete()
		{
			return this.typeDescription.isObsolete();
		}

		@Override
		public final XGettingSequence<? extends PersistenceTypeDescriptionMember> members()
		{
			return this.typeDescription.members();
		}

		@Override
		public final void iterateReferences(final long entityCacheAddress, final _longProcedure procedure)
		{
//			DEBUGStorage.println(Thread.currentThread().getName() + " iterating type " + this.typeName());
//			System.out.flush();

			if(this.simpleReferenceRange != 0)
			{
				// handling here spares a lot of traverser pointer chasing
				BinaryReferenceTraverser.iterateReferenceRange(
					BinaryPersistence.entityDataAddress(entityCacheAddress),
					this.simpleReferenceRange,
					procedure
				);
			}
			else
			{
				BinaryReferenceTraverser.iterateReferences(
					BinaryPersistence.entityDataAddress(entityCacheAddress),
					this.referenceTraversers,
					procedure
				);
			}
		}

		@Override
		public final void validateEntity(final long length, final long typeId, final long objectId)
		{
			this.validateEntityGuaranteedType(length, objectId);
		}

		@Override
		public boolean isValidEntityGuaranteedType(final long length, final long objectId)
		{
			if(length < this.minimumEntityLength)
			{
				return false;
			}
			if(length > this.maximumEntityLength)
			{
				return false;
			}

			// type id does not need to be validated here as the handler always got looked up via it beforehand
			// object id can be an arbitrary value as far as the handler is concerned, no check here
			return true;
		}

		@Override
		public final void validateEntityGuaranteedType(final long length, final long objectId)
		{
			if(length < this.minimumEntityLength)
			{
				// (07.05.2014)EXCP: proper exception
				throw new RuntimeException("Invalid entity length: " + length + " < " + this.minimumEntityLength);
			}
			if(length > this.maximumEntityLength)
			{
				// (07.05.2014)EXCP: proper exception
				throw new RuntimeException("Invalid entity length: " + length + " > " + this.maximumEntityLength);
			}

			// type id does not need to be validated here as the handler always got looked up via it beforehand
			// object id can be an arbitrary value as far as the handler is concerned, no check here
		}

		@Override
		public final boolean hasPersistedReferences()
		{
//			DEBUGStorage.debugln(this.hasReferences + "\t" + this.typeName());
			return this.hasReferences;
		}

		@Override
		public final boolean isPrimitiveType()
		{
			return this.isPrimitive;
		}

		@Override
		public final boolean hasPersistedVariableLength()
		{
			return this.hasVariableLength;
		}
		
		@Override
		public final boolean hasVaryingPersistedLengthInstances()
		{
			return this.hasVariableLength;
		}

		@Override
		public final long simpleReferenceCount()
		{
			return this.simpleReferenceCount;
		}

		@Override
		public final long minimumLength()
		{
			return this.minimumEntityLength;
		}

		@Override
		public final long maximumLength()
		{
			return this.maximumEntityLength;
		}

		@Override
		public String toString()
		{
			return this.typeName();
		}

	}

}
