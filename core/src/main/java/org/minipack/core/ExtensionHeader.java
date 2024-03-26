/*
 * Copyright 2024 the minipack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.minipack.core;

/**
 * The header of a MessagePack extension value.
 *
 * @param length the byte length of the extension value's payload
 * @param type the numeric identifier of the extension type
 */
public record ExtensionHeader(int length, byte type) {}
