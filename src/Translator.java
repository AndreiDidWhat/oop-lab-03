import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Translator {

    private final Map<String, String> dictionary = new HashMap<>();
    private final List<String> sortedKeys = new ArrayList<>();

    public void loadDictionaryFromFile(String filePath) throws FileReadException, InvalidFileFormatException {
        dictionary.clear();
        sortedKeys.clear();

        try {
            for (String line : Files.readAllLines(Path.of(filePath), StandardCharsets.UTF_8)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }

                String[] parts = line.split("\\|", -1);
                if (parts.length != 2 || parts[0].trim().isEmpty() || parts[1].trim().isEmpty()) {
                    throw new InvalidFileFormatException("Ошибка в строке словаря: " + line);
                }

                dictionary.put(parts[0].trim().toLowerCase(), parts[1].trim());
            }
        } catch (IOException e) {
            throw new FileReadException("Ошибка чтения файла '" + filePath + "': " + e.getMessage(), e);
        }

        sortedKeys.addAll(dictionary.keySet());
        sortedKeys.sort((a, b) -> Integer.compare(b.length(), a.length()));
    }

    private boolean isWordBoundary(String text, int index) {
        if (index < 0 || index >= text.length()) {
            return true;
        }
        return !Character.isLetterOrDigit(text.charAt(index));
    }

    public String translate(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            boolean matched = false;
            if (i == 0 || isWordBoundary(text, i - 1)) {
                for (String key : sortedKeys) {
                    if (text.regionMatches(true, i, key, 0, key.length()) && isWordBoundary(text, i + key.length())) {
                        result.append(dictionary.get(key));
                        i += key.length();
                        matched = true;
                        break;
                    }
                }
            }
            if (!matched) {
                result.append(text.charAt(i++));
            }
        }
        return result.toString();
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Translator translator = new Translator();

        try {
            System.out.print("Путь к файлу словаря: ");
            String path = scanner.nextLine().trim().replace("\"", "");

            translator.loadDictionaryFromFile(path);

            while (true) {
                System.out.print("Введите текст: ");
                if (!scanner.hasNextLine()) {
                    break;
                }
                String input = scanner.nextLine();
                if ("exit".equalsIgnoreCase(input.trim())) {
                    break;
                }

                String result = translator.translate(input);
                System.out.println("Перевод: " + result);
            }
        } catch (FileReadException | InvalidFileFormatException e) {
            System.err.println("Ошибка: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}
