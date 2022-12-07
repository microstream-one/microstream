package one.microstream.integrations.lucene.types;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FloatPoint;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;

import one.microstream.integrations.lucene.annotations.IndexId;
import one.microstream.integrations.lucene.annotations.IndexProperty;
import one.microstream.reflect.XReflect;

public interface EntityMapper<E>
{
	public Document toDocument(E entity);
	
	public E toEntity(Document document);
	
	
	public static class AnnotationBased<E> implements EntityMapper<E>
	{
		private final Class<E>                    type          ;
		private final Function<IndexableField, E> entityResolver;
		private PropertyInfo                      idInfo        ;
		private List<PropertyInfo>                propertyInfos ;
		
		AnnotationBased(
			final Class<E>                    type          ,
			final Function<IndexableField, E> entityResolver
		)
		{
			super();
			this.type           = type          ;
			this.entityResolver = entityResolver;
		
			
		}
		
		private void lazyInit()
		{
			// Double-checked locking to reduce the overhead of acquiring a lock
			PropertyInfo idInfo = this.idInfo;
			if(idInfo == null)
			{
				synchronized(this)
				{
					if((idInfo = this.idInfo) == null)
					{
						this.init();
					}
				}
			}
		}
		
		private void init()
		{
			try
			{
				this.propertyInfos = new ArrayList<>();
				
				final BeanInfo beanInfo = Introspector.getBeanInfo(this.type);
				for(final PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors())
				{
					final Method        method   = propertyDescriptor.getReadMethod();
					final IndexId       id       = method.getAnnotation(IndexId.class);
					final IndexProperty property = method.getAnnotation(IndexProperty.class);
					if(id != null && property != null)
					{
						throw new IllegalStateException(
							"Property with both IndexId and IndexProperty annotation is not allowed: " +
							method.getDeclaringClass().getCanonicalName() + "." + propertyDescriptor.getName()
						);
					}
					if(id != null)
					{
						if(this.idInfo != null)
						{
							throw new IllegalStateException(
								"Multiple IndexId annotations in type: " + this.type.getCanonicalName()
							);
						}
						this.validateProperty(propertyDescriptor);
						this.idInfo = new PropertyInfo(
							this.fieldName(id.name(), propertyDescriptor),
							method,
							Store.YES,
							false
						);
					}
					else if(property != null)
					{
						this.validateProperty(propertyDescriptor);
						this.propertyInfos.add(new PropertyInfo(
							this.fieldName(property.name(), propertyDescriptor),
							method,
							property.store(),
							property.tokenize()
						));
					}
				}
				
				if(this.idInfo == null)
				{
					throw new IllegalStateException(
						"No IndexId annotations found for type: " + this.type.getCanonicalName()
					);
				}
			}
			catch(final IntrospectionException e)
			{
				throw new RuntimeException(e);
			}
		}
		
		private void validateProperty(final PropertyDescriptor property)
		{
			final Class<?> type = property.getPropertyType();
			if(!(
				type == String.class
			 || type == int.class
			 || type == Integer.class
			 || type == long.class
			 || type == Long.class
			 || type == double.class
			 || type == Double.class
			 || type == float.class
			 || type == Float.class
			))
			{
				throw new IllegalStateException(
					"Unsupported type for index: " + type.getCanonicalName() + "  " +
					property.getReadMethod().getDeclaringClass().getCanonicalName() + "." + property.getName()
				);
			}
		}
		
		private String fieldName(final String annotationName, final PropertyDescriptor propertyDescriptor)
		{
			return !annotationName.isEmpty()
				? annotationName
				: propertyDescriptor.getName()
			;
		}
		
		private void addFields(final E entity, final PropertyInfo info, final Document document)
		{
			final Object value = XReflect.invoke(info.readMethod, entity);
			if(value == null)
			{
				return;
			}
								
			final Class<?> type = value.getClass();
			if(type == Integer.class || type == int.class)
			{
				final int intValue = ((Integer)value).intValue();
				document.add(new IntPoint(info.name, intValue));
				if(info.store == Store.YES)
				{
					document.add(new StoredField(info.name, intValue));
				}
			}
			else if(type == Long.class || type == long.class)
			{
				final long longValue = ((Long)value).longValue();
				document.add(new LongPoint(info.name, longValue));
				if(info.store == Store.YES)
				{
					document.add(new StoredField(info.name, longValue));
				}
			}
			else if(type == Double.class || type == double.class)
			{
				final double doubleValue = ((Double)value).doubleValue();
				document.add(new DoublePoint(info.name, doubleValue));
				if(info.store == Store.YES)
				{
					document.add(new StoredField(info.name, doubleValue));
				}
			}
			else if(type == Float.class || type == float.class)
			{
				final float floatValue = ((Float)value).floatValue();
				document.add(new FloatPoint(info.name, floatValue));
				if(info.store == Store.YES)
				{
					document.add(new StoredField(info.name, floatValue));
				}
			}
			else
			{
				document.add(info.tokenize
					? new TextField(info.name, value.toString(), info.store)
					: new StringField(info.name, value.toString(), info.store)
				);
			}
		}
		
		@Override
		public Document toDocument(final E entity)
		{
			this.lazyInit();
			
			final Document document = new Document();
			
			this.addFields(entity, this.idInfo, document);
			for(final PropertyInfo propertyInfo : this.propertyInfos)
			{
				this.addFields(entity, propertyInfo, document);
			}
			
			return document;
		}
		
		@Override
		public E toEntity(final Document document)
		{
			this.lazyInit();
			
			return this.entityResolver.apply(
				document.getField(this.idInfo.name)
			);
		}
		
		
		static final class PropertyInfo
		{
			final String  name      ;
			final Method  readMethod;
			final Store   store     ;
			final boolean tokenize  ;
			
			PropertyInfo(
				final String  name      ,
				final Method  readMethod,
				final Store   store     ,
				final boolean tokenize
			)
			{
				super();
				this.name       = name      ;
				this.readMethod = readMethod;
				this.store      = store     ;
				this.tokenize   = tokenize  ;
			}
			
		}

	}
	
}
