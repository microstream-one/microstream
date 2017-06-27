package net.jadoth.traversal2;

import java.lang.reflect.Field;

import net.jadoth.reflect.JadothReflect;

public final class TraverserByFields implements ReferenceAccessor, TraversalHandler
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final Field[] fields;
	private       Field   currentField;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	TraverserByFields(final Field[] fields)
	{
		super();
		this.fields = fields;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public Object get(final Object parent)
	{
		return JadothReflect.getFieldValue(this.currentField, parent);
	}

	@Override
	public void set(final Object parent, final Object newValue)
	{
		JadothReflect.setFieldValue(this.currentField, parent, newValue);
	}

	@Override
	public void traverseReferences(final Object parent, final TraversalAcceptor acceptor, final TraversalEnqueuer enqueuer)
	{
		final Field[] fields = this.fields;
		final int     length = fields.length;
		
		for(int i = 0; i < length; i++)
		{
			acceptor.acceptInstance(JadothReflect.getFieldValue(this.currentField = fields[i], parent), parent, this, enqueuer);
		}
		this.currentField = null;
	}

}
