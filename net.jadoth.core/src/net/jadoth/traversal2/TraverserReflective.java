package net.jadoth.traversal2;

import java.lang.reflect.Field;

import net.jadoth.reflect.JadothReflect;

public final class TraverserReflective implements TraversalHandler
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final Class<?> type  ;
	private final Field[]  fields;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	TraverserReflective(final Class<?> type, final Field[] fields)
	{
		super();
		this.type   = type  ;
		this.fields = fields;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public final Class<?> type()
	{
		return this.type;
	}

	@Override
	public final void traverseReferences(
		final Object            instance,
		final TraversalAcceptor acceptor,
		final TraversalEnqueuer enqueuer
	)
	{
		final Field[] fields = this.fields  ;
		final int     length = fields.length;
		
		try
		{
			for(int i = 0; i < length; i++)
			{
				final Object current, returned;
				if((returned = acceptor.acceptInstance(
					current = JadothReflect.getFieldValue(fields[i], instance), instance, enqueuer)
				) != current)
				{
					JadothReflect.setFieldValue(fields[i], instance, returned);
				}
				
				// note: if the current (now prior) value has to be enqueued, the acceptor can do that internally
				enqueuer.enqueue(returned);
			}
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
	}

}
