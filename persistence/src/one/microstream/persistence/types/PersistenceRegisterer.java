package one.microstream.persistence.types;

import static java.lang.System.identityHashCode;
import static one.microstream.X.notNull;

import one.microstream.math.XMath;

public interface PersistenceRegisterer extends PersistenceFunction
{
	public long register(Object instance);

	public long[] registerAll(Object... instances);



	public class Default implements PersistenceRegisterer
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final PersistenceObjectManager<?>      objectManager     ;
		private final PersistenceTypeHandlerManager<?> typeHandlerManager;

		private final Entry[]                          oidsSlots         ;
		private final int                              oidsModulo        ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(
			final PersistenceObjectManager<?>      objectManager,
			final PersistenceTypeHandlerManager<?> typeManager
		)
		{
			super();
			this.objectManager      = notNull(objectManager);
			this.typeHandlerManager = notNull(typeManager)  ;
			this.oidsSlots          = new Entry[1]          ;
			this.oidsModulo         = 0                     ;
		}

		public Default(
			final PersistenceObjectManager<?>      objectManager,
			final PersistenceTypeHandlerManager<?> typeManager  ,
			final int                              hashRange
		)
		{
			super();
			this.objectManager      = notNull(objectManager);
			this.typeHandlerManager = notNull(typeManager  );
			this.oidsSlots          = new Entry[XMath.pow2BoundCapped(hashRange)];
			this.oidsModulo         = this.oidsSlots.length - 1;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public <T> long apply(final T instance)
		{
			// abort on null reference or already handled instance
			if(instance == null || this.isRegisteredLocal(instance))
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
			this.objectManager.ensureObjectId(instance);

			// iterate references
			handler.iterateInstanceReferences(instance, this);

			return 0L; // registerer does not need to return the oid
		}

//		@Override
//		public void clearRegistered()
//		{
//			this.clearRegistry();
//		}
//
//		@Override
//		public void registerSkip(final Object instance)
//		{
//			this.registerLocal(instance);
//		}



		///////////////////////////////////////////////////////////////////////////
		// OID registry map //
		/////////////////////

		private boolean isRegisteredLocal(final Object instance)
		{
			for(Entry e = this.oidsSlots[identityHashCode(instance) & this.oidsModulo]; e != null; e = e.link)
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
			Entry e;
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

//		private void clearRegistry()
//		{
//			final Entry[] slots = this.oidsSlots;
//			for(int i = 0; i < slots.length; i++)
//			{
//				slots[i] = null;
//			}
//		}

		private static final class Entry
		{
			final Object ref;
			Entry link;

			Entry(final Object instance)
			{
				super();
				this.ref  = instance;
				this.link = null;
			}

			Entry(final Object instance, final Entry link)
			{
				super();
				this.ref  = instance;
				this.link = link;
			}

		}

		///////////////////////////////////////////////////////////////////////////
		// End OID registry map //
		/////////////////////////

		public static class Creator implements PersistenceRegisterer.Creator
		{
			@Override
			public PersistenceRegisterer createRegisterer(
				final PersistenceObjectManager<?>      objectManager,
				final PersistenceTypeHandlerManager<?> typeManager
			)
			{
				return new PersistenceRegisterer.Default(objectManager, typeManager);

			}
		}

		@Override
		public long register(final Object instance)
		{
			return this.apply(instance);
		}

		@Override
		public long[] registerAll(final Object... instances)
		{
			final long[] oids = new long[instances.length]; // implicit null check
			for(int i = 0; i < instances.length; i++)
			{
				oids[i] = this.apply(instances[i]);
			}
			return oids;
		}

	}

	public interface Creator
	{
		public PersistenceRegisterer createRegisterer(
			PersistenceObjectManager<?>      objectManager,
			PersistenceTypeHandlerManager<?> typeManager
		);
	}
}
