package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Util {
	
	static final Pattern numEx = Pattern.compile("^(0|-?[1-9][0-9]*)$");
	static final Pattern decEx = Pattern.compile("^-?([0-9]+(\\.[0-9]+)?|\\.[0-9]+)([eE]-?[0-9]+)?$");
	static final Charset encoding = Charset.forName("UTF-8");
	
	private static boolean isInteger(String line) {
		return numEx.matcher(line).matches();
	}

	private static boolean isFloat(String line) {
		return decEx.matcher(line.replace(",", ".")).matches();
	}
	
	private static boolean isValidFilename(String name) {
		if (name.contains("/") || name.contains("\\") || name.contains("..")) return false;
		if (!name.matches("^[a-zA-Z0-9._-]+$")) return false;
		String upper = name.toUpperCase();
	    List<String> reserved = List.of("CON","PRN","AUX","NUL",
	            "COM1","COM2","COM3","COM4","COM5","COM6","COM7","COM8","COM9",
	            "LPT1","LPT2","LPT3","LPT4","LPT5","LPT6","LPT7","LPT8","LPT9");
	    if (reserved.contains(upper)) return false;
		return true;
	}

	private static void writeToTheFile(String path, String filename, ArrayList<?> data, boolean append) {
		Path filepath;

		try {
			Path dir = Paths.get(path);
			Files.createDirectories(dir);
			filepath = dir.resolve(filename);
		} catch (IOException e) {
			System.out.println("Не удалось использовать директорию " + path + ", файл будет сохранён в текущей папке.");
			filepath = Paths.get(".");
		}

		try (BufferedWriter writer = Files.newBufferedWriter(filepath, StandardCharsets.UTF_8,
				StandardOpenOption.CREATE, append ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING))

		{
			for (Object elem : data) {
				writer.write(elem.toString());
				writer.newLine();
			}
		} catch (IOException e) {
			System.err.println("Ошибка при записи в файл " + filename);
		}
	}

	public static void main(String[] args) {
		String path = ".";
		String prefix = "";
		String stat = null;
		boolean addToExisting = false;
		ArrayList<String> files = new ArrayList<String>();

		ArrayList<String> strings = new ArrayList<String>();
		ArrayList<String> integers = new ArrayList<String>();
		ArrayList<String> floats = new ArrayList<String>();

		int argsSize = args.length;
		
		for (int i = 0; i < argsSize; i++) {
			String arg = args[i];

			switch (arg) {
			case ("-o"):
				if (i + 1 < argsSize) path = args[i + 1];
				i++;
				continue;
			case ("-p"):
				if (i + 1 < argsSize && isValidFilename(args[i + 1])) prefix = args[i + 1];
				i++;
				continue;
			case ("-s"):
				stat = "short";
				continue;
			case ("-f"):
				stat = "full";
				continue;
			case ("-a"):
				addToExisting = true;
				continue;
			}

			if (isValidFilename(arg)) files.add(arg);
		}

		for (String filename : files) {
			try (BufferedReader reader = new BufferedReader(new FileReader(filename, encoding))) {
				String line;
				while ((line = reader.readLine()) != null) {
					if (isInteger(line))
						integers.add(line);
					else if (isFloat(line))
						floats.add(line);
					else
						strings.add(line);
				}
			} catch (IOException e) {
				System.err.println("Ошибка при прочтении файла " + filename);
			}
		}

		if (!integers.isEmpty()) {
			writeToTheFile(path, prefix + "integers.txt", integers, addToExisting);
		}

		if (!floats.isEmpty()) {
			writeToTheFile(path, prefix + "floats.txt", floats, addToExisting);
		}

		if (!strings.isEmpty()) {
			writeToTheFile(path, prefix + "strings.txt", strings, addToExisting);
		}

		if (stat.equals("full") || stat.equals("short")) {
			System.out.printf("В файл %sintegers.txt записано чисел: %d\n", prefix, integers.size());
			System.out.printf("В файл %sfloats.txt записано чисел: %d\n", prefix, floats.size());
			System.out.printf("В файл %sstrings.txt записано строк: %d \n", prefix, strings.size());
		}

		if (stat.equals("full")) {

			if (!strings.isEmpty()) {
				int maxLen = 0;
				int minLen = Integer.MAX_VALUE;
				for (String string : strings) {
					int l = string.length();
					if (l > maxLen)
						maxLen = l;
					if (l < minLen)
						minLen = l;
				}

				System.out.println();

				System.out.println("Максимальная длина строки: " + maxLen);
				System.out.println("Минимальная длина строки: " + minLen);
			}

			if (!floats.isEmpty() || !integers.isEmpty()) {
				Stats stats = new Stats();

				if (!floats.isEmpty()) {
					System.out.println();
					stats.decimalStats(floats);
				}
				if (!integers.isEmpty()) {
					System.out.println();
					stats.intStats(integers);
				}
			}

		}
	}
}
