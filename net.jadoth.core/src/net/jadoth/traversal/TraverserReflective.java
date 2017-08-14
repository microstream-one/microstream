package net.jadoth.traversal;

import static net.jadoth.Jadoth.notNull;

import java.lang.reflect.Field;
import java.util.function.Predicate;

import net.jadoth.collections.HashEnum;
import net.jadoth.functional.JadothPredicates;
import net.jadoth.reflect.JadothReflect;

public final class TraverserReflective<T> implements TypeTraverser<T>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final Class<T> type  ;
	private final Field[]  fields;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	TraverserReflective(final Class<T> type, final Field[] fields)
	{
		super();
		this.type   = type  ;
		this.fields = fields;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public final Class<T> type()
	{
		return this.type;
	}
	
	@Override
	public final void traverseReferences(
		final T          instance,
		final TraversalEnqueuer enqueuer,
		final TraversalAcceptor acceptor
	)
	{
		final Field[] fields = this.fields  ;
		final int     length = fields.length;
		
		try
		{
			for(int i = 0; i < length; i++)
			{
				final Object current;
				if(acceptor.acceptReference(current = JadothReflect.getFieldValue(fields[i], instance), instance))
				{
					enqueuer.enqueue(current);
				}
			}
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
	}
	
	@Override
	public final void traverseReferences(
		final T                 instance        ,
		final TraversalEnqueuer enqueuer        ,
		final TraversalMutator  mutator         ,
		final MutationListener  mutationListener
	)
	{
		final Field[] fields = this.fields  ;
		final int     length = fields.length;
		
		try
		{
			for(int i = 0; i < length; i++)
			{
				final Object current = JadothReflect.getFieldValue(fields[i], instance);
				final Object returned;
				if((returned = mutator.mutateReference(current, instance)) != current)
				{
					JadothReflect.setFieldValue(fields[i], instance, returned); // must be BEFORE registerChange
					if(mutationListener != null)
					{
						try
						{
							mutationListener.registerChange(instance, current, returned);
						}
						catch(final TraversalSignalSkipEnqueueReference s)
						{
							continue; // skip enqueue call (clever! 8-))
						}
					}
				}
				enqueuer.enqueue(returned);
			}
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
	}
	
	@Override
	public final void traverseReferences(
		final T                 instance        ,
		final TraversalEnqueuer enqueuer        ,
		final TraversalAcceptor acceptor        ,
		final TraversalMutator  mutator         ,
		final MutationListener  mutationListener
	)
	{
		final Field[] fields = this.fields  ;
		final int     length = fields.length;
		
		try
		{
			for(int i = 0; i < length; i++)
			{
				final Object current, returned;
				if(acceptor.acceptReference(current = JadothReflect.getFieldValue(fields[i], instance), instance))
				{
					enqueuer.enqueue(current);
				}
				
				if((returned = mutator.mutateReference(current, instance)) != current)
				{
					JadothReflect.setFieldValue(fields[i], instance, returned); // must be BEFORE registerChange
					if(mutationListener != null)
					{
						try
						{
							mutationListener.registerChange(instance, current, returned);
						}
						catch(final TraversalSignalSkipEnqueueReference s)
						{
							continue; // skip enqueue call (clever! 8-))
						}
					}
				}
				enqueuer.enqueue(returned);
			}
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
	}

	
	
	public static TraverserReflective.Creator Creator()
	{
		return new TraverserReflective.Creator(
			JadothPredicates.all()
		);
	}
	
	public static TraverserReflective.Creator Creator(final Predicate<? super Field> fieldSelector)
	{
		return new TraverserReflective.Creator(
			notNull(fieldSelector)
		);
	}
	
	public static final class Creator implements TypeTraverser.Creator
	{
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Predicate<? super Field> fieldSelector;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Creator(final Predicate<? super Field> fieldSelector)
		{
			super();
			this.fieldSelector = fieldSelector;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		private final Field[] collectFields(final Class<?> type)
		{
			final HashEnum<Field> selectedFields = HashEnum.New();
			JadothReflect.collectTypedFields(selectedFields, type, field ->
				JadothReflect.isInstanceField(field)
				&& this.fieldSelector.test(field))
			;
			
			selectedFields.iterate(f -> f.setAccessible(true));
			
			return selectedFields.toArray(Field.class);
		}
		
		protected final <T> TraverserReflective<T> internalCreateTraverser(final Class<T> type)
		{
			final Field[] collectedFields = this.collectFields(type);
			
			return collectedFields.length != 0
				? new TraverserReflective<>(type, collectedFields)
				: null
			;
		}
		
		@Override
		public final <T> TraverserReflective<T> createTraverser(final Class<T> type)
		{
			return this.internalCreateTraverser(type);
		}
		
	}
	
}
