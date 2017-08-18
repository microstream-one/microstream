package net.jadoth.traversal;

import java.lang.reflect.Field;

import net.jadoth.collections.HashEnum;
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
		final T                 instance,
		final TraversalEnqueuer enqueuer
	)
	{
		final Field[] fields = this.fields  ;
		final int     length = fields.length;
		
		for(int i = 0; i < length; i++)
		{
			enqueuer.enqueue(JadothReflect.getFieldValue(fields[i], instance));
		}
	}
	
	@Override
	public final void traverseReferences(
		final T                 instance,
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
				final Object current, returned;
				enqueuer.enqueue(current = JadothReflect.getFieldValue(fields[i], instance));
				if((returned = mutator.mutateReference(current, instance)) != current)
				{
					if(mutationListener != null)
					{
						if(mutationListener.registerChange(instance, current, returned))
						{
							enqueuer.enqueue(returned);
						}
					}
					// actual setting must occur at the end for consistency with collection handling
					JadothReflect.setFieldValue(fields[i], instance, returned);
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
					if(mutationListener != null)
					{
						if(mutationListener.registerChange(instance, current, returned))
						{
							enqueuer.enqueue(returned);
						}
					}
					// actual setting must occur at the end for consistency with collection handling
					JadothReflect.setFieldValue(fields[i], instance, returned);
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
		final T                 instance,
		final TraversalAcceptor acceptor
	)
	{
		final Field[] fields = this.fields  ;
		final int     length = fields.length;
		
		try
		{
			for(int i = 0; i < length; i++)
			{
				acceptor.acceptReference(JadothReflect.getFieldValue(fields[i], instance), instance);
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
				final Object current = JadothReflect.getFieldValue(fields[i], instance), returned;
				if((returned = mutator.mutateReference(current, instance)) != current)
				{
					if(mutationListener != null)
					{
						mutationListener.registerChange(instance, current, returned);
					}
					// actual setting must occur at the end for consistency with collection handling
					JadothReflect.setFieldValue(fields[i], instance, returned);
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
				acceptor.acceptReference(current = JadothReflect.getFieldValue(fields[i], instance), instance);
				if((returned = mutator.mutateReference(current, instance)) != current)
				{
					if(mutationListener != null)
					{
						mutationListener.registerChange(instance, current, returned);
					}
					// actual setting must occur at the end for consistency with collection handling
					JadothReflect.setFieldValue(fields[i], instance, returned);
				}
			}
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
	}

	
	
	public static TraverserReflective.Creator Creator()
	{
		return Creator(null); // force default
	}
	
	public static TraverserReflective.Creator Creator(final TraversalFieldSelector fieldSelector)
	{
		// by default, only references are handled
		return new TraverserReflective.Creator(
			fieldSelector != null
			? fieldSelector
			: TraversalFieldSelector.New(JadothReflect::isReference)
		);
	}
	
	public static final class Creator implements TypeTraverser.Creator
	{
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final TraversalFieldSelector fieldSelector;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Creator(final TraversalFieldSelector fieldSelector)
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
				&& this.fieldSelector.test(type, field))
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
