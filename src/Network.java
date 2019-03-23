
public class Network extends Thread {
	private Computer host; // l'ordinateur à qui Network est associé
	private Controller cloud; // le contrôleur maître à qui Network est associé
	private double transferSpeed = 3; // en Mo/s
	private static final double transferSpeedDefault = 3;
	private boolean shutDown = false;

	public Network(double speed) {
		this.transferSpeed = speed;
	}

	public Network() {
		this(transferSpeedDefault);
	}

	public void setting(Computer computer) {
		this.host = computer;
		this.cloud = computer.getCloud();
	}

	public void shutDown() {
		shutDown = true;
	}

	public void run() {
		while (!shutDown) {
			synchronized (host.getResultQueue()) {
				if (!host.getResultQueue().isEmpty())
					synchronized (cloud.getResults()) {
						cloud.getResults().add(host.getResultQueue().poll());
					}
			}
			try {
				sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
