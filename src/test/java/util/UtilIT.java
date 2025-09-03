package test.java.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class UtilIT {
	
	public static record Result(int exitCode, String stdout) {
	}

	public static Result runJar(Path dir, String... extra) throws Exception {
		Path jar = Path.of("target", "sorting-util-1.0.jar");

		List<String> cmd = new java.util.ArrayList<>();
		cmd.add("java");
		cmd.add("-jar");
		cmd.add(jar.toString());
		cmd.add("-o");
		cmd.add(dir.toString());
		if (extra != null) {
			cmd.addAll(Arrays.asList(extra));
		}

		Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();

		String out;
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(p.getInputStream(), Charset.forName("windows-1251")))) {
			out = br.lines().collect(Collectors.joining("\n"));
		}
		int code = p.waitFor();
		return new Result(code, out);
	}

	static Stream<Arguments> fileCases() {
		return Stream.of(Arguments.of("simple.txt", List.of("integers.txt", "floats.txt", "strings.txt"), List.of()),
				Arguments.of("emoji.txt", List.of("strings.txt"), List.of("integers.txt", "floats.txt")),
				Arguments.of("spaces.txt", List.of("strings.txt"), List.of("integers.txt", "floats.txt")),
				Arguments.of("numbers.txt", List.of("integers.txt", "floats.txt", "strings.txt"), List.of()),
				Arguments.of("reserved.txt", List.of("strings.txt"), List.of("integers.txt", "floats.txt")));
	}

	static Stream<Arguments> inputCases() {
		return Stream.of(Arguments.of("-f", List.of("введена неверно", "Использование: "), List.of(".txt записано")),
				Arguments.of("-s -o", List.of("введена неверно", "Использование: "), List.of(".txt записано")),
				Arguments.of("-s src/test/resources/simple.txt", List.of(".txt записано"),
						List.of("Сумма", "длина строки")),
				Arguments.of("-f src/test/resources/long-line.txt", List.of("строк: 1", "длина строки: 496890"),
						List.of("Сумма")));
	}

	@DisplayName("В принципе работает, ошибки не выдаёт")
	@Tag("smoke")
	@Test
	void programWorks(@TempDir Path tempDir) throws Exception {
		Result res = runJar(tempDir, "src/test/resources/simple.txt");
		assertTrue(res.exitCode == 0);
	}

	@DisplayName("Работает с несколькими файлами")
	@Test
	void processesMultipleFiles(@TempDir Path tempDir) throws Exception {
		Result res = runJar(tempDir, "-s", "src/test/resources/numbers.txt", "src/test/resources/languages.txt",
				"src/test/resources/emoji.txt");
		assertTrue(res.exitCode == 0);
		assertTrue(res.stdout.contains("строк: 12"), "Записано меньше/больше строк, чем содержится в файлах");
		assertTrue(Files.size(tempDir.resolve("strings.txt")) > 0, "Файл со строками не должен быть пустым");
		assertTrue(Files.size(tempDir.resolve("integers.txt")) > 0, "Файл со целыми числами не должен быть пустым");
		assertTrue(Files.size(tempDir.resolve("floats.txt")) > 0, "Файл с дробными числами не должен быть пустым");
	}

	@DisplayName("Создаёт только нужные файлы")
	@ParameterizedTest(name = "набор #{index}")
	@MethodSource("fileCases")
	void processesDifferentTypes(String fileName, List<String> expectedFiles, List<String> unexpectedFiles,
			@TempDir Path tempDir) throws Exception {
		Result res = runJar(tempDir, "-s", "src/test/resources/" + fileName);
		assertTrue(res.exitCode == 0);
		for (String fname : expectedFiles) {
			Path p = tempDir.resolve(fname);
			assertTrue(Files.exists(p), "Файл должен быть создан: " + p);
			assertTrue(Files.size(p) > 0, "Файл не должен быть пустым: " + p);
		}
		for (String fname : unexpectedFiles) {
			Path p = tempDir.resolve(fname);
			assertFalse(Files.exists(p), "Файл не должен быть создан: " + p);
		}
	}

	@DisplayName("Корректно обрабатывает флаги статистики")
	@ParameterizedTest(name = "набор #{index}")
	@MethodSource("inputCases")
	void processesDifferentParameters(String param, List<String> expectedSubstrings, List<String> unexpectedSubstrings,
			@TempDir Path tempDir) throws Exception {
		Result res = runJar(tempDir, param.split(" "));
		assertTrue(res.exitCode == 0);
		for (String rule : expectedSubstrings) {
			assertTrue(res.stdout.contains(rule),
					"В выводе должна содержатся строка: " + rule + "\nВывод:\n" + res.stdout);
		}
		for (String rule : unexpectedSubstrings) {
			assertFalse(res.stdout.contains(rule),
					"В выводе не должна содержатся строка: " + rule + "\nВывод:\n" + res.stdout);
		}
	}

	@DisplayName("С флагом -a дописывает в файл")
	@Test
	void appendsToFile(@TempDir Path sharedDir) throws Exception {
		runJar(sharedDir, "src/test/resources/languages.txt");
		long lineCount = Files.lines(sharedDir.resolve("strings.txt")).count();
		Result res2 = runJar(sharedDir, "-a", "src/test/resources/languages.txt");
		long lineCount2 = Files.lines(sharedDir.resolve("strings.txt")).count();

		assertTrue(res2.exitCode == 0);
		assertTrue(lineCount2 == 2 * lineCount,
				"Количество строк после повторной записи должно было увеличиться вдвое");
	}

	@DisplayName("С флагом -p добавляет префикс")
	@Test
	void createsFilesWithPrefix(@TempDir Path tempDir) throws Exception {
		Result res = runJar(tempDir, "-p", "TEST-", "src/test/resources/simple.txt");
		assertTrue(res.exitCode == 0);
		assertAll(() -> assertTrue(Files.exists(tempDir.resolve("TEST-strings.txt"))),
				() -> assertTrue(Files.exists(tempDir.resolve("TEST-integers.txt"))),
				() -> assertTrue(Files.exists(tempDir.resolve("TEST-floats.txt"))));
	}
}
