package game;

import java.nio.ByteBuffer;

public class DataAlgorithms
{
	public static byte[] toByteArray(float value) {
	    byte[] bytes = new byte[4];
	    ByteBuffer.wrap(bytes).putFloat(value);
	    return bytes;
	}

	public static double toFloat(byte[] bytes) {
	    return ByteBuffer.wrap(bytes).getFloat();
	}
}
