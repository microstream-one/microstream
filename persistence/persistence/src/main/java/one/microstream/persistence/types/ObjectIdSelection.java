package one.microstream.persistence.types;

/*-
 * #%L
 * MicroStream Persistence
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

import one.microstream.collections.Set_long;
import one.microstream.functional._longPredicate;

public interface ObjectIdSelection extends _longPredicate
{
	@Override
	public boolean test(long objectId);



	public static ObjectIdSelection New(final Set_long objectIds)
	{
		return new ObjectIdSelection.Default(
			notNull(objectIds)
		);
	}

	public final class Default implements ObjectIdSelection
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final Set_long objectIds;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final Set_long objectIds)
		{
			super();
			this.objectIds = objectIds;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final boolean test(final long objectId)
		{
			return this.objectIds.contains(objectId);
		}

	}

}
