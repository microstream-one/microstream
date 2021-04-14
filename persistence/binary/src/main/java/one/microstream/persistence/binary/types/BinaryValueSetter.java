package one.microstream.persistence.binary.types;

import one.microstream.persistence.types.PersistenceLoadHandler;


public interface BinaryValueSetter
{
	/* (20.09.2018 TM)TODO: BinaryValueSetter performance optimization
	 * A distinction betwee BinaryValueSetterPrimitive and BinaryValueSetterReference could be made.
	 * Since the persistent form order all references at the start and all primitives afterwards,
	 * the switching overhead from one array iteration to another would be minimal.
	 * As a result, setting a primitive value could omit the reference to the idResolver.
	 * However it is not clear how much performance that would effectively bring.
	 * 
	 * Before a premature optimization is done, this should be tested.
	 * However, if the order of all primitives in instance form and in persistent form is exactely the same,
	 * a shortcut for copying primitives could be made where a low-level loop (Unsafe) copies all primitive
	 * values at one swoop. Or at least all per inheritence level.
	 * However, all these assumptions about memory layout would have to be thoroughly validated at type handler
	 * initialization and if not true, a fallback to the current mechanism would have to be used.
	 * 
	 * In short: here should be a lot of performance optimization potential, but it has to be done properly.
	 */
	
	/**
	 * Sets a single value, read from binary (persisted) form at the absolute memory {@code address}to the memory so that it can be used by common program
	 * logic, usually to the field offset of a target object or an index of a target array.
	 * If {@code target} is null, the {@code targetOffset} is interpreted as an absolute memory address
	 * instead of a relative offset.
	 * 
	 * @param address the absolute source memory address of the value to be set.
	 * @param target the target object to set the value to or {@code null} for absolute memory addressing.
	 * @param targetOffset the target object's relative memory offset or an absolute target memory address.
	 * @param handler a helper instance to resolve OIDs to instance references.
	 * @return absolute source memory address pointing at the first byte following the read value.
	 */
	public long setValueToMemory(
		long                   address     ,
		Object                 target      ,
		long                   targetOffset,
		PersistenceLoadHandler handler
	);
}
