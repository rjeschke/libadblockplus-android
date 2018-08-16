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

import android.os.SystemClock;

import java.util.List;

public abstract class BaseFilterEngineTest extends BaseJsEngineTest
{
  protected final static int UPDATE_SUBSCRIPTIONS_WAIT_DELAY_MS = 5 * 1000;
  protected final static int UPDATE_SUBSCRIPTIONS_WAIT_CHUNKS = 50;

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    this.setupFilterEngine();
  }

  protected int getUpdateRequestCount()
  {
    return 0;
  }

  protected int updateSubscriptions()
  {
    return updateSubscriptions(false);
  }

  protected int updateSubscriptions(boolean noWait)
  {
    final int init = getUpdateRequestCount();

    List<Subscription> subscriptions = filterEngine.getListedSubscriptions();
    final int num = subscriptions.size();
    for (final Subscription s : subscriptions)
    {
      try
      {
        s.updateFilters();
      }
      finally
      {
        s.dispose();
      }
    }

    if (!noWait)
    {
      for (int i = 0; i < UPDATE_SUBSCRIPTIONS_WAIT_CHUNKS; i++)
      {
        if (getUpdateRequestCount() - init >= num)
        {
          break;
        }
        SystemClock.sleep(UPDATE_SUBSCRIPTIONS_WAIT_DELAY_MS / UPDATE_SUBSCRIPTIONS_WAIT_CHUNKS);
      }
    }

    return num;
  }
}
