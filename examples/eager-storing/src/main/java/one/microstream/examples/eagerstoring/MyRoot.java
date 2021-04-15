package one.microstream.examples.eagerstoring;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MyRoot
{
	@StoreEager
	final List<Integer> numbers = new ArrayList<>();
	
	final List<LocalDateTime> dates = new ArrayList<>();
}
