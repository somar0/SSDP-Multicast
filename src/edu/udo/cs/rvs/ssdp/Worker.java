package edu.udo.cs.rvs.ssdp;

import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

public class Worker implements Runnable {
	/*
	 * Liste, die die Geräte, die dienste im Network anbieten, dem Worker-Thread zur
	 * Verfügung stellt
	 */
	public static LinkedList<String[]> gerateliste = new LinkedList<>();
	/* Objekt von dem Listen-Threads */
	Listen datagrampacketeliste;

	/*
	 * Damit das Worker-Threads einen Zugriff auf die Liste Der empfangenen
	 * Datagramme des Listen-Threads hat
	 */
	public Worker(Listen x) {
		datagrampacketeliste = x;
	}

	@Override
	/* Um mehrere Programmabläufe innerhalb des selben Prozesses durchzuführen */
	public void run() {
		/* soll bis zum Programmende in Endlosschleife laufen */
		while (true) {
			/*
			 * fals die Liste Der empfangenen Datagramme leer ist, schläft der Thread einige
			 * Millisekunden
			 */
			if (Listen.datagrampacketeliste.isEmpty()) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			/* Wenn nun Datagrampackete vorliegen */
			else {
				DatagramPacket dp;
				/* Das älteste Datagramepacket nehmen und aus der Liste entfernen */
				synchronized (Listen.datagrampacketeliste) {
					dp = Listen.datagrampacketeliste.removeFirst();
				}
				/* Die Informationen des Datagrampacket in einem Byte Array speichern */
				byte[] data = dp.getData();
				/*
				 * Die Zeile des Datagrampacket in einem Array speichern, Jede Zeile als eines
				 * Element des Array
				 */
				String[] lines = new String(data, StandardCharsets.UTF_8).split("\r\n");
				/*
				 * Die erste Zeile wird geprüft, um zu wissen, ob es um einen Unicast Antwort
				 * geht
				 */
				if (lines[0].contentEquals("HTTP/1.1 200 OK")) {
					/* Speichern die wichtigsten Informationen in einem Array von zwei Elemente */
					String[] uni = new String[2];
					/* Die anderen Zeilen durchlaufen */
					for (int i = 1; i < lines.length; i++) {
						/* Die leere Zeile schließt die Antwort ab */
						if (lines[i].isEmpty()) {
							break;
						}
						/* falls den Inhalt mit ST anfängt */
						if (lines[i].startsWith("ST")) {
							/* extrahieren und speichern den Service-Type */
							uni[0] = lines[i].substring(4);
						}
						/* falls den Inhalt mit USN anfängt */
						if (lines[i].startsWith("USN")) {
							/* extrahieren und speichern nur die UUID */
							uni[1] = lines[i].split("uuid:", 2)[1].split(":", 2)[0];
						}
					}

					/* falls das Gerät nicht defekt ist */
					if (uni[0] != null && uni[1] != null) {
						/* wird das Gerät zu der Liste der bekannten Geräte hinzugefügt */
						synchronized (gerateliste) {
							gerateliste.addLast(uni);
						}
					}
					/* sonst wird ausgegeben, dass Eine defektes Antwort empfangen wurde */
					else {
						System.out.println("Eine defektes Antwort wurde empfangen !");
					}
				}
				boolean anmeldet = false;
				/*
				 * Die erste Zeile wird geprüft um zu wissen, ob es um einen Multicast Antwort
				 * geht
				 */ if (lines[0].contentEquals("NOTIFY * HTTP/1.1")) {
					/* Speichern die wichtigsten Informationen in einem Array von zwei Elemente */
					String[] multi = new String[2];
					for (int i = 1; i < lines.length; i++) {
						/* Die leere Zeile schließt die Antwort ab */
						if (lines[i].isEmpty()) {
							break;
						}
						/* falls den Inhalt mit NT: anfängt */
						if (lines[i].startsWith("NT:")) {
							/* extrahieren und speichern den Service-Type */
							multi[0] = lines[i].substring(4);
						}
						/* falls den Inhalt mit USN anfängt */
						if (lines[i].startsWith("USN")) {
							/* extrahieren und speichern nur die UUID */
							multi[1] = lines[i].split("uuid:", 2)[1].split(":", 2)[0];
						}
						/* teste ob das Gerät sich anmeldet oder abmeldet */
						if (lines[i].contentEquals("NTS: ssdp:alive")) {
							anmeldet = true;
						}
						if (lines[i].contentEquals("NTS: ssdp:byebye")) {
							anmeldet = false;
						}
					}
					/* falls das Gerät nicht defekt ist */
					if (multi[0] != null && multi[1] != null) {
						/* wird das Gerät zu der Liste der bekannten Geräte hinzugefügt */
						synchronized (gerateliste) {
							gerateliste.addLast(multi);
						}
					}
					/*
					 * Geräte die sich abmelden, werden aus der Liste der bekannten Geräte entfernt
					 */
					if (multi[0] != null && multi[1] != null && !anmeldet) {
						synchronized (gerateliste) {
							for (int j = 0; j < gerateliste.size(); j++) {
								if (gerateliste.get(j)[1].equals(multi[1])) {
									gerateliste.remove(j);
									j--;
								}
							}
						}
					}
				}
			}
		}
	}
}
