package util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;

public final class Stats {
	
	static final MathContext mc = new MathContext(10, RoundingMode.HALF_UP);

	private static int compareIntStr(String a, String b) {
		boolean negA = a.startsWith("-");
		boolean negB = b.startsWith("-");

		if (negA && !negB)
			return -1;
		if (!negA && negB)
			return 1;

		if (a.length() < b.length())
			return negA ? 1 : -1;
		if (a.length() > b.length())
			return negA ? -1 : 1;

		return negA ? b.compareTo(a) : a.compareTo(b);
	}

	public void intStats(ArrayList<String> strNums) {
		String max = String.valueOf(Long.MIN_VALUE);
		String min = String.valueOf(Long.MAX_VALUE);
		Long sum = 0L;
		BigInteger sumBig = null;
		BigDecimal avg = null;

		for (String strNum : strNums) {
			
			if (compareIntStr(strNum, min) < 0)
				min = strNum;
			if (compareIntStr(strNum, max) > 0)
				max = strNum;

			/*
			 * считаем в long до тех пор, пока не выходим из допустимого диапазона, чтобы не
			 * тратить лишние ресурсы на biginteger, если числа небольшие
			 */
			if (sumBig == null) {
				try {
					sum += Long.parseLong(strNum);
				} catch (NumberFormatException | ArithmeticException e) {
					sumBig = new BigInteger(strNum).add(BigInteger.valueOf(sum));
				}
			} else {
				sumBig = sumBig.add(new BigInteger(strNum));
			}
		}

		if (sumBig == null) {
			avg = new BigDecimal(sum).divide(new BigDecimal(strNums.size()), mc);
		} else {
			avg = new BigDecimal(sumBig).divide(new BigDecimal(strNums.size()), mc);
		}

		System.out.println("Минимальное целое число: " + min);
		System.out.println("Максимальное целое число: " + max);
		System.out.println("Сумма: " + (sumBig == null ? sum : sumBig));
		System.out.println("Среднее: " + avg);
	}

	public void decimalStats(ArrayList<String> strNums) {
		BigDecimal max = BigDecimal.valueOf(Long.MIN_VALUE);
		BigDecimal min = BigDecimal.valueOf(Long.MAX_VALUE);
		BigDecimal sum = BigDecimal.ZERO;
		
		for (String strNum : strNums) {
			BigDecimal num = new BigDecimal(strNum);
			if (num.compareTo(min) < 0) min = num;
			if (num.compareTo(max) > 0) max = num;
			sum = sum.add(num);
		}
		
		BigDecimal avg = sum.divide(new BigDecimal(strNums.size()), mc);

		System.out.println("Минимальное число с плавающей запятой: " + min);
		System.out.println("Максимальное число с плавающей запятой: " + max);
		System.out.println("Сумма (точность до 10 значащих цифр): " + sum.round(mc));
		System.out.println("Среднее (точность до 10 значащих цифр): " + avg);
	}
}
