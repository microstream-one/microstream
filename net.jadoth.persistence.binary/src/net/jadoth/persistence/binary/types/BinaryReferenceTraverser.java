package net.jadoth.persistence.binary.types;

import java.util.function.Consumer;

import net.jadoth.collections.BulkList;
import net.jadoth.collections.JadothArrays;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.functional._longProcedure;
import net.jadoth.memory.Memory;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMember;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMemberPseudoFieldComplex;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMemberPseudoFieldVariableLength;
import net.jadoth.reflect.JadothReflect;
import net.jadoth.util.JadothTypes;


@FunctionalInterface
public interface BinaryReferenceTraverser
{
	public long apply(long address, _longProcedure procedure);

	public default int count()
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
		final long                       address   ,
		final BinaryReferenceTraverser[] traversers,
		final _longProcedure             procedure
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
		final long           address       ,
		final long           referenceRange,
		final _longProcedure procedure
	)
	{
		final long addressBound = address + referenceRange;
		for(long a = address; a < addressBound; a += Static.REFERENCE_LENGTH)
		{
			procedure.accept(Memory.get_long(a));
		}
	}

	public static BinaryReferenceTraverser[] none()
	{
		return Static.EMPTY;
	}




	public static final class Static
	{
		static final boolean hasReferences(final BinaryReferenceTraverser[] traversers)
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
			REFERENCE_LENGTH   = JadothTypes.to_int(BinaryPersistence.oidLength()),
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
			public final long apply(final long address, final _longProcedure procedure)
			{
				return address + C1;
			}
			
			@Override
			public int count()
			{
				return C1;
			}
		};

		static final BinaryReferenceTraverser SKIP_2 = new BinaryReferenceTraverser()
		{
			@Override
			public final long apply(final long address, final _longProcedure procedure)
			{
				return address + C2;
			}
			
			@Override
			public int count()
			{
				return C2;
			}
		};

		static final BinaryReferenceTraverser SKIP_3 = new BinaryReferenceTraverser()
		{
			@Override
			public final long apply(final long address, final _longProcedure procedure)
			{
				return address + C3;
			}
			
			@Override
			public int count()
			{
				return C3;
			}
		};

		static final BinaryReferenceTraverser SKIP_4 = new BinaryReferenceTraverser()
		{
			@Override
			public final long apply(final long address, final _longProcedure procedure)
			{
				return address + C4;
			}
			
			@Override
			public int count()
			{
				return C4;
			}
		};

		static final BinaryReferenceTraverser SKIP_5 = new BinaryReferenceTraverser()
		{
			@Override
			public final long apply(final long address, final _longProcedure procedure)
			{
				return address + C5;
			}
			
			@Override
			public int count()
			{
				return C5;
			}
		};

		static final BinaryReferenceTraverser SKIP_6 = new BinaryReferenceTraverser()
		{
			@Override
			public final long apply(final long address, final _longProcedure procedure)
			{
				return address + C6;
			}
			
			@Override
			public int count()
			{
				return C6;
			}
		};

		static final BinaryReferenceTraverser SKIP_7 = new BinaryReferenceTraverser()
		{
			@Override
			public final long apply(final long address, final _longProcedure procedure)
			{
				return address + C7;
			}
			
			@Override
			public int count()
			{
				return C7;
			}
		};

		static final BinaryReferenceTraverser SKIP_8 = new BinaryReferenceTraverser()
		{
			@Override
			public final long apply(final long address, final _longProcedure procedure)
			{
				return address + C8;
			}
			
			@Override
			public int count()
			{
				return C8;
			}
		};

		static final BinaryReferenceTraverser SKIP_VARIABLE_LENGTH =
			new BinaryReferenceTraverser()
			{
				@Override
				public final long apply(final long address, final _longProcedure procedure)
				{
					// first 8 bytes of inlined data is its total binary length
					return address + Memory.get_long(address);
				}

			}
		;

		static final BinaryReferenceTraverser REFERENCE_1 = new BinaryReferenceTraverser()
		{
			@Override
			public final long apply(final long address, final _longProcedure procedure)
			{
				procedure.accept(Memory.get_long(address));
				return address + REFERENCE_LENGTH;
			}
			
			@Override
			public int count()
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
			public final long apply(final long address, final _longProcedure procedure)
			{
				procedure.accept(Memory.get_long(address));
				procedure.accept(Memory.get_long(address + REFERENCE_LENGTH));
				return address + REFERENCE_LENGTH_2;
			}
			
			@Override
			public int count()
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
			public final long apply(final long address, final _longProcedure procedure)
			{
				procedure.accept(Memory.get_long(address));
				procedure.accept(Memory.get_long(address + REFERENCE_LENGTH));
				procedure.accept(Memory.get_long(address + REFERENCE_LENGTH_2));
				return address + REFERENCE_LENGTH_3;
			}
			
			@Override
			public int count()
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
			public final long apply(final long address, final _longProcedure procedure)
			{
				final long bound = address + REFERENCE_LENGTH_4;
				for(long a = address; a < bound; a += REFERENCE_LENGTH)
				{
					procedure.accept(Memory.get_long(a));
				}
				return bound;
			}
			
			@Override
			public int count()
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
			public final long apply(final long address, final _longProcedure procedure)
			{
				final long bound = address + REFERENCE_LENGTH_5;
				for(long a = address; a < bound; a += REFERENCE_LENGTH)
				{
					procedure.accept(Memory.get_long(a));
				}
				return bound;
			}
			
			@Override
			public int count()
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
			public final long apply(final long address, final _longProcedure procedure)
			{
				final long bound = address + REFERENCE_LENGTH_6;
				for(long a = address; a < bound; a += REFERENCE_LENGTH)
				{
					procedure.accept(Memory.get_long(a));
				}
				return bound;
			}
			
			@Override
			public int count()
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
			public final long apply(final long address, final _longProcedure procedure)
			{
				final long bound = address + REFERENCE_LENGTH_7;
				for(long a = address; a < bound; a += REFERENCE_LENGTH)
				{
					procedure.accept(Memory.get_long(a));
				}
				return bound;
			}
			
			@Override
			public int count()
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
			public final long apply(final long address, final _longProcedure procedure)
			{
				final long bound = address + REFERENCE_LENGTH_8;
				for(long a = address; a < bound; a += REFERENCE_LENGTH)
				{
					procedure.accept(Memory.get_long(a));
				}
				return bound;
			}
			
			@Override
			public int count()
			{
				return REFERENCE_LENGTH_8;
			}
			
			@Override
			public boolean hasReferences()
			{
				return true;
			}
		};

		static final BinaryReferenceTraverser REFERENCE_VARIABLE_LENGTH = new BinaryReferenceTraverser()
		{
			@Override
			public final long apply(final long address, final _longProcedure procedure)
			{
				// using length instead of element count is crucial for consolidated multi-reference iteration
				final long bound = address + BinaryPersistence.getListBinaryLength(address);
				for(long a = BinaryPersistence.getListElementsAddress(address); a < bound; a += REFERENCE_LENGTH)
				{
					procedure.accept(Memory.get_long(a));
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
			final Class<?> primitiveType = JadothReflect.primitiveType(typeName);
			return primitiveType == null
				? 0
				: Memory.byteSizePrimitive(primitiveType) // intentionally throw exception for void.class
			;
		}

		static final BinaryReferenceTraverser wrap(final BinaryReferenceTraverser[] traversers)
		{
			// if elements are comprised solely of references, the traversal can be simplified (inlined)
			if(traversers.length == 1 && traversers[0].hasReferences() && traversers[0].count() > 0)
			{
				return REFERENCE_VARIABLE_LENGTH;
			}

			// if the elements have no references at all, no matter how complex, the traversal can be skipped completely
			if(!BinaryReferenceTraverser.Static.hasReferences(traversers))
			{
				return SKIP_VARIABLE_LENGTH;
			}

			// otherwise truely complex types have to be traversed in the full complex way
			return new BinaryReferenceTraverser.InlinedComplexType(traversers, true);
		}

		public static final BinaryReferenceTraverser[] deriveReferenceTraversers(
			final XGettingSequence<? extends PersistenceTypeDescriptionMember> members
		)
		{
			return members.iterate(new Analyzer()).yield();
		}

		public static final BinaryReferenceTraverser deriveNested(
			final PersistenceTypeDescriptionMemberPseudoFieldComplex member
		)
		{
//			if(!member.hasReferences())
//			{
//				return Static.skipVariableLength;
//			}

			return Static.wrap(Static.deriveReferenceTraversers(member.members()));
		}

		public static int calculateSimpleReferenceCount(final BinaryReferenceTraverser[] traversers)
		{
			// check if structure is not suitable at all
			if(traversers.length == 0 || !traversers[0].hasReferences() || traversers[0].count() == 0)
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
			return traversers[0].count() / Static.REFERENCE_LENGTH;
		}

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
			if(i == traversers.length)
			{
				return traversers;
			}
			if(i < 0)
			{
				return Static.EMPTY;
			}
			return JadothArrays.subArray(traversers, 0, i + 1);
		}

	}

	final class Analyzer implements Consumer<PersistenceTypeDescriptionMember>
	{
		final BulkList<BinaryReferenceTraverser> traversers = BulkList.New(16);
		int skipLength, referenceCount;


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
						Static.deriveNested((PersistenceTypeDescriptionMemberPseudoFieldComplex)member)
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
		public final long apply(final long address, final _longProcedure procedure)
		{
			final long addressBound = address + this.referenceRange;
			for(long a = address; a < addressBound; a += Static.REFERENCE_LENGTH)
			{
				procedure.accept(Memory.get_long(a));
			}
			return addressBound;
		}

		@Override
		public int count()
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
		public final long apply(final long address, final _longProcedure procedure)
		{
			return address + this.skipLength;
		}

		@Override
		public int count()
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
		final BinaryReferenceTraverser[] traversers   ;
		final boolean                    hasReferences;

		InlinedComplexType(final BinaryReferenceTraverser[] traversers)
		{
			this(traversers, Static.hasReferences(traversers));
		}

		InlinedComplexType(final BinaryReferenceTraverser[] traversers, final boolean hasReferences)
		{
			super();
			this.traversers    = traversers;
			this.hasReferences = hasReferences;
		}

		@Override
		public final long apply(final long address, final _longProcedure procedure)
		{
			final long elementCount = BinaryPersistence.getListElementCount(address);

			// apply all element traversers to each element
			long a = BinaryPersistence.getListElementsAddress(address);
			for(long i = 0; i < elementCount; i++)
			{
				a = BinaryReferenceTraverser.iterateReferences(a, this.traversers, procedure);
			}

			// return resulting address for recursive continued use
			return a;
		}

		@Override
		public final int count()
		{
			return 0;
		}

		@Override
		public final boolean hasReferences()
		{
			return this.hasReferences;
		}

		@Override
		public final boolean isVariableLength()
		{
			return true;
		}

	}

}
