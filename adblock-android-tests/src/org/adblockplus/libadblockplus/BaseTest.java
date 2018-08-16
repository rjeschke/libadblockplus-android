/*
 * This file is part of Adblock Plus <https://adblockplus.org/>,
 * Copyright (C) 2006-present eyeo GmbH
 *
 * Adblock Plus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * Adblock Plus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Adblock Plus.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.adblockplus.libadblockplus;

import android.content.Context;
import android.os.SystemClock;
import android.test.InstrumentationTestCase;

import org.adblockplus.libadblockplus.AppInfo;
import org.adblockplus.libadblockplus.FilterEngine;
import org.adblockplus.libadblockplus.IsAllowedConnectionCallback;
import org.adblockplus.libadblockplus.JsEngine;
import org.adblockplus.libadblockplus.LazyLogSystem;
import org.adblockplus.libadblockplus.LogSystem;
import org.adblockplus.libadblockplus.Platform;
import org.adblockplus.libadblockplus.ThrowingWebRequest;
import org.adblockplus.libadblockplus.WebRequest;

import java.io.File;

public class BaseTest extends InstrumentationTestCase
{
  protected Platform platform;
  protected JsEngine jsEngine;
  protected FilterEngine filterEngine;
  private final SetupInfo setupInfo;

  public BaseTest()
  {
    super();
    setupInfo = new SetupInfo();
  }

  @Override
  protected void setUp() throws Exception
  {
    deleteFiles();
    super.setUp();
  }

  private static class SetupInfo
  {
    public AppInfo appInfo;
    public LogSystem logSystem;
    public WebRequest webRequest;
    public String basePath;
    public IsAllowedConnectionCallback isAllowedConnectionCallback;

    public SetupInfo()
    {
      appInfo = AppInfo.builder().build();
      logSystem = new LazyLogSystem();
      webRequest = new ThrowingWebRequest();
    }

    public void updateBasePath(Context context)
    {
      if (basePath == null)
      {
        basePath = context.getFilesDir().getAbsolutePath();
      }
    }
  }

  protected void setAppInfo(AppInfo appInfo)
    {
        setupInfo.appInfo = appInfo;
    }

  protected void setLogSystem(LogSystem logSystem)
    {
        setupInfo.logSystem = logSystem;
    }

  protected void setWebRequest(WebRequest webRequest)
    {
        setupInfo.webRequest = webRequest;
    }

  protected void setBasePath(String basePath)
    {
        setupInfo.basePath = basePath;
    }

  protected void setIsAllowedConnectionCallback(IsAllowedConnectionCallback callback)
  {
    setupInfo.isAllowedConnectionCallback = callback;
  }

  protected void deleteFiles()
  {
    for (File f : getContext().getFilesDir().listFiles())
    {
      f.delete();
    }
  }

  protected void setupPlatform()
  {
    if (platform == null)
    {
      setupInfo.updateBasePath(getContext());
      platform = new Platform(setupInfo.logSystem, setupInfo.webRequest, setupInfo.basePath);
    }
  }

  protected void setupJsEngine()
  {
    setupPlatform();
    if (jsEngine == null)
    {
      platform.setUpJsEngine(setupInfo.appInfo);
      jsEngine = platform.getJsEngine();
    }
  }

  protected void setupFilterEngine()
  {
    setupJsEngine();
    if (filterEngine == null)
    {
      if (setupInfo.isAllowedConnectionCallback != null)
      {
        platform.setUpFilterEngine(setupInfo.isAllowedConnectionCallback);
      }

      filterEngine = platform.getFilterEngine();

      if (filterEngine.isFirstRun())
      {
        // Wait until stuff got persisted ...
        File patterns = new File(getContext().getFilesDir(), "patterns.ini");
        while (!patterns.exists())
        {
          SystemClock.sleep(50);
        }
      }
    }
  }

  protected void disposeEngines()
  {
    filterEngine = null;
    jsEngine = null;
    if (platform != null)
    {
      platform.dispose();
      platform = null;
    }
  }

  protected Context getContext()
  {
    return getInstrumentation().getTargetContext();
  }

  @Override
  protected void tearDown() throws Exception
  {
    super.tearDown();
    disposeEngines();
  }
}
