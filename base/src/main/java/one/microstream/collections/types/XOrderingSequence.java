package one.microstream.collections.types;

/*-
 * #%L
 * microstream-base
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import one.microstream.collections.IndexExceededException;
import one.microstream.collections.interfaces.ExtendedSequence;

public interface XOrderingSequence<E> extends ExtendedSequence<E>
{
	/**
	 * Moves the element from the sourceIndex in the sequence to the targetIndex.<br>
	 * All other elements are possibly moved to create the empty slot for the shifting element.
	 * <p>
	 * Does not expand or shrink the capacity of the sequence.
	 * <p>
	 * Throws a {@link IndexExceededException} if sourceIndex or targetIndex are
	 * greater than the size of the sequence.
	 * 
	 * @param sourceIndex points to the source element; Index of the source element
	 * @param targetIndex points to the target element; Index of the target element
	 * @return this
	 */
	public XOrderingSequence<E> shiftTo(long sourceIndex, long targetIndex);
	
	/**
	 * Moves multiple elements from the sourceIndex in the sequence to the targetIndex.<br>
	 * All other elements are possibly moved to create the empty slot for the shifting element.
	 * <p>
	 * Does not expand or shrink the capacity of the sequence.
	 * <p>
	 * Throws a {@link IndexExceededException} if sourceIndex or targetIndex
	 * exceed the size of the sequence.
	 * 
	 * @param sourceIndex points to the source element; Index of the source element
	 * @param targetIndex points to the target element; Index of the target element
	 * @param length Amount of moved elements.
	 * @return self
	 */
	public XOrderingSequence<E> shiftTo(long sourceIndex, long targetIndex, long length);
	
	/**
	 * Moves the element from the sourceIndex in the sequence to a higher index position.<br>
	 * All other elements are possibly moved to create the empty slot for the shifting element.
	 * ("to the right")
	 * <p>
	 * Does not expand or shrink the capacity of the sequence.
	 * <p>
	 * Throws a {@link IndexExceededException} if sourceIndex or targetIndex
	 * (sourceIndex+distance) exceed the size of the sequence.
	 * 
	 * @param sourceIndex points to the source element; Index of the source element
	 * @param distance of how far the element should be moved.
	 * Example: 1 moves the element from position 21 to position 22
	 * @return self
	 */
	public XOrderingSequence<E> shiftBy(long sourceIndex, long distance);
	
	/**
	 * Moves multiple elements from the sourceIndex in the sequence to a higher index position.<br>
	 * All other elements are possibly moved to create the empty slot for the shifting elements.
	 * ("to the right")
	 * <p>
	 * Does not expand or shrink the capacity of the sequence.
	 * <p>
	 * Throws a {@link IndexExceededException} if sourceIndex or targetIndex
	 * (sourceIndex+distance+length) exceed the size of the sequence.
	 * 
	 * @param sourceIndex points to the source element; Index of the source element
	 * @param distance of how far the element should be moved.
	 * Example: 1 moves the element from position 21 to position 22
	 * @param length Amount of moved elements.
	 * 
	 * @return self
	 */
	public XOrderingSequence<E> shiftBy(long sourceIndex, long distance, long length);

	public XOrderingSequence<E> swap(long indexA, long indexB);
	
	public XOrderingSequence<E> swap(long indexA, long indexB, long length);

	/**
	 * Reverses the order of its own elements and returns itself.
	 * 
	 * @return this
	 */
	public XOrderingSequence<E> reverse();

}
