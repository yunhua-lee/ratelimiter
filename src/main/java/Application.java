import limiter.*;
import biz.*;
import org.apache.commons.cli.*;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @Desctiption
 * @Author wallace
 * @Date 2023/7/19
 */
public class Application {
	public static void main(String[] args) throws Exception {

		Options options = buildOptions();

		CommandLine cmd = parseCmd(options, args);
		if (cmd.hasOption("h")) {
			printHelp(options);
			return;
		}

		//Get option value
		Integer limiterInterval = Integer.valueOf(cmd.getOptionValue("i", "10"));
		if( limiterInterval < 1 ){
			System.err.println("limiter interval(-i) should be larger than 0;");
			return;
		}

		Integer limiterCount = Integer.valueOf(cmd.getOptionValue("c", "100"));
		if( limiterCount < 1 ){
			System.err.println("limiter count(-c) should be larger than 0;");
			return;
		}

		Integer requestCount = Integer.valueOf(cmd.getOptionValue("C", "100"));
		if( requestCount < 1 ){
			System.err.println("request count(-C) should be larger than 0;");
			return;
		}

		Integer threadCount = Integer.valueOf(cmd.getOptionValue("T", "3"));
		if( threadCount < 1 ){
			System.err.println("thread count(-T) should be larger than 0;");
			return;
		}

		String limiterType = cmd.getOptionValue("t");
		if( limiterType == null){
			System.err.println("Please choose right limiter type by option -t");
			printHelp(options);
			return;
		}

		Limiter limiter = null;
		if( limiterType.equalsIgnoreCase("fw")) {
			limiter = new FixedTimeWindow(limiterInterval, limiterCount);
		} else if( limiterType.equalsIgnoreCase("sw")){
			limiter = new SlideTimeWindow(limiterInterval, limiterCount);
		} else if( limiterType.equalsIgnoreCase("tb")){
			limiter = new TokenBucket(limiterInterval, limiterCount);
		} else {
			System.err.println("Please choose right limiter type by option -t");
			printHelp(options);
			return;
		}
		limiter.start();

		BlockingQueue<Long> queue = new ArrayBlockingQueue<>(1000);

		Requester requester = new Requester(requestCount);
		requester.start(queue);

		for(int i = 0; i < threadCount; i++){
			Processor processor = new Processor(limiter);
			processor.start(queue);
		}
	}

	private static Options buildOptions(){
		final Options options = new Options();

		final Option limiterInterval = new Option("i", true, "limiter interval(s)");
		options.addOption(limiterInterval);

		final Option limiterCount = new Option("c", true, "limiter count");
		options.addOption(limiterCount);

		final Option requestCount = new Option("C", true, "request count");
		options.addOption(requestCount);

		final Option limiterType = new Option("t", true, "limiter type, fw = fixed time window," +
				"sw = slide time window, tb = token bucket");
		options.addOption(limiterType);

		final Option threadCount = new Option("T", true, "thread count, default 3");
		options.addOption(threadCount);

		final Option help = new Option("h", false, "help");
		options.addOption(help);

		return options;
	}

	private static CommandLine parseCmd(Options options, String[] args) throws Exception {
		final CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
			return cmd;
		} catch (final ParseException e) {
			throw new Exception("parser command line error",e);
		}
	}

	private static void printHelp(Options options){

		System.out.println("Usage: java -jar ratelimiter.jar -i 10 -c 100 -C 200 -t fw -T 3");

		Collection<Option> optionList = options.getOptions();
		for(Option o : optionList){
			System.out.println("-" + o.getOpt() + ", " + o.getDescription());
		}
	}
}
