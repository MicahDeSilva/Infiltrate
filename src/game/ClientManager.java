package game;

import java.io.*;
import java.net.*;

public class ClientManager
{
	static DatagramSocket clientSocket;
	static InetAddress IPAddress;
	static int port;
	static byte[] packetBuffer = new byte[DataPacket.packetSize];

	ClientManager(String host, int port) throws UnknownHostException, SocketException
	{
		clientSocket = new DatagramSocket();
		IPAddress = InetAddress.getByName(host);
		ClientManager.port = port;
	}
	
	public static void sendData(byte[] dataPacket) throws IOException
	{
		DatagramPacket sendPacket = new DatagramPacket(dataPacket, dataPacket.length, IPAddress, port);
		clientSocket.send(sendPacket);
	}

	public static byte[] receiveData() throws IOException
	{
		DatagramPacket receivePacket = new DatagramPacket(packetBuffer, packetBuffer.length);
		clientSocket.receive(receivePacket);
		return receivePacket.getData();
	}
	
	/*
	 * public void writeIntToServer(int i) { try { out.writeInt(i); } catch (IOException e) { e.printStackTrace(); } }
	 * 
	 * public void writeDoubleToServer(double d) { try { out.writeDouble(d); } catch (IOException e) { e.printStackTrace(); } }
	 * 
	 * public int readIntFromServer() throws IOException { return in.readInt(); }
	 * 
	 * public double readDoubleFromServer() throws IOException { return in.readDouble(); }
	 */
}
