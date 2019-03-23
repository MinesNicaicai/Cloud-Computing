import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;

public class UI extends JFrame {

	private static final int WIDTH = 1200;
	private static final int HEIGHT = 600;

	private String[] taskTypes = { "A", "B", "C", "D", "E" };
	private JButton addComputerButton = new JButton("Add Item");
	private JButton deleteComputerButton = new JButton("Clear Item");

	private JTextArea text = new JTextArea(taskTypes.length, 20);
	private JTextArea textCpu = new JTextArea(2, 2);
	private JTextArea textRam = new JTextArea(2, 2);
	private JTextArea textNetwork = new JTextArea(2, 2);

	private DefaultListModel<String> listmodel = new DefaultListModel<>();
	private JList<String> list = new JList<>(listmodel);

	private int count = 0;
	private boolean flag = false;

	public UI() {
		this.setSize(WIDTH, HEIGHT);
		this.setLayout(null);

		text.setEditable(false);// 只是用来显示，不能编辑
		textCpu.setEditable(true);
		textRam.setEditable(true);
		textNetwork.setEditable(true);

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);// 默认关闭操作
	}

	public void addComputer(List<Computer> nodes) {
		int widthPanel = WIDTH / nodes.size() - 30;
		for (int i = 0; i < nodes.size(); i++) {
			NodePanel panel = new NodePanel(nodes.get(i));
			panel.setSize(widthPanel, HEIGHT);
			panel.setLocation(5 + i * (widthPanel + 10), 5);
			System.out.println("add one computer !");
			Thread t = new Thread(panel);
			t.start();
			add(panel);
		}

	}

	public static void main(String[] args) {

		UI myUI = new UI();
		myUI.setLocation(100, 100);
		RequestGenerator.RequestType requestType;
		double parameter;
		double calculationSize;
		double memorySize;
		boolean goodChoice;
		double timeSimulation;

		Controller cloud = new Controller(Controller.Algorithm.Naive, 2);
		String choice;
		Scanner sc = new Scanner(System.in);
		
		
		System.out.println("Bienvenue à la simulation de Cloud Computing ! \n ");
		System.out.println("La configuration des noeuds est définie dans le fichier \"Nodes.log\", "
				+ "veuillez d'abord régler les noeuds selon le format prédéfini. ");
		System.out.println("Veuillez choisir l'état d'avancement des requêtes : ");
		System.out.println("A - Processus de Poisson");
		System.out.println("B - Processus instantanné");
		System.out.println("C - Lecture du fichier \"Tasks.log\"");
		

		goodChoice = false;
		while (!goodChoice) {
			choice = sc.next();
			if (choice.equals("A")) {
				requestType = RequestGenerator.RequestType.Poisson;
				System.out.println("Veuillez donner le nombre de requêtes créées par seconde");
				parameter = sc.nextDouble();
				cloud.setRequestGenerator(requestType, parameter);
				System.out.println("Veuillez donner la quantité moyenne du calcal des tâches (en gflo)");
				calculationSize = sc.nextDouble();
				System.out.println("Veuillez donner la taille moyenne de la mémoire occupée par des tâches (en Mo)");
				memorySize = sc.nextDouble();
				cloud.setTaskAvgSize(calculationSize, memorySize);
				goodChoice = true;
			} else if (choice.equals("B")) {
				requestType = RequestGenerator.RequestType.Instantaneous;
				System.out.println("Veuillez donner le nombre des requêtes");
				parameter = sc.nextDouble();
				cloud.setRequestGenerator(requestType, parameter);
				System.out.println("Veuillez donner la quantité moyenne du calcal des tâches (en gflo)");
				calculationSize = sc.nextDouble();
				System.out.println("Veuillez donner la taille moyenne de la mémoire occupée par des tâches (en Mo)");
				memorySize = sc.nextDouble();
				cloud.setTaskAvgSize(calculationSize, memorySize);
				goodChoice = true;
			} else if (choice.equals("C")) {
				requestType = null;
				cloud.readTasks("Tasks.log");
				goodChoice = true;
			} else {
				System.out.println("Choix non identifié. Veuillez recommencer à choisir : \n");
			}
		}
		
		
		System.out.println();
		System.out.println("Veuillez choisir l'algorithme de la répartition des tâches pour le contrôleur maître");
		System.out.println("A - Algorithme naïf");
		System.out.println("B - Algorithme Round-Robin");
		System.out.println("C - Algorithme Round-Robin-Smart");
		

		goodChoice = false;
		while (!goodChoice) {
			choice = sc.next();
			if (choice.equals("A")) {
				cloud.setAlgorithm(Controller.Algorithm.Naive);
				goodChoice = true;
			}
			else if (choice.equals("B")) {
				cloud.setAlgorithm(Controller.Algorithm.RoundRobin);
				goodChoice = true;
			}
			else if (choice.equals("C")) {
				System.out.println("Donnez le seuil d'utilisation de la mémoire (mémoire utilisée max/mémoire vive max)");
				parameter = sc.nextDouble();
				cloud.setAlgorithm(Controller.Algorithm.RoundRobinSmart, parameter);
				goodChoice = true;
			}
			else {
				System.out.println("Choix non identifié. Veuillez recommencer à choisir : \n");
			}
		}
		
		System.out.println("\nVeuillez rentrer la durée de la simulation (en seconde)");
		timeSimulation = sc.nextDouble();
		System.out.println("\nConfiguration finie.");
		cloud.readNodes("Nodes.log");
		myUI.setVisible(true);

		List<Computer> nodes = cloud.getNodes();

		myUI.addComputer(nodes);
		myUI.setVisible(true);

		cloud.start();
		try {
			Thread.sleep(Math.round(1000 * timeSimulation));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		cloud.shutDown();
		
		System.out.println("\nVeuillez rentrer le nom de fichier pour enregistrer les statistiques");
		
		cloud.writeResults(sc.next());

		System.out.println("\nProgramme fini.");
		System.exit(0);

	}

}
