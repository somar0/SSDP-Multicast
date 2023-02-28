package edu.udo.cs.rvs.ssdp;

import edu.udo.cs.rvs.ssdp.Listen;
import edu.udo.cs.rvs.ssdp.Worker;
import edu.udo.cs.rvs.ssdp.User;

/**
 * This class is first instantiated on program launch and IF (and only if) it
 * implements Runnable, a {@link Thread} is created and started.
 *
 */
public class SSDPPeer implements Runnable {
	@Override
	public void run() {
		Thread a = new Thread(new Listen());
		Thread b = new Thread(new Worker(new Listen()));
		Thread c = new Thread(new User(new Worker(new Listen())));
		a.start();
		b.start();
		c.start();
	}
}
