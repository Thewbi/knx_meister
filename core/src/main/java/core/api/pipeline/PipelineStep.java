package core.api.pipeline;

public interface PipelineStep<S, T> {

	T execute(S source) throws Exception;

}
