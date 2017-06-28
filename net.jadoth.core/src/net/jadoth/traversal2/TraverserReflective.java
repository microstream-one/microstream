package net.jadoth.traversal2;

import java.lang.reflect.Field;

import net.jadoth.reflect.JadothReflect;

public final class TraverserReflective implements TraversalHandler
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final Field[] fields;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	TraverserReflective(final Field[] fields)
	{
		super();
		this.fields = fields;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void traverseReferences(final Object parent, final TraversalAcceptor acceptor, final TraversalEnqueuer enqueuer)
	{
		final Field[] fields = this.fields;
		final int     length = fields.length;
		
		for(int i = 0; i < length; i++)
		{
			final Object current, v;
			if((v = acceptor.acceptInstance(current = JadothReflect.getFieldValue(fields[i], parent), parent, enqueuer)) != current)
			{
				JadothReflect.setFieldValue(fields[i], parent, v);
			}
		}
	}

}
