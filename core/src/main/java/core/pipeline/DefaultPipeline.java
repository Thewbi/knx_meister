package core.pipeline;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import core.api.pipeline.Pipeline;
import core.api.pipeline.PipelineStep;

public class DefaultPipeline implements Pipeline<Object, Object> {

	private final List<PipelineStep<Object, Object>> steps = new ArrayList<>();

	@Override
	public Object execute(final Object source) throws Exception {

		if (CollectionUtils.isEmpty(steps)) {
			return null;
		}

		Object tempResult = source;
		for (final PipelineStep<Object, Object> step : steps) {
			tempResult = step.execute(tempResult);
		}

		return tempResult;
	}

	public List<PipelineStep<Object, Object>> getSteps() {
		return steps;
	}

	@Override
	public void addStep(final PipelineStep<Object, Object> step) {
		getSteps().add(step);
	}

}
