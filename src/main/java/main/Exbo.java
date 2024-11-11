package main;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Exbo {
    // Класс для хранения информации о награде
    static class Reward {

        int money;
        int details;
        int reputation;

        Reward(int money, int details, int reputation) {
            this.money = money;
            this.details = details;
            this.reputation = reputation;
        }
    }
    
    public static void main(String[] args) throws IOException {
        // Чтение файлов
        JSONObject tasks = readJsonFromFile("G:\\TPRG\\Exbo\\src\\main\\java\\test\\exbo\\task.json");
        JSONObject contracts = readJsonFromFile("G:\\TPRG\\Exbo\\src\\main\\java\\test\\exbo\\file.json");
        Map<String, Reward> rewards = readRewardsFromCsv("G:\\TPRG\\Exbo\\src\\main\\java\\test\\exbo\\items.csv");

        // Создание нового JSON объекта для используемых контрактов
        JSONObject usedContracts = new JSONObject();

        // Обработка всех задач
        for (String key : tasks.keySet()) {
            JSONObject taskSlot = tasks.getJSONObject(key);
            JSONArray taskList = taskSlot.getJSONArray("list");

            for (int i = 0; i < taskList.length(); i++) {
                String objectName = taskList.getString(i);

                if (contracts.has(objectName)) {
                    JSONObject contract = contracts.getJSONObject(objectName);
                    String rewardKey = contract.getString("reward");

                    if (rewards.containsKey(rewardKey)) {
                        Reward reward = rewards.get(rewardKey);
                        JSONObject newContract = new JSONObject();
                        newContract.put("reward", rewardKey);
                        newContract.put("money", reward.money);
                        newContract.put("details", reward.details);
                        newContract.put("reputation", reward.reputation);
                        usedContracts.put(objectName, newContract);
                    }
                }
            }
        }

        // Запись нового JSON файла
        try (FileWriter file = new FileWriter("G:\\TPRG\\Exbo\\src\\main\\java\\test\\exbo\\used_contracts.json")) {
            file.write(usedContracts.toString(4));
        }

        // Создание и заполнение Excel файла
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Contracts");

            // Создание заголовков
            Row headerRow = sheet.createRow(0);
            String[] headers = {"list_name", "object_name", "reward_key", "money", "details", "reputation", "isUsed"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Заполнение данными
            int rowNum = 1;
            for (String listName : tasks.keySet()) {
                JSONObject taskSlot = tasks.getJSONObject(listName);
                JSONArray taskList = taskSlot.getJSONArray("list");

                for (int i = 0; i < taskList.length(); i++) {
                    String objectName = taskList.getString(i);

                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(listName);
                    row.createCell(1).setCellValue(objectName);

                    if (contracts.has(objectName)) {
                        JSONObject contract = contracts.getJSONObject(objectName);
                        String rewardKey = contract.getString("reward");

                        if (rewards.containsKey(rewardKey)) {
                            Reward reward = rewards.get(rewardKey);
                            row.createCell(2).setCellValue(rewardKey);
                            row.createCell(3).setCellValue(reward.money);
                            row.createCell(4).setCellValue(reward.details);
                            row.createCell(5).setCellValue(reward.reputation);
                            row.createCell(6).setCellValue(1);
                        } else {
                            for (int j = 2; j <= 6; j++) {
                                row.createCell(j).setCellValue("N/A");
                            }
                        }
                    } else {
                        for (int j = 2; j <= 6; j++) {
                            row.createCell(j).setCellValue("N/A");
                        }
                        row.createCell(6).setCellValue(0);
                    }
                }
            }

            // Запись Excel файла
            try (FileOutputStream fileOut = new FileOutputStream("G:\\TPRG\\Exbo\\src\\main\\java\\test\\exbo\\contracts.xlsx")) {
                workbook.write(fileOut);
            }
        }
    }
    
    // Чтение JSON файлов
    private static JSONObject readJsonFromFile(String filePath) throws IOException {
        try (Reader reader = new FileReader(filePath)) {
            StringBuilder jsonText = new StringBuilder();
            int i;
            while ((i = reader.read()) != -1) {
                jsonText.append((char) i);
            }
            return new JSONObject(jsonText.toString());
        }
    }
    
    // Чтение CSV файла и создание карты<название, награды> наград
    private static Map<String, Reward> readRewardsFromCsv(String filePath) throws IOException {
        Map<String, Reward> rewards = new HashMap<>();
        try (Reader reader = new FileReader(filePath)) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .withDelimiter(';')
                    .withFirstRecordAsHeader()
                    .parse(reader);
           
            for (CSVRecord record : records) {
                String name = record.get("name").trim();
                int money = Integer.parseInt(record.get("money").trim());
                int details = Integer.parseInt(record.get("details").trim());
                int reputation = Integer.parseInt(record.get("reputation").trim());

                rewards.put(name, new Reward(money, details, reputation));
            }
        }
        return rewards;
    }
}
