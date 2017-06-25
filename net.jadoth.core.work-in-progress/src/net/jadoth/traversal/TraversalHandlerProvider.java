package net.jadoth.traversal;

import static net.jadoth.Jadoth.notNull;

import java.lang.reflect.Field;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import net.jadoth.collections.HashTable;
import net.jadoth.collections.types.XGettingMap;
import net.jadoth.reflect.JadothReflect;
import net.jadoth.traversal.handlers.TraversalHandlerArray;
import net.jadoth.traversal.handlers.TraversalHandlerLeaf;
import net.jadoth.traversal.handlers.TraversalHandlerReflective;
import net.jadoth.traversal.TraversalHandler;
import net.jadoth.traversal.TraversalHandlerProvider;
import net.jadoth.util.KeyValue;
import net.jadoth.util.branching.ThrowBreak;

@FunctionalInterface
public interface TraversalHandlerProvider
{
	public <T> TraversalHandler<T> provideTraversalHandler(final Class<? extends T> type);




	public static TraversalHandlerProvider New(
		final TraversalHandlingLogicProvider                                     handlingLogicProvider            ,
		final BiPredicate<Class<?>, ? super Field>                               traversableFieldSelector         ,
		final XGettingMap<Class<?>, ? extends TraversalHandlerCustomProvider<?>> specificTraversalHandlerProviders
	)
	{
		return new TraversalHandlerProvider.ReflectiveAnalyzer(
			notNull(handlingLogicProvider)                     ,
			traversableFieldSelector                           , // may be null
			HashTable.New(specificTraversalHandlerProviders)
		);
	}

	public final class ReflectiveAnalyzer implements TraversalHandlerProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final TraversalHandlingLogicProvider                         handlingLogicProvider            ;
		private final BiPredicate<Class<?>, ? super Field>                   traversableFieldSelector         ;
		private final HashTable<Class<?>, TraversalHandlerCustomProvider<?>> specificTraversalHandlerProviders;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		ReflectiveAnalyzer(
			final TraversalHandlingLogicProvider                         handlingLogicProvider            ,
			final BiPredicate<Class<?>, ? super Field>                   traversableFieldSelector         ,
			final HashTable<Class<?>, TraversalHandlerCustomProvider<?>> specificTraversalHandlerProviders
		)
		{
			super();
			this.handlingLogicProvider                = handlingLogicProvider               ;
			this.traversableFieldSelector             = traversableFieldSelector            ;
			this.specificTraversalHandlerProviders    = specificTraversalHandlerProviders   ;
		}


		/**
		 * Arrays must be special-cased. Both unhandled reference arrays and especially primitive arrays.
		 * 
		 * @param type
		 * @param handlingLogic
		 * @return
		 */
		static final <T> TraversalHandler<T> checkForArrays(
			final Class<? extends T>   type         ,
			final Predicate<? super T> handlingLogic
		)
		{
			/*
			 * special case for arrays. Must be hardcoded as a fallback to prevent array types from being
			 * analyzed as normal types.
			 */
			if(type.isArray())
			{
				// primitive arrays might still be viable (leaf) subjects for the handling logic
				if(type.getComponentType().isPrimitive())
				{
					return handlingLogic != null
						? TraversalHandlerLeaf.New(handlingLogic)
						: null
					;
				}

				@SuppressWarnings("unchecked") // cast safety guaranteed by array checking logic above
				final TraversalHandler<T> arrayHandler =
					(TraversalHandler<T>)TraversalHandlerArray.New((Predicate<Object[]>)handlingLogic)
				;
				return arrayHandler;
			}

			// no handled special case. Returned null indicates generic case.
			return null;
		}
		
		/*
		 * The handler map generifies all type information to "?".
		 * This method is required to compensate that.
		 * The safety of the cast is guaranteed by the logic that populates the map.
		 */
		@SuppressWarnings("unchecked")
		private static <T> TraversalHandlerCustomProvider<T> normalizeTypeParamter(
			final TraversalHandlerCustomProvider<?> handlerProvider
		)
		{
			return (TraversalHandlerCustomProvider<T>)handlerProvider;
		}

		@Override
		public <T> TraversalHandler<T> provideTraversalHandler(final Class<? extends T> type)
		{
			// check for specific handling being registered for the type
			final TraversalHandlerCustomProvider<T> customHandlerProvider = normalizeTypeParamter(
				this.specificTraversalHandlerProviders.get(type)
			);
			if(customHandlerProvider != null)
			{
				// the specific provider might use the passed handlingLogicProvider, but doesn't have to.
				return customHandlerProvider.provideTraversalHandler(type, this.handlingLogicProvider);
			}
			
			// cannot be done by hash lookup as the types used as keys are interpreted polymorphical
			for(final KeyValue<Class<?>, ? extends TraversalHandlerCustomProvider<?>> e : this.specificTraversalHandlerProviders)
			{
				if(e.key().isAssignableFrom(type))
				{
					final TraversalHandlerCustomProvider<T> provider = normalizeTypeParamter(e.value());
					return provider.provideTraversalHandler(type, this.handlingLogicProvider);
				}
			}

			// may be null if the instance type shall only be traversed but not handled
			final Predicate<? super T> handlingLogic = this.handlingLogicProvider.provideHandlingLogic(type);
			
			// outsourced check for special cases
			final TraversalHandler<T> specialCaseHandler = checkForArrays(type, handlingLogic);
			if(specialCaseHandler != null)
			{
				return specialCaseHandler;
			}

			// generic case: traversable fields are collected and a generic handler is created
			try
			{
				// field selector is simplified if no custom selector is present
				final Predicate<Field> traversableFieldSelector = this.traversableFieldSelector != null
					? field ->
						JadothReflect.isInstanceReferenceField(field)
						&& this.traversableFieldSelector.test(type, field)
					: JadothReflect::isInstanceReferenceField
				;
				final Field[] traversableFields = JadothReflect.queryAllFields(type, traversableFieldSelector);

				// instance is created even if no relevant fields are found in order to still apply the handling logic
				return traversableFields.length == 0
					? handlingLogic == null
						? null
						: TraversalHandlerLeaf.New(handlingLogic)
					: TraversalHandlerReflective.New(handlingLogic, traversableFields)
				;
			}
			catch(final ThrowBreak e)
			{
				// the field selector logic indicated to skip the type completely, hence return null
				return null;
			}
		}

	}

}
