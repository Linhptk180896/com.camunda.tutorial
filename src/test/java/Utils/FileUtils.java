package Utils;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.Map;

public class FileUtils {
    static FileUtils fileUtils;

    public static FileUtils getInstance() {
        if (fileUtils == null) {
            fileUtils = new FileUtils();
        }
        return fileUtils;
    }

    public Map<String, String> readJsonFileToMap(String fileName) {
        String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + fileName;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(new File(filePath), new TypeReference<Map<String, String>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public String readJsonFileToString(String fileName, Object... jsonValues) {
        String filePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + fileName;
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream inputStream;
        String json;
        try {
            inputStream = new FileInputStream(filePath);
            json = objectMapper.readTree(inputStream).toString();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (jsonValues.length > 0) {
            for (int i = 0; i < jsonValues.length; i++) {
//                json = String.format(json, jsonValues[i], jsonValues[i+1]);
                json = json.replace( "%s", (CharSequence) jsonValues[i]);
            }
        }
        return json;

    }
}
