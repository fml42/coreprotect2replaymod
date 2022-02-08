package co2rm;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CliParser {
	
	public static void main(String[] args) {
		/*
		-w world
		-bb1 x y z
		-bb2 x y z
		-t1 24352345
		-t2 24352345
		-l 10
		 */
		
		Options options = new Options();
		
		Option help = new Option("h", "help", false, "shows help text");
		options.addOption(help);
		
        Option world = new Option("w", "world", true, "server world name");
        options.addOption(world);
        
        Option bb1 = new Option("bb1", "bounding-box-1", true, "bounding box corner 1");
        bb1.setArgs(3);
        bb1.setArgName("x y z");
        options.addOption(bb1);
        
        Option bb2 = new Option("bb2", "bounding-box-2", true, "bounding box corner 2");
        bb2.setArgs(3);
        bb2.setArgName("x y z");
        options.addOption(bb2);
        
        Option t1 = new Option("t1", "timestamp-1", true, "time start");
        options.addOption(t1);
        
        Option t2 = new Option("t2", "timestamp-2", true, "time end");
        options.addOption(t2);
        
        Option length = new Option("l", "length", true, "replay length in seconds");
        options.addOption(length);
        
        Option block = new Option("b", "blocks", true, "json block map");
        options.addOption(block);
        
        Option output = new Option("o", "output", true, "recording output");
        options.addOption(output);
        
        Option override = new Option("y", "override", false, "override output");
        options.addOption(override);
        
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
            
            if (cmd.hasOption(help)) {
            	HelpFormatter formatter = new HelpFormatter();
            	formatter.printHelp(100, "<cmd> <database> <recording> [options]", "", options, 
            			"Required arguments: w, bb1, bb2, t1, t2, l");
            	return;
            }
            
            List<Option> missing = new ArrayList<>();
            if (!cmd.hasOption(bb1)) missing.add(bb1);
            if (!cmd.hasOption(bb2)) missing.add(bb2);
            if (!cmd.hasOption(t1)) missing.add(t1);
            if (!cmd.hasOption(t2)) missing.add(t2);
            if (!cmd.hasOption(world)) missing.add(world);
            if (!cmd.hasOption(length)) missing.add(length);
            if (missing.size()>0) {
            	List<String> missingStr = new ArrayList<>();
            	for (Option om: missing) missingStr.add(om.getOpt());
            	System.out.println("Missing required arguments: "+String.join(", ", missingStr));
            	System.out.println("See -h for help");
            	return;
            }
            
            String cmdWorld = cmd.getOptionValue(world);
            String[] bb1Values = cmd.getOptionValues(bb1);
            String[] bb2Values = cmd.getOptionValues(bb2);
            Position cmdPos1 = new Position(Integer.parseInt(bb1Values[0]), Integer.parseInt(bb1Values[1]), Integer.parseInt(bb1Values[2]));
            Position cmdPos2 = new Position(Integer.parseInt(bb2Values[0]), Integer.parseInt(bb2Values[1]), Integer.parseInt(bb2Values[2]));
            int cmdTime1 = Integer.parseInt(cmd.getOptionValue(t1));
            int cmdTime2 = Integer.parseInt(cmd.getOptionValue(t2));
            int cmdLength = Integer.parseInt(cmd.getOptionValue(length)) * 1000;
            
            List<String> argList = cmd.getArgList();
            if (argList.size() >= 2) {
            	File dbIn = new File(argList.get(0));
            	File recIn = new File(argList.get(1));
            	
            	
            	List<String> fileSplit = Arrays.asList(recIn.getAbsolutePath().split("\\."));
            	if (fileSplit.get(fileSplit.size()-1).equalsIgnoreCase("mcpr")) fileSplit.set(fileSplit.size()-1, "out.mcpr");
            	else fileSplit.add("out.mcpr");
            	File recOut = new File(String.join(".", fileSplit));
            	if (cmd.hasOption(output)) {
            		recOut = new File(cmd.getOptionValue(output));
            	}
            	
            	if (recOut.exists()) {
            		if (!cmd.hasOption(override)) {
                		System.out.println("warning: output file exists! (use -y to override)");   
                		System.exit(0);
            		}
            	}
            	
            	File blockMapFile = new File("blocks.json");
            	if (cmd.hasOption(block)) {
            		blockMapFile = new File(cmd.getOptionValue(block));
            	}
            	if (!blockMapFile.exists()) {
            		System.out.println("blocks.json file missing!");   
            		System.exit(0);
            	}
            	if (!dbIn.exists()) {
            		System.out.println("database file not found!");   
            		System.exit(0);
            	}
            	if (!recIn.exists()) {
            		System.out.println("recording template file not found!");   
            		System.exit(0);
            	}
                
                Co2Rm.start(cmdWorld, cmdPos1, cmdPos2, cmdTime1, cmdTime2, cmdLength,
                		recIn, recOut, blockMapFile, dbIn);
            } else {
            	System.out.println("Usage: co2mr <database> <recording> [options]");
            	System.out.println("See -h for help");
            }
            
        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }
	}

}
