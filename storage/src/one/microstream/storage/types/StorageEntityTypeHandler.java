package one.microstream.storage.types;

import one.microstream.collections.types.XGettingEnum;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryReferenceTraverser;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeDescriptionMember;

public interface StorageEntityTypeHandler extends PersistenceTypeDefinition
{
	public long simpleReferenceCount();

	public void iterateReferences(long entityCacheAddress, PersistenceObjectIdAcceptor acceptor);

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



	public final class Implementation implements StorageEntityTypeHandler
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods    //
		/////////////////////

		static long calculateMinimumEntityLength(final PersistenceTypeDefinition typeDescription)
		{
			long minimumEntityLength = Binary.entityHeaderLength();

			for(final PersistenceTypeDescriptionMember e : typeDescription.members())
			{
				// minimum length is assumed to never be max long
				minimumEntityLength += e.persistentMinimumLength();
			}

			return minimumEntityLength;
		}

		static long calculateMaximumEntityLength(final PersistenceTypeDefinition typeDescription)
		{
			long maximumEntityLength = Binary.entityHeaderLength();

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

		private final PersistenceTypeDefinition  typeDefinition      ;
		private final BinaryReferenceTraverser[] referenceTraversers ;
		private final int                        simpleReferenceCount;
		private final long                       simpleReferenceRange;
		private final long                       minimumEntityLength ;
		private final long                       maximumEntityLength ;
		private final boolean                    hasReferences       ;
		private final boolean                    isPrimitive         ;
		private final boolean                    hasVariableLength   ;
		private final boolean                    switchByteOrder     ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Implementation(
			final PersistenceTypeDefinition typeDefinition ,
			final boolean                   switchByteOrder
		)
		{
			super();

			final BinaryReferenceTraverser[] referenceTraversers =
				BinaryReferenceTraverser.Static.deriveReferenceTraversers(typeDefinition.members(), switchByteOrder)
			;

			this.typeDefinition       = typeDefinition;
			this.isPrimitive          = typeDefinition.isPrimitiveType();
			this.hasReferences        = typeDefinition.hasPersistedReferences();
			this.simpleReferenceCount = BinaryReferenceTraverser.Static.calculateSimpleReferenceCount(referenceTraversers);
			this.simpleReferenceRange = this.simpleReferenceCount * Binary.oidByteLength();
			this.referenceTraversers  = BinaryReferenceTraverser.Static.cropToReferences(referenceTraversers);
			this.minimumEntityLength  = calculateMinimumEntityLength(typeDefinition);
			this.maximumEntityLength  = calculateMaximumEntityLength(typeDefinition);
			this.hasVariableLength    = this.minimumEntityLength != this.maximumEntityLength;
			this.switchByteOrder      = switchByteOrder;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final long typeId()
		{
			return this.typeDefinition.typeId();
		}

		@Override
		public final String typeName()
		{
			return this.typeDefinition.typeName();
		}
		
		@Override
		public final Class<?> type()
		{
			return this.typeDefinition.type();
		}

		@Override
		public final XGettingEnum<? extends PersistenceTypeDefinitionMember> members()
		{
			return this.typeDefinition.members();
		}
		
		@Override
		public final long membersPersistedLengthMinimum()
		{
			return this.typeDefinition.membersPersistedLengthMinimum();
		}
		
		@Override
		public final long membersPersistedLengthMaximum()
		{
			return this.typeDefinition.membersPersistedLengthMaximum();
		}

		@Override
		public final void iterateReferences(
			final long                        entityCacheAddress,
			final PersistenceObjectIdAcceptor acceptor
		)
		{
//			DEBUGStorage.println(Thread.currentThread().getName() + " iterating type " + this.typeName());
//			System.out.flush();

			if(this.simpleReferenceRange != 0)
			{
				// this special casing spares a lot of traverser pointer chasing
				this.iterateSimpleReferences(entityCacheAddress, acceptor);
			}
			else
			{
				BinaryReferenceTraverser.iterateReferences(
					Binary.entityContentAddress(entityCacheAddress),
					this.referenceTraversers,
					acceptor
				);
			}
		}
		
		private void iterateSimpleReferences(
			final long                        entityCacheAddress,
			final PersistenceObjectIdAcceptor acceptor
		)
		{
			// JVM might probably jit out the never occuring case
			if(this.switchByteOrder)
			{
				BinaryReferenceTraverser.iterateReferenceRangeReversed(
					Binary.entityContentAddress(entityCacheAddress),
					this.simpleReferenceRange,
					acceptor
				);
			}
			else
			{
				BinaryReferenceTraverser.iterateReferenceRange(
					Binary.entityContentAddress(entityCacheAddress),
					this.simpleReferenceRange,
					acceptor
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

			// type id does not need to be validated here as the handler always got looked up via it beforehand.
			// object id can be an arbitrary value as far as the handler is concerned, no check here.
			// value validations on a business-logical level are no concern of the storage.
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

			// type id does not need to be validated here as the handler always got looked up via it beforehand.
			// object id can be an arbitrary value as far as the handler is concerned, no check here.
			// value validations on a business-logical level are no concern of the storage.
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
