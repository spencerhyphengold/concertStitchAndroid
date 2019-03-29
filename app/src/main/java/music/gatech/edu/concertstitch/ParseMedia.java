package music.gatech.edu.concertstitch;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static music.gatech.edu.concertstitch.ResourceConstants.ANNOTATION_BASE_URL;
import static music.gatech.edu.concertstitch.ResourceConstants.FPS;
import static music.gatech.edu.concertstitch.ResourceConstants.INSTRUMENT_LABELS;
import static music.gatech.edu.concertstitch.ResourceConstants.MEDIA_SRC_URL;
import static music.gatech.edu.concertstitch.ResourceConstants.SYNC_TIME_URL;
import static music.gatech.edu.concertstitch.ResourceConstants.VIDEO_NAMES;

/**
 * @author mcw0805
 */
public class ParseMedia {

    public static final int ANNOATIONS_LENGTH = 6517;

    public static  Map<String, String> mediaUrlMap = new HashMap<>();
    public static Map<String, double[]> syncMap = new HashMap<>();
    public static Map<String, HashMap<Integer, double[][]>> annotationsMap = new HashMap<>();



    public static Map<String, double[]> getSyncTimes() {

        // Map<String, double[]> syncMap = new HashMap<>();

        URL url = null;
        try {
            url = new URL(SYNC_TIME_URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        URLConnection request;
        try {
            request = url.openConnection();
            request.connect();

            // Convert to a JSON object to print data
            JsonParser jp = new JsonParser(); //from gson
            JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element
            JsonArray jsonArray = root.getAsJsonArray(); //May be an array, may be an object.

            for (JsonElement je : jsonArray) {
                JsonObject jo = je.getAsJsonObject();
                syncMap.put(jo.get("name").getAsString(), new double[]{jo.get("index").getAsDouble(), jo.get("duration").getAsDouble()});

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return syncMap;
    }


    private static Document loadDocument(String urlStr) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        URL url = null;
        Document doc = null;

        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            doc = factory.newDocumentBuilder().parse(url.openStream());
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        return doc;
    }

    public static Map<String, HashMap<Integer, double[][]>> getAnnotations() {

        // Map<String, HashMap<Integer, double[][]>> annotationsMap = new HashMap<>();

        if (syncMap != null) {
            for (String v : VIDEO_NAMES) {
                HashMap<Integer, double[][]> annotationForCurrVid = new HashMap<>();
                final int makeupTime = (int) Math.floor(syncMap.get(v)[0] * FPS);
                final String currVidAnnotationsURL = ANNOTATION_BASE_URL + v + ".xml";

                // annotation xml specific to current video
                Document doc = loadDocument(currVidAnnotationsURL);
                doc.getDocumentElement().normalize();

                // track tag for each instrument
                NodeList trackList = doc.getElementsByTagName("track");

                for (int i = 0; i < trackList.getLength(); i++) {

                    String instrumentLabel = trackList.item(i).getAttributes().getNamedItem("label").getNodeValue();

                    // https://stackoverflow.com/questions/20259742/why-am-i-getting-extra-text-nodes-as-child-nodes-of-root-node
                    // gets all the child nodes, including whitespace
                    NodeList childNodes = trackList.item(i).getChildNodes();

                    for (int j = 0; j < childNodes.getLength(); j++) {

                        // whitespace counts as a child node, so only look at the element node
                        if (childNodes.item(j).getNodeType() == Node.ELEMENT_NODE) { // get box
                            Node box = childNodes.item(j);
                            NamedNodeMap boxAttributes = box.getAttributes();

                            final int frame = Integer.parseInt(boxAttributes.getNamedItem("frame").getNodeValue());
                            final double xtl = Double.parseDouble(boxAttributes.getNamedItem("xtl").getNodeValue());
                            final double ytl = Double.parseDouble(boxAttributes.getNamedItem("ytl").getNodeValue());
                            final double xbr = Double.parseDouble(boxAttributes.getNamedItem("xbr").getNodeValue());
                            final double ybr = Double.parseDouble(boxAttributes.getNamedItem("ybr").getNodeValue());

                            final double width = (xbr - xtl) / 1280.0;
                            final double height = (ybr - ytl) / 720.0;

                            int instrumentIndex = Arrays.binarySearch(INSTRUMENT_LABELS, instrumentLabel);
                            if (annotationForCurrVid.get(frame + makeupTime) != null) {
                                annotationForCurrVid.get(frame + makeupTime)[instrumentIndex][0] = xtl / 1280;
                                annotationForCurrVid.get(frame + makeupTime)[instrumentIndex][1] = ytl / 720;
                                annotationForCurrVid.get(frame + makeupTime)[instrumentIndex][2] = width;
                                annotationForCurrVid.get(frame + makeupTime)[instrumentIndex][3] = height;
                            } else {
                                double[][] tempArr = new double[INSTRUMENT_LABELS.length][4];
                                for (double[] row : tempArr) {
                                    Arrays.fill(row, -1);
                                }

                                annotationForCurrVid.put(frame + makeupTime, tempArr);
                                annotationForCurrVid.get(frame + makeupTime)[instrumentIndex][0] = xtl  / 1280;
                                annotationForCurrVid.get(frame + makeupTime)[instrumentIndex][1] = ytl / 720;
                                annotationForCurrVid.get(frame + makeupTime)[instrumentIndex][2] = width;
                                annotationForCurrVid.get(frame + makeupTime)[instrumentIndex][3] = height;
                            }

                        }
                    } // end inner for

                    annotationsMap.put(v, annotationForCurrVid);

                } // end for


            }
        }

        return annotationsMap;
    }


    public static Map<String, String> getMediaSrc() {
        // Map<String, String> mediaUrlMap = new HashMap<>();

        Document doc = loadDocument(MEDIA_SRC_URL);
        doc.getDocumentElement().normalize();

        NodeList mediaUrlList = doc.getElementsByTagName("url");

        for (int i = 0; i < mediaUrlList.getLength(); i++) {
            Node mediaUrlNode = mediaUrlList.item(i);

            if (mediaUrlNode.getNodeType() == Node.ELEMENT_NODE) {
                String videoName = mediaUrlNode.getAttributes().getNamedItem("name").getNodeValue();
                String urlTextContent = mediaUrlNode.getTextContent();
                mediaUrlMap.put(videoName, urlTextContent);

            }
        }

        return mediaUrlMap;
    }


}
