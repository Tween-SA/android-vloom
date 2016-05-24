package com.viacelular.utils;

/**
 *
 * @author pogui
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;

public final class ReadPropFile
{
	private Properties properties = new Properties();

	public ReadPropFile(String filename) throws IOException
	{
		try
		{
			File file = new File(filename);
			try (FileInputStream fileInput = new FileInputStream(file))
			{
				properties.load(fileInput);
			}
		}
		catch (FileNotFoundException e)
		{
			System.out.println("ReadPropFile|" + "Error " + Arrays.toString(e.getStackTrace()));
			Runtime.getRuntime().exit(0);
		}
	}

	public void showKeys()
	{
		Enumeration<Object> enuKeys = properties.keys();
		while (enuKeys.hasMoreElements())
		{
			String key = (String) enuKeys.nextElement();
			String value = properties.getProperty(key);
		}
	}

	public String getKey(String key)
	{
		return properties.getProperty(key);
	}
}