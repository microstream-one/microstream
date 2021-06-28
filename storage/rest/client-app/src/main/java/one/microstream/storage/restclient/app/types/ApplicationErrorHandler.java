package one.microstream.storage.restclient.app.types;

/*-
 * #%L
 * microstream-storage-restclient-app
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
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

import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.DefaultErrorHandler;
import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.ErrorHandler;
import com.vaadin.flow.server.VaadinSession;

import one.microstream.storage.restclient.app.ui.InternalErrorView;


public class ApplicationErrorHandler implements ErrorHandler
{
	public static final String THROWABLE_ATTRIBUTE = ApplicationErrorHandler.class.getName() + "#THROWABLE";
	
	public static void handle(Throwable throwable)
	{
		LoggerFactory.getLogger(ApplicationErrorHandler.class)
			.error(throwable.getMessage(), throwable);
		
		VaadinSession.getCurrent().setAttribute(THROWABLE_ATTRIBUTE, throwable);
		UI.getCurrent().navigate(InternalErrorView.class);
	}
	
	
	public ApplicationErrorHandler()
	{
		super();
	}
	
	@Override
	public void error(ErrorEvent event)
	{
		handle(DefaultErrorHandler.findRelevantThrowable(event.getThrowable()));
	}
	
}
