package net.jadoth.graphConsolidator;

import java.util.function.Predicate;

import net.jadoth.traversal.TraversalHandlingLogicProvider;

public interface EntitySubjectSubstitutionHandlerProvider extends TraversalHandlingLogicProvider
{
	public final class Implementation implements EntitySubjectSubstitutionHandlerProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final SubjectSubstitutorProvider subjectSubstitutorProvider;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(final SubjectSubstitutorProvider subjectSubstitutorProvider)
		{
			super();
			this.subjectSubstitutorProvider = subjectSubstitutorProvider;
		}

		
		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////
		
		static final <E> Predicate<? super E> checkForSpecialCases(final Class<? extends E> entityType)
		{
			// (10.04.2016)TODO: special cases or lookup reference
			return null;
		}
		

		@Override
		public <E> Predicate<? super E> provideHandlingLogic(final Class<? extends E> entityType)
		{
			final Predicate<? super E> specialCaseLogic = checkForSpecialCases(entityType);
			if(specialCaseLogic != null)
			{
				return specialCaseLogic;
			}
			
			if(entityType.isArray())
			{
				if(entityType.getComponentType().isPrimitive())
				{
					// a primitive array has no members that could be substituted
					return null;
				}
				@SuppressWarnings("unchecked")
				final Predicate<? super E> logic = (Predicate<? super E>)new EntitySubjectSubstitutionHandler.GenericArray(
					this.subjectSubstitutorProvider
				);
				return logic;
			}
			
			/* (10.04.2016)FIXME: EntitySubjectSubstitutionHandlerProvider for generic field case
			 * - select relevant fields
			 * - return generic field handler or null depending on selection
			 * 
			 */
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME EntitySubjectSubstitutionHandlerProvider.Implementation#provideHandlingLogic()
		}
		
	}
}
