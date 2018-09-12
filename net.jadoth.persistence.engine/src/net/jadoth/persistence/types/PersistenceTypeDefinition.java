package net.jadoth.persistence.types;

import static net.jadoth.X.mayNull;
import static net.jadoth.X.notNull;

import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XImmutableEnum;
import net.jadoth.swizzling.types.SwizzleTypeLink;

public interface PersistenceTypeDefinition<T> extends PersistenceTypeDictionaryEntry, SwizzleTypeLink<T>
{
	@Override
	public long   typeId();

	@Override
	public String typeName();

	/* (30.06.2015 TM)TODO: PersistenceTypeDescription <?>Generics
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

	/**
	 * Enum (unique elements with order), using {@link PersistenceTypeDescriptionMember#identityHashEqualator()}.
	 * 
	 * @return
	 */
	@Override
	public XGettingEnum<? extends PersistenceTypeDescriptionMember> members();

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
	
	public default String toTypeString()
	{
		return this.getClass().getName() + "<" + this.type().getName() + ">(TID " + this.typeId() + ")";
	}

	

	public static boolean determineVariableLength(
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> members
	)
	{
		for(final PersistenceTypeDescriptionMember member : members)
		{
			if(member.isVariableLength())
			{
				return true;
			}
		}
		return false;
	}

	public static boolean determineIsPrimitive(
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> members
	)
	{
		return members.size() == 1 && members.get().isPrimitiveDefinition();
	}

	/**
	 * Equal content description, without TypeId comparison
	 * 
	 * @param td1
	 * @param td2
	 * @return
	 */
	public static boolean equalDescription(
		final PersistenceTypeDefinition<?> td1,
		final PersistenceTypeDefinition<?> td2
	)
	{
		return td1 == td2 || td1 != null && td2 != null
			&& td1.typeName().equals(td1.typeName())
			&& PersistenceTypeDescriptionMember.equalDescriptions(td1.members(), td2.members())
		;
	}

	
	
	public static <T> PersistenceTypeDefinition<T> New(
		final String                                                   typeName,
		final Class<T>                                                 type    ,
		final long                                                     typeId  ,
		final XGettingEnum<? extends PersistenceTypeDescriptionMember> members
	)
	{
		// as defined by interface contract.
		if(members.equality() != PersistenceTypeDescriptionMember.identityHashEqualator())
		{
			throw new IllegalArgumentException();
		}
		
		// no-op for already immutable collection type (e.g. PersistenceTypeDescriptionMember#validateAndImmure)
		final XImmutableEnum<? extends PersistenceTypeDescriptionMember> internalMembers = members.immure();
		return new PersistenceTypeDefinition.Implementation<>(
			                                                         typeId          ,
			                                                 notNull(typeName)       ,
			                                                 mayNull(type)           ,
			                                                         internalMembers ,
			PersistenceTypeDescriptionMember.determineHasReferences (internalMembers),
			PersistenceTypeDefinition       .determineIsPrimitive   (internalMembers),
			PersistenceTypeDefinition       .determineVariableLength(internalMembers)
		);
	}



	public final class Implementation<T>
	extends PersistenceTypeDictionaryEntry.AbstractImplementation
	implements PersistenceTypeDefinition<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final long                                                       typeId        ;
		final String                                                     typeName      ;
		final Class<T>                                                   type          ;
		final XImmutableEnum<? extends PersistenceTypeDescriptionMember> members       ;
		final boolean                                                    hasReferences ;
		final boolean                                                    isPrimitive   ;
		final boolean                                                    variableLength;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(
			final long                                                       typeId        ,
			final String                                                     typeName      ,
			final Class<T>                                                   type          ,
			final XImmutableEnum<? extends PersistenceTypeDescriptionMember> members       ,
			final boolean                                                    hasReferences ,
			final boolean                                                    isPrimitive   ,
			final boolean                                                    variableLength
		)
		{
			super();
			this.typeId         = typeId        ;
			this.typeName       = typeName      ;
			this.type           = type          ;
			this.members        = members       ;
			this.hasReferences  = hasReferences ;
			this.isPrimitive    = isPrimitive   ;
			this.variableLength = variableLength;
		}

		

		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

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
		public final XImmutableEnum<? extends PersistenceTypeDescriptionMember> members()
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
			return this.toTypeString();
		}
		

	}
	
}
