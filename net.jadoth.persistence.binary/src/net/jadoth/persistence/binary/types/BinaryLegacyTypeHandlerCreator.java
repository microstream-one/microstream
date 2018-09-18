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
		
		private static long calculateBinaryContentLength(final PersistenceTypeHandler<Binary, ?> typeHandler)
		{
			long binaryContentLength = 0;
			for(final PersistenceTypeDescriptionMember e : typeHandler.members())
			{
				// returned length values are expected to never be more than 3-digit, so no overflow check needed.
				binaryContentLength += e.persistentMaximumLength();
			}
			
			return binaryContentLength;
		}
			
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
			// (18.09.2018 TM)FIXME: OGS-3: binary order offsets, not declaration order offsets!
			final HashTable<PersistenceTypeDescriptionMember, Integer> currentMemberOffsets = createMemberOffsetMap(
				result.currentTypeHandler().members()
			);
			
			final XGettingMap<PersistenceTypeDescriptionMember, PersistenceTypeDescriptionMember> legacyToCurrentMembers =
				result.legacyToCurrentMembers()
			;
			
			final BulkList<BinaryValueTranslator> translators = BulkList.New();

			/* (18.09.2018 TM)FIXME: OGS-3: binary order offsets, not declaration order offsets!
			 * Also see AbstractGenericBinaryHandler constructor note about confusing orders
			 */
			
			final int legacyTotalOffset = 0;
			for(final PersistenceTypeDescriptionMember legacyMember : result.legacyTypeDefinition().members())
			{
				final PersistenceTypeDescriptionMember currentMember = legacyToCurrentMembers.get(legacyMember);
				
				final BinaryValueTranslator valueTranslator = this.valueTranslatorCreator.createValueTranslator(
					legacyMember,
					legacyTotalOffset,
					currentMember,
					currentMemberOffsets.get(currentMember)
				);
				translators.add(valueTranslator);
			}
			
			return translators;
		}
		
		@Override
		protected <T> PersistenceLegacyTypeHandler<Binary, T> deriveCustomWrappingHandler(
			final PersistenceLegacyTypeMappingResult<Binary, T> mappingResult
		)
		{
			final PersistenceTypeHandler<Binary, T> typeHandler = mappingResult.currentTypeHandler();
			if(typeHandler.hasVaryingPersistedLengthInstances())
			{
				// (14.09.2018 TM)TODO: support VaryingPersistedLengthInstances
				throw new UnsupportedOperationException(
					"Types with instances of varying persisted length are not supported, yet by generic mapping."
				);
			}
			
			// (18.09.2018 TM)FIXME: OGS-3: already available in the TypeHandler. Resolve API conflict on Persistence level.
			final long binaryTotalLength = BinaryPersistence.entityTotalLength(
				calculateBinaryContentLength(typeHandler)
			);
						
			return BinaryLegacyTypeTranslatingMapper.New(
				mappingResult.legacyTypeDefinition()      ,
				typeHandler                               ,
				this.deriveValueTranslators(mappingResult),
				binaryTotalLength
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
