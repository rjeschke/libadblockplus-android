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

public class MockFilterChangeCallback extends FilterChangeCallback
{
  private volatile int timesCalled;
  private String checkAction;
  private String checkKey;
  private String checkValue;

  public MockFilterChangeCallback(String checkAction, String checkKey, String checkValue)
  {
    this.checkAction = checkAction;
    this.checkKey = checkKey;
    this.checkValue = checkValue;
  }

  public void clearCheckValues()
  {
    checkAction = null;
  }

  public int getTimesCalled()
  {
    return timesCalled;
  }

  @Override
  public void filterChangeCallback(String action, JsValue jsValue)
  {
    if (checkAction != null && (!action.equals(checkAction) ||
            !jsValue.getProperty(checkKey).asString().equals(checkValue)))
    {
      return;
    }
    timesCalled++;
  }
}
