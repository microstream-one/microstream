package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

import net.jadoth.collections.X;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.collections.types.XImmutableSequence;
import net.jadoth.reflect.JadothReflect;
import net.jadoth.swizzling.types.SwizzleTypeLink;
import net.jadoth.swizzling.types.SwizzleTypeManager;
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

	public boolean isLatestPersisted();
	
	public default boolean isCurrent()
	{
		return this.current() == this;
	}
	
	public XGettingTable<Long, PersistenceTypeDescription<T>> obsoletes();
		
	public PersistenceTypeDescription<T> current();
	
	
	
	
	public static boolean isEqualDescription(final PersistenceTypeDescription<?> td1, final PersistenceTypeDescription<?> td2)
	{
		return td1 == td2 || td1 != null && td2 != null
			&& td1.typeId() == td2.typeId()
			&& td1.typeName().equals(td2.typeName())
			&& PersistenceTypeDescriptionMember.equalMembers(td1.members(), td2.members())
		;
	}
	

	public static PersistenceTypeDescription.Builder Builder()
	{
		return new PersistenceTypeDescription.Builder.Implementation();
	}

	public static <T> PersistenceTypeDescription<T> New(
		final long                                                         typeId  ,
		final String                                                       typeName,
		final Class<T>                                                     type    ,
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> members
	)
	{
		return New(typeId, typeName, type, members, null, true, true, X.emptyTable());
	}
	
	public static <T> PersistenceTypeDescription<T> New(
		final long                                                         typeId           ,
		final String                                                       typeName         ,
		final Class<T>                                                     type             ,
		final XGettingSequence<? extends PersistenceTypeDescriptionMember> members          ,
		final PersistenceTypeDescription<T>                                current          ,
		final boolean                                                      isCurrent        ,
		final boolean                                                      isLatestPersisted,
		final XGettingTable<Long, PersistenceTypeDescription<T>>           obsoletes
	)
	{
		return new PersistenceTypeDescription.Implementation<>(
			        typeId           ,
			notNull(typeName)        ,
			        type             , // may be null in case type is not resolvable
			notNull(members)         ,
			        current          , // may be null to indicate "this"
			        isCurrent        ,
			        isLatestPersisted,
			notNull(obsoletes)
		);
	}
	
	
	public interface Initializer<T> extends SwizzleTypeLink<T>
	{
		public XGettingSequence<? extends PersistenceTypeDescriptionMember> members();
		
		public PersistenceTypeDescription<T> initialize(long typeId, XGettingTable<Long, PersistenceTypeDescription<T>> obsoletes);
	}
	
	public interface InitializerLookup
	{
		public <T> PersistenceTypeDescription.Initializer<T> lookupInitializer(String typeName);
		
		
		public final class Implementation implements PersistenceTypeDescription.InitializerLookup
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final PersistenceTypeHandlerEnsurerLookup<?> typeHandlerEnsurerLookup;
			private final SwizzleTypeManager                     typeManager             ;

			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			Implementation(
				final PersistenceTypeHandlerEnsurerLookup<?> typeHandlerEnsurerLookup,
				final SwizzleTypeManager                     typeManager
			)
			{
				super();
				this.typeHandlerEnsurerLookup = typeHandlerEnsurerLookup;
				this.typeManager              = typeManager             ;
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////

			@SuppressWarnings("unchecked")
			private static <T> Class<T> resolveType(final String typename)
			{
				try
				{
					return (Class<T>)JadothReflect.classForName(typename);
				}
				catch (final ClassNotFoundException e)
				{
					throw new RuntimeException(e); // (30.04.2017 TM)EXCP: proper exception
				}
			}
			
			@Override
			public <T> Initializer<T> lookupInitializer(final String typename)
			{
				final Class<T> type = resolveType(typename);
				
				final PersistenceTypeHandlerEnsurer<?> ensurer = this.typeHandlerEnsurerLookup.lookupEnsurer(type);
				
//				ensurer.ensureTypeHandler(type, 0, this.typeManager)
				
				throw new net.jadoth.meta.NotImplementedYetError(); // FIXME PersistenceTypeDescription.InitializerLookup#lookupInitializer()
			}
			
			
		}
		
	}

	public final class Implementation<T> implements PersistenceTypeDescription<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final long                                                           typeId           ;
		final String                                                         typeName         ;
		final Class<T>                                                       type             ;
		final XImmutableSequence<? extends PersistenceTypeDescriptionMember> members          ;
		final boolean                                                        hasReferences    ;
		final boolean                                                        isPrimitive      ;
		final boolean                                                        variableLength   ;
		final boolean                                                        isLatestPersisted;
		final PersistenceTypeDescription<T>                                  current          ;
		final XGettingTable<Long, PersistenceTypeDescription<T>>             obsoletes        ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(
			final long                                                         typeId           ,
			final String                                                       typeName         ,
			final Class<T>                                                     type             ,
			final XGettingSequence<? extends PersistenceTypeDescriptionMember> members          ,
			final PersistenceTypeDescription<T>                                current          ,
			final boolean                                                      isCurrent        ,
			final boolean                                                      isLatestPersisted,
			final XGettingTable<Long, PersistenceTypeDescription<T>>           obsoletes
		)
		{
			super();
			this.typeId            = typeId           ;
			this.typeName          = typeName         ;
			this.type              = type             ; // may be null for obsolete type description or external process
			this.members           = members.immure() ; // same instance if already immutable
			this.isLatestPersisted = isLatestPersisted;
			this.hasReferences     = PersistenceTypeDescriptionMember.determineHasReferences (members);
			this.isPrimitive       = PersistenceTypeDescriptionMember.determineIsPrimitive   (members);
			this.variableLength    = PersistenceTypeDescriptionMember.determineVariableLength(members);
			this.current           = current != null ? current : isCurrent ? this : null;
			this.obsoletes         = obsoletes       ;
		}

		

		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

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
		public final boolean isLatestPersisted()
		{
			return this.isLatestPersisted;
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
		public final PersistenceTypeDescription<T> current()
		{
			return this.current;
		}
		
		@Override
		public final XGettingTable<Long, PersistenceTypeDescription<T>> obsoletes()
		{
			return this.obsoletes;
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
	
	@FunctionalInterface
	public interface Builder
	{
		public <T> PersistenceTypeDescription<T> build(
			long                                                         typeId           ,
			String                                                       typeName         ,
			Class<T>                                                     type             ,
			XGettingSequence<? extends PersistenceTypeDescriptionMember> members          ,
			PersistenceTypeDescription<T>                                current          ,
			boolean                                                      isCurrent        ,
			boolean                                                      isLatestPersisted,
			XGettingTable<Long, PersistenceTypeDescription<T>>           obsoletes
		);
				
		public final class Implementation implements Builder
		{
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			Implementation()
			{
				super();
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////

			@Override
			public <T> PersistenceTypeDescription<T> build(
				final long                                                         typeId           ,
				final String                                                       typeName         ,
				final Class<T>                                                     type             ,
				final XGettingSequence<? extends PersistenceTypeDescriptionMember> members          ,
				final PersistenceTypeDescription<T>                                current          ,
				final boolean                                                      isCurrent        ,
				final boolean                                                      isLatestPersisted,
				final XGettingTable<Long, PersistenceTypeDescription<T>>           obsoletes
			)
			{
				return PersistenceTypeDescription.New(
					typeId, typeName, type, members, current, isCurrent, isLatestPersisted, obsoletes
				);
			}
			
		}
		
	}
	
}
