import java.io.*;
import java.util.*;
import java.time.*;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.println("Введите путь к файлу или введите exit для завершения :");
            String input = scanner.nextLine();
            
            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Завершение работы программы...");
                break;
            }
            
            processFile(input);
        }
        
        scanner.close();
    }
    
    private static void processFile(String filePath) {
        Instant start = Instant.now();
        
        try {
            List<Address> addresses = readAddresses(filePath);
            
            // Поиск дубликатов
            Map<Address, Long> duplicates = addresses.stream()
                .collect(Collectors.groupingBy(a -> a, Collectors.counting()))
                .entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                
            // Подсчет этажности по городам
            Map<String, Map<Integer, Long>> buildingsByCity = addresses.stream()
                .collect(Collectors.groupingBy(Address::getCity,
                    Collectors.groupingBy(Address::getFloors, Collectors.counting())));
            
            // Вывод статистики
            System.out.println("\nПовторяющиеся адреса:");
            duplicates.forEach((address, count) -> 
                System.out.println(address + "    - " + count + " раз(а)"));
            
            System.out.println("\nРезультаты подсчета:");
            buildingsByCity.forEach((city, floors) -> {
                System.out.println("\nГород: " + city);
                for (int i = 1; i <= 5; i++) {
                    Long count = floors.getOrDefault(i, 0L);
                    System.out.println(i + " этаж: " + count + " зданий");
                }
            });
            
            Instant finish = Instant.now();
            long timeElapsed = Duration.between(start, finish).toMillis();
            System.out.println("\nВремя обработки файла: " + timeElapsed + " мс\n");
            
        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Произошла ошибка: " + e.getMessage());
        }
    }
    
    private static List<Address> readAddresses(String filePath) throws Exception {
        List<Address> addresses = new ArrayList<>();
        String fileExtension = "";
        String delimiter = "";
        
        int lastDotIndex = filePath.lastIndexOf(".");
        if (lastDotIndex > 0) {
            fileExtension = filePath.substring(lastDotIndex + 1).toLowerCase().trim();
        }
        
        filePath = filePath.replace("\"", "").trim();
        
        switch (fileExtension) {
            case "xml":
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(new File(filePath));
                
                // читаем адреса (теперь ищем элементы item)
                NodeList addressNodes = document.getElementsByTagName("item");
                for (int i = 0; i < addressNodes.getLength(); i++) {
                    Element addressElement = (Element) addressNodes.item(i);
                    try {
                        addresses.add(parseXMLElement(addressElement));
                    } catch (Exception e) {
                        System.out.println("Ошибка при разборе адреса: " + e.getMessage());
                    }
                }
                break;
            
            case "csv":
                try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                    String template = reader.readLine();
                    if (template != null) {
                        if (template.contains(";")) delimiter = ";";
                        else if (template.contains(",")) delimiter = ",";
                        else throw new IllegalArgumentException("Неподдерживаемый формат разделителя");
                    }
                    
                    String line;
                    while ((line = reader.readLine()) != null) {
                        addresses.add(parseCSVLine(line, delimiter));
                    }
                }
                break;
            
            default:
                throw new IllegalArgumentException("Неподдерживаемый формат файла: " + fileExtension);
        }
        
        return addresses;
    }
    
    private static Address parseCSVLine(String line, String delimiter) {
        String[] parts = line.split(delimiter);
        return new Address(
            parts[0].replace("\"", "").trim(), // город
            parts[1].replace("\"", "").trim(), // улица
            Integer.parseInt(parts[2].trim()), // дом
            Integer.parseInt(parts[3].trim())  // этажность
        );
    }
    
    private static Address parseXMLElement(Element element) {
        try {
            String city = element.getAttribute("city");
            String street = element.getAttribute("street");
            int house = Integer.parseInt(element.getAttribute("house"));
            int floors = Integer.parseInt(element.getAttribute("floor"));
            
            if (city.isEmpty() || street.isEmpty()) {
                throw new IllegalArgumentException("Отсутствуют обязательные атрибуты в XML");
            }
            
            return new Address(city, street, house, floors);
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка в формате XML данных: " + e.getMessage());
        }
    }
}
