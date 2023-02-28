package edu.udo.cs.rvs.ssdp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;

public class Listen implements Runnable {
	/*
	 * Liste, um die empfangene DatagramPackete dem Worker-Thread zur Verfügung
	 * stellt
	 */
	public static LinkedList<DatagramPacket> datagrampacketeliste = new LinkedList<>();
	/* MulticastSocket stellt sich dem User-Thread zur Verfügung */
	public static MulticastSocket ms;

	@Override
	/* Um mehrere Programmabläufe innerhalb des selben Prozesses durchzuführen */
	public void run() {
		try {
			/* Multisocket auf dem Port 1900 öffnen */
			ms = new MulticastSocket(1900);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		InetAddress ia = null;
		try {
			/* Bistimmen den Name der Multicast-gruppe */
			ia = InetAddress.getByName("239.255.255.250");
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			/* Die Multicast-gruppe mit der eingegebenen Name beitreten */
			ms.joinGroup(ia);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		/* solange der Socket verbunden, nicht leer und nicht geschlossen ist */
		while (ms != null && ms.isBound() && !ms.isClosed()) {
			try {
				/*
				 * Die Informationen des empfangene DatagramPackete in einem Byte Array
				 * speichern
				 */
				byte[] data = new byte[ms.getReceiveBufferSize()];
				DatagramPacket dp = new DatagramPacket(data, data.length);
				/* Die DatagramPackete werden empfangen */
				ms.receive(dp);
				/* Die Datagrammpackete werden in der Liste gespeichert */
				synchronized (datagrampacketeliste) {
					datagrampacketeliste.add(dp);
				}
			} catch (SocketException sexc) {
				// Fehlerbehandlung
				sexc.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		/* Das Multi.Socket wird automatisch geschlossen bei der Programmende */
		ms.close();
	}
}
