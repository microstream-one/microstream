package net.jadoth.swizzling.types;

import static net.jadoth.X.notNull;

import net.jadoth.chars.ObjectStringConverter;
import net.jadoth.chars.VarString;
import net.jadoth.chars._charArrayRange;
import net.jadoth.collections.HashTable;
import net.jadoth.collections.types.XImmutableMap;

public interface SwizzleIdStrategyStringConverter extends ObjectStringConverter<SwizzleIdStrategy>
{
	@Override
	public VarString assemble(VarString vs, SwizzleIdStrategy subject);
	
	@Override
	public default VarString provideAssemblyBuffer()
	{
		return ObjectStringConverter.super.provideAssemblyBuffer();
	}
	
	@Override
	public default String assemble(final SwizzleIdStrategy subject)
	{
		return ObjectStringConverter.super.assemble(subject);
	}
	
	@Override
	public SwizzleIdStrategy parse(_charArrayRange input);

	@Override
	public default SwizzleIdStrategy parse(final String input)
	{
		return ObjectStringConverter.super.parse(input);
	}
	
	
	
	public static SwizzleIdStrategyStringConverter.Creator Creator()
	{
		return new SwizzleIdStrategyStringConverter.Creator.Implementation();
	}
	
	public static interface Creator
	{
		public <S extends SwizzleObjectIdStrategy> Creator register(
			Class<S>                             objectIdStrategyType,
			SwizzleObjectIdStrategy.Assembler<S> assembler
		);
		
		public <S extends SwizzleTypeIdStrategy> Creator register(
			Class<S>                           typeIdStrategyType,
			SwizzleTypeIdStrategy.Assembler<S> assembler
		);
		
		public SwizzleIdStrategyStringConverter create();
		
		
		
		public final class Implementation implements SwizzleIdStrategyStringConverter.Creator
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final HashTable<Class<?>, SwizzleObjectIdStrategy.Assembler<?>> oidAssemblers = HashTable.New();
			private final HashTable<Class<?>, SwizzleTypeIdStrategy.Assembler<?>>   tidAssemblers = HashTable.New();
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			Implementation()
			{
				super();
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			@Override
			public synchronized <S extends SwizzleObjectIdStrategy> Creator.Implementation register(
				final Class<S>                             objectIdStrategyType,
				final SwizzleObjectIdStrategy.Assembler<S> assembler
			)
			{
				this.oidAssemblers.put(
					notNull(objectIdStrategyType),
					notNull(assembler)
				);

				return this;
			}
			
			@Override
			public synchronized <S extends SwizzleTypeIdStrategy>  Creator.Implementation register(
				final Class<S>                           typeIdStrategyType,
				final SwizzleTypeIdStrategy.Assembler<S> assembler
			)
			{
				this.tidAssemblers.put(
					notNull(typeIdStrategyType),
					notNull(assembler)
				);

				return this;
			}
			
			@Override
			public final synchronized SwizzleIdStrategyStringConverter create()
			{
				return new SwizzleIdStrategyStringConverter.Implementation(
					this.oidAssemblers.immure(),
					this.tidAssemblers.immure()
				);
			}
			
		}
		
	}
	
	public static SwizzleIdStrategyStringConverter New()
	{
		// generics magic! 8-)
		return Creator()
			.register(SwizzleObjectIdStrategy.Transient.class, SwizzleObjectIdStrategy.Transient::assemble)
			.register(SwizzleTypeIdStrategy.Transient.class  , SwizzleTypeIdStrategy.Transient::assemble  )
			.register(SwizzleTypeIdStrategy.None.class       , SwizzleTypeIdStrategy.None::assemble       )
			.create()
		;
	}
	
	public final class Implementation implements SwizzleIdStrategyStringConverter
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////
		
		public static String labelType()
		{
			return "Type";
		}
		
		public static String labelObject()
		{
			return "Object";
		}
		
		public static char typeAssigner()
		{
			return ':';
		}
		
		public static char separator()
		{
			return ',';
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final XImmutableMap<Class<?>, SwizzleObjectIdStrategy.Assembler<?>> objectIdAssemblers;
		final XImmutableMap<Class<?>, SwizzleTypeIdStrategy.Assembler<?>  > typeIdAssemblers  ;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Implementation(
			final XImmutableMap<Class<?>, SwizzleObjectIdStrategy.Assembler<?>> objectIdAssemblers,
			final XImmutableMap<Class<?>, SwizzleTypeIdStrategy.Assembler<?>  > typeIdAssemblers
		)
		{
			super();
			this.objectIdAssemblers = objectIdAssemblers;
			this.typeIdAssemblers   = typeIdAssemblers  ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		private <S extends SwizzleObjectIdStrategy> SwizzleObjectIdStrategy.Assembler<S> lookupObjectIdStrategyAssembler(
			final Class<?> type
		)
		{
			@SuppressWarnings("unchecked") // cast safety is guaranteed by the registration logic
			final SwizzleObjectIdStrategy.Assembler<S> assembler =
				(SwizzleObjectIdStrategy.Assembler<S>)this.objectIdAssemblers.get(type)
			;
			
			return assembler;
		}
		
		private <S extends SwizzleTypeIdStrategy> SwizzleTypeIdStrategy.Assembler<S> lookupTypeIdStrategyAssembler(
			final Class<?> type
		)
		{
			@SuppressWarnings("unchecked") // cast safety is guaranteed by the registration logic
			final SwizzleTypeIdStrategy.Assembler<S> assembler =
				(SwizzleTypeIdStrategy.Assembler<S>)this.typeIdAssemblers.get(type)
			;
			
			return assembler;
		}
		
		private <S extends SwizzleObjectIdStrategy> void assembleObjectIdStrategy(
			final VarString vs        ,
			final S         idStrategy
		)
		{
			final SwizzleObjectIdStrategy.Assembler<S> assembler = this.lookupObjectIdStrategyAssembler(idStrategy.getClass());
			assembler.assembleIdStrategy(vs, idStrategy);
		}
		
		private <S extends SwizzleTypeIdStrategy> void assembleTypeIdStrategy(
			final VarString vs        ,
			final S         idStrategy
		)
		{
			final SwizzleTypeIdStrategy.Assembler<S> assembler = this.lookupTypeIdStrategyAssembler(idStrategy.getClass());
			assembler.assembleIdStrategy(vs, idStrategy);
		}
		
		@Override
		public VarString assemble(final VarString vs, final SwizzleIdStrategy idStrategy)
		{
			vs
			.add(labelType()).add(typeAssigner()).blank().apply(v ->
				this.assembleTypeIdStrategy(v, idStrategy.typeIdStragegy())
			).add(separator())
			.add(labelObject()).add(typeAssigner()).blank().apply(v ->
				this.assembleObjectIdStrategy(v, idStrategy.objectIdStragegy())
			);
			
			return vs;
		}

		@Override
		public SwizzleIdStrategy parse(final _charArrayRange input)
		{
			// FIXME SwizzleIdStrategyStringConverter#parse()
			throw new net.jadoth.meta.NotImplementedYetError();
		}
		
	}
		
}
