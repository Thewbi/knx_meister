package project.parsing.knx.steps;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import project.parsing.knx.KNXProjectParsingContext;
import project.parsing.steps.ParsingStep;

public class GroupAddressParsingStep implements ParsingStep<KNXProjectParsingContext> {

	private static final Logger LOG = LogManager.getLogger(GroupAddressParsingStep.class);

	@Override
	public void process(final KNXProjectParsingContext context) throws IOException {

	}

}
