package project.parsing.knx.steps;

import java.io.IOException;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import api.project.KNXManufacturer;
import project.parsing.knx.KNXProjectParsingContext;
import project.parsing.steps.ParsingStep;

public class ManufacturerParsingStep implements ParsingStep<KNXProjectParsingContext> {

	private static final Logger LOG = LogManager.getLogger(ManufacturerParsingStep.class);

	@Override
	public void process(final KNXProjectParsingContext context) throws IOException {

		final Path tempDirectory = context.getTempDirectory();
		final Path path = tempDirectory.resolve("knx_master.xml");

		try {
			final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			final Document document = documentBuilder.parse(path.toFile());

			processManufacturers(context, document);

			LOG.info("done");

		} catch (final ParserConfigurationException | SAXException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	private void processManufacturers(final KNXProjectParsingContext context, final Document document) {

		final NodeList manufacturersNodeList = document.getElementsByTagName("Manufacturers");
		final Element manufacturersElement = (Element) manufacturersNodeList.item(0);

		for (int i = 0; i < manufacturersElement.getChildNodes().getLength(); i++) {

			final Node manufacturerNode = manufacturersElement.getChildNodes().item(i);

			if (!(manufacturerNode instanceof Element)) {
				continue;
			}

			final Element manufacturerElement = (Element) manufacturerNode;

			final String manufacturerId = manufacturerElement.getAttribute("Id");
			final String manufacturerName = manufacturerElement.getAttribute("Name");

			LOG.trace(manufacturerId + " " + manufacturerName);

			final KNXManufacturer knxManufacturer = new KNXManufacturer();
			knxManufacturer.setId(manufacturerId);
			knxManufacturer.setName(manufacturerName);

			context.getKnxProject().getManufacturerMap().put(knxManufacturer.getId(), knxManufacturer);
		}
	}

}
