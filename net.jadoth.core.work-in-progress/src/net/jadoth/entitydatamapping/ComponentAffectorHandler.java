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

import net.jadoth.entitydatamapping.interfaces.ComponentAffector;

// TODO: Auto-generated Javadoc
/**
 * The Interface ComponentAffectorHandler.
 * 
 * @author Thomas Muenz
 */
public interface ComponentAffectorHandler
{
	
	/**
	 * Register.
	 * 
	 * @param <C> the generic type
	 * @param annotation the annotation
	 * @param component the component
	 * @param affector the affector
	 */
	public <C extends Component> void register(Class<? extends Annotation> annotation, C component, ComponentAffector<C> affector);
	
	/**
	 * Execute affector.
	 * 
	 * @param <C> the generic type
	 * @param component the component
	 */
	public <C extends Component> void executeAffector(C component);
	
	/**
	 * Execute affector.
	 * 
	 * @param <C> the generic type
	 * @param annotation the annotation
	 */
	public <C extends Component> void executeAffector(Class<? extends Annotation> annotation);
	
	// (09.09.2009 TM)FIXME: clear() etc einbauen
	
	/**
	 * The Class Body.
	 */
	public class Body implements ComponentAffectorHandler
	{
		
		/** The entries by annotation. */
		private final Map<Class<? extends Annotation>, List<Entry<?>>> entriesByAnnotation = new HashMap<>();
		
		/** The entries by component. */
		private final Map<Component, List<Entry<?>>> entriesByComponent = new HashMap<>();
		


		/**
		 * Execute all.
		 * 
		 * @param <C> the generic type
		 * @param entries the entries
		 */
		@SuppressWarnings("unchecked")
		protected <C extends Component> void executeAll(final List<Entry<?>> entries) {
			if(entries == null) return;
			for(final Entry<?> entry : entries) {
				final Entry<C> e = (Entry<C>)entry;
				e.affector.affect(e.component);
			}
		}
		
		/**
		 * @param <C>
		 * @param component
		 * @see net.jadoth.entitydatamapping.ComponentAffectorHandler#executeAffector(java.awt.Component)
		 */
		@Override
		public <C extends Component> void executeAffector(final C component) {
			executeAll(this.entriesByComponent.get(component));
		}
		
		/**
		 * @param <C>
		 * @param annotation
		 * @see net.jadoth.entitydatamapping.ComponentAffectorHandler#executeAffector(java.lang.Class)
		 */
		@Override
		public <C extends Component> void executeAffector(final Class<? extends Annotation> annotation) {
			executeAll(this.entriesByAnnotation.get(annotation));
		}
		
		/**
		 * @param <C>
		 * @param annotation
		 * @param component
		 * @param affector
		 * @see net.jadoth.entitydatamapping.ComponentAffectorHandler#register(java.lang.Class, java.awt.Component, net.jadoth.entitydatamapping.interfaces.ComponentAffector)
		 */
		@Override
		public <C extends Component> void register(final Class<? extends Annotation> annotation, final C component, final ComponentAffector<C> affector) {
			final Entry<C> e = new Entry<>(annotation, component, affector);
			
			List<Entry<?>> byAnnotationList = this.entriesByAnnotation.get(annotation);
			if(byAnnotationList == null){
				byAnnotationList = new LinkedList<>();
				this.entriesByAnnotation.put(annotation, byAnnotationList);
			}
			byAnnotationList.add(e);
			
			List<Entry<?>> byComponentList = this.entriesByComponent.get(component);
			if(byComponentList == null){
				byComponentList = new LinkedList<>();
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
			private final Class<? extends Annotation> annotation;
			
			/** The component. */
			final C component;
			
			/** The affector. */
			ComponentAffector<C> affector;
			
			/**
			 * Instantiates a new entry.
			 * 
			 * @param annotation the annotation
			 * @param component the component
			 * @param affector the affector
			 */
			public Entry(final Class<? extends Annotation> annotation, final C component, final ComponentAffector<C> affector) {
				super();
				this.annotation = annotation;
				this.component = component;
				this.affector = affector;
			}
		}
		
	}
	
}
