package one.microstream.persistence.binary.types;

/*-
 * #%L
 * microstream-persistence-binary
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

import java.util.function.Consumer;

import one.microstream.collections.BulkList;
import one.microstream.collections.XArrays;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.exceptions.BinaryPersistenceException;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceTypeDescriptionMember;
import one.microstream.persistence.types.PersistenceTypeDescriptionMemberFieldGenericComplex;
import one.microstream.persistence.types.PersistenceTypeDescriptionMemberFieldGenericVariableLength;
import one.microstream.reflect.XReflect;


@FunctionalInterface
public interface BinaryReferenceTraverser
{
	public long apply(long address, PersistenceObjectIdAcceptor acceptor);

	/**
	 * This method reports the amount of bytes that a particular instance of an implementing type covers or advances.
	 * For example, an objectId is 8 bytes long. 5 objectIds are 40 bytes long.
	 * Skipping 6 bytes (primitives) is 6 bytes long.
	 * An implementation handling a variable length structure reports 0 bytes here.
	 * @return the amount of bytes that a particular instance of an implementing type covers or advances
	 */
	public default int coveredConstantByteCount()
	{
		return 0;
	}

	public default boolean hasReferences()
	{
		return false;
	}

	public default boolean isVariableLength()
	{
		return false;
	}



	public static long iterateReferences(
		final long                        address   ,
		final BinaryReferenceTraverser[]  traversers,
		final PersistenceObjectIdAcceptor acceptor
	)
	{
		long a = address;
		for(int i = 0; i < traversers.length; i++)
		{
			a = traversers[i].apply(a, acceptor);
		}
		return a;
	}

	public static void iterateReferenceRange(
		final long                        address       ,
		final long                        referenceRange,
		final PersistenceObjectIdAcceptor iterator
	)
	{
		final long addressBound = address + referenceRange;
		for(long a = address; a < addressBound; a += Static.REFERENCE_LENGTH)
		{
			iterator.acceptObjectId(XMemory.get_long(a));
		}
	}
	
	public static void iterateReferenceRangeReversed(
		final long                        address       ,
		final long                        referenceRange,
		final PersistenceObjectIdAcceptor iterator
	)
	{
		final long addressBound = address + referenceRange;
		for(long a = address; a < addressBound; a += Static.REFERENCE_LENGTH)
		{
			iterator.acceptObjectId(Long.reverseBytes(XMemory.get_long(a)));
		}
	}

	public static BinaryReferenceTraverser[] none()
	{
		return Static.EMPTY;
	}



	public static boolean hasReferences(final BinaryReferenceTraverser[] traversers)
	{
		for(final BinaryReferenceTraverser traverser : traversers)
		{
			if(traverser.hasReferences())
			{
				return true;
			}
		}
		return false;
	}

	public static final class Static
	{
		
		// to avoid suppressing CheckStyle warnings
		static final int
			C1 = 1,
			C2 = 2,
			C3 = 3,
			C4 = 4,
			C5 = 5,
			C6 = 6,
			C7 = 7,
			C8 = 8
		;

		static final int
			REFERENCE_LENGTH   = Binary.objectIdByteLength(),
			REFERENCE_LENGTH_2 = REFERENCE_LENGTH * C2,
			REFERENCE_LENGTH_3 = REFERENCE_LENGTH * C3,
			REFERENCE_LENGTH_4 = REFERENCE_LENGTH * C4,
			REFERENCE_LENGTH_5 = REFERENCE_LENGTH * C5,
			REFERENCE_LENGTH_6 = REFERENCE_LENGTH * C6,
			REFERENCE_LENGTH_7 = REFERENCE_LENGTH * C7,
			REFERENCE_LENGTH_8 = REFERENCE_LENGTH * C8
		;

		static final BinaryReferenceTraverser[] EMPTY = new BinaryReferenceTraverser[0];

		static final BinaryReferenceTraverser SKIP_1 = new BinaryReferenceTraverser()
		{
			@Override
			public final long apply(final long address, final PersistenceObjectIdAcceptor acceptor)
			{
				return address + C1;
			}
			
			@Override
			public int coveredConstantByteCount()
			{
				return C1;
			}
		};

		static final BinaryReferenceTraverser SKIP_2 = new BinaryReferenceTraverser()
		{
			@Override
			public final long apply(final long address, final PersistenceObjectIdAcceptor acceptor)
			{
				return address + C2;
			}
			
			@Override
			public int coveredConstantByteCount()
			{
				return C2;
			}
		};

		static final BinaryReferenceTraverser SKIP_3 = new BinaryReferenceTraverser()
		{
			@Override
			public final long apply(final long address, final PersistenceObjectIdAcceptor acceptor)
			{
				return address + C3;
			}
			
			@Override
			public int coveredConstantByteCount()
			{
				return C3;
			}
		};

		static final BinaryReferenceTraverser SKIP_4 = new BinaryReferenceTraverser()
		{
			@Override
			public final long apply(final long address, final PersistenceObjectIdAcceptor acceptor)
			{
				return address + C4;
			}
			
			@Override
			public int coveredConstantByteCount()
			{
				return C4;
			}
		};

		static final BinaryReferenceTraverser SKIP_5 = new BinaryReferenceTraverser()
		{
			@Override
			public final long apply(final long address, final PersistenceObjectIdAcceptor acceptor)
			{
				return address + C5;
			}
			
			@Override
			public int coveredConstantByteCount()
			{
				return C5;
			}
		};

		static final BinaryReferenceTraverser SKIP_6 = new BinaryReferenceTraverser()
		{
			@Override
			public final long apply(final long address, final PersistenceObjectIdAcceptor acceptor)
			{
				return address + C6;
			}
			
			@Override
			public int coveredConstantByteCount()
			{
				return C6;
			}
		};

		static final BinaryReferenceTraverser SKIP_7 = new BinaryReferenceTraverser()
		{
			@Override
			public final long apply(final long address, final PersistenceObjectIdAcceptor acceptor)
			{
				return address + C7;
			}
			
			@Override
			public int coveredConstantByteCount()
			{
				return C7;
			}
		};

		static final BinaryReferenceTraverser SKIP_8 = new BinaryReferenceTraverser()
		{
			@Override
			public final long apply(final long address, final PersistenceObjectIdAcceptor acceptor)
			{
				return address + C8;
			}
			
			@Override
			public int coveredConstantByteCount()
			{
				return C8;
			}
		};
		
		static final BinaryReferenceTraverser variableLengthSkipper(final boolean switchByteOrder)
		{
			return switchByteOrder
				? SKIP_VARIABLE_LENGTH_REVERSED
				: SKIP_VARIABLE_LENGTH
			;
		}

		static final BinaryReferenceTraverser SKIP_VARIABLE_LENGTH =
			new BinaryReferenceTraverser()
			{
				@Override
				public final long apply(final long address, final PersistenceObjectIdAcceptor acceptor)
				{
					// first 8 bytes of inlined data is its total binary length
					return address + XMemory.get_long(address);
				}

			}
		;
		
		static final BinaryReferenceTraverser SKIP_VARIABLE_LENGTH_REVERSED =
			new BinaryReferenceTraverser()
			{
				@Override
				public final long apply(final long address, final PersistenceObjectIdAcceptor acceptor)
				{
					// first 8 bytes of inlined data is its total binary length
					return address + Long.reverseBytes(XMemory.get_long(address));
				}

			}
		;

		static final BinaryReferenceTraverser REFERENCE_1 = new BinaryReferenceTraverser()
		{
			@Override
			public final long apply(final long address, final PersistenceObjectIdAcceptor acceptor)
			{
				acceptor.acceptObjectId(XMemory.get_long(address));
				return address + REFERENCE_LENGTH;
			}
			
			@Override
			public int coveredConstantByteCount()
			{
				return REFERENCE_LENGTH;
			}
			
			@Override
			public boolean hasReferences()
			{
				return true;
			}
		};

		static final BinaryReferenceTraverser REFERENCE_2 = new BinaryReferenceTraverser()
		{
			@Override
			public final long apply(final long address, final PersistenceObjectIdAcceptor acceptor)
			{
				acceptor.acceptObjectId(XMemory.get_long(address));
				acceptor.acceptObjectId(XMemory.get_long(address + REFERENCE_LENGTH));
				return address + REFERENCE_LENGTH_2;
			}
			
			@Override
			public int coveredConstantByteCount()
			{
				return REFERENCE_LENGTH_2;
			}
			
			@Override
			public boolean hasReferences()
			{
				return true;
			}
		};

		static final BinaryReferenceTraverser REFERENCE_3 = new BinaryReferenceTraverser()
		{
			@Override
			public final long apply(final long address, final PersistenceObjectIdAcceptor acceptor)
			{
				acceptor.acceptObjectId(XMemory.get_long(address));
				acceptor.acceptObjectId(XMemory.get_long(address + REFERENCE_LENGTH));
				acceptor.acceptObjectId(XMemory.get_long(address + REFERENCE_LENGTH_2));
				return address + REFERENCE_LENGTH_3;
			}
			
			@Override
			public int coveredConstantByteCount()
			{
				return REFERENCE_LENGTH_3;
			}
			
			@Override
			public boolean hasReferences()
			{
				return true;
			}
		};

		static final BinaryReferenceTraverser REFERENCE_4 = new BinaryReferenceTraverser()
		{
			@Override
			public final long apply(final long address, final PersistenceObjectIdAcceptor acceptor)
			{
				final long bound = address + REFERENCE_LENGTH_4;
				for(long a = address; a < bound; a += REFERENCE_LENGTH)
				{
					acceptor.acceptObjectId(XMemory.get_long(a));
				}
				return bound;
			}
			
			@Override
			public int coveredConstantByteCount()
			{
				return REFERENCE_LENGTH_4;
			}
			
			@Override
			public boolean hasReferences()
			{
				return true;
			}
		};

		static final BinaryReferenceTraverser REFERENCE_5 = new BinaryReferenceTraverser()
		{
			@Override
			public final long apply(final long address, final PersistenceObjectIdAcceptor acceptor)
			{
				final long bound = address + REFERENCE_LENGTH_5;
				for(long a = address; a < bound; a += REFERENCE_LENGTH)
				{
					acceptor.acceptObjectId(XMemory.get_long(a));
				}
				return bound;
			}
			
			@Override
			public int coveredConstantByteCount()
			{
				return REFERENCE_LENGTH_5;
			}
			
			@Override
			public boolean hasReferences()
			{
				return true;
			}
		};

		static final BinaryReferenceTraverser REFERENCE_6 = new BinaryReferenceTraverser()
		{
			@Override
			public final long apply(final long address, final PersistenceObjectIdAcceptor acceptor)
			{
				final long bound = address + REFERENCE_LENGTH_6;
				for(long a = address; a < bound; a += REFERENCE_LENGTH)
				{
					acceptor.acceptObjectId(XMemory.get_long(a));
				}
				return bound;
			}
			
			@Override
			public int coveredConstantByteCount()
			{
				return REFERENCE_LENGTH_6;
			}
			
			@Override
			public boolean hasReferences()
			{
				return true;
			}
		};

		static final BinaryReferenceTraverser REFERENCE_7 = new BinaryReferenceTraverser()
		{
			@Override
			public final long apply(final long address, final PersistenceObjectIdAcceptor acceptor)
			{
				final long bound = address + REFERENCE_LENGTH_7;
				for(long a = address; a < bound; a += REFERENCE_LENGTH)
				{
					acceptor.acceptObjectId(XMemory.get_long(a));
				}
				return bound;
			}
			
			@Override
			public int coveredConstantByteCount()
			{
				return REFERENCE_LENGTH_7;
			}
			
			@Override
			public boolean hasReferences()
			{
				return true;
			}
		};

		static final BinaryReferenceTraverser REFERENCE_8 = new BinaryReferenceTraverser()
		{
			@Override
			public final long apply(final long address, final PersistenceObjectIdAcceptor acceptor)
			{
				final long bound = address + REFERENCE_LENGTH_8;
				for(long a = address; a < bound; a += REFERENCE_LENGTH)
				{
					acceptor.acceptObjectId(XMemory.get_long(a));
				}
				return bound;
			}
			
			@Override
			public int coveredConstantByteCount()
			{
				return REFERENCE_LENGTH_8;
			}
			
			@Override
			public boolean hasReferences()
			{
				return true;
			}
		};

		static final BinaryReferenceTraverser REFERENCE_VARIABLE_LENGTH_START_BOUND_BASED = new BinaryReferenceTraverser()
		{
			@Override
			public final long apply(final long address, final PersistenceObjectIdAcceptor acceptor)
			{
				// using length instead of element count is crucial for consolidated multi-reference iteration
				final long bound = address + XMemory.get_long(Binary.toBinaryListByteLengthOffset(address));
				for(long a = Binary.toBinaryListElementsOffset(address); a < bound; a += REFERENCE_LENGTH)
				{
					// see REFERENCE_VARIABLE_LENGTH_START_BOUND_BASED_REVERSED for ByteOrder switching
					acceptor.acceptObjectId(XMemory.get_long(a));
				}
				
				return bound;
			}

			@Override
			public boolean isVariableLength()
			{
				return true;
			}

			@Override
			public boolean hasReferences()
			{
				return true;
			}

		};
		
		static final BinaryReferenceTraverser REFERENCE_1_REVERSED = new BinaryReferenceTraverser()
		{
			@Override
			public final long apply(final long address, final PersistenceObjectIdAcceptor acceptor)
			{
				acceptor.acceptObjectId(Long.reverseBytes(XMemory.get_long(address)));
				return address + REFERENCE_LENGTH;
			}
			
			@Override
			public int coveredConstantByteCount()
			{
				return REFERENCE_LENGTH;
			}
			
			@Override
			public boolean hasReferences()
			{
				return true;
			}
		};

		static final BinaryReferenceTraverser REFERENCE_2_REVERSED = new BinaryReferenceTraverser()
		{
			@Override
			public final long apply(final long address, final PersistenceObjectIdAcceptor acceptor)
			{
				acceptor.acceptObjectId(Long.reverseBytes(XMemory.get_long(address)));
				acceptor.acceptObjectId(Long.reverseBytes(XMemory.get_long(address + REFERENCE_LENGTH)));
				return address + REFERENCE_LENGTH_2;
			}
			
			@Override
			public int coveredConstantByteCount()
			{
				return REFERENCE_LENGTH_2;
			}
			
			@Override
			public boolean hasReferences()
			{
				return true;
			}
		};

		static final BinaryReferenceTraverser REFERENCE_3_REVERSED = new BinaryReferenceTraverser()
		{
			@Override
			public final long apply(final long address, final PersistenceObjectIdAcceptor acceptor)
			{
				acceptor.acceptObjectId(Long.reverseBytes(XMemory.get_long(address)));
				acceptor.acceptObjectId(Long.reverseBytes(XMemory.get_long(address + REFERENCE_LENGTH)));
				acceptor.acceptObjectId(Long.reverseBytes(XMemory.get_long(address + REFERENCE_LENGTH_2)));
				return address + REFERENCE_LENGTH_3;
			}
			
			@Override
			public int coveredConstantByteCount()
			{
				return REFERENCE_LENGTH_3;
			}
			
			@Override
			public boolean hasReferences()
			{
				return true;
			}
		};

		static final BinaryReferenceTraverser REFERENCE_4_REVERSED = new BinaryReferenceTraverser()
		{
			@Override
			public final long apply(final long address, final PersistenceObjectIdAcceptor acceptor)
			{
				final long bound = address + REFERENCE_LENGTH_4;
				for(long a = address; a < bound; a += REFERENCE_LENGTH)
				{
					acceptor.acceptObjectId(Long.reverseBytes(XMemory.get_long(a)));
				}
				return bound;
			}
			
			@Override
			public int coveredConstantByteCount()
			{
				return REFERENCE_LENGTH_4;
			}
			
			@Override
			public boolean hasReferences()
			{
				return true;
			}
		};

		static final BinaryReferenceTraverser REFERENCE_5_REVERSED = new BinaryReferenceTraverser()
		{
			@Override
			public final long apply(final long address, final PersistenceObjectIdAcceptor acceptor)
			{
				final long bound = address + REFERENCE_LENGTH_5;
				for(long a = address; a < bound; a += REFERENCE_LENGTH)
				{
					acceptor.acceptObjectId(Long.reverseBytes(XMemory.get_long(a)));
				}
				return bound;
			}
			
			@Override
			public int coveredConstantByteCount()
			{
				return REFERENCE_LENGTH_5;
			}
			
			@Override
			public boolean hasReferences()
			{
				return true;
			}
		};

		static final BinaryReferenceTraverser REFERENCE_6_REVERSED = new BinaryReferenceTraverser()
		{
			@Override
			public final long apply(final long address, final PersistenceObjectIdAcceptor acceptor)
			{
				final long bound = address + REFERENCE_LENGTH_6;
				for(long a = address; a < bound; a += REFERENCE_LENGTH)
				{
					acceptor.acceptObjectId(Long.reverseBytes(XMemory.get_long(a)));
				}
				return bound;
			}
			
			@Override
			public int coveredConstantByteCount()
			{
				return REFERENCE_LENGTH_6;
			}
			
			@Override
			public boolean hasReferences()
			{
				return true;
			}
		};

		static final BinaryReferenceTraverser REFERENCE_7_REVERSED = new BinaryReferenceTraverser()
		{
			@Override
			public final long apply(final long address, final PersistenceObjectIdAcceptor acceptor)
			{
				final long bound = address + REFERENCE_LENGTH_7;
				for(long a = address; a < bound; a += REFERENCE_LENGTH)
				{
					acceptor.acceptObjectId(Long.reverseBytes(XMemory.get_long(a)));
				}
				return bound;
			}
			
			@Override
			public int coveredConstantByteCount()
			{
				return REFERENCE_LENGTH_7;
			}
			
			@Override
			public boolean hasReferences()
			{
				return true;
			}
		};

		static final BinaryReferenceTraverser REFERENCE_8_REVERSED = new BinaryReferenceTraverser()
		{
			@Override
			public final long apply(final long address, final PersistenceObjectIdAcceptor acceptor)
			{
				final long bound = address + REFERENCE_LENGTH_8;
				for(long a = address; a < bound; a += REFERENCE_LENGTH)
				{
					acceptor.acceptObjectId(Long.reverseBytes(XMemory.get_long(a)));
				}
				return bound;
			}
			
			@Override
			public int coveredConstantByteCount()
			{
				return REFERENCE_LENGTH_8;
			}
			
			@Override
			public boolean hasReferences()
			{
				return true;
			}
		};

		static final BinaryReferenceTraverser REFERENCE_VARIABLE_LENGTH_START_BOUND_BASED_REVERSED = new BinaryReferenceTraverser()
		{
			@Override
			public final long apply(final long address, final PersistenceObjectIdAcceptor iterator)
			{
				// using length instead of element count is crucial for consolidated multi-reference iteration
				final long bound = address + Long.reverseBytes(XMemory.get_long(Binary.toBinaryListByteLengthOffset(address)));
				for(long a = Binary.toBinaryListElementsOffset(address); a < bound; a += REFERENCE_LENGTH)
				{
					iterator.acceptObjectId(Long.reverseBytes(XMemory.get_long(a)));
				}
				
				return bound;
			}

			@Override
			public boolean isVariableLength()
			{
				return true;
			}

			@Override
			public boolean hasReferences()
			{
				return true;
			}

		};

		static final BinaryReferenceTraverser skippingTraverser(final int length)
		{
			switch(length)
			{
				case  C1: return SKIP_1;
				case  C2: return SKIP_2;
				case  C3: return SKIP_3;
				case  C4: return SKIP_4;
				case  C5: return SKIP_5;
				case  C6: return SKIP_6;
				case  C7: return SKIP_7;
				case  C8: return SKIP_8;
				default: return new SkippingRangeTraverser(length);
			}
		}

		static final BinaryReferenceTraverser referenceRangeTraverser(
			final int     referenceCount ,
			final boolean switchByteOrder
		)
		{
			if(switchByteOrder)
			{
				return referenceRangeTraverserReversed(referenceCount);
			}
			
			switch(referenceCount)
			{
				case C1: return REFERENCE_1;
				case C2: return REFERENCE_2;
				case C3: return REFERENCE_3;
				case C4: return REFERENCE_4;
				case C5: return REFERENCE_5;
				case C6: return REFERENCE_6;
				case C7: return REFERENCE_7;
				case C8: return REFERENCE_8;
				default: return new ReferenceRangeTraverser(referenceCount * REFERENCE_LENGTH);
			}
		}
		
		static final BinaryReferenceTraverser referenceRangeTraverserReversed(
			final int referenceCount
		)
		{
			switch(referenceCount)
			{
				case C1: return REFERENCE_1_REVERSED;
				case C2: return REFERENCE_2_REVERSED;
				case C3: return REFERENCE_3_REVERSED;
				case C4: return REFERENCE_4_REVERSED;
				case C5: return REFERENCE_5_REVERSED;
				case C6: return REFERENCE_6_REVERSED;
				case C7: return REFERENCE_7_REVERSED;
				case C8: return REFERENCE_8_REVERSED;
				default: return new ReferenceRangeTraverserReversed(referenceCount * REFERENCE_LENGTH);
			}
		}

		static final int primitiveByteLength(final String typeName)
		{
			final Class<?> primitiveType = XReflect.tryResolvePrimitiveType(typeName);
			return primitiveType == null
				? 0
				: XMemory.byteSizePrimitive(primitiveType) // intentionally throw exception for void.class
			;
		}

		public static final BinaryReferenceTraverser[] deriveReferenceTraversers(
			final XGettingSequence<? extends PersistenceTypeDescriptionMember> members        ,
			final boolean                                                      switchByteOrder
		)
		{
			return members.iterate(new Analyzer(switchByteOrder)).yield();
		}

		public static final BinaryReferenceTraverser deriveComplexVariableLengthTraversers(
			final PersistenceTypeDescriptionMemberFieldGenericComplex member         ,
			final boolean                                             switchByteOrder
		)
		{
			final BinaryReferenceTraverser[] traversers = Static.deriveReferenceTraversers(
				member.members(),
				switchByteOrder
			);
			
			// if elements are comprised solely of references, the traversal can be simplified (inlined) to a variable length iteration
			if(traversers.length == 1 && traversers[0].hasReferences() && traversers[0].coveredConstantByteCount() > 0)
			{
				return switchByteOrder
					? REFERENCE_VARIABLE_LENGTH_START_BOUND_BASED_REVERSED
					: REFERENCE_VARIABLE_LENGTH_START_BOUND_BASED
				;
			}

			// if the elements have no references at all, no matter how complex, the traversal can be skipped completely
			if(!BinaryReferenceTraverser.hasReferences(traversers))
			{
				return variableLengthSkipper(switchByteOrder);
			}

			// otherwise truely complex types have to be traversed in the full complex way
			return new BinaryReferenceTraverser.InlinedComplexType(traversers, switchByteOrder);
		}

		public static int calculateSimpleReferenceCount(final BinaryReferenceTraverser[] traversers)
		{
			// check if structure is not suitable at all
			if(traversers.length == 0 || !traversers[0].hasReferences() || traversers[0].coveredConstantByteCount() == 0)
			{
				return 0;
			}

			// if there is anything else with references after the initial simple references, concept can't be applied.
			for(int i = 1; i < traversers.length; i++)
			{
				if(traversers[i].hasReferences())
				{
					return 0;
				}
			}

			// leading simple references guaranteed, calculate reference count
			return traversers[0].coveredConstantByteCount() / Static.REFERENCE_LENGTH;
		}

		/* important note:
		 * trailing non-references may NOT be cropped, because nested complex structures
		 * must skip all their bytes correctly. If the iteration falls only one byte short,
		 * the reference traversal behavior is undefined (usually a JVM crash).
		 * Only type-top-level traversers can be cropped!
		 */
		public static BinaryReferenceTraverser[] cropToReferences(final BinaryReferenceTraverser[] traversers)
		{
			// cut off trailing non-reference traversers at the top level
			int i = traversers.length;
			while(--i >= 0)
			{
				if(traversers[i].hasReferences())
				{
					break;
				}
			}

			// nothing to crop, return transiently
			// (25.09.2018 TM)NOTE: "+ 1" because a pre-decremented variable can never reach its initial value again.
			if(i + 1 == traversers.length)
			{
				return traversers;
			}
			if(i < 0)
			{
				return Static.EMPTY;
			}
			return XArrays.subArray(traversers, 0, i + 1);
		}

	}

	final class Analyzer implements Consumer<PersistenceTypeDescriptionMember>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final BulkList<BinaryReferenceTraverser> traversers = BulkList.New(16);
		final boolean switchByteOrder;
		int skipLength, referenceCount;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public Analyzer(final boolean switchByteOrder)
		{
			super();
			this.switchByteOrder = switchByteOrder;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		private void finishSkipRange()
		{
			if(this.skipLength != 0)
			{
				this.traversers.add(Static.skippingTraverser(this.skipLength));
				this.skipLength = 0;
			}
		}

		private void finishReferenceRange()
		{
			if(this.referenceCount != 0)
			{
				this.traversers.add(Static.referenceRangeTraverser(this.referenceCount, this.switchByteOrder));
				this.referenceCount = 0;
			}
		}

		@Override
		public void accept(final PersistenceTypeDescriptionMember member)
		{
			if(member.isVariableLength())
			{
				if(!(member instanceof PersistenceTypeDescriptionMemberFieldGenericVariableLength))
				{
					throw new BinaryPersistenceException(
						"Unhandled " + PersistenceTypeDescriptionMember.class.getSimpleName() + " type: " + member
					);
				}

				this.finishSkipRange();
				this.finishReferenceRange();
				if(member instanceof PersistenceTypeDescriptionMemberFieldGenericComplex)
				{
					this.traversers.add(
						Static.deriveComplexVariableLengthTraversers(
							(PersistenceTypeDescriptionMemberFieldGenericComplex)member,
							this.switchByteOrder
						)
					);
				}
				else if(member.hasReferences())
				{
					throw new BinaryPersistenceException(
						"Invalid referential " + PersistenceTypeDescriptionMember.class.getSimpleName() + ": " + member
					);
				}
				else
				{
					// anything else like [byte] and [char] are simply skippable
					this.traversers.add(
						Static.variableLengthSkipper(this.switchByteOrder)
					);
				}
			}
			else if(member.isReference())
			{
				this.finishSkipRange();
				this.referenceCount++;
			}
			else
			{
				// only remaining option is fixed length primitive
				this.finishReferenceRange();
				this.skipLength += member.persistentMinimumLength(); // fixed length primitive ensured above
			}
		}

		final BinaryReferenceTraverser[] yield()
		{
			this.finishSkipRange();
			this.finishReferenceRange();
			
			/* important note:
			 * trailing non-references may NOT be cropped here, because nested complex structures
			 * must skip all their bytes correctly. If the iteration falls only one byte short,
			 * the reference traversal behavior is undefined (usually a JVM crash).
			 * Only type-top-level traversers can be cropped!
			 */
			return this.traversers.toArray(BinaryReferenceTraverser.class);
		}

	}


	final class ReferenceRangeTraverser implements BinaryReferenceTraverser
	{
		private final int referenceRange;

		ReferenceRangeTraverser(final int referenceRange)
		{
			super();
			this.referenceRange = referenceRange;
		}

		@Override
		public final long apply(final long address, final PersistenceObjectIdAcceptor acceptor)
		{
			final long addressBound = address + this.referenceRange;
			for(long a = address; a < addressBound; a += Static.REFERENCE_LENGTH)
			{
				// see ReferenceRangeTraverserReversed for the ByteOrder switching alternative
				acceptor.acceptObjectId(XMemory.get_long(a));
			}
			
			return addressBound;
		}

		@Override
		public int coveredConstantByteCount()
		{
			return this.referenceRange;
		}

		@Override
		public boolean hasReferences()
		{
			return true;
		}

		@Override
		public boolean isVariableLength()
		{
			return false;
		}

	}
	
	final class ReferenceRangeTraverserReversed implements BinaryReferenceTraverser
	{
		private final int referenceRange;

		ReferenceRangeTraverserReversed(final int referenceRange)
		{
			super();
			this.referenceRange = referenceRange;
		}

		@Override
		public final long apply(final long address, final PersistenceObjectIdAcceptor acceptor)
		{
			final long addressBound = address + this.referenceRange;
			for(long a = address; a < addressBound; a += Static.REFERENCE_LENGTH)
			{
				acceptor.acceptObjectId(Long.reverseBytes(XMemory.get_long(a)));
			}
			
			return addressBound;
		}

		@Override
		public int coveredConstantByteCount()
		{
			return this.referenceRange;
		}

		@Override
		public boolean hasReferences()
		{
			return true;
		}

		@Override
		public boolean isVariableLength()
		{
			return false;
		}

	}

	final class SkippingRangeTraverser implements BinaryReferenceTraverser
	{
		private final int skipLength;

		SkippingRangeTraverser(final int skipLength)
		{
			super();
			this.skipLength = skipLength;
		}

		@Override
		public final long apply(final long address, final PersistenceObjectIdAcceptor acceptor)
		{
			return address + this.skipLength;
		}

		@Override
		public int coveredConstantByteCount()
		{
			return this.skipLength;
		}

		@Override
		public boolean hasReferences()
		{
			return false;
		}

		@Override
		public boolean isVariableLength()
		{
			return false;
		}

	}



	final class InlinedComplexType implements BinaryReferenceTraverser
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final BinaryReferenceTraverser[] traversers     ;
		final boolean                    switchByteOrder;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		InlinedComplexType(
			final BinaryReferenceTraverser[] traversers     ,
			final boolean                    switchByteOrder
		)
		{
			super();
			this.traversers      = traversers     ;
			this.switchByteOrder = switchByteOrder;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final long apply(final long address, final PersistenceObjectIdAcceptor acceptor)
		{
			/* Note on security:
			 * The traverser neither handles newly received data nor does it create new instances.
			 * It always only traverses existing, already validated data.
			 * 
			 * (22.01.2019 TM)XXX: BinaryReferenceTraverser naively safe?
			 * But is that assumption really true? Also in the future?
			 * What it received data shall be traversed to analyze/etc. it?
			 * A (intentionally) malformed element count would cause the traverser to read data
			 * way beyond the limits of the to be traversed data.
			 * 
			 * Using the validating element count getter would require to know the element binary length.
			 * And that can get very ugly if the element of a complex type has variable length on its own.
			 */
			final long elementCountRawValue = XMemory.get_long(Binary.toBinaryListElementCountOffset(address));
			final long elementCount = this.switchByteOrder
				? Long.reverseBytes(elementCountRawValue)
				: elementCountRawValue
			;

			// apply all element traversers to each element
			long a = Binary.toBinaryListElementsOffset(address);
			for(long i = 0; i < elementCount; i++)
			{
				a = BinaryReferenceTraverser.iterateReferences(a, this.traversers, acceptor);
			}

			// return resulting address for recursive continued use
			return a;
		}

		@Override
		public final int coveredConstantByteCount()
		{
			// 0 means no constant amount of references. See calling methods of this method.
			return 0;
		}

		@Override
		public final boolean hasReferences()
		{
			// this type is only used if there is at least one reference. Otherwise, the variable length skipper is used.
			return true;
		}

		@Override
		public final boolean isVariableLength()
		{
			return true;
		}

	}

}
