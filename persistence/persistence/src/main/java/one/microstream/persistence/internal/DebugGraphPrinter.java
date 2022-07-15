package one.microstream.persistence.internal;

/*-
 * #%L
 * microstream-persistence
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import static java.lang.System.identityHashCode;
import static one.microstream.X.notNull;

import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.math.XMath;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceObjectManager;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.persistence.types.PersistenceTypeHandlerManager;
import one.microstream.reference.Swizzling;

public class DebugGraphPrinter implements PersistenceFunction
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	private static final int MAX_LITERAL_LENGTH_LONG = 19;



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final PersistenceObjectManager<?>      objectManager     ;
	private final PersistenceTypeHandlerManager<?> typeHandlerManager;
	private final DebugGraphPrinter.Entry[]        objectIdsSlots    ;
	private final int                              objectIdsModulo   ;
	private       int                              level             ;
	private final VarString                        vc                 = VarString.New();



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public DebugGraphPrinter(
		final PersistenceObjectManager<?>             objectManager,
		final PersistenceTypeHandlerManager<?> typeManager
	)
	{
		super();
		this.objectManager      = notNull(objectManager)        ;
		this.typeHandlerManager = notNull(typeManager)          ;
		this.objectIdsSlots     = new DebugGraphPrinter.Entry[1];
		this.objectIdsModulo    = 0                             ;
	}

	public DebugGraphPrinter(
		final PersistenceObjectManager<?>      objectManager,
		final PersistenceTypeHandlerManager<?> typeManager,
		final int hashRange
	)
	{
		super();
		this.objectManager      = notNull(objectManager);
		this.typeHandlerManager = notNull(typeManager  );
		this.objectIdsSlots     = new DebugGraphPrinter.Entry[XMath.pow2BoundCapped(hashRange)];
		this.objectIdsModulo    = this.objectIdsSlots.length - 1;
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
			return Swizzling.nullId();
		}

		final long objectId = this.objectManager.ensureObjectId(instance);
		this.vc.padLeft(Long.toString(objectId), MAX_LITERAL_LENGTH_LONG, '0').blank().add(XChars.systemString(instance));
		if(!instance.getClass().isArray())
		{
			this.vc.tab(2).add(instance);
		}
		System.out.println(this.vc);
		if(this.isRegisteredLocal(instance))
		{
			return Swizzling.nullId();
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

		// return value is irrelevant for a debuging graph printer.
		return Swizzling.nullId();
	}



	///////////////////////////////////////////////////////////////////////////
	// OID registry map //
	/////////////////////

	private boolean isRegisteredLocal(final Object instance)
	{
		for(DebugGraphPrinter.Entry e = this.objectIdsSlots[identityHashCode(instance) & this.objectIdsModulo]; e != null; e = e.link)
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
		if((e = this.objectIdsSlots[index = identityHashCode(instance) & this.objectIdsModulo]) == null)
		{
			this.objectIdsSlots[index] = new Entry(instance);
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
		this.objectIdsSlots[index] = new Entry(instance, this.objectIdsSlots[index]);
	}

//	private void clearRegistry()
//	{
//		final DebugGraphPrinter.Entry[] slots = this.objectIdsSlots;
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
