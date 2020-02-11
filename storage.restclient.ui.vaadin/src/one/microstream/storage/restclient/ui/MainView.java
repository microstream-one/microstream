
package one.microstream.storage.restclient.ui;

import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.material.Material;


@Route
@Push
@Theme(value = Material.class, variant = Material.DARK)
@JsModule("./styles/shared-styles.js")
public class MainView extends VerticalLayout
{
	
	public MainView()
	{
		super();
		
		
	}
}
