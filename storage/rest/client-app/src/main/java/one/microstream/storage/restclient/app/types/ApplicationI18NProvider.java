package one.microstream.storage.restclient.app.types;

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
