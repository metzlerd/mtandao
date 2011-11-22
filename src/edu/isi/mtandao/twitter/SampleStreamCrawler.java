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

import org.apache.commons.cli.CommandLine;

/**
 * @author metzler
 *
 */
public class SampleStreamCrawler extends StreamCrawler {

	// Twitter Streaming API URL
	private static final String SAMPLE_URL = "https://stream.twitter.com/1/statuses/sample.json";

	public SampleStreamCrawler() {
		super();
	}
	
	@Override
	protected void initialize(CommandLine cmd) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		super.initialize(cmd);
	}

	@Override
	protected String getRequestURL() {
		return SAMPLE_URL;
	}
}
