package project.parsing.knx.steps;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import project.parsing.knx.KNXProjectParsingContext;
import project.parsing.steps.ParsingStep;

public class ExtractArchiveParsingStep implements ParsingStep<KNXProjectParsingContext> {

	private static final int BUFFER_SIZE = 4096;

	private static final Logger LOG = LogManager.getLogger(ExtractArchiveParsingStep.class);

	@Override
	public void process(final KNXProjectParsingContext context) throws IOException {

		final Path tempDirectory = Files.createTempDirectory(null);
		LOG.info("pathToTempDirectory = " + tempDirectory);
		context.setTempDirectory(tempDirectory);

		try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(context.getKnxProjectFile()))) {

			ZipEntry zipEntry = zipInputStream.getNextEntry();

			// iterates over entries in the zip file
			while (zipEntry != null) {

				final String filePathAsString = tempDirectory + File.separator + zipEntry.getName();
				final File filePath = new File(filePathAsString);
				filePath.getParentFile().mkdirs();

				if (zipEntry.isDirectory()) {

					// if the entry is a directory, make the directory
					final File dir = new File(filePathAsString);
					dir.mkdir();

				} else {

					// if the entry is a file, extracts it
					extractFile(zipInputStream, filePathAsString);

				}

				zipInputStream.closeEntry();
				zipEntry = zipInputStream.getNextEntry();
			}
		}
	}

	private void extractFile(final ZipInputStream zipInputStream, final String filePath)
			throws FileNotFoundException, IOException {

		try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(filePath))) {

			final byte[] bytesIn = new byte[BUFFER_SIZE];
			int read = 0;

			while ((read = zipInputStream.read(bytesIn)) != -1) {
				bufferedOutputStream.write(bytesIn, 0, read);
			}
		}
	}

}
