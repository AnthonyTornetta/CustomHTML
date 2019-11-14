package com.cornchipss.chtml.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class DirectoryUtils
{
	private static String relativeDir;
	private static String saveTo;
	
	/**
	 * Finds the absolute path of a directory that is relative<br>
	 * For example: "/index.html" if the absolute path was "C:/asdf/stuff/" would return "C:/asdf/stuff/index.html"
	 * @param dir The directory to get the absolute directory of
	 * @return The absolute path of a directory that is relative
	 */
	public static String calculateRelativeDir(String dir)
	{
		if(dir.charAt(0) == '/')
		{
			if(relativeDir.charAt(relativeDir.length() - 1) == '/')
				relativeDir = relativeDir.substring(0, relativeDir.length() - 1);

			dir = relativeDir + dir;
		}
		else
		{
			if(relativeDir.charAt(relativeDir.length() - 1) != '/')
				relativeDir += "/";
			dir = relativeDir + dir;
		}

		return dir;
	}
	
	/**
	 * Gets the full path to save the new version of the file to
	 * @param f The old file to make a new version of
	 * @return The path to the new file
	 */
	public static String getSaveToPath(File f)
	{
		// Checks if a slash should be added to the end of the directory
		String slashOrNoSlash = (saveTo.endsWith("/") || saveTo.endsWith("\\")) ? "" : "/";

		String filesPath = f.getPath();

		// Finds the index of where to place the new path
		int x = 0;
		while(x < filesPath.length() && relativeDir.indexOf(filesPath.substring(0, x + 1)) == 0)
			x++;

		String howToSave = filesPath.substring(x);

		String saveToPath = saveTo + slashOrNoSlash + howToSave;

		// Makes sure the directory actually exists
		String directoriesPath = saveToPath.substring(0, saveToPath.lastIndexOf("\\") + 1);

		File pathTo = new File(directoriesPath);
		pathTo.mkdirs(); // Creates the file's directory if it doesn't exist

		return saveToPath;
	}
	
	/**
	 * Saves a new file to the new directory with the new lines
	 * @param f The old file to get the name of
	 * @param newLines The lines to put in the new file
	 * @throws IOException If there is some sort of saving error
	 */
	public static void saveToCompiledDir(File f, String newLines) throws IOException
	{
		File newFile = new File(getSaveToPath(f));
		newFile.createNewFile();

		BufferedWriter bw = new BufferedWriter(new FileWriter(newFile));
		bw.write(newLines);
		bw.close();
	}
	
	/**
	 * Copies the old file to the new file's location
	 * @param f The file to copy
	 * @throws IOException If there is an error finding the new path or copying
	 */
	public static void copyFileToNewDir(File f) throws IOException
	{
		Files.copy(f.toPath(), FileSystems.getDefault().getPath(getSaveToPath(f)), StandardCopyOption.REPLACE_EXISTING);
	}

	public static String getRelativeDir() { return relativeDir; }
	public static void setRelativeDir(String relativeDir) { DirectoryUtils.relativeDir = relativeDir; }

	public static String getSaveTo() { return saveTo; }
	public static void setSaveTo(String saveTo) { DirectoryUtils.saveTo = saveTo; }
}
