package grad.proj.matching;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MeanSquareErrorMatcherTest {

	@Test
	public void testSimpleMatchingTop2(){
		List<List<Double>> vecs = new ArrayList<List<Double>>();
		vecs.add(Arrays.asList(0.1, 0.2));
		vecs.add(Arrays.asList(0.3, 0.6));
		vecs.add(Arrays.asList(0.2, 0.2));
		
		Matcher<List<Double>> matcher = new MeanSquareErrorMatcher();
		List<Integer> resultIndicies = matcher.match(Arrays.asList(0.1, 0.1), vecs, 2);
		assertEquals(0, (int) resultIndicies.get(0));
		assertEquals(2, (int) resultIndicies.get(1));	
	}
	
	@Test
	public void testAnotherSimpleMatchingTop2(){
		List<List<Double>> vecs = new ArrayList<List<Double>>();
		vecs.add(Arrays.asList(0.1, 0.2));
		vecs.add(Arrays.asList(0.3, 0.6));
		vecs.add(Arrays.asList(0.2, 0.2));
		
		Matcher<List<Double>> matcher = new MeanSquareErrorMatcher();
		List<Integer> resultIndicies = matcher.match(Arrays.asList(0.5, 0.6), vecs, 2);
		assertEquals(1, (int) resultIndicies.get(0));
		assertEquals(2, (int) resultIndicies.get(1));	
	}
}
