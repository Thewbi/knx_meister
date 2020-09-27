package project.parsing;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DefaultFullTranslationElementParser implements FullTranslationElementParser {

    private static final Logger LOG = LogManager.getLogger(DefaultFullTranslationElementParser.class);

    @Override
    public Map<String, Map<String, Map<String, String>>> parse(final Document document) {

//        LOG.info("parsing");

        // first key: language ISO code
        // second key: refid of communication object M-0169_A-0001-10-098F_P-21
        // third map: key-value of translation object and translated value for that
        // language
        final Map<String, Map<String, Map<String, String>>> languagesMap = new HashMap<>();

        // device instance and COM objects within the device instance
        final NodeList deviceInstanceNodeList = document.getElementsByTagName("Languages");
        final Element languagesElement = (Element) deviceInstanceNodeList.item(0);

        // over all <Languages> elements
        for (int i = 0; i < languagesElement.getChildNodes().getLength(); i++) {

            final Node item = languagesElement.getChildNodes().item(i);
            if (!(item instanceof Element)) {
                continue;
            }
            final Element languageElement = (Element) item;

            // this is the language isoCode such as de-DE
            final String identifierAttribute = languageElement.getAttribute("Identifier");

            final Map<String, Map<String, String>> languageMap = new HashMap<>();
            languagesMap.put(identifierAttribute, languageMap);

            // over all <TranslationUnit> elements
            for (int j = 0; j < languagesElement.getChildNodes().getLength(); j++) {

                final Node tempNode = languageElement.getChildNodes().item(j);
                if (!(tempNode instanceof Element)) {
                    continue;
                }

                final Element translationUnitElement = (Element) tempNode;

                // over all <TranslationElement> elements
                for (int k = 0; k < translationUnitElement.getChildNodes().getLength(); k++) {

                    final Node tempNodeA = translationUnitElement.getChildNodes().item(k);
                    if (!(tempNodeA instanceof Element)) {
                        continue;
                    }

                    final Element translationElement = (Element) tempNodeA;
                    final String refId = translationElement.getAttribute("RefId");

                    if (refId.equalsIgnoreCase("M-00C9_A-1040-11-9162_O-402")) {
                        LOG.info("test");
                    }
//                    else {
//                        continue;
//                    }

//                    final Map<String, Map<String, String>> refIdTranslationMap = new HashMap();
                    final Map<String, String> refIdTranslationMap = new HashMap<>();
                    languageMap.put(refId, refIdTranslationMap);

                    // over all <Translation> elements
                    for (int l = 0; l < translationElement.getChildNodes().getLength(); l++) {

                        final Node tempNodeB = translationElement.getChildNodes().item(l);
                        if (!(tempNodeB instanceof Element)) {
                            continue;
                        }

                        final Element translation = (Element) tempNodeB;
                        refIdTranslationMap.put(translation.getAttribute("AttributeName"),
                                translation.getAttribute("Text"));

                    }

//                    final Element translationElement = (Element) translationElement.getChildNodes().item(1);
//
//                    languageMap.put(refId, translationElement.getAttribute("Text"));
                }
            }
        }

        return languagesMap;
    }

}
