package some.app.entitylogging;

import net.jadoth.concurrent.JadothThreads;
import net.jadoth.entity.Entity;
import net.jadoth.entity.EntityLayer;
import net.jadoth.entity.EntityLayerProvider;
import net.jadoth.util.chars.JadothStrings;

public class EntityLogger<E extends Entity<E>> extends EntityLayer<E>
{
	public static <E extends Entity<E>> EntityLayerProvider<E> provideLogging()
	{
		return e ->
			new EntityLogger<>(e)
		;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final String declaringClassname;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public EntityLogger(final Entity<E> innerInstance)
	{
		super(innerInstance);
		this.declaringClassname = innerInstance.$entity().getClass().getName();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private void logAction(final String action, final String methodName)
	{
		System.out.println(
			"Thread \"" + Thread.currentThread().getName()
			+ "\" " + action + JadothStrings.systemString(this.$entity())
			+ (methodName != null ? " via #" + methodName: "")
			+ "."
		);
	}
	
	@Override
	public E $data()
	{
		this.logAction("reads data of ", JadothThreads.getMethodNameForDeclaringClassName(this.declaringClassname));
		return super.$data();
	}
	
	@Override
	public boolean $updateData(final E newData)
	{
		this.logAction("updates data of ", null);
		return super.$updateData(newData);
	}
	
}
