package one.microstream.afs.sql;

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
import java.util.List;
import java.util.function.LongFunction;

import one.microstream.io.ByteBufferInputStream;
import one.microstream.io.LimitedInputStream;
import one.microstream.reference.Reference;
import one.microstream.typing.KeyValue;


public interface SqlConnector
{
	public long fileSize(SqlPath file);

	public boolean fileExists(SqlPath file);

	public boolean directoryExists(SqlPath directory);

	public void visitChildren(SqlPath directory, SqlPathVisitor visitor);

	public boolean createDirectory(SqlPath directory);

	public boolean deleteFile(SqlPath file);

	public ByteBuffer readData(SqlPath file, long offset, long length);

	public long readData(SqlPath file, ByteBuffer targetBuffer, long offset, long length);

	public long writeData(SqlPath file, Iterable<? extends ByteBuffer> sourceBuffers);

	public void moveFile(SqlPath sourceFile, SqlPath targetFile);

	public long copyFile(SqlPath sourceFile, SqlPath targetFile);

	public long copyFile(SqlPath sourceFile, SqlPath targetFile, long offset, long length);

	public void truncateFile(SqlPath file, long newLength);


	public static SqlConnector New(
		final SqlProvider provider
	)
	{
		return new Default(
			notNull(provider)
		);
	}


	public static class Default implements SqlConnector
	{
		// indexes in JDBC-API are 1-based not 0-based ¯\_(ツ)_/¯
		public final static int IDENTIFIER_COLUMN_INDEX = 1;
		public final static int START_COLUMN_INDEX      = 2;
		public final static int END_COLUMN_INDEX        = 3;
		public final static int DATA_COLUMN_INDEX       = 4;

		private final SqlProvider provider;

		Default(
			final SqlProvider provider
		)
		{
			super();
			this.provider = provider;
		}

		private boolean internalDirectoryExists(
			final SqlPath    directory ,
			final Connection connection
		)
		throws SQLException
		{
			try(final ResultSet result = connection.getMetaData().getTables(
				this.provider.catalog(),
				this.provider.schema(),
				directory.fullQualifiedName(),
				new String[] {"TABLE"}
			))
			{
				return result.next();
			}
		}

		private void internalVisitChildren(
			final SqlPath        directory ,
			final SqlPathVisitor visitor   ,
			final Connection     connection
		)
		throws SQLException
		{
			final List<String> directoryNames = new ArrayList<>();
			final List<String> fileNames      = new ArrayList<>();

			final String directoryPrefix = directory.fullQualifiedName() + SqlPath.DIRECTORY_TABLE_NAME_SEPARATOR;
			try(final ResultSet result = connection.getMetaData().getTables(
				this.provider.catalog(),
				this.provider.schema(),
				directoryPrefix + "%",
				new String[] {"TABLE"}
			))
			{
				while(result.next())
				{
					final String name = result.getString("TABLE_NAME");
					if(name.startsWith(directoryPrefix)
					&& name.length() > directoryPrefix.length()
					&& name.indexOf(SqlPath.DIRECTORY_TABLE_NAME_SEPARATOR, directoryPrefix.length()) == -1
					)
					{
						directoryNames.add(name.substring(directoryPrefix.length()));
					}
				}
			}

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

			directoryNames.forEach(name -> visitor.visitDirectory(directory, name));
			fileNames     .forEach(name -> visitor.visitFile     (directory, name));
		}

		private void internalCreateDirectory(
			final SqlPath    directory ,
			final Connection connection
		)
		throws SQLException
		{
			for(final String sql : this.provider.createDirectoryQueries(
				directory.fullQualifiedName()
			))
			{
				try(Statement statement = connection.createStatement())
				{
					statement.executeUpdate(sql);
				}
			}
		}

		private Long internalFileSize(
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
			final DatabaseMetaData metaData   = connection.getMetaData();
			final long             maxLobSize = metaData.getMaxLogicalLobSize();
			return maxLobSize > 0L
				? maxLobSize
				: 1048576L // 1MB
			;
		}

		@Override
		public long fileSize(
			final SqlPath file
		)
		{
			return this.provider.execute(connection ->
			{
				return this.internalFileSize(file, connection);
			});
		}

		@Override
		public boolean fileExists(
			final SqlPath file
		)
		{
			return this.provider.execute(connection ->
			{
				if(!this.internalDirectoryExists(file.parentPath(), connection))
				{
					return false;
				}

				final String sql = this.provider.fileExistsQuery(
					file.parentPath().fullQualifiedName()
				);
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
			});
		}

		@Override
		public boolean directoryExists(final SqlPath directory)
		{
			return this.provider.execute(connection ->
			{
				return this.internalDirectoryExists(directory, connection);
			});
		}

		@Override
		public void visitChildren(
			final SqlPath        directory,
			final SqlPathVisitor visitor
		)
		{
			this.provider.execute(connection ->
			{
				this.internalVisitChildren(directory, visitor, connection);

				return null;
			});
		}

		@Override
		public boolean createDirectory(final SqlPath directory)
		{
			return this.provider.execute(connection ->
			{
				this.internalCreateDirectory(directory, connection);

				return true;
			});
		}

		@Override
		public boolean deleteFile(
			final SqlPath file
		)
		{
			return this.provider.execute(connection ->
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
		}

		@Override
		public ByteBuffer readData(
			final SqlPath file  ,
			final long    offset,
			final long    length
		)
		{
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
			final LongFunction<ByteBuffer> bufferProvider = capacity ->
			{
				if(targetBuffer.remaining() < capacity)
				{
					// (03.06.2020 FH)EXCP: proper exception
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
			return this.provider.execute(connection ->
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
				      long                  offset      = Math.max(0L, this.internalFileSize(file, connection));
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
		}

		@Override
		public long copyFile(
			final SqlPath sourceFile,
			final SqlPath targetFile
		)
		{
			return this.provider.execute(connection ->
			{
				final String sql = this.provider.copyFileQuery(
					sourceFile.parentPath().fullQualifiedName(),
					targetFile.parentPath().fullQualifiedName()
				);
				try(final PreparedStatement statement = connection.prepareStatement(sql))
				{
					statement.setString(1, targetFile.identifier());
					statement.setString(2, sourceFile.identifier());
					statement.executeUpdate();
				}

				return this.internalFileSize(sourceFile, connection);
			});
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
			final SqlPath file   ,
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
		}

	}

}
