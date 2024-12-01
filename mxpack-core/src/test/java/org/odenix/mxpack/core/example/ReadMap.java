/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.core.example;

import net.jqwik.api.ForAll;
import net.jqwik.api.constraints.Size;
import org.odenix.mxpack.core.MessageReader;

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
  void test(@ForAll @Size(max = 16) Map<String, Integer> map) throws IOException {
    new WriteMap().test(map);
  }
}
