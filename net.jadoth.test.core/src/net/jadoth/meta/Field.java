package net.jadoth.meta;

import static net.jadoth.Jadoth.notNull;
import net.jadoth.util.chars.VarString;



public interface Field
{
	public String typeName();

	public String fieldName();

	public void assembleInterfaceGetter(VarString vs, Type type);

	public void assembleInterfaceSetter(VarString vs, Type type);

	public void assembleConstructorParameter(VarString vs, int typeLength, int nameLength);

	public void assembleClassField(VarString vs, Type type, int typeLength, int nameLength);

	public void assembleConstructorInitialization(VarString vs, Type type, int nameLength);

	public void assembleClassGetter(VarString vs, Type type);

	public void assembleClassSetter(VarString vs, Type type);



	public abstract class AbstractImplementation implements Field
	{
		private final String typeName, fieldName;

		public AbstractImplementation(final String typeName, final String fieldName)
		{
			super();
			this.typeName  = typeName;
			this.fieldName = fieldName != null ? fieldName : Code.toLowerCaseFirstLetter(typeName);
		}

		@Override
		public final String typeName()
		{
			return this.typeName;
		}

		@Override
		public final String fieldName()
		{
			return this.fieldName;
		}

	}

	public final class FinalProperty extends AbstractImplementation
	{
		private final Visibility       visibility       ;
		private final String           directInitializer;
		private final SettingValidator settingValidator ;

		FinalProperty(
			final String           typeName         ,
			final String           fieldName        ,
			final Visibility       visibility       ,
			final String           directInitializer,
			final SettingValidator settingValidator
		)
		{
			super(typeName, fieldName);
			this.visibility        = notNull(visibility);
			this.directInitializer = directInitializer  ;
			this.settingValidator  = settingValidator   ;
		}

		@Override
		public final void assembleInterfaceGetter(final VarString vs, final Type type)
		{
			vs.lf(2).tab().add("public ").add(this.typeName()).blank().add(this.fieldName()).add("()").add(';');
		}

		@Override
		public void assembleInterfaceSetter(final VarString vs, final Type type)
		{
			// no-op for final property
		}

		@Override
		public final void assembleClassField(final VarString vs, final Type type, final int typeLength, final int nameLength)
		{
			vs.lf().tab().tab();
			this.visibility.assemble(vs)
			.add("final ")
			.padRight(this.typeName(), typeLength, ' ').blank()
			.padRight(this.fieldName(), nameLength, ' ');
			if(this.directInitializer != null)
			{
				vs.add(" = ").add(this.directInitializer);
			}
			vs.add(';');
		}

		@Override
		public final void assembleConstructorParameter(final VarString vs, final int typeLength, final int nameLength)
		{
			if(this.directInitializer != null)
			{
				return;
			}
			vs.lf().tab(3).add("final ")
			.padRight(this.typeName(), typeLength, ' ').blank()
			.padRight(this.fieldName(), nameLength, ' ').add(',')
			;
		}

		@Override
		public final void assembleConstructorInitialization(final VarString vs, final Type type, final int nameLength)
		{
			if(this.directInitializer != null)
			{
				return;
			}
			vs.lf().tab(3).add("this.").padRight(this.fieldName(), nameLength, ' ').add(" = ");
			if(this.settingValidator != null)
			{
				this.settingValidator.assemble(vs, this.fieldName(), nameLength);
			}
			else
			{
				vs.padRight(this.fieldName(), nameLength, ' ');
			}
			vs.add(';');
		}

		@Override
		public final void assembleClassGetter(final VarString vs, final Type type)
		{
			vs.lf();
			Code.appendOverride(vs, 2)
			.lf().tab(2)
			.add("public final ").add(this.typeName()).blank().add(this.fieldName()).add("()")
			.lf().tab(2).add('{')
			.lf().tab(3).add("return this.").add(this.fieldName()).add(';')
			.lf().tab(2).add('}')
			;
		}

		@Override
		public final void assembleClassSetter(final VarString vs, final Type type)
		{
			// no-op for final field
		}

	}

}
