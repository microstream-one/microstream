package one.microstream.persistence.types;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;
import static one.microstream.math.XMath.positive;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import one.microstream.collections.types.XGettingCollection;

public interface PersistenceTypeDefinitionMemberFieldReflective
extends PersistenceTypeDefinitionMemberField, PersistenceTypeDescriptionMemberFieldReflective
{
	public String runtimeDeclaringClassName();
	
	@Override
	public default String runtimeQualifier()
	{
		return this.runtimeDeclaringClassName();
	}
	
	public Class<?> declaringClass();
	
	@Override
	public Field field();
	
	
	
	public static <C extends Consumer<? super Field>> C unbox(
		final XGettingCollection<? extends PersistenceTypeDefinitionMemberFieldReflective> members,
		final C collector
	)
	{
		for(final PersistenceTypeDefinitionMemberFieldReflective member : members)
		{
			collector.accept(member.field());
		}
		
		return collector;
	}
	
		
	public static PersistenceTypeDefinitionMemberFieldReflective New(
		final String   runtimeDeclaringClass  ,
		final Class<?> declaringClass         ,
		final Field    field                  ,
		final Class<?> type                   ,
		final String   typeName               ,
		final String   name                   ,
		final String   declaringTypeName      ,
		final boolean  isReference            ,
		final long     persistentMinimumLength,
		final long     persistentMaximumLength
	)
	{
		return new PersistenceTypeDefinitionMemberFieldReflective.Default(
			 mayNull(runtimeDeclaringClass)  ,
			 mayNull(declaringClass)         ,
			 mayNull(field)                  ,
			 mayNull(type)                   ,
			 notNull(typeName)               ,
			 notNull(name)                   ,
			 notNull(declaringTypeName)      ,
			         isReference             ,
			positive(persistentMinimumLength),
			positive(persistentMaximumLength)
		);
	}
	
	public static PersistenceTypeDefinitionMemberFieldReflective New(
		final Field field                  ,
		final long  persistentMinimumLength,
		final long  persistentMaximumLength
	)
	{
		return New(
			field.getDeclaringClass().getName(),
			field.getDeclaringClass()          ,
			field                              ,
			field.getType()                    ,
			field.getType().getName()          ,
			field.getName()                    ,
			field.getDeclaringClass().getName(),
			!field.getType().isPrimitive()     ,
			positive(persistentMinimumLength)  ,
			positive(persistentMaximumLength)
		);
	}

	public final class Default
	extends PersistenceTypeDescriptionMemberFieldReflective.Default
	implements PersistenceTypeDefinitionMemberFieldReflective
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		// owner type must be initialized effectively final to prevent circular constructor dependencies
		private final Class<?> type                     ;
		private final String   runtimeDeclaringClassName;
		private final Class<?> declaringClass           ;
		private final Field    field                    ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Default(
			final String   runtimeDeclClassName,
			final Class<?> declaringClass      ,
			final Field    field               ,
			final Class<?> type                ,
			final String   typeName            ,
			final String   name                ,
			final String   declaringTypeName   ,
			final boolean  isReference         ,
			final long     persistentMinLength ,
			final long     persistentMaxLength
		)
		{
			super(
				typeName           ,
				declaringTypeName  ,
				name               ,
				isReference        ,
				persistentMinLength,
				persistentMaxLength
			);

			this.runtimeDeclaringClassName = runtimeDeclClassName;
			this.declaringClass            = declaringClass      ;
			this.field                     = field               ;
			this.type                      = type                ;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public String runtimeDeclaringClassName()
		{
			return this.runtimeDeclaringClassName;
		}
				
		@Override
		public final Class<?> declaringClass()
		{
			return this.declaringClass;
		}
		
		@Override
		public final Field field()
		{
			return this.field;
		}
		
		@Override
		public final Class<?> type()
		{
			return this.type;
		}

	}

}
