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

import project.parsing.domain.KNXComObject;
import project.parsing.domain.KNXDeviceInstance;
import project.parsing.knx.KNXProjectParsingContext;
import project.parsing.steps.ParsingStep;

public class ApplicationProgramParsingStep implements ParsingStep<KNXProjectParsingContext> {

	private static final Logger LOG = LogManager.getLogger(ApplicationProgramParsingStep.class);

	@Override
	public void process(final KNXProjectParsingContext context) throws IOException {

		for (final KNXDeviceInstance knxDeviceInstance : context.getKnxProject().getDeviceInstances()) {

			if (knxDeviceInstance.getKnxProduct() == null) {
				continue;
			}

			// build path to the application program file
			final Path tempDirectory = context.getTempDirectory();

			// retrieve the application program xml file from the manufacturer folder
			final Path path = tempDirectory.resolve(knxDeviceInstance.getManufacturerId())
					.resolve(knxDeviceInstance.getKnxProduct().getApplicationProgramRef() + ".xml");

			try {
				final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
				final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
				final Document document = documentBuilder.parse(path.toFile());

				final NodeList manufacturerNodeList = document.getElementsByTagName("ComObjectRefs");
				final Node comObjectRefsNode = manufacturerNodeList.item(0);
				final NodeList comObjectRefNodeList = comObjectRefsNode.getChildNodes();

				for (int i = 0; i < comObjectRefNodeList.getLength(); i++) {

					final Node item = comObjectRefNodeList.item(i);
					if (!(item instanceof Element)) {
						continue;
					}

					final Element comObjectRefElement = (Element) item;

					final String id = comObjectRefElement.getAttribute("Id");
					final String[] idSplit = id.split("_");
					final String comObjectNumberSplit = idSplit[2];
					final String[] comObjectNumberSplitSplit = comObjectNumberSplit.split("-");
					final int comObjectNumber = Integer.parseInt(comObjectNumberSplitSplit[1]);

					final String name = comObjectRefElement.getAttribute("Name");
					final String text = comObjectRefElement.getAttribute("Text");

					final String comObjectId = idSplit[2] + "_" + idSplit[3];

					final boolean isGroupObject = knxDeviceInstance.getGroupObjectInstancesSet().contains(comObjectId);

					if (knxDeviceInstance.getComObjects().containsKey(comObjectId)) {

						final KNXComObject knxComObject = knxDeviceInstance.getComObjects().get(comObjectId);

						knxComObject.setHardwareName(knxComObject.getHardwareName() + "," + name);
						knxComObject.setHardwareText(knxComObject.getHardwareText() + "," + text);

					} else {

						final KNXComObject knxComObject = new KNXComObject();
						knxComObject.setId(comObjectId);
						knxComObject.setNumber(comObjectNumber);
						knxComObject.setHardwareName(name);
						knxComObject.setHardwareText(text);
						knxComObject.setGroupObject(isGroupObject);

						knxDeviceInstance.getComObjects().put(knxComObject.getId(), knxComObject);
					}
				}

			} catch (final ParserConfigurationException | SAXException e) {
				LOG.error(e.getMessage(), e);
			}
		}

	}

}
