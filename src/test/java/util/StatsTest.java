package test.java.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import main.java.util.Stats;
import main.java.util.Stats.IntResult;
import main.java.util.Stats.DecResult;

public class StatsTest {

	static final MathContext mc = new MathContext(15, RoundingMode.HALF_UP);

	private static IntResult expIntRes(String min, String max, String sum, String avg) {
		return new IntResult(min, max, new BigInteger(sum), new BigDecimal(avg, mc));
	}

	private static DecResult expDecRes(String min, String max, String sum, String avg) {
		return new DecResult(new BigDecimal(min), new BigDecimal(max), new BigDecimal(sum), new BigDecimal(avg));
	}

	static Stream<Arguments> intInputCases() {
		return Stream.of(
				Arguments.of(Arrays.asList("0", "2", "3", "56", "-1", "0", "-45", "21", "-56"),
						expIntRes("-56", "56", "-20", "-2.22222222222222")),
				Arguments.of(Arrays.asList("1", "563442546356", "3255464643665", "-352453", "-342463752465321"),
						expIntRes("-342463752465321", "3255464643665", "-338644845627752", "-67728969125550.4")),
				Arguments.of(Arrays.asList("-45", "-35567", "-76", "-6783", "-57433"),
						expIntRes("-57433", "-45", "-99904", "-19980.8")),
				Arguments.of(Arrays.asList("333333"), expIntRes("333333", "333333", "333333", "333333")),
				Arguments.of(Arrays.asList("-34565", "-3553", "0", "23", "9494"),
						expIntRes("-34565", "9494", "-28601", "-5720.2")),
				Arguments.of(Arrays.asList("9494", "23", "0", "-3553", "-34565"),
						expIntRes("-34565", "9494", "-28601", "-5720.2")));
	}

	static Stream<Arguments> decInputCases() {
		return Stream.of(
				Arguments.of(Arrays.asList("0.0", "2.5", "3.125", "56.75", "-1.0", "0.00", "-45.5", "21.25", "-56.125"),
						expDecRes("-56.125", "56.75", "-19.000", "-2.11111111111111")),
				Arguments.of(
						Arrays.asList("1.25", "5634425.46356", "3.255464643665e6", "-3524.53", "-3.42463752465321e14"),
						expDecRes("-342463752465321", "5634425.46356", "-342463743578954", "-68492748715790.8")),
				Arguments.of(Arrays.asList(".5", "-.125", "1.75", "-2.5", "3.0e-2", "-4.0E-3"),
						expDecRes("-2.5", "1.75", "-0.3490", "-0.0581666666666667")),
				Arguments.of(Arrays.asList("0.3"), expDecRes("0.3", "0.3", "0.3", "0.3")),
				Arguments.of(Arrays.asList("123.456", "789.012", "-345.678", "0.001", "-0.0005"),
						expDecRes("-345.678", "789.012", "566.7905", "113.3581")));
	}

	@DisplayName("Считает статистику по целым числам")
	@ParameterizedTest(name = "набор #{index}")
	@MethodSource("intInputCases")
	void collectsRightStatisticsOnInts(List<String> input, IntResult expected) throws Exception {
		Stats stats = new Stats();
		IntResult res = stats.intStats(input);
		assertEquals(0, expected.min().compareTo(res.min()), "min " + res.min() + ", ожидалось " + expected.min());
		assertEquals(0, expected.max().compareTo(res.max()), "max " + res.max() + ", ожидалось " + expected.max());
		assertEquals(0, expected.sum().compareTo(res.sum()), "sum" + res.sum() + ", ожидалось " + expected.sum());
		assertEquals(0, expected.avg().compareTo(res.avg()), "avg " + res.avg() + ", ожидалось " + expected.avg());
	}

	@DisplayName("Считает статистику по числам с плавающей запятой")
	@ParameterizedTest(name = "набор #{index}")
	@MethodSource("decInputCases")
	void collectsRightStatisticsOnDecimals(List<String> input, DecResult expected) throws Exception {
		Stats stats = new Stats();
		DecResult res = stats.decimalStats(input);

		assertEquals(0, expected.min().compareTo(res.min()), "min " + res.min() + ", ожидалось " + expected.min());
		assertEquals(0, expected.max().compareTo(res.max()), "max " + res.max() + ", ожидалось " + expected.max());
		assertEquals(0, expected.sum().compareTo(res.sum()), "sum" + res.sum() + ", ожидалось " + expected.sum());
		assertEquals(0, expected.avg().compareTo(res.avg()), "avg " + res.avg() + ", ожидалось " + expected.avg());
	}
}
