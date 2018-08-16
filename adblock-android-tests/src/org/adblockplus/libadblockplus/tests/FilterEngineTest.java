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

import android.util.Log;

import org.adblockplus.libadblockplus.BaseFilterEngineTest;
import org.adblockplus.libadblockplus.Filter;
import org.adblockplus.libadblockplus.FilterEngine;
import org.adblockplus.libadblockplus.MockFilterChangeCallback;
import org.adblockplus.libadblockplus.Subscription;

import org.junit.Test;

import java.util.List;

public class FilterEngineTest extends BaseFilterEngineTest
{
  private static final String TAG = FilterEngineTest.class.getSimpleName();

  @Test
  public void testFilterCreation()
  {
    Filter filter1 = filterEngine.getFilter("foo");
    assertEquals(Filter.Type.BLOCKING, filter1.getType());
    Filter filter2 = filterEngine.getFilter("@@foo");
    assertEquals(Filter.Type.EXCEPTION, filter2.getType());
    Filter filter3 = filterEngine.getFilter("example.com##foo");
    assertEquals(Filter.Type.ELEMHIDE, filter3.getType());
    Filter filter4 = filterEngine.getFilter("example.com#@#foo");
    assertEquals(Filter.Type.ELEMHIDE_EXCEPTION, filter4.getType());
    Filter filter5 = filterEngine.getFilter("  foo  ");
    assertEquals(filter1, filter5);
  }

  @Test
  public void testAddRemoveFilters()
  {
    while (filterEngine.getListedFilters().size() > 0)
    {
      int prev = filterEngine.getListedFilters().size();
      filterEngine.getListedFilters().get(0).removeFromList();
      if (prev == filterEngine.getListedFilters().size())
      {
        Log.e(TAG, "Failed to clear listed filters.");
      }
    }

    assertEquals(0, filterEngine.getListedFilters().size());
    Filter filter = filterEngine.getFilter("foo");
    assertEquals(0, filterEngine.getListedFilters().size());
    assertFalse(filter.isListed());

    filter.addToList();
    assertEquals(1, filterEngine.getListedFilters().size());
    assertEquals(filter, filterEngine.getListedFilters().get(0));
    assertTrue(filter.isListed());

    filter.addToList();
    assertEquals(1, filterEngine.getListedFilters().size());
    assertEquals(filter, filterEngine.getListedFilters().get(0));
    assertTrue(filter.isListed());

    filter.removeFromList();
    assertEquals(0, filterEngine.getListedFilters().size());
    assertFalse(filter.isListed());

    filter.removeFromList();
    assertEquals(0, filterEngine.getListedFilters().size());
    assertFalse(filter.isListed());
  }

  @Test
  public void testAddedSubscriptionIsEnabled()
  {
    Subscription subscription = filterEngine.getSubscription("foo");
    assertFalse(subscription.isDisabled());
  }

  @Test
  public void testDisablingSubscriptionDisablesItAndFiresEvent()
  {
    Subscription subscription = filterEngine.getSubscription("foo");
    MockFilterChangeCallback callback = new MockFilterChangeCallback("subscription.disabled", "url","foo");
    filterEngine.setFilterChangeCallback(callback);
    assertFalse(subscription.isDisabled());
    subscription.setDisabled(true);
    assertEquals(1, callback.getTimesCalled());
    assertTrue(subscription.isDisabled());
    filterEngine.removeFilterChangeCallback();
  }

  @Test
  public void testEnablingSubscriptionEnablesItAndFiresEvent()
  {
    Subscription subscription = filterEngine.getSubscription("foo");
    assertFalse(subscription.isDisabled());
    subscription.setDisabled(true);
    assertTrue(subscription.isDisabled());

    MockFilterChangeCallback callback = new MockFilterChangeCallback("subscription.disabled", "url", "foo");
    filterEngine.setFilterChangeCallback(callback);
    subscription.setDisabled(false);
    assertEquals(1, callback.getTimesCalled());
    assertFalse(subscription.isDisabled());
    filterEngine.removeFilterChangeCallback();
  }

  @Test
  public void testAddRemoveSubscriptions()
  {
    while (filterEngine.getListedSubscriptions().size() > 0)
    {
      int prev = filterEngine.getListedSubscriptions().size();
      filterEngine.getListedSubscriptions().get(0).removeFromList();
      if (prev == filterEngine.getListedSubscriptions().size())
      {
        Log.e(TAG, "Failed to clear listed subscriptions.");
        break;
      }
    }

    assertEquals(0, filterEngine.getListedSubscriptions().size());
    Subscription subscription = filterEngine.getSubscription("foo");
    assertEquals(0, filterEngine.getListedSubscriptions().size());
    assertFalse(subscription.isListed());
    subscription.addToList();
    assertEquals(1, filterEngine.getListedSubscriptions().size());
    assertEquals(subscription, filterEngine.getListedSubscriptions().get(0));
    assertTrue(subscription.isListed());
    subscription.addToList();
    assertEquals(1, filterEngine.getListedSubscriptions().size());
    assertEquals(subscription, filterEngine.getListedSubscriptions().get(0));
    assertTrue(subscription.isListed());
    subscription.removeFromList();
    assertEquals(0, filterEngine.getListedSubscriptions().size());
    assertFalse(subscription.isListed());
    subscription.removeFromList();
    assertEquals(0, filterEngine.getListedSubscriptions().size());
    assertFalse(subscription.isListed());
  }

  @Test
  public void testSubscriptionUpdates()
  {
    Subscription subscription = filterEngine.getSubscription("foo");
    assertFalse(subscription.isUpdating());
    subscription.updateFilters();
  }

  @Test
  public void testMatches()
  {
    filterEngine.getFilter("adbanner.gif").addToList();
    filterEngine.getFilter("@@notbanner.gif").addToList();
    filterEngine.getFilter("tpbanner.gif$third-party").addToList();
    filterEngine.getFilter("fpbanner.gif$~third-party").addToList();
    filterEngine.getFilter("combanner.gif$domain=example.com").addToList();
    filterEngine.getFilter("orgbanner.gif$domain=~example.com").addToList();

    Filter match1 = filterEngine.matches(
      "http://example.org/foobar.gif",
      FilterEngine.ContentType.IMAGE,
      "");
    assertNull(match1);

    Filter match2 = filterEngine.matches(
      "http://example.org/adbanner.gif",
      FilterEngine.ContentType.IMAGE,
      "");
    assertNotNull(match2);
    assertEquals(Filter.Type.BLOCKING, match2.getType());

    Filter match3 = filterEngine.matches(
      "http://example.org/notbanner.gif",
      FilterEngine.ContentType.IMAGE,
      "");
    assertNotNull(match3);
    assertEquals(Filter.Type.EXCEPTION, match3.getType());

    Filter match4 = filterEngine.matches(
      "http://example.org/notbanner.gif",
      FilterEngine.ContentType.IMAGE, "");
    assertNotNull(match4);
    assertEquals(Filter.Type.EXCEPTION, match4.getType());

    Filter match5 = filterEngine.matches(
      "http://example.org/tpbanner.gif",
      FilterEngine.ContentType.IMAGE,
      "http://example.org/");
    assertNull(match5);

    Filter match6 = filterEngine.matches(
      "http://example.org/fpbanner.gif",
      FilterEngine.ContentType.IMAGE,
      "http://example.org/");
    assertNotNull(match6);
    assertEquals(Filter.Type.BLOCKING, match6.getType());

    Filter match7 = filterEngine.matches(
      "http://example.org/tpbanner.gif",
      FilterEngine.ContentType.IMAGE,
      "http://example.com/");
    assertNotNull(match7);
    assertEquals(Filter.Type.BLOCKING, match7.getType());

    Filter match8 = filterEngine.matches(
      "http://example.org/fpbanner.gif",
      FilterEngine.ContentType.IMAGE,
      "http://example.com/");
    assertNull(match8);

    Filter match9 = filterEngine.matches(
      "http://example.org/combanner.gif",
      FilterEngine.ContentType.IMAGE,
      "http://example.com/");
    assertNotNull(match9);
    assertEquals(Filter.Type.BLOCKING, match9.getType());

    Filter match10 = filterEngine.matches(
      "http://example.org/combanner.gif",
      FilterEngine.ContentType.IMAGE,
      "http://example.org/");
    assertNull(match10);

    Filter match11 = filterEngine.matches(
      "http://example.org/orgbanner.gif",
      FilterEngine.ContentType.IMAGE,
      "http://example.com/");
    assertNull(match11);

    Filter match12 = filterEngine.matches(
      "http://example.org/orgbanner.gif",
      FilterEngine.ContentType.IMAGE,
      "http://example.org/");
    assertNotNull(match12);
    assertEquals(Filter.Type.BLOCKING, match12.getType());
  }

  @Test
  public void testMatchesOnWhitelistedDomain()
  {
    filterEngine.getFilter("adbanner.gif").addToList();
    filterEngine.getFilter("@@||example.org^$document").addToList();

    Filter match1 = filterEngine.matches(
      "http://ads.com/adbanner.gif",
      FilterEngine.ContentType.IMAGE,
      "http://example.com/");
    assertNotNull(match1);
    assertEquals(Filter.Type.BLOCKING, match1.getType());

    Filter match2 = filterEngine.matches(
      "http://ads.com/adbanner.gif",
      FilterEngine.ContentType.IMAGE,
      "http://example.org/");
    assertNotNull(match2);
    assertEquals(Filter.Type.EXCEPTION, match2.getType());
  }

  @Test
  public void testMatchesNestedFrameRequest()
  {
    filterEngine.getFilter("adbanner.gif").addToList();
    filterEngine.getFilter("@@adbanner.gif$domain=example.org").addToList();

    Filter match1 = filterEngine.matches(
      "http://ads.com/adbanner.gif",
      FilterEngine.ContentType.IMAGE,
      new String[]
        {
          "http://ads.com/frame/",
          "http://example.com/"
        });
    assertNotNull(match1);
    assertEquals(Filter.Type.BLOCKING, match1.getType());

    Filter match2 = filterEngine.matches(
      "http://ads.com/adbanner.gif",
      FilterEngine.ContentType.IMAGE,
      new String[]
        {
          "http://ads.com/frame/",
          "http://example.org/"
        });
    assertNotNull(match2);
    assertEquals(Filter.Type.EXCEPTION, match2.getType());

    Filter match3 = filterEngine.matches(
      "http://ads.com/adbanner.gif",
      FilterEngine.ContentType.IMAGE,
      new String[]
        {
          "http://example.org/",
          "http://ads.com/frame/"
        });
    assertNotNull(match3);
    assertEquals(Filter.Type.BLOCKING, match3.getType());
  }

  @Test
  public void testMatchesNestedFrameOnWhitelistedDomain()
  {
    filterEngine.getFilter("adbanner.gif").addToList();
    filterEngine.getFilter("@@||example.org^$document,domain=ads.com").addToList();

    Filter match1 = filterEngine.matches(
      "http://ads.com/adbanner.gif",
      FilterEngine.ContentType.IMAGE,
      new String[]
        {
          "http://ads.com/frame/",
          "http://example.com/"
        });
    assertNotNull(match1);
    assertEquals(Filter.Type.BLOCKING, match1.getType());

    Filter match2 = filterEngine.matches(
      "http://ads.com/adbanner.gif",
      FilterEngine.ContentType.IMAGE,
      new String[]
        {
          "http://ads.com/frame/",
          "http://example.org/"
        });
    assertNotNull(match2);
    assertEquals(Filter.Type.EXCEPTION, match2.getType());

    Filter match3 = filterEngine.matches(
      "http://ads.com/adbanner.gif",
      FilterEngine.ContentType.IMAGE,
      new String[]
        {
          "http://example.org/"
        });
    assertNotNull(match3);
    assertEquals(Filter.Type.BLOCKING, match3.getType());

    Filter match4 = filterEngine.matches(
      "http://ads.com/adbanner.gif",
      FilterEngine.ContentType.IMAGE,
      new String[]
        {
          "http://example.org/",
          "http://ads.com/frame/"
        });
    assertNotNull(match4);
    assertEquals(Filter.Type.BLOCKING, match4.getType());

    Filter match5 = filterEngine.matches(
      "http://ads.com/adbanner.gif",
      FilterEngine.ContentType.IMAGE,
      new String[]
        {
          "http://ads.com/frame/",
          "http://example.org/",
          "http://example.com/"
        });
    assertNotNull(match5);
    assertEquals(Filter.Type.EXCEPTION, match5.getType());
  }

  @Test
  public void testSetRemoveFilterChangeCallback()
  {
    MockFilterChangeCallback callback = new MockFilterChangeCallback("subscription.added", "defaults", "blocking");

    filterEngine.setFilterChangeCallback(callback);
    Filter filter = filterEngine.getFilter("foo");
    assertFalse(filter.isListed());
    filter.addToList();
    assertEquals(1, callback.getTimesCalled());

    filterEngine.removeFilterChangeCallback();
    callback.clearCheckValues();
    filter.removeFromList();
    assertFalse(filter.isListed());
    assertEquals(1, callback.getTimesCalled());
  }

  @Test
  public void testDocumentWhitelisting()
  {
    filterEngine.getFilter("@@||example.org^$document").addToList();
    filterEngine.getFilter("@@||example.com^$document,domain=example.de").addToList();

    String[] emptyArray = new String[]
      {
      };

    assertTrue(filterEngine.isDocumentWhitelisted("http://example.org", emptyArray));
    assertFalse(filterEngine.isDocumentWhitelisted("http://example.co.uk", emptyArray));
    assertFalse(filterEngine.isDocumentWhitelisted("http://example.com", emptyArray));

    String[] documentUrls1 = new String[]
      {
        "http://example.de"
      };
    assertTrue(filterEngine.isDocumentWhitelisted("http://example.com", documentUrls1));
    assertFalse(filterEngine.isDocumentWhitelisted("http://example.co.uk", documentUrls1));
  }

  @Test
  public void testElemhideWhitelisting()
  {
    filterEngine.getFilter("@@||example.org^$elemhide").addToList();
    filterEngine.getFilter("@@||example.com^$elemhide,domain=example.de").addToList();

    String[] emptyArray = new String[]
      {
      };

    assertTrue(filterEngine.isElemhideWhitelisted("http://example.org", emptyArray));
    assertFalse(filterEngine.isElemhideWhitelisted("http://example.co.uk", emptyArray));
    assertFalse(filterEngine.isElemhideWhitelisted("http://example.com", emptyArray));

    String[] documentUrls1 = new String[]
      {
        "http://example.de"
      };
    assertTrue(filterEngine.isElemhideWhitelisted("http://example.com", documentUrls1));
    assertFalse(filterEngine.isElemhideWhitelisted("http://example.co.uk", documentUrls1));
  }

  @Test
  public void testGetAcceptableAdsSubscriptionUrl()
  {
    String url = filterEngine.getAcceptableAdsSubscriptionURL();
    assertNotNull(url);
  }

  @Test
  public void testSetGetAcceptableAds()
  {
    boolean isAA = filterEngine.isAcceptableAdsEnabled();
    isAA = !isAA;
    filterEngine.setAcceptableAdsEnabled(isAA);
    assertEquals(isAA, filterEngine.isAcceptableAdsEnabled());
    isAA = !isAA;
    filterEngine.setAcceptableAdsEnabled(isAA);
    assertEquals(isAA, filterEngine.isAcceptableAdsEnabled());
  }

  @Test
  public void testIsAcceptableAdsIfEnabled()
  {
    if (!filterEngine.isAcceptableAdsEnabled())
    {
      filterEngine.setAcceptableAdsEnabled(true);
    }
    assertTrue(filterEngine.isAcceptableAdsEnabled());

    List<Subscription> listedSubscriptions = filterEngine.getListedSubscriptions();
    for (Subscription eachSubscription : listedSubscriptions)
    {
      if (eachSubscription.isAcceptableAds())
      {
        return;
      }
    }
    fail("AA subscription not found in listed subscriptions when enabled");
  }

  @Test
  public void testSubscriptionsAreNotDisabled()
  {
    if (!filterEngine.isAcceptableAdsEnabled())
    {
      filterEngine.setAcceptableAdsEnabled(true);
    }
    assertTrue(filterEngine.isAcceptableAdsEnabled());

    List<Subscription> listedSubscriptions = filterEngine.getListedSubscriptions();
    for (Subscription eachSubscription : listedSubscriptions)
    {
      assertFalse(eachSubscription.isDisabled());
    }
  }

  @Test
  public void testSubscriptionsSetDisabled()
  {
    List<Subscription> listedSubscriptions = filterEngine.getListedSubscriptions();
    Subscription subscription = listedSubscriptions.get(0);
    boolean originalDisabled = subscription.isDisabled();

    subscription.setDisabled(!originalDisabled);
    assertEquals(!originalDisabled, subscription.isDisabled());

    subscription.setDisabled(originalDisabled);
    assertEquals(originalDisabled, subscription.isDisabled());
  }

  @Test
  public void testDisableEnableAcceptableAdsSubscription()
  {
    if (filterEngine.isAcceptableAdsEnabled())
    {
      filterEngine.setAcceptableAdsEnabled(false);
    }
    assertFalse(filterEngine.isAcceptableAdsEnabled());

    List<Subscription> listedSubscriptions = filterEngine.getListedSubscriptions();
    for (Subscription eachSubscription : listedSubscriptions)
    {
      if (eachSubscription.isAcceptableAds())
      {
        assertTrue(eachSubscription.isDisabled());
      }
    }

    filterEngine.setAcceptableAdsEnabled(true);
    listedSubscriptions = filterEngine.getListedSubscriptions();
    for (Subscription eachSubscription : listedSubscriptions)
    {
      if (eachSubscription.isAcceptableAds())
      {
        assertFalse(eachSubscription.isDisabled());
      }
    }
  }
}
