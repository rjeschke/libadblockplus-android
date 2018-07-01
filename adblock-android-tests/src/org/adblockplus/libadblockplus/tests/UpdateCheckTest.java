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

package org.adblockplus.libadblockplus.tests;

import android.os.SystemClock;

import org.adblockplus.libadblockplus.AppInfo;
import org.adblockplus.libadblockplus.BaseFilterEngineTest;
import org.adblockplus.libadblockplus.EventCallback;
import org.adblockplus.libadblockplus.HeaderEntry;
import org.adblockplus.libadblockplus.JsValue;
import org.adblockplus.libadblockplus.ServerResponse;
import org.adblockplus.libadblockplus.ThrowingWebRequest;
import org.adblockplus.libadblockplus.UpdateCheckDoneCallback;

import org.junit.Test;

import java.util.List;

public class UpdateCheckTest extends BaseFilterEngineTest
{
  protected String previousRequestUrl;

  public class TestWebRequest extends ThrowingWebRequest
  {
    public ServerResponse response = new ServerResponse();

    @Override
    public ServerResponse httpGET(String url, List<HeaderEntry> headers)
    {
      if (url.indexOf("easylist") >= 0)
      {
        return super.httpGET(url, headers);
      }

      previousRequestUrl = url;
      return response;
    }
  }

  protected TestWebRequest webRequest;

  protected boolean eventCallbackCalled;
  protected List<JsValue> eventCallbackParams;
  protected boolean updateCallbackCalled;
  protected String updateError;

  private EventCallback eventCallback = new EventCallback()
  {
    @Override
    public void eventCallback(List<JsValue> params)
    {
      eventCallbackCalled = true;
      eventCallbackParams = params;
    }
  };

  private UpdateCheckDoneCallback updateCallback = new UpdateCheckDoneCallback()
  {
    @Override
    public void updateCheckDoneCallback(String error)
    {
      updateCallbackCalled = true;
      updateError = error;
    }
  };

  public void reset()
  {
    disposeEngines();
    setupJsEngine();
    jsEngine.setEventCallback("updateAvailable", eventCallback);
    setupFilterEngine();
  }

  @Override
  protected void setUp() throws Exception
  {
    webRequest = new TestWebRequest();
    setWebRequest(webRequest);
    eventCallbackCalled = false;
    updateCallbackCalled = false;
    super.setUp();
  }

  public void forceUpdateCheck()
  {
    filterEngine.forceUpdateCheck(updateCallback);
    for (int i = 0; i < UPDATE_SUBSCRIPTIONS_WAIT_CHUNKS; i++)
    {
      if (eventCallbackCalled || updateCallbackCalled)
      {
        break;
      }
      SystemClock.sleep(UPDATE_SUBSCRIPTIONS_WAIT_DELAY_MS / UPDATE_SUBSCRIPTIONS_WAIT_CHUNKS);
    }
  }

  @Test
  public void testRequestFailure()
  {
    webRequest.response.setStatus(ServerResponse.NsStatus.ERROR_FAILURE);

    AppInfo appInfo = AppInfo
      .builder()
      .setName("1")
      .setVersion("3")
      .setApplication("4")
      .setApplicationVersion("2")
      .setDevelopmentBuild(false)
      .build();
    setAppInfo(appInfo);

    reset();
    forceUpdateCheck();

    assertFalse(eventCallbackCalled);
    assertTrue(updateCallbackCalled);
    assertNotNull(updateError);

    String expectedUrl = filterEngine.getPref("update_url_release").asString();
    String platform = "libadblockplus";
    String platformVersion = "1.0";

    expectedUrl = expectedUrl
      .replaceAll("%NAME%", appInfo.name)
      .replaceAll("%TYPE%", "1"); // manual update

    expectedUrl +=
      "&addonName=" + appInfo.name +
      "&addonVersion=" + appInfo.version +
      "&application=" + appInfo.application +
      "&applicationVersion=" + appInfo.applicationVersion +
      "&platform=" + platform +
      "&platformVersion=" + platformVersion +
      "&lastVersion=0&downloadCount=0";

    assertEquals(expectedUrl, previousRequestUrl);
  }

  @Test
  public void testApplicationUpdateAvailable()
  {
    webRequest.response.setStatus(ServerResponse.NsStatus.OK);
    webRequest.response.setResponseStatus(200);
    webRequest.response.setResponse(
      "{\"1/4\": {\"version\":\"3.1\",\"url\":\"https://foo.bar/\"}}");

    AppInfo appInfo = AppInfo
      .builder()
      .setName("1")
      .setVersion("3")
      .setApplication("4")
      .setApplicationVersion("2")
      .setDevelopmentBuild(true)
      .build();
    setAppInfo(appInfo);

    reset();
    forceUpdateCheck();

    assertTrue(eventCallbackCalled);
    assertNotNull(eventCallbackParams);
    assertEquals(1l, eventCallbackParams.size());
    assertEquals("https://foo.bar/", eventCallbackParams.get(0).asString());
    assertTrue(updateCallbackCalled);
    assertEquals("", updateError);
  }

  @Test
  public void testWrongApplication()
  {
    webRequest.response.setStatus(ServerResponse.NsStatus.OK);
    webRequest.response.setResponseStatus(200);
    webRequest.response.setResponse(
      "{\"1/3\": {\"version\":\"3.1\",\"url\":\"https://foo.bar/\"}}");

    AppInfo appInfo = AppInfo
      .builder()
      .setName("1")
      .setVersion("3")
      .setApplication("4")
      .setApplicationVersion("2")
      .setDevelopmentBuild(true)
      .build();
    setAppInfo(appInfo);

    reset();
    forceUpdateCheck();

    assertFalse(eventCallbackCalled);
    assertTrue(updateCallbackCalled);
    assertEquals("", updateError);
  }

  @Test
  public void testWrongVersion()
  {
    webRequest.response.setStatus(ServerResponse.NsStatus.OK);
    webRequest.response.setResponseStatus(200);
    webRequest.response.setResponse(
      "{\"1\": {\"version\":\"3\",\"url\":\"https://foo.bar/\"}}");

    AppInfo appInfo = AppInfo
      .builder()
      .setName("1")
      .setVersion("3")
      .setApplication("4")
      .setApplicationVersion("2")
      .setDevelopmentBuild(true)
      .build();
    setAppInfo(appInfo);

    reset();
    forceUpdateCheck();

    assertFalse(eventCallbackCalled);
    assertTrue(updateCallbackCalled);
    assertEquals("", updateError);
  }

  @Test
  public void testWrongURL()
  {
    webRequest.response.setStatus(ServerResponse.NsStatus.OK);
    webRequest.response.setResponseStatus(200);
    webRequest.response.setResponse(
      "{\"1\": {\"version\":\"3.1\",\"url\":\"http://insecure/\"}}");

    AppInfo appInfo = AppInfo
      .builder()
      .setName("1")
      .setVersion("3")
      .setApplication("4")
      .setApplicationVersion("2")
      .setDevelopmentBuild(true)
      .build();
    setAppInfo(appInfo);

    reset();
    forceUpdateCheck();

    assertFalse(eventCallbackCalled);
    assertTrue(updateCallbackCalled);
    assertTrue(updateError.length() > 0);
  }
}
