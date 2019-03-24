package one.microstream.persistence.internal;

import static java.lang.System.identityHashCode;
import static one.microstream.X.notNull;

import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.math.XMath;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceObjectManager;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.persistence.types.PersistenceTypeHandlerManager;

public class DebugGraphPrinter implements PersistenceFunction
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	private static final int MAX_LITERAL_LENGTH_LONG = 19;



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final PersistenceObjectManager         objectManager     ;
	private final PersistenceTypeHandlerManager<?> typeHandlerManager;
	private final DebugGraphPrinter.Entry[]        oidsSlots         ;
	private final int                              oidsModulo        ;
	private       int                              level             ;
	private final VarString                        vc                 = VarString.New();



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public DebugGraphPrinter(
		final PersistenceObjectManager             objectManager,
		final PersistenceTypeHandlerManager<?> typeManager
	)
	{
		super();
		this.objectManager      = notNull(objectManager)      ;
		this.typeHandlerManager = notNull(typeManager)        ;
		this.oidsSlots          = new DebugGraphPrinter.Entry[1];
		this.oidsModulo         = 0       ;
	}

	public DebugGraphPrinter(
		final PersistenceObjectManager objectManager,
		final PersistenceTypeHandlerManager<?> typeManager,
		final int hashRange
	)
	{
		super();
		this.objectManager = notNull(objectManager);
		this.typeHandlerManager   = notNull(typeManager  );
		this.oidsSlots     = new DebugGraphPrinter.Entry[XMath.pow2BoundCapped(hashRange)];
		this.oidsModulo    = this.oidsSlots.length - 1;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public <T> long apply(final T instance)
	{
		this.vc.clear();
		for(int i = this.level; i-- > 0;)
		{
			this.vc.add('|').tab();
		}
		this.vc.add(" + ---");

		// abort on null reference or already handled instance
		if(instance == null)
		{
			System.out.println(this.vc.add(instance));
			return 0L;
		}

		final long oid = this.objectManager.ensureObjectId(instance);
		this.vc.padLeft(Long.toString(oid), MAX_LITERAL_LENGTH_LONG, '0').blank().add(XChars.systemString(instance));
		if(!instance.getClass().isArray())
		{
			this.vc.tab(2).add(instance);
		}
		System.out.println(this.vc);
		if(this.isRegisteredLocal(instance))
		{
			return 0L;
		}

		// ensure type handler (or fail if type is not persistable) before ensuring oid
		@SuppressWarnings("unchecked") // cast type safety guaranteed by management logic
		final PersistenceTypeHandler<?, Object> handler =
			(PersistenceTypeHandler<?, Object>)this.typeHandlerManager.ensureTypeHandler(instance.getClass())
		;

		// ensure and register oid for that instance
		this.registerLocal(instance);

		// iterate references
		this.level++;
		handler.iterateInstanceReferences(instance, this);
		this.level--;

		return 0L; // registerer does not need to return the oid
	}



	///////////////////////////////////////////////////////////////////////////
	// OID registry map     //
	/////////////////////////

	private boolean isRegisteredLocal(final Object instance)
	{
		for(DebugGraphPrinter.Entry e = this.oidsSlots[identityHashCode(instance) & this.oidsModulo]; e != null; e = e.link)
		{
			if(e.ref == instance)
			{
				return true;
			}
		}
		return false;
	}

	private void registerLocal(final Object instance)
	{
		final int index;
		DebugGraphPrinter.Entry e;
		if((e = this.oidsSlots[index = identityHashCode(instance) & this.oidsModulo]) == null)
		{
			this.oidsSlots[index] = new Entry(instance);
			return;
		}
		do
		{
			if(e.ref == instance)
			{
				return;
			}
		}
		while((e = e.link) != null);
		this.oidsSlots[index] = new Entry(instance, this.oidsSlots[index]);
	}

//	private void clearRegistry()
//	{
//		final DebugGraphPrinter.Entry[] slots = this.oidsSlots;
//		for(int i = 0; i < slots.length; i++)
//		{
//			slots[i] = null;
//		}
//	}

	private static final class Entry
	{
		final Object ref;
		DebugGraphPrinter.Entry link;

		Entry(final Object instance)
		{
			super();
			this.ref  = instance;
			this.link = null;
		}

		Entry(final Object instance, final DebugGraphPrinter.Entry link)
		{
			super();
			this.ref  = instance;
			this.link = link;
		}

	}

	///////////////////////////////////////////////////////////////////////////
	// End OID registry map //
	/////////////////////////


	public long register(final Object instance)
	{
		return this.apply(instance);
	}

}
