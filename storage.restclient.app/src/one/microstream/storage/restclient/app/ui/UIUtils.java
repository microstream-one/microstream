package one.microstream.storage.restclient.app.ui;

import com.vaadin.flow.component.orderedlayout.ThemableLayout;

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

	public static String imagePath(String image)
	{
		return "frontend/images/" + image;
	}
	
	
	private UIUtils()
	{
		throw new Error();
	}
}
