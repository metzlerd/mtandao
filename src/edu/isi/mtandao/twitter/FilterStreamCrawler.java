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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.scribe.utils.URLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author metzler
 *
 */
public class FilterStreamCrawler extends StreamCrawler {

	// logging
	private static final Logger LOGGER = LoggerFactory.getLogger(FilterStreamCrawler.class);

	// Twitter Streaming API URL
	private static final String FILTER_URL = "https://stream.twitter.com/1/statuses/filter.json";

	static {
		// additional command line arguments
		CLI_OPTIONS.addOption("followArgs", true, "Follow arguments.");
		CLI_OPTIONS.addOption("locationsArgs", true, "Location arguments.");
		CLI_OPTIONS.addOption("trackArgs", true, "Track arguments.");
	}
	
	// filter parameter map
	private final Map<String, String> mParams = new HashMap<String, String>();

	public FilterStreamCrawler() {
		super();

		// clear params
		mParams.clear();
	}

	@Override
	protected void initialize(CommandLine cmd) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		super.initialize(cmd);

		// set up param map options ("follow", "locations", "track")
		String followArgs = cmd.getOptionValue("followArgs");
		if(followArgs != null) {
			mParams.put("follow", followArgs);
		}

		String locationsArgs = cmd.getOptionValue("locationsArgs");
		if(locationsArgs != null) {
			mParams.put("locations", locationsArgs);
		}

		String trackArgs = cmd.getOptionValue("trackArgs");
		if(trackArgs != null) {
			mParams.put("track", trackArgs);
		}
		
		if(mParams.size() == 0) {
			LOGGER.error("Must specify at least one type of argument (followArgs, locationsArgs, or trackArgs) for FilterStreamCrawler!");
			throw new RuntimeException("Must specify at least one type of argument for FilterStreamCrawler!");
		}
	}

	@Override
	protected String getRequestURL() {
		return URLUtils.appendParametersToQueryString(FILTER_URL, mParams);
	}
}
