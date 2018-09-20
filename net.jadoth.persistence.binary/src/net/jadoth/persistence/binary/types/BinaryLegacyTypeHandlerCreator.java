package net.jadoth.persistence.binary.types;

import static net.jadoth.X.notNull;

import net.jadoth.collections.HashTable;
import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.collections.types.XGettingMap;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.persistence.types.PersistenceLegacyTypeHandler;
import net.jadoth.persistence.types.PersistenceLegacyTypeHandlerCreator;
import net.jadoth.persistence.types.PersistenceLegacyTypeMappingResult;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMember;
import net.jadoth.persistence.types.PersistenceTypeHandler;

public interface BinaryLegacyTypeHandlerCreator extends PersistenceLegacyTypeHandlerCreator<Binary>
{
	public static BinaryLegacyTypeHandlerCreator New()
	{
		return new BinaryLegacyTypeHandlerCreator.Implementation(
			BinaryValueTranslator.Creator()
		);
	}
	
	public static BinaryLegacyTypeHandlerCreator New(final BinaryValueTranslator.Creator valueTranslatorCreator)
	{
		return new BinaryLegacyTypeHandlerCreator.Implementation(
			notNull(valueTranslatorCreator)
		);
	}
	
	public final class Implementation
	extends PersistenceLegacyTypeHandlerCreator.AbstractImplementation<Binary>
	implements BinaryLegacyTypeHandlerCreator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final BinaryValueTranslator.Creator valueTranslatorCreator;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(final BinaryValueTranslator.Creator valueTranslatorCreator)
		{
			super();
			this.valueTranslatorCreator = valueTranslatorCreator;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
					
		private static HashTable<PersistenceTypeDescriptionMember, Long> createMemberOffsetMap(
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
		
		private XGettingTable<BinaryValueTranslator, Long> deriveValueTranslators(
			final PersistenceLegacyTypeMappingResult<Binary, ?> result
		)
		{
			final HashTable<PersistenceTypeDescriptionMember, Long> currentMemberOffsets = createMemberOffsetMap(
				result.currentTypeHandler().members()
			);
			
			final XGettingMap<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> legacyToCurrentMembers =
				result.legacyToCurrentMembers()
			;
			
			final HashTable<BinaryValueTranslator, Long> translatorsWithTargetOffsets = HashTable.New();
			
			final BinaryValueTranslator.Creator creator = this.valueTranslatorCreator;

			for(final PersistenceTypeDescriptionMember legacyMember : result.legacyTypeDefinition().members())
			{
				// currentMember null means the value is to be discarded.
				final PersistenceTypeDescriptionMember currentMember = legacyToCurrentMembers.get(legacyMember);
				final BinaryValueTranslator translator   = creator.createValueTranslator(legacyMember, currentMember);
				final Long                  targetOffset = currentMemberOffsets.get(currentMember);
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
			
			final XGettingTable<BinaryValueTranslator, Long> translatorsWithTargetOffsets =
				this.deriveValueTranslators(mappingResult)
			;
						
			return BinaryLegacyTypeHandlerRerouting.New(
				mappingResult.legacyTypeDefinition(),
				typeHandler                         ,
				translatorsWithTargetOffsets
			);
		}

		@Override
		protected <T> PersistenceLegacyTypeHandler<Binary, T> deriveReflectiveHandler(
			final PersistenceLegacyTypeMappingResult<Binary, T> result
		)
		{
			// (17.09.2018 TM)FIXME: OGS-3: deriveReflectiveHandler
			throw new net.jadoth.meta.NotImplementedYetError();
		}
		
	}
}
