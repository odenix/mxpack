/*
 * Copyright 2024 the MxPack project authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.odenix.mxpack.kotlin

import org.odenix.mxpack.core.MessageReader
import org.odenix.mxpack.core.MessageWriter

/** Reads an integer value that fits into `kotlin.UByte`. */
fun MessageReader.readUByteKotlin(): UByte = readUByte().toUByte()

/** Reads an integer value that fits into `kotlin.UShort`. */
fun MessageReader.readUShortKotlin(): UShort = readUShort().toUShort()

/** Reads an integer value that fits into `kotlin.UInt`. */
fun MessageReader.readUIntKotlin(): UInt = readUInt().toUInt()

/** Reads an integer value that fits into `kotlin.ULong`. */
fun MessageReader.readULongKotlin(): ULong = readULong().toULong()

/** Writes an integer value that fits into `kotlin.UByte`. */
fun MessageWriter.write(value: UByte) = writeUnsigned(value.toByte())

/** Writes an integer value that fits into `kotlin.UShort`. */
fun MessageWriter.write(value: UShort) = writeUnsigned(value.toShort())

/** Writes an integer value that fits into `kotlin.UInt`. */
fun MessageWriter.write(value: UInt) = writeUnsigned(value.toInt())

/** Writes an integer value that fits into `kotlin.ULong`. */
fun MessageWriter.write(value: ULong) = writeUnsigned(value.toLong())
