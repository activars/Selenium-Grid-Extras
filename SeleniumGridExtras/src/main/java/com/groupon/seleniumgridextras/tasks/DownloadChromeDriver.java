/**
 * Copyright (c) 2013, Groupon, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * Neither the name of GROUPON nor the names of its contributors may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 * Created with IntelliJ IDEA.
 * User: Dima Kovalenko (@dimacus) && Darko Marinov
 * Date: 5/10/13
 * Time: 4:06 PM
 */

package com.groupon.seleniumgridextras.tasks;

import com.google.gson.JsonObject;

import com.groupon.seleniumgridextras.downloader.ChromeDriverDownloader;
import com.groupon.seleniumgridextras.downloader.Downloader;
import com.groupon.seleniumgridextras.config.RuntimeConfig;

import org.apache.log4j.Logger;

import java.io.File;
import java.util.Map;

public class DownloadChromeDriver extends ExecuteOSTask {

  private String bit = "32";
  private static Logger logger = Logger.getLogger(DownloadChromeDriver.class);

  public DownloadChromeDriver() {
    setEndpoint("/download_chromedriver");
    setDescription("Downloads a version of ChromeDriver to local machine");
    JsonObject params = new JsonObject();
    params.addProperty("version", "Version of ChromeDriver to download, such as 2.6");
    params.addProperty("bit", "Bit Version of ChromeDriver 32/64 - (default: 32)");
    setAcceptedParams(params);
    setRequestType("GET");
    setResponseType("json");
    setClassname(this.getClass().getCanonicalName().toString());
    setCssClass("btn-success");
    setButtonText("Download Chrome-Driver");
    setEnabledInGui(true);

    addResponseDescription("root_dir", "Directory to which executable file was saved to");
    addResponseDescription("file", "Relative path to file on the node");
    addResponseDescription("file_full_path", "Full path to file on node");
    addResponseDescription("source_url",
                           "Url from which the executable was downloaded. If file already exists, this will be blank, and download will be skipped");

    // bit value should be initialized from configuration
    this.bit = RuntimeConfig.getConfig().getChromeDriver().getBit();

    logger.debug(RuntimeConfig.getConfig());
    getJsonResponse()
        .addKeyValues("root_dir", RuntimeConfig.getConfig().getChromeDriver().getDirectory());
    getJsonResponse().addKeyValues("source_url", "");

  }

  @Override
  public JsonObject execute() {
    return execute(RuntimeConfig.getConfig().getChromeDriver().getVersion());
  }

  @Override
  public JsonObject execute(Map<String, String> parameter) {

    if (!parameter.isEmpty() && parameter.containsKey("version")) {
      if (parameter.containsKey("bit")) {
        this.bit = parameter.get("bit").toString();
      } else {
        this.bit = "32";
      }
      return execute(parameter.get("version").toString());
    } else {
      return execute();
    }
  }

  @Override
  public JsonObject execute(String version) {


    Downloader
        downloader =
        new ChromeDriverDownloader(version, this.bit);

    if (!new File(RuntimeConfig.getConfig().getChromeDriver().getExecutablePath()).exists()) {
      Boolean downloaded = downloader.download();
      getJsonResponse().addKeyValues("source_url", downloader.getSourceURL());

      if (!downloaded) {
        getJsonResponse().addKeyValues("error", downloader.getErrorMessage());
      }
    } else {
      logger.debug("No need for download");
      getJsonResponse().addKeyValues("out", "File already downloaded, will not download again");
    }

    getJsonResponse()
        .addKeyValues("file_full_path",
                      downloader.getDestinationFileFullPath().getAbsolutePath());

    getJsonResponse()
        .addKeyValues("file", downloader.getDestinationFileFullPath().getName());


    return getJsonResponse().getJson();
  }

  @Override
  public boolean initialize() {

    try {
      File chromeDriverExecutable = new File(RuntimeConfig.getConfig().getChromeDriver().getExecutablePath());
      File chromeDriverHome = new File(RuntimeConfig.getConfig().getChromeDriver().getDirectory());

      if (!chromeDriverHome.exists()) {
        chromeDriverHome.mkdir();
      }

      if (!chromeDriverExecutable.exists()) {
        logger.debug("No Chrome Driver Executable, will download");
        logger.debug(execute().toString());
      }


    } catch (NullPointerException error) {
      printInitilizedFailure();
      logger.error(error);
      return false;
    }

    printInitilizedSuccessAndRegisterWithAPI();
    return true;

  }


}

