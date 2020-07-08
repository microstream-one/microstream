package one.microstream.afs.jpa.hibernate;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.hibernate.NullPrecedence;
import org.hibernate.Session;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.dialect.Dialect;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PrimaryKey;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.sql.Delete;
import org.hibernate.sql.Insert;
import org.hibernate.sql.InsertSelect;
import org.hibernate.sql.Update;
import org.hibernate.tool.schema.internal.StandardTableExporter;
import org.hibernate.type.StandardBasicTypes;

import one.microstream.afs.sql.SqlOperation;
import one.microstream.afs.sql.SqlProvider;
import one.microstream.chars.XChars;

public interface HibernateProvider extends SqlProvider
{
	public EntityManager entityManager();


	public static HibernateProvider New(
		final String persistenceUnit
	)
	{
		return new Default(
			HibernateIntegrator.getHibernateContext(persistenceUnit)
		);
	}


	public static class Default implements HibernateProvider
	{
		private final HibernateContext           context         ;
		private final ThreadLocal<EntityManager> entityManagerRef;

		Default(
			final HibernateContext context
		)
		{
			super();
			this.context          = context;
			this.entityManagerRef = new ThreadLocal<EntityManager>()
			{
				@Override
				protected EntityManager initialValue()
				{
					return Persistence
						.createEntityManagerFactory(context.persistenceUnit())
						.createEntityManager()
					;
				}
			};
		}

		private Table table(
			final String tableName
		)
		{
			return new Table(
				this.context,
				this.catalog(),
				this.schema() ,
				tableName
			);
		}

		private Select readDataQuerySelect(
			final String tableName
		)
		{
			final Table  table  = this.table(tableName);
			final Select select = new Select(table);
			select.setFromClause(table.sqlName());
			select.setSelectClause("*");
			select.setWhereClause(table.identifierColumnSqlName() + "=?");
			select.setOrderByClause(this.context.dialect().renderOrderByElement(
				table.startColumnSqlName(),
				null,
				"desc",
				NullPrecedence.NONE
			));
			return select;
		}

		@Override
		public EntityManager entityManager()
		{
			return this.entityManagerRef.get();
		}

		@Override
		public String catalog()
		{
			return this.context.defaultCatalog();
		}

		@Override
		public String schema()
		{
			return this.context.defaultSchema();
		}

		@Override
		public <T> T execute(
			final SqlOperation<T> operation
		)
		{
			final EntityManager entityManager = this.entityManager();
			synchronized(entityManager)
			{
				final EntityTransaction transaction = entityManager.getTransaction();
				try
				{
					transaction.begin();

					return entityManager
						.unwrap(Session.class)
						.doReturningWork(operation::execute)
					;
				}
				finally
				{
					transaction.commit();
				}
			}
		}
		
		@Override
		public long maxBlobSize(final Connection connection)
		{
			try
			{
				final DatabaseMetaData metaData   = connection.getMetaData();
				final long             maxLobSize = metaData.getMaxLogicalLobSize();
				return maxLobSize > 0L
					? maxLobSize
					: 1048576L // 1MB
				;
			}
			catch(final SQLException e)
			{
				// TODO: proper exception
				throw new RuntimeException(e);
			}
		}

		@Override
		public String fileSizeQuery(
			final String tableName
		)
		{
			final Table  table  = this.table(tableName);
			final Select select = new Select(table);
			select.setFromClause(table.sqlName());
			select.setSelectClause("count(*), max(" + table.endColumnSqlName() + ")");
			select.setWhereClause(table.identifierColumnSqlName() + "=?");
			return select.toStatementString();
		}

		@Override
		public String readMetadataQuery(
			final String tableName
		)
		{
			final Table  table  = this.table(tableName);
			final Select select = new Select(table);
			select.setFromClause(table.sqlName());
			select.setSelectClause(table.startColumnSqlName() + ", " + table.endColumnSqlName());
			select.setWhereClause(table.identifierColumnSqlName() + "=?");
			return select.toStatementString();
		}

		@Override
		public String readMetadataQuerySingleSegment(
			final String tableName
		)
		{
			final Table  table  = this.table(tableName);
			final Select select = new Select(table);
			select.setFromClause(table.sqlName());
			select.setSelectClause(table.startColumnSqlName() + ", " + table.endColumnSqlName());
			select.setWhereClause(
				table.identifierColumnSqlName() + "=? and " +
				table.startColumnSqlName() + "<=? and " +
				table.endColumnSqlName() + ">=?"
			);
			return select.toStatementString();
		}

		@Override
		public String readDataQuery(
			final String tableName
		)
		{
			return this.readDataQuerySelect(tableName).toStatementString();
		}

		@Override
		public String readDataQueryWithLength(
			final String tableName
		)
		{
			final Select select = this.readDataQuerySelect(tableName);
			return select
				.addWhereClause("and", select.table.startColumnSqlName() + "<?")
				.toStatementString()
			;
		}

		@Override
		public String readDataQueryWithOffset(
			final String tableName
		)
		{
			final Select select = this.readDataQuerySelect(tableName);
			return select
				.addWhereClause("and", select.table.endColumnSqlName() + ">=?")
				.toStatementString()
			;
		}

		@Override
		public String readDataQueryWithRange(
			final String tableName
		)
		{
			final Select select = this.readDataQuerySelect(tableName);
			return select
				.addWhereClause("and", select.table.endColumnSqlName() + ">=?")
				.addWhereClause("and", select.table.startColumnSqlName() + "<=?")
				.toStatementString()
			;
		}

		@Override
		public String fileExistsQuery(
			final String tableName
		)
		{
			final Table  table  = this.table(tableName);
			final Select select = new Select(table);
			select.setFromClause(table.sqlName());
			select.setSelectClause("count(*)");
			select.setWhereClause(table.identifierColumnSqlName() + "=?");
			return select.toStatementString();
		}

		@Override
		public Iterable<String> createDirectoryQueries(
			final String tableName
		)
		{
			return Arrays.asList(
				new StandardTableExporter(this.context.dialect()).getSqlCreateStrings(
					this.table(tableName),
					this.context.metadata()
				)
			);
		}

		@Override
		public String deleteFileQuery(
			final String tableName
		)
		{
			final Table  table  = this.table(tableName);
			final Delete delete = new Delete();
			delete.setTableName(table.sqlName());
			delete.setWhere(table.identifierColumnSqlName() + "=?");
			return delete.toStatementString();
		}

		@Override
		public String deleteFileQueryFromStart(
			final String tableName
		)
		{
			final Table  table  = this.table(tableName);
			final Delete delete = new Delete();
			delete.setTableName(table.sqlName());
			delete.setWhere(table.identifierColumnSqlName() + "=? and " + table.startColumnSqlName() + ">=?");
			return delete.toStatementString();
		}

		@Override
		public String deleteFileQueryFromEnd(
			final String tableName
		)
		{
			final Table  table  = this.table(tableName);
			final Delete delete = new Delete();
			delete.setTableName(table.sqlName());
			delete.setWhere(table.identifierColumnSqlName() + "=? and " + table.endColumnSqlName() + ">=?");
			return delete.toStatementString();
		}

		@Override
		public String writeDataQuery(
			final String tableName
		)
		{
			final Dialect dialect = this.context.dialect();
			final Table   table   = this.table(tableName);
			final Insert  insert  = new Insert(dialect);
			insert.setTableName(table.sqlName());
			@SuppressWarnings("unchecked")
			final Iterator<Column> columnIterator = table.getColumnIterator();
			while(columnIterator.hasNext())
			{
				final Column column = columnIterator.next();
				insert.addColumn(column.getQuotedName(dialect));
			}
			return insert.toStatementString();
		}

		@Override
		public String moveFileQuerySameParent(
			final String tableName
		)
		{
			final Table  table                = this.table(tableName);
			final String identifierColumnName = table.identifierColumnSqlName();
			final Update update               = new Update(this.context.dialect());
			update.setTableName(table.sqlName());
			update.addColumn(identifierColumnName);
			update.addWhereColumn(identifierColumnName);
			return update.toStatementString();
		}

		@Override
		public String copyFileQuery(
			final String sourceTableName,
			final String targetTableName
		)
		{
			final Table  sourceTable          = this.table(sourceTableName);
			final Table  targetTable          = this.table(targetTableName);
			final String identifierColumnName = sourceTable.identifierColumnSqlName();
			final String startColumnName      = sourceTable.startColumnSqlName();
			final String endColumnName        = sourceTable.endColumnSqlName();
			final String dataColumnName       = sourceTable.dataColumnSqlName();
			final Select select               = new Select(sourceTable);
			select.setFromClause(sourceTable.sqlName());
			select.setSelectClause("?, " + startColumnName + ", " + endColumnName + ", " + dataColumnName);
			select.setWhereClause(identifierColumnName + "=?");
			final InsertSelect insert = new InsertSelect(this.context.dialect());
			insert.setTableName(targetTable.sqlName());
			insert.addColumn(identifierColumnName);
			insert.addColumn(startColumnName);
			insert.addColumn(endColumnName);
			insert.addColumn(dataColumnName);
			insert.setSelect(select);
			return insert.toStatementString();
		}


		private static final class Table extends org.hibernate.mapping.Table
		{
			final HibernateContext hibernateContext;
			final Column           identifierColumn;
			final Column           startColumn     ;
			final Column           endColumn       ;
			final Column           dataColumn      ;

			Table(
				final HibernateContext hibernateContext,
				final String           catalog         ,
				final String           schema          ,
				final String           tableName
			)
			{
				super(
					Identifier.toIdentifier(catalog  , true),
					Identifier.toIdentifier(schema   , true),
					Identifier.toIdentifier(tableName, true),
					false
				);

				this.hibernateContext = hibernateContext;

				final char quote = Dialect.QUOTE.charAt(0);

				this.identifierColumn = new Column(quote + IDENTIFIER_COLUMN_NAME + quote);
				this.identifierColumn.setSqlTypeCode(IDENTIFIER_COLUMN_TYPE);
				this.identifierColumn.setLength(IDENTIFIER_COLUMN_LENGTH);
				this.identifierColumn.setNullable(false);
				final SimpleValue identifierValue = this.createValue(hibernateContext);
				identifierValue.addColumn(this.identifierColumn);
				identifierValue.setTypeName(StandardBasicTypes.STRING.getName());
				this.identifierColumn.setValue(identifierValue);

				this.startColumn = new Column(quote + START_COLUMN_NAME + quote);
				this.startColumn.setSqlTypeCode(START_COLUMN_TYPE);
				this.startColumn.setNullable(false);
				final SimpleValue startValue = this.createValue(hibernateContext);
				startValue.addColumn(this.startColumn);
				startValue.setTypeName(StandardBasicTypes.LONG.getName());
				this.startColumn.setValue(startValue);

				this.endColumn = new Column(quote + END_COLUMN_NAME + quote);
				this.endColumn.setSqlTypeCode(END_COLUMN_TYPE);
				this.endColumn.setNullable(false);
				final SimpleValue endValue = this.createValue(hibernateContext);
				endValue.addColumn(this.endColumn);
				endValue.setTypeName(StandardBasicTypes.LONG.getName());
				this.endColumn.setValue(endValue);

				this.dataColumn = new Column(quote + DATA_COLUMN_NAME + quote);
				this.dataColumn.setSqlTypeCode(DATA_COLUMN_TYPE);
				this.dataColumn.setNullable(false);
				final SimpleValue dataValue = this.createValue(hibernateContext);
				dataValue.addColumn(this.dataColumn);
				dataValue.setTypeName(StandardBasicTypes.BLOB.getName());
				dataValue.makeLob();
				this.dataColumn.setValue(dataValue);

				this.addColumn(this.identifierColumn);
				this.addColumn(this.startColumn);
				this.addColumn(this.endColumn);
				this.addColumn(this.dataColumn);

				final PrimaryKey pk = new PrimaryKey(this);
				pk.setName(tableName + "_pk");
				pk.addColumn(this.identifierColumn);
				pk.addColumn(this.startColumn);
				this.setPrimaryKey(pk);
			}

			@SuppressWarnings("deprecation")
			private SimpleValue createValue(
				final HibernateContext hibernateContext
			)
			{
				return new SimpleValue(
					(MetadataImplementor)hibernateContext.metadata(),
					this
				);
			}

			String sqlName()
			{
				return this.hibernateContext
					.jdbcServices()
					.getJdbcEnvironment()
					.getQualifiedObjectNameFormatter()
					.format(
						this.getQualifiedTableName(),
						this.hibernateContext.dialect()
					)
				;
			}

			String identifierColumnSqlName()
			{
				return this.identifierColumn.getQuotedName(this.hibernateContext.dialect());
			}

			String startColumnSqlName()
			{
				return this.startColumn.getQuotedName(this.hibernateContext.dialect());
			}

			String endColumnSqlName()
			{
				return this.endColumn.getQuotedName(this.hibernateContext.dialect());
			}

			String dataColumnSqlName()
			{
				return this.dataColumn.getQuotedName(this.hibernateContext.dialect());
			}

		}


		private static final class Select extends org.hibernate.sql.Select
		{
			final Table table;

			Select(
				final Table table
			)
			{
				super(table.hibernateContext.dialect());
				this.table = table;
			}

			Select addWhereClause(
				final String connector,
				final String condition
			)
			{
				final String whereClause = this.whereClause;
				if(XChars.isEmpty(whereClause))
				{
					this.setWhereClause(condition);
				}
				else
				{
					this.setWhereClause(whereClause + " " + connector + " " + condition);
				}
				return this;
			}

		}

	}

}
