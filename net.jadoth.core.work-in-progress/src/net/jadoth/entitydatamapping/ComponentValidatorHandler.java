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
package net.jadoth.entitydatamapping;

import java.awt.Component;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.jadoth.entitydatamapping.interfaces.ComponentValidator;

// TODO: Auto-generated Javadoc
/**
 * The Interface ComponentValidatorHandler.
 * 
 * @author Thomas Muenz
 */
/* (09.10.2009 TM)TODO:
 * This class is almost the same as ComponentAffectorHandler, 
 * except for the boolean logic and the executor type.<br>
 * Maybe it can be consolidated with ComponentAffectorHandler . 
 */
public interface ComponentValidatorHandler 
{
	
	/**
	 * Register.
	 * 
	 * @param <C> the generic type
	 * @param annotation the annotation
	 * @param component the component
	 * @param validator the validator
	 */
	public <C extends Component> void register(Class<? extends Annotation> annotation, C component, ComponentValidator<C> validator);
	
	/**
	 * Validate.
	 * 
	 * @param <C> the generic type
	 * @param component the component
	 * @return true, if successful
	 */
	public <C extends Component> boolean validate(C component);
	
	/**
	 * Validate.
	 * 
	 * @param <C> the generic type
	 * @param annotation the annotation
	 * @return true, if successful
	 */
	public <C extends Component> boolean validate(Class<? extends Annotation> annotation);
	
	// (09.09.2009 TM)FIXME: clear() etc einbauen
	
	/**
	 * The Class Body.
	 */
	public class Body implements ComponentValidatorHandler
	{
		
		/** The entries by annotation. */
		private Map<Class<? extends Annotation>, List<Entry<?>>> entriesByAnnotation = new HashMap<Class<? extends Annotation>, List<Entry<?>>>();
		
		/** The entries by component. */
		private Map<Component, List<Entry<?>>> entriesByComponent = new HashMap<Component, List<Entry<?>>>();
		


		/**
		 * Execute all.
		 * 
		 * @param <C> the generic type
		 * @param entries the entries
		 * @return true, if successful
		 */
		@SuppressWarnings("unchecked")
		protected <C extends Component> boolean executeAll(List<Entry<?>> entries) 
		{
			if(entries == null) return true; //no validator, so return true
			
			for(Entry<?> entry : entries) {
				final Entry<C> e = (Entry<C>)entry;
				if(!e.validator.validate(e.component)) return false;				
			}
			return true;
		}		
		
		/**
		 * @param <C>
		 * @param component
		 * @return
		 * @see net.jadoth.entitydatamapping.ComponentValidatorHandler#validate(java.awt.Component)
		 */
		@Override
		public <C extends Component> boolean validate(C component) {
			return executeAll(this.entriesByComponent.get(component));						
		}
		
		/**
		 * @param <C>
		 * @param annotation
		 * @return
		 * @see net.jadoth.entitydatamapping.ComponentValidatorHandler#validate(java.lang.Class)
		 */
		@Override
		public <C extends Component> boolean validate(Class<? extends Annotation> annotation) {
			return executeAll(this.entriesByAnnotation.get(annotation));				
		}
		
		/**
		 * @param <C>
		 * @param annotation
		 * @param component
		 * @param validator
		 * @see net.jadoth.entitydatamapping.ComponentValidatorHandler#register(java.lang.Class, java.awt.Component, net.jadoth.entitydatamapping.interfaces.ComponentValidator)
		 */
		@Override
		public <C extends Component> void register(Class<? extends Annotation> annotation, C component, ComponentValidator<C> validator) {
			final Entry<C> e = new Entry<C>(annotation, component, validator);
			
			List<Entry<?>> byAnnotationList = this.entriesByAnnotation.get(annotation);
			if(byAnnotationList == null){
				byAnnotationList = new LinkedList<Entry<?>>();
				this.entriesByAnnotation.put(annotation, byAnnotationList);
			}
			byAnnotationList.add(e);
			
			List<Entry<?>> byComponentList = this.entriesByComponent.get(component);
			if(byComponentList == null){
				byComponentList = new LinkedList<Entry<?>>();
				this.entriesByComponent.put(component, byComponentList);
			}
			byComponentList.add(e);		
		}
		
				
		/**
		 * The Class Entry.
		 * 
		 * @param <C> the generic type
		 */
		private class Entry<C extends Component>
		{
			
			/** The annotation. */
			@SuppressWarnings("unused") //maybe later
			private Class<? extends Annotation> annotation;		
			
			/** The component. */
			private C component;
			
			/** The validator. */
			private ComponentValidator<C> validator;
			
			/**
			 * Instantiates a new entry.
			 * 
			 * @param annotation the annotation
			 * @param component the component
			 * @param validator the validator
			 */
			public Entry(Class<? extends Annotation> annotation, C component, ComponentValidator<C> validator) {
				super();
				this.annotation = annotation;
				this.component = component;
				this.validator = validator;				
			}
		}
		
	}	
	
}
