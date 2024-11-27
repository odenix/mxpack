/*
 * Copyright 2024 the MiniPack contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core.example;

import net.jqwik.api.ForAll;
import org.minipack.core.MessageReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class ReadArray extends Example {
  // -8<- [start:snippet]
  List<String> read(MessageReader reader) throws IOException {
    var size = reader.readArrayHeader();
    var list = new ArrayList<String>(size);
    for (int i = 0; i < size; i++) {
      var element = reader.readString();
      list.add(element);
    }
    return list;
  }
  // -8<- [end:snippet]

  @net.jqwik.api.Example
  void test(@ForAll List<String> list) throws IOException {
    new WriteArray().test(list);
  }
}
