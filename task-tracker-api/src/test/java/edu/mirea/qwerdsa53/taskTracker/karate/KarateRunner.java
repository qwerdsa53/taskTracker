package edu.mirea.qwerdsa53.taskTracker.karate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.intuit.karate.Results;
import com.intuit.karate.Runner;

@Tag("karate")
class KarateRunner {

	@Test
	void runKarateFeatures() {
		Results results =
				Runner.path("classpath:karate/features")
						.tags("~@ignore")
						.parallel(1);
		assertEquals(0, results.getFailCount(), results.getErrorMessages());
	}
}
