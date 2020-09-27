package project.parsing;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DefaultTranslationElementParser implements TranslationElementParser {

    private static final Logger LOG = LogManager.getLogger(DefaultTranslationElementParser.class);

    @Override
    public Map<String, Map<String, String>> parse(final Document document) {

        LOG.info("parsing");

        final Map<String, Map<String, String>> languagesMap = new HashMap<String, Map<String, String>>();

        // device instance and COM objects within the device instance
        final NodeList deviceInstanceNodeList = document.getElementsByTagName("Languages");
        final Element languagesElement = (Element) deviceInstanceNodeList.item(0);
        for (int i = 0; i < languagesElement.getChildNodes().getLength(); i++) {

            final Node item = languagesElement.getChildNodes().item(i);

            if (!(item instanceof Element)) {
                continue;
            }

            final Element languageElement = (Element) item;

            // this is the language isoCode such as de-DE
            final String identifierAttribute = languageElement.getAttribute("Identifier");

            final Map<String, String> languageMap = new HashMap<>();
//            context.getKnxProject().getLanguageStoreMap().put(identifierAttribute, languageMap);
            languagesMap.put(identifierAttribute, languageMap);

            for (int j = 0; j < languagesElement.getChildNodes().getLength(); j++) {

                final Node tempNode = languageElement.getChildNodes().item(j);
                if (!(tempNode instanceof Element)) {
                    continue;
                }

                final Element translationUnitElement = (Element) tempNode;

                for (int k = 0; k < translationUnitElement.getChildNodes().getLength(); k++) {

                    final Node tempNodeA = translationUnitElement.getChildNodes().item(k);
                    if (!(tempNodeA instanceof Element)) {
                        continue;
                    }

                    final Element translationElementElement = (Element) tempNodeA;
                    final Element translationElement = (Element) translationElementElement.getChildNodes().item(1);

                    languageMap.put(translationElementElement.getAttribute("RefId"),
                            translationElement.getAttribute("Text"));
                }
            }
        }

        return languagesMap;
    }

}
