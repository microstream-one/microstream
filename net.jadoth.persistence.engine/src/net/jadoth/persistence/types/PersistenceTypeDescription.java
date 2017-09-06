package net.jadoth.persistence.types;

import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XImmutableSequence;
import net.jadoth.swizzling.types.SwizzleTypeLink;
import net.jadoth.util.chars.VarString;

public interface PersistenceTypeDescription<T> extends PersistenceTypeDictionaryEntry, SwizzleTypeLink<T>
{
	@Override
	public long   typeId();

	@Override
	public String typeName();

	/* (30.06.2015 TM)TODO: PersistenceTypeDescription<?> Generics
	 * Must consider Generics Type information as well, at least as a simple normalized String for
	 * equality comparison.
	 * Otherwise, changing type parameter won't be recognized by the type validation and
	 * loading/building of entities will result in heap pollution (wrong instance for the type).
	 * Example:
	 * Lazy<Person> changed to Lazy<Employee>.
	 * Currently, this is just recognized as Lazy.
	 * 
	 * (05.04.2017 TM)NOTE: but does it really have to be stored here?
	 * Wouldn't it be enough to store it in the member description?
	 * E.g. Type "Lazy" PLUS type parameter "[full qualified] Person"
	 */

	@Override
	public XGettingSequence<? extends PersistenceTypeDescriptionMember> members();

	public boolean hasPersistedReferences();

	/**
	 * Provides information if two instances of the handled type can have different length in persisted form.<p>
	 * Examples for variable length types:
	 * <ul>
	 * <li> arrays</li>
	 * <li>{@code java.lang.String}</li>
	 * <li>{@code java.util.ArrayList}</li>
	 * <li>{@code java.math.BigDecimal}</li>
	 * </ul><p>
	 * Examples for fixed length types:
	 * <ul>
	 * <li>primitive value wrapper types</li>
	 * <li>{@code java.lang.Object}</li>
	 * <li>{@code java.util.Date}</li>
	 * <li>typical entity types (without unshared inlined variable length component instances)</li>
	 * </ul>
	 *
	 * @return
	 */
	public boolean hasPersistedVariableLength();

	public boolean isPrimitiveType();
	
	/**
	 * Provides information if one particular instance can have variing binary length from one store to another.<p>
	 * Examples for variable length instances:
	 * <ul>
	 * <li> variable size collection instances</li>
	 * <li> variable size pesudo collection instances like {@code java.util.StringBuilder}</li>
	 * <li> instances of custom defined types similar to collections</li>
	 * </ul><p>
	 * Examples for fixed length instances:
	 * <ul>
	 * <li>arrays</li>
	 * <li>all immutable type instances (like {@code java.lang.String} )</li>
	 * <li>all fixed length types (see {@link #isVariableBinaryLengthType()}</li>
	 * </ul>
	 *
	 * @return
	 */
	public boolean hasVaryingPersistedLengthInstances();
	
	public PersistenceTypeDescriptionLineage<T> lineage();

	public default boolean isRuntime()
	{
		return this.lineage().runtimeDescription() == this;
	}
	
	public PersistenceTypeDescriptionLineage<T> initializeLineage(PersistenceTypeDescriptionLineage<T> lineage);
	
	
	
	
	public static boolean isEqualDescription(final PersistenceTypeDescription<?> td1, final PersistenceTypeDescription<?> td2)
	{
		return td1 == td2
			|| td1 != null && td2 != null
			&& td1.typeId() == td2.typeId()
			&& td1.typeName().equals(td2.typeName())
			&& PersistenceTypeDescriptionMember.equalMembers(td1.members(), td2.members())
		;
	}
	

	public static PersistenceTypeDescriptionBuilder Builder()
	{
		return new PersistenceTypeDescriptionBuilder.Implementation();
	}
	
	public static <T> PersistenceTypeDescription<T> New(
		final PersistenceTypeDescriptionLineage<T>                         lineage,
		final long                                                         typeId ,
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> members
	)
	{
		return new PersistenceTypeDescription.Implementation<>(lineage, typeId, members);
	}
	
	
	public final class Implementation<T> implements PersistenceTypeDescription<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final long                                                           typeId        ;
		final String                                                         typeName      ;
		final Class<T>                                                       type          ;
		final XImmutableSequence<? extends PersistenceTypeDescriptionMember> members       ;
		final boolean                                                        hasReferences ;
		final boolean                                                        isPrimitive   ;
		final boolean                                                        variableLength;
		final PersistenceTypeDescriptionLineage<T>                           lineage       ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(
			final PersistenceTypeDescriptionLineage<T>                         lineage,
			final long                                                         typeId ,
			final XGettingSequence<? extends PersistenceTypeDescriptionMember> members
		)
		{
			super();
			this.lineage           = lineage              ;
			this.typeId            = typeId               ;
			this.typeName          = lineage.typeName()   ;
			this.type              = lineage.runtimeType(); // may be null for an obsolete type description
			this.members           = members.immure()     ; // same instance if already immutable
			this.hasReferences     = PersistenceTypeDescriptionMember.determineHasReferences (members);
			this.isPrimitive       = PersistenceTypeDescriptionMember.determineIsPrimitive   (members);
			this.variableLength    = PersistenceTypeDescriptionMember.determineVariableLength(members);
		}

		

		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////
		
		@Override
		public final PersistenceTypeDescriptionLineage<T> lineage()
		{
			return this.lineage;
		}
		
		@Override
		public final PersistenceTypeDescriptionLineage<T> initializeLineage(final PersistenceTypeDescriptionLineage<T> lineage)
		{
			// this implementation only validates the immutable reference.
			if(this.lineage == lineage)
			{
				return lineage;
			}
			
			// (01.09.2017 TM)EXCP: proper exception
			throw new RuntimeException(
				"Already initialized for another " + PersistenceTypeDescriptionLineage.class.getSimpleName()
			);
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
		
		@Override
		public final Class<T> type()
		{
			return this.type;
		}
		
		@Override
		public final XImmutableSequence<? extends PersistenceTypeDescriptionMember> members()
		{
			return this.members;
		}
		
		@Override
		public final boolean hasPersistedReferences()
		{
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
			return this.variableLength;
		}
		
		@Override
		public final boolean hasVaryingPersistedLengthInstances()
		{
			return this.variableLength;
		}
		
		@Override
		public final String toString()
		{
			final VarString vc = VarString.New();
			
			vc.add(this.typeId).blank().add(this.typeName).blank().add('{');
			if(!this.members.isEmpty())
			{
				vc.lf();
				for(final PersistenceTypeDescriptionMember member : this.members)
				{
					vc.tab().add(member).add(';').lf();
				}
			}
			vc.add('}');
			
			return vc.toString();
		}

	}
	
}
