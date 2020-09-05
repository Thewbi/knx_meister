package project.parsing.knx.steps;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import project.parsing.knx.KNXProjectParsingContext;
import project.parsing.steps.ParsingStep;

/**
 * Reads project
 * <ul>
 * <li />id
 * <li />name
 * <li />group address style
 * </ul>
 */
public class ReadProjectParsingStep implements ParsingStep<KNXProjectParsingContext> {

	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger(ReadProjectParsingStep.class);

	private final ProjectParserConsumer consumer = new ProjectParserConsumer();

	@Override
	public void process(final KNXProjectParsingContext context) throws IOException {
		walkFolder(context);
	}

	private void walkFolder(final KNXProjectParsingContext context) throws IOException {

		consumer.setContext(context);

		// @formatter:off

		// for some reason, the stream has to be closed.
		// If this stream is not closed, then at cleaning up the
		// temporary folder, an exception is thrown because some file is still locked
		//
		// https://stackoverflow.com/questions/19935624/java-nio-file-files-deletepath-path-occasional-failure-to-recursively-delete
		try (final Stream<Path> stream = Files
			.walk(context.getTempDirectory())
			.filter(Files::isRegularFile)
			.filter(p -> p.getFileName().endsWith("project.xml"))) {

			final Optional<Path> projectFileOptional = stream.findFirst();

			if (projectFileOptional.isPresent()) {
				consumer.accept(projectFileOptional.get());
			} else {
				throw new IOException("Cannot find a project.xml in any of the project's folders! Cannot parse project!");
			}
		}

		// @formatter:on
	}

}
