/*
 * Copyright (c) 2008-2010, Thomas Muenz
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.jadoth.reflect;

import net.jadoth.exceptions.NotAnArrayException;


/**
 *
 * @author Thomas Muenz
 */
public class ArrayMetaInformation
{
	///////////////////////////////////////////////////////////////////////////
	//  static methods   //
	/////////////////////

	public static final int determineDimensions(final Class<?> arrayClass) throws NotAnArrayException
	{
		if(!arrayClass.isArray())
		{
			throw new NotAnArrayException(arrayClass);
		}
		int dim = 1;
		Class<?> componentType = arrayClass.getComponentType();
		while(componentType.isArray())
		{
			dim++;
			componentType = componentType.getComponentType();
		}
		return dim;
	}

	public static final Class<?> determineComponentType(final Class<?> arrayClass) throws NotAnArrayException
	{
		if(!arrayClass.isArray())
		{
			throw new NotAnArrayException(arrayClass);
		}
		Class<?> componentType = arrayClass.getComponentType();
		while(componentType.isArray())
		{
			componentType = componentType.getComponentType();
		}
		return componentType;
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////
	final Class<?> componentType;
	final int      dimensions   ;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public ArrayMetaInformation(final Class<?> arrayClass) throws NotAnArrayException
	{
		if(!arrayClass.isArray())
		{
			throw new NotAnArrayException(arrayClass);
		}
		int dim = 1;
		Class<?> componentType = arrayClass.getComponentType();
		while(componentType.isArray())
		{
			dim++;
			componentType = componentType.getComponentType();
		}
		this.dimensions = dim;
		this.componentType = componentType;
	}

}
