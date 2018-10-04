package game;

import java.io.*;
import java.net.*;

public class ServerManager
{
	static InetAddress IPAddress;
	static int port;
	static DatagramSocket serverSocket;
	public static boolean connected = false;
	static byte[] packetBuffer = new byte[DataPacket.packetSize];

	ServerManager(int portSet) throws IOException
	{
		serverSocket = new DatagramSocket(portSet);
		port = portSet;
	}

	public static void unbindPort()
	{
		serverSocket.close();
	}

	public static void sendData(byte[] dataPacket) throws IOException
	{
		if (connected)
		{
			DatagramPacket sendPacket = new DatagramPacket(dataPacket, dataPacket.length, IPAddress.getLocalHost(), port);
			serverSocket.send(sendPacket);
		}
	}

	public static byte[] receiveData() throws IOException, SocketException
	{
		DatagramPacket receivePacket = new DatagramPacket(packetBuffer, packetBuffer.length);
		serverSocket.receive(receivePacket);
		connected = true;
		IPAddress = receivePacket.getAddress();
		port = receivePacket.getPort();
		return receivePacket.getData();
	}

	/*
	 * public void writeIntToClient(int i) { try { out.writeInt(i); } catch (IOException e) { e.printStackTrace(); } }
	 * 
	 * public void writeDoubleToClient(double d) { try { out.writeDouble(d); } catch (IOException e) { Logger.log("Socket write error.", this.getClass()); } }
	 * 
	 * public int readIntFromClient() throws IOException { return in.readInt(); }
	 * 
	 * public double readDoubleFromClient() throws IOException { return in.readDouble(); }
	 */
}
