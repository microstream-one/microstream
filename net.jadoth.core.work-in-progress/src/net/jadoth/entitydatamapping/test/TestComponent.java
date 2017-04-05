/**
 * 
 */
package net.jadoth.entitydatamapping.test;

import net.jadoth.entitydatamapping.EntityDataMapper;

/**
 * @author Thomas Muenz
 *
 */
public class TestComponent<E, D> extends EntityDataMapper.AbstractImplementation<E, D, TestComponent<E, D>>
{
	private String name = null;
	private D dataValue = null;

	public TestComponent(final String name, final Class<D> dataType, final Class<E> entityClass) {
		super(entityClass, dataType);
		this.name = name;
	}

	@Override
	public D getEntityDataValue() {
		return this.dataValue;
	}

	@Override
	public TestComponent<E, D> setEntityDataValue(final D value) {
		this.dataValue = value;
		return this;
	}

	
	public void render(){
		System.out.println(this.name + ": " + this.dataValue);
	}
	
	public void clearAssignments(){
		this.field = null;
		this.getter = null;
		this.setter = null;
		this.dataAnnotation = null;
	}
	
	public void printClause(final String caption){
		System.out.println("");
		this.print(caption);
	}
	
	public void print(final String caption){
		System.out.println(caption);
		System.out.println(this.toString());
	}

	@Override
	public boolean validateForSave() {
		return true;
	}
	

}
