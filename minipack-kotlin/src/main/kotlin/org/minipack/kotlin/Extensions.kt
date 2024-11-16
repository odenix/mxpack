package org.minipack.kotlin

import org.minipack.java.MessageReader
import org.minipack.java.MessageWriter

fun MessageReader.readUByteKotlin(): UByte = readUByte().toUByte()

fun MessageReader.readUShortKotlin(): UShort = readUShort().toUShort()

fun MessageReader.readUIntKotlin(): UInt = readUInt().toUInt()

fun MessageReader.readULongKotlin(): ULong = readULong().toULong()

fun MessageWriter.write(value: UByte) = writeUnsigned(value.toByte())

fun MessageWriter.write(value: UShort) = writeUnsigned(value.toShort())

fun MessageWriter.write(value: UInt) = writeUnsigned(value.toInt())

fun MessageWriter.write(value: ULong) = writeUnsigned(value.toLong())
