package one.microstream.persistence.binary.types;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import java.lang.reflect.Field;

import one.microstream.collections.EqHashTable;
import one.microstream.collections.HashTable;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingMap;
import one.microstream.collections.types.XGettingTable;
import one.microstream.memory.XMemory;
import one.microstream.persistence.types.PersistenceLegacyTypeHandler;
import one.microstream.persistence.types.PersistenceLegacyTypeHandlerCreator;
import one.microstream.persistence.types.PersistenceLegacyTypeHandlerWrapperEnum;
import one.microstream.persistence.types.PersistenceLegacyTypeHandlingListener;
import one.microstream.persistence.types.PersistenceLegacyTypeMappingResult;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberFieldReflective;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.persistence.types.PersistenceTypeHandlerReflective;
import one.microstream.reflect.XReflect;
import one.microstream.util.similarity.Similarity;

public interface BinaryLegacyTypeHandlerCreator extends PersistenceLegacyTypeHandlerCreator<Binary>
{
	public static BinaryLegacyTypeHandlerCreator New(
		final BinaryValueTranslatorProvider                 valueTranslatorProvider   ,
		final PersistenceLegacyTypeHandlingListener<Binary> legacyTypeHandlingListener,
		final boolean                                       switchByteOrder
	)
	{
		return new BinaryLegacyTypeHandlerCreator.Default(
			notNull(valueTranslatorProvider)   ,
			mayNull(legacyTypeHandlingListener),
			switchByteOrder
		);
	}
	
	public final class Default
	extends PersistenceLegacyTypeHandlerCreator.Abstract<Binary>
	implements BinaryLegacyTypeHandlerCreator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final BinaryValueTranslatorProvider                 valueTranslatorProvider   ;
		private final PersistenceLegacyTypeHandlingListener<Binary> legacyTypeHandlingListener;
		private final boolean                                       switchByteOrder           ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final BinaryValueTranslatorProvider                 valueTranslatorProvider   ,
			final PersistenceLegacyTypeHandlingListener<Binary> legacyTypeHandlingListener,
			final boolean                                       switchByteOrder
		)
		{
			super();
			this.valueTranslatorProvider    = valueTranslatorProvider   ;
			this.legacyTypeHandlingListener = legacyTypeHandlingListener;
			this.switchByteOrder            = switchByteOrder           ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
					
		private static HashTable<PersistenceTypeDefinitionMember, Long> createBinaryOffsetMap(
			final XGettingEnum<? extends PersistenceTypeDefinitionMember> members
		)
		{
			final HashTable<PersistenceTypeDefinitionMember, Long> memberOffsets = HashTable.New();
			long totalOffset = 0;
			for(final PersistenceTypeDefinitionMember member : members)
			{
				memberOffsets.add(member, totalOffset);
				totalOffset += member.persistentMaximumLength();
			}
			
			return memberOffsets;
		}
				
		private static HashTable<PersistenceTypeDefinitionMember, Long> createFieldOffsetMap(
			final XGettingEnum<? extends PersistenceTypeDefinitionMemberFieldReflective> members
		)
		{
			final HashTable<PersistenceTypeDefinitionMember, Long> memberOffsets = HashTable.New();
			for(final PersistenceTypeDefinitionMemberFieldReflective member : members)
			{
				final Field field = notNull(member.field());
				final long fieldOffset = XMemory.objectFieldOffset(field);
				memberOffsets.add(member, fieldOffset);
			}
			
			return memberOffsets;
		}
		
		private XGettingTable<Long, BinaryValueSetter> deriveValueTranslators(
			final PersistenceTypeDefinition                                                                 legacyTypeDefinition ,
			final PersistenceTypeHandler<Binary, ?>                                                         currentTypeHandler   ,
			final XGettingMap<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> legacyToTargetMembers,
			final HashTable<PersistenceTypeDefinitionMember, Long>                                          targetMemberOffsets  ,
			final boolean                                                                                   resolveReferences
		)
		{
			final EqHashTable<Long, BinaryValueSetter> translatorsWithTargetOffsets = EqHashTable.New();
			
			final BinaryValueTranslatorProvider creator = this.valueTranslatorProvider;

			for(final PersistenceTypeDefinitionMember legacyMember : legacyTypeDefinition.instanceMembers())
			{
				// currentMember null means the value is to be discarded.
				final PersistenceTypeDefinitionMember currentMember = Similarity.targetElement(
					legacyToTargetMembers.get(legacyMember)
				);
				
				final BinaryValueSetter translator = resolveReferences
					? creator.provideTargetValueTranslator(legacyTypeDefinition, legacyMember, currentTypeHandler, currentMember)
					: creator.provideBinaryValueTranslator(legacyTypeDefinition, legacyMember, currentTypeHandler, currentMember)
				;
					
				// can be null if the currentMember is not a settingMember. Must be discarded here, then.
				final Long targetOffset = targetMemberOffsets.get(currentMember);
				if(targetOffset != null)
				{
					translatorsWithTargetOffsets.add(targetOffset, translator);
				}
			}
			
			return translatorsWithTargetOffsets;
		}
		
		@Override
		protected <T> PersistenceLegacyTypeHandler<Binary, T> deriveCustomWrappingHandler(
			final PersistenceLegacyTypeMappingResult<Binary, T> mappingResult
		)
		{
			final PersistenceTypeHandler<Binary, T> currentTypeHandler = mappingResult.currentTypeHandler();
			if(currentTypeHandler.hasPersistedVariableLength())
			{
				// (14.09.2018 TM)TODO: Legacy Type Mapping: support VaryingPersistedLengthInstances
				throw new UnsupportedOperationException(
					"Types with varying persisted length are not supported, yet by generic mapping."
					+ " Use a custom handler for type" + currentTypeHandler.toRuntimeTypeIdentifier()
				);
			}
			
			final HashTable<PersistenceTypeDefinitionMember, Long> targetMemberOffsets = createBinaryOffsetMap(
				mappingResult.currentTypeHandler().instanceMembers()
			);
			
			final XGettingTable<Long, BinaryValueSetter> translatorsWithTargetOffsets = this.deriveValueTranslators(
				mappingResult.legacyTypeDefinition()  ,
				mappingResult.currentTypeHandler()    ,
				mappingResult.legacyToCurrentMembers(),
				targetMemberOffsets,
				false
			);
			
			final BinaryLegacyTypeHandlerRerouting<T> reroutingTypeHandler = BinaryLegacyTypeHandlerRerouting.New(
				mappingResult.legacyTypeDefinition(),
				currentTypeHandler                  ,
				translatorsWithTargetOffsets        ,
				this.legacyTypeHandlingListener     ,
				this.switchByteOrder
			);
			
			if(XReflect.isEnum(currentTypeHandler.type()))
			{
				return this.deriveCustomWrappingHandlerEnum(mappingResult, reroutingTypeHandler);
			}
						
			return reroutingTypeHandler;
		}
		
		protected <T> PersistenceLegacyTypeHandler<Binary, T> deriveCustomWrappingHandlerEnum(
			final PersistenceLegacyTypeMappingResult<Binary, T> mappingResult,
			final BinaryLegacyTypeHandlerRerouting<T> reroutingTypeHandler
		)
		{
			if(PersistenceLegacyTypeMappingResult.isUnchangedStaticStructure(mappingResult))
			{
				/*
				 * Tricky:
				 * The current custom type handler can be assumed to already be a proper enum handler
				 * (since it must be), so for unchanged static (enum constant) structure, the rerouting
				 * handler alone should already suffice. So fall through to returning it.
				 */
				return reroutingTypeHandler;
			}
			
			// "almost sufficient" reroutingTypeHandler has to be wrapped with an ordinal mapping
			final Integer[] ordinalMapping = deriveEnumOrdinalMapping(mappingResult);
			
			return PersistenceLegacyTypeHandlerWrapperEnum.New(
				mappingResult.legacyTypeDefinition(),
				reroutingTypeHandler,
				ordinalMapping
			);
		}

		@Override
		protected <T> AbstractBinaryLegacyTypeHandlerReflective<T> deriveReflectiveHandler(
			final PersistenceLegacyTypeMappingResult<Binary, T> mappingResult,
			final PersistenceTypeHandlerReflective<Binary, T>   currentTypeHandler
		)
		{
			// May only use setting members here, since legacy type mapping is only about setting values, not storing.
			final HashTable<PersistenceTypeDefinitionMember, Long> targetMemberOffsets = createFieldOffsetMap(
				currentTypeHandler.settingMembers()
			);
			
			final XGettingTable<Long, BinaryValueSetter> valueTranslators = this.deriveValueTranslators(
				mappingResult.legacyTypeDefinition(),
				mappingResult.currentTypeHandler(),
				mappingResult.legacyToCurrentMembers(),
				targetMemberOffsets,
				true
			);
			
			return XReflect.isEnum(currentTypeHandler.type())
				? this.deriveReflectiveHandlerGenericEnum(mappingResult, currentTypeHandler, valueTranslators)
				: this.deriveReflectiveHandlerGenericType(mappingResult, currentTypeHandler, valueTranslators)
			;
		}
		
		private <T> AbstractBinaryLegacyTypeHandlerReflective<T> deriveReflectiveHandlerGenericEnum(
			final PersistenceLegacyTypeMappingResult<Binary, T> mappingResult               ,
			final PersistenceTypeHandler<Binary, T>             currentTypeHandler          ,
			final XGettingTable<Long, BinaryValueSetter>        translatorsWithTargetOffsets
		)
		{
			if(PersistenceLegacyTypeMappingResult.isUnchangedStaticStructure(mappingResult))
			{
				return BinaryLegacyTypeHandlerGenericEnum.New(
					mappingResult.legacyTypeDefinition(),
					currentTypeHandler,
					translatorsWithTargetOffsets,
					this.legacyTypeHandlingListener,
					this.switchByteOrder
				);
			}
			
			final Integer[] ordinalMapping = deriveEnumOrdinalMapping(mappingResult);
			
			return BinaryLegacyTypeHandlerGenericEnumMapped.New(
				mappingResult.legacyTypeDefinition(),
				currentTypeHandler,
				translatorsWithTargetOffsets,
				ordinalMapping,
				this.legacyTypeHandlingListener,
				this.switchByteOrder
			);
		}
		
		private <T> BinaryLegacyTypeHandlerGenericType<T> deriveReflectiveHandlerGenericType(
			final PersistenceLegacyTypeMappingResult<Binary, T> mappingResult               ,
			final PersistenceTypeHandlerReflective<Binary, T>   currentTypeHandler          ,
			final XGettingTable<Long, BinaryValueSetter>        translatorsWithTargetOffsets
		)
		{
			return BinaryLegacyTypeHandlerGenericType.New(
				mappingResult.legacyTypeDefinition(),
				currentTypeHandler,
				translatorsWithTargetOffsets,
				this.legacyTypeHandlingListener,
				this.switchByteOrder
			);
		}
		
	}
	
}
