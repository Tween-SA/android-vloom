package com.viacelular.android.publisher;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.api.client.repackaged.com.google.common.base.Strings;

public class Main
{
	public static void main(String[] args)
	{
		String configFilePath = args[0];
		Preconditions.checkArgument(!Strings.isNullOrEmpty(configFilePath), "You need to pass the path to the configuration file as the first argument to the program!");

		String versionNumber = args[1];
		Preconditions.checkArgument(!Strings.isNullOrEmpty(versionNumber), "You need to pass a version number as the second argument to the program!");

		BasicUploadApk publisher = new BasicUploadApk();
		publisher.init(configFilePath, versionNumber);
	}
}