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

	public TestComponent(String name, Class<D> dataType, Class<E> entityClass) {
		super(entityClass, dataType);
		this.name = name;
	}

	@Override
	public D getEntityDataValue() {
		return dataValue;
	}

	@Override
	public TestComponent<E, D> setEntityDataValue(D value) {
		this.dataValue = value;
		return this;
	}

	
	public void render(){
		System.out.println(this.name + ": "+dataValue);
	}
	
	public void clearAssignments(){
		this.field = null;
		this.getter = null;
		this.setter = null;
		this.dataAnnotation = null;
	}
	
	public void printClause(String caption){
		System.out.println("");
		print(caption);		
	}
	
	public void print(String caption){
		System.out.println(caption);
		System.out.println(this.toString());
	}

	@Override
	public boolean validateForSave() {
		return true;
	}
	

}
