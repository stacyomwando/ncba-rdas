package com.ncba.rdas.client;

import com.ncba.rdas.model.Country;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Calls the CountryInfo SOAP service using Java's built-in HttpClient.
 * No WSDL code generation needed — sends raw SOAP envelopes and parses XML responses.
 * Uses FullCountryInfoAllCountries: ONE call returns ALL countries with ALL fields.
 */
@Component
public class CountryInfoSoapClient {

    private static final Logger log = LoggerFactory.getLogger(CountryInfoSoapClient.class);
    private static final String NS = "http://www.oorsprong.org/websamples.countryinfo";

    @Value("${soap.service.url}")
    private String soapUrl;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public List<Country> fetchAllCountries() {
        log.info("Calling SOAP: FullCountryInfoAllCountries");
        String envelope = """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <FullCountryInfoAllCountries xmlns="http://www.oorsprong.org/websamples.countryinfo"/>
                  </soap:Body>
                </soap:Envelope>
                """;
        try {
            String xml = post(envelope);
            return parseCountries(xml);
        } catch (Exception e) {
            log.error("SOAP call failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<String[]> fetchAllContinents() {
        log.info("Calling SOAP: ListOfContinentsByName");
        String envelope = """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <ListOfContinentsByName xmlns="http://www.oorsprong.org/websamples.countryinfo"/>
                  </soap:Body>
                </soap:Envelope>
                """;
        try {
            String xml = post(envelope);
            return parsePairs(xml, "tContinent", "sCode", "sName");
        } catch (Exception e) {
            log.error("SOAP continents failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<String[]> fetchAllCurrencies() {
        log.info("Calling SOAP: ListOfCurrenciesByName");
        String envelope = """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <ListOfCurrenciesByName xmlns="http://www.oorsprong.org/websamples.countryinfo"/>
                  </soap:Body>
                </soap:Envelope>
                """;
        try {
            String xml = post(envelope);
            return parsePairs(xml, "tCurrency", "sISOCode", "sName");
        } catch (Exception e) {
            log.error("SOAP currencies failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<String[]> fetchAllLanguages() {
        log.info("Calling SOAP: ListOfLanguagesByName");
        String envelope = """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <ListOfLanguagesByName xmlns="http://www.oorsprong.org/websamples.countryinfo"/>
                  </soap:Body>
                </soap:Envelope>
                """;
        try {
            String xml = post(envelope);
            return parsePairs(xml, "tLanguage", "sISOCode", "sName");
        } catch (Exception e) {
            log.error("SOAP languages failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private String post(String body) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(soapUrl))
                .header("Content-Type", "text/xml; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofSeconds(30))
                .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new RuntimeException("SOAP returned HTTP " + res.statusCode());
        }
        return res.body();
    }

    private List<Country> parseCountries(String xml) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes()));
        doc.getDocumentElement().normalize();

        NodeList items = doc.getElementsByTagNameNS(NS, "tCountryInfo");
        List<Country> result = new ArrayList<>();

        for (int i = 0; i < items.getLength(); i++) {
            Element el = (Element) items.item(i);
            Country c = new Country();
            c.setIsoCode(text(el, "sISOCode"));
            c.setName(text(el, "sName"));
            c.setCapitalCity(text(el, "sCapitalCity"));
            c.setPhoneCode(text(el, "sPhoneCode"));
            c.setContinentCode(text(el, "sContinentCode"));
            c.setCurrencyIsoCode(text(el, "sCurrencyISOCode"));
            c.setFlagUrl(text(el, "sCountryFlag"));

            // languages
            List<String> langs = new ArrayList<>();
            NodeList langNodes = el.getElementsByTagNameNS(NS, "tLanguage");
            for (int j = 0; j < langNodes.getLength(); j++) {
                Element lang = (Element) langNodes.item(j);
                String langName = text(lang, "sName");
                if (langName != null && !langName.isBlank()) langs.add(langName);
            }
            c.setLanguages(langs);
            result.add(c);
        }
        log.info("Parsed {} countries from SOAP", result.size());
        return result;
    }

    private List<String[]> parsePairs(String xml, String tag, String key1, String key2) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes()));
        NodeList items = doc.getElementsByTagNameNS(NS, tag);
        List<String[]> result = new ArrayList<>();
        for (int i = 0; i < items.getLength(); i++) {
            Element el = (Element) items.item(i);
            result.add(new String[]{ text(el, key1), text(el, key2) });
        }
        return result;
    }

    private String text(Element el, String tag) {
        NodeList nl = el.getElementsByTagNameNS(NS, tag);
        if (nl.getLength() == 0) {
            nl = el.getElementsByTagName(tag); // fallback without namespace
        }
        return nl.getLength() > 0 ? nl.item(0).getTextContent() : "";
    }
}
