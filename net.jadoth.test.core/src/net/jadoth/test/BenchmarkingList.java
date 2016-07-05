/**
 *
 */
package net.jadoth.test;

/*

List Benchmarking


Addition types
- single element
- array
- collection

Addition locations
- append at end
- append at start
- insert multiple times at a randomly chosen but then fixed index
- multiple inserts at random index

Writing element access
- set first
- set last
- set random

Reading Element access
- get first
- get last
- get random

Removing
- remove first
- remove last
- remove random

Iteration
- iterate (foreach loop)
- iterate (indexed)
- iterate (Processor obejct)
- iterate backwards
- toArray


Instantiation Optimisation:
- default instantiation
- instantiation for known size (if applicable)
- instantiation with minimal size (if applicable)

Reading Optimisation:
- optimise for reading (if applicable)

Searching:
- contains
- indexOf
- lastIndexOf

Cobined Tests:
- add all data, then read all data (iterate & random access)
- add all data, optimise (if applicable), then read all data (iterate & random access)
- instantiate for known size (if applicable), add all data, optimise (if applicable), then read all data (iterate & random access)

Sizes:
- 10
- 100
- 1.000
- 10.000
- 100.000
- 1.000.000
- 10.000.000
- 100.000.000
- 1.000.000.000
- 2.000.000.000
- Integer.MAX_VALUE



Memory usage
- constant overhead per list instance
- overhead per element
- capacity overhead (best case, worst case, average)


Measuring Accuracy
- Measure measuring time and substract from all measured times
- Iterate many times (~100) and build best / worst / average values
- Do not measure first 5 iterations (= do not measure hot spot compile time)
- Reserve enough java heap beforehand (should already be done by the not measured iterations).
- Beware of / eliminate garbage collection (as far as possible)

 */
public class BenchmarkingList
{
	// empty
}
