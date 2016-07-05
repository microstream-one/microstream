package net.jadoth.graphConsolidator;

import java.lang.reflect.Field;
import java.util.function.Function;
import java.util.function.Predicate;

import net.jadoth.reflect.JadothReflect;

public interface EntitySubjectSubstitutionHandler<E> extends Predicate<E>
{
	
	
	public final class GenericFields<E> implements EntitySubjectSubstitutionHandler<E>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final SubjectSubstitutorProvider subjectSubstitutorProvider;
		private final Field[]                    relevantFields            ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		GenericFields(final SubjectSubstitutorProvider subjectSubstitutorProvider, final Field[] relevantFields)
		{
			super();
			this.subjectSubstitutorProvider = subjectSubstitutorProvider;
			this.relevantFields             = relevantFields            ;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public boolean test(final E instance)
		{
			final SubjectSubstitutorProvider substitutorProvider = this.subjectSubstitutorProvider;
			
			boolean changed = false;
			for(final Field field : this.relevantFields)
			{
				final Object current = JadothReflect.getFieldValue(field, instance);
				if(current == null)
				{
					continue;
				}
				
				@SuppressWarnings("unchecked")
				final SubjectSubstitutor<Object> substitutor = substitutorProvider.provideSubjectSubstitutor((Class<Object>)current.getClass());
				
				// replace with substitute if applicable
				final Object substitute = substitutor.substitute(current);
				if(substitute != current)
				{
					JadothReflect.setFieldValue(field, instance, substitute);

					// looks funny, but should be faster (n reads instead of writes)
					if(!changed)
					{
						changed = true;
					}
				}
			}
			
			return changed;
		}
		
	}
	
	public final class GenericArray implements EntitySubjectSubstitutionHandler<Object[]>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final SubjectSubstitutorProvider subjectSubstitutorProvider;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		GenericArray(final SubjectSubstitutorProvider subjectSubstitutorProvider)
		{
			super();
			this.subjectSubstitutorProvider = subjectSubstitutorProvider;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public boolean test(final Object[] instance)
		{
			final SubjectSubstitutorProvider substitutorProvider = this.subjectSubstitutorProvider;
			
			boolean changed = false;
			final int length = instance.length;
			for(int i = 0; i < length; i++)
			{
				if(instance[i] == null)
				{
					continue;
				}
				
				@SuppressWarnings("unchecked")
				final SubjectSubstitutor<Object> substitutor = substitutorProvider.provideSubjectSubstitutor((Class<Object>)instance[i].getClass());
				
				// replace with substitute if applicable
				final Object substitute = substitutor.substitute(instance[i]);
				if(substitute != instance[i])
				{
					instance[i] = substitute;

					// looks funny, but should be faster (n reads instead of writes)
					if(!changed)
					{
						changed = true;
					}
				}
			}
			
			return changed;
		}
		
	}
	
	@Deprecated
	public static boolean substituteArrayElemens(
		final Object[]                 instance   ,
		final Function<Object, Object> substitutor
	)
	{
		// iterate all relevant fields and track change
		boolean changed = false;
		final int length = instance.length;
		for(int i = 0; i < length; i++)
		{
			// replace with substitute if applicable
			final Object substitute = substitutor.apply(instance[i]);
			if(substitute != instance[i])
			{
				instance[i] = substitute;

				// looks funny, but should be faster (n reads instead of writes)
				if(!changed)
				{
					changed = true;
				}
			}
		}

		return changed;
	}

	@Deprecated
	public static boolean substituteFieldValues(
		final Object                   instance   ,
		final Field[]                  fields     ,
		final Function<Object, Object> substitutor
	)
	{
		// iterate all relevant fields and track change
		boolean changed = false;
		for(final Field field : fields)
		{
			// get current value and substitute and replace if applicable
			final Object current    = JadothReflect.getFieldValue(field, instance);
			final Object substitute = substitutor.apply(current);
			if(substitute != current)
			{
				JadothReflect.setFieldValue(field, instance, substitute);

				// looks funny, but should be faster (n reads instead of writes)
				if(!changed)
				{
					changed = true;
				}
			}
		}

		return changed;
	}
}
