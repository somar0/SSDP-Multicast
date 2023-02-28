package edu.udo.cs.rvs.ssdp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class User implements Runnable {
	/* Objekt von dem Worker-Threads */
	Worker gerateliste;
	/* Objekt von dem Listen-Threads */
	Listen ms;

	/*
	 * Damit das User-Threads einen Zugriff auf die Liste des bekannten Geräte des
	 * Worker-Threads hat
	 */
	public User(Worker x) {
		gerateliste = x;
	}

	/*
	 * Damit das User-Threads einen Zugriff auf das MulticastSocket des
	 * Listen-Threads hat
	 */
	public User(Listen y) {
		ms = y;
	}

	@Override
	/* Um mehrere Programmabläufe innerhalb des selben Prozesses durchzuführen */
	public void run() {
		/* Um die StreamDaten als String lesen können */
		InputStreamReader streamReader = new InputStreamReader(System.in, StandardCharsets.UTF_8);
		BufferedReader reader = new BufferedReader(streamReader);
		/* soll bis zum Programmende in Endlosschleife laufen */
		while (true) {

			String input = null;
			try {
				/*
				 * falls der reader nicht bereit zu Lesen, schläft der Thread einige
				 * Millisekunden
				 */
				while (!reader.ready()) {
					Thread.sleep(10);
				}
				/* liest die Eingaben von der konsole, die der Benutzer angibt */
				input = reader.readLine();
				/*
				 * wird den MulticastSocket geschlossen und das Programm terminiert, false EXIT
				 * eingegeben wurde
				 */
				if (input.equalsIgnoreCase("EXIT")) {
					System.out.println("MulticastSocket Socket ist geshclosen und das Program terminiert !");
					Listen.ms.close();
					System.exit(0);
				}
				/* wird die Liste der bekannten Geräte geleert, false CLEAR eingegeben wurde */
				else if (input.equalsIgnoreCase("CLEAR")) {
					synchronized (Worker.gerateliste) {
						Worker.gerateliste.clear();
						System.out.println("Liste der bekannten Geräte Wurde geleert");
					}
				}
				/* false LIST eingegeben wurde */
				else if (input.equalsIgnoreCase("LIST")) {
					/* wenn keine Geräte vorliegen */
					if (Worker.gerateliste.isEmpty()) {
						System.out.println("Liste der bekannten Geräte ist noch leer !");

					}
					/* Wenn nun Geräte vorliegen werden die ausgegeben */
					else {
						System.out.println("Liste der bekannten Geräte :");
						synchronized (Worker.gerateliste) {
							for (int i = 0; i < Worker.gerateliste.size(); i++) {
								String[] a = Worker.gerateliste.get(i);
								System.out.print(a[1] + " - " + a[0] + "\r\n");

							}
						}
					}
				}
				/* false SCAN eingegeben wurde */
				else if (input.equalsIgnoreCase("SCAN")) {
					byte[] data;
					/*
					 * Our suchanfrage mit einer echten UUID ersetzt werden und die als String
					 * erstellen
					 */
					String suchanfrage = "M-SEARCH * HTTP/1.1\r\nS: uuid:" + UUID.randomUUID().toString()
							+ "\nHOST: 239.255.255.250:1900\nMAN: \"ssdp:discover\"\nST: ssdp:all\r\n\n";
					/* Speichern unsere Suchanfrage als Bytes in einem Byte Array */
					data = suchanfrage.getBytes();
					/* Erstellen ein Packet von unsere data ersetzt für 239.255.255.250:1900 */
					DatagramPacket datagramPacket = new DatagramPacket(data, data.length,
							InetAddress.getByName("239.255.255.250"), 1900);
					/* Senden das packet über das MulticastSocket des Listen-Threads */
					Listen.ms.send(datagramPacket);
					System.out.println("Packet wurde gesendet ! ");
				}
				/* Wenn ungültig Eingabe angegeben wurde */
				else {
					System.out.println("ungültig Eingabe!, kann nur / EXIT | CLEAR | LIST | SCAN / sein !");
					Thread.sleep(10);
				}
			} catch (InterruptedException e) {
				// ignore
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}