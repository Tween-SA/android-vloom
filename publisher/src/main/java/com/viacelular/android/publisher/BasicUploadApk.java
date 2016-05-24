/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.viacelular.android.publisher;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.FileContent;
import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits.Apks.Upload;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits.Commit;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits.Insert;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits.Tracks.Update;
import com.google.api.services.androidpublisher.model.Apk;
import com.google.api.services.androidpublisher.model.AppEdit;
import com.google.api.services.androidpublisher.model.Track;
import com.viacelular.utils.ReadPropFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

/**
 * Uploads an apk to the alpha track.
 */
public class BasicUploadApk
{
	private static final Log log = LogFactory.getLog(BasicUploadApk.class);
	private String versionNumber;

	public void init(String configFilePath, String versionNumber)
	{
		this.versionNumber = versionNumber;
		log.info("Attempting to publish new APK with versionName: " + versionNumber);

		try
		{
			initializeConfiguration(configFilePath);
		}
		catch(IOException e)
		{
			log.error(e.getMessage());
		}

		publishAPKToGooglePlay();
	}


	private void publishAPKToGooglePlay()
	{
		try
		{
			// Create the API service.
			AndroidPublisher service = AndroidPublisherHelper.init(
					ApplicationConfig.APPLICATION_NAME,
					ApplicationConfig.SERVICE_ACCOUNT_EMAIL);

			final Edits edits = service.edits();

			// Create a new edit to make changes to your listing.
			Insert editRequest = edits.insert(ApplicationConfig.PACKAGE_NAME, null /** no content */);
			AppEdit edit = editRequest.execute();
			final String editId = edit.getId();
			log.info(String.format("Created edit with id: %s", editId));

			// Upload new apk to developer console
			final String apkPath = BasicUploadApk.class
					.getResource(ApplicationConfig.APK_FILE_PATH)
					.toURI().getPath();
			final AbstractInputStreamContent apkFile = new FileContent(AndroidPublisherHelper.MIME_TYPE_APK, new File(apkPath));
			Upload uploadRequest = edits
					.apks()
					.upload(ApplicationConfig.PACKAGE_NAME,
							editId,
							apkFile);
			try
			{
				Apk apk = uploadRequest.execute();
				log.info(String.format("Version code %d has been uploaded", apk.getVersionCode()));

				// Assign apk to track.
				List<Integer> apkVersionCodes = new ArrayList<>();
				apkVersionCodes.add(apk.getVersionCode());
				Update updateTrackRequest = edits
						.tracks()
						.update(ApplicationConfig.PACKAGE_NAME,
								editId,
								ApplicationConfig.TRACK,
								new Track().setVersionCodes(apkVersionCodes));
				Track updatedTrack = updateTrackRequest.execute();
				log.info(String.format("Track %s has been updated.", updatedTrack.getTrack()));

				// Commit changes for edit.
				Commit commitRequest = edits.commit(ApplicationConfig.PACKAGE_NAME, editId);
				AppEdit appEdit = commitRequest.execute();
				log.info(String.format("App edit with id %s has been comitted", appEdit.getId()));
			}
			catch(GoogleJsonResponseException ge)
			{
				log.error(ge.getMessage());
			}

		}
		catch(IOException | URISyntaxException | GeneralSecurityException ex)
		{
			log.error("Excpetion was thrown while uploading apk to alpha track", ex);
		}
	}

	/**
	 * Parse the configuration file with the corresponding keys.
	 *
	 * @param String the file name and path to be loaded.
	 */
	private void initializeConfiguration(String file) throws IOException
	{
		log.info("Android App Publisher | Config file path received: " + file);

		final ReadPropFile rpf = new ReadPropFile(file);

		ApplicationConfig.APPLICATION_NAME = rpf.getKey("applicationName") + "/" + versionNumber;
		ApplicationConfig.PACKAGE_NAME = rpf.getKey("packageName");
		ApplicationConfig.SERVICE_ACCOUNT_EMAIL = rpf.getKey("serviceAccountEmail");
		ApplicationConfig.TRACK = rpf.getKey("track");
		ApplicationConfig.APK_FILE_PATH = rpf.getKey("apkFilePath");

		Preconditions.checkArgument(!Strings.isNullOrEmpty(ApplicationConfig.PACKAGE_NAME), "ApplicationConfig.PACKAGE_NAME cannot be null or empty!");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(ApplicationConfig.APPLICATION_NAME), "ApplicationConfig.APPLICATION_NAME cannot be null or empty!");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(ApplicationConfig.APK_FILE_PATH), "ApplicationConfig.APK_FILE_PATH cannot be null or empty!");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(ApplicationConfig.TRACK), "ApplicationConfig.TRACK cannot be null or empty!");
	}
}