/**
 * 
 */
package net.jadoth.entitydatamapping.test;

import net.jadoth.reflect.JadothReflect;

/**
 * @author Thomas Muenz
 *
 */
public class MainTestEntityDataMapping
{

	public static void main(final String[] args)
	{
		final Class<TestEntity> c = TestEntity.class;
		
		final TestForm<TestEntity, TestComponent<TestEntity, ?>> form = new TestForm<>(c);
		
		final TestComponent<TestEntity, Integer> compInt = new TestComponent<>("compInt", int.class, c);
		compInt.assignDataGetter(JadothReflect.getAnyMethod(TestEntity.class, "getIntValue"));
		compInt.assignDataSetter(JadothReflect.getAnyMethod(TestEntity.class, "setIntValue"));
		form.addMapper(compInt);
		
		final TestComponent<TestEntity, Integer> compInteger = new TestComponent<>("compInteger", Integer.class, c);
		compInteger.assignDataGetter(JadothReflect.getAnyMethod(TestEntity.class, "getIntegerValue"));
		compInteger.assignDataSetter(JadothReflect.getAnyMethod(TestEntity.class, "setIntegerValue"));
		form.addMapper(compInteger);
		
		final TestComponent<TestEntity, String> compString = new TestComponent<>("compString", String.class, c);
		compString.assignDataGetter(JadothReflect.getAnyMethod(TestEntity.class, "getStringValue"));
		compString.assignDataSetter(JadothReflect.getAnyMethod(TestEntity.class, "setStringValue"));
		form.addMapper(compString);
		
		final TestEntity entity = new TestEntity();
		entity.setIntegerValue(111);
		entity.setIntValue(1);
		entity.setStringValue("testEntity");
		
		System.out.println("Initial Entity:");
		System.out.println(entity);
		System.out.println("");
		
		System.out.println("Assign entity to Form");
		form.setDataEntity(entity);
		System.out.println("");
		
		System.out.println("Update Form:");
		form.readFromEntity();
		form.render();
		System.out.println("");
		
		
		System.out.print("User inputting data into Form...: ");
		final String userInput = "Entered Text";
		System.out.println("\""+userInput+"\"");
		compString.setEntityDataValue(userInput);
		compString.saveToEntity();
		System.out.println("");
		
		System.out.println("Render Form:");
		form.render();
		System.out.println("");
		
		System.out.println("Entity now is:");
		System.out.println(entity);
		System.out.println("");

		
		System.out.println("Modify entity programmatically");
		entity.setIntValue(entity.getIntValue()+1);
		entity.setStringValue(entity.getStringValue()+" and modified: intValue+1");
		System.out.println("");
		
		System.out.println("Update Form:");
		form.readFromEntity();
		form.render();
		System.out.println("");
		
	}

}
