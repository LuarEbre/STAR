package sumo.sim;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class XML {
// XML file read/write class
    private static File file;
    private static SAXBuilder saxBuilder;
    private static Document document;

    public XML(String path) throws Exception{
        file = new File(path);

        saxBuilder = new SAXBuilder();
        document = saxBuilder.build(file);

    }

    /*
    public static void insert_edge(String[] args) throws Exception {

        // Root holen (sollte <net> sein)
        Element root = document.getRootElement();Sonnenschein Verteilung

        // Neuen Edge erstellen
        Element edge = new Element("edge");
        edge.setAttribute("kleineKatze", ":J7_3");
        edge.setAttribute("GroßeKatze", "internal");

        // Die drei Lanes hinzufügen
        Element lane0 = new Element("lane");
        lane0.setAttribute("id", ":J7_3_0");
        lane0.setAttribute("index", "0");
        lane0.setAttribute("speed", "9.20");
        lane0.setAttribute("length", "3.88");
        lane0.setAttribute("shape", "70.01,6.04 69.30,9.86");
        edge.addContent(lane0);

        Element lane1 = new Element("lane");
        lane1.setAttribute("id", ":J7_3_1");
        lane1.setAttribute("index", "1");
        lane1.setAttribute("speed", "7.93");
        lane1.setAttribute("length", "3.85");
        lane1.setAttribute("shape", "66.82,5.91 66.11,9.69");
        edge.addContent(lane1);

        Element lane2 = new Element("lane");
        lane2.setAttribute("id", ":J7_3_2");
        lane2.setAttribute("index", "2");
        lane2.setAttribute("speed", "6.42");
        lane2.setAttribute("length", "3.99");
        lane2.setAttribute("shape", "63.62,5.77 63.16,8.22 62.32,9.46");
        edge.addContent(lane2);

        List<Element> children = root.getChildren();
        int insert_index = 0;

        for(int i = 0; i < children.size(); i++){
            if(children.get(i).getName().equals("edge")) {
                insert_index = i;
            }
        }
        children.add(insert_index, edge);

        // Datei speichern
        XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
        xmlOutputter.output(document, System.out);            // Für Debug
        xmlOutputter.output(document, new java.io.FileWriter(file));
    }
    */

    public static void new_route(String id, String[] edges ) throws Exception{

        Element root = document.getRootElement();

        Element route = new Element("route");
        route.setAttribute("id", id);

        String route_string = "";
        for(int i = 0; i < edges.length; i++){
            route_string = route_string.concat(edges[i]);
        }

        route.setAttribute("edges",  route_string);

        List<Element> children = root.getChildren();
        int insert_index = 0;

        for(int i = 0; i < children.size(); i++){
            if(children.get(i).getName().equals("route")) {
                insert_index = i;
            }
        }

        children.add(insert_index, route);

        //XML Speichern
        XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
        xmlOutputter.output(document, System.out);            // Für Debug
        xmlOutputter.output(document, new java.io.FileWriter(file));
    }

    public static String[][] get_vehicles(){

        Element root = document.getRootElement();
        String[][] vehicles = null;

        List<Element> children = root.getChildren("vehicle");
        vehicles = new String[children.size()][3];

        for(int i = 0; i < children.size(); i++){
            Element v = (Element) children.get(i);
            vehicles[i][0] = v.getAttributeValue("id");
            vehicles[i][1] = v.getAttributeValue("depart");
            vehicles[i][2] = v.getAttributeValue("route");
        }


        return vehicles;
    }

    public String get_from_junction(String id){
        Element root = document.getRootElement();
        List<Element> children = root.getChildren("edge");

        for(Element i : children){
            if(id.equals(i.getAttributeValue("id"))){
                return i.getAttributeValue("from");
            }
        }
        return null;
    }

    public String get_to_junction(String id){
        Element root = document.getRootElement();
        String to = null;

        List<Element> children = root.getChildren("edge");

        for(Element i : children){
            if(id.equals(i.getAttributeValue("id"))){
                return i.getAttributeValue("to");
            }
        }
        return null;
    }

}
