package one.microstream.storage.restclient.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.treegrid.TreeGrid;

import one.microstream.storage.restclient.types.StorageViewElement;

public class StorageViewElementField extends CustomField<StorageViewElement>
{
	private StorageViewElement element;
	
	public StorageViewElementField()
	{
		super();
	}

	@Override
	protected void setPresentationValue(
		final StorageViewElement element
	)
	{
		this.element = element;
		
		this.removeAll();
		
		if(element == null)
		{
			return;
		}
		
		if(element.hasMembers())
		{
			final TreeGrid<StorageViewElement> treeGrid = StorageViewTreeGridBuilder.New().build();
			treeGrid.setDataProvider(StorageViewDataProvider.New(element));
			treeGrid.expand(element);
			this.add(treeGrid);
		}
		else
		{
			final String   value    = element.value();
			final TextArea textArea = new TextArea();
			textArea.setValue(value != null ? value : "");
			textArea.setReadOnly(true);
			textArea.setWidth("100%");
			this.add(textArea);
		}
	}

	@Override
	protected StorageViewElement generateModelValue()
	{
		return this.element;
	}

	private void removeAll()
	{
		final Component[] children = this.getChildren().toArray(Component[]::new);
		if(children.length > 0)
		{
			this.remove(children);
		}
	}
	
}
