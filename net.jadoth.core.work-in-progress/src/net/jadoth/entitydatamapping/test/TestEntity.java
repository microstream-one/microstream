/**
 * 
 */
package net.jadoth.entitydatamapping.test;

import net.jadoth.reflect.Label;

/**
 * @author Thomas Muenz
 *
 */
public class TestEntity
{
	public static final String LABEL_EntityData = "EntityData";
	public static final String LABEL_EntityDataField = LABEL_EntityData+"Field";
	public static final String LABEL_EntityDataGetter = LABEL_EntityData+"Getter";
	public static final String LABEL_EntityDataSetter = LABEL_EntityData+"Setter";
	
	
	
	private int intValue = 0;
	private Integer integerValue = null;
	
	@Label({LABEL_EntityData, LABEL_EntityDataField})
	private String stringValue = "nix";
	protected String gibtKeineGetterSetter = "haha";
	
	
	
	/**
	 * @return the intValue
	 */
	public int getIntValue() {
		return this.intValue;
	}
	/**
	 * @return the integerValue
	 */
	public Integer getIntegerValue() {
		return this.integerValue;
	}
	/**
	 * @return the stringValue
	 */
	@Label({LABEL_EntityData, LABEL_EntityDataGetter})
	public String getStringValue() {
		return this.stringValue;
	}
	/**
	 * @param intValue the intValue to set
	 */
	public void setIntValue(final int intValue) {
		this.intValue = intValue;
	}
	/**
	 * @param integerValue the integerValue to set
	 */
	public void setIntegerValue(final Integer integerValue) {
		this.integerValue = integerValue;
	}
	/**
	 * @param stringValue the stringValue to set
	 */
	@Label({LABEL_EntityData, LABEL_EntityDataSetter})
	public void setStringValue(final String stringValue) {
		this.stringValue = stringValue;
	}
	
	
	@Override
	public String toString() {
		final String n = "\n";
		final String t = "\t";
		final String _is_ = " = ";
		final StringBuilder sb = new StringBuilder(512);
		
		sb
		.append(this.getClass().getSimpleName()).append(":")
		.append(n).append(t).append("intValue").append(_is_).append(this.intValue)
		.append(n).append(t).append("integerValue").append(_is_).append(this.integerValue)
		.append(n).append(t).append("stringValue").append(_is_).append(this.stringValue)
		;
		return sb.toString();
	}
	
	
	
	
	
}
