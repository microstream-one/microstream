/**
 * 
 */
package net.jadoth.entitydatamapping.test;

import net.jadoth.entitydatamapping.EntityDataMapper;
import net.jadoth.entitydatamapping.EntityDataMapperForm;

/**
 * @author Thomas Muenz
 *
 */
public class TestForm<E, M extends EntityDataMapper<E, ?, M>> extends EntityDataMapperForm.Implementation<E, M>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////	
	/**
	 * @param entityClass
	 */
	public TestForm(Class<E> entityClass) {
		super(entityClass);
	}
	/**
	 * @param entity
	 */
	public TestForm(E entity) {
		super(entity);
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////
	@SuppressWarnings("unchecked")
	public void render(){
		System.out.println(TestForm.class.getSimpleName()+": ");
		for(EntityDataMapper<E, ?, M> comp : getMappers()) {
			((TestComponent<E, ?>)comp).render();
		}
	}
}
