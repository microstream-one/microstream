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
import one.microstream.persistence.types.PersistenceLegacyTypeHandlingListener;
import one.microstream.persistence.types.PersistenceLegacyTypeMappingResult;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberField;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.persistence.types.PersistenceTypeHandlerReflective;

public interface BinaryLegacyTypeHandlerCreator extends PersistenceLegacyTypeHandlerCreator<Binary>
{
	public static BinaryLegacyTypeHandlerCreator New(
		final BinaryValueTranslatorProvider                 valueTranslatorProvider   ,
		final PersistenceLegacyTypeHandlingListener<Binary> legacyTypeHandlingListener,
		final boolean                                       switchByteOrder
	)
	{
		return new BinaryLegacyTypeHandlerCreator.Implementation(
			notNull(valueTranslatorProvider)   ,
			mayNull(legacyTypeHandlingListener),
			switchByteOrder
		);
	}
	
	public final class Implementation
	extends PersistenceLegacyTypeHandlerCreator.AbstractImplementation<Binary>
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
		
		Implementation(
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
			final XGettingEnum<? extends PersistenceTypeDefinitionMemberField> members
		)
		{
			final HashTable<PersistenceTypeDefinitionMember, Long> memberOffsets = HashTable.New();
			for(final PersistenceTypeDefinitionMemberField member : members)
			{
				final Field field = notNull(member.field());
				final long fieldOffset = XMemory.objectFieldOffset(field);
				memberOffsets.add(member, fieldOffset);
			}
			
			return memberOffsets;
		}
		
		private XGettingTable<Long, BinaryValueSetter> deriveValueTranslators(
			final PersistenceTypeDefinition                                                     legacyTypeDefinition ,
			final PersistenceTypeHandler<Binary, ?>                                             currentTypeHandler   ,
			final XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> legacyToTargetMembers,
			final HashTable<PersistenceTypeDefinitionMember, Long>                              targetMemberOffsets  ,
			final boolean                                                                       resolveReferences
		)
		{
			final EqHashTable<Long, BinaryValueSetter> translatorsWithTargetOffsets = EqHashTable.New();
			
			final BinaryValueTranslatorProvider creator = this.valueTranslatorProvider;

			for(final PersistenceTypeDefinitionMember legacyMember : legacyTypeDefinition.members())
			{
				// currentMember null means the value is to be discarded.
				final PersistenceTypeDefinitionMember currentMember = legacyToTargetMembers.get(legacyMember);
				
				final BinaryValueSetter translator = resolveReferences
					? creator.provideTargetValueTranslator(legacyTypeDefinition, legacyMember, currentTypeHandler, currentMember)
					: creator.provideBinaryValueTranslator(legacyTypeDefinition, legacyMember, currentTypeHandler, currentMember)
				;
				final Long targetOffset = targetMemberOffsets.get(currentMember);
				translatorsWithTargetOffsets.add(targetOffset, translator);
			}
			
			return translatorsWithTargetOffsets;
		}
		
		@Override
		protected <T> PersistenceLegacyTypeHandler<Binary, T> deriveCustomWrappingHandler(
			final PersistenceLegacyTypeMappingResult<Binary, T> mappingResult
		)
		{
			final PersistenceTypeHandler<Binary, T> typeHandler = mappingResult.currentTypeHandler();
			if(typeHandler.hasPersistedVariableLength())
			{
				// (14.09.2018 TM)TODO: Legacy Type Mapping: support VaryingPersistedLengthInstances
				throw new UnsupportedOperationException(
					"Types with varying persisted length are not supported, yet by generic mapping."
					+ " Use a custom handler for type" + typeHandler.toRuntimeTypeIdentifier()
				);
			}
			
			final HashTable<PersistenceTypeDefinitionMember, Long> targetMemberOffsets = createBinaryOffsetMap(
				mappingResult.currentTypeHandler().members()
			);
			
			final XGettingTable<Long, BinaryValueSetter> translatorsWithTargetOffsets = this.deriveValueTranslators(
				mappingResult.legacyTypeDefinition()  ,
				mappingResult.currentTypeHandler()    ,
				mappingResult.legacyToCurrentMembers(),
				targetMemberOffsets,
				false
			);
						
			return BinaryLegacyTypeHandlerRerouting.New(
				mappingResult.legacyTypeDefinition(),
				typeHandler                         ,
				translatorsWithTargetOffsets        ,
				this.legacyTypeHandlingListener     ,
				this.switchByteOrder
			);
		}

		@Override
		protected <T> PersistenceLegacyTypeHandler<Binary, T> deriveReflectiveHandler(
			final PersistenceLegacyTypeMappingResult<Binary, T> mappingResult,
			final PersistenceTypeHandlerReflective<Binary, T>   typeHandler
		)
		{
			final HashTable<PersistenceTypeDefinitionMember, Long> targetMemberOffsets = createFieldOffsetMap(
				typeHandler.members()
			);
			
			final XGettingTable<Long, BinaryValueSetter> translatorsWithTargetOffsets = this.deriveValueTranslators(
				mappingResult.legacyTypeDefinition()  ,
				mappingResult.currentTypeHandler()    ,
				mappingResult.legacyToCurrentMembers(),
				targetMemberOffsets,
				true
			);
			
			return BinaryLegacyTypeHandlerReflective.New(
				mappingResult.legacyTypeDefinition(),
				typeHandler                         ,
				translatorsWithTargetOffsets        ,
				this.legacyTypeHandlingListener     ,
				this.switchByteOrder
			);
		}
		
	}
}
