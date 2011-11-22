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

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.mtandao.twitter.Tweet;

import lemurproject.indri.IndexEnvironment;
import lemurproject.indri.QueryEnvironment;

/**
 * @author metzler
 *
 */
public class IndriHandler extends Handler {

	// logging
	private static final Logger LOGGER = LoggerFactory.getLogger(IndriHandler.class);

	// indri jni library name
	private static final String LIB_NAME = "lemur";	

	// default amount of memory to allocate to index environment
	private static final long DEFAULT_MEMORY = 1024*1024*1024;

	// indri file class to use to index documents
	private static final String FILE_CLASS = "trectext";

	// indexed fields
	private static final String [] INDEXED_FIELDS = { "text" };

	// metadata fields
	private static final String [] METADATA_FORWARD_FIELDS = { "docno", "time" };
	private static final String [] METADATA_BACKWARD_FIELDS = { "docno", "time" };
		
	// numeric fields
	private static final String [] NUMERIC_FIELDS = { "time", "longitude", "latitude" };

	// metadata map
	private final Map<String,String> mMetadata = new HashMap<String,String>();

	// indri index environment
	private IndexEnvironment mIndex = null;

	// indri query environment
	private QueryEnvironment mQuery = null;

	public IndriHandler() {
		super();

		// initialize indri jni library
		System.loadLibrary(LIB_NAME);

		// get a new indri index environment
		mIndex = new IndexEnvironment();

		// get an indri query environment
		mQuery = new QueryEnvironment();		
	}

	@Override
	public void finalize() {
		// close the query environment
		try {
			if(mQuery != null) {
				mQuery.close();
			}
		}
		catch(Exception e) {
			throw new RuntimeException("Error closing QueryEnvironment -- " + e);
		}

		// close the index environment
		try {
			if(mIndex != null) {
				mIndex.close();
			}
		}
		catch(Exception e) {
			throw new RuntimeException("Error closing IndexEnvironment -- " + e);
		}
	}

	/* (non-Javadoc)
	 * @see edu.isi.mtandao.handle.Handler#initialize(java.lang.String)
	 */
	@Override
	public void initialize(String args) {
		try {
			// create a new index
			mIndex.create(args);

			// initialize the index
			mIndex.setMemory(DEFAULT_MEMORY);
			mIndex.setIndexedFields(INDEXED_FIELDS);
			mIndex.setMetadataIndexedFields(METADATA_FORWARD_FIELDS, METADATA_BACKWARD_FIELDS);
			for(String field : NUMERIC_FIELDS) {
				mIndex.setNumericField(field, true);
			}
			mIndex.setStoreDocs(true);

			// tie the query environment to this index
			mQuery.addIndex(mIndex);
		}
		catch(Exception e) {
			throw new RuntimeException("Error initializing IndriHandler -- " + e);
		}
	}

	/* (non-Javadoc)
	 * @see edu.isi.mtandao.handle.Handler#handle(java.lang.String)
	 */
	@Override
	public void handle(String record) {
		Tweet t = null;
		try {
			t = new Tweet(record);
		}
		catch(JSONException e) {
			LOGGER.warn("IndriHandler encountered a malformed JSON object -- skipping!");
			return;
		}

		try {
			mMetadata.put("docno", t.getId());
			mMetadata.put("time", Long.toString(t.getTimestamp()));
			mIndex.addString(t.toIndriDocument(), FILE_CLASS, mMetadata);
		}
		catch(Exception e) {
			LOGGER.warn("An Exception was encountered in IndriHandler -- " + e);
		}
	}

}
