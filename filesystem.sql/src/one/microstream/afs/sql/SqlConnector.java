package one.microstream.afs.sql;

import static one.microstream.X.notNull;

import java.nio.ByteBuffer;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.function.LongFunction;

import one.microstream.X;
import one.microstream.io.ByteBufferInputStream;
import one.microstream.io.LimitedInputStream;
import one.microstream.reference.Reference;


public interface SqlConnector
{
	public long fileSize(SqlPath file);

	public boolean fileExists(SqlPath directory);

	public boolean ensureDirectory(SqlPath directory);

	public boolean deleteFile(SqlPath file);

	public ByteBuffer readData(SqlPath file, long offset, long length);

	public long readData(SqlPath file, ByteBuffer targetBuffer, long offset, long length);

	public long writeData(SqlPath file, Iterable<? extends ByteBuffer> sourceBuffers);

	public void moveFile(SqlPath sourceFile, SqlPath targetFile);

	public long copyFile(SqlPath sourceFile, SqlPath targetFile);

	public long copyFile(SqlPath sourceFile, SqlPath targetFile, long offset, long length);


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
					return result.next()
						? result.getLong(1) + 1
						: -1L;
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
				if(offset == 0 && length <= 0)
				{
					// read all

					final String sql = this.provider.readDataQuery(
						file.parentPath().fullQualifiedName()
					);
					try(final PreparedStatement statement = connection.prepareStatement(
						sql,
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY
					))
					{
						statement.setString(1, file.identifier());
						try(final ResultSet result = statement.executeQuery())
						{
							long capacity = 0L;
							if(result.last())
							{
								capacity = result.getLong(END_COLUMN_INDEX) + 1;
							}

							if(capacity > 0L && result.first())
							{
								final ByteBuffer targetBuffer = bufferProvider.apply(capacity);
								do
								{
									final Blob   blob  = result.getBlob(DATA_COLUMN_INDEX);
									final byte[] bytes = blob.getBytes(1, X.checkArrayRange(blob.length()));
									blob.free();
									targetBuffer.put(bytes);
								}
								while(result.next());
							}

							return capacity;
						}
					}
				}

				if(offset == 0 && length > 0)
				{
					// read from beginning up to length

					final String sql = this.provider.readDataQueryWithLength(
						file.parentPath().fullQualifiedName()
					);
					try(final PreparedStatement statement = connection.prepareStatement(
						sql                        ,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_READ_ONLY
					))
					{
						statement.setString(1, file.identifier());
						statement.setLong  (2, length);
						try(final ResultSet result = statement.executeQuery())
						{
							ByteBuffer targetBuffer = null;
							long       remaining    = length;
							while(remaining > 0 && result.next())
							{
								final Blob   blob       = result.getBlob(DATA_COLUMN_INDEX);
								final long   blobLength = blob.length();
								final long   amount     = Math.min(blobLength, remaining);
								final byte[] bytes      = blob.getBytes(1, X.checkArrayRange(amount));
								blob.free();

								if(targetBuffer == null)
								{
									targetBuffer = bufferProvider.apply(length);
								}
								targetBuffer.put(bytes);
								remaining -= amount;
							}

							return length;
						}
					}
				}

				if(offset > 0 && length <= 0)
				{
					// read all from offset on

					final String sql = this.provider.readDataQueryWithOffset(
						file.parentPath().fullQualifiedName()
					);
					try(final PreparedStatement statement = connection.prepareStatement(
						sql,
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY
					))
					{
						statement.setString(1, file.identifier());
						statement.setLong  (2, offset           );
						try(final ResultSet result = statement.executeQuery())
						{
							long capacity = 0L;
							if(result.last())
							{
								capacity = Math.max(0L, result.getLong(END_COLUMN_INDEX) - offset + 1);
							}

							if(capacity > 0L && result.first())
							{
								final ByteBuffer targetBuffer = bufferProvider.apply(capacity);
								      Blob       blob         = result.getBlob(DATA_COLUMN_INDEX);
								final long       start        = offset - result.getLong(START_COLUMN_INDEX);
								final long       amount       = blob.length() - start;
								      byte[]     bytes        = blob.getBytes(start + 1, X.checkArrayRange(amount));
								blob.free();
								targetBuffer.put(bytes);

								while(result.next())
								{
									blob  = result.getBlob(DATA_COLUMN_INDEX);
									bytes = blob.getBytes(1, X.checkArrayRange(blob.length()));
									blob.free();
									targetBuffer.put(bytes);
								}
							}

							return capacity;
						}
					}
				}

				// read segment

				final String sql = this.provider.readDataQueryWithRange(
					file.parentPath().fullQualifiedName()
				);
				try(final PreparedStatement statement = connection.prepareStatement(
					sql,
					ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY
				))
				{
					statement.setString(1, file.identifier()  );
					statement.setLong  (2, offset             );
					statement.setLong  (3, offset + length - 1);
					try(final ResultSet result = statement.executeQuery())
					{
						ByteBuffer targetBuffer = null;
						long       remaining    = length;
						while(remaining > 0 && result.next())
						{
							final Blob   blob     = result.getBlob(DATA_COLUMN_INDEX);
							final long   rowStart = result.getLong(START_COLUMN_INDEX);
							final long   rowEnd   = result.getLong(END_COLUMN_INDEX);
							final long   start    = rowStart < offset
								? offset - rowStart
								: 0
							;
							final long   amount   = Math.min(remaining, rowEnd - rowStart - start + 1);
							final byte[] bytes    = blob.getBytes(start + 1, X.checkArrayRange(amount));
							blob.free();

							if(targetBuffer == null)
							{
								targetBuffer = bufferProvider.apply(length);
							}
							targetBuffer.put(bytes);
							remaining -= amount;
						}

						return length;
					}
				}
			});
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
		public boolean ensureDirectory(final SqlPath directory)
		{
			return this.provider.execute(connection ->
			{
				if(!this.internalDirectoryExists(directory, connection))
				{
					this.internalCreateDirectory(directory, connection);
				}

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
				final ByteBuffer buffer = ByteBuffer.allocateDirect(X.checkArrayRange(capacity));
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
				final long maxLobSize = connection.getMetaData().getMaxLogicalLobSize();
				final long batchSize  = maxLobSize > 0L
					? Math.min(maxLobSize, buffersLength)
					: buffersLength
				;

				final ByteBufferInputStream inputStream = ByteBufferInputStream.New(sourceBuffers);
				      long                  offset      = this.internalFileSize(file, connection);
				      long                  available   = buffersLength;
				while(available > 0)
				{
					final long currentBatchSize = Math.min(available, batchSize);

					final Blob blob             = this.provider.createBlob(
						connection,
						LimitedInputStream.New(inputStream, currentBatchSize),
						currentBatchSize
					);

					try(final PreparedStatement statement = connection.prepareStatement(sql))
					{
						statement.setString(IDENTIFIER_COLUMN_INDEX, file.identifier()            );
						statement.setLong  (START_COLUMN_INDEX     , offset                       );
						statement.setLong  (END_COLUMN_INDEX       , offset + currentBatchSize - 1);
						statement.setBlob  (DATA_COLUMN_INDEX      , blob                         );
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

	}

}
