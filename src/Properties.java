/**
 * 
 */
package aho.program.dynhosted;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Adam Hlavacek
 *         Loads data from properties file
 */
public class Properties {
    private File langFile; // File where are properties saved

    public final List<String> propertiesNames = new ArrayList<String>();
    private final List<String> propertiesValues = new ArrayList<String>();
    private final List<File> usedImports = new ArrayList<File>();

    private File untranslatedFile; // Where to store Strings that have to be translated

    /*
     * Used when loading file in private void loadFile(final File file)
     */
    private final char FINDING_NAME = 0;
    private final char INSIDE_NAME = 1;
    private final char FINDING_VALUE = 2;
    private final char INSIDE_VALUE = 3;

    /**
     * Reads specified file with language specific Strings. If file does not contains property langCode throws
     * exception.
     * 
     * @param langFile
     *            File with language specific Strings
     * @throws Exception
     */
    public Properties(final File langFile) throws Exception {

	this.langFile = langFile;
	usedImports.add(langFile);
	loadFile(langFile);


	untranslatedFile = this.langFile;
    }

    /**
     * Tries to load property named key in loaded langFile's lists. If that property is not found, prints
     * error into process's error output stream and returns back key itself.
     * 
     * @param key
     *            Key supposed to be load from language file
     * @return Found language specified representation of key if key is found or key itself if key is not
     *         found in language file
     */
    public String getString(String key) {
	for (int i = 0; i < propertiesNames.size(); i++)
	    if (propertiesNames.get(i).contentEquals(key))
		return propertiesValues.get(i);
	System.err.println("Key '" + key + "' does not exist for language '"
		+ "' in language file '" + langFile.getAbsolutePath() + "'. Saving it into "
		+ untranslatedFile.getAbsolutePath());
	try {
	    saveNewKey(key, key, untranslatedFile);
	    saveNewImport(untranslatedFile);
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return key;
    }

    /**
     * Goes through list's of knows properties name's and search for key
     * 
     * @param key
     *            Key to be found
     * @return true if key was found inside name's list
     */
    public boolean keyExist(final String key) {
	for (int i = 0; i < propertiesNames.size(); i++)
	    if (propertiesNames.get(i).contentEquals(key))
		return true;
	return false;
    }

    /**
     * Finds out if import file has been used
     * 
     * @param importFile
     *            File to check if used
     * @return true if importFile is found in list, false otherwise
     */
    public boolean importUsed(final File importFile) {
	for (File file : usedImports)
	    if (file.getAbsolutePath().contentEquals(importFile.getAbsolutePath()))
		return true;
	return false;
    }

    /**
     * Goes through list's of knows properties name's, when target is found, assigns new value by using
     * newValue.toString()
     * If key does not exist, adds it automatically
     * 
     * @param key
     *            Key to change
     * @param newValue
     *            New Object to save. To save object is used newValue.toString()
     * @return false if searching for key fails
     */
    public boolean changeValue(final String key, final Object newValue) {
	return (changeValue(key, newValue, false));
    }

    /**
     * Goes through list's of knows properties name's, when target is found, assigns new value by using
     * newValue.toString()
     * 
     * @param key
     *            Key to change
     * @param newValue
     *            New Object to save. To save object is used newValue.toString()
     * @param disableKeyAdd
     *            if set to true disable automatic adding of new key which not already exist
     * @return false if searching for key fails
     */
    public boolean changeValue(final String key, final Object newValue, final boolean disableKeyAdd) {
	for (int i = 0; i < propertiesNames.size(); i++)
	    if (propertiesNames.get(i).contentEquals(key)) {
		propertiesValues.set(i, newValue.toString());
		return true;
	    }
	if (!disableKeyAdd)
	    return addValue(key, newValue);
	return false;
    }

    /**
     * Adds new key and its value to lists
     * 
     * @param key
     *            Key to add
     * @param value
     *            Key's value. Saved by using value.toString()
     * @return false if key already exists
     */
    public boolean addValue(final String key, final Object value) {
	if (keyExist(key))
	    return false;
	propertiesNames.add(key);
	propertiesValues.add(value.toString());
	return true;
    }

    /**
     * Saves new value into File file. If file does not exist, creates it.
     * 
     * @param key
     *            Key to be saved
     * @param o
     *            Value of key to be saved
     * @param file
     *            File where to save the key
     * @throws IOException
     *             if error in reading file or file is a directory
     */
    public void saveNewKey(final String key, final Object o, final File file) throws IOException {
	if (file.isDirectory())
	    throw new IOException("Specified file is a directory!");
	if (!file.exists())
	    file.createNewFile();
	FileWriter fw = new FileWriter(file, true);
	BufferedWriter buf = new BufferedWriter(fw);
	PrintWriter out = new PrintWriter(buf);
	out.println("{" + key + "}={" + o.toString() + "}");

	buf.close();
	fw.close();
    }

    /**
     * Append to language file command telling to use new file
     * 
     * @param file
     *            File to start using.
     *            If file does not exist, creates it
     * @throws IOException
     */
    public void saveNewImport(final File file) throws IOException {
	if (importUsed(file))
	    return;
	if (!file.exists())
	    file.createNewFile();
	FileWriter fw = new FileWriter(langFile, true);
	BufferedWriter buf = new BufferedWriter(fw);
	PrintWriter out = new PrintWriter(buf);

	usedImports.add(file);

	String importString = file.getName();

	out.println("import " + importString);

	buf.close();
	fw.close();
    }

    /**
     * Saves all properties to a file
     * WARNING: Saves only properties, all commentary is removed
     * WARNING: Properties from include all also saved
     * 
     * @param file
     *            File where to save all properties
     * @throws IOException
     *             if error in reading file or file is a directory
     */
    public void saveAll2File(final File file) throws IOException {
	if (file.isDirectory())
	    throw new IOException("Specified file is a directory!");
	if (!file.exists())
	    file.createNewFile();
	FileWriter fw = new FileWriter(file, true);
	BufferedWriter buf = new BufferedWriter(fw);
	PrintWriter out = new PrintWriter(buf);
	for (int i = 0; i < propertiesNames.size(); i++)
	    out.println("{" + propertiesNames.get(i) + "}={" + propertiesValues.get(i) + "}");

	buf.close();
	fw.close();
    }

    /**
     * 
     * Reads all lines from file
     * If line starts with ' ' (space) and is not inside properties name or value, remove spaces until some
     * character or adds blank line.
     * "import filename.lang" says to load another file with language and adds its properties into current
     * list.
     * If no filename is specified, prints error to System.err
     * "#" at start of line means commentary
     * If line starts with "{" and is not inside properties name or value it means we entered into properties
     * name
     * 
     * @param file
     * @throws IOException
     */
    private void loadFile(final File file) throws IOException {
	if (!file.exists()) {
	    System.err.println("Language file " + file.getAbsolutePath() + " does not exist!");
	    return;
	}
	FileReader fr = new FileReader(file);
	BufferedReader buf = new BufferedReader(fr);

	String line = null;

	/*
	 * Here are saved all characters before adding into right list
	 */
	StringBuilder blockContent = new StringBuilder();

	/*
	 * This char told us if we are reading name, value, nothing or after name finding value start
	 */
	char currentPossition = FINDING_NAME;

	while ((line = buf.readLine()) != null) {

	    if ((currentPossition == FINDING_NAME) || (currentPossition == FINDING_VALUE)) {
		if (line.startsWith(" ")) {
		    boolean blankLine = false;
		    while (line.charAt(0) == ' ') {
			/*
			 * If line has length == 1 and first character is ' ' it means, that it is blank line.
			 * So,
			 * we will not add this nowhere
			 */
			if (line.length() == 1) {
			    blankLine = true;
			    break;
			}
			/*
			 * If this space is not line's last character, we will substring it out, so line will
			 * start by current second character
			 */
			line = line.substring(1);
		    }
		    if (blankLine)
			continue;
		}
		if (line.startsWith("#")) // That means line is a commentary
		    continue;

		// This told us to load another language file and add its content here
		if ((line.startsWith("import"))) {
		    if (line.length() < 8) { // "import " has 7 characters. Wee need at least 6 for filename
			System.err.println("Warning: File " + file.getAbsolutePath()
				+ " uses 'import' paramether withou specifing a file!");
			continue;
		    }
		    File importFile = new File(langFile.getAbsolutePath().substring(0,
			    langFile.getAbsolutePath().lastIndexOf("/") + 1)
			    + line.replace("import ", ""));
		    if (!importUsed(importFile)) {
			usedImports.add(importFile);
			loadFile(importFile);
		    }

		    continue;
		}
	    }

	    /*
	     * When we are reading text and we jump onto new line, add this character into block content
	     */
	    if ((currentPossition == INSIDE_NAME) || (currentPossition == INSIDE_VALUE))
		blockContent.append("\n");

	    char[] characters = line.toCharArray();
	    for (int i = 0; i < characters.length; i++) {
		if (((i - 1) >= 0) && (characters[i - 1] == '\\')) {
		    // That told us to do not listen for operators this turn
		    continue;
		}
		if (characters[i] == '{') {
		    /*
		     * If we found this character and we are not reading text we have to start reading
		     */
		    if (currentPossition == FINDING_NAME) {
			currentPossition = INSIDE_NAME;
			continue;
		    }
		    if (currentPossition == FINDING_VALUE) {
			currentPossition = INSIDE_VALUE;
			continue;
		    }

		}

		if (characters[i] == '}') {
		    /*
		     * If we found this character and we are reading text we have to stop reading and start
		     * finding another block start and saves its content into right list and then delete it
		     * from StringBuilder blockContent
		     */
		    if ((currentPossition == INSIDE_NAME)) {
			propertiesNames.add(blockContent.toString());
			currentPossition = FINDING_VALUE;
		    }
		    if (currentPossition == INSIDE_VALUE) {
			propertiesValues.add(blockContent.toString());
			currentPossition = FINDING_NAME;
		    }

		    blockContent.setLength(0);
		    continue;
		}

		/*
		 * If we are reading name or value. So we will append character
		 * into text.
		 */
		if ((currentPossition == INSIDE_NAME) || (currentPossition == INSIDE_VALUE))
		    blockContent.append(characters[i]);
	    }

	}

	/*
	 * If we do not have same size of names and variables -> prints warning into error stream
	 */
	if (propertiesNames.size() != propertiesValues.size())
	    System.err.println("Error with checksum in file " + file.getAbsolutePath());

	buf.close();
	fr.close();
    }
}
