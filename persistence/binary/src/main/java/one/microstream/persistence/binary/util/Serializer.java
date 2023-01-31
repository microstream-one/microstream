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
import java.util.function.Function;

import one.microstream.X;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.ChunksWrapper;
import one.microstream.persistence.exceptions.PersistenceExceptionTransfer;
import one.microstream.persistence.types.PersistenceIdSet;
import one.microstream.persistence.types.PersistenceManager;
import one.microstream.persistence.types.PersistenceSource;
import one.microstream.persistence.types.PersistenceTarget;
import one.microstream.persistence.types.Storer;

/**
 * Convenient API layer to use the binary persistence functionality for a simple serializer.
 * <p>
 * It is based on a {@link SerializerFoundation}, which can be configured to various needs.
 * <p>
 * Per default {@link Binary} and <code>byte[]</code> are supported as medium types.
 *
 * @param <M> the medium type
 */
public interface Serializer<M> extends AutoCloseable
{
	/**
	 * Serializes the given object graph into the medium type.
	 * @param object the graph's root
	 * @return the binary format
	 */
	public M serialize(Object object);
	
	/**
	 * Recreates an object graph based on the given data.
	 * @param <T> the object's type
	 * @param medium the medium to read from
	 * @return the deserialized object graph
	 */
	public <T> T deserialize(M medium);
	
	
	public static Serializer<Binary> Binary()
	{
		return Binary(SerializerFoundation.New());
	}
	
	public static Serializer<Binary> Binary(final SerializerFoundation<?> foundation)
	{
		return New(
			foundation         ,
			Function.identity(),
			Function.identity()
		);
	}
	
	public static Serializer<byte[]> Bytes()
	{
		return Bytes(SerializerFoundation.New());
	}
	
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
		return new Serializer.Default<>(
			notNull(foundation),
			notNull(toMedium  ),
			notNull(toBinary  )
		);
	}
	
	
	public final static class Static
	{
		public static byte[] toBytes(final Binary binary)
		{
			return XMemory.toArray(binary.buffers());
		}
		
		public static Binary toBinary(final byte[] bytes)
		{
			final ByteBuffer buffer = XMemory.allocateDirectNative(bytes.length);
			buffer.put(bytes);
			return ChunksWrapper.New(buffer);
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
	
	
	public static interface Source extends PersistenceSource<Binary>
	{
		@Override
		default XGettingCollection<? extends Binary> readByObjectIds(final PersistenceIdSet[] oids)
			throws PersistenceExceptionTransfer
		{
			return null;
		}
	}
	
	
	public static interface Target extends PersistenceTarget<Binary>
	{
		@Override
		default boolean isWritable()
		{
			return true;
		}
	}
	
	
	public static class Default<M> implements Serializer<M>
	{
		private final SerializerFoundation<?> foundation        ;
		private final Function<Binary, M>     toMedium          ;
		private final Function<M, Binary>     toBinary          ;
		private PersistenceManager<Binary>    persistenceManager;
		private Storer                        storer            ;
		private Binary                        input             ;
		private Binary                        output            ;
				
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
			this.lazyInit();
		}
		
		@Override
		public synchronized M serialize(final Object object)
		{
			this.storer.store(object);
			this.storer.commit();
			return this.toMedium.apply(this.output);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public synchronized <T> T deserialize(final M data)
		{
			this.input = this.toBinary.apply(data);
			return (T)this.persistenceManager.get();
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
		
		private void lazyInit()
		{
			if(this.persistenceManager == null)
			{
				final Source source = ()   -> X.Constant(this.input);
				final Target target = data -> this.output = data    ;
				
				this.persistenceManager = this.foundation
					.setPersistenceSource(source)
					.setPersistenceTarget(target)
					.createPersistenceManager()
				;
				this.storer = this.persistenceManager.createEagerStorer();
			}
			else
			{
				this.persistenceManager.objectRegistry().truncateAll();
			}
		}
		
	}
	
}
