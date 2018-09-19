package net.jadoth.persistence.binary.types;

import static net.jadoth.X.notNull;

import net.jadoth.collections.BulkList;
import net.jadoth.collections.HashTable;
import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.collections.types.XGettingMap;
import net.jadoth.collections.types.XGettingSequence;
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
					
		private static HashTable<PersistenceTypeDescriptionMember, Integer> createMemberOffsetMap(
			final XGettingEnum<? extends PersistenceTypeDescriptionMember> members
		)
		{
			final HashTable<PersistenceTypeDescriptionMember, Integer> memberOffsets = HashTable.New();
			int totalOffset = 0;
			for(final PersistenceTypeDescriptionMember member : members)
			{
				memberOffsets.add(member, totalOffset);
				totalOffset += member.persistentMaximumLength();
			}
			
			return memberOffsets;
		}
		
		private XGettingSequence<BinaryValueTranslator> deriveValueTranslators(
			final PersistenceLegacyTypeMappingResult<Binary, ?> result
		)
		{
			final HashTable<PersistenceTypeDescriptionMember, Integer> currentMemberOffsets = createMemberOffsetMap(
				result.currentTypeHandler().members()
			);
			
			final XGettingMap<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> legacyToCurrentMembers =
				result.legacyToCurrentMembers()
			;
			
			final BulkList<BinaryValueTranslator> translators = BulkList.New();
			
			// (19.09.2018 TM)FIXME: OGS-3: refactor to function stateless, like BinaryValueSetter?
			
			int legacyTotalOffset = 0;
			for(final PersistenceTypeDescriptionMember legacyMember : result.legacyTypeDefinition().members())
			{
				final PersistenceTypeDescriptionMember currentMember = legacyToCurrentMembers.get(legacyMember);
								
				// a legacy member might be unmapped (e.g. the value is discarded).
				if(currentMember != null)
				{
					final BinaryValueTranslator valueTranslator = this.valueTranslatorCreator.createValueTranslator(
						legacyMember,
						legacyTotalOffset,
						currentMember,
						currentMemberOffsets.get(currentMember)
					);
					translators.add(valueTranslator);
				}
				
				// total offset mus be incremented in any case, including deleted member / discarded value
				legacyTotalOffset += legacyMember.persistentMaximumLength();
			}
			
			return translators;
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
						
			return BinaryLegacyTypeTranslatingMapper.New(
				mappingResult.legacyTypeDefinition()      ,
				typeHandler                               ,
				this.deriveValueTranslators(mappingResult),
				typeHandler.membersPersistedLengthMaximum()
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
