package net.jadoth.persistence.types;

import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XImmutableSequence;
import net.jadoth.swizzling.types.SwizzleTypeLink;
import net.jadoth.util.chars.VarString;

/**
 * Type that further defines a {@link PersistenceTypeDictionaryEntry} regarding runtime links and
 * special attributes.
 * 
 * @author TM
 *
 * @param <T>
 */
public interface PersistenceTypeDefinition<T> extends PersistenceTypeDictionaryEntry, SwizzleTypeLink<T>
{
	@Override
	public long   typeId();

	@Override
	public String typeName();

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
	
	
	
	
	public static boolean isEqualDescription(final PersistenceTypeDefinition<?> td1, final PersistenceTypeDefinition<?> td2)
	{
		return td1 == td2
			|| td1 != null && td2 != null
			&& td1.typeId() == td2.typeId()
			&& PersistenceTypeDescription.isEqualStructure(td1, td2)
		;
	}
		

	public static PersistenceTypeDefinitionCreator Builder()
	{
		return new PersistenceTypeDefinitionCreator.Implementation();
	}
	
	public static <T> PersistenceTypeDefinition<T> New(
//		final PersistenceTypeLineage<T>                         lineage,
		final String                                                       typeName,
		final Class<T>                                                     type    ,
		final long                                                         typeId ,
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> members
	)
	{
		return new PersistenceTypeDefinition.Implementation<>(typeName, type, typeId, members);
	}
	
	
	public final class Implementation<T> implements PersistenceTypeDefinition<T>
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
//		final PersistenceTypeLineage<T>                                      typeLineage   ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(
			final String                                                       typeName,
			final Class<T>                                                     type    ,
			final long                                                         typeId  ,
			final XGettingSequence<? extends PersistenceTypeDescriptionMember> members
		)
		{
			super();
//			this.typeLineage       = typeLineage              ;
			this.typeId            = typeId                   ;
			this.typeName          = typeName                 ;
			this.type              = type                     ; // may be null for an obsolete type description
			this.members           = members.immure()         ; // same instance if already immutable
			this.hasReferences     = PersistenceTypeDescriptionMember.determineHasReferences (members);
			this.isPrimitive       = PersistenceTypeDescriptionMember.determineIsPrimitive   (members);
			this.variableLength    = PersistenceTypeDescriptionMember.determineVariableLength(members);
		}

		

		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////
		
//		@Override
//		public final PersistenceTypeLineage<T> lineage()
//		{
//			return this.typeLineage;
//		}
//
//		@Override
//		public final PersistenceTypeLineage<T> initializeLineage(final PersistenceTypeLineage<T> lineage)
//		{
//			// this implementation only validates the immutable reference.
//			if(this.typeLineage == lineage)
//			{
//				return lineage;
//			}
//
//			// (01.09.2017 TM)EXCP: proper exception
//			throw new RuntimeException(
//				"Already initialized for another " + PersistenceTypeLineage.class.getSimpleName()
//			);
//		}

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
