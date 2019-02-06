package net.jadoth.persistence.binary.types;

import java.util.function.Consumer;

import net.jadoth.collections.BulkList;
import net.jadoth.collections.XArrays;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.memory.XMemory;
import net.jadoth.persistence.types.PersistenceObjectIdAcceptor;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMember;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMemberPseudoFieldComplex;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMemberPseudoFieldVariableLength;
import net.jadoth.reflect.XReflect;


@FunctionalInterface
public interface BinaryReferenceTraverser
{
	public long apply(long address, PersistenceObjectIdAcceptor acceptor);

	/**
	 * This method reports the amount of bytes that a particular instance of an implementing type covers or advances.
	 * For example, an objectId is 8 bytes long. 5 objectIds are 40 bytes long.
	 * Skipping 6 bytes (primitives) is 6 bytes long.
	 * An implementation handling a variable length structure reports 0 bytes here.
	 * 
	 * @return
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
		final PersistenceObjectIdAcceptor procedure
	)
	{
		long a = address;
		for(int i = 0; i < traversers.length; i++)
		{
			a = traversers[i].apply(a, procedure);
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
			iterator.accept(XMemory.get_long(a));
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
			REFERENCE_LENGTH   = Binary.oidByteLength(),
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
			public final long apply(final long address, final PersistenceObjectIdAcceptor procedure)
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
			public final long apply(final long address, final PersistenceObjectIdAcceptor procedure)
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
			public final long apply(final long address, final PersistenceObjectIdAcceptor procedure)
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
			public final long apply(final long address, final PersistenceObjectIdAcceptor procedure)
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
			public final long apply(final long address, final PersistenceObjectIdAcceptor procedure)
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
			public final long apply(final long address, final PersistenceObjectIdAcceptor procedure)
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
			public final long apply(final long address, final PersistenceObjectIdAcceptor procedure)
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
			public final long apply(final long address, final PersistenceObjectIdAcceptor procedure)
			{
				return address + C8;
			}
			
			@Override
			public int coveredConstantByteCount()
			{
				return C8;
			}
		};

		static final BinaryReferenceTraverser SKIP_VARIABLE_LENGTH =
			new BinaryReferenceTraverser()
			{
				@Override
				public final long apply(final long address, final PersistenceObjectIdAcceptor procedure)
				{
					// first 8 bytes of inlined data is its total binary length
					return address + XMemory.get_long(address);
				}

			}
		;

		static final BinaryReferenceTraverser REFERENCE_1 = new BinaryReferenceTraverser()
		{
			@Override
			public final long apply(final long address, final PersistenceObjectIdAcceptor procedure)
			{
				procedure.accept(XMemory.get_long(address));
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
			public final long apply(final long address, final PersistenceObjectIdAcceptor procedure)
			{
				procedure.accept(XMemory.get_long(address));
				procedure.accept(XMemory.get_long(address + REFERENCE_LENGTH));
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
			public final long apply(final long address, final PersistenceObjectIdAcceptor procedure)
			{
				procedure.accept(XMemory.get_long(address));
				procedure.accept(XMemory.get_long(address + REFERENCE_LENGTH));
				procedure.accept(XMemory.get_long(address + REFERENCE_LENGTH_2));
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
			public final long apply(final long address, final PersistenceObjectIdAcceptor procedure)
			{
				final long bound = address + REFERENCE_LENGTH_4;
				for(long a = address; a < bound; a += REFERENCE_LENGTH)
				{
					procedure.accept(XMemory.get_long(a));
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
			public final long apply(final long address, final PersistenceObjectIdAcceptor procedure)
			{
				final long bound = address + REFERENCE_LENGTH_5;
				for(long a = address; a < bound; a += REFERENCE_LENGTH)
				{
					procedure.accept(XMemory.get_long(a));
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
			public final long apply(final long address, final PersistenceObjectIdAcceptor procedure)
			{
				final long bound = address + REFERENCE_LENGTH_6;
				for(long a = address; a < bound; a += REFERENCE_LENGTH)
				{
					procedure.accept(XMemory.get_long(a));
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
			public final long apply(final long address, final PersistenceObjectIdAcceptor procedure)
			{
				final long bound = address + REFERENCE_LENGTH_7;
				for(long a = address; a < bound; a += REFERENCE_LENGTH)
				{
					procedure.accept(XMemory.get_long(a));
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
			public final long apply(final long address, final PersistenceObjectIdAcceptor procedure)
			{
				final long bound = address + REFERENCE_LENGTH_8;
				for(long a = address; a < bound; a += REFERENCE_LENGTH)
				{
					procedure.accept(XMemory.get_long(a));
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
			public final long apply(final long address, final PersistenceObjectIdAcceptor procedure)
			{
				// using length instead of element count is crucial for consolidated multi-reference iteration
				final long bound = address + Binary.getBinaryListByteLengthRawValue(address);
				for(long a = Binary.binaryListElementsAddressAbsolute(address); a < bound; a += REFERENCE_LENGTH)
				{
					procedure.accept(XMemory.get_long(a));
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

		static final BinaryReferenceTraverser referenceRangeTraverser(final int referenceCount)
		{
			switch(referenceCount)
			{
				case  C1: return REFERENCE_1;
				case  C2: return REFERENCE_2;
				case  C3: return REFERENCE_3;
				case  C4: return REFERENCE_4;
				case  C5: return REFERENCE_5;
				case  C6: return REFERENCE_6;
				case  C7: return REFERENCE_7;
				case  C8: return REFERENCE_8;
				default: return new ReferenceRangeTraverser(referenceCount * REFERENCE_LENGTH);
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
			final XGettingSequence<? extends PersistenceTypeDescriptionMember> members
		)
		{
			return members.iterate(new Analyzer()).yield();
		}

		public static final BinaryReferenceTraverser deriveComplexVariableLengthTraversers(
			final PersistenceTypeDescriptionMemberPseudoFieldComplex member
		)
		{
			final BinaryReferenceTraverser[] traversers = Static.deriveReferenceTraversers(member.members());
			
			// if elements are comprised solely of references, the traversal can be simplified (inlined) to a variable length iteration
			if(traversers.length == 1 && traversers[0].hasReferences() && traversers[0].coveredConstantByteCount() > 0)
			{
				return REFERENCE_VARIABLE_LENGTH_START_BOUND_BASED;
			}

			// if the elements have no references at all, no matter how complex, the traversal can be skipped completely
			if(!BinaryReferenceTraverser.hasReferences(traversers))
			{
				return SKIP_VARIABLE_LENGTH;
			}

			// otherwise truely complex types have to be traversed in the full complex way
			return new BinaryReferenceTraverser.InlinedComplexType(traversers);
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
		int skipLength, referenceCount;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public Analyzer()
		{
			super();
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
				this.traversers.add(Static.referenceRangeTraverser(this.referenceCount));
				this.referenceCount = 0;
			}
		}

		@Override
		public void accept(final PersistenceTypeDescriptionMember member)
		{
			if(member.isVariableLength())
			{
				if(!(member instanceof PersistenceTypeDescriptionMemberPseudoFieldVariableLength))
				{
					// (20.12.2014)EXCP: proper exception
					throw new RuntimeException(
						"Unhandled " + PersistenceTypeDescriptionMember.class.getSimpleName() + " type: " + member
					);
				}

				this.finishSkipRange();
				this.finishReferenceRange();
				if(member instanceof PersistenceTypeDescriptionMemberPseudoFieldComplex)
				{
					this.traversers.add(
						Static.deriveComplexVariableLengthTraversers((PersistenceTypeDescriptionMemberPseudoFieldComplex)member)
					);
				}
				else if(member.hasReferences())
				{
					// (20.12.2014)EXCP: proper exception
					throw new RuntimeException(
						"Invalid referential " + PersistenceTypeDescriptionMember.class.getSimpleName() + ": " + member
					);
				}
				else
				{
					// anything else like [byte] and [char] are simply skippable
					this.traversers.add(Static.SKIP_VARIABLE_LENGTH);
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
		public final long apply(final long address, final PersistenceObjectIdAcceptor procedure)
		{
			final long addressBound = address + this.referenceRange;
			for(long a = address; a < addressBound; a += Static.REFERENCE_LENGTH)
			{
				procedure.accept(XMemory.get_long(a));
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
		public final long apply(final long address, final PersistenceObjectIdAcceptor procedure)
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
		final BinaryReferenceTraverser[] traversers;

		InlinedComplexType(final BinaryReferenceTraverser[] traversers)
		{
			super();
			this.traversers = traversers;
		}

		@Override
		public final long apply(final long address, final PersistenceObjectIdAcceptor procedure)
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
			final long elementCount = Binary.getBinaryListElementCountRawValue(address);

			// apply all element traversers to each element
			long a = Binary.binaryListElementsAddressAbsolute(address);
			for(long i = 0; i < elementCount; i++)
			{
				a = BinaryReferenceTraverser.iterateReferences(a, this.traversers, procedure);
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
