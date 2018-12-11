package net.jadoth.persistence.binary.types;

import static net.jadoth.X.mayNull;
import static net.jadoth.X.notNull;

import java.lang.reflect.Field;

import net.jadoth.collections.HashTable;
import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.collections.types.XGettingMap;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.low.XMemory;
import net.jadoth.persistence.types.PersistenceLegacyTypeHandler;
import net.jadoth.persistence.types.PersistenceLegacyTypeHandlerCreator;
import net.jadoth.persistence.types.PersistenceLegacyTypeHandlingListener;
import net.jadoth.persistence.types.PersistenceLegacyTypeMappingResult;
import net.jadoth.persistence.types.PersistenceTypeDefinition;
import net.jadoth.persistence.types.PersistenceTypeDefinitionMember;
import net.jadoth.persistence.types.PersistenceTypeDefinitionMemberField;
import net.jadoth.persistence.types.PersistenceTypeHandler;
import net.jadoth.persistence.types.PersistenceTypeHandlerReflective;

public interface BinaryLegacyTypeHandlerCreator extends PersistenceLegacyTypeHandlerCreator<Binary>
{
	public static BinaryLegacyTypeHandlerCreator New(
		final BinaryValueTranslatorProvider                 valueTranslatorProvider   ,
		final PersistenceLegacyTypeHandlingListener<Binary> legacyTypeHandlingListener
	)
	{
		return new BinaryLegacyTypeHandlerCreator.Implementation(
			notNull(valueTranslatorProvider),
			mayNull(legacyTypeHandlingListener)
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
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final BinaryValueTranslatorProvider                 valueTranslatorProvider   ,
			final PersistenceLegacyTypeHandlingListener<Binary> legacyTypeHandlingListener
		)
		{
			super();
			this.valueTranslatorProvider    = valueTranslatorProvider   ;
			this.legacyTypeHandlingListener = legacyTypeHandlingListener;
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
		
		private XGettingTable<BinaryValueSetter, Long> deriveValueTranslators(
			final PersistenceTypeDefinition                                                     legacyTypeDefinition ,
			final PersistenceTypeHandler<Binary, ?>                                             currentTypeHandler   ,
			final XGettingMap<PersistenceTypeDefinitionMember, PersistenceTypeDefinitionMember> legacyToTargetMembers,
			final HashTable<PersistenceTypeDefinitionMember, Long>                              targetMemberOffsets  ,
			final boolean                                                                       resolveReferences
		)
		{
			final HashTable<BinaryValueSetter, Long> translatorsWithTargetOffsets = HashTable.New();
			
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
				translatorsWithTargetOffsets.add(translator, targetOffset);
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
					+ " Use a custom handler."
				);
			}
			
			final HashTable<PersistenceTypeDefinitionMember, Long> targetMemberOffsets = createBinaryOffsetMap(
				mappingResult.currentTypeHandler().members()
			);
			
			final XGettingTable<BinaryValueSetter, Long> translatorsWithTargetOffsets = this.deriveValueTranslators(
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
				this.legacyTypeHandlingListener
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
			
			final XGettingTable<BinaryValueSetter, Long> translatorsWithTargetOffsets = this.deriveValueTranslators(
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
				this.legacyTypeHandlingListener
			);
		}
		
	}
}
