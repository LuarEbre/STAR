package sumo.sim;

import javax.xml.stream.*;
import java.io.FileInputStream;
import java.util.Map;
import java.util.*;

public class XML {
// XML file read/write class
    private FileInputStream file;
    //private static SAXBuilder saxBuilder;
    //private static Document document;
    private static XMLInputFactory factory = null;
    private String path;

    public XML(String path) throws Exception{
        this.path = path;
        file = new FileInputStream(path);
        factory = XMLInputFactory.newInstance();

    }

    public String get_from_junction(String id){
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

    public String get_to_junction(String id) {
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

    //Same as get_from_junction and get_to_junction, but instead returns a map of all edges and their from and to junction
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
}
