package net.jadoth.persistence.types;

import java.util.function.Consumer;

import net.jadoth.collections.HashTable;
import net.jadoth.collections.LimitList;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.functional.BiProcedure;
import net.jadoth.reflect.JadothReflect;
import net.jadoth.util.KeyValue;
import net.jadoth.util.chars.VarString;

public interface PersistenceTypeDefinitionResolver
{
	public void resolveTypeDefinitions(
		XGettingSequence<PersistenceTypeDescription>               typeDescriptions                    ,
		Consumer<? super PersistenceTypeDefinition<?>>            typeDefinitionCollector             ,
		BiProcedure<? super PersistenceTypeDescription, Exception> unresolvableTypeDescriptionCollector
	);
	
	
	public static VarString assembleResolveExceptions(
		final XGettingSequence<KeyValue<PersistenceTypeDescription, Exception>> resolveProblems,
		final VarString                                                         vs
	)
	{
		if(resolveProblems.isEmpty())
		{
			return vs;
		}
		
		vs.add(resolveProblems.size() + " type resolving problems:").lf();
		for(final KeyValue<? extends PersistenceTypeDescription, ? extends Exception> p : resolveProblems)
		{
			vs.add(p.key().typeId() + " " + p.key().typeName() + ": " + p.value().toString()).lf();
		}
		vs.deleteLast();
		
		return vs;
	}
	
	
	public static PersistenceTypeDefinitionResolver New()
	{
		return new PersistenceTypeDefinitionResolver.Implementation();
	}
	
	public final class Implementation implements PersistenceTypeDefinitionResolver
	{
		@Override
		public void resolveTypeDefinitions(
			final XGettingSequence<PersistenceTypeDescription>               typeDescriptions                    ,
			final Consumer<? super PersistenceTypeDefinition<?>>            typeDefinitionCollector             ,
			final BiProcedure<? super PersistenceTypeDescription, Exception> unresolvableTypeDescriptionCollector
		)
		{
			// results are buffered until all items have been processed without unhandled exception
			final LimitList<PersistenceTypeDefinition<?>>          tdBuffer =
				LimitList.New(typeDescriptions.size())
			;
			final HashTable<PersistenceTypeDescription, Exception> exBuffer =
				HashTable.NewCustom(typeDescriptions.intSize())
			;
			
			// type descriptions are iterated and either successfully resolved or associated with the exception
			for(final PersistenceTypeDescription typeDescription : typeDescriptions)
			{
				final String typeName = typeDescription.typeName();
				
				final Class<?> type;
				try
				{
					// strictly only the resolving call inside the try, nothing else
					type = JadothReflect.classForName(typeName);
				}
				catch(final ReflectiveOperationException e)
				{
					exBuffer.add(typeDescription, e);
					continue;
				}
				
				final PersistenceTypeDefinition<?> td =
					new PersistenceTypeDefinition.Implementation<>(type, typeDescription)
				;
				tdBuffer.add(td);
			}
			
			// results are copied into the target collectors
			tdBuffer.iterate(typeDefinitionCollector);
			for(final KeyValue<PersistenceTypeDescription, Exception> ex : exBuffer)
			{
				unresolvableTypeDescriptionCollector.accept(ex.key(), ex.value());
			}
		}
		
	}
}
