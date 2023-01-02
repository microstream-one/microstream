package one.microstream.io;

/*-
 * #%L
 * microstream-base
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Wrapper for {@link ByteBuffer}s to be used as an {@link InputStream}.
 * <p>
 * If you need {@link InputStream#mark(int)} and {@link InputStream#reset()} to work,
 * simply wrap this one in a {@link BufferedInputStream}.
 *
 */
public final class ByteBufferInputStream extends InputStream
{
	public static ByteBufferInputStream New(
		final ByteBuffer sourceBuffer
	)
	{
		return new ByteBufferInputStream(
			Arrays.asList(notNull(sourceBuffer))
		);
	}

	public static ByteBufferInputStream New(
		final Iterable<? extends ByteBuffer> sourceBuffers
	)
	{
		return new ByteBufferInputStream(
			notNull(sourceBuffers)
		);
	}


	private final List<ByteBuffer> sourceBuffers         ;
	private       int              currentBufferIndex = 0;

	private ByteBufferInputStream(
		final Iterable<? extends ByteBuffer> sourceBuffers
	)
	{
		super();
		this.sourceBuffers = new ArrayList<>();
		sourceBuffers.forEach(this.sourceBuffers::add);
	}

	private synchronized int internalRead(
		final Function<ByteBuffer, Integer> reader
	)
	{
		if(this.sourceBuffers == null
		|| this.currentBufferIndex >= this.sourceBuffers.size()
		)
		{
			return -1;
		}

		ByteBuffer currentBuffer = this.sourceBuffers.get(this.currentBufferIndex);
		while(!currentBuffer.hasRemaining())
		{
			if(++this.currentBufferIndex >= this.sourceBuffers.size())
			{
				return -1;
			}
			currentBuffer = this.sourceBuffers.get(this.currentBufferIndex);
		}

		return reader.apply(currentBuffer);
	}

	@Override
	public synchronized int available() throws IOException
	{
		int available = 0;
		for(int i = this.currentBufferIndex, c = this.sourceBuffers.size(); i < c; i++)
		{
			available += this.sourceBuffers.get(i).remaining();
		}
		return available;
	}

	@Override
	public int read() throws IOException
	{
		return this.internalRead(
			buffer -> buffer.get() & 0xFF
		);
	}

	@Override
	public int read(
		final byte[] bytes ,
		final int    offset,
		final int    length
	)
	throws IOException
	{
		return this.internalRead(buffer ->
		{
			final int amount = Math.min(length, buffer.remaining());
			buffer.get(bytes, offset, amount);
			return amount;
		});
	}

}
