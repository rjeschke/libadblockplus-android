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

import org.adblockplus.libadblockplus.BaseFilterEngineTest;
import org.adblockplus.libadblockplus.HeaderEntry;
import org.adblockplus.libadblockplus.IsAllowedConnectionCallback;
import org.adblockplus.libadblockplus.ServerResponse;
import org.adblockplus.libadblockplus.ThrowingWebRequest;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

public class IsAllowedConnectionCallbackTest extends BaseFilterEngineTest
{
  private static final class TestRequest extends ThrowingWebRequest
  {
    private volatile int callCount;

    private List<String> urls = new LinkedList<String>();

    public List<String> getUrls()
    {
      return urls;
    }

    public int getCallCount() { return this.callCount; }

    public void reset() { this.callCount = 0; }

    @Override
    public ServerResponse httpGET(String url, List<HeaderEntry> headers)
    {
      urls.add(url);
      this.callCount++;
      return super.httpGET(url, headers);
    }
  }

  private static final class TestCallback implements IsAllowedConnectionCallback
  {
    private volatile int callCount;
    private boolean result;
    private volatile String connectionType;

    public void setResult(boolean result)
    {
      this.result = result;
    }

    public String getConnectionType()
    {
      return connectionType;
    }

    public boolean isInvoked()
    {
      return this.callCount > 0;
    }

    public int getCallCount() { return this.callCount; }

    public void reset() { this.callCount = 0; }

    @Override
    public boolean isConnectionAllowed(String connectionType)
    {
      this.connectionType = connectionType;
      this.callCount++;
      return result;
    }
  }

  private TestRequest request;
  private TestCallback callback;

  @Override
  protected void setUp() throws Exception
  {
    callback = new TestCallback();
    request = new TestRequest();
    setIsAllowedConnectionCallback(callback);
    setWebRequest(request);
    super.setUp();
  }

  @Override
  protected int getUpdateRequestCount()
  {
    return callback.getCallCount() + request.getCallCount();
  }

  private void setResult(boolean result)
  {
    callback.reset();
    request.reset();
    callback.setResult(result);
  }

  @Test
  public void testAllow()
  {
    final String allowedConnectionType = "wifi1";
    filterEngine.setAllowedConnectionType(allowedConnectionType);

    setResult(true);
    assertEquals(0, request.getUrls().size());

    updateSubscriptions();

    assertTrue(callback.isInvoked());
    assertNotNull(callback.getConnectionType());
    assertEquals(allowedConnectionType, callback.getConnectionType());

    assertTrue(request.getUrls().size() > 0);
  }

  @Test
  public void testDeny()
  {
    final String allowedConnectionType = "wifi2";
    filterEngine.setAllowedConnectionType(allowedConnectionType);

    setResult(false);
    assertEquals(0, request.getUrls().size());

    updateSubscriptions();

    assertTrue(callback.isInvoked());
    assertNotNull(callback.getConnectionType());
    assertEquals(allowedConnectionType, callback.getConnectionType());

    assertEquals(0, request.getUrls().size());
  }
}
