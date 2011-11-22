/*
 * Mtandao: A Social Media Toolkit
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.isi.mtandao.twitter;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author metzler
 *
 */
public class Tweet {

	// date parser
	private static final SimpleDateFormat DATE_PARSER = new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy");

	// tweet id
	private long mTweetId;

	// user id
	private String mUserScreenName;

	// user location (text)
	private String mUserLocation;

	// user location (geo-based)
	private double mUserLongitude;
	private double mUserLatitude;

	// text of tweet
	private String mText;

	// timestamp
	private long mTimestamp;

	// is this a retweet?
	private boolean mIsRetweet;

	// re-usable string buffer
	private final StringBuffer mBuffer = new StringBuffer();

	// create a tweet object from a json object
	public Tweet(String jsonText) throws JSONException {
		JSONObject jobj = new JSONObject(jsonText);

		// get tweet id 
		if(jobj.has("id")) {
			mTweetId = jobj.getLong("id");
		}
		else {
			mTweetId = 0;
		}

		// get tweet text
		if(jobj.has("text")) {
			mText = jobj.getString("text");
		}
		else {
			mText = "";
		}

		// get tweet timestamp
		if(jobj.has("created_at")) {
			try {
				mTimestamp = DATE_PARSER.parse(jobj.getString("created_at")).getTime();
			}
			catch (ParseException e) {
				mTimestamp = 0L;
			}
		}
		else {
			mTimestamp = 0L;
		}

		// is this a retweet?
		if(jobj.has("retweeted")) {
			mIsRetweet = jobj.getBoolean("retweeted") || (mText != null && mText.toLowerCase().startsWith("rt "));
		}
		else {
			mIsRetweet = false;
		}

		// get user information
		if(jobj.has("user")) {
			JSONObject userinfo = jobj.getJSONObject("user");
			if(userinfo.has("screen_name")) {
				mUserScreenName = userinfo.getString("screen_name");
			}
			if(userinfo.has("location")) {
				mUserLocation = userinfo.getString("location");
			}
			else {
				mUserLocation = "";
			}
		}

		// get geo information
		if(jobj.has("geo")) {
			try {
				JSONObject geoinfo = jobj.getJSONObject("geo");
				if(geoinfo.has("type") && geoinfo.getString("type").equals("Point") && geoinfo.has("coordinates")) {
					JSONArray coords = geoinfo.getJSONArray("coordinates");
					mUserLongitude = coords.getDouble(0);
					mUserLatitude = coords.getDouble(1);
				}
			}
			catch(JSONException e) {
				mUserLongitude = 0.0;
				mUserLatitude = 0.0;
			}
		}
	}

	public String getContent() {
		return mText;
	}

	public String getId() {
		return Long.toString(mTweetId);
	}

	public String getUserScreenName() {
		return mUserScreenName;
	}

	public long getTimestamp() {
		return mTimestamp;
	}

	public String getUserLocation() {
		return mUserLocation;
	}

	public boolean isRetweet() {
		return mIsRetweet;
	}

	public String toIndriDocument() {
		mBuffer.setLength(0);

		mBuffer.append("<DOC>");

		mBuffer.append("<DOCNO>");
		mBuffer.append(getId());
		mBuffer.append("</DOCNO>");

		mBuffer.append("<TIME>");
		mBuffer.append(getTimestamp());
		mBuffer.append("</TIME>");

		if(mUserScreenName != null && !mUserScreenName.equals("")) {
			mBuffer.append("<USER>");
			mBuffer.append(mUserScreenName);
			mBuffer.append("</USER>");
		}

		if(mUserLocation != null && !mUserLocation.equals("")) {
			mBuffer.append("<LOCATION>");
			mBuffer.append(mUserLocation);
			mBuffer.append("</LOCATION>");
		}

		if(mUserLongitude != 0.0 || mUserLatitude != 0.0) {
			mBuffer.append("<LONGITUDE>");
			mBuffer.append(mUserLongitude);
			mBuffer.append("</LONGITUDE>");

			mBuffer.append("<LATITUDE>");
			mBuffer.append(mUserLatitude);
			mBuffer.append("</LATITUDE>");
		}

		if(mText != null && !mText.equals("")) {
			mBuffer.append("<TEXT>");
			mBuffer.append(mText);
			mBuffer.append("</TEXT>");
		}

		mBuffer.append("</DOC>");

		return mBuffer.toString();
	}
}
