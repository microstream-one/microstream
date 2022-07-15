package echoexample;

/*-
 * #%L
 * microstream-examples-communication-echo
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

import one.microstream.communication.binarydynamic.ComBinaryDynamic;
import one.microstream.communication.types.ComChannel;
import one.microstream.communication.types.ComClient;

public class EchoClient
{
	public static void main(final String[] args)
	{
		//create the client
		final ComClient<?> client = ComBinaryDynamic.Foundation()
			.createClient();
		
		//connect to the host and communicate
		try(final ComChannel channel = client.connect())
		{
			channel.send("Hello Host");
			final Object received = channel.receive();
			System.out.println("received: " + received);
		}
	}
}
