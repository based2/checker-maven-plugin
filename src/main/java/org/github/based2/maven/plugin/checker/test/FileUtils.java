package org.github.based2.maven.plugin.checker.test;

/* 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

import java.io.File;

/**
 * 
 * FileUtils is a collection of routines for common file system operations.
 * 
 * @author Dan Jemiolo (danj)
 * 
 */

public final class FileUtils {

  /**
   * Convert a list of path elements to a platform-specific path.
   * 
   * @param strings
   *          Elements in a path
   * @return an absolute path using the current platform's
   *         <code>File.separator</code>
   */
  public static String makePath(String[] strings) {
    String result = "";

    for (int i = 0; i < strings.length; i++)
      result += File.separator + strings[i];

    return result;
  }
}