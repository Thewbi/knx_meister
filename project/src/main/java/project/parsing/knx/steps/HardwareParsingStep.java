package project.parsing.knx.steps;

import java.io.IOException;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import project.parsing.domain.KNXDeviceInstance;
import project.parsing.domain.KNXProduct;
import project.parsing.knx.KNXProjectParsingContext;
import project.parsing.steps.ParsingStep;

public class HardwareParsingStep implements ParsingStep<KNXProjectParsingContext> {

	private static final Logger LOG = LogManager.getLogger(HardwareParsingStep.class);

	@Override
	public void process(final KNXProjectParsingContext context) throws IOException {

		for (final KNXDeviceInstance knxDeviceInstance : context.getKnxProject().getDeviceInstances()) {

			// validate manufacturer
			if (!context.getKnxProject().getManufacturerMap().containsKey(knxDeviceInstance.getManufacturerId())) {
				throw new RuntimeException("device manufacturer code is not contained in the manufacturer list!");
			}

			// build path to the manufacturer file
			final Path tempDirectory = context.getTempDirectory();

			// retrieve the Hardware.xml file from the manufacturer folder
			final Path path = tempDirectory.resolve(knxDeviceInstance.getManufacturerId()).resolve("Hardware.xml");

			try {
				final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
				final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
				final Document document = documentBuilder.parse(path.toFile());

				// manufacturers
				final NodeList manufacturerNodeList = document.getElementsByTagName("Manufacturer");
				final Element manufacturerElement = (Element) manufacturerNodeList.item(0);
				final NodeList hardwareNodeList = manufacturerElement.getChildNodes().item(1).getChildNodes();

				for (int i = 0; i < hardwareNodeList.getLength(); i++) {

					final Node item = hardwareNodeList.item(i);
					if (!(item instanceof Element)) {
						continue;
					}

					final Element hardwareElement = (Element) item;

					// hardware
					final KNXProduct knxProduct = new KNXProduct();
					knxProduct.setHardwareId(hardwareElement.getAttribute("Id"));
					knxProduct.setHardwareName(hardwareElement.getAttribute("Name"));
					knxProduct.setHardwareSerialNumber(hardwareElement.getAttribute("SerialNumber"));
					knxProduct.setHardwareVersionNumber(hardwareElement.getAttribute("VersionNumber"));

					// product
					final Node productsNode = hardwareElement.getChildNodes().item(1);
					final Element productElement = (Element) productsNode.getChildNodes().item(1);
					knxProduct.setProductId(productElement.getAttribute("Id"));
					knxProduct.setProductOrderNumber(productElement.getAttribute("OrderNumber"));
					knxProduct.setProductText(productElement.getAttribute("Text"));

					// application program
					final NodeList hardware2ProgramsNodeList = hardwareElement
							.getElementsByTagName("Hardware2Programs");
					final Node hardware2ProgramsNode = hardware2ProgramsNodeList.item(0);
					final Node hardware2ProgramNode = hardware2ProgramsNode.getChildNodes().item(1);
					final Element applicationProgramRef = (Element) hardware2ProgramNode.getChildNodes().item(1);

					knxProduct.setApplicationProgramRef(applicationProgramRef.getAttribute("RefId"));

					for (final KNXDeviceInstance tempKnxDeviceInstance : context.getKnxProject().getDeviceInstances()) {
						if (StringUtils.equalsIgnoreCase(tempKnxDeviceInstance.getProductRefId(),
								knxProduct.getProductId())) {
							tempKnxDeviceInstance.setKnxProduct(knxProduct);
						}
					}
				}
			} catch (final ParserConfigurationException | SAXException e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

}
