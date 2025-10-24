package main.java.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import main.java.util.Stats.DecResult;
import main.java.util.Stats.IntResult;

public class Util {

	static final Pattern numEx = Pattern.compile("^(0|-?[1-9][0-9]*)$");
	static final Pattern decEx = Pattern.compile("^-?([0-9]*(\\.[0-9]+)?|\\.[0-9]+)([eE]-?[0-9]+)?$");
	static final Charset encoding = Charset.forName("UTF-8");

	private static String usage = """
			Использование: java -jar sorting-data-from-files.jar [ОПЦИИ] <входной_файл1> <входной_файл2> ... <входной_файлN>

						Строки из файлов читаются по очереди в порядке перечисления.
						Разделитель записей во входных файлах - перевод строки.
						По умолчанию результаты записываются в файлы integers.txt, floats.txt и strings.txt

						Параметры:
						  -o <путь>          Каталог для выходных файлов
						                     (по умолчанию - текущая папка).

						  -p <префикс>       Префикс имён выходных файлов.

						  -a                 Режим добавления: дописывать в существующие файлы
						                     (по умолчанию файлы перезаписываются).

						  -s                 Краткая статистика: только количество записанных элементов.

						  -f                 Полная статистика:
						                       * для чисел - min/max/сумма/среднее
						                       * для строк - минимальная и максимальная длина
						  """;

	private static boolean isInteger(String line) {
		line = line.trim();
		return numEx.matcher(line).matches();
	}

	private static boolean isFloat(String line) {
		line = line.replace(",", ".").trim();
		return decEx.matcher(line).matches();
	}

	public static Path resolveFilepath(String dirPath) {
		try {
			Path input = Paths.get(dirPath);
			Path dir;

			if (Files.isDirectory(input) || dirPath.startsWith("/")) {
				dir = input;
			} else {
				Path workDir = Paths.get(System.getProperty("user.dir"));
				dir = workDir.resolve(input);
			}

			Files.createDirectories(dir);
						if (!Files.isDirectory(dir)) {
				throw new IOException("Директория не была создана: " + dir);
			}
			return dir;
		} catch (IOException e) {
			System.out.println("Не удалось использовать директорию " + dirPath
					+ ", файл будет сохранён в текущей папке.");
			return Paths.get(System.getProperty("user.dir"));
		}
	}

	private static boolean isValidFilename(String name) {
		String upper = name.toUpperCase();
		List<String> reserved = List.of("CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6",
				"COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9");
		if (reserved.contains(upper))
			return false;
		if (name.isBlank())
			return false;
		return true;
	}

	private static void writeToTheFile(Path dir, String filename, ArrayList<?> data, boolean append) {
		Path filepath = dir.resolve(filename);

		try (BufferedWriter writer = Files.newBufferedWriter(filepath, encoding, StandardOpenOption.CREATE,
				append ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING))

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
		String stat = "";
		boolean addToExisting = false;
		ArrayList<String> files = new ArrayList<String>();

		ArrayList<String> strings = new ArrayList<String>();
		ArrayList<String> integers = new ArrayList<String>();
		ArrayList<String> floats = new ArrayList<String>();

		int argsSize = args.length;

		System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
		System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8));

		for (int i = 0; i < argsSize; i++) {
			String arg = args[i];

			switch (arg) {
				case ("-o"):
					if (i + 1 < argsSize)
						path = args[i + 1];
					i++;
					continue;
				case ("-p"):
					if (i + 1 < argsSize && isValidFilename(args[i + 1]))
						prefix = args[i + 1];
					i++;
					continue;
				case ("-s"):
					if (stat.isBlank())
						stat = "short";
					continue;
				case ("-f"):
					if (stat.isBlank())
						stat = "full";
					continue;
				case ("-a"):
					addToExisting = true;
					continue;
			}

			if (isValidFilename(arg))
				files.add(arg);
			else
				System.out.println("Недопустимое имя файла " + arg);
		}

		if (argsSize == 0 || files.size() == 0) {
			System.out.println("Команда введена неверно.");
			System.out.println(usage);
			return;
		}

		Path dir = resolveFilepath(path);

		for (String filename : files) {
			try (BufferedReader reader = new BufferedReader(new FileReader(filename, encoding))) {
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.isBlank())
						continue;
					if (isInteger(line))
						integers.add(line.trim());
					else if (isFloat(line))
						floats.add(line.replace(",", ".").trim());
					else
						strings.add(line);
				}
						} catch (IOException e) {
				System.err.println("Ошибка при прочтении файла " + filename);
			}
		}

		if (integers.isEmpty() && floats.isEmpty() && strings.isEmpty()) {
			System.out.println("Сортировка не была выполнена, проверьте содержимое исходных файлов.");
			return;
		}

		if (!integers.isEmpty()) {
			writeToTheFile(dir, prefix + "integers.txt", integers, addToExisting);
		}

		if (!floats.isEmpty()) {
			writeToTheFile(dir, prefix + "floats.txt", floats, addToExisting);
		}

		if (!strings.isEmpty()) {
			writeToTheFile(dir, prefix + "strings.txt", strings, addToExisting);
		}

		if (stat.equals("full") || stat.equals("short")) {
			if (!integers.isEmpty())
				System.out.printf("В файл %sintegers.txt записано чисел: %d\n", prefix, integers.size());
			if (!floats.isEmpty())
				System.out.printf("В файл %sfloats.txt записано чисел: %d\n", prefix, floats.size());
			if (!strings.isEmpty())
				System.out.printf("В файл %sstrings.txt записано строк: %d \n", prefix, strings.size());
		} else {
			System.out.printf("Сортировка была произведена, результаты можете проверить в %s.\n",
					(path == "." ? "текущей папке" : dir.toString()));
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
					DecResult res = stats.decimalStats(floats);
					System.out.println("Минимальное число с плавающей запятой: " + res.min());
					System.out.println("Максимальное число с плавающей запятой: " + res.max());
					System.out.println("Сумма (точность до 10 значащих цифр): " + res.sum());
					System.out.println("Среднее (точность до 10 значащих цифр): " + res.avg());
				}
				if (!integers.isEmpty()) {
					System.out.println();
					IntResult res = stats.intStats(integers);
					System.out.println("Минимальное целое число: " + res.min());
					System.out.println("Максимальное целое число: " + res.max());
					System.out.println("Сумма: " + res.sum());
					System.out.println("Среднее: " + res.avg());
				}
			}

		}
	}
}
