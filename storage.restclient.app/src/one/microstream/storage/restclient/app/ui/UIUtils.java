package one.microstream.storage.restclient.app.ui;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.ThemableLayout;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;

final class UIUtils
{
	public static <L extends ThemableLayout> L compact(
		final L layout
	)
	{
		layout.setPadding(false);
		layout.setMargin(false);
		layout.setSpacing(true);
		return layout;
	}
	
	public static String addErrorStyle(UI ui)
	{
		final String             clazz    = "error-style";
		final String             styles   = "." + clazz + " { color: #faa; }";
		final StreamRegistration resource = ui.getSession().getResourceRegistry().
			registerResource(new StreamResource("styles.css", () ->
			{
				return new ByteArrayInputStream(styles.getBytes(StandardCharsets.UTF_8));
			}
		));
		ui.getPage().addStyleSheet(
			"base://" + resource.getResourceUri().toString());
		return clazz;
	}
	
	private UIUtils()
	{
		throw new Error();
	}
}
