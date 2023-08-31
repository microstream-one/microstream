package one.microstream.storage.types;

/*-
 * #%L
 * microstream-storage
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

import one.microstream.afs.types.AFile;
import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.collections.EqConstHashTable;
import one.microstream.collections.EqHashEnum;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingTable;
import one.microstream.collections.types.XImmutableTable;
import one.microstream.typing.KeyValue;


public interface StorageEntityTypeExportStatistics
{
	public long entityCount();
	
	public long bytesWritten();
	
	public long startTime();

	public long finishTime();
	
	public XGettingTable<Long, ? extends TypeStatistic> typeStatistics();
	
	public XGettingTable<Integer, ? extends ChannelStatistic> channelStatistics();
	
	/**
	 * Collects and returns all exported files.
	 * 
	 * @return all export files
	 * @since 08.00.00
	 */
	public XGettingEnum<AFile> files();
	
	
	
	public final class Default extends AbstractStatistic implements StorageEntityTypeExportStatistics
	{
		private static final String[] TABLE_COLUMN_NAMES =
		{
			"Channel"     ,
			"Entity Count",
			"Byte Count"  ,
			"Start Time"  ,
			"Finish Time" ,
			"Total Time"  ,
			"Longest Time",
			"Type Id"     ,
			"Type Name"   ,
			"Type File"
		};
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////
		
		public static final void assembleTableHeader(final VarString vs)
		{
			XChars.assembleNewLinedTabbed(vs, TABLE_COLUMN_NAMES);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final XGettingTable<Long, TypeStatistic.Default>  typeStatistics       ;
		final XGettingTable<Long, TypeStatistic.Default>  viewTypeStatistics   ;
		
		final XGettingTable<Integer, ? extends ChannelStatistic> channelStatistics    ;
		final XGettingTable<Integer, ? extends ChannelStatistic> viewChannelStatistics;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final XGettingTable<Integer, ? extends ChannelStatistic> channelStatistics)
		{
			super();
			final EqHashTable<Long, TypeStatistic.Default> typeStatistics = EqHashTable.New();
			
			for(final KeyValue<Integer, ? extends ChannelStatistic> e1 : channelStatistics)
			{
				final ChannelStatistic cs = e1.value();
				this.update(cs.entityCount(), cs.bytesWritten(), cs.startTime(), cs.finishTime());
				
				for(final KeyValue<Long, ? extends TypeStatistic> e2 : cs.typeStatistics())
				{
					final TypeStatistic cts = e2.value();
					TypeStatistic.Default ts  = typeStatistics.get(e2.key());
					if(ts == null)
					{
						typeStatistics.add(
							e2.key(),
							ts = new TypeStatistic.Default(cts.typeId(), cts.typeName(), cts.file())
						);
					}
					ts.update(cts.entityCount(), cts.bytesWritten(), cts.startTime(), cts.finishTime());
				}
			}
			
			this.viewTypeStatistics    = (this.typeStatistics    = typeStatistics   ).view();
			this.viewChannelStatistics = (this.channelStatistics = channelStatistics).view();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final XGettingTable<Long, ? extends TypeStatistic> typeStatistics()
		{
			return this.viewTypeStatistics;
		}
		
		@Override
		public final XGettingTable<Integer, ? extends ChannelStatistic> channelStatistics()
		{
			return this.viewChannelStatistics;
		}
		
		@Override
		public XGettingEnum<AFile> files()
		{
			final EqHashEnum<AFile> files = EqHashEnum.New();
			this.viewTypeStatistics.values().forEach(s ->
				files.add(s.file())
			);
			return files.immure();
		}
		
		final void assembleTableRecord(final VarString vs)
		{
			vs.add("Total");
			assembleTableHeader(vs);
			
			super.assembleTableRecord(vs, "");
			
			vs.tab().tab().tab();
			vs.lf().add("Total per Type:");
			assembleTableHeader(vs);
			for(final TypeStatistic.Default e : this.typeStatistics.values())
			{
				e.assembleTableRecord(vs, "");
			}
			vs.lf().add("Per Channel:");
			for(final ChannelStatistic e : this.channelStatistics.values())
			{
				e.assembleTableRecord(vs);
			}
		}
		
		@Override
		public final String toString()
		{
			final VarString vs = VarString.New(1_000_000);
			this.assembleTableRecord(vs);
			return vs.toString();
		}
		
	}
	
	
	abstract class AbstractStatistic
	{
		long entityCount, bytesWritten, tLongest, tTotal, tEnd, tStart = Long.MAX_VALUE;
		
		final void update(final long entityCount, final long bytesWritten, final long tStart, final long tEnd)
		{
			this.entityCount  += entityCount ;
			this.bytesWritten += bytesWritten;
			this.tStart       = Math.min(this.tStart, tStart);
			this.tEnd         = Math.max(this.tEnd, tEnd)    ;
			this.tLongest     = Math.max(this.tLongest, tEnd - tStart);
			this.tTotal       += tEnd - tStart;
		}

		public final long entityCount()
		{
			return this.entityCount;
		}

		public final long bytesWritten()
		{
			return this.bytesWritten;
		}

		public final long startTime()
		{
			return this.tStart;
		}

		public final long finishTime()
		{
			return this.tEnd;
		}
		
		public final long longestDuration()
		{
			return this.tLongest;
		}
		
		public final long totalDuration()
		{
			return this.tTotal;
		}
		
		void assembleTableRecord(final VarString vs, final String channel)
		{
			vs
			.lf() .add(channel)
			.tab().add(this.entityCount)
			.tab().add(this.bytesWritten)
			.tab().add(this.tStart)
			.tab().add(this.tEnd)
			.tab().add(this.tTotal)
			.tab().add(this.tLongest)
			;
		}
	}
		
		
	public interface TypeStatistic
	{
		public long entityCount();
		
		public long bytesWritten();
		
		public long startTime();

		public long finishTime();

		public long typeId();
		
		public String typeName();
		
		public AFile file();
		
			
		
		final class Default extends AbstractStatistic implements TypeStatistic
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			final long   typeId  ;
			final String typeName;
			final AFile  file    ;
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			Default(final long typeId, final String typeName, final AFile file)
			{
				super();
				this.typeId   = typeId  ;
				this.typeName = typeName;
				this.file     = file    ;
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// override methods //
			/////////////////////
			
			@Override
			public final long typeId()
			{
				return this.typeId;
			}
			
			@Override
			public final String typeName()
			{
				return this.typeName;
			}
			
			@Override
			public final AFile file()
			{
				return this.file;
			}
			
			@Override
			final void assembleTableRecord(final VarString vs, final String channel)
			{
				super.assembleTableRecord(vs, channel);
				vs
				.tab().add(this.typeId)
				.tab().add(this.typeName)
				.tab().add(this.file.identifier())
				;
			}
			
		}
		
	}
	
	public interface ChannelStatistic
	{
		public long entityCount();
		
		public long bytesWritten();
		
		public long startTime();

		public long finishTime();
		
		public int channelIndex();
		
		public XGettingTable<Long, ? extends TypeStatistic> typeStatistics();
		
		public void assembleTableRecord(VarString vs);
		
		
		
		final class Default extends AbstractStatistic implements ChannelStatistic
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			final int                                          channelIndex  ;
			final XImmutableTable<Long, TypeStatistic.Default> typeStatistics;
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			Default(
				final int                                        channelIndex  ,
				final XGettingTable<Long, TypeStatistic.Default> typeStatistics
			)
			{
				super();
				this.channelIndex   = channelIndex                        ;
				this.typeStatistics = EqConstHashTable.New(typeStatistics);
				
				for(final TypeStatistic.Default tr : typeStatistics.values())
				{
					this.update(tr.entityCount, tr.bytesWritten, tr.tStart, tr.tEnd);
				}
			}

			
			
			///////////////////////////////////////////////////////////////////////////
			// override methods //
			/////////////////////
			
			@Override
			public final int channelIndex()
			{
				return this.channelIndex;
			}

			@Override
			public final XGettingTable<Long, ? extends TypeStatistic> typeStatistics()
			{
				return this.typeStatistics;
			}
			
			@Override
			public final void assembleTableRecord(final VarString vs)
			{
				final String channelIdentifier = Integer.toString(this.channelIndex);
				StorageEntityTypeExportStatistics.Default.assembleTableHeader(vs);
				
				super.assembleTableRecord(vs, channelIdentifier);
				
				vs.tab().tab().tab();
				StorageEntityTypeExportStatistics.Default.assembleTableHeader(vs);
				for(final TypeStatistic.Default e : this.typeStatistics.values())
				{
					e.assembleTableRecord(vs, channelIdentifier);
				}
			}
			
		}
		
	}
	
}
