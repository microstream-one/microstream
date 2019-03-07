package some.app.entitylogging;

import one.microstream.chars.XChars;
import one.microstream.concurrency.XThreads;
import one.microstream.entity.Entity;
import one.microstream.entity.EntityLayer;
import one.microstream.entity.EntityLayerProvider;

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
			+ "\" " + action + XChars.systemString(this.$entity())
			+ (methodName != null ? " via #" + methodName: "")
			+ "."
		);
	}
	
	@Override
	public E $data()
	{
		this.logAction("reads data of ", XThreads.getMethodNameForDeclaringClassName(this.declaringClassname));
		return super.$data();
	}
	
	@Override
	public boolean $updateData(final E newData)
	{
		this.logAction("updates data of ", null);
		return super.$updateData(newData);
	}
	
}
