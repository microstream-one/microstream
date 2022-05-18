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

import static one.microstream.X.KeyValue;
import static one.microstream.X.notNull;

import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XGettingTable;
import one.microstream.typing.KeyValue;

// used to automate and hide simple type path/name changes to provide backwards compatibility
public interface PersistenceTypeNameMapper
{
	public String mapClassName(String oldClassName);
	
	public String mapInterfaceName(String oldInterfaceName);
	
	
	public static PersistenceTypeNameMapper New()
	{
		return New(
			Defaults.defaultClassNameMappings(),
			Defaults.defaultInterfaceNameMappings()
		);
	}
		
	public static PersistenceTypeNameMapper New(
		final XGettingTable<String, String> classNameMapping    ,
		final XGettingTable<String, String> interfaceNameMapping
	)
	{
		return new PersistenceTypeNameMapper.Default(
			notNull(classNameMapping),
			notNull(interfaceNameMapping)
		);
	}
	
	public final class Default implements PersistenceTypeNameMapper
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final XGettingTable<String, String> classNameMapping    ;
		private final XGettingTable<String, String> interfaceNameMapping;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final XGettingTable<String, String> classNameMapping    ,
			final XGettingTable<String, String> interfaceNameMapping
		)
		{
			super();
			this.classNameMapping     = classNameMapping    ;
			this.interfaceNameMapping = interfaceNameMapping;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final String mapClassName(final String oldClassName)
		{
			return this.classNameMapping.get(oldClassName);
		}
		
		@Override
		public final String mapInterfaceName(final String oldInterfaceName)
		{
			return this.interfaceNameMapping.get(oldInterfaceName);
		}
		
	}
	
	public interface Defaults
	{
		/*
		 * Note on Lazy type history:
		 * 
		 * 1.)
		 * one.microstream.persistence.lazy.Lazy was a class
		 * 
		 * 2.)
		 * one.microstream.persistence.lazy.Lazy became an interface
		 * one.microstream.persistence.lazy.Lazy$Default was the class
		 * 
		 * 3.)
		 * one.microstream.persistence.lazy.Lazy         refactored to one.microstream.reference.Lazy
		 * one.microstream.persistence.lazy.Lazy$Default refactored to one.microstream.reference.Lazy$Default
		 * 
		 * Don't create cycles!
		 */
		
		public static XGettingTable<String, String> defaultClassNameMappings()
		{
			return EqHashTable.New(
				microstreamMapping("persistence.lazy.Lazy"        , "reference.Lazy$Default"),
				microstreamMapping("persistence.lazy.Lazy$Default", "reference.Lazy$Default")
			);
		}
		
		public static XGettingTable<String, String> defaultInterfaceNameMappings()
		{
			return EqHashTable.New(
				microstreamMapping("persistence.lazy.Lazy", "reference.Lazy")
			);
		}
		
		public static KeyValue<String, String> microstreamMapping(final String s1, final String s2)
		{
			return KeyValue("one.microstream." + s1, "one.microstream." + s2);
		}
		
	}
	
}
