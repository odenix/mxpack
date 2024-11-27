/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.example;

import net.jqwik.api.ForAll;
import org.minipack.core.MessageReader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class ReadMap extends Example {
  // -8<- [start:snippet]
  Map<String, Integer> read(MessageReader reader) throws IOException {
    var size = reader.readMapHeader();
    // JDK 19+: var map = HashMap.<String, Integer>newHashMap(size);
    var map = new HashMap<String, Integer>();
    for (int i = 0; i < size; i++) {
      var key = reader.readString();
      var value = reader.readInt();
      map.put(key, value);
    }
    return map;
  }
  // -8<- [end:snippet]

  @net.jqwik.api.Example
  void test(@ForAll Map<String, Integer> map) throws IOException {
    new WriteMap().test(map);
  }
}
