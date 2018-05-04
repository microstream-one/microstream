/**
 * 
 */
package net.jadoth.entitydatamapping.test;

import net.jadoth.reflect.XReflect;



/**
 * @author Thomas Muenz
 *
 */
public class MainTestAssignmentMethods
{
	private static final String stringValue = "stringValue";

	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		final TestEntity entity = new TestEntity();
		
		final TestComponent<TestEntity, String> mapper = new TestComponent<>("stringValueMapper", String.class, TestEntity.class);
		mapper.setDataEntity(entity);
		
		mapper.printClause("Init");
		
		mapper.assignDataField(XReflect.getAnyField(TestEntity.class, stringValue), false);
		mapper.printClause("assignDataField /false");
		
		mapper.clearAssignments();
		mapper.assignDataField(XReflect.getAnyField(TestEntity.class, stringValue), true);
		mapper.printClause("assignDataField /true");
				
		mapper.clearAssignments();
		mapper.assignDataAccessByLabel(TestEntity.LABEL_EntityDataField, true);
		mapper.printClause("assignDataAccessByLabel /field /true");
		mapper.assignDataAccessByLabel(TestEntity.LABEL_EntityDataGetter, true);
		mapper.print("assignDataAccessByLabel /getter /true");
		mapper.assignDataAccessByLabel(TestEntity.LABEL_EntityDataSetter, true);
		mapper.print("assignDataAccessByLabel /setter /true");
		
		mapper.clearAssignments();
		mapper.assignDataAccessByLabel(TestEntity.LABEL_EntityData, true);
		mapper.printClause("assignDataAccessByLabel /alle /true");
		
		mapper.clearAssignments();
		mapper.assignDataAccessBySearchString("setStringValue", true);
		mapper.printClause("assignDataAccessBySearchString /stringValue /false");
		
		mapper.assignDataAccessBySearchString(TestEntity.LABEL_EntityData, true);
		mapper.printClause("assignDataAccessBySearchString /LABEL_EntityData /true");

	}

}
