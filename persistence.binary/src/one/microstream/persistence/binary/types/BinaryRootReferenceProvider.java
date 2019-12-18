package one.microstream.persistence.binary.types;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import java.util.function.Supplier;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustom;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceObjectRegistry;
import one.microstream.persistence.types.PersistenceRootReference;
import one.microstream.persistence.types.PersistenceRootReferenceProvider;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.persistence.types.PersistenceTypeHandler;

public interface BinaryRootReferenceProvider<R extends PersistenceRootReference>
extends
PersistenceRootReferenceProvider<Binary>,
PersistenceTypeHandler<Binary, R>
{
	public static BinaryRootReferenceProvider<PersistenceRootReference.Default> New(
		final Supplier<?>               rootSupplier  ,
		final PersistenceObjectRegistry globalRegistry
	)
	{
		return New(
			PersistenceRootReference.New(rootSupplier),
			globalRegistry
		);
	}
	
	public static BinaryRootReferenceProvider<PersistenceRootReference.Default> New(
		final PersistenceRootReference.Default instance      ,
		final PersistenceObjectRegistry        globalRegistry
	)
	{
		return new BinaryRootReferenceProvider.Default(
			mayNull(instance),
			notNull(globalRegistry)
		);
	}
	
	public final class Default
	extends AbstractBinaryHandlerCustom<PersistenceRootReference.Default>
	implements BinaryRootReferenceProvider<PersistenceRootReference.Default>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		/**
		 * The handler instance directly knowing the global registry might suprise at first and seem like a shortcut hack.
		 * However, when taking a closer look at the task of this handler: (globally) resolving global root instances,
		 * it becomes clear that a direct access for registering resolved global instances at the global registry is
		 * indeed part of this handler's task.
		 */
		final PersistenceRootReference.Default rootReference ;
		final PersistenceObjectRegistry        globalRegistry;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final PersistenceRootReference.Default instance      ,
			final PersistenceObjectRegistry        globalRegistry
			)
		{
			super(
				PersistenceRootReference.Default.class,
				CustomFields(
					CustomField(Object.class, "root")
					)
				);
			this.rootReference       = instance      ;
			this.globalRegistry = globalRegistry;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		static long getRootObjectId(final Binary bytes)
		{
			return bytes.read_long(0);
		}

		@Override
		public final void store(
			final Binary                           bytes   ,
			final PersistenceRootReference.Default instance,
			final long                             objectId,
			final PersistenceStoreHandler          handler
			)
		{
			// root instance may even be null. Probably just temporarily to "truncate" a database or something like that.
			final Object rootInstance  = instance.get()             ;
			final long   contentLength = Binary.objectIdByteLength();
			final long   rootObjectId  = handler.apply(rootInstance);
			bytes.storeEntityHeader(contentLength, this.typeId(), objectId);
			bytes.store_long(rootObjectId);
		}

		@Override
		public final PersistenceRootReference.Default create(
			final Binary                      bytes     ,
			final PersistenceObjectIdResolver idResolver
			)
		{
			final Object rootInstance = this.rootReference.get();
			if(rootInstance != null)
			{
				/*
				 * If the singleton instance references a defined root object, it must be registered for the persisted
				 * objectId, even if the id references a record of different, incompatible, type. This conflict
				 * has to be recognized and reported in the corresponding type handler, but the defined root instance
				 * must have the persisted root object id associated in any case. Otherwise, there would be an
				 * inconsistency: a generic instance would be created for the persisted record and be generically
				 * registered with the persistetd object id, thus leaving no object id for the actually defined root
				 * instance to be registered/associated with.
				 * If no rootInstance is defined, there is no such conflict. The generic instance of whatever type
				 * gets created and registered and can be queried by the application logic after initialization is
				 * complete.
				 */
				final long rootObjectId = getRootObjectId(bytes);
				idResolver.registerRoot(this.rootReference.get(), rootObjectId);
			}

			// instance is a singleton. Hence, no instance is created, here, but the singleton is returned.
			return this.rootReference;
		}

		@Override
		public final void update(
			final Binary                           bytes   ,
			final PersistenceRootReference.Default instance,
			final PersistenceObjectIdResolver      handler
			)
		{
			final Object rootInstance = this.rootReference.get();
			if(rootInstance == null)
			{
				/*
				 * If the instance has no explicit root instance set, a
				 * generically loaded and instantiated root instance is set.
				 */
				final long   rootObjectId = getRootObjectId(bytes);
				final Object loadedRoot   = handler.lookupObject(rootObjectId);
				this.rootReference.setRoot(loadedRoot);

				return;
			}

			// (10.12.2019 TM)FIXME: priv#194
		}


		@Override
		public final void iterateInstanceReferences(
			final PersistenceRootReference.Default instance,
			final PersistenceFunction              iterator
			)
		{
			instance.iterate(iterator);
		}

		@Override
		public final boolean hasInstanceReferences()
		{
			return true;
		}

		@Override
		public final void iterateLoadableReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
		{
			// (10.12.2019 TM)FIXME: priv#194
		}

		@Override
		public final boolean hasVaryingPersistedLengthInstances()
		{
			return false;
		}

		@Override
		public final boolean hasPersistedReferences()
		{
			return true;
		}

		@Override
		public final boolean hasPersistedVariableLength()
		{
			return false;
		}
		
		@Override
		public PersistenceRootReference provideRootReference()
		{
			return this.rootReference;
		}
		
		@Override
		public PersistenceTypeHandler<Binary, ? extends PersistenceRootReference> provideTypeHandler()
		{
			return this;
		}
		
	}
	
}
