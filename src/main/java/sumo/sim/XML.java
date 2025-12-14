package sumo.sim;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import javax.xml.stream.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.*;

/**
 * XML is used for writing and reading the xml files of the sumo conifg.
 * @author simonr
 */

public class XML {
// XML file read/write class

    private FileInputStream file;
    private static XMLInputFactory factory = null;
    private String path;

    /**
     * The Constructor of XML
     * Simply creates an XMLInputFactory Instance and a FileInputStream with the given Path
     * @param path
     * @throws Exception
     */
    public XML(String path) throws Exception{
        this.path = path;
        file = new FileInputStream(path);
        factory = XMLInputFactory.newInstance();

    }

    /**
     * Sets the Duration of a specific Traffic Light of a Junction
     * This then overwrites the <phase><duration></duration></phase> segment of the .net.xml
     * @param id
     * @param phaseIndex
     * @param newDuration
     */
    public void setPhaseDuration(String id, int phaseIndex, double newDuration){
        try {
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(path);
            Element root = doc.getRootElement();

            for(Element tlLogic : root.getChildren("tlLlogic")) {
                if(!(tlLogic.getAttributeValue("id").equals(id))) {
                    continue;
                }

                List<Element> phases = tlLogic.getChildren("phase");
                phases.get(phaseIndex).setAttribute("duration", String.valueOf(newDuration));
            }

            new XMLOutputter(Format.getPrettyFormat()).output(doc, new FileOutputStream(path));

        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Reads a Streets FromJunction and returns it
     * @param id
     * @return fromJunction
     */
    public String getFromJunction(String id){
        try (FileInputStream file = new FileInputStream(path)){
            XMLStreamReader reader = factory.createXMLStreamReader(file);
            while(reader.hasNext()){
                int event =  reader.next();
                //reads all entries of the xml till the children are edges
                if(event == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("edge")){

                    String edgeID = reader.getAttributeValue(null, "id");
                    //if at the edge we are looking for, return the attribute for "from"
                    if(id.equals(edgeID)){
                        return reader.getAttributeValue(null, "from");
                    }
                }
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * Reads a Streets ToJunction and returns it
     * @param id
     * @return toJunction
     */
    public String getToJunction(String id) {
        try (FileInputStream file = new FileInputStream(path)){
            XMLStreamReader reader = factory.createXMLStreamReader(file);
            while (reader.hasNext()) {
                int event = reader.next();
                //reads all entries of the xml till the children are edges
                if (event == XMLStreamConstants.START_ELEMENT &&
                        reader.getLocalName().equals("edge")) {

                    String edgeId = reader.getAttributeValue(null, "id");
                    //if at the edge we are looking for, return the attribute for "to"
                    if (id.equals(edgeId)) {
                        return reader.getAttributeValue(null, "to");
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * Reads all Streets, and their from and toJucntions. Then returns them as a HashMap
     * @return Map<String, String[]> Map of all Edges from and to Jucntions
     */
    public Map<String, String[]> readAllEdges() {
        Map<String, String[]> map = new HashMap<>();

        try (FileInputStream file = new FileInputStream(path)){
            XMLStreamReader reader = factory.createXMLStreamReader(file);
            while (reader.hasNext()) {
                int event = reader.next();

                if (event == XMLStreamConstants.START_ELEMENT &&
                        reader.getLocalName().equals("edge")) {

                    String id = reader.getAttributeValue(null, "id");
                    String from = reader.getAttributeValue(null, "from");
                    String to = reader.getAttributeValue(null, "to");

                    if (id != null && from != null && to != null) {
                        map.put(id, new String[]{from, to});
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return map;
    }

    /**
     * Reads every Route in the rou.xml and returns them as a Hashmap
     * @return Map<String, List<String> allRoutes
     */
    public Map<String, List<String>> getRoutes(){
        Map<String, List<String>> map = new HashMap<>();
        try(FileInputStream file = new FileInputStream(path)){
            XMLStreamReader reader = factory.createXMLStreamReader(file);
            while (reader.hasNext()) {
                int event = reader.next();

                if (event == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("route")) {
                    String id = reader.getAttributeValue(null, "id");
                    String edges = reader.getAttributeValue(null, "edges");

                    if (id != null && edges != null) {
                        // Split the string by space and convert to List
                        List<String> edgesList = new ArrayList<>(Arrays.asList(edges.split("\\s+")));
                        map.put(id, edgesList);
                    }
                }
            }
            reader.close();

        } catch (Exception e) {
            throw new RuntimeException("Error reading XML file.", e);
        }
        return map;
    }

    /**
     * Reads all Streets Data from .net.xml. Returns the Data as a Hashmap with Index to Attributes.
     * @return Map<String, List<String> AllStreets and their Attributes
     */
    public Map<String, List<String>> getStreetsData(){
        Map<String, List<String>> map = new HashMap<>();
        try(FileInputStream file = new FileInputStream(path)){
            XMLStreamReader reader = factory.createXMLStreamReader(file);
            while (reader.hasNext()) {
                int event = reader.next();

                if (event == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("edge")) {
                    String id = reader.getAttributeValue(null, "id");
                    List<String> attributes = new ArrayList<>();
                    for(int i = 1; i < reader.getAttributeCount(); i++){
                        attributes.add(reader.getAttributeValue(i));
                    }

                    if(id != null && !(attributes.contains("internal"))){
                        map.put(id, attributes);
                    }
                }
            }
            reader.close();

        }catch(Exception e){
            throw new RuntimeException(e);
        }
        return map;
    }
    /**
     * Reads all TrafficLight Data from .net.xml. Returns the Data as a Hashmap with Index to a Hashmap with Phases and their Attributes.
     * @return Map<String, Map<String,String> AllTrafficLights and their Attributes
     */
    public Map<String, Map<String, String>> getTrafficLightsData() {

        Map<String, Map<String, String>> result = new HashMap<>();

        try (FileInputStream file = new FileInputStream(path)) {
            XMLStreamReader reader = factory.createXMLStreamReader(file);

            while (reader.hasNext()) {
                int event = reader.next();

                if (event == XMLStreamConstants.START_ELEMENT &&
                        reader.getLocalName().equals("junction") &&
                        "traffic_light".equals(reader.getAttributeValue(null, "type"))) {

                    String id = reader.getAttributeValue(null, "id");
                    Map<String, String> map = new HashMap<>();

                    // attributes: id,type,x,y,incLanes...
                    for (int i = 0; i < reader.getAttributeCount(); i++) {
                        map.put(reader.getAttributeLocalName(i),
                                reader.getAttributeValue(i));
                    }

                    result.put(id, map);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    /**
     * Creates a new Route in the rou.xml.
     * @param id
     * @param edges
     */
    public void newRoute(String id, List<String> edges) {
        if (edges == null || edges.isEmpty()) {
            throw new IllegalArgumentException("Route needs at least one edge!");
        }

        try {
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(path);
            Element root = doc.getRootElement();

            root.getChildren("route").removeIf(r -> id.equals(r.getAttributeValue("id")));

            Element route = new Element("route");
            route.setAttribute("id", id);
            route.setAttribute("edges", String.join(" ", edges));

            root.addContent(route);

            XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
            out.output(doc, new FileOutputStream(path));

        } catch (Exception e) {
            throw new RuntimeException("Error writing new route to XML.", e);
        }
    }



}
