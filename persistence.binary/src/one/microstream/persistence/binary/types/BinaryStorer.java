package one.microstream.persistence.binary.types;

import static java.lang.System.identityHashCode;
import static one.microstream.X.notNull;

import one.microstream.hashing.XHashing;
import one.microstream.math.XMath;
import one.microstream.persistence.types.PersistenceAcceptor;
import one.microstream.persistence.types.PersistenceEagerStoringFieldEvaluator;
import one.microstream.persistence.types.PersistenceLocalObjectIdRegistry;
import one.microstream.persistence.types.PersistenceObjectManager;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.persistence.types.PersistenceStorer;
import one.microstream.persistence.types.PersistenceTarget;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.persistence.types.PersistenceTypeHandlerManager;
import one.microstream.reference.ObjectSwizzling;
import one.microstream.reference.Swizzling;
import one.microstream.util.BufferSizeProviderIncremental;


public interface BinaryStorer extends PersistenceStorer
{
//	@Override
//	public BinaryStorer initialize();
//
//	@Override
//	public BinaryStorer initialize(long initialCapacity);

	@Override
	public PersistenceStorer reinitialize();

	@Override
	public PersistenceStorer reinitialize(long initialCapacity);

	@Override
	public PersistenceStorer ensureCapacity(long desiredCapacity);

	@Override
	public long currentCapacity();

	@Override
	public long maximumCapacity();


	
	/**
	 * Default implementation that stores referenced instances only if required (i.e. if they have no OID assigned yet,
	 * therefore have not been stored yet, therefore require to be stored). It can be seen as a "lazy" or "on demand"
	 * storer as opposed to{@link Eager}.<br>
	 * For a more differentiated solution between the two simple, but extreme strategies,
	 * see {@link PersistenceEagerStoringFieldEvaluator}.
	 * 
	 * @author TM
	 */
	public class Default implements BinaryStorer, PersistenceStoreHandler, PersistenceLocalObjectIdRegistry
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////

		protected static int defaultSlotSize()
		{
			// why permanently occupy additional memory with fields and instances for constant values?
			return 1024; // anthing below 1024 doesn't pay of
		}

		

		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final boolean                               switchByteOrder;
		private final PersistenceObjectManager              objectManager  ;
		private final ObjectSwizzling                       objectRetriever;
		private final PersistenceTypeHandlerManager<Binary> typeManager    ;
		private final PersistenceTarget<Binary>             target         ;
		
		// channel hashing fields
		private final BufferSizeProviderIncremental bufferSizeProvider;
		private final int                           chunksHashRange   ;
		
		// cannot be final since every commit needs to pass an independant instance, anyway
		private ChunksBuffer[] chunks;

		/*
		 * item hashing structures get initialized lazily for the following reasons:
		 * - the storer instance can commit (be cleared) and be reinitialized multiple times.
		 * - the storer instance can be explicitly initialized to a certain capacity.
		 * - clearing after committing can simply null the array reference, easing garbage collection.
		 */
		final   Item   head = new Item(null, 0L, null, null);
		private Item   tail;
		private Item[] hashSlots;
		private int    hashRange;
		private long   itemCount;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
			final PersistenceObjectManager              objectManager     ,
			final ObjectSwizzling                       objectRetriever   ,
			final PersistenceTypeHandlerManager<Binary> typeManager       ,
			final PersistenceTarget<Binary>             target            ,
			final BufferSizeProviderIncremental         bufferSizeProvider,
			final int                                   channelCount      ,
			final boolean                               switchByteOrder
		)
		{
			super();
			this.objectManager      = notNull(objectManager)     ;
			this.objectRetriever    = notNull(objectRetriever)   ;
			this.typeManager        = notNull(typeManager)       ;
			this.target             = notNull(target)            ;
			this.bufferSizeProvider = notNull(bufferSizeProvider);
			this.chunksHashRange    =         channelCount - 1   ;
			this.switchByteOrder    =         switchByteOrder    ;
			
			this.internalInitialize();
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		protected ChunksBuffer chunk(final long objectId)
		{
			return this.chunks[(int)(objectId & this.chunksHashRange)];
		}

		protected Binary complete()
		{
			for(final ChunksBuffer chunk : this.chunks)
			{
				chunk.complete();
			}

			// all chunks know the array internally, so passing one means passing all. And there is always at least one.
			return this.chunks[0];
		}

		@Override
		public <T> long apply(final T instance)
		{
			if(instance == null)
			{
				return Swizzling.nullId();
			}

			final long objectIdLocal;
			if(Swizzling.isFoundId(objectIdLocal = this.lookupOid(instance)))
			{
				// returning 0 is a valid case: an instance registered to be skipped by using the null-OID.
				return objectIdLocal;
			}
			
			/*
			 * Lazy storing logic:
			 * If the instance already has an OID registered with it (= already known / handled globally),
			 * it is assumed to be already stored and therefore not stored here ("again").
			 * Only if a new OID has to be assigned, the instance is registered (via registerAdd)
			 */
			return this.objectManager.ensureObjectId(instance, this);
		}
		
		@Override
		public final <T> long applyEager(final T instance)
		{
			if(instance == null)
			{
				return Swizzling.nullId();
			}
			
			/*
			 * "Eager" must still mean that if this storer has already stored the passed instance,
			 * it may not store it again. That would not only be data-wise redundant and unnecessary,
			 * but would also create infinite storing loops and overflows.
			 * So "eager" can only mean to not check the global registry, but it must still mean to check
			 * the local registry.
			 */
			final long objectIdLocal;
			if(Swizzling.isFoundId(objectIdLocal = this.lookupOid(instance)))
			{
				// returning 0 is a valid case: an instance registered to be skipped by using the null-OID.
				return objectIdLocal;
			}

			/*
			 * Eager storing logic:
			 * If the instance is not already handled locally (already stored by this storer), it is now stored.
			 */
			return this.registerAdd(instance);
		}

		@Override
		public final ObjectSwizzling getObjectRetriever()
		{
			return this.objectRetriever;
		}

		@Override
		public PersistenceStorer reinitialize()
		{
			// clear does initialization as well, since the storer may never be in an uninitialized state.
			this.clear();
			
			return this;
		}

		@Override
		public PersistenceStorer reinitialize(final long initialCapacity)
		{
			this.internalInitialize(XHashing.padHashLength(initialCapacity));
			
			return this;
		}

		private void internalInitialize()
		{
			this.internalInitialize(defaultSlotSize());
		}

		protected void internalInitialize(final int hashLength)
		{
			synchronized(this.head)
			{
				this.hashSlots = new Item[hashLength];
				this.hashRange = hashLength - 1;
				
				// initializing/clearing item chain
				(this.tail = this.head).next = null;
				
				this.synchCreateStoringChunksBuffers();
			}
		}
		
		private void synchCreateStoringChunksBuffers()
		{
			/* Note:
			 * May explicitly NOT clear (deallocate) the current (old/previous) chunks
			 * because in use with embedded (in-process) storage the chunks
			 * might still be used by the storage worker threads to update their entity caches.
			 * The released chunks must be handled by those threads if existing
			 * or ultimately by the garbage collector (or by some tailored additional logic)
			 */
			
			final ChunksBuffer[] chunks = this.chunks = new ChunksBuffer[this.chunksHashRange + 1];
			for(int i = 0; i < chunks.length; i++)
			{
				chunks[i] = this.switchByteOrder
					? ChunksBufferByteReversing.New(chunks, this.bufferSizeProvider)
					: ChunksBuffer.New(chunks, this.bufferSizeProvider)
				;
			}
		}

		@Override
		public final long currentCapacity()
		{
			synchronized(this.head)
			{
				return this.hashSlots.length;
			}
		}

		@Override
		public final long maximumCapacity()
		{
			return Long.MAX_VALUE;
		}

		@Override
		public PersistenceStorer ensureCapacity(final long desiredCapacity)
		{
			synchronized(this.head)
			{
				if(this.currentCapacity() >= desiredCapacity)
				{
					return this;
				}
				this.synchRebuildStoreItems(XHashing.padHashLength(desiredCapacity));
			}
			
			return this;
		}

		@Override
		public final long size()
		{
			synchronized(this.head)
			{
				return this.itemCount;
			}
		}

		@Override
		public final long store(final Object root)
		{
			return this.storeGraph(root);
		}

		@Override
		public final long[] storeAll(final Object... instances)
		{
			final long[] oids = new long[instances.length];
			for(int i = 0; i < instances.length; i++)
			{
				oids[i] = this.storeGraph(instances[i]);
			}
			return oids;
		}
		
		@Override
		public void storeAll(final Iterable<?> instances)
		{
			for(final Object instance : instances)
			{
				this.storeGraph(instance);
			}
		}
		
		@Override
		public final PersistenceObjectManager parentObjectManager()
		{
			return this.objectManager;
		}
		
		@Override
		public void iterateMergeableEntries(final PersistenceAcceptor iterator)
		{
			synchronized(this.head)
			{
				for(Item e = this.head; (e = e.next) != null;)
				{
					// skip items are local only and not valid for being visible to (i.e. merged into) global context
					if(isSkipItem(e))
					{
						continue;
					}
					
					// mergeable entry
					iterator.accept(e.oid, e.instance);
				}
			}
		}

		@Override
		public final Object commit()
		{
			if(!this.isEmpty())
			{
				final Binary writeData;
				synchronized(this.head)
				{
					this.typeManager.checkForPendingRootInstances();
					this.typeManager.checkForPendingRootsStoring(this);
					writeData = this.complete();
				}
				
				// very costly IO-operation does not need to occupy the lock
				this.target.write(writeData);
				
				synchronized(this.head)
				{
					this.typeManager.clearStorePendingRoots();
					this.objectManager.mergeEntries(this);
				}
			}
			this.clear();
			
			// not used
			return null;
		}

		@Override
		public void clear()
		{
			this.internalInitialize();
		}
		
		public final long lookupOid(final Object object)
		{
			synchronized(this.head)
			{
				for(Item e = this.hashSlots[identityHashCode(object) & this.hashRange]; e != null; e = e.link)
				{
					if(e.instance == object)
					{
						return e.oid;
					}
				}

				// returning 0 is a valid case: an instance registered to be skipped by using the null-OID.
				return Swizzling.notFoundId();
			}
		}
		
		private static boolean isSkipItem(final Item item)
		{
			return item.typeHandler == null;
		}
		
		@Override
		public final long lookupObjectId(
			final Object              object  ,
			final PersistenceAcceptor receiver
		)
		{
			synchronized(this.head)
			{
				for(Item e = this.hashSlots[identityHashCode(object) & this.hashRange]; e != null; e = e.link)
				{
					if(e.instance == object)
					{
						if(isSkipItem(e))
						{
							// skip-entry for this storer, so it can offer nothing to the receiver.
							break;
						}
						
						// found a local entry in the current storer, transfer object<->id association to the receiver.
						receiver.accept(e.oid, object);
						return e.oid;
					}
				}
				
				return Swizzling.notFoundId();
			}
		}
		
		/**
		 * Stores the passed instance (always) and interprets it as the root of a graph to be traversed and
		 * have its instances stored recursively if deemed necessary by the logic until all instance
		 * that can be reached by that logic have been handled.
		 */
		protected final long storeGraph(final Object root)
		{
			/* (03.12.2019 TM)NOTE:
			 * Special case logic to handle explicitely passed instances:
			 * - if already handled by this storer, don't handle again.
			 * - register to be handled in any case, even if already registered in the object registry.
			 * - handle all registered items recursively (but transformed to an iteration).
			 * Note that this is NOT the same as apply, which does NOT store if the instance is already registry-known.
			 */
			long rootOid;
			if(Swizzling.isFoundId(rootOid = this.lookupOid(root)))
			{
				return rootOid;
			}
			rootOid = this.registerAdd(notNull(root));

			// process and collect required instances uniquely in item chain (graph recursion transformed to iteration)
			for(Item item = this.tail; item != null; item = item.next)
			{
				this.storeItem(item);
			}

			return rootOid;
		}
		
		protected final void storeItem(final Item item)
		{
			// (20.03.2020 TM)FIXME: priv#182: where to put the lock for this method?
//			XDebug.println("Storing     " + item.oid + ": " + XChars.systemString(item.instance) + " ("  + item.instance + ")");
			item.typeHandler.store(this.chunk(item.oid), item.instance, item.oid, this);
		}
		
		@Override
		public final void accept(final long objectId, final Object instance)
		{
//			XDebug.println("Registering " + objectId + ": " + XChars.systemString(instance) + " ("  + instance + ")");
			synchronized(this.head)
			{
				// ensure handler (or fail if type is not persistable) before ensuring an OID.
				final PersistenceTypeHandler<Binary, Object> typeHandler = this.typeManager.ensureTypeHandler(instance);
				final Item item = this.synchRegisterObjectId(instance, typeHandler, objectId);
				this.tail = this.tail.next = item;
			}
		}
		
		protected final long registerAdd(final Object instance)
		{
			final long objectId = this.objectManager.ensureObjectId(instance, this);
			
			// accept does locking internally
			this.accept(objectId, instance);

			return objectId;
		}
		
		@Override
		public final boolean skipMapped(final Object instance, final long objectId)
		{
			return this.internalSkip(instance, objectId);
		}

		@Override
		public final boolean skip(final Object instance)
		{
			final long foundObjectId = this.objectManager.lookupObjectId(instance);
			
			// not found means store as null. Lookup will never return 0
			if(Swizzling.isNotFoundId(foundObjectId))
			{
				return this.skipNulled(instance);
			}
			
			return this.internalSkip(instance, foundObjectId);
		}
		
		@Override
		public final boolean skipNulled(final Object instance)
		{
			return this.internalSkip(instance, Swizzling.nullId());
		}
		
		final boolean internalSkip(final Object instance, final long objectId)
		{
			synchronized(this.head)
			{
				// lookup returns -1 on failure, so 0 is a valid lookup result. Main reason for -1 vs. 0 distinction!
				if(Swizzling.isNotFoundId(this.lookupOid(instance)))
				{
					// only register if not found locally, of course
					this.synchRegisterObjectId(instance, null, objectId);
					return true;
				}
				
				// already locally present (found), do nothing.
				return false;
			}
		}
		
		public final Item synchRegisterObjectId(
			final Object                                 instance   ,
			final PersistenceTypeHandler<Binary, Object> typeHandler,
			final long                                   objectId
		)
		{
			if(++this.itemCount >= this.hashRange)
			{
				this.synchRebuildStoreItems();
			}

			return this.hashSlots[identityHashCode(instance) & this.hashRange] =
				new Item(instance, objectId, typeHandler, this.hashSlots[identityHashCode(instance) & this.hashRange])
			;
		}

		public final void synchRebuildStoreItems()
		{
			this.synchRebuildStoreItems(this.hashSlots.length * 2);
		}

		public final void synchRebuildStoreItems(final int newLength)
		{
			// moreless academic check for more than 1 billion entries
			if(this.hashSlots.length >= XMath.highestPowerOf2_int())
			{
				return; // note that aborting rebuild does not ruin anything.
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

//		@Override
//		public final boolean isInitialized()
//		{
//			return this.hashSlots != null;
//		}
//
//		@Override
//		public BinaryStorer initialize()
//		{
//			if(!this.isInitialized())
//			{
//				this.internalInitialize(defaultSlotSize());
//			}
//
//			return this;
//		}
//
//		@Override
//		public BinaryStorer initialize(final long initialCapacity)
//		{
//			if(!this.isInitialized())
//			{
//				this.internalInitialize(XHashing.padHashLength(initialCapacity));
//			}
//
//			return this;
//		}
		
	}
	
	/**
	 * Identical to {@link Default}, but stores every referenced instance eagerly.<br>
	 * For a more differentiated solution between the two simple, but extreme strategies,
	 * see {@link PersistenceEagerStoringFieldEvaluator}.<br>
	 * 
	 * @author TM
	 */
	public final class Eager extends Default
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Eager(
			final PersistenceObjectManager              objectManager     ,
			final ObjectSwizzling                       objectRetriever   ,
			final PersistenceTypeHandlerManager<Binary> typeManager       ,
			final PersistenceTarget<Binary>             target            ,
			final BufferSizeProviderIncremental         bufferSizeProvider,
			final int                                   channelCount      ,
			final boolean                               switchByteOrder
		)
		{
			super(
				objectManager     ,
				objectRetriever   ,
				typeManager       ,
				target            ,
				bufferSizeProvider,
				channelCount      ,
				switchByteOrder
			);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final <T> long apply(final T instance)
		{
			// for a "full" graph storing strategy, the logic is simply to store everything forced.
			return this.applyEager(instance);
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
		
	public static BinaryStorer.Creator Creator(
		final BinaryChannelCountProvider channelCountProvider,
		final boolean                    switchByteOrder
	)
	{
		return new BinaryStorer.Creator.Default(
			notNull(channelCountProvider),
			        switchByteOrder
		);
	}
		
	public interface Creator extends PersistenceStorer.Creator<Binary>
	{
		@Override
		public BinaryStorer createLazyStorer(
			PersistenceTypeHandlerManager<Binary> typeManager       ,
			PersistenceObjectManager              objectManager     ,
			ObjectSwizzling                       objectRetriever   ,
			PersistenceTarget<Binary>             target            ,
			BufferSizeProviderIncremental         bufferSizeProvider
		);
		
		@Override
		public default BinaryStorer createStorer(
			final PersistenceTypeHandlerManager<Binary> typeManager       ,
			final PersistenceObjectManager              objectManager     ,
			final ObjectSwizzling                       objectRetriever   ,
			final PersistenceTarget<Binary>             target            ,
			final BufferSizeProviderIncremental         bufferSizeProvider
		)
		{
			return this.createLazyStorer(typeManager, objectManager, objectRetriever, target, bufferSizeProvider);
		}
		
		@Override
		public BinaryStorer createEagerStorer(
			PersistenceTypeHandlerManager<Binary> typeManager       ,
			PersistenceObjectManager              objectManager     ,
			ObjectSwizzling                       objectRetriever   ,
			PersistenceTarget<Binary>             target            ,
			BufferSizeProviderIncremental         bufferSizeProvider
		);
		
		
		
		public abstract class Abstract implements BinaryStorer.Creator
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////


			private final BinaryChannelCountProvider channelCountProvider;
			private final boolean                    switchByteOrder     ;



			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////

			protected Abstract(
				final BinaryChannelCountProvider channelCountProvider,
				final boolean                    switchByteOrder
			)
			{
				super();
				this.channelCountProvider = channelCountProvider;
				this.switchByteOrder      = switchByteOrder     ;
			}

			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			protected int channelCount()
			{
				return this.channelCountProvider.getChannelCount();
			}
			
			protected boolean switchByteOrder()
			{
				return this.switchByteOrder;
			}

		}
		
		public final class Default extends Abstract
		{
			Default(
				final BinaryChannelCountProvider channelCountProvider,
				final boolean                    switchByteOrder
			)
			{
				super(channelCountProvider, switchByteOrder);
			}

			@Override
			public final BinaryStorer createLazyStorer(
				final PersistenceTypeHandlerManager<Binary> typeManager       ,
				final PersistenceObjectManager              objectManager     ,
				final ObjectSwizzling                       objectRetriever   ,
				final PersistenceTarget<Binary>             target            ,
				final BufferSizeProviderIncremental         bufferSizeProvider
			)
			{
				final BinaryStorer.Default storer = new BinaryStorer.Default(
					objectManager         ,
					objectRetriever       ,
					typeManager           ,
					target                ,
					bufferSizeProvider    ,
					this.channelCount()   ,
					this.switchByteOrder()
				);
				objectManager.registerLocalRegistry(storer);
				
				return storer;
			}
			@Override
			public BinaryStorer createEagerStorer(
				final PersistenceTypeHandlerManager<Binary> typeManager       ,
				final PersistenceObjectManager              objectManager     ,
				final ObjectSwizzling                       objectRetriever   ,
				final PersistenceTarget<Binary>             target            ,
				final BufferSizeProviderIncremental         bufferSizeProvider
			)
			{
				final BinaryStorer.Eager storer = new BinaryStorer.Eager(
					objectManager         ,
					objectRetriever       ,
					typeManager           ,
					target                ,
					bufferSizeProvider    ,
					this.channelCount()   ,
					this.switchByteOrder()
				);
				objectManager.registerLocalRegistry(storer);
				
				return storer;
			}

		}
				
	}

}
