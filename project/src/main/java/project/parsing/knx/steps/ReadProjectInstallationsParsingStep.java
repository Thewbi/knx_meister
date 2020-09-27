package project.parsing.knx.steps;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import api.exception.ProjectParsingException;
import api.project.KNXComObject;
import api.project.KNXDeviceInstance;
import api.project.KNXGroupAddress;
import api.project.KNXProject;
import common.utils.dom.DOMUtils;
import project.parsing.DefaultFullTranslationElementParser;
import project.parsing.FullTranslationElementParser;
import project.parsing.knx.KNXProjectParsingContext;
import project.parsing.steps.ParsingStep;

/**
 * The file 0.xml contains the elements KNX > Project > Installations >
 * Installation.
 *
 * This step reads
 * <ul>
 * <li />DeviceInstance
 * <li />GroupAddress
 * </ul>
 * XML elements from the Installation element.
 *
 */
public class ReadProjectInstallationsParsingStep implements ParsingStep<KNXProjectParsingContext> {

    private static final int HEX_RADIX = 16;

    private static final Logger LOG = LogManager.getLogger(ReadProjectInstallationsParsingStep.class);

    private Map<String, Map<String, Map<String, Map<String, String>>>> deviceLanguagesMap = new HashMap<>();

    @Override
    public void process(final KNXProjectParsingContext context) throws IOException, ProjectParsingException {

        deviceLanguagesMap = new HashMap<>();

        final Path tempDirectory = context.getTempDirectory();
        final KNXProject knxProject = context.getKnxProject();

        // retrieve the 0.xml file from the project folder
        final Path path = tempDirectory.resolve(knxProject.getId()).resolve("0.xml");

        try {

            final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            final Document document = documentBuilder.parse(path.toFile());

            // device instance and COM objects within the device instance
            final NodeList deviceInstanceNodeList = document.getElementsByTagName("DeviceInstance");
            for (int i = 0; i < deviceInstanceNodeList.getLength(); i++) {

                final Element deviceInstanceElement = (Element) deviceInstanceNodeList.item(i);

                final KNXDeviceInstance deviceInstance = convertKNXDeviceInstance(deviceInstanceElement, knxProject,
                        context);
                knxProject.getDeviceInstances().add(deviceInstance);
            }

            // group addresses
            final NodeList groupRangesNodeList = document.getElementsByTagName("GroupRanges");
            final Element groupRangesElement = (Element) groupRangesNodeList.item(0);

            final KNXGroupAddress knxGroupAddress = new KNXGroupAddress();

            for (int i = 0; i < groupRangesElement.getChildNodes().getLength(); i++) {
                final Node item = groupRangesElement.getChildNodes().item(i);
                if (!(item instanceof Element)) {
                    continue;
                }
                recurseGroupAddresses((Element) item, knxGroupAddress);
            }
            knxGroupAddress.sortChildren();
            knxGroupAddress.assignAddresses();

            // DEBUG
            knxGroupAddress.dump();

            context.setKnxGroupAddress(knxGroupAddress);

        } catch (final ParserConfigurationException | SAXException e) {
            LOG.error(e.getMessage(), e);
            throw new ProjectParsingException(e);
        }
    }

    private void recurseGroupAddresses(final Element element, final KNXGroupAddress knxGroupAddress) {

        // handle a leaf
        if (element.getChildNodes().getLength() == 0) {

            final KNXGroupAddress childKNXGroupAddress = new KNXGroupAddress();
            childKNXGroupAddress.setId(element.getAttribute("Id"));
            childKNXGroupAddress.setName(element.getAttribute("Name"));

            final String address = element.getAttribute("Address");
            final int addressAsInt = StringUtils.isNotBlank(address) ? Integer.parseInt(address) : -1;
            childKNXGroupAddress.setAddress(addressAsInt);
            childKNXGroupAddress.setRangeStart(0);
            childKNXGroupAddress.setDataPointType(element.getAttribute("DatapointType"));

            knxGroupAddress.getKNXGroupAddresses().add(childKNXGroupAddress);
            childKNXGroupAddress.setParentKNXGroupAddress(knxGroupAddress);

            return;
        }

        final KNXGroupAddress childKNXGroupAddress = new KNXGroupAddress();
        childKNXGroupAddress.setId(element.getAttribute("Id"));
        childKNXGroupAddress.setName(element.getAttribute("Name"));
        childKNXGroupAddress.setAddress(0);
        childKNXGroupAddress.setRangeStart(Integer.parseInt(element.getAttribute("RangeStart")));

        knxGroupAddress.getKNXGroupAddresses().add(childKNXGroupAddress);
        childKNXGroupAddress.setParentKNXGroupAddress(knxGroupAddress);

        for (int i = 0; i < element.getChildNodes().getLength(); i++) {

            final Node node = element.getChildNodes().item(i);

            if (!(node instanceof Element)) {
                continue;
            }

            final Element childElement = (Element) node;

            recurseGroupAddresses(childElement, childKNXGroupAddress);
        }
    }

    private KNXDeviceInstance convertKNXDeviceInstance(final Element deviceInstanceElement, final KNXProject knxProject,
            final KNXProjectParsingContext context) throws ParserConfigurationException, SAXException, IOException {

        final KNXDeviceInstance knxDeviceInstance = new KNXDeviceInstance();
        knxDeviceInstance.setId(deviceInstanceElement.getAttribute("Id"));

        parseGroupObjectTreeElement(deviceInstanceElement, knxDeviceInstance);

        final String address = retrieveAddress(deviceInstanceElement, knxProject);
        final List<KNXComObject> comObjects = retrieveCOMObjects(knxDeviceInstance, deviceInstanceElement, knxProject,
                context);

        // product refid contains the manufacturer code which is needed to find the
        // manufacturer sub folder and the application program xml file within that
        // manufacturer sub folder.
        // From the application program xml file, the ComObjectRef names are parsed
        knxDeviceInstance.setProductRefId(deviceInstanceElement.getAttribute("ProductRefId"));
        knxDeviceInstance.setAddress(address);

        for (final KNXComObject knxComObject : comObjects) {

            LOG.info("PUT_B into knxDeviceInstance " + knxDeviceInstance.getAddress() + " " + knxComObject.getId() + " "
                    + knxComObject.getNumber() + " " + knxComObject.getNumber());
            knxDeviceInstance.getComObjects().put(knxComObject.getId(), knxComObject);
        }

        return knxDeviceInstance;
    }

    private void parseGroupObjectTreeElement(final Element deviceInstanceElement,
            final KNXDeviceInstance knxDeviceInstance) {

        // group object tree (optional element)
        final Element groupObjectTreeElement = (Element) deviceInstanceElement.getElementsByTagName("GroupObjectTree")
                .item(0);

        if (groupObjectTreeElement == null) {
            return;
        }

        final String groupObjectInstances = groupObjectTreeElement.getAttribute("GroupObjectInstances");
        final String[] split = groupObjectInstances.split(" ");
        final Set<String> groupObjectInstancesSet = new HashSet<>(Arrays.asList(split));

        knxDeviceInstance.setGroupObjectInstancesSet(groupObjectInstancesSet);
    }

    private List<KNXComObject> retrieveCOMObjects(final KNXDeviceInstance knxDeviceInstance,
            final Element deviceInstanceElement, final KNXProject knxProject, final KNXProjectParsingContext context)
            throws ParserConfigurationException, SAXException, IOException {

        // COM objects
        final List<KNXComObject> comObjectList = new ArrayList<>();

        final NodeList comObjectInstanceRefsNodeList = deviceInstanceElement
                .getElementsByTagName("ComObjectInstanceRefs");
        final Element objectInstanceRefs = (Element) comObjectInstanceRefsNodeList.item(0);
        if (objectInstanceRefs != null) {

            for (int i = 0; i < objectInstanceRefs.getChildNodes().getLength(); i++) {

                final Node item = objectInstanceRefs.getChildNodes().item(i);

                if (item instanceof Element) {

                    final KNXComObject convertCOMObject = convertCOMObject(item, knxProject, context);

                    // if the device's id is part of the GroupObjectTree, the COM object is flagged
                    // as a group object and can be displayed to the user using the flag
                    final Set<String> groupMap = knxDeviceInstance.getGroupObjectInstancesSet();
                    if (SetUtils.emptyIfNull(groupMap).contains(convertCOMObject.getId())) {
                        convertCOMObject.setGroupObject(true);
                    }
                    comObjectList.add(convertCOMObject);
                }
            }
        }

        return comObjectList;
    }

    /**
     * XMLelement [ComObjectInstanceRef] in 0.xml
     *
     * @param node
     * @return
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    private KNXComObject convertCOMObject(final Node node, final KNXProject knxProject,
            final KNXProjectParsingContext context) throws ParserConfigurationException, SAXException, IOException {

        // from file: 0.xml
        final Element element = (Element) node;

        final String refIdAttribute = element.getAttribute("RefId");
        LOG.info(refIdAttribute);
        final String[] refIdAttributeSplit = refIdAttribute.split("_");
        final String numberAsString = refIdAttributeSplit[0].split("-")[1];
        final int number = Integer.parseInt(numberAsString, HEX_RADIX);

        final KNXComObject knxComObject = new KNXComObject();
        knxComObject.setId(refIdAttribute);
        knxComObject.setKnxProject(knxProject);
        knxComObject.setNumber(number);

        // M-0169_A-0001-10-098F_O-41_R-46
        // M-0169 - Device
        // A-0001-10-098F - ???
        // O-41 - ???
        // R-46 - ???
        final String[] split = refIdAttribute.split("_");
        final String deviceName = split[0];

        if (deviceName.equalsIgnoreCase("M-00C9")) {
            LOG.info("test");
        }

        // not all ETS project files write out a 'Text' attribute.
        // Sometimes the text has to be retrieved from the translations stored in the
        // device file
        if (element.hasAttribute("Text")) {

            knxComObject.setText(element.getAttribute("Text"));

        } else {

            // Parse the device specific file for ComObjectS
            //
            // M-0169_A-0001-10-098F_O-41_R-46
            // Split[0] - M-0169 - Device
            // Split[1] - A-0001-10-098F - ???
            // Split[2] - O-41 - ???
            // Split[3] - R-46 - ???
            final String fileName = split[0] + "_" + split[1] + ".xml";
            final Path deviceFilenamePath = context.getTempDirectory().resolve(deviceName).resolve(fileName);

            Map<String, Map<String, Map<String, String>>> languagesMap = null;
            if (deviceLanguagesMap.containsKey(deviceName)) {
                languagesMap = deviceLanguagesMap.get(deviceName);
            } else {
                LOG.info("Parsing for translations: '{}'", deviceFilenamePath);
                languagesMap = parseLanguagesMap(deviceFilenamePath);
                deviceLanguagesMap.put(deviceName, languagesMap);
            }

//            if (deviceName.equals("M-00C9") && refIdAttribute.equalsIgnoreCase("M-00C9_A-1040-11-9162_O-1_R-2101")) {
//                LOG.info("test");
//            }

            if (deviceName.equals("M-00C9") && refIdAttribute.startsWith("M-00C9_A-1040-11-9162_O-402")) {
                LOG.info("test");
            }

            // translation
            final Map<String, Map<String, Map<String, String>>> map = deviceLanguagesMap.get(deviceName);
            final Map<String, Map<String, String>> map2 = map.get("de-DE");

            Map<String, String> map3 = map2.get(refIdAttribute);
            if (map3 == null) {
                map3 = map2.get(split[0] + "_" + split[1] + "_" + split[2]);
            }

            if (map3 != null) {
                final StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(map3.get("Text"));
                if (map3.containsKey("FunctionText")) {
                    stringBuilder.append(" - ").append(map3.get("FunctionText"));
                }
//                stringBuilder.append(" - ").append("Ausgang");

//                knxComObject.setText("From Translations: " + stringBuilder.toString());
                knxComObject.setText(stringBuilder.toString());
            }

//          // translation
//          final String deviceName = split[0];
//          final Map<String, Map<String, Map<String, String>>> map = deviceLanguagesMap.get(deviceName);
//          final Map<String, Map<String, String>> map2 = map.get("de-DE");
//          final Map<String, String> map3 = map2.get(refIdAttribute);
//
//          if (map3 != null) {
//              final StringBuilder stringBuilder = new StringBuilder();
//              stringBuilder.append(map3.get("Text"));
//              if (map3.containsKey("FunctionText")) {
//                  stringBuilder.append(" - ").append(map3.get("FunctionText"));
//              }
//              stringBuilder.append(" - ").append("Receive");
//
//              knxComObject.setText(stringBuilder.toString());
//          }
        }

        // not all ETS project files write out a Links attribute to connect
        // ComObjectInstanceRefs to GroupAddresse!
        // Sometimes there is a nested 'Connectors' element which contains refs to
        // 'Send' and 'Receive' Group addresses
        final Optional<Element> connectorsOptional = DOMUtils.firstChildElementByTagName(element, "Connectors");
        if (element.hasAttribute("Links")) {

            knxComObject.setGroupAddressLink(element.getAttribute("Links"));

        } else if (connectorsOptional.isPresent()) {

            final Element connectorsElement = connectorsOptional.get();
            processSendAndReceiveElements(refIdAttribute, knxComObject, split, connectorsElement);

        }

        return knxComObject;
    }

    private void processSendAndReceiveElements(final String refIdAttribute, final KNXComObject knxComObject,
            final String[] split, final Element connectorsElement) {

        // Send Element
        final Optional<Element> sendOptional = DOMUtils.firstChildElementByTagName(connectorsElement, "Send");
        if (sendOptional.isPresent()) {

            final String attribute = sendOptional.get().getAttribute("GroupAddressRefId");
            final String[] refIdSplit = attribute.split("_");

            // Set the send group address
            knxComObject.setGroupAddressLink(refIdSplit[1]);
        }

        // Receive Element
        final Optional<Element> receiveOptional = DOMUtils.firstChildElementByTagName(connectorsElement, "Receive");
        if (receiveOptional.isPresent()) {
            final String attribute = receiveOptional.get().getAttribute("GroupAddressRefId");

            // Set the send group address
            final String[] refIdSplit = attribute.split("_");
            knxComObject.setGroupAddressLink(refIdSplit[1]);

        }
    }

    private Map<String, Map<String, Map<String, String>>> parseLanguagesMap(final Path deviceFilenamePath)
            throws ParserConfigurationException, SAXException, IOException {

//        try {

        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        final Document document = documentBuilder.parse(deviceFilenamePath.toFile());

//                final DefaultTranslationElementParser parser = new DefaultTranslationElementParser();
//                final Map<String, Map<String, String>> languagesMap = parser.parse(document);

        final FullTranslationElementParser parser = new DefaultFullTranslationElementParser();
        final Map<String, Map<String, Map<String, String>>> languagesMap = parser.parse(document);

        return languagesMap;
//        } catch (final ParserConfigurationException | SAXException | IOException e) {
//            LOG.error(e.getMessage(), e);
//        }
    }

    private String retrieveAddress(final Element deviceInstanceElement, final KNXProject knxProject) {

        final int addressLevels = 3;

        final List<Element> parentElements = new ArrayList<>();
        Element currentElement = deviceInstanceElement;
        for (int i = 0; i < addressLevels; i++) {
            parentElements.add(0, currentElement);
            currentElement = (Element) currentElement.getParentNode();
        }
        final StringBuffer stringBuffer = new StringBuffer();
        for (final Element element : parentElements) {
            if (stringBuffer.length() != 0) {
                stringBuffer.append(".");
            }
            stringBuffer.append(element.getAttribute("Address"));
        }
        return stringBuffer.toString();
    }

}
