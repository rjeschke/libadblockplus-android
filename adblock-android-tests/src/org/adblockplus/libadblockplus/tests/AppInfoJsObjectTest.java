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

import org.adblockplus.libadblockplus.BasePlatformTest;
import org.junit.Test;

public class AppInfoJsObjectTest extends BasePlatformTest
{
  @Test
  public void testAllProperties()
  {
    setAppInfo(AppInfo
      .builder()
      .setVersion("1")
      .setName("3")
      .setApplication("4")
      .setApplicationVersion("5")
      .setLocale("2")
      .setDevelopmentBuild(true)
      .build());

    setupJsEngine();
    assertEquals("1", jsEngine.evaluate("_appInfo.version").asString());
    assertEquals("3", jsEngine.evaluate("_appInfo.name").asString());
    assertEquals("4", jsEngine.evaluate("_appInfo.application").asString());
    assertEquals("5", jsEngine.evaluate("_appInfo.applicationVersion").asString());
    assertEquals("2", jsEngine.evaluate("_appInfo.locale").asString());
    assertTrue(jsEngine.evaluate("_appInfo.developmentBuild").asBoolean());
  }

  @Test
  public void testDefaultPropertyValues()
  {
    setAppInfo(AppInfo
      .builder()
      .build());
    setupJsEngine();
    assertEquals("1.0", jsEngine.evaluate("_appInfo.version").asString());
    assertEquals("libadblockplus-android", jsEngine.evaluate("_appInfo.name").asString());
    assertEquals("android", jsEngine.evaluate("_appInfo.application").asString());
    assertEquals("0", jsEngine.evaluate("_appInfo.applicationVersion").asString());
    assertEquals("en_US", jsEngine.evaluate("_appInfo.locale").asString());
    assertFalse(jsEngine.evaluate("_appInfo.developmentBuild").asBoolean());
  }
}
