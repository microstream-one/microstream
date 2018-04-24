package net.jadoth.persistence.binary.types;

import static java.lang.System.identityHashCode;
import static net.jadoth.Jadoth.notNull;

import net.jadoth.persistence.types.BufferSizeProvider;
import net.jadoth.persistence.types.PersistenceStorer;
import net.jadoth.persistence.types.PersistenceTarget;
import net.jadoth.persistence.types.PersistenceTypeHandler;
import net.jadoth.persistence.types.PersistenceTypeHandlerManager;
import net.jadoth.reference._intReference;
import net.jadoth.swizzling.types.Swizzle;
import net.jadoth.swizzling.types.SwizzleObjectManager;
import net.jadoth.swizzling.types.SwizzleObjectSupplier;
import net.jadoth.swizzling.types.SwizzleStoreLinker;
import net.jadoth.util.Stateless;


public interface BinaryStorer extends PersistenceStorer<Binary>
{
	public interface Creator extends PersistenceStorer.Creator<Binary>
	{
		@Override
		public BinaryStorer createPersistenceStorer(
			SwizzleObjectManager                  objectManager     ,
			SwizzleObjectSupplier                 objectSupplier    ,
			PersistenceTypeHandlerManager<Binary> typeManager       ,
			PersistenceTarget<Binary>             target            ,
			BufferSizeProvider                    bufferSizeProvider
		);
	}

	@Override
	public BinaryStorer initialize();

	@Override
	public BinaryStorer initialize(long initialCapacity);

	@Override
	public PersistenceStorer<Binary> reinitialize();

	@Override
	public PersistenceStorer<Binary> reinitialize(long initialCapacity);

	@Override
	public PersistenceStorer<Binary> ensureCapacity(long desiredCapacity);

	@Override
	public long currentCapacity();

	@Override
	public long maximumCapacity();



	public abstract class AbstractImplementation implements BinaryStorer, SwizzleStoreLinker
	{
		///////////////////////////////////////////////////////////////////////////
		// constants        //
		/////////////////////

		private static final int DEFAULT_SLOT_SIZE = 1024; // anthing below 1024 doesn't pay of

		private static final int MAX_POW_2_INT     = 1_073_741_824; // technical 2^n int limit


		public static final int padCapacity(final long n)
		{
			if(n >= MAX_POW_2_INT)
			{
				return MAX_POW_2_INT;
			}
			int i = 1;
			while(i < n)
			{
				i <<= 1;
			}
			return i;
		}



		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		// (12.04.2013)XXX: encapsulate by "PersistenceDistrictManager" complementary to builder?
		final SwizzleObjectManager                  objectManager ;
		final SwizzleObjectSupplier                 objectSupplier;
		final PersistenceTypeHandlerManager<Binary> typeManager   ;
		final PersistenceTarget<Binary>             target        ;

		// can only directly implement SwizzleStoreLinker once, so the alternative has to use a detour solution.
		final FullStorer                            fullStorer;

		/* item hashing structures get initialized lazyly for the following reasons:
		 * - storer instance can commit (be cleared) and reinitialized multiple times.
		 * - storer instance can be explicitely initialized to a certain capacity.
		 * - clearing after commit can simply null the array reference, easing garbage collection by a great deal.
		 */
		final   Item   head      = new Item(null, 0L, null, null);
		private Item   tail      = this.head                     ;
		private Item[] hashSlots;
		private int    hashRange;
		private long   itemCount;




		final class FullStorer implements SwizzleStoreLinker
		{
			final SwizzleObjectSupplier objectSupplier;

			public FullStorer(final SwizzleObjectSupplier objectSupplier)
			{
				super();
				this.objectSupplier = objectSupplier;
			}

			@Override
			public <T> long apply(final T instance)
			{
				return AbstractImplementation.this.registerAddFull(instance);
			}

			@Override
			public SwizzleObjectSupplier getSwizzleObjectSupplier()
			{
				return this.objectSupplier;
			}

		}


		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public AbstractImplementation(
			final SwizzleObjectManager                  objectManager ,
			final SwizzleObjectSupplier                 objectSupplier,
			final PersistenceTypeHandlerManager<Binary> typeManager   ,
			final PersistenceTarget<Binary>             target
		)
		{
			super();
			this.objectManager  = notNull(objectManager );
			this.objectSupplier = notNull(objectSupplier);
			this.typeManager    = notNull(typeManager   );
			this.target         = notNull(target        );
			this.fullStorer     = new FullStorer(objectSupplier);
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		protected abstract ChunksBuffer chunk(long oid);

		protected abstract Binary[] complete();

		final long registerAddFull(final Object instance)
		{
			if(instance == null)
			{
				return Swizzle.nullId();
			}

			final long oid;
			if((oid = this.lookupOid(instance)) != Swizzle.nullId())
			{
				return oid;
			}

			return this.registerAdd(instance);
		}

		@Override
		public final <T> long apply(final T instance)
		{
			if(instance == null)
			{
				return Swizzle.nullId();
			}

			final long oidLocal;
			if((oidLocal = this.lookupOid(instance)) != Swizzle.nullId())
			{
				return oidLocal;
			}

			final long oidGlobal;
			if((oidGlobal = this.objectManager.lookupObjectId(instance)) != Swizzle.nullId())
			{
				return oidGlobal;
			}

			return this.registerAdd(instance);
		}

		@Override
		public final SwizzleObjectSupplier getSwizzleObjectSupplier()
		{
			return this.objectSupplier;
		}

		private long registerAdd(final Object instance)
		{
			final long oid;

			// ensure handler (or fail if type is not persistable) before ensuring an OID.
			this.tail = this.tail.next = this.registerOid(
				instance,
				this.typeManager.ensureTypeHandler(instance),
				oid = this.objectManager.ensureObjectId(instance)
			);

			return oid;
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public BinaryStorer initialize()
		{
			this.ensureInitialized();
			return this;
		}

		@Override
		public BinaryStorer initialize(final long initialCapacity)
		{
			if(this.isInitialized())
			{
				return this;
			}
			this.internalInitialize(padCapacity(initialCapacity));
			return this;
		}

		@Override
		public PersistenceStorer<Binary> reinitialize()
		{
			this.clear();
			this.internalInitialize();
			return this;
		}

		@Override
		public PersistenceStorer<Binary> reinitialize(final long initialCapacity)
		{
			this.clear();
			this.internalInitialize(padCapacity(initialCapacity));
			return this;
		}

		private void internalInitialize()
		{
			this.internalInitialize(DEFAULT_SLOT_SIZE);
		}

		protected void internalInitialize(final int pow2Capacity)
		{
			this.hashSlots = new Item[pow2Capacity];
			this.hashRange = pow2Capacity - 1;
		}

		@Override
		public final long currentCapacity()
		{
			return this.hashSlots == null ? 0 : this.hashSlots.length;
		}

		@Override
		public final long maximumCapacity()
		{
			return MAX_POW_2_INT;
		}

		@Override
		public PersistenceStorer<Binary> ensureCapacity(final long desiredCapacity)
		{
			if(this.currentCapacity() >= desiredCapacity)
			{
				return this;
			}
			this.rebuildStoreItems(padCapacity(desiredCapacity));
			return this;
		}

		private void ensureInitialized()
		{
			if(this.isInitialized())
			{
				return;
			}
			this.internalInitialize();
		}

		@Override
		public final boolean isInitialized()
		{
			return this.hashSlots != null;
		}

		@Override
		public final long size()
		{
			return this.itemCount;
		}

		@Override
		public final long storeFull(final Object root)
		{
			this.ensureInitialized();
			return this.internalStoreFull(root);
		}

		private long internalStoreFull(final Object root)
		{
			final long rootOid = this.registerAdd(notNull(root));

			// process and collect all instances uniquely in item chain (graph recursion transformed to iteration)
			for(Item item = this.tail; item != null; item = item.next)
			{
//				JadothConsole.debugln("Storing\t" + c.oid + "\t" + c.typeHandler.typeName());
				item.typeHandler.store(this.chunk(item.oid), item.instance, item.oid, this.fullStorer);
			}

			return rootOid;
		}

		@Override
		public final long storeRequired(final Object root)
		{
			this.ensureInitialized();
			return this.internalStoreRequired(root);
		}

		private long internalStoreRequired(final Object root)
		{
			final long rootOid = this.registerAdd(notNull(root));

			// process and collect required instances uniquely in item chain (graph recursion transformed to iteration)
			for(Item item = this.tail; item != null; item = item.next)
			{
//				JadothConsole.debugln("Storing\t" + item.oid + "\t" + item.typeHandler.typeName());
				item.typeHandler.store(this.chunk(item.oid), item.instance, item.oid, this);
			}

			return rootOid;
		}

		@Override
		public final long[] storeAllFull(final Object... instances)
		{
			this.ensureInitialized();

			final long[] oids = new long[instances.length];
			for(int i = 0; i < instances.length; i++)
			{
				oids[i] = this.internalStoreFull(instances[i]);
			}
			return oids;
		}

		@Override
		public final long[] storeAllRequired(final Object... instances)
		{
			this.ensureInitialized();

			final long[] oids = new long[instances.length];
			for(int i = 0; i < instances.length; i++)
			{
				oids[i] = this.internalStoreRequired(instances[i]);
			}
			return oids;
		}

		@Override
		public final Object commit()
		{
			if(!this.isEmpty())
			{
				this.target.write(this.complete());
			}
			this.clear();
			return null;
		}

		@Override
		public void clear()
		{
			this.clearRegistered();
		}

		@Override
		public final void clearRegistered()
		{
			// clear hash table
			this.hashSlots = null;
			this.itemCount = 0;

			// clear item chain
			(this.tail = this.head).next = null;
		}

		@Override
		public final void registerSkip(final Object instance, final long oid)
		{
			this.registerOid(instance, null, oid);
		}

		@Override
		public final void registerSkip(final Object instance)
		{
			this.registerOid(instance, null, this.objectManager.lookupObjectId(instance));
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		public final long lookupOid(final Object instance)
		{
			for(Item e = this.hashSlots[identityHashCode(instance) & this.hashRange]; e != null; e = e.link)
			{
				if(e.instance == instance)
				{
					return e.oid;
				}
			}
			return 0L;
		}

		public final Item registerOid(
			final Object                                 instance   ,
			final PersistenceTypeHandler<Binary, Object> typeHandler,
			final long                                   oid
		)
		{
			if(++this.itemCount >= this.hashRange)
			{
				this.rebuildStoreItems();
			}

			return this.hashSlots[identityHashCode(instance) & this.hashRange] =
				new Item(instance, oid, typeHandler, this.hashSlots[identityHashCode(instance) & this.hashRange])
			;
		}

		public final void rebuildStoreItems()
		{
			this.rebuildStoreItems(this.hashSlots.length * 2);
		}

		public final void rebuildStoreItems(final int newLength)
		{
			// moreless academic check for more than 1 billion entries
			if(this.hashSlots.length >= MAX_POW_2_INT)
			{
				return; // note that aborting rebuild does not ruin anything, only performance degrades
			}

			final int newRange;
			final Item[] newSlots = new Item[(newRange = newLength - 1) + 1];
			for(Item entry : this.hashSlots)
			{
				for(Item next; entry != null; entry = next)
				{
					next = entry.link;
					entry.link = newSlots[identityHashCode(entry.instance) & newRange];
					newSlots[identityHashCode(entry.instance) & newRange] = entry;
				}
			}
			this.hashSlots = newSlots;
			this.hashRange = newRange;
		}

	}



	public final class SimpleDeep extends AbstractImplementation
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final BufferSizeProvider bufferSizeProvider;

		private ChunksBuffer chunk;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public SimpleDeep(
			final SwizzleObjectManager                  objectManager     ,
			final SwizzleObjectSupplier                 objectSupplier    ,
			final PersistenceTypeHandlerManager<Binary> typeManager       ,
			final PersistenceTarget<Binary>             target            ,
			final BufferSizeProvider                    bufferSizeProvider
		)
		{
			super(objectManager, objectSupplier, typeManager, target);
			this.bufferSizeProvider = notNull(bufferSizeProvider);
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		protected void internalInitialize(final int pow2Capacity)
		{
			super.internalInitialize(pow2Capacity);

			this.chunk = ChunksBuffer.New(this.bufferSizeProvider);
		}

		@Override
		protected final ChunksBuffer chunk(final long oid)
		{
			return this.chunk; // always return single chunk in simple implementation
		}

		@Override
		protected final Binary[] complete()
		{
			return new Binary[]{this.chunk.complete()};
		}

		@Override
		public final void clear()
		{
			super.clear();

			// intentionally no deallocation here because chunks might still be concurrently iterated
			this.chunk = null;
		}

	}

	public final class CreatorSimple implements BinaryStorer.Creator, Stateless
	{
		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public final BinaryStorer.SimpleDeep createPersistenceStorer(
			final SwizzleObjectManager                  objectManager     ,
			final SwizzleObjectSupplier                 objectSupplier    ,
			final PersistenceTypeHandlerManager<Binary> typeManager       ,
			final PersistenceTarget<Binary>             target            ,
			final BufferSizeProvider                    bufferSizeProvider
		)
		{
			return new BinaryStorer.SimpleDeep(objectManager, objectSupplier, typeManager, target, bufferSizeProvider);
		}

	}



	public final class ChannelHashingDeep extends AbstractImplementation
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final BufferSizeProvider bufferSizeProvider;
		private final int                channelHashRange  ;
		private final ChunksBuffer[]     chunks            ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public ChannelHashingDeep(
			final SwizzleObjectManager                  objectManager     ,
			final SwizzleObjectSupplier                 objectSupplier    ,
			final PersistenceTypeHandlerManager<Binary> typeManager       ,
			final PersistenceTarget<Binary>             target            ,
			final BufferSizeProvider                    bufferSizeProvider,
			final int                                   channelCount
		)
		{
			super(objectManager, objectSupplier, typeManager, target);
			this.bufferSizeProvider = notNull(bufferSizeProvider)   ;
			this.chunks             = new ChunksBuffer[channelCount];
			this.channelHashRange   = channelCount - 1              ;
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		protected final void internalInitialize(final int pow2Capacity)
		{
			super.internalInitialize(pow2Capacity);

			final ChunksBuffer[] chunks = this.chunks;
			for(int i = 0; i < chunks.length; i++)
			{
				chunks[i] = ChunksBuffer.New(this.bufferSizeProvider);
			}
		}

		@Override
		protected final ChunksBuffer chunk(final long oid)
		{
			return this.chunks[(int)(oid & this.channelHashRange)];
		}

		@Override
		protected final ChunksBuffer[] complete()
		{
			// (21.03.2016 TM)NOTE: added to avoid NPEs
			this.initialize();

			for(final ChunksBuffer chunk : this.chunks)
			{
				chunk.complete();
			}

			/* must return a local copy of the array as the task may not use this instance's interal array.
			 * For example:
			 * - storer clears the array after completing, while task needs an uncleared array to clear
			 *   the chunks themselves
			 * - task implementation might mutate the array, potentially ruining the storer's state
			 * The chunks array is usually tiny (length equal to channel count, so 1, 2, 4, maybe 8 or even 16 ... tiny)
			 */
			return this.chunks.clone();
		}

		@Override
		public final void clear()
		{
			super.clear();

			final ChunksBuffer[] chunks = this.chunks;
			for(int i = 0; i < chunks.length; i++)
			{
				/* Note:
				 * may explicitely NOT clear (deallocate) the current chunks
				 * because in use with embedded (in-process) storage the chunks
				 * might still be used by the storage worker threads to update their entity caches.
				 * The released chunks must be handled by those threads if existing
				 * or ultimately by the garbage collector (or by some tailored additional logic)
				 */
				chunks[i] = null;
			}
		}

	}

	public final class CreatorChannelHashingDeep implements BinaryStorer.Creator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final _intReference channelCountProvider;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public CreatorChannelHashingDeep(final _intReference channelCountProvider)
		{
			super();
			this.channelCountProvider = notNull(channelCountProvider);
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public final BinaryStorer.ChannelHashingDeep createPersistenceStorer(
			final SwizzleObjectManager                  objectManager     ,
			final SwizzleObjectSupplier                 objectSupplier    ,
			final PersistenceTypeHandlerManager<Binary> typeManager       ,
			final PersistenceTarget<Binary>             target            ,
			final BufferSizeProvider                    bufferSizeProvider
		)
		{
			return new ChannelHashingDeep(
				objectManager,
				objectSupplier,
				typeManager,
				target,
				bufferSizeProvider,
				this.channelCountProvider.get()
			);
		}

	}



	static final class Item
	{
		final PersistenceTypeHandler<Binary, Object> typeHandler;
		final Object                                 instance   ;
		final long                                   oid        ;
		      Item                                   link, next ;

		Item(
			final Object                                 instance   ,
			final long                                   oid        ,
			final PersistenceTypeHandler<Binary, Object> typeHandler,
			final Item                                   link
		)
		{
			super();
			this.instance    = instance   ;
			this.oid         = oid        ;
			this.typeHandler = typeHandler;
			this.link        = link       ;
		}

	}

}
