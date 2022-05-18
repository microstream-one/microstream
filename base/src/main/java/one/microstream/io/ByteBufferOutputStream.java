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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public final class ByteBufferOutputStream extends OutputStream
{
	public static ByteBufferOutputStream New(
		final ByteBuffer targetBuffer
	)
	{
		return new ByteBufferOutputStream(
			notNull(targetBuffer)
		);
	}


	private final ByteBuffer targetBuffer;

	private ByteBufferOutputStream(
		final ByteBuffer targetBuffer
	)
	{
		super();
		this.targetBuffer = targetBuffer;
	}

	@Override
	public void write(
		final int b
	)
	throws IOException
	{
		this.targetBuffer.put((byte)b);
	}

	@Override
	public void write(
		final byte[] bytes ,
		final int    offset,
		final int    length
	)
	throws IOException
	{
		notNull(bytes);
		if(offset < 0
		|| offset > bytes.length
		|| length < 0
		|| offset + length > bytes.length
		|| offset + length < 0)
		{
			throw new IndexOutOfBoundsException();
		}
		if(length == 0)
		{
			return;
		}

		this.targetBuffer.put(bytes, offset, length);
	}

}
