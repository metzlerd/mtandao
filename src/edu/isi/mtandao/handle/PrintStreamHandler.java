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

package edu.isi.mtandao.handle;

import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author metzler
 *
 */
public abstract class PrintStreamHandler extends Handler {

	// logging
	private static final Logger LOGGER = LoggerFactory.getLogger(PrintStreamHandler.class);
	
	// number of records to handle before printing a status update
	private static final int RECORDS_PER_UPDATE = 10000;
	
	// total number of records processed by this handler
	private long mRecordsProcessed;
	
	// print stream
	protected PrintStream mStream = null;
	
	public PrintStreamHandler() {
		super();

		// set the number of records processed to 0
		mRecordsProcessed = 0;
	}
	
	/* (non-Javadoc)
	 * @see edu.isi.mtandao.output.Handler#handle(java.lang.String)
	 */
	@Override
	public void handle(String record) {
		// show update
		if(++mRecordsProcessed % RECORDS_PER_UPDATE == 0) {
			LOGGER.info("Records handled: " + mRecordsProcessed);
		}

		// write the record to the print stream
		mStream.println(record);
	}

}
