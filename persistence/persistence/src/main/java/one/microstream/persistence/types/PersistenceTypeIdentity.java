package one.microstream.persistence.types;

/*-
 * #%L
 * microstream-persistence
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


public interface PersistenceTypeIdentity extends PersistenceTypeIdOwner
{
	@Override
	public long typeId();

	public String typeName();


	public static int hashCode(final PersistenceTypeIdentity typeIdentity)
	{
		return Long.hashCode(typeIdentity.typeId()) & typeIdentity.typeName().hashCode();
	}

	public static boolean equals(
		final PersistenceTypeIdentity ti1,
		final PersistenceTypeIdentity ti2
	)
	{
		return ti1 == ti2
			|| ti1 != null && ti2 != null
			&& ti1.typeId() == ti2.typeId()
			&& ti1.typeName().equals(ti2.typeName())
		;
	}

}
