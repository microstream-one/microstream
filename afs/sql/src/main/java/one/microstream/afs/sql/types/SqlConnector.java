package one.microstream.afs.sql.types;

/*-
 * #%L
 * microstream-afs-sql
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

import static one.microstream.X.checkArrayRange;
import static one.microstream.X.notNull;

import java.nio.ByteBuffer;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.LongFunction;

import one.microstream.chars.XChars;
import one.microstream.io.ByteBufferInputStream;
import one.microstream.io.LimitedInputStream;
import one.microstream.reference.Reference;
import one.microstream.typing.KeyValue;


public interface SqlConnector
{
	public long fileSize(SqlPath file);

	public boolean fileExists(SqlPath file);

	public boolean directoryExists(SqlPath directory);

	public void visitDirectories(SqlPath directory, SqlPathVisitor visitor);
	
	public void visitFiles(SqlPath directory, SqlPathVisitor visitor);

	public boolean createDirectory(SqlPath directory);

	public boolean deleteFile(SqlPath file);

	public ByteBuffer readData(SqlPath file, long offset, long length);

	public long readData(SqlPath file, ByteBuffer targetBuffer, long offset, long length);

	public long writeData(SqlPath file, Iterable<? extends ByteBuffer> sourceBuffers);

	public void moveFile(SqlPath sourceFile, SqlPath targetFile);

	public long copyFile(SqlPath sourceFile, SqlPath targetFile, long offset, long length);

	public void truncateFile(SqlPath file, long newLength);

	public boolean isEmpty(SqlPath directory);
	

	/**
	 * Creates a new {@link SqlConnector} which doesn't use caching.
	 * 
	 * @param provider the sql provider for the connector, not null
	 * @return the newly created connector
	 * @see #Caching(SqlProvider)
	 */
	public static SqlConnector New(
		final SqlProvider provider
	)
	{
		return new Default(
			notNull(provider),
			false
		);
	}
	
	/**
	 * Creates a new {@link SqlConnector} which uses caching.
	 * 
	 * @param provider the sql provider for the connector, not null
	 * @return the newly created connector
	 * @see #New(SqlProvider)
	 */
	public static SqlConnector Caching(
		final SqlProvider provider
	)
	{
		return new Default(
			notNull(provider),
			true
		);
	}


	public static class Default implements SqlConnector
	{
		// indexes in JDBC-API are 1-based not 0-based
		public final static int IDENTIFIER_COLUMN_INDEX = 1;
		public final static int START_COLUMN_INDEX      = 2;
		public final static int END_COLUMN_INDEX        = 3;
		public final static int DATA_COLUMN_INDEX       = 4;

		private final SqlProvider          provider                         ;
		private       Long                 maxBlobSize                      ;
		private final boolean              useCache                         ;
		private       Set<String>          directoryCache                   ;
		private final Map<String, Boolean> fileExistsCache = new HashMap<>();
		private final Map<String, Long>    fileSizeCache   = new HashMap<>();

		Default(
			final SqlProvider provider,
			final boolean     useCache
		)
		{
			super();
			this.provider = provider;
			this.useCache = useCache;
		}
		
		private boolean queryFileExists(
			final SqlPath    file      ,
			final Connection connection
		)
		throws SQLException
		{
			final String sql = this.provider.fileExistsQuery(file.parentPath().fullQualifiedName());
			try(final PreparedStatement statement = connection.prepareStatement(sql))
			{
				statement.setString(1, file.identifier());
				try(final ResultSet result = statement.executeQuery())
				{
					return result.next()
						? result.getLong(1) > 0L
						: false
					;
				}
			}
		}

		private boolean queryDirectoryExists(
			final SqlPath    directory ,
			final Connection connection
		)
		throws SQLException
		{
			return this.provider.queryDirectoryExists(connection, directory.fullQualifiedName());
		}

		private Set<String> queryDirectories(
			final Connection connection
		)
		throws SQLException
		{
			return this.provider.queryDirectories(connection, null);
		}

		private void internalVisitDirectories(
			final SqlPath        directory ,
			final SqlPathVisitor visitor   ,
			final Connection     connection
		)
		throws SQLException
		{
			final String       directoryPrefix = directory.fullQualifiedName() + SqlPath.getSeparatorString();
			final List<String> directories     = new ArrayList<>();

			if(this.useCache)
			{
				synchronized(this)
				{
					if(this.directoryCache == null)
					{
						this.directoryCache = this.queryDirectories(connection);
					}
					directories.addAll(this.directoryCache);
				}
			}
			else
			{
				directories.addAll(
					this.provider.queryDirectories(
						connection,
						directoryPrefix + "%"
					)
				);
			}
		
			directories.stream()
				.filter(name -> name.startsWith(directoryPrefix)
					&& name.length() > directoryPrefix.length()
				)
				.map( name ->
					name.replace(directoryPrefix, "")
				)
				.map( name ->
						XChars.splitSimple(name, SqlPath.getSeparatorString())[0]
				)
				.forEach(visitor::visitItem);
		}

		private void internalVisitFiles(
			final SqlPath        directory ,
			final SqlPathVisitor visitor   ,
			final Connection     connection
		)
		throws SQLException
		{
			final List<String> fileNames = new ArrayList<>();

			final String sql = this.provider.listFilesQuery(directory.fullQualifiedName());
			try(final Statement statement = connection.createStatement())
			{
				try(final ResultSet result = statement.executeQuery(sql))
				{
					while(result.next())
					{
						fileNames.add(result.getString(IDENTIFIER_COLUMN_INDEX));
					}
				}
			}

			fileNames.forEach(visitor::visitItem);
		}
		
		private boolean internalIsEmpty(
			final SqlPath    directory ,
			final Connection connection
		)
		throws SQLException
		{
			final String sql = this.provider.countFilesQuery(directory.fullQualifiedName());
			try(final Statement statement = connection.createStatement())
			{
				try(final ResultSet result = statement.executeQuery(sql))
				{
					if(result.next())
					{
						return result.getInt(1) <= 0;
					}
				}
			}
			
			return false;
		}

		private void queryCreateDirectory(
			final SqlPath    directory ,
			final Connection connection
		)
		throws SQLException
		{
			for(final String sql : this.provider.createDirectoryQueries(directory.fullQualifiedName()))
			{
				try(Statement statement = connection.createStatement())
				{
					statement.executeUpdate(sql);
				}
			}
		}
		
		private long internalFileSize(
			final SqlPath file,
			final Connection connection
		)
		throws SQLException
		{
			if(!this.useCache)
			{
				return this.queryFileSize(file, connection);
			}
			
			synchronized(this)
			{
				return this.fileSizeCache.computeIfAbsent(
					file.fullQualifiedName(),
					name -> {
						try
						{
							return this.queryFileSize(file, connection);
						}
						catch(final SQLException e)
						{
							throw new RuntimeException(e);
						}
					}
				);
			}
		}

		private Long queryFileSize(
			final SqlPath    file      ,
			final Connection connection
		)
		throws SQLException
		{
			final String sql = this.provider.fileSizeQuery(
				file.parentPath().fullQualifiedName()
			);
			try(final PreparedStatement statement = connection.prepareStatement(sql))
			{
				statement.setString(1, file.identifier());
				try(final ResultSet result = statement.executeQuery())
				{
					result.next();
					final long count = result.getLong(1);
					final long max   = result.getLong(2);
					return count > 0
						? max + 1L
						: 0L
					;
				}
			}
		}

		private long internalReadData(
			final SqlPath                  file          ,
			final LongFunction<ByteBuffer> bufferProvider,
			final long                     offset        ,
			final long                     length
		)
		{
			return this.provider.execute(connection ->
			{
				final KeyValue<ByteBuffer, Long> kv = this.internalReadData(
					file,
					bufferProvider,
					offset,
					length,
					connection
				);
				final ByteBuffer targetBuffer = kv.key();
				final long       amount       = kv.value();
				if(targetBuffer != null)
				{
					/*
					 * Buffer is filled in reverse, see below.
					 */
					targetBuffer.position(checkArrayRange(amount));
				}

				return amount;
			});
		}

		/*
		 *  Results are ordered in reverse, so the first row's end index equals the overall size.
		 *  This is done that way because some JDBC drivers don't support scrollable resultsets.
		 */
		private KeyValue<ByteBuffer, Long> internalReadData(
			final SqlPath                  file          ,
			final LongFunction<ByteBuffer> bufferProvider,
			final long                     offset        ,
			final long                     length        ,
			final Connection               connection
		)
		throws SQLException
		{
			if(offset == 0 && length <= 0)
			{
				return this.readDataFull(file, bufferProvider, connection);
			}
			if(offset == 0 && length > 0)
			{
				return this.readDataBeginning(file, bufferProvider, length, connection);
			}
			if(offset > 0 && length <= 0)
			{
				return this.readDataEnd(file, bufferProvider, offset, connection);
			}
			return this.readDataSegment(file, bufferProvider, offset, length, connection);
		}

		private KeyValue<ByteBuffer, Long> readDataFull(
			final SqlPath                  file          ,
			final LongFunction<ByteBuffer> bufferProvider,
			final Connection               connection
		)
		throws SQLException
		{
			final String sql = this.provider.readDataQuery(
				file.parentPath().fullQualifiedName()
			);
			try(final PreparedStatement statement = connection.prepareStatement(sql))
			{
				statement.setString(1, file.identifier());
				try(final ResultSet result = statement.executeQuery())
				{
					ByteBuffer targetBuffer = null;
					long       capacity     = 0L;
					while(result.next())
					{
						if(targetBuffer == null)
						{
							capacity     = result.getLong(END_COLUMN_INDEX) + 1L;
							targetBuffer = bufferProvider.apply(capacity);
							targetBuffer.position(checkArrayRange(capacity));
						}

						final long rowStart   = result.getLong(START_COLUMN_INDEX);
						final long rowEnd     = result.getLong(END_COLUMN_INDEX);
						final long blobLength = rowEnd - rowStart + 1L;

						this.readBlob(
							result,
							DATA_COLUMN_INDEX,
							0,
							blobLength,
							targetBuffer
						);
					}

					return KeyValue.New(targetBuffer, capacity);
				}
			}
		}

		private KeyValue<ByteBuffer, Long> readDataBeginning(
			final SqlPath                  file          ,
			final LongFunction<ByteBuffer> bufferProvider,
			final long                     length        ,
			final Connection               connection
		)
		throws SQLException
		{
			final String sql = this.provider.readDataQueryWithLength(
				file.parentPath().fullQualifiedName()
			);
			try(final PreparedStatement statement = connection.prepareStatement(sql))
			{
				statement.setString(1, file.identifier());
				statement.setLong  (2, length);
				try(final ResultSet result = statement.executeQuery())
				{
					ByteBuffer targetBuffer = null;
					while(result.next())
					{
						if(targetBuffer == null)
						{
							targetBuffer = bufferProvider.apply(length);
							targetBuffer.position(checkArrayRange(length));
						}

						final long rowStart   = result.getLong(START_COLUMN_INDEX);
						final long rowEnd     = result.getLong(END_COLUMN_INDEX);
						final long blobLength = Math.min(rowEnd + 1L, length) - rowStart;
						this.readBlob(
							result,
							DATA_COLUMN_INDEX,
							0,
							blobLength,
							targetBuffer
						);
					}

					return KeyValue.New(targetBuffer, length);
				}
			}
		}

		private KeyValue<ByteBuffer, Long> readDataEnd(
			final SqlPath                  file          ,
			final LongFunction<ByteBuffer> bufferProvider,
			final long                     offset        ,
			final Connection               connection
		)
		throws SQLException
		{
			final String sql = this.provider.readDataQueryWithOffset(
				file.parentPath().fullQualifiedName()
			);
			try(final PreparedStatement statement = connection.prepareStatement(sql))
			{
				statement.setString(1, file.identifier());
				statement.setLong  (2, offset           );
				try(final ResultSet result = statement.executeQuery())
				{
					ByteBuffer targetBuffer = null;
					long       capacity     = 0L;
					while(result.next())
					{
						if(targetBuffer == null)
						{
							capacity     = result.getLong(END_COLUMN_INDEX) - offset + 1L;
							targetBuffer = bufferProvider.apply(capacity);
							targetBuffer.position(checkArrayRange(capacity));
						}

						final long rowStart   = result.getLong(START_COLUMN_INDEX);
						final long rowEnd     = result.getLong(END_COLUMN_INDEX);
						final long blobOffset = rowStart < offset
							? offset - rowStart
							: 0L
						;
						final long blobLength = rowEnd - rowStart - blobOffset + 1L;

						this.readBlob(
							result,
							DATA_COLUMN_INDEX,
							blobOffset,
							blobLength,
							targetBuffer
						);
					}

					return KeyValue.New(targetBuffer, capacity);
				}
			}
		}

		private KeyValue<ByteBuffer, Long> readDataSegment(
			final SqlPath                  file          ,
			final LongFunction<ByteBuffer> bufferProvider,
			final long                     offset        ,
			final long                     length        ,
			final Connection               connection
		)
		throws SQLException
		{
			final String sql = this.provider.readDataQueryWithRange(
				file.parentPath().fullQualifiedName()
			);
			try(final PreparedStatement statement = connection.prepareStatement(sql))
			{
				statement.setString(1, file.identifier()  );
				statement.setLong  (2, offset             );
				statement.setLong  (3, offset + length - 1);
				try(final ResultSet result = statement.executeQuery())
				{
					ByteBuffer targetBuffer = null;
					while(result.next())
					{
						if(targetBuffer == null)
						{
							targetBuffer = bufferProvider.apply(length);
							targetBuffer.position(checkArrayRange(length));
						}

						final long rowStart   = result.getLong(START_COLUMN_INDEX);
						final long rowEnd     = result.getLong(END_COLUMN_INDEX);
						final long blobOffset = rowStart < offset
							? offset - rowStart
							: 0
						;
						final long maxEnd     = offset + length - 1L;
						final long blobLength = Math.min(maxEnd, rowEnd) - rowStart - blobOffset + 1L;

						this.readBlob(
							result,
							DATA_COLUMN_INDEX,
							blobOffset,
							blobLength,
							targetBuffer
						);
					}

					return KeyValue.New(targetBuffer, length);
				}
			}
		}

		private void readBlob(
			final ResultSet  result             ,
			final int        columnIndex        ,
			final long       offset             ,
			final long       length             ,
			final ByteBuffer reverseTargetBuffer
		)
		throws SQLException
		{
			try
			{
				final Blob blob = result.getBlob(columnIndex);
				try
				{
					final byte[] bytes    = blob.getBytes(
						offset + 1,
						checkArrayRange(length)
					);
					final int    position = reverseTargetBuffer.position() - bytes.length;
					reverseTargetBuffer.position(position);
					reverseTargetBuffer.put(bytes);
					reverseTargetBuffer.position(position);
				}
				finally
				{
					blob.free();
				}
			}
			catch(final SQLException e)
			{
				final byte[] bytes    = result.getBytes(columnIndex);
				final int    amount   = checkArrayRange(length);
				final int    position = reverseTargetBuffer.position() - amount;
				reverseTargetBuffer.position(position);
				reverseTargetBuffer.put(
					bytes,
					checkArrayRange(offset),
					amount
				);
				reverseTargetBuffer.position(position);
			}
		}

		private long maxBlobSize(
			final Connection connection
		)
		throws SQLException
		{
			synchronized(this)
			{
				if(this.maxBlobSize == null)
				{
					final DatabaseMetaData metaData   = connection.getMetaData();
					final long             maxLobSize = metaData.getMaxLogicalLobSize();
					this.maxBlobSize = maxLobSize > 0L
						? maxLobSize
						: 1048576L // 1MB
					;
				}
				return this.maxBlobSize;
			}
		}

		@Override
		public long fileSize(
			final SqlPath file
		)
		{
			if(!this.useCache)
			{
				return this.provider.execute(
					connection -> this.queryFileSize(file, connection)
				);
			}
			
			synchronized(this)
			{
				return this.fileSizeCache.computeIfAbsent(
					file.fullQualifiedName(),
					name -> this.provider.execute(
						connection -> this.queryFileSize(file, connection)
					)
				);
			}
		}

		@Override
		public boolean fileExists(
			final SqlPath file
		)
		{
			if(!this.directoryExists(file.parentPath()))
			{
				return false;
			}
			
			if(!this.useCache)
			{
				return this.provider.execute(connection ->
					this.queryFileExists(file, connection)
				);
			}
			
			synchronized(this)
			{
				return this.fileExistsCache.computeIfAbsent(
					file.fullQualifiedName(),
					name -> this.provider.execute(connection ->
						this.queryFileExists(file, connection)
					)
				);
			}
		}

		@Override
		public boolean directoryExists(final SqlPath directory)
		{
			if(!this.useCache)
			{
				return this.provider.execute(connection ->
					this.queryDirectoryExists(directory, connection)
				);
			}
			
			synchronized(this)
			{
				if(this.directoryCache == null)
				{
					this.directoryCache = this.provider.execute(this::queryDirectories);
				}
				
				return this.directoryCache.contains(directory.fullQualifiedName());
			}
		}

		@Override
		public void visitDirectories(
			final SqlPath        directory,
			final SqlPathVisitor visitor
		)
		{
			this.provider.execute(connection ->
			{
				this.internalVisitDirectories(directory, visitor, connection);

				return null;
			});
		}

		@Override
		public void visitFiles(
			final SqlPath        directory,
			final SqlPathVisitor visitor
		)
		{
			this.provider.execute(connection ->
			{
				this.internalVisitFiles(directory, visitor, connection);

				return null;
			});
		}
		
		@Override
		public boolean isEmpty(final SqlPath directory)
		{
			return this.provider.execute(connection ->
				this.internalIsEmpty(directory, connection)
			);
		}

		@Override
		public boolean createDirectory(final SqlPath directory)
		{
			final boolean success = this.provider.execute(connection ->
			{
				this.queryCreateDirectory(directory, connection);

				return true;
			});
			
			if(this.useCache && success)
			{
				synchronized(this)
				{
					this.directoryCache.add(directory.fullQualifiedName());
				}
			}
			
			return success;
		}

		@Override
		public boolean deleteFile(
			final SqlPath file
		)
		{
			final boolean success = this.provider.execute(connection ->
			{
				final String sql = this.provider.deleteFileQuery(
					file.parentPath().fullQualifiedName()
				);
				try(final PreparedStatement statement = connection.prepareStatement(sql))
				{
					statement.setString(1, file.identifier());
					final int affectedRows = statement.executeUpdate();
					return affectedRows > 0;
				}
			});
			
			if(this.useCache)
			{
				synchronized(this)
				{
					this.fileExistsCache.remove(file.fullQualifiedName());
					this.fileSizeCache.remove(file.fullQualifiedName());
				}
			}
			
			return success;
		}

		@Override
		public ByteBuffer readData(
			final SqlPath file  ,
			final long    offset,
			final long    length
		)
		{
			if(length == 0L)
			{
				/*
				 * (10.12.2020 FH)XXX priv#383, nothing to read, abort.
				 */
				return ByteBuffer.allocateDirect(0);
			}
			
			final Reference   <ByteBuffer> bufferRef      = Reference.New(null);
			final LongFunction<ByteBuffer> bufferProvider = capacity ->
			{
				final ByteBuffer buffer = ByteBuffer.allocateDirect(checkArrayRange(capacity));
				bufferRef.set(buffer);
				return buffer;
			};

			this.internalReadData(file, bufferProvider, offset, length);

			final ByteBuffer buffer = bufferRef.get();
			if(buffer != null)
			{
				buffer.flip();
				return buffer;
			}

			return ByteBuffer.allocateDirect(0);
		}

		@Override
		public long readData(
			final SqlPath    file        ,
			final ByteBuffer targetBuffer,
			final long       offset      ,
			final long       length
		)
		{
			if(length == 0)
			{
				/*
				 * (10.12.2020 FH)XXX priv#383, nothing to read, abort.
				 */
				return 0L;
			}
			
			final LongFunction<ByteBuffer> bufferProvider = capacity ->
			{
				if(targetBuffer.remaining() < capacity)
				{
					throw new IllegalArgumentException(
						"Provided target buffer has not enough space remaining to load the content: "
						+ targetBuffer.remaining() + " < " + capacity
					);
				}
				return targetBuffer;
			};
			return this.internalReadData(file, bufferProvider, offset, length);
		}

		@Override
		public long writeData(
			final SqlPath                        file         ,
			final Iterable<? extends ByteBuffer> sourceBuffers
		)
		{
			final long written = this.provider.execute(connection ->
			{
				final String sql = this.provider.writeDataQuery(
					file.parentPath().fullQualifiedName()
				);

				long buffersLength = 0L;
				for(final ByteBuffer buffer : sourceBuffers)
				{
					buffersLength += buffer.remaining();
				}
				final long maxBatchSize = Math.min(
					this.maxBlobSize(connection),
					buffersLength
				);

				final ByteBufferInputStream inputStream = ByteBufferInputStream.New(sourceBuffers);
				final long                  fileSize    = this.internalFileSize(file, connection);
				      long                  offset      = Math.max(0L, fileSize);
				      long                  available   = buffersLength;
				while(available > 0)
				{
					final long        currentBatchSize = Math.min(available, maxBatchSize);
					try(final PreparedStatement statement = connection.prepareStatement(sql))
					{
						statement.setString(IDENTIFIER_COLUMN_INDEX, file.identifier()            );
						statement.setLong  (START_COLUMN_INDEX     , offset                       );
						statement.setLong  (END_COLUMN_INDEX       , offset + currentBatchSize - 1);
						this.provider.setBlob(
							statement,
							DATA_COLUMN_INDEX,
							LimitedInputStream.New(inputStream, currentBatchSize),
							currentBatchSize
						);
						statement.executeUpdate();
					}

					offset    += currentBatchSize;
					available -= currentBatchSize;
				}

				return buffersLength;
			});
			
			if(this.useCache)
			{
				synchronized(this)
				{
					this.fileExistsCache.put(file.fullQualifiedName(), Boolean.TRUE);
					this.fileSizeCache.merge(file.fullQualifiedName(), written, Math::addExact);
				}
			}
			
			return written;
		}

		@Override
		public void moveFile(
			final SqlPath sourceFile,
			final SqlPath targetFile
		)
		{
			this.provider.execute(connection ->
			{
				if(sourceFile.parentPath().fullQualifiedName().equals(
					targetFile.parentPath().fullQualifiedName()
				))
				{
					// same parent

					final String sql = this.provider.moveFileQuerySameParent(
						sourceFile.parentPath().fullQualifiedName()
					);
					try(PreparedStatement statement = connection.prepareStatement(sql))
					{
						statement.setString(1, targetFile.identifier());
						statement.setString(2, sourceFile.identifier());
						statement.executeUpdate();
					}
				}
				else
				{
					// different parent

					String sql = this.provider.copyFileQuery(
						sourceFile.parentPath().fullQualifiedName(),
						targetFile.parentPath().fullQualifiedName()
					);
					try(final PreparedStatement statement = connection.prepareStatement(sql))
					{
						statement.setString(1, targetFile.identifier());
						statement.setString(2, sourceFile.identifier());
						statement.executeUpdate();
					}

					sql = this.provider.deleteFileQuery(
						sourceFile.parentPath().fullQualifiedName()
					);
					try(final PreparedStatement statement = connection.prepareStatement(sql))
					{
						statement.setString(1, sourceFile.identifier());
						statement.executeUpdate();
					}
				}
				
				return null;
			});
			
			if(this.useCache)
			{
				synchronized(this)
				{
					this.fileExistsCache.put(sourceFile.fullQualifiedName(), Boolean.FALSE);
					this.fileExistsCache.put(targetFile.fullQualifiedName(), Boolean.TRUE);
					
					final Long fileSize = this.fileSizeCache.remove(sourceFile.fullQualifiedName());
					if(fileSize != null)
					{
						this.fileSizeCache.put(targetFile.fullQualifiedName(), fileSize);
					}
				}
			}
		}

		@Override
		public long copyFile(
			final SqlPath sourceFile,
			final SqlPath targetFile,
			final long    offset    ,
			final long    length
		)
		{
			final ByteBuffer buffer = this.readData(sourceFile, offset, length);
			return this.writeData(targetFile, Arrays.asList(buffer));
		}

		@Override
		public void truncateFile(
			final SqlPath file     ,
			final long    newLength
		)
		{
			if(newLength == 0L)
			{
				this.deleteFile(file);
				return;
			}
			
			this.provider.execute(connection ->
			{
				final long currentLength = this.internalFileSize(file, connection);
				if(newLength >= currentLength)
				{
					throw new IllegalArgumentException(
						"New size must be smaller than current size: " +
						newLength + " >= " + currentLength
					);
				}

				final String tableName = file.parentPath().fullQualifiedName();
			          long   segmentStart;
				      long   segmentEnd  ;
				try(final PreparedStatement statement = connection.prepareStatement(
					this.provider.readMetadataQuerySingleSegment(tableName)
				))
				{
					statement.setString(1, file.identifier());
					statement.setLong(2, newLength);
					statement.setLong(3, newLength);
					try(final ResultSet result = statement.executeQuery())
					{
						result.next();
						segmentStart = result.getLong(1);
						segmentEnd = result.getLong(2);
					}
				}

				if(segmentStart == newLength)
				{
					try(final PreparedStatement statement = connection.prepareStatement(
						this.provider.deleteFileQueryFromStart(tableName)
					))
					{
						statement.setString(1, file.identifier());
						statement.setLong(2, newLength);
						statement.executeUpdate();
					}
				}
				else if(segmentEnd == newLength - 1)
				{
					try(final PreparedStatement statement = connection.prepareStatement(
						this.provider.deleteFileQueryFromEnd(tableName)
					))
					{
						statement.setString(1, file.identifier());
						statement.setLong(2, newLength);
						statement.executeUpdate();
					}
				}
				else
				{
					final long       newSegmentLength = newLength - segmentStart;
					final ByteBuffer buffer           = ByteBuffer.allocateDirect(
						checkArrayRange(newSegmentLength)
					);
					this.internalReadData(
						file,
						size -> buffer,
						0L,
						newSegmentLength,
						connection
					);

					try(final PreparedStatement statement = connection.prepareStatement(
						this.provider.deleteFileQueryFromStart(tableName)
					))
					{
						statement.setString(1, file.identifier());
						statement.setLong(2, segmentStart);
						statement.executeUpdate();
					}

					try(final PreparedStatement statement = connection.prepareStatement(
						this.provider.writeDataQuery(tableName)
					))
					{
						statement.setString(IDENTIFIER_COLUMN_INDEX, file.identifier()                  );
						statement.setLong  (START_COLUMN_INDEX     , segmentStart                       );
						statement.setLong  (END_COLUMN_INDEX       , segmentStart + newSegmentLength - 1);
						this.provider.setBlob(
							statement,
							DATA_COLUMN_INDEX,
							ByteBufferInputStream.New(buffer),
							newSegmentLength
						);
						statement.executeUpdate();
					}
				}

				return null;
			});
			
			if(this.useCache)
			{
				synchronized(this)
				{
					this.fileSizeCache.put(file.fullQualifiedName(), newLength);
				}
			}
		}

	}
	
}
