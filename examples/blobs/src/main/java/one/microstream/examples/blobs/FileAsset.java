
package one.microstream.examples.blobs;

/*-
 * #%L
 * microstream-examples-blobs
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
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

import java.util.UUID;

public class FileAsset
{
	private final String path;
	private final String name;
	private final String uuid;
	
	public FileAsset(final String path, final String name)
	{
		super();
		
		this.path = path;
		this.name = name;
		this.uuid = UUID.randomUUID().toString();
	}
	
	public String getPath()
	{
		return this.path;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public String getUUID()
	{
		return this.uuid;
	}
}
