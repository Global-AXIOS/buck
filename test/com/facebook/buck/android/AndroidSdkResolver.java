/*
 * Copyright 2019-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.facebook.buck.android;

import com.facebook.buck.android.toolchain.AndroidBuildToolsLocation;
import com.facebook.buck.android.toolchain.AndroidPlatformTarget;
import com.facebook.buck.android.toolchain.AndroidSdkLocation;
import com.facebook.buck.android.toolchain.TestAndroidSdkLocationFactory;
import com.facebook.buck.android.toolchain.impl.AndroidBuildToolsResolver;
import com.facebook.buck.android.toolchain.impl.AndroidPlatformTargetProducer;
import com.facebook.buck.android.toolchain.ndk.impl.AndroidNdkHelper;
import com.facebook.buck.io.filesystem.ProjectFilesystem;
import com.facebook.buck.testutil.integration.ProjectWorkspace;
import com.facebook.buck.util.VersionStringComparator;
import java.io.IOException;
import java.util.Optional;

/**
 * Wraps a {@link ProjectWorkspace}, providing utilities that are convenient for writing Android
 * tests that are dependent on aspects of the Android SDK.
 */
final class AndroidSdkResolver {
  private static final VersionStringComparator VERSION_STRING_COMPARATOR =
      new VersionStringComparator();

  private final AndroidSdkLocation sdkLocation;
  private final AndroidBuildToolsResolver toolsResolver;
  private final AndroidPlatformTarget platformTarget;

  AndroidSdkResolver(ProjectWorkspace projectWorkspace) throws IOException {
    this(projectWorkspace.getProjectFileSystem());
  }

  AndroidSdkResolver(ProjectFilesystem fileSystem) throws IOException {
    sdkLocation = TestAndroidSdkLocationFactory.create(fileSystem);
    toolsResolver = new AndroidBuildToolsResolver(AndroidNdkHelper.DEFAULT_CONFIG, sdkLocation);

    AndroidBuildToolsLocation buildToolsLocation =
        AndroidBuildToolsLocation.of(toolsResolver.getBuildToolsPath());
    platformTarget =
        AndroidPlatformTargetProducer.getDefaultPlatformTarget(
            fileSystem, buildToolsLocation, sdkLocation, Optional.empty(), Optional.empty());
  }

  /** Returns the {@link AndroidPlatformTarget} foer the current workspace. */
  public AndroidPlatformTarget getAndroidPlatformTarget() {
    return platformTarget;
  }

  /**
   * Checks whether the Android build tools version is newer than or equal to the given version.
   *
   * @return true if the build tools version is at least {@code expectedVersion}.
   */
  public boolean isBuildToolsVersionAtLeast(String expectedVersion) {
    Optional<String> actualVersion = getBuildToolsVersion();
    return actualVersion.isPresent()
        && VERSION_STRING_COMPARATOR.compare(actualVersion.get(), expectedVersion) >= 0;
  }

  private Optional<String> getBuildToolsVersion() {
    return toolsResolver.getBuildToolsVersion();
  }
}
