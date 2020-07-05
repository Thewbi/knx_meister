package project.parsing.knx;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import api.project.KNXProject;
import project.parsing.ProjectParser;
import project.parsing.steps.ParsingStep;

public class KNXProjectParser implements ProjectParser<KNXProjectParsingContext> {

	private static final Logger LOG = LogManager.getLogger(KNXProjectParser.class);

	private final List<ParsingStep<KNXProjectParsingContext>> parsingSteps = new ArrayList<>();

	@Override
	public KNXProject parse(final File file) throws IOException {

		if (CollectionUtils.isEmpty(parsingSteps)) {
			throw new RuntimeException("There are no parsing steps! The system is illconfigured!");
		}

		final KNXProjectParsingContext context = new KNXProjectParsingContext();
		context.setKnxProjectFile(file);

		for (final ParsingStep<KNXProjectParsingContext> parsingStep : parsingSteps) {
			try {
				parsingStep.process(context);
			} catch (final Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}

		return context.getKnxProject();
	}

	@Override
	public List<ParsingStep<KNXProjectParsingContext>> getParsingSteps() {
		return parsingSteps;
	}

}
