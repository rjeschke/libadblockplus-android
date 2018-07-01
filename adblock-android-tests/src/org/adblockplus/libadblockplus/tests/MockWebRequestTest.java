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

import org.adblockplus.libadblockplus.AdblockPlusException;
import org.adblockplus.libadblockplus.BaseJsEngineTest;
import org.adblockplus.libadblockplus.HeaderEntry;
import org.adblockplus.libadblockplus.ServerResponse;
import org.adblockplus.libadblockplus.WebRequest;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class MockWebRequestTest extends BaseJsEngineTest
{
  @Override
  protected void setUp() throws Exception
  {
    setWebRequest(new LocalMockWebRequest());
    super.setUp();
  }

  private class LocalMockWebRequest implements WebRequest
  {
    @Override
    public ServerResponse httpGET(String url, List<HeaderEntry> headers)
    {
      SystemClock.sleep(50);

      ServerResponse result = new ServerResponse();
      result.setStatus(ServerResponse.NsStatus.OK);
      result.setResponseStatus(123);
      result.setReponseHeaders(Arrays.asList(new HeaderEntry("Foo", "Bar")));

      result.setResponse(
        url + "\n" + 
        headers.get(0).getKey() + "\n" +
        headers.get(0).getValue());
      return result;
    }
  }

  @Test
  public void testBadCall()
  {
    final String[] sources =
      {
        "_webRequest.GET()", "_webRequest.GET('', {}, function(){})",
        "_webRequest.GET({toString: false}, {}, function(){})",
        "_webRequest.GET('http://example.com/', null, function(){})",
        "_webRequest.GET('http://example.com/', {}, null)",
        "_webRequest.GET('http://example.com/', {}, function(){}, 0)"
      };

    for (String source : sources)
    {
      try
      {
        jsEngine.evaluate(source);
        fail(source);
      } catch (AdblockPlusException e)
      {
        // ignored
      }
    }
  }

  @Test
  public void testSuccessfulRequest()
  {
    jsEngine.evaluate(
      "let foo = true; _webRequest.GET('http://example.com/', {X: 'Y'}, function(result) {foo = result;} )");
    assertTrue(jsEngine.evaluate("foo").isBoolean());
    assertTrue(jsEngine.evaluate("foo").asBoolean());

    SystemClock.sleep(200);

    assertEquals(
      ServerResponse.NsStatus.OK.getStatusCode(),
      jsEngine.evaluate("foo.status").asLong());
    assertEquals("http://example.com/\nX\nY", jsEngine.evaluate("foo.responseText").asString());
    assertEquals("{\"Foo\":\"Bar\"}",
      jsEngine.evaluate("JSON.stringify(foo.responseHeaders)").asString());
  }
}
