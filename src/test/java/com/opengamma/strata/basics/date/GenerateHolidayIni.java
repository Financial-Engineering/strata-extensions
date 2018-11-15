package com.opengamma.strata.basics.date;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.junit.Ignore;
import org.junit.Test;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;

public class GenerateHolidayIni {

	private static final String DIR = "/Users/rlewis/Downloads/";
	private static final String CALENDAR = "HKHK";

	// Convert a list of dates in 2013-Jan-01 format into a Strata holiday record
	@Test
	public void test() {
		String fileName = DIR + CALENDAR + ".txt";
		Map<String, List<Tuple2<String, String>>> map = HashMap.empty();

		System.out.println("[" + CALENDAR + "]");
		System.out.println("Weekend = Sat,Sun");
		try (BufferedReader br = Files.newBufferedReader(Paths.get(fileName))) {

			map = List.ofAll(br.lines()).filter(d -> {
				LocalDate dt = LocalDate.parse(d, DateTimeFormatter.ofPattern("yyyy-MMM-dd", Locale.US));
				return !(dt.getDayOfWeek() == DayOfWeek.SATURDAY || dt.getDayOfWeek() == DayOfWeek.SUNDAY);
			}).map(l -> List.of(l.split("-")).transform(s -> Tuple.of(s.get(0), s.get(1) + s.get(2))))
					.groupBy(t -> t._1).toSortedMap(e -> e);

		} catch (IOException e) {
			e.printStackTrace();
		}

		map.forEach((k, v) -> System.out.println(k + " = " + v.map(t -> t._2).mkString(",")));
	}

}
