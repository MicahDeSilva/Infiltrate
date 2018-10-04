package game;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.net.SocketException;

public class DataPacket
{
	static int packetSize = Main.unitCount * 4 * 4 * 3; // Number of units * data size (float = 4 bytes) * Data points per unit (4) * Include bullets
	static byte[] dataPacket = new byte[packetSize];
	static volatile float[] receivedPacket = new float[packetSize / 4];
	static int offset = 4, bulletOffset = 0;
	static int dataPointsPerUnit = 4;

	public static void add(double d)
	{
		byte[] b = DataAlgorithms.toByteArray((float) d);
		System.arraycopy(b, 0, dataPacket, offset, b.length);
		offset += 4;
	}

	public static void add(Unit unit)
	{
		byte[] b = DataAlgorithms.toByteArray((float) unit.x);
		System.arraycopy(b, 0, dataPacket, offset, b.length);
		offset += 4;
		b = DataAlgorithms.toByteArray((float) unit.y);
		System.arraycopy(b, 0, dataPacket, offset, b.length);
		offset += 4;
		b = DataAlgorithms.toByteArray((float) unit.direction);
		System.arraycopy(b, 0, dataPacket, offset, b.length);
		offset += 4;
		b = DataAlgorithms.toByteArray(unit.alive ? Float.MIN_VALUE : Float.MAX_VALUE);
		System.arraycopy(b, 0, dataPacket, offset, b.length);
		offset += 4;
	}

	public static void clearDataPacket()
	{
		dataPacket = new byte[packetSize];
		offset = 0;
		bulletOffset = 0;
	}

	public static void add(Bullet bullet)
	{
		byte[] b = DataAlgorithms.toByteArray((float) bullet.x);
		System.arraycopy(b, 0, dataPacket, Main.unitCount * dataPointsPerUnit * 4 + bulletOffset, b.length);
		bulletOffset += 4;
		b = DataAlgorithms.toByteArray((float) bullet.y);
		System.arraycopy(b, 0, dataPacket, Main.unitCount * dataPointsPerUnit * 4 + bulletOffset, b.length);
		bulletOffset += 4;
		b = DataAlgorithms.toByteArray((float) bullet.direction);
		System.arraycopy(b, 0, dataPacket, Main.unitCount * dataPointsPerUnit * 4 + bulletOffset, b.length);
		bulletOffset += 4;
		b = DataAlgorithms.toByteArray(bullet.instantiated ? Float.MIN_VALUE : Float.MAX_VALUE);
		bulletOffset += 4;
	}

	public static void parse(byte[] receiveData)
	{
		ByteBuffer byteBuffer = ByteBuffer.wrap(receiveData);
		int i = 0;
		while (byteBuffer.hasRemaining())
		{
			receivedPacket[i] = byteBuffer.getFloat();
			i++;
		}
	}

	class DataReceiveThread implements Runnable
	{
		@Override
		public void run()
		{
			while (true)
			{

				if (Main.connectionType == Main.Connection.SERVER)
				{
					try
					{
						parse(ServerManager.receiveData());
					}
					catch (SocketException e)
					{
						break;
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}

				if (Main.connectionType == Main.Connection.CLIENT)
				{
					try
					{
						parse(ClientManager.receiveData());
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void startReceivingData()
	{
		DataReceiveThread main = new DataReceiveThread();
		new Thread(main).start();
	}

}
