package project.parsing.knx.steps;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import project.parsing.knx.KNXProjectParsingContext;
import project.parsing.steps.ParsingStep;

public class DeleteTempFolderParsingStep implements ParsingStep<KNXProjectParsingContext> {

	private static final Logger LOG = LogManager.getLogger(DeleteTempFolderParsingStep.class);

	@Override
	public void process(final KNXProjectParsingContext context) throws IOException {

		if (context.getTempDirectory() == null) {
			return;
		}

		LOG.info("Deleting path = " + context.getTempDirectory());

		FileUtils.deleteDirectory(context.getTempDirectory().toFile());
	}
}
