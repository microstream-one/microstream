package one.microstream.chars;

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

import java.util.function.Consumer;

public final class StringSubstituter implements StringStamper
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	private static Entry[] newSlots(final int slotsLength)
	{
		return new Entry[slotsLength];
	}

	public static final  StringSubstituter New()
	{
		return new StringSubstituter();
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private Entry[] slots = newSlots(1);
	private int     range;
	private int     size ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	StringSubstituter()
	{
		super();
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	private void synchRebuildHashTable(final int newLength)
	{
		final int newRange = newLength == Integer.MAX_VALUE ? newLength : newLength - 1;
		final Entry[] oldSlots = this.slots, newSlots = newSlots(newLength);
		for(int i = 0; i < oldSlots.length; i++)
		{
			if(oldSlots[i] == null)
			{
				continue;
			}
			for(Entry next, entry = oldSlots[i]; entry != null; entry = next)
			{
				next = entry.link;
				entry.link = newSlots[entry.hash & newRange];
				newSlots[entry.hash & newRange] = entry;
			}
		}
		this.slots = newSlots;
		this.range = newRange;
	}

	private void synchIncrement()
	{
		if(++this.size >= this.range)
		{
			this.synchRebuildHashTable((int)(this.slots.length * 2.0f));
		}
	}

	private void synchDecrement()
	{
		// check for hash table downsizing
		if(--this.size << 1 < this.slots.length)
		{
			this.synchRebuildHashTable(this.slots.length >> 1);
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	public final synchronized String substitute(final String item)
	{
		if(item == null)
		{
			return null;
		}
		final int hash = item.hashCode();
		for(Entry e = this.slots[this.range & hash]; e != null; e = e.link)
		{
			if(e.hash == hash && e.item.equals(item))
			{
				return e.item;
			}
		}
		this.slots[this.range & hash] = new Entry(hash, item, this.slots[this.range & hash]);
		this.synchIncrement();
		return item;
	}

	private static boolean equals(final char[] sChars, final char[] chars, final int offset, final int length)
	{
		for(int i = 0; i < length; i++)
		{
			if(sChars[i] != chars[offset + i])
			{
				return false;
			}
		}
		return true;
	}

	public final synchronized String substitute(final char[] chars, final int offset, final int length)
	{
		XChars.validateRange(chars, offset, length);
		if(length == 0)
		{
			return this.substitute("");
		}
		final int hash = XChars.internalHashCode(chars, offset, length);
		for(Entry e = this.slots[this.range & hash]; e != null; e = e.link)
		{
			if(e.hash == hash && e.item.length() == length && equals(XChars.readChars(e.item), chars, offset, length))
			{
				return e.item;
			}
		}
		final String item = new String(chars, offset, length);
		this.slots[this.range & hash] = new Entry(hash, item, this.slots[this.range & hash]);
		this.synchIncrement();
		return item;
	}

	public final synchronized void clear()
	{
		this.size = this.range = 0;
		this.slots = newSlots(1);
	}

	public final synchronized StringSubstituter iterate(final Consumer<? super String> procedure)
	{
		final Entry[] hashSlots = this.slots;
		for(int i = 0; i < hashSlots.length; i++)
		{
			if(hashSlots[i] == null)
			{
				continue;
			}
			for(Entry entry = hashSlots[i]; entry != null; entry = entry.link)
			{
				procedure.accept(entry.item);
			}
		}
		return this;
	}

	public final synchronized boolean contains(final String item)
	{
		final int hash = item.hashCode();
		for(Entry e = this.slots[this.range & hash]; e != null; e = e.link)
		{
			if(e.hash == hash && e.item.equals(item))
			{
				return true;
			}
		}
		return false;
	}

	public final synchronized String remove(final String item)
	{
		final int hash = item.hashCode();
		Entry last = this.slots[this.range & hash];
		if(last.item.equals(item))
		{
			this.slots[this.range & hash] = last.link;
			this.synchDecrement();
			return last.item;
		}
		for(Entry e; (e = last.link) != null; last = e)
		{
			/* (04.04.2016 TM)NOTE:
			 * removed "e.item == item" to avoid FindBugs false positive.
			 * Performaned by String#equals anyway.
			 */
			if(e.hash == hash && e.item.equals(item))
			{
				last = e.link;
				this.synchDecrement();
				return last.item;
			}
		}
		return null;
	}

	@Override
	public final String stampString(final char[] chars, final int offset, final int length)
	{
		return this.substitute(chars, offset, length);
	}

	static final class Entry
	{
		final int    hash;
		final String item;
		      Entry  link;

		Entry(final int hash, final String item, final Entry next)
		{
			super();
			this.hash = hash;
			this.item = item;
			this.link = next;
		}

	}

}
