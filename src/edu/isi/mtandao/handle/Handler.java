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

/**
 * @author metzler
 *
 */
public abstract class Handler {

	// instantiate a new handler from a class name and argument string
	public static Handler createHandler(String className, String args) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		Handler handler = (Handler)Class.forName(className).newInstance();
		handler.initialize(args);
		return handler;
	}

	// create a new handler
	public Handler() {
		// do nothing
	}
	
	// initialize the handler
	public abstract void initialize(String args);
	
	// handle a record
	public abstract void handle(String record);
}
