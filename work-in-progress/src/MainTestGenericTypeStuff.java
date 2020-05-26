

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import one.microstream.chars.XChars;
import one.microstream.collections.types.XReference;

public class MainTestGenericTypeStuff
{
	
	public static void main(final String[] args)
	{
		doit(MyClass2.class);
	}
	
	
	static void doit(final Class<?> clazz)
	{
		for(Class<?> c = clazz; c != Object.class; c = c.getSuperclass())
		{
			for(final Field field : c.getDeclaredFields())
			{
				System.out.println("Field " + field);
				
				final Type genericType = field.getGenericType();
				if(genericType instanceof ParameterizedType)
				{
					final ParameterizedType parameterizedType = (ParameterizedType)genericType;
					final Type typeParameter = parameterizedType.getActualTypeArguments()[0];
					System.out.println(" has parameterized type " + XChars.systemString(parameterizedType) + " " + parameterizedType);
					System.out.println(" with type parameter " + XChars.systemString(typeParameter) + " " + typeParameter);
				}
				else
				{
					System.out.println(" has an omitted generic type.");
				}
				
				System.out.println();
			}
		}
	}
	
}


class MyClass1<T>
{
	XReference<? extends T> reference1;
}

class MyClass2 extends MyClass1<String>
{
	XReference<String> reference2;
	XReference reference3;
}