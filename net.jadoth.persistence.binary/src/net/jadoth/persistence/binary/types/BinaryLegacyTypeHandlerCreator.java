package net.jadoth.persistence.binary.types;

import static net.jadoth.X.notNull;

import java.lang.reflect.Field;

import net.jadoth.collections.HashTable;
import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.collections.types.XGettingMap;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.low.XVM;
import net.jadoth.persistence.types.PersistenceLegacyTypeHandler;
import net.jadoth.persistence.types.PersistenceLegacyTypeHandlerCreator;
import net.jadoth.persistence.types.PersistenceLegacyTypeMappingResult;
import net.jadoth.persistence.types.PersistenceTypeDefinition;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMember;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMemberField;
import net.jadoth.persistence.types.PersistenceTypeHandler;
import net.jadoth.persistence.types.PersistenceTypeHandlerReflective;

public interface BinaryLegacyTypeHandlerCreator extends PersistenceLegacyTypeHandlerCreator<Binary>
{
	public static BinaryLegacyTypeHandlerCreator New()
	{
		return new BinaryLegacyTypeHandlerCreator.Implementation(
			BinaryValueTranslatorProvider.New()
		);
	}
	
	public static BinaryLegacyTypeHandlerCreator New(final BinaryValueTranslatorProvider valueTranslatorProvider)
	{
		return new BinaryLegacyTypeHandlerCreator.Implementation(
			notNull(valueTranslatorProvider)
		);
	}
	
	public final class Implementation
	extends PersistenceLegacyTypeHandlerCreator.AbstractImplementation<Binary>
	implements BinaryLegacyTypeHandlerCreator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final BinaryValueTranslatorProvider valueTranslatorProvider;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(final BinaryValueTranslatorProvider valueTranslatorProvider)
		{
			super();
			this.valueTranslatorProvider = valueTranslatorProvider;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
					
		private static HashTable<PersistenceTypeDescriptionMember, Long> createBinaryOffsetMap(
			final XGettingEnum<? extends PersistenceTypeDescriptionMember> members
		)
		{
			final HashTable<PersistenceTypeDescriptionMember, Long> memberOffsets = HashTable.New();
			long totalOffset = 0;
			for(final PersistenceTypeDescriptionMember member : members)
			{
				memberOffsets.add(member, totalOffset);
				totalOffset += member.persistentMaximumLength();
			}
			
			return memberOffsets;
		}
				
		private static HashTable<PersistenceTypeDescriptionMember, Long> createFieldOffsetMap(
			final XGettingEnum<? extends PersistenceTypeDescriptionMemberField> members
		)
		{
			final HashTable<PersistenceTypeDescriptionMember, Long> memberOffsets = HashTable.New();
			for(final PersistenceTypeDescriptionMemberField member : members)
			{
				final Field field = notNull(member.field());
				final long fieldOffset = XVM.objectFieldOffset(field);
				memberOffsets.add(member, fieldOffset);
			}
			
			return memberOffsets;
		}
		
		private XGettingTable<BinaryValueSetter, Long> deriveValueTranslators(
			final PersistenceTypeDefinition<?>                                                    legacyTypeDefinition ,
			final XGettingMap<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> legacyToTargetMembers,
			final HashTable<PersistenceTypeDescriptionMember, Long>                               targetMemberOffsets  ,
			final boolean                                                                         resolveReferences
		)
		{
			final HashTable<BinaryValueSetter, Long> translatorsWithTargetOffsets = HashTable.New();
			
			final BinaryValueTranslatorProvider creator = this.valueTranslatorProvider;

			for(final PersistenceTypeDescriptionMember legacyMember : legacyTypeDefinition.members())
			{
				// currentMember null means the value is to be discarded.
				final PersistenceTypeDescriptionMember currentMember = legacyToTargetMembers.get(legacyMember);
				
				final BinaryValueSetter translator = legacyMember.isReference() && resolveReferences
					? creator.provideReferenceResolver(legacyMember, currentMember)
					: creator.providePrimitiveValueTranslator(legacyMember, currentMember)
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
				// (14.09.2018 TM)TODO: support VaryingPersistedLengthInstances
				throw new UnsupportedOperationException(
					"Types with varying persisted length are not supported, yet by generic mapping."
					+ " Use a custom handler."
				);
			}
			
			final HashTable<PersistenceTypeDescriptionMember, Long> targetMemberOffsets = createBinaryOffsetMap(
				mappingResult.currentTypeHandler().members()
			);
			
			final XGettingTable<BinaryValueSetter, Long> translatorsWithTargetOffsets = this.deriveValueTranslators(
				mappingResult.legacyTypeDefinition(),
				mappingResult.legacyToCurrentMembers(),
				targetMemberOffsets,
				false
			);
						
			return BinaryLegacyTypeHandlerRerouting.New(
				mappingResult.legacyTypeDefinition(),
				typeHandler                         ,
				translatorsWithTargetOffsets
			);
		}

		@Override
		protected <T> PersistenceLegacyTypeHandler<Binary, T> deriveReflectiveHandler(
			final PersistenceLegacyTypeMappingResult<Binary, T> mappingResult,
			final PersistenceTypeHandlerReflective<Binary, T>   typeHandler
		)
		{
			final HashTable<PersistenceTypeDescriptionMember, Long> targetMemberOffsets = createFieldOffsetMap(
				typeHandler.members()
			);
			
			final XGettingTable<BinaryValueSetter, Long> translatorsWithTargetOffsets = this.deriveValueTranslators(
				mappingResult.legacyTypeDefinition(),
				mappingResult.legacyToCurrentMembers(),
				targetMemberOffsets,
				true
			);
			
			return BinaryLegacyTypeHandlerReflective.New(
				mappingResult.legacyTypeDefinition(),
				typeHandler                         ,
				translatorsWithTargetOffsets
			);
		}
		
	}
}
