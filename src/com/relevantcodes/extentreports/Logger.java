/*
Copyright 2015 ExtentReports committer(s)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0
	
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/


package com.relevantcodes.extentreports;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.relevantcodes.extentreports.markup.*;
import com.relevantcodes.extentreports.support.*;

class Logger extends AbstractLog {
	// HTML report file path
	private String filePath;
	
	// package where markup files are created 
	private String packagePath = "com/relevantcodes/extentreports/markup/";
	
	// default DisplayOrder = OLDEST tests first, followed by NEWEST
	private DisplayOrder testDisplayOrder = DisplayOrder.BY_OLDEST_TO_LATEST;
	
	@Override
	protected void log() {
		String statusIcon = FontAwesomeIco.get(logStatus);
		
		if (screenCapturePath != "") {
			String img = MarkupFlag.img(screenCapturePath);
			
			if (screenCapturePath.indexOf("http") == 0 || screenCapturePath.indexOf(".") == 0) {
				img = img.replace("file:///", "");
			}
			
			details += img;
			screenCapturePath = "";
		}
		
		String markup = FileReaderEx.readAllText(filePath)
				.replace(MarkupFlag.get("step"), Resources.getText(packagePath + "step.txt") + MarkupFlag.get("step"))
				.replace(MarkupFlag.get("timestamp"), new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()))
				.replace(MarkupFlag.get("stepstatus"), logStatus.toString().toLowerCase())
				.replace(MarkupFlag.get("stepstatusu"), logStatus.toString().toUpperCase())
				.replace(MarkupFlag.get("statusicon"), statusIcon)
				.replace(MarkupFlag.get("stepname"), stepName)
				.replace(MarkupFlag.get("details"), details);
		
		markup = markup.replace(RegexMatcher.getNthMatch(markup, MarkupFlag.get("testEndTime") + ".*" + MarkupFlag.get("testEndTime"), 0), MarkupFlag.get("testEndTime") + new SimpleDateFormat("MM/dd HH:mm:ss").format(new Date()).toString() + MarkupFlag.get("testEndTime"))
				.replace(RegexMatcher.getNthMatch(markup, MarkupFlag.get("timeEnded") + ".*" + MarkupFlag.get("timeEnded"), 0), MarkupFlag.get("timeEnded") + new SimpleDateFormat("MM/dd HH:mm:ss").format(new Date()).toString() + MarkupFlag.get("timeEnded"));
				
		FileWriterEx.write(filePath, markup);
	}
	
	@Override
	protected void startTest() {		
		// this order of creating entries in markup is important
		String markup = FileReaderEx.readAllText(filePath)
				.replace(MarkupFlag.get("teststatus"), getLastRunStatus().toString().toLowerCase())
				.replace(MarkupFlag.get("step"), "")
				.replace(MarkupFlag.get("testEndTime"), "");  //.replace("\n", "").replace("\r", "");
		
		if (testDisplayOrder == DisplayOrder.BY_LATEST_TO_OLDEST) {
			markup = markup.replace(MarkupFlag.get("test"), MarkupFlag.get("test") + Resources.getText(packagePath + "test.txt"));
		}
		else {
			markup = markup.replace(MarkupFlag.get("test"), Resources.getText(packagePath + "test.txt") + MarkupFlag.get("test"));
		}
		
		if (testDescription == "") {
			markup = markup.replace(MarkupFlag.get("descvis"), "style='display:none;'");
		}
		
		markup = markup.replace(MarkupFlag.get("testname"), testName)
				.replace(MarkupFlag.get("testdescription"), testDescription)
				.replace(MarkupFlag.get("descvis"), "")
				.replace(MarkupFlag.get("testStartTime"), new SimpleDateFormat("MM/dd HH:mm:ss").format(new Date()).toString());
		
		FileWriterEx.write(filePath, markup);
	}
	
	@Override
	protected void endTest() {
		String markup = FileReaderEx.readAllText(filePath)
				.replace(MarkupFlag.get("teststatus"), getLastRunStatus().toString().toLowerCase())
				.replace(MarkupFlag.get("step"), "")
				.replace(MarkupFlag.get("testEndTime"), "");
		
		testName = "";
		
		FileWriterEx.write(filePath, markup);
	}	
	
	private void writeBaseMarkup(Boolean replaceExisting) throws IOException {
		if (replaceExisting) {
			FileWriterEx.createNewFile(filePath, Resources.getText(packagePath + "base.txt"));
		}
	}
	
	public Logger(String filePath, Boolean replaceExisting, DisplayOrder order) {
		this.filePath = filePath;
		
		if (!new File(filePath).isFile()) {
			replaceExisting = true;
		}
		
		try {
			writeBaseMarkup(replaceExisting);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		testDisplayOrder = order;
	}
	
	public Logger(String filePath) throws IOException {
		this(filePath, false);
	}
	
	public Logger(String filePath, Boolean replaceExisting) {
		this(filePath, replaceExisting, DisplayOrder.BY_OLDEST_TO_LATEST);
	}
	
	public Logger(String filePath, DisplayOrder order) {
		this(filePath, false, order);
	}
}
