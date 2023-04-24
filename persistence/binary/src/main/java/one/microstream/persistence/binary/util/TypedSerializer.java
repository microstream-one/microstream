package one.microstream.persistence.binary.util;

/*-
 * #%L
 * microstream-persistence-binary
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

import static one.microstream.X.notNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;

import one.microstream.X;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.ChunksWrapper;
import one.microstream.persistence.binary.util.Serializer.Default.SerializerStorer;
import one.microstream.persistence.types.PersistenceManager;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.Storer;
import one.microstream.util.logging.Logging;

/**
 * Convenient API layer to use the binary persistence functionality for a serializer
 * that includes type informations in the serialized data.
 * <p>
 * It is based on a {@link SerializerFoundation}, which can be configured to various needs.
 * <p>
 * Per default {@link Binary} and <code>byte[]</code> are supported as medium types.
 * <p>
 * The included type information can be configured by supplying a {@link SerializerTypeInfoStrategy}
 * using a {@link SerializerTypeInfoStrategyCreator} to the {@link SerializerFoundation}.
 *
 * @param <M> the medium type
 */
public interface TypedSerializer<M> extends Serializer<M>
{
	/**
	 * Create a new TypedSerializer instance using the default configuration.
	 * The serialized data is supplied as MicroStream {@link Binary}.
	 * The serializer will include the whole set of current type information
	 * in every serialized Binary.
	 * 
	 * @return A new TypedSerializer instance.
	 */
	public static Serializer<Binary> Binary()
	{
		return Binary(SerializerFoundation.New());
	}
	
	/**
	 * Create a new TypedSerializer instance based upon the supplied
	 * {@link SerializerFoundation}.
	 * The serialized data is supplied as byte array.
	 * The serializer will include the whole set of current type information
	 * in every serialized Binary.
	 * 
	 * @param foundation the foundation to base the serializer on
	 * @return A new TypedSerializer instance.
	 */
	public static Serializer<Binary> Binary(final SerializerFoundation<?> foundation)
	{
		return New(
			foundation         ,
			Function.identity(),
			Function.identity()
		);
	}
	
	/**
	 * Create a new TypedSerializer instance using the default configuration.
	 * The serialized data is supplied as byte array.
	 * The serializer will include the whole set of current type information
	 * in every serialized byte array.
	 * 
	 * @return A new TypedSerializer instance.
	 */
	public static Serializer<byte[]> Bytes()
	{
		return Bytes(SerializerFoundation.New());
	}
	
	/**
	 * Create a new TypedSerializer instance based upon the supplied
	 * {@link SerializerFoundation}.
	 * The serialized data is supplied as byte array.
	 * The serializer will include the whole set of current type information
	 * in every serialized byte array.
	 * 
	 * @param foundation {@link SerializerFoundation} used to configure the serializer instance.
	 * @return A new TypedSerializer instance.
	 */
	public static Serializer<byte[]> Bytes(final SerializerFoundation<?> foundation)
	{
		return New(
			foundation      ,
			Static::toBytes ,
			Static::toBinary
		);
	}
	
	public static <M> Serializer<M> New(
		final Function<Binary, M> toMedium,
		final Function<M, Binary> toBinary
	)
	{
		return New(
			SerializerFoundation.New(),
			toMedium                  ,
			toBinary
		);
	}
		
	public static <M> Serializer<M> New(
		final SerializerFoundation<?> foundation,
		final Function<Binary, M>     toMedium  ,
		final Function<M, Binary>     toBinary
	)
	{
		return new TypedSerializer.Default<>(
			notNull(foundation),
			notNull(toMedium  ),
			notNull(toBinary  )
		);
	}
	
	
	public final static class Static
	{
		public static byte[] toBytes(final Binary binary)
		{
			int overallLength = 0;
			for(final ByteBuffer source : binary.buffers())
			{
				overallLength += source.remaining() + XMemory.byteSize_int();
			}
			
			final byte[] bytes = new byte[overallLength];
			int pos = 0;
			for(final ByteBuffer source : binary.buffers())
			{
				final int length                = source.remaining();
				final int currentSourcePosition = source.position();
				
				XMemory.set_intInBytes(bytes, pos, length);
				pos += XMemory.byteSize_int();
				source.get(bytes, pos, length);
				pos += length;
							
				source.position(currentSourcePosition);
			}
			return bytes;
		}
				
		public static Binary toBinary(final byte[] bytes)
		{
			//first byte has the source byte order coded: 0 = LITTLE_ENDIAN, otherwise BIG_ENDIAN
			final byte b = bytes[XMemory.byteSize_int()];
			final ByteOrder byteOrder = b == 0x0 ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
			
			final ByteBuffer bb = ByteBuffer.wrap(bytes).order(byteOrder);
			final List<ByteBuffer> buffers = new ArrayList<>();
						
			while(bb.hasRemaining()) {
				final int contentSize = bb.getInt();
				final ByteBuffer contentBuffer = XMemory.allocateDirectNative(contentSize);
				contentBuffer.put(bytes, bb.position(), contentSize);
				bb.position(bb.position() + contentSize);
				buffers.add(contentBuffer);
			}

			return ChunksWrapper.New(buffers.toArray(new ByteBuffer[0]));
		}
		
		/**
		 * Dummy constructor to prevent instantiation of this static-only utility class.
		 *
		 * @throws UnsupportedOperationException when called
		 */
		private Static()
		{
			// static only
			throw new UnsupportedOperationException();
		}
	}

		
	public static class Default<M> implements TypedSerializer<M>
	{
		private static class TypeInfoCache
		{
			private final ByteBuffer[] cachedTypeInfoBuffers;
			private final long lastTypeInfoTimeStamp;
			
			public TypeInfoCache(final ByteBuffer[] cachedTypeInfoBuffers, final long lastTypeInfoTimeStamp)
			{
				super();
				this.cachedTypeInfoBuffers = cachedTypeInfoBuffers;
				this.lastTypeInfoTimeStamp = lastTypeInfoTimeStamp;
			}

			public ByteBuffer[] getCachedTypeInfoBuffers()
			{
				return this.cachedTypeInfoBuffers;
			}

			public long getLastTypeInfoTimeStamp()
			{
				return this.lastTypeInfoTimeStamp;
			}
		}
		
		
		private final static Logger logger = Logging.getLogger(Serializer.class);
		
		private final SerializerFoundation<?>         foundation                 ;
		private final Function<Binary, M>             toMedium                   ;
		private final Function<M, Binary>             toBinary                   ;
		private PersistenceManager<Binary>            persistenceManager         ;
		private Storer                                storer                     ;
		private Binary                                input                      ;
		private Binary                                output                     ;
		private TypeDefinitionBuilder                 typeDefintionBuilder       ;
		private TypeDefinitionImporter                typeDefinitionImporter     ;
		private SerializerTypeInfoStrategy			  typeInfoStrategy           ;
		private TypeInfoCache                         typeInfoCache              ;
		private long                                  lastTypeInfoImportTimeStamp;
				
				
		Default(
			final SerializerFoundation<?> foundation,
			final Function<Binary, M>     toMedium  ,
			final Function<M, Binary>     toBinary
		)
		{
			super();
			this.foundation = foundation;
			this.toMedium   = toMedium  ;
			this.toBinary   = toBinary  ;
			this.initialize();
		}
		
		@Override
		public synchronized M serialize(final Object object)
		{
			//serialize data
			this.storer.store(object);
			this.storer.commit();
			final ByteBuffer[] dataBuffers = this.output.buffers();
			
			final ByteBuffer[] typeInfoBuffers = this.updateTypeInfo();
						
			final ByteBuffer headerBuffer = XMemory.allocateDirectNative(
				XMemory.byteSize_byte() +
				XMemory.byteSize_int() +
				XMemory.byteSize_long());
			
			//set byteOrder as first byte in the output
			headerBuffer.put((byte)(XMemory.nativeByteOrder() == ByteOrder.LITTLE_ENDIAN ? 0 : 1));
			headerBuffer.putInt(typeInfoBuffers.length);
			headerBuffer.putLong(this.typeInfoCache.getLastTypeInfoTimeStamp());
			headerBuffer.rewind();
			
			//preparer output buffers
			final ByteBuffer[] buffers = new ByteBuffer[1 + dataBuffers.length + typeInfoBuffers.length];
		
			buffers[0] = headerBuffer;
			System.arraycopy(typeInfoBuffers, 0, buffers, 1                        , typeInfoBuffers.length);
			System.arraycopy(dataBuffers,     0, buffers, 1+ typeInfoBuffers.length, dataBuffers.length);
						
			if(this.typeInfoStrategy.includeOnce())
			{
				this.typeInfoCache = new TypeInfoCache(new ByteBuffer[0], 0);
			}
			
			return this.toMedium.apply(ChunksWrapper.New(buffers));
		}
		
		private ByteBuffer[] updateTypeInfo()
		{
			if(this.typeInfoStrategy.hasUpdate() || this.typeInfoCache == null)
			{
				this.storer.store(this.typeInfoStrategy.get());
				this.storer.commit();
				this.typeInfoCache = new TypeInfoCache(
					this.output.buffers(),
					System.nanoTime());
			}
			
			return this.typeInfoCache.getCachedTypeInfoBuffers();
		}

		@SuppressWarnings("unchecked")
		@Override
		public synchronized <T> T deserialize(final M data)
		{
			final Binary in = this.toBinary.apply(data);
			
			//skip byteOder byte, not needed here
			in.buffers()[0].position(XMemory.byteSize_byte());
			final int typeInfoCount = in.buffers()[0].getInt();
			final long typeInfoTimeStamp = in.buffers()[0].getLong();
			
			if(typeInfoCount > 0 && this.lastTypeInfoImportTimeStamp < typeInfoTimeStamp)
			{
				logger.debug("importing type information from input");
				
				this.input = ChunksWrapper.New(Arrays.copyOfRange(in.buffers(), 1, typeInfoCount + 1));
				final SerializerTypeInfo typeInfo = (SerializerTypeInfo)this.persistenceManager.get();
							
				for (final String type : typeInfo.getSerializedTypes())
				{
					final XGettingSequence<PersistenceTypeDefinition> typeDefs = this.typeDefintionBuilder.buildTypeDefinitions(type);
					this.typeDefinitionImporter.importTypeDefinitions(typeDefs);
				}
				
				this.lastTypeInfoImportTimeStamp = typeInfoTimeStamp;
			}
			
			this.input = ChunksWrapper.New(Arrays.copyOfRange(in.buffers(), typeInfoCount + 1, in.buffers().length));
			final T content = (T)this.persistenceManager.get();
			
			return content;
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
			}
		}
		
		private void initialize()
		{
			if(this.persistenceManager == null)
			{
				final Source source = ()   -> X.Constant(this.input);
				final Target target = data -> this.output = data    ;
								
				this.foundation.registerEntityType(SerializerTypeInfo.class);
				this.persistenceManager = this.foundation
					.setPersistenceSource(source)
					.setPersistenceTarget(target)
					.createPersistenceManager()
				;
				this.storer             = this.persistenceManager.createStorer(
					new SerializerStorer.Creator(this.foundation.isByteOrderMismatch())
				);
				
				this.typeDefintionBuilder = new TypeDefinitionBuilder.Default(
					this.foundation.getTypeDictionaryParser(),
					this.foundation.getTypeDefinitionCreator(),
					this.foundation.getTypeDescriptionResolverProvider()
				);
				
				this.typeDefinitionImporter = new TypeDefinitionImporter.Default(
					this.foundation.getTypeHandlerManager(),
					this.foundation.getTypeHandlerEnsurer()
				);
			}
			else
			{
				this.persistenceManager.objectRegistry().truncateAll();
			}
					
			this.typeInfoStrategy = this.foundation.getSerializerTypeInfoStrategyCreator().create(this.persistenceManager);
		}
				
	}
	
}
