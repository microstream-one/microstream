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

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.springframework.stereotype.Component;

import com.vaadin.flow.i18n.I18NProvider;

@Component
public class ApplicationI18NProvider implements I18NProvider
{
	private final Map<Locale, ResourceBundle> bundles = new HashMap<>();
		
	public ApplicationI18NProvider()
	{
		super();
	}

	@Override
	public List<Locale> getProvidedLocales()
	{
		return Arrays.asList(Locale.ENGLISH);
	}

	@Override
	public String getTranslation(String key, Locale locale, Object... params)
	{
		final ResourceBundle bundle = this.bundles.computeIfAbsent(locale, language ->
			ResourceBundle.getBundle(
				"META-INF/resources/frontend/i18n/i18n", 
				language, 
				ApplicationI18NProvider.class.getClassLoader()
			)
		);
		
		String value;
		try
		{
			value = bundle.getString(key);
		}
		catch(final MissingResourceException e)
		{
			return "!" + locale.getLanguage() + ": " + key;
		}
		
		if(params.length > 0)
		{
			value = MessageFormat.format(value, params);
		}
		
		return value;
	}
}
