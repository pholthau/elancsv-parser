/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.csra.elancsv.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author Patrick Holthaus
 * (<a href=mailto:patrick.holthaus@uni-bielefeld.de>patrick.holthaus@uni-bielefeld.de</a>)
 */
public class SimpleParser {

	enum ElanFormat {

		TIER(0),
		START(1),
		STOP(2),
		DURATION(3),
		VALUE(4),
		FILE(5);

		private ElanFormat(int field) {
			this.field = field;
		}
		final int field;

	}

	private static void helpExit(Options opts, String header) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("elancsv-parser [OPTION...]", header, opts, null);
		System.exit(0);
	}

	public static void main(String[] args) throws IOException, ParseException {

		Options opts = new Options();
		opts.addOption("file", true, "Tab-separated ELAN export file to load.");
		opts.addOption("tier", true, "Tier to analyze. Optional: Append ::num to interpret annotations numerically.");
		opts.addOption("format", true, "How to read information from the file name. %V -> participant, %A -> annoatator, %C -> condition, e.g. \"%V - %A\"");
		opts.addOption("help", false, "Print this help and exit");

		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse(opts, args);
		if (cmd.hasOption("help")) {
			helpExit(opts, "where OPTION includes:");
		}
		
		String infile = cmd.getOptionValue("file");
		if (infile == null) {
			helpExit(opts, "Error: no file given.");
		}

		String format = cmd.getOptionValue("format");
		if (format == null) {
			helpExit(opts, "Error: no format given.");
		}
		
		String tier = cmd.getOptionValue("tier");
		if (tier == null) {
			helpExit(opts, "Error: no tier given.");
		}
		
		String[] tn = tier.split("::");
		boolean numeric = false;
		if (tn.length == 2 && tn[1].equals("num")) {
			numeric = true;
			tier = tn[0];
		}

		format = "^" + format + "$";
		format = format.replaceFirst("%V", "(?<V>.*?)");
		format = format.replaceFirst("%A", "(?<A>.*?)");
		format = format.replaceFirst("%C", "(?<C>.*?)");
		Pattern pa = Pattern.compile(format);

		Map<String, Participant> participants = new HashMap<>();
		BufferedReader br = new BufferedReader(new FileReader(infile));
		String line;
		while ((line = br.readLine()) != null) {
			String[] parts = line.split("\t");

			Annotation a = new Annotation(
					Long.valueOf(parts[ElanFormat.START.field]),
					Long.valueOf(parts[ElanFormat.STOP.field]),
					Long.valueOf(parts[ElanFormat.DURATION.field]),
					parts[ElanFormat.VALUE.field]);
			String tname = parts[ElanFormat.TIER.field];
			String file = parts[ElanFormat.FILE.field].replaceAll(".eaf", "");

			Matcher m = pa.matcher(file);
			String vp = file;
			String condition = "?";
			String annotator = "?";
			String participantID = vp;

			if (m.find()) {
				vp = m.group("V");
				if (format.indexOf("<A>") > 0) {
					annotator = m.group("A");
				}

				if (format.indexOf("<C>") > 0) {
					condition = m.group("C");
				}
			}
			participantID = vp + ";" + annotator;

			if (!participants.containsKey(participantID)) {
				participants.put(participantID, new Participant(vp, condition, annotator));
			}
			Participant p = participants.get(participantID);

			if (!p.tiers.containsKey(tname)) {
				p.tiers.put(tname, new Tier(tname));
			}

			p.tiers.get(tname).annotations.add(a);

		}

		Map<String, Map<String, Number>> values = new HashMap<>();
		Set<String> rownames = new HashSet<>();

		String allCountKey = "c: all values";
		String allDurationKey = "d: all values";
		String allMeanKey = "m: all values";

		for (Map.Entry<String, Participant> e : participants.entrySet()) {
//			System.out.println(e);
			Tier t = e.getValue().tiers.get(tier);
			String participantID = e.getKey();

			if (!values.containsKey(participantID)) {
				values.put(participantID, new HashMap<String, Number>());
			}
			Map<String, Number> row = values.get(participantID); //participant id

			if (t != null) {

				row.put(allCountKey, 0l);
				row.put(allDurationKey, 0l);
				row.put(allMeanKey, 0l);

				for (Annotation a : t.annotations) {

					long countAll = (long) row.get(allCountKey) + 1;
					long durationAll = (long) row.get(allDurationKey) + a.duration;
					long meanAll = durationAll / countAll;

					row.put(allCountKey, countAll);
					row.put(allDurationKey, durationAll);
					row.put(allMeanKey, meanAll);

					if (!numeric) {
						String countKey = "c: " + a.value;
						String durationKey = "d: " + a.value;
						String meanKey = "m: " + a.value;

						if (!row.containsKey(countKey)) {
							row.put(countKey, 0l);
						}
						if (!row.containsKey(durationKey)) {
							row.put(durationKey, 0l);
						}
						if (!row.containsKey(meanKey)) {
							row.put(meanKey, 0d);
						}

						long count = (long) row.get(countKey) + 1;
						long duration = (long) row.get(durationKey) + a.duration;
						double mean = duration * 1.0 / count;

						row.put(countKey, count);
						row.put(durationKey, duration);
						row.put(meanKey, mean);

						rownames.add(countKey);
						rownames.add(durationKey);
						rownames.add(meanKey);
					} else {
						String countKey = "c: " + t.name;
						String sumKey = "s: " + t.name;
						String meanKey = "m: " + t.name;

						if (!row.containsKey(countKey)) {
							row.put(countKey, 0l);
						}
						if (!row.containsKey(sumKey)) {
							row.put(sumKey, 0d);
						}
						if (!row.containsKey(meanKey)) {
							row.put(meanKey, 0d);
						}

						double d = 0;
						try {
							d = Double.valueOf(a.value);
						} catch (NumberFormatException ex) {

						}

						long count = (long) row.get(countKey) + 1;
						double sum = (double) row.get(sumKey) + d;
						double mean = sum / count;

						row.put(countKey, count);
						row.put(sumKey, sum);
						row.put(meanKey, mean);

						rownames.add(countKey);
						rownames.add(sumKey);
						rownames.add(meanKey);
					}

				}
			}

		}

		ArrayList<String> list = new ArrayList(rownames);
		Collections.sort(list);
		StringBuilder header = new StringBuilder("ID;Annotator;");
		header.append(allCountKey);
		header.append(";");
		header.append(allDurationKey);
		header.append(";");
		header.append(allMeanKey);
		header.append(";");
		for (String l : list) {
			header.append(l);
			header.append(";");
		}
		System.out.println(header);

		for (Map.Entry<String, Map<String, Number>> e : values.entrySet()) {
			StringBuilder row = new StringBuilder(e.getKey());
			row.append(";");
			if (e.getValue().containsKey(allCountKey)) {
				row.append(e.getValue().get(allCountKey));
			} else {
				row.append("0");
			}
			row.append(";");
			if (e.getValue().containsKey(allDurationKey)) {
				row.append(e.getValue().get(allDurationKey));
			} else {
				row.append("0");
			}
			row.append(";");
			if (e.getValue().containsKey(allMeanKey)) {
				row.append(e.getValue().get(allMeanKey));
			} else {
				row.append("0");
			}
			row.append(";");
			for (String l : list) {
				if (e.getValue().containsKey(l)) {
					row.append(e.getValue().get(l));
				} else {
					row.append("0");
				}
				row.append(";");
			}
			System.out.println(row);
		}
	}
}
