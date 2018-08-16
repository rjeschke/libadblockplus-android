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

import org.adblockplus.libadblockplus.AppInfo;
import org.adblockplus.libadblockplus.BaseFilterEngineTest;
import org.adblockplus.libadblockplus.Subscription;
import org.junit.Test;

import java.util.List;

public class FilterEngineFirstRunTest extends BaseFilterEngineTest
{
  @Override
  protected void setUp() throws Exception
  {
    setAppInfo(AppInfo.builder().setLocale("zh").build());
    super.setUp();
  }

  @Test
  public void testFirstRunSetWithoutData()
  {
    assertTrue(filterEngine.isFirstRun());
  }

  @Test
  public void testFirstRunClearWithData()
  {
    assertTrue(filterEngine.isFirstRun());
    disposeEngines();
    setupFilterEngine();
    assertFalse(filterEngine.isFirstRun());
  }

  @Test
  public void testLangAndAASubscriptionsAreChosenOnFirstRun()
  {
    assertTrue(filterEngine.isFirstRun());

    final String langUrl = "https://easylist-downloads.adblockplus.org/easylistchina+easylist.txt";

    List<Subscription> subscriptions = filterEngine.getListedSubscriptions();
    assertEquals(2, subscriptions.size());

    Subscription aaSubscription, langSubscription;
    final int aaIdx = subscriptions.get(0).isAcceptableAds() ? 0 : 1;
    aaSubscription = subscriptions.get(aaIdx);
    langSubscription = subscriptions.get(1 - aaIdx);

    assertTrue(aaSubscription.isAcceptableAds());
    assertEquals(langUrl, langSubscription.getProperty("url").asString());
    assertTrue(filterEngine.isAcceptableAdsEnabled());
  }
}
