package one.microstream.storage.types;

/*-
 * #%L
 * microstream-storage
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

import one.microstream.collections.types.XGettingEnum;
import one.microstream.math.XMath;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryReferenceTraverser;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.storage.exceptions.StorageExceptionInvalidEntityLength;

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



	public final class Default implements StorageEntityTypeHandler
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

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

		public Default(
			final PersistenceTypeDefinition typeDefinition ,
			final boolean                   switchByteOrder
		)
		{
			super();
			final BinaryReferenceTraverser[] referenceTraversers = deriveReferenceTraversers(
				typeDefinition.instanceMembers(),
				switchByteOrder
			);

			this.typeDefinition       = typeDefinition;
			this.isPrimitive          = typeDefinition.isPrimitiveType();
			this.hasReferences        = typeDefinition.hasPersistedReferences();
			this.simpleReferenceCount = BinaryReferenceTraverser.Static.calculateSimpleReferenceCount(referenceTraversers);
			this.simpleReferenceRange = this.simpleReferenceCount * Binary.objectIdByteLength();
			this.referenceTraversers  = BinaryReferenceTraverser.Static.cropToReferences(referenceTraversers);
			this.minimumEntityLength  = XMath.addCapped(Binary.entityHeaderLength(), typeDefinition.membersPersistedLengthMinimum());
			this.maximumEntityLength  = XMath.addCapped(Binary.entityHeaderLength(), typeDefinition.membersPersistedLengthMaximum());
			this.hasVariableLength    = this.minimumEntityLength != this.maximumEntityLength;
			this.switchByteOrder      = switchByteOrder;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		static final BinaryReferenceTraverser[] deriveReferenceTraversers(
			final XGettingEnum<? extends PersistenceTypeDefinitionMember> typeDefMembers,
			final boolean                                                 switchByteOrder
		)
		{
			return BinaryReferenceTraverser.Static.deriveReferenceTraversers(typeDefMembers, switchByteOrder);
		}

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
		public final XGettingEnum<? extends PersistenceTypeDefinitionMember> allMembers()
		{
			return this.typeDefinition.allMembers();
		}

		@Override
		public final XGettingEnum<? extends PersistenceTypeDefinitionMember> instanceMembers()
		{
			return this.typeDefinition.instanceMembers();
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

			if(this.simpleReferenceRange != 0)
			{
				// this special casing spares a lot of traverser pointer chasing
				this.iterateSimpleReferences(entityCacheAddress, acceptor);
			}
			else
			{
				BinaryReferenceTraverser.iterateReferences(
					Binary.toEntityContentOffset(entityCacheAddress),
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
					Binary.toEntityContentOffset(entityCacheAddress),
					this.simpleReferenceRange,
					acceptor
				);
			}
			else
			{
				BinaryReferenceTraverser.iterateReferenceRange(
					Binary.toEntityContentOffset(entityCacheAddress),
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
				throw new StorageExceptionInvalidEntityLength(
					"Invalid entity length for objectId " + objectId
					+ " of type " + this.toRuntimeTypeIdentifier()
					+ " : " + length + " < " + this.minimumEntityLength
				);
			}
			if(length > this.maximumEntityLength)
			{
				throw new StorageExceptionInvalidEntityLength(
					"Invalid entity length for objectId " + objectId
					+ " of type " + this.toRuntimeTypeIdentifier()
					+ " : " + length + " > " + this.maximumEntityLength
				);
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
