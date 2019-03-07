package net.jadoth.util.code;

import net.jadoth.chars.VarString;



public interface Field
{
	public String typeName();

	public String fieldName();

	public void assembleInterfaceGetter(VarString vs, Type type, String getterPrefixBoolean, String getterPrefixNormal);

	public void assembleInterfaceSetter(VarString vs, Type type, String setterPrefix);

	public void assembleConstructorParameter(VarString vs, int typeLength, int nameLength);

	public void assembleClassField(VarString vs, Type type, int typeLength, int nameLength);

	public void assembleConstructorInitialization(VarString vs, Type type, int nameLength);

	public void assembleClassGetter(VarString vs, Type type, String getterPrefixBoolean, String getterPrefixNormal);

	public void assembleClassSetter(VarString vs, Type type, String setterPrefix);



	public class Implementation implements Field
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final String     typeName               ;
		private final String     fieldName              ;
		private final Visibility visibility             ;
		private final FieldType  type                   ;
		private final String     directInitializer      ;
		
		private transient String  upperFieldName;
		private transient boolean hasBooleanType;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Implementation(
			final String     typeName         ,
			final String     fieldName        ,
			final Visibility visibility       ,
			final FieldType  type             ,
			final String     directInitializer
		)
		{
			super();
			this.typeName                = typeName         ;
			this.visibility              = visibility       ;
			this.directInitializer       = directInitializer;
			this.type                    = type             ;
			this.fieldName               = fieldName != null
				? fieldName
				: Code.toLowerCaseFirstLetter(typeName)
			;
			this.upperFieldName = Code.toUpperCaseFirstLetter(this.fieldName);
			this.hasBooleanType = typeName.equals(boolean.class.getSimpleName())
				|| typeName.equals(Boolean.class.getSimpleName())
			;
		}
		
		public boolean hasBooleanType()
		{
			return this.hasBooleanType;
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
		
		protected VarString assembleModifiers(final VarString vs, final Type type, final int typeLength, final int nameLength)
		{
			this.visibility.assemble(vs);
			return vs;
		}
		
		@Override
		public void assembleClassField(final VarString vs, final Type type, final int typeLength, final int nameLength)
		{
			vs.lf().tab().tab();
			this.assembleModifiers(vs, type, typeLength, nameLength)
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
			if(this.directInitializer != null || !this.type.hasCtor())
			{
				return;
			}
			
			vs
			.lf().tab(3).add("final ")
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
			
			vs
			.lf().tab(3).add("this.").padRight(this.fieldName(), nameLength, ' ').add(" = ")
			.padRight(this.fieldName(), nameLength, ' ')
			.add(';')
			;
		}
		
		@Override
		public void assembleInterfaceGetter(
			final VarString vs                 ,
			final Type      type               ,
			final String    getterPrefixBoolean,
			final String    getterPrefixNormal
		)
		{
			vs.lf()
			.tab().add("public ").add(this.typeName()).blank();
			this.assembleGetterName(vs, this.hasBooleanType ? getterPrefixBoolean : getterPrefixNormal).add("()")
			.add(';').lf()
			;
		}
		
		protected VarString assembleGetterName(final VarString vs, final String getterPrefix)
		{
			if(getterPrefix != null)
			{
				vs.add(getterPrefix).add(this.upperFieldName);
			}
			else
			{
				vs.add(this.fieldName());
			}
			return vs;
		}
		
		@Override
		public void assembleClassGetter(
			final VarString vs                 ,
			final Type      type               ,
			final String    getterPrefixBoolean,
			final String    getterPrefixNormal
		)
		{
			vs.lf();
			Code.appendOverride(vs, 2)
			.lf().tab(2)
			.add("public final ").add(this.typeName()).blank();
			this.assembleGetterName(vs, this.hasBooleanType ? getterPrefixBoolean : getterPrefixNormal).add("()")
			.lf().tab(2).add('{')
			.lf().tab(3).add("return this.").add(this.fieldName()).add(';')
			.lf().tab(2).add('}')
			;
		}
		
		protected VarString assembleSetterReturnType(final VarString vs, final Type type)
		{
			if(this.type.hasChainingSetter())
			{
				type.assembleTypeName(vs);
			}
			else
			{
				vs.add("void");
			}
			return vs;
		}
		
		protected VarString assembleSetterName(final VarString vs, final Type type, final String setterPrefix)
		{
			if(setterPrefix != null)
			{
				vs.add(setterPrefix).add(this.upperFieldName);
			}
			else
			{
				vs.add(this.fieldName());
			}
			
			return vs;
		}
		
		protected VarString assembleSetterCommonPart(final VarString vs, final Type type, final String setterPrefix)
		{
			this.assembleSetterReturnType(vs, type).blank();
			this.assembleSetterName(vs, type, setterPrefix).add("(").add(this.typeName()).blank().add(this.fieldName()).add(")");
			return vs;
		}
		
		@Override
		public void assembleInterfaceSetter(final VarString vs, final Type type, final String setterPrefix)
		{
			vs.lf()
			.tab().add("public ");
			this.assembleSetterCommonPart(vs, type, setterPrefix)
			.add(';').lf()
			;
		}

		@Override
		public void assembleClassSetter(final VarString vs, final Type type, final String setterPrefix)
		{
			vs.lf();
			Code.appendOverride(vs, 2)
			.lf().tab(2)
			.add("public final ");
			this.assembleSetterCommonPart(vs, type, setterPrefix)
			.lf().tab(2).add('{')
			.lf().tab(3).add("this.").add(this.fieldName()).add(" = ").add(this.fieldName()).add(';');
			if(this.type.hasChainingSetter())
			{
				vs.lf().tab(3).add("return this").add(';');
			}
			vs.lf().tab(2).add('}')
			;
		}

	}

	public final class FinalField extends Field.Implementation
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		FinalField(
			final String           typeName         ,
			final String           fieldName        ,
			final Visibility       visibility       ,
			final String           directInitializer
		)
		{
			super(typeName, fieldName, visibility, FieldType.FINAL, directInitializer);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		protected VarString assembleModifiers(final VarString vs, final Type type, final int typeLength, final int nameLength)
		{
			super.assembleModifiers(vs, type, typeLength, nameLength);
			vs.add("final ");
			return vs;
		}

		@Override
		public void assembleInterfaceSetter(final VarString vs, final Type type, final String setterPrefix)
		{
			// no-op for final property
		}

		@Override
		public final void assembleClassSetter(final VarString vs, final Type type, final String setterPrefix)
		{
			// no-op for final field
		}

	}

}
