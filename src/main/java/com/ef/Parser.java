package com.ef;

import com.ef.components.LogRecordCSVParser;
import com.ef.model.TimeDuration;
import com.ef.utils.DateFormatUtils;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.time.LocalDateTime;
import java.util.List;

@EnableJpaRepositories(basePackages ="com.ef")
public class Parser
{



	public static void main( String[] args )
	{
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"applicationContext.xml");
		((ClassPathXmlApplicationContext) context).registerShutdownHook();
		LogRecordCSVParser logRecordCSVParser = context.getBean(LogRecordCSVParser.class);
		run(logRecordCSVParser, args);
	}

	private static void run(LogRecordCSVParser logRecordCSVParser, String[] args) {
		try
		{
			Options options = new Options();
			Option startDate = new Option("s", "startDate", true, "Start date for eval, in ISO8601 Format");
			startDate.setRequired(true);
			options.addOption(startDate);
			Option duration = new Option("d","duration", true, "Duration - hourly, daily supported");
			duration.setRequired(true);
			options.addOption(duration);
			Option threshold = new Option("t", "threshold", true, "Threshold limit of IP occurences within state duration.");
			threshold.setRequired(true);
			options.addOption(threshold);
			Option accesslog = new Option("l", "accesslog", true, "Path to csv file for loading. Will use default access.log if not provided.");
			accesslog.setRequired(false);
			options.addOption(accesslog);

			CommandLineParser parser = new DefaultParser();
			HelpFormatter formatter = new HelpFormatter();
			CommandLine commandLine;
			try
			{
				commandLine = parser.parse(options, args);
				String startDateArg = commandLine.getOptionValue("startDate");
				LocalDateTime dateTime = DateFormatUtils.toParsedDateTime(startDateArg);
				TimeDuration durationArg = TimeDuration.valueOf(commandLine.getOptionValue("duration").toUpperCase());
				Long thresholdArg = Long.valueOf(commandLine.getOptionValue("threshold"));
				String accessLog = commandLine.getOptionValue("accesslog");
				if(StringUtils.isBlank(accessLog)) {
					accessLog = "access.log";
				}
				System.out.println("Starting import of " + accessLog);
				logRecordCSVParser.importCSVFile(accessLog);
				System.out.println("Finished import of log files");
				List<String> results = logRecordCSVParser.findThreshold(dateTime, durationArg, thresholdArg);
				if(results == null) {
					throw new IllegalStateException("Results should not be null");
				}
				if(results.size() > 0) {
					System.out.println("The following IP addresses exceeded threshold:");
				}
				results.stream().forEach(result->System.out.println(result));
			} catch (ParseException e) {
				System.out.println(e.getMessage());
				formatter.printHelp("com.ef.Parser", options);
				System.exit(1);
			}

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			System.exit(1);
		}
	}
}
