import java.awt.Color;
import java.awt.Graphics;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class NodePanel extends JPanel implements Runnable {

	private static final DecimalFormat df = new DecimalFormat("0.00");
	private JLabel labelCpu;
	private JLabel labelRam;
	private JLabel labelAlgorithm;
	private Computer host;
	private boolean shutDown = false;

	public NodePanel(Computer host) {
		
		super();
		
		labelCpu = new JLabel("CPU ratio: ");
		labelRam = new JLabel("RAM used : ");
		this.host = host;
		
		add(new JLabel("Computer IP: "+ host.getIp()));
		add(labelCpu);
		add(labelRam);
	}

	public void paint(Graphics g) {
		super.paint(g);
		//this.setBackground(Color.BLUE);
		
		labelCpu.setText("CPU ratio: " + df.format(host.getCpuUsedRate()*100) + "%");
		labelRam.setText("RAM used : " + df.format(host.getRamUsedRate()) + " Mo");
		double percentage;
		
		g.setColor(Color.GREEN);
		int numTasks = host.getNumTasks();
		for (int i = 0; i < numTasks; i++) {
			//System.out.println("IP " + host.getIp() + " has " + numTasks + " tasks");
			percentage = host.getTaskLogValue(i);
			if (percentage < 0.99)
				g.setColor(Color.GREEN);
			else
				g.setColor(Color.BLUE);
			g.fillRect(30, 13 * i + 45, (int)((this.getWidth()-15) *  percentage), 10);
		}
	}

	public void shutDown() {
		shutDown = true;
	}

	@Override
	public void run() {
		while (!shutDown) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			repaint();
		}
	}

}
