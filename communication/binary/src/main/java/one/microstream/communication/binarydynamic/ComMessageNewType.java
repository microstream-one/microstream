package one.microstream.communication.binarydynamic;

/*-
 * #%L
 * MicroStream Communication Binary
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

import one.microstream.chars.VarString;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDictionaryAssembler;

public class ComMessageNewType implements ComMessage
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private String typeEntry;
	private transient PersistenceTypeDefinition typeDefinition;
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComMessageNewType(final PersistenceTypeDefinition typeDefinition)
	{
		this.typeDefinition = typeDefinition;
		this.typeEntry = "";
		final PersistenceTypeDictionaryAssembler assembler = PersistenceTypeDictionaryAssembler.New();
		
		final VarString vc = VarString.New();
		assembler.assembleTypeDescription(vc, typeDefinition);
		this.typeEntry = vc.toString();
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public String typeEntry()
	{
		return this.typeEntry;
	}


	public PersistenceTypeDefinition typeDefinition() 
	{
		return this.typeDefinition;
	}
	
	
}
