package one.microstream.memory;

import static one.microstream.X.notNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Vector;

public final class XMemoryJDK8
{
	// CHECKSTYLE.OFF: ConstantName: type names are intentionally unchanged
	private static final long
		OFFSET_ArrayList_elementData     = XMemory.internalGetFieldOffset(ArrayList.class    , "elementData"      ),
		OFFSET_ArrayList_size            = XMemory.internalGetFieldOffset(ArrayList.class    , "size"             ),
		OFFSET_HashSet_map               = XMemory.internalGetFieldOffset(HashSet.class      , "map"              ),
		OFFSET_HashMap_loadFactor        = XMemory.internalGetFieldOffset(HashMap.class      , "loadFactor"       ),
		OFFSET_Hashtable_loadFactor      = XMemory.internalGetFieldOffset(Hashtable.class    , "loadFactor"       ),
		OFFSET_LinkedHashMap_loadFactor  = XMemory.internalGetFieldOffset(LinkedHashMap.class, "loadFactor"       ),
		OFFSET_LinkedHashMap_accessOrder = XMemory.internalGetFieldOffset(LinkedHashMap.class, "accessOrder"      ),
		OFFSET_PriorityQueue_queue       = XMemory.internalGetFieldOffset(PriorityQueue.class, "queue"            ),
		OFFSET_PriorityQueue_size        = XMemory.internalGetFieldOffset(PriorityQueue.class, "size"             ),
		OFFSET_Vector_elementData        = XMemory.internalGetFieldOffset(Vector.class       , "elementData"      ),
		OFFSET_Vector_elementCount       = XMemory.internalGetFieldOffset(Vector.class       , "elementCount"     ),
		OFFSET_Vector_capacityIncrement  = XMemory.internalGetFieldOffset(Vector.class       , "capacityIncrement"),
		OFFSET_Properties_Defaults       = XMemory.internalGetFieldOffset(Properties.class   , "defaults"         )
	;
	// CHECKSTYLE.ON: ConstantName
	
	
	public static Object[] accessArray(final ArrayList<?> arrayList)
	{
		// must check not null here explictely to prevent VM crashes
		return (Object[])XMemory.VM.getObject(notNull(arrayList), OFFSET_ArrayList_elementData);
	}

	public static void setSize(final ArrayList<?> arrayList, final int size)
	{
		// must check not null here explictely to prevent VM crashes
		XMemory.VM.putInt(notNull(arrayList), OFFSET_ArrayList_size, size);
	}

	/**
	 * My god. How incompetent can one be: they provide a constructor for configuring the load factor,
	 * but they provide no means to querying it. So if a hashset instance shall be transformed to another
	 * context and back (e.g. persistence), what is one supposed to do? Ignore the load factor and change
	 * the program behavior? What harm would it do to add an implementation-specific getter?
	 * <p>
	 * Not to mention the set wraps a map internally which is THE most moronic thing to do both memory-
	 * and performance-wise.
	 * <p>
	 * So another hack method has to provide basic functionality that is missing in the JDK.
	 * And should they ever get the idea to implement the set properly, this method will break.
	 *
	 * @param hashSet
	 * @return
	 */
	public static float getLoadFactor(final HashSet<?> hashSet)
	{
		// must check not null here explictely to prevent VM crashes
		final HashMap<?, ?> map = (HashMap<?, ?>)XMemory.VM.getObject(notNull(hashSet), OFFSET_HashSet_map);
		return getLoadFactor(map);
	}

	public static float getLoadFactor(final HashMap<?, ?> hashMap)
	{
		// must check not null here explictely to prevent VM crashes
		return XMemory.VM.getFloat(notNull(hashMap), OFFSET_HashMap_loadFactor);
	}

	public static float getLoadFactor(final Hashtable<?, ?> hashtable)
	{
		// must check not null here explictely to prevent VM crashes
		return XMemory.VM.getFloat(notNull(hashtable), OFFSET_Hashtable_loadFactor);
	}

	public static float getLoadFactor(final LinkedHashMap<?, ?> linkedHashMap)
	{
		// must check not null here explictely to prevent VM crashes
		return XMemory.VM.getFloat(notNull(linkedHashMap), OFFSET_LinkedHashMap_loadFactor);
	}

	public static boolean getAccessOrder(final LinkedHashMap<?, ?> linkedHashMap)
	{
		// must check not null here explictely to prevent VM crashes
		return XMemory.VM.getBoolean(notNull(linkedHashMap), OFFSET_LinkedHashMap_accessOrder);
	}
	
	public static Object[] accessArray(final Vector<?> vector)
	{
		// must check not null here explictely to prevent VM crashes
		return (Object[])XMemory.VM.getObject(notNull(vector), OFFSET_Vector_elementData);
	}
	
	public static int getElementCount(final Vector<?> vector)
	{
		// must check not null here explictely to prevent VM crashes
		return XMemory.VM.getInt(notNull(vector), OFFSET_Vector_elementCount);
	}

	public static void setElementCount(final Vector<?> vector, final int size)
	{
		// must check not null here explictely to prevent VM crashes
		XMemory.VM.putInt(notNull(vector), OFFSET_Vector_elementCount, size);
	}
	
	public static int getCapacityIncrement(final Vector<?> vector)
	{
		// must check not null here explictely to prevent VM crashes
		return XMemory.VM.getInt(notNull(vector), OFFSET_Vector_capacityIncrement);
	}

	public static void setCapacityIncrement(final Vector<?> vector, final int size)
	{
		// must check not null here explictely to prevent VM crashes
		XMemory.VM.putInt(notNull(vector), OFFSET_Vector_capacityIncrement, size);
	}
	
	public static Properties accessDefaults(final Properties properties)
	{
		// must check not null here explictely to prevent VM crashes
		return (Properties)XMemory.VM.getObject(notNull(properties), OFFSET_Properties_Defaults);
	}

	public static void setDefaults(final Properties properties, final Properties defaults)
	{
		// must check not null here explictely to prevent VM crashes
		XMemory.VM.putObject(notNull(properties), OFFSET_Properties_Defaults, defaults);
	}

	public static Object[] accessArray(final PriorityQueue<?> priorityQueue)
	{
		// must check not null here explictely to prevent VM crashes
		return (Object[])XMemory.VM.getObject(notNull(priorityQueue), OFFSET_PriorityQueue_queue);
	}

	public static void setSize(final PriorityQueue<?> priorityQueue, final int size)
	{
		// must check not null here explictely to prevent VM crashes
		XMemory.VM.putInt(notNull(priorityQueue), OFFSET_PriorityQueue_size, size);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException
	 */
	private XMemoryJDK8()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}
