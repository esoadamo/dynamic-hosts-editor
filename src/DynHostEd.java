package aho.program.dynhosted;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class DynHostEd {
    final static File settingsFile = new File("dhe.conf");
    final static String paramHostnameFile = "hostFile"; // Says where is hosts file located
    final static String paramScanInterval = "scan"; // Says how often scan in seconds
    final static int defaultScanInterval = 180; // In seconds
    final static String pingCommand = "ping -c 1 "; // Command used to obtain ping status of ip
    static File fileHosts = null; // Hosts file
    static Properties prop = null;

    public static void main(String[] args) {
	System.out.println("Welcome to dynamic host editor, made by Adam Hlavacek in 2016!");

	/*
	 * Check if settings file exist, if not, ask if it shall be created. If user answer yes, create it and
	 * print usage:
	 * {hostFile} = {/etc/hosts} - required parameter. Specify where is hosts file located.
	 * other parameters are in format
	 * {hostname} = {firstTryIP, secondTryIP}
	 */
	if (!settingsFile.exists()) {
	    System.out.println("Setting file (" + settingsFile.getAbsolutePath() + ") does not exist.\nDo you wish to be created? Y/n");

	    Scanner in = new Scanner(System.in);
	    if (in.nextLine().toLowerCase().startsWith("n")) {
		System.out.println("As you wish. Do it yourself...");
		in.close();
		System.exit(0);
	    }
	    in.close();

	    String fileHostsDefaultLocation = "/etc/hosts";
	    if (System.getProperty("os.name").toLowerCase().contains("win"))
		fileHostsDefaultLocation = "C:/Windows/System32/drivers/etc/hosts";

	    try {
		settingsFile.createNewFile();
		prop = new Properties(settingsFile);
		prop.saveNewKey(paramHostnameFile, fileHostsDefaultLocation, settingsFile);
		prop.saveNewKey(paramScanInterval, defaultScanInterval, settingsFile);
		prop.saveNewKey("home-pc", "10.0.0.14,178.65.24.36", settingsFile);
	    } catch (Exception e) {
		System.out.print("The settings file cannot be created: ");
		e.printStackTrace();
		System.exit(1);
	    }
	    System.out.println("Setting file has been sucessfuly created, now you shlould edit.\n" + "Usage is:");
	    System.out.println("{" + paramHostnameFile + "} = {" + fileHostsDefaultLocation + "} - required parameter. " + "Specify where is hosts file located.");
	    System.out.println("{" + paramScanInterval + "} = {10} - How often start ping on IPs in seconds." + " If not found, use default " + defaultScanInterval + " sec");
	    System.out.println("{hostname} = {firstIPAttemp, secondIPAttemp, thirdIPAttemp, XIPAttemp}");
	    System.out.println("{anotherHostname} = {firstIPAttemp, secondIPAttemp}");
	    System.out.println("etc.");
	    System.out.println("If fisrt attemp is sucessful, assing it to hostname. Otherwise try next one.");
	    System.exit(0);
	}

	System.out.println("Loading settings...");
	try {
	    prop = new Properties(settingsFile);
	} catch (Exception e) {
	    System.out.println("Error in loading settins file " + settingsFile.getAbsolutePath());
	    e.printStackTrace();
	}

	if (!prop.keyExist(paramHostnameFile)) {
	    System.err.println("Param '" + paramHostnameFile + "' is required in settings file (" + settingsFile.getAbsolutePath() + ")");
	    System.exit(1);
	}
	fileHosts = new File(prop.getString(paramHostnameFile));
	if (!fileHosts.exists()) {
	    System.err.println("Hosts file " + fileHosts.getAbsolutePath() + " does not exist!");
	    System.exit(1);
	}
	if (!fileHosts.canRead()) {
	    System.err.println("Hosts file " + fileHosts.getAbsolutePath() + " cannot be read!");
	    System.exit(1);
	}
	if (!fileHosts.canWrite()) {
	    System.err.println("Hosts file " + fileHosts.getAbsolutePath() + " cannot be wrtiten!");
	    System.exit(1);
	}

	int pingInterval = defaultScanInterval;
	if (prop.keyExist(paramScanInterval))
	    pingInterval = Integer.parseInt(prop.getString(paramScanInterval));
	System.out.println("Setting ping interval to " + pingInterval);

	new Timer().schedule(new TimerTask() {

	    @Override
	    public void run() {
		System.out.println("Pinging hosts...");
		final AtomicInteger runningThreads = new AtomicInteger(0);
		for (final String hostname : prop.propertiesNames) {
		    if (hostname.contentEquals(paramHostnameFile) || hostname.contentEquals(paramScanInterval))
			continue;
		    runningThreads.incrementAndGet();
		    new Thread() {
			public void run() {
			    System.out.println("Checking " + hostname);
			    for (String ip : prop.getString(hostname).split(","))
				if (ipReachable(ip)) {
				    StringBuilder outStr = new StringBuilder();
				    outStr.append("Assiging " + hostname + " to " + ip + " ... ");
				    try {
					BufferedReader in = new BufferedReader(new FileReader(fileHosts));
					List<String> lines = new ArrayList<String>();
					String lineIn;
					boolean alreadySet = false;
					boolean hostFound = false;
					while ((lineIn = in.readLine()) != null) {
					    if (lineIn.endsWith(" " + hostname) || lineIn.endsWith("\t" + hostname)) {
						hostFound = true;
						if (lineIn.startsWith(ip)) {
						    alreadySet = true;
						    outStr.append("already set");
						    break;
						}
						lineIn = ip + "\t" + hostname;
					    }
					    lines.add(lineIn);
					}
					in.close();

					if (!hostFound) {
					    outStr.append(" not found in hosts file, adding it manually ... ");
					    lines.add(ip + "\t" + hostname);
					}

					if (!alreadySet) {
					    BufferedWriter out = new BufferedWriter(new FileWriter(fileHosts));
					    for (String lineOut : lines)
						out.write(lineOut + System.lineSeparator());
					    out.close();
					    outStr.append("done");
					}

				    } catch (IOException e) {
					outStr.append("fail");
					e.printStackTrace();
				    } finally {
					System.out.println(outStr.toString());
				    }
				    break;
				}
			    runningThreads.decrementAndGet();
			}
		    }.start();
		}
		while (runningThreads.get() > 0) {
		    System.out.println("There is " + runningThreads.get() + " pinging threads left");
		    try {
			Thread.sleep(2000);
		    } catch (InterruptedException e) {
			e.printStackTrace();
		    }
		}
		System.out.println("Pinging completed\n\n");
	    }

	    private boolean ipReachable(final String ip) {
		System.out.println("\tPingining " + ip);
		try {
		    Process ping = Runtime.getRuntime().exec(pingCommand + ip);
		    ping.waitFor();
		    if (ping.exitValue() != 0) {
			System.out.println("\t" + ip + " is not reachable");
			return false;
		    }
		    System.out.println("\t" + ip + " is reachable");
		    return true;
		} catch (IOException | InterruptedException e) {
		    System.out.println("Error while pinging " + ip);
		    e.printStackTrace();
		    System.exit(1);
		}
		return false;
	    }
	}, 0, pingInterval * 1000);
    }
}
