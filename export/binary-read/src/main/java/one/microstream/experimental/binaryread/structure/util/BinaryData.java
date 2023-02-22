package one.microstream.experimental.binaryread.structure.util;

/*-
 * #%L
 * binary-read
 * %%
 * Copyright (C) 2023 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import one.microstream.experimental.binaryread.exception.IncorrectByteArrayLength;

import java.nio.ByteBuffer;

public class BinaryData
{

    private final static boolean REVERSE = true; // FIXME config

    private final static StringBuilder STRING_BUILDER = new StringBuilder();

    /**
     * Converts bytes from the {@code ByteBuffer} to a long value. An offset value of 0 is assumed.
     * The byte array can be longer than just holding the bytes for the long value.
     *
     * @param buffer The {@link ByteBuffer}  containing the long value from position 0 onwards.
     * @return The long value.
     */
    public static long bytesToLong(final ByteBuffer buffer)
    {
        return bytesToLong(buffer, 0);
    }

    /**
     * Converts bytes within the {@code ByteBuffer} from the position indicated by offset to a long value.
     * The byte buffer must be at least containing {@code Long.BYTES} after the offset or an exception is thrown
     * indicating not enough data is available for the conversion.
     *
     * @param buffer The {@link ByteBuffer} containing the long value from position offset onwards.
     * @param offset The offset
     * @return The long value.
     */
    public static long bytesToLong(final ByteBuffer buffer, final int offset)
    {
        if (buffer.capacity() < offset + Long.BYTES)
        {
            throw new IncorrectByteArrayLength("Long", Long.BYTES);
        }

        final long result = buffer.getLong(offset);
        if (REVERSE)
        {
            return Long.reverseBytes(result);
        }
        else
        {
            return result;
        }
    }

    /**
     * Converts bytes within the {@code ByteBuffer} to an int value. An offset value of 0 is assumed.
     * The byte array can be longer than just holding the bytes for the int value.
     *
     * @param buffer The {@link ByteBuffer} containing the integer value from position 0 onwards.
     * @return The integer value.
     */
    public static int bytesToInt(final ByteBuffer buffer)
    {
        return bytesToInt(buffer, 0);
    }

    /**
     * Converts bytes within the {@code ByteBuffer} from the position indicated by offset to a integer value.
     * The byte buffer must be at least containing {@code Integer.BYTES} after the offset or an exception is thrown
     * indicating not enough data is available for the conversion.
     *
     * @param buffer The {@link ByteBuffer} containing the integer value from position offset onwards.
     * @param offset The offset
     * @return The integer value.
     */
    public static int bytesToInt(final ByteBuffer buffer, final int offset)
    {
        if (buffer.capacity() < offset + Integer.BYTES)
        {
            throw new IncorrectByteArrayLength("Integer", Integer.BYTES);
        }

        final int result = buffer.getInt(offset);

        if (REVERSE)
        {
            return Integer.reverseBytes(result);
        }
        else
        {
            return result;
        }

    }

    /**
     * Converts bytes within the {@code ByteBuffer} to a short value. An offset value of 0 is assumed.
     * The byte array can be longer than just holding the bytes for the short value.
     *
     * @param buffer The {@link ByteBuffer} containing the short value from position 0 onwards.
     * @return The short value.
     */
    public static short bytesToShort(final ByteBuffer buffer)
    {
        return bytesToShort(buffer, 0);
    }

    /**
     * Converts bytes within the {@code ByteBuffer} from the position indicated by offset to a short value.
     * The byte buffer must be at least containing {@code Short.BYTES} after the offset or an exception is thrown
     * indicating not enough data is available for the conversion.
     *
     * @param buffer The {@link ByteBuffer} containing the short value from position offset onwards.
     * @param offset The offset
     * @return The short value
     */
    public static short bytesToShort(final ByteBuffer buffer, final int offset)
    {
        if (buffer.capacity() < offset + Short.BYTES)
        {
            throw new IncorrectByteArrayLength("Short", Short.BYTES);
        }

        final short result = buffer.getShort(offset);

        if (REVERSE)
        {
            return Short.reverseBytes(result);
        }
        else
        {
            return result;
        }
    }

    /**
     * Converts bytes within the {@code ByteBuffer} to a double value. An offset value of 0 is assumed.
     * The byte array can be longer than just holding the bytes for the double value.
     *
     * @param buffer The {@link ByteBuffer} containing the double value from position 0 onwards.
     * @return The double value.
     */
    public static double bytesToDouble(final ByteBuffer buffer)
    {
        return bytesToDouble(buffer, 0);
    }

    /**
     * Converts bytes within the {@code ByteBuffer} from the position indicated by offset to a double value.
     * The byte buffer must be at least containing {@code Double.BYTES} after the offset or an exception is thrown
     * indicating not enough data is available for the conversion.
     *
     * @param buffer The {@link ByteBuffer} containing the double value from position offset onwards.
     * @param offset The offset
     * @return The double value.
     */
    public static double bytesToDouble(final ByteBuffer buffer, final int offset)
    {
        if (buffer.capacity() < offset + Double.BYTES)
        {
            throw new IncorrectByteArrayLength("Double", Double.BYTES);
        }

        final double result = buffer.getDouble(offset);

        if (REVERSE)
        {
            return reverseDouble(result);
        }
        else
        {
            return result;
        }
    }

    private static double reverseDouble(final double d)
    {
        final long bits = Double.doubleToRawLongBits(d);
        final long reversedBits = Long.reverseBytes(bits);
        return Double.longBitsToDouble(reversedBits);
    }

    /**
     * Converts bytes within the {@code ByteBuffer} to a float value. An offset value of 0 is assumed.
     * The byte array can be longer than just holding the bytes for the float value.
     *
     * @param buffer The {@link ByteBuffer} containing the float value from position 0 onwards.
     * @return The float value.
     */
    public static float bytesToFloat(final ByteBuffer buffer)
    {

        return bytesToFloat(buffer, 0);

    }

    /**
     * Converts bytes within the {@code ByteBuffer} from the position indicated by offset to a float value.
     * The byte buffer must be at least containing {@code Float.BYTES} after the offset or an exception is thrown
     * indicating not enough data is available for the conversion.
     *
     * @param buffer The {@link ByteBuffer} containing the long value from position offset onwards.
     * @param offset The offset
     * @return the float value.
     */
    public static float bytesToFloat(final ByteBuffer buffer, final int offset)
    {
        if (buffer.capacity() < offset + Float.BYTES)
        {
            throw new IncorrectByteArrayLength("Float", Float.BYTES);
        }

        final float result = buffer.getFloat(offset);

        if (REVERSE)
        {
            return reverseFloat(result);
        }
        else
        {
            return result;
        }
    }

    private static float reverseFloat(final float f)
    {
        final int bits = Float.floatToRawIntBits(f);
        final int reversedBits = Integer.reverseBytes(bits);
        return Float.intBitsToFloat(reversedBits);
    }

    /**
     * Converts bytes within the {@code ByteBuffer} to a string value.
     * The byte array can be longer than just holding the bytes for the string value.
     * Not Thread-safe.
     *
     * @param buffer The {@link ByteBuffer} containing the String value from position offset onwards.
     * @param offset The offset
     * @return The String
     */
    public static String bytesToString(final ByteBuffer buffer, final int offset)
    {
        // String Structure [Bytes Length][String length][data]
        // Bytes Length = [String Length] * 2 + 16
        STRING_BUILDER.setLength(0);  // Reset

        final long stringLength = bytesToLong(buffer, offset + Long.BYTES);
        int idx = offset + 2 * Long.BYTES;

        for (int j = 0; j < stringLength; j++)
        {

            final int ch = buffer.get(idx) + buffer.get(idx + 1) * 256;
            STRING_BUILDER.append((char) ch);

            idx += 2;
        }
        return STRING_BUILDER.toString();
    }
}
