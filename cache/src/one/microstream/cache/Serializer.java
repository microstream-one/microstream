
package one.microstream.cache;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.function.Consumer;

import one.microstream.X;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryPersistence;
import one.microstream.persistence.binary.types.BinaryPersistenceFoundation;
import one.microstream.persistence.binary.types.ChunksWrapper;
import one.microstream.persistence.exceptions.PersistenceExceptionTransfer;
import one.microstream.persistence.types.PersistenceContextDispatcher;
import one.microstream.persistence.types.PersistenceIdSet;
import one.microstream.persistence.types.PersistenceManager;
import one.microstream.persistence.types.PersistenceSource;
import one.microstream.persistence.types.PersistenceTarget;
import one.microstream.persistence.types.PersistenceTypeDictionaryManager;
import one.microstream.persistence.types.PersistenceTypeHandlerManager;
import one.microstream.reflect.XReflect;
import one.microstream.util.traversing.ObjectGraphTraverser;


public interface Serializer extends Closeable
{
	public byte[] write(Object object);
	
	public Object read(byte[] data);
	
	@Override
	public void close();
	
	public static Serializer New()
	{
		return new Default();
	}
	
	public static class Default implements Serializer
	{
		private SerializerPersistenceSource persistenceSource;
		private SerializerPersistenceTarget persistenceTarget;
		private PersistenceManager<Binary>  persistenceManager;
		private ObjectGraphTraverser        typeHandlerEnsurer;
		
		Default()
		{
			super();
		}
		
		@Override
		public synchronized byte[] write(final Object object)
		{
			this.lazyInit();
			this.typeHandlerEnsurer.traverse(object);
			this.persistenceTarget.reset();
			this.persistenceManager.store(object);
			return this.persistenceTarget.getBytes();
		}
		
		@Override
		public synchronized Object read(final byte[] data)
		{
			this.lazyInit();
			this.persistenceSource.setData(data);
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
				
				this.persistenceSource  = null;
				this.persistenceTarget  = null;
				
				this.typeHandlerEnsurer = null;
			}
		}
		
		private void lazyInit()
		{
			if(this.persistenceManager == null)
			{
				this.persistenceSource = new SerializerPersistenceSource();
				this.persistenceTarget = new SerializerPersistenceTarget();
				
				final BinaryPersistenceFoundation<?> foundation = BinaryPersistence.Foundation()
					.setPersistenceSource(this.persistenceSource)
					.setPersistenceTarget(this.persistenceTarget)
					.setContextDispatcher(
						PersistenceContextDispatcher.LocalObjectRegistration());
				
				foundation.setTypeDictionaryManager(
					PersistenceTypeDictionaryManager.Transient(
						foundation.getTypeDictionaryCreator()));
				
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
					.fieldPredicate(XReflect::isNotTransient)
					.acceptorLogic(objectAcceptor)
					.buildObjectGraphTraverser();
			}
			else
			{
				this.persistenceManager.objectRegistry().clear();
			}
		}
		
		static class SerializerPersistenceSource implements PersistenceSource<Binary>
		{
			private byte[] data;
			
			SerializerPersistenceSource()
			{
				super();
			}
			
			public void setData(final byte[] data)
			{
				this.data = data;
			}
			
			@Override
			public XGettingCollection<? extends Binary> read() throws PersistenceExceptionTransfer
			{
				final ByteBuffer buffer = XMemory.allocateDirectNative(this.data.length);
				buffer.put(this.data);
				buffer.flip();
				return X.<Binary>Constant(ChunksWrapper.New(buffer));
			}
			
			@Override
			public XGettingCollection<? extends Binary> readByObjectIds(final PersistenceIdSet[] oids)
				throws PersistenceExceptionTransfer
			{
				return X.empty();
			}
		}
		
		static class SerializerPersistenceTarget implements PersistenceTarget<Binary>
		{
			private final ByteArrayOutputStream byteArrayOutputStream;
			
			SerializerPersistenceTarget()
			{
				super();
				
				this.byteArrayOutputStream = new ByteArrayOutputStream();
			}
			
			public void reset()
			{
				this.byteArrayOutputStream.reset();
			}
			
			@Override
			public void write(final Binary data) throws PersistenceExceptionTransfer
			{
				try
				{
					final WritableByteChannel channel = Channels.newChannel(this.byteArrayOutputStream);
					
					final ByteBuffer[]        buffers = data.buffers();
					for(final ByteBuffer buffer : buffers)
					{
						while(buffer.hasRemaining())
						{
							channel.write(buffer);
						}
					}
				}
				catch(final IOException e)
				{
					throw new PersistenceExceptionTransfer(e);
				}
			}
			
			public byte[] getBytes()
			{
				return this.byteArrayOutputStream.toByteArray();
			}
		}
		
	}
	
}
