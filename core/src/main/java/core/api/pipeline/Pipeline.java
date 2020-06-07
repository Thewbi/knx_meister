package core.api.pipeline;

public interface Pipeline<S, T> {

	T execute(S source) throws Exception;

	void addStep(PipelineStep<S, T> step);

}
