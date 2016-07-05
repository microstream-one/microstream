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
package net.jadoth.entitydatamapping.exceptions;


// TODO: Auto-generated Javadoc
/**
 * The Class EntitySetDataException.
 * 
 * @author Thomas Muenz
 */
public class EntitySetDataException extends EntityMappingException 
{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 8026760227925777967L;

	/**
	 * Instantiates a new entity set data exception.
	 */
	public EntitySetDataException() {
		super();
	}

	/**
	 * Instantiates a new entity set data exception.
	 * 
	 * @param message the message
	 */
	public EntitySetDataException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new entity set data exception.
	 * 
	 * @param cause the cause
	 */
	public EntitySetDataException(Throwable cause) {
		super(cause);
	}

	/**
	 * Instantiates a new entity set data exception.
	 * 
	 * @param message the message
	 * @param cause the cause
	 */
	public EntitySetDataException(String message, Throwable cause) {
		super(message, cause);
	}

}
