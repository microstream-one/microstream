package one.microstream.afs.types;

/*-
 * #%L
 * microstream-afs
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

import one.microstream.chars.VarString;
import one.microstream.chars.XChars;

public interface AItem
{
	public AFileSystem fileSystem();
	
	/**
	 * The directory (identifying container) in which this item is located and in which
	 * no other item can have the same {@link #identifier()} as this item.
	 * 
	 * @see #identifier()
	 * @see #toPathString()
	 * 
	 * @return the item's parent directory.
	 */
	public ADirectory parent();
	
	/**
	 * The value that uniquely identifies the item globally in the whole file system.
	 * <p>
	 * Note that this value is usually a combination of the identifiers of {@link #parent()} directories
	 * and the local {@link #identifier()}, but such a relation is not mandatory.
	 * 
	 * @see #parent()
	 * @see #identifier()
	 * 
	 * @return the item's globally unique identifier.
	 */
	public String toPathString();
	
	public String[] toPath();

	/**
	 * The value that uniquely identifies the item locally in its {@link #parent()} directory.
	 * 
	 * @see #parent()
	 * @see #toPathString()
	 * 
	 * @return the item's locally unique identifier.
	 */
	public String identifier();
	
	/**
	 * Queries whether the item represented by this instance actually physically exists on the underlying storage layer.
	 * 
	 * @return whether the item exists.
	 */
	public boolean exists();
		
	
	public static char defaultSeparator()
	{
		return '/';
	}

	
	public static String[] buildItemPath(final AItem item)
	{
		// doing the quick loop twice is enormously faster than populating a dynamically growing collection.
		int depth = 0;
		for(AItem i = item; i != null; i = i.parent())
		{
			depth++;
		}
		
		final String[] path = new String[depth];
		for(AItem i = item; i != null; i = i.parent())
		{
			path[--depth] = i.identifier();
		}
		
		return path;
	}
	
	public static VarString assembleDebugString(final AItem item, final VarString vs)
	{
		XChars.addSystemString(item, vs);
		vs.add('(').add('"');
		XChars.appendArraySeperated(vs, AItem.defaultSeparator(), (Object[])AItem.buildItemPath(item));
		vs.add('"').add(')');
		
		return vs;
	}
	
	
	public abstract class Base implements AItem
	{
		
		@Override
		public String toString()
		{
			final VarString vs = VarString.New(50);
			AItem.assembleDebugString(this, vs);
			
			return vs.toString();
		}
	}
	
	public abstract class Abstract extends AItem.Base
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final String identifier;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Abstract(final String identifier)
		{
			super();
			this.identifier = notNull(identifier);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
				
		@Override
		public final String identifier()
		{
			return this.identifier;
		}
		
		protected final Object mutex()
		{
			// must be accessible externally, anyway, because of observer calling.
			return this;
		}
		
	}
	
		
	
	public static AItem actual(final AItem item)
	{
		return item instanceof AItem.Wrapper
			? ((AItem.Wrapper)item).actual()
			: item
		;
	}
	
	public interface Wrapper extends AItem
	{
		public AItem actual();
						
		public Object user();
	}
		
}
