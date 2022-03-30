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

import one.microstream.collections.HashEnum;
import one.microstream.collections.types.XEnum;

public interface PersistenceRefactoringTypeIdentifierBuilder
{
	public String buildTypeIdentifier(PersistenceTypeDescription typeDescription);
	
	
	
	public static XEnum<? extends PersistenceRefactoringTypeIdentifierBuilder> createDefaultRefactoringLegacyTypeIdentifierBuilders()
	{
		/* Identifier builders in descending order of priority:
		 * - [TypeId]:[TypeName]
		 * - [TypeName]
		 * Note that the first one is the only one that is unambiguous for all cases.
		 */
		return HashEnum.New(
			PersistenceTypeDescription::buildTypeIdentifier,
			PersistenceTypeDescription::typeName
		);
	}
	
}
