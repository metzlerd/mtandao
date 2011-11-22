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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.mtandao.handle.Handler;

/**
 * @author metzler
 *
 */
public abstract class StreamCrawler {

	// logging
	private static final Logger LOGGER = LoggerFactory.getLogger(StreamCrawler.class);

	// HTTP "STATUS OK" code
	private static final int HTTP_STATUS_OK = 200;

	// default handler class
	private static final String DEFAULT_HANDLER = "edu.isi.mtandao.handle.StdoutHandler";

	// Twitter API URL to verify a user credentials
	private static final String VERIFY_CREDENTIALS_URL = "https://api.twitter.com/1/account/verify_credentials.json";

	// set up command line options
	protected static Options CLI_OPTIONS = new Options();

	static {
		// crawler class
		CLI_OPTIONS.addOption("crawlerClass", true, "Crawler class name.");

		// api key, secret options
		CLI_OPTIONS.addOption("apiKey", true, "Twitter API key.");
		CLI_OPTIONS.addOption("apiSecret", true, "Twitter API secret.");

		// access token key, secret options
		CLI_OPTIONS.addOption("accessToken", true, "Twitter access token.");
		CLI_OPTIONS.addOption("accessSecret", true, "Twitter access token secret.");

		// handler options
		CLI_OPTIONS.addOption("handlerClass", true, "Handler class name (optional).");
		CLI_OPTIONS.addOption("handlerArgs", true, "Handler argument string (optional).");		
	}

	// Twitter OAuth service
	private OAuthService mService = null;

	// handler
	private Handler mHandler = null;

	// api key and secret
	private String mApiKey = null;
	private String mApiSecret = null;

	// access token
	private Token mAccessToken = null;

	public StreamCrawler() {
		/* do nothing */
	}

	protected void initialize(CommandLine cmd) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		// api key and secret
		mApiKey = cmd.getOptionValue("apiKey");
		mApiSecret = cmd.getOptionValue("apiSecret");

		// access token and secret
		String accessToken = cmd.getOptionValue("accessToken");
		String accessSecret = cmd.getOptionValue("accessSecret");

		// access token
		mAccessToken = new Token(accessToken, accessSecret);

		// handler class name and argument string
		String handlerClass = cmd.getOptionValue("handlerClass");
		String handlerArgs = cmd.getOptionValue("handlerArgs");

		// check the command line arguments
		if(mApiKey == null || mApiSecret == null || accessToken == null || accessSecret == null) {
			throw new IllegalArgumentException("Missing required arguments!");
		}

		// initialize handler
		if(handlerClass == null || handlerArgs == null) {
			LOGGER.warn("No handler class and/or arguments specified. Using defaults.");

			handlerClass = DEFAULT_HANDLER;
			handlerArgs = "";
		}

		mHandler = Handler.createHandler(handlerClass, handlerArgs);
	}

	// crawl the Twitter Streaming API
	public void crawl() {
		// establish OAuth service
		establishService();

		// verify provided account credentials
		if(!verifyCredentials()) {
			throw new RuntimeException("Unable to verify credentials -- check your access token and secret.");
		}

		// process tweets from the stream
		processStream();
	}

	// establish the Twitter OAuth service
	private void establishService() {
		mService = new ServiceBuilder().provider(TwitterApi.class).apiKey(mApiKey).apiSecret(mApiSecret).build();
	}

	// verify user credentials
	private boolean verifyCredentials() {
		// create the OAuth signed request to verify our credentials with the Twitter API
		OAuthRequest request = new OAuthRequest(Verb.GET, VERIFY_CREDENTIALS_URL);
		mService.signRequest(mAccessToken, request);
		Response response = request.send();

		// check response code
		if(response.getCode() != HTTP_STATUS_OK) {
			return false;
		}

		return true;
	}

	// create the OAuth signed request to access the Twitter Streaming API
	protected OAuthRequest getOAuthRequest(Verb verb, String url, boolean keepAlive) {
		OAuthRequest request = new OAuthRequest(verb, url);
		request.setConnectionKeepAlive(keepAlive);
		mService.signRequest(mAccessToken, request);
		return request;
	}

	// process (indefinitely) from Twitter Streaming API
	protected void processStream() {
		// current status code
		int status = HTTP_STATUS_OK;

		// current delay (if any)
		int delay = 0;

		while(true) { // purposeful infinite loop
			if(status > HTTP_STATUS_OK) {
				LOGGER.warn("The Twitter API returned the following HTTP status code: " + status);
				delay = (delay == 0) ? 10000 : Math.min(240000, 2*delay); // per twitter's suggestion
			}

			if(delay != 0) {
				LOGGER.warn("Waiting " + delay + " milliseconds before reconnecting...");
				try {
					Thread.sleep(delay);
				}
				catch(InterruptedException e) {
					/* do nothing */
				}
			}

			// create the OAuth signed request to access the Twitter Streaming API
			String requestURL = getRequestURL();
			LOGGER.info("Sending request: " + requestURL);
			OAuthRequest request = getOAuthRequest(Verb.GET, requestURL, true);
			Response response = request.send();

			status = response.getCode();
			if(status != HTTP_STATUS_OK) {
				continue;
			}

			// read from the stream
			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getStream()));

			try {
				String line;
				while((line = reader.readLine()) != null) {
					if(!line.isEmpty()) {
						mHandler.handle(line);
					}
				}
			}
			catch(IOException e) {
				LOGGER.warn("An IOException was caught. Details: " + status);
				delay = (delay == 0) ? 250 : Math.min(16000, delay + 250); // per twitter's suggestion
			}

			// close the stream
			try {
				reader.close();
			}
			catch(IOException e) {
				// do nothing
			}
		}
	}

	// returns the Twitter API URL that will be processed as a stream
	protected abstract String getRequestURL();

	// generic main method that can be used by all stream crawlers
	public static void main(String [] args) throws ParseException, InstantiationException, IllegalAccessException, ClassNotFoundException {		
		// extract command line arguments
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(CLI_OPTIONS, args);

		// get crawler class
		String crawlerClass = cmd.getOptionValue("crawlerClass");
		if(crawlerClass == null) {
			printUsage(CLI_OPTIONS);
		}

		// instantiate and initialize the crawler
		StreamCrawler crawler = (StreamCrawler)Class.forName(crawlerClass).newInstance();

		try {
			crawler.initialize(cmd);
		}
		catch(Exception e) {
			printUsage(CLI_OPTIONS);
		}

		// start crawling
		crawler.crawl();
	}

	private static void printUsage(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("StreamCrawler", options);
		System.exit(-1);
	}

}
