package ru.netology;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
// ЗАДАНИЕ 1: CSV - JSON парсер:
        // Исходная информация
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        String fileName = "data.csv";

        // Получаем список сотрудников из CSV файла
        List<Employee> listCSV = parseCSV(columnMapping, fileName);
        // Полученный список преобразуем в строчку в формате JSON
        String json = listToJson(listCSV);
        // Запишем полученный JSON в файл
        writeString(json, "data.json");

// ЗАДАНИЕ 2: XML - JSON парсер:
        // Получаем список сотрудников из XML файла
        List<Employee> list2 = parseXML("data.xml");
        // Полученный список преобразуем в строчку в формате JSON
        String json2 = listToJson(list2);
        // Запишем полученный JSON в файл
        writeString(json2, "data2.json");

// ЗАДАНИЕ 3: JSON парсер:
        // Получаем JSON из файла
        String json3 = readString("data2.json");
        // JSON преобразовываем в список сотрудников
        List<Employee> list3 = jsonToList(json);
        list3.forEach(System.out::println);

    }

    private static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        List<Employee> listEmp = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);
            // Работаем с прочитанными данными.
            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(reader)
                    .withMappingStrategy(strategy)
                    .build();
            listEmp = csv.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listEmp;
    }

    private static String listToJson(List<Employee> list) {
        Type listType = new TypeToken<List<Employee>>() {
        }.getType();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().create();
        return gson.toJson(list, listType);
    }

    private static void writeString(String json, String pathFile) {
        try (FileWriter file = new
                FileWriter(pathFile)) {
            file.write(json);
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<Employee> parseXML(String fileName) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        List<Employee> listEmpXML = new ArrayList<>();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(fileName));
            Node root = doc.getDocumentElement();
            listEmpXML = read(root, listEmpXML);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return listEmpXML;
    }

    private static List<Employee> read(Node node, List<Employee> listEmpXML) {
        NodeList nodeList = node.getChildNodes();
        long id = 0;
        String firstName = "";
        String lastName = "";
        String country = "";
        int age = 0;
        boolean flagNewEmployee = false;

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node_ = nodeList.item(i);
            if (Node.ELEMENT_NODE == node_.getNodeType()) {
                Element element = (Element) node_;
                NodeList listTextNode = element.getChildNodes();
                for (int a = 0; a < listTextNode.getLength(); a++) {
                    Node item = listTextNode.item(a);
                    String attrValue = listTextNode.item(a).getNodeValue();
                    String parentNodeName = item.getParentNode().getNodeName();
                    switch (parentNodeName) {
                        case "id":
                            flagNewEmployee = true;
                            id = Long.parseLong(attrValue);
                            break;
                        case "firstName":
                            firstName = attrValue;
                            break;
                        case "lastName":
                            lastName = attrValue;
                            break;
                        case "country":
                            country = attrValue;
                            break;
                        case "age":
                            age = Integer.parseInt(attrValue);
                    }
                }
                read(node_, listEmpXML);
            }
        }
        if (flagNewEmployee) {
            listEmpXML.add(new Employee(id, firstName, lastName, country, age));
        }
        return listEmpXML;
    }

    private static String readString(String path) {
        JSONParser parser = new JSONParser();
        String jsonString = "";
        try {
            Object obj = parser.parse(new FileReader("new_data.json"));
            JSONArray jsonArrayObject = (JSONArray) obj;
            jsonString = jsonArrayObject.toJSONString();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return jsonString;
    }

    private static List<Employee> jsonToList(String json) {
        JSONParser jsonParser = new JSONParser();
        List<Employee> listEmp = new ArrayList<>();
        try {
            JSONArray jsonArray = (JSONArray) jsonParser.parse(json);
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                String employeeString = jsonObject.toJSONString();
                Employee employee = gson.fromJson(employeeString, Employee.class);
                listEmp.add(employee);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return listEmp;
    }
}