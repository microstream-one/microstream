
package one.microstream.cache;

import java.io.Closeable;
import java.lang.reflect.Field;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.X;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryPersistence;
import one.microstream.persistence.binary.types.BinaryPersistenceFoundation;
import one.microstream.persistence.exceptions.PersistenceExceptionTransfer;
import one.microstream.persistence.types.PersistenceContextDispatcher;
import one.microstream.persistence.types.PersistenceIdSet;
import one.microstream.persistence.types.PersistenceManager;
import one.microstream.persistence.types.PersistenceSource;
import one.microstream.persistence.types.PersistenceTarget;
import one.microstream.persistence.types.PersistenceTypeDictionaryManager;
import one.microstream.persistence.types.PersistenceTypeHandlerManager;
import one.microstream.storage.types.Database;
import one.microstream.util.traversing.ObjectGraphTraverser;


public interface Serializer extends Closeable
{
	public Binary write(Object object);
	
	public Object read(Binary data);
	
	@Override
	public void close();
	
	public static Serializer get(final ClassLoader classLoader)
	{
		return Static.get(classLoader, CacheConfiguration.DefaultSerializerFieldPredicate());
	}
	
	public static Serializer get(final ClassLoader classLoader, final Predicate<? super Field> fieldPredicate)
	{
		return Static.get(classLoader, fieldPredicate);
	}
	
	public static class Static
	{
		private final static WeakHashMap<ClassLoader, Serializer> cache = new WeakHashMap<>();
				
		static synchronized Serializer get(final ClassLoader classLoader, final Predicate<? super Field> fieldPredicate)
		{
			return cache.computeIfAbsent(
				classLoader,
				cl -> new Serializer.Default(fieldPredicate)
			);
		}
		
		private Static()
		{
			throw new Error();
		}
	}
	
	public static class Default implements Serializer
	{
		private final Predicate<? super Field> fieldPredicate;
		private PersistenceManager<Binary>     persistenceManager;
		private ObjectGraphTraverser           typeHandlerEnsurer;
		private Binary                         input;
		private Binary                         output;
		
		Default(final Predicate<? super Field> fieldPredicate)
		{
			super();
			
			this.fieldPredicate = fieldPredicate;
		}
		
		@Override
		public synchronized Binary write(final Object object)
		{
			this.lazyInit();
			this.typeHandlerEnsurer.traverse(object);
			this.persistenceManager.store(object);
			return this.output;
		}
		
		@Override
		public synchronized Object read(final Binary data)
		{
			this.lazyInit();
			this.input = data;
			return this.persistenceManager.get();
		}
		
		@Override
		public synchronized void close()
		{
			if(this.persistenceManager != null)
			{
				this.persistenceManager.objectRegistry().truncateAll();
				this.persistenceManager.close();
				this.persistenceManager = null;
				this.input              = null;
				this.output             = null;
				this.typeHandlerEnsurer = null;
			}
		}
		
		private void lazyInit()
		{
			if(this.persistenceManager == null)
			{
				final PersistenceSourceBinary source = ()   -> X.Constant(this.input);
				final PersistenceTargetBinary target = data -> this.output = data;
				
				final BinaryPersistenceFoundation<?> foundation = BinaryPersistence.Foundation()
					.setPersister(Database.New(Serializer.class.getName()))
					.setPersistenceSource(source)
					.setPersistenceTarget(target)
					.setContextDispatcher(
						PersistenceContextDispatcher.LocalObjectRegistration()
					);
				
				foundation.setTypeDictionaryManager(
					PersistenceTypeDictionaryManager.Transient(
						foundation.getTypeDictionaryCreator()
					)
				);
				
				final PersistenceTypeHandlerManager<Binary> typeHandlerManager = foundation.getTypeHandlerManager();
				typeHandlerManager.initialize();
				
				this.persistenceManager = foundation.createPersistenceManager();
				
				final Consumer<Object> objectAcceptor = obj -> {
					if(obj != null)
					{
						typeHandlerManager.ensureTypeHandler(obj);
					}
				};
				this.typeHandlerEnsurer = ObjectGraphTraverser.Builder()
					.modeFull()
					.fieldPredicate(this.fieldPredicate)
					.acceptorLogic(objectAcceptor)
					.buildObjectGraphTraverser();
			}
			else
			{
				this.persistenceManager.objectRegistry().clearAll();
			}
		}
		
		
		static interface PersistenceSourceBinary extends PersistenceSource<Binary>
		{
			@Override
			default XGettingCollection<? extends Binary> readByObjectIds(final PersistenceIdSet[] oids)
				throws PersistenceExceptionTransfer
			{
				return null;
			}
		}
		
		
		static interface PersistenceTargetBinary extends PersistenceTarget<Binary>
		{
			@Override
			default boolean isWritable()
			{
				return true;
			}
		}
		
	}
	
}
