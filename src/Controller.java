import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Controller extends Thread {

	public static enum Algorithm {
		Naive, RoundRobin, RoundRobinSmart;
	}

	public static Instant startTime = Instant.now();

	private List<Computer> nodes; // noeuds
	private Queue<Task> tasks; // les tâches non réparties
	private List<Task> results; // les résultats (tâches finies)
	private RequestGenerator requestGenerator; // Générateur des requêtes

	private Algorithm algorithm; // indiquer l'algorithme de répartition des tâches
	private double algorithmParameter = 2;
	private boolean shutDown;

	public Random randomGenerator; 

	public Controller(Algorithm algorithm) {
		nodes = new LinkedList<>();
		tasks = new LinkedList<>();
		results = new LinkedList<>();
		requestGenerator = new RequestGenerator(this, RequestGenerator.RequestType.Instantaneous, 150);
		this.algorithm = algorithm;
		randomGenerator = new Random(0);

		/*
		 * for (int i = 0; i < numNodes; i++) { nodes.add(new Computer(new Ram(1000),
		 * new CpuCore(20), new Network(3), this)); }
		 */
		shutDown = false;
	}

	public Controller(Algorithm algorithm, double algorithmParameter) {
		this(algorithm);
		this.algorithmParameter = algorithmParameter;
	}
	
	public void setRequestGenerator(RequestGenerator.RequestType requestType, double paramater) {
		this.requestGenerator.setParameters(requestType, paramater);
	}
	
	public void setTaskAvgSize(double calculationSize, double memorySize) {
		requestGenerator.setRequestAvgSize(calculationSize, memorySize);
	}

	public void setAlgorithm(Algorithm algorithm) {
		this.algorithm = algorithm;
	}
	
	public void setAlgorithm(Algorithm algorithm, double algorithmParameter) {
		this.algorithm = algorithm;
		this.algorithmParameter = algorithmParameter;
	}
	
	
	public void distributeTask(Task task, Computer node) {
		task.setNodeIp(node.getIp());
		node.addTask(task);
		node.updateTaskLog(task);
	}
	

	public List<Task> getResults() {
		return results;
	}

	public Queue<Task> getTasks() {
		return tasks;
	}

	public void readNodes(String filename) {
		nodes.clear();
		double cpuCapacity;
		double ramMemory;
		double networkSpeed;
		Task.TaskType[] serviceTypes;
		String strServiceTypes;
		try {
			FileInputStream in = new FileInputStream(filename);
			Scanner sc = new Scanner(in);
			while (sc.hasNextLine()) {
				sc.nextLine();
				ramMemory = sc.nextDouble();
				cpuCapacity = sc.nextDouble();
				networkSpeed = sc.nextDouble();
				strServiceTypes = sc.next("[A-E]*");
				serviceTypes = new Task.TaskType[strServiceTypes.length()];
				for (int i = 0; i < strServiceTypes.length(); i++) {
					switch (strServiceTypes.charAt(i)) {
					case 'A':
						serviceTypes[i] = Task.TaskType.A;
						break;
					case 'B':
						serviceTypes[i] = Task.TaskType.B;
						break;
					case 'C':
						serviceTypes[i] = Task.TaskType.C;
						break;
					case 'D':
						serviceTypes[i] = Task.TaskType.D;
						break;
					case 'E':
						serviceTypes[i] = Task.TaskType.E;
						break;
					}
				}
				System.out.println(strServiceTypes);
				nodes.add(new Computer(new Ram(ramMemory), new CpuCore(cpuCapacity), new Network(networkSpeed), this,
						serviceTypes));
			}
			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void readTasks(String filename) {
		// tasks.clear();
		double calculationSize;
		double memorySize;
		String taskType;
		double timeOfCreation;
		requestGenerator.shutDown();
		try {
			FileInputStream in = new FileInputStream(filename);
			Scanner sc = new Scanner(in);
			while (sc.hasNextLine()) {
				sc.nextLine();
				calculationSize = sc.nextDouble();
				memorySize = sc.nextDouble();
				taskType = sc.next("[A-E]");
				timeOfCreation = sc.nextDouble();

				if (taskType.equals("A"))
					tasks.add(new Task(calculationSize, memorySize, Task.TaskType.A, timeOfCreation));
				else if (taskType.equals("B"))
					tasks.add(new Task(calculationSize, memorySize, Task.TaskType.B, timeOfCreation));
				else if (taskType.equals("C"))
					tasks.add(new Task(calculationSize, memorySize, Task.TaskType.C, timeOfCreation));
				else if (taskType.equals("D"))
					tasks.add(new Task(calculationSize, memorySize, Task.TaskType.D, timeOfCreation));
				else if (taskType.equals("E"))
					tasks.add(new Task(calculationSize, memorySize, Task.TaskType.E, timeOfCreation));

				System.out.println(calculationSize + " ; " + memorySize + " ; " + taskType);
			}
			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void writeResults(String filename) {
		String str;
		try {
			File file = new File(filename);
			FileWriter fw = new FileWriter(file);
			fw.write("Algorithme : " + algorithm + "\n");
			fw.write("Le temps d'attente moyen : " + this.getAverageWaitingTime() + " s\n");
			fw.write("Ecart type : " + this.getRootMeanSqare() + " s\n");
			fw.write("Le pire cas : " + this.getWorstCase() + " s\n");
			fw.write("\n");
			fw.write("Task    Type    Calculation_size(gflo)    Memory_size(MB)   "
					+ " Time_waing    Time_Execution    Time_total    Time_arrival\n");
			results.sort((Comparator<Task>) (Task t1, Task t2) -> {
				int diff = t1.getCode() - t2.getCode();
				if (diff < 0)
					return -1;
				else if (diff > 0)
					return 1;
				else
					return 0;
			});
			for (int i = 0; i < results.size(); i++) {
				str = "No." + results.get(i).getCode();
				fw.write(String.format("%-8s", str));
				fw.write(String.format("%-8s", results.get(i).getType()));
				fw.write(String.format("%-26s", String.format("%.1f", results.get(i).getCalculationSize())));
				fw.write(String.format("%-19s", String.format("%.1f", results.get(i).getMemorySize())));
				fw.write(String.format("%-14s", String.format("%.2f", results.get(i).getWaitingTime()) + 's'));
				fw.write(String.format("%-18s", String.format("%.2f", results.get(i).getExecutionTime()) + 's'));
				fw.write(String.format("%-14s", String.format("%.2f", results.get(i).getTotalTime()) + "s"));
				fw.write(String.format("%-7s", String.format("%.2f", results.get(i).getTimeOfCreation()) + "s \n"));

			}

			fw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void algorithmNaive() {
		int index = 0;
		Task temporyTask;
		while (!tasks.isEmpty()) {
			temporyTask = tasks.poll();
			while (Duration.between(startTime, Instant.now()).toNanos() < 1000000000 * temporyTask.getTimeOfCreation()) {
				try {
					sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			while (!nodes.get(index).hasService(temporyTask.getType()))
				index = (index + 1) % nodes.size();
			this.distributeTask(temporyTask, nodes.get(index));
			index = (index + 1) % nodes.size();
		}
	}

	public void algorithmRoundRobin() {
		Task temporyTask;
		Computer candidate;
		int index;
		while (!tasks.isEmpty()) {
			temporyTask = tasks.poll();
			nodes.sort((Comparator<Computer>) (Computer c1, Computer c2) -> {
				double dif = c1.getTimeTotalExecution() - c2.getTimeTotalExecution();
				if (dif == 0)
					return 0;
				else if (dif > 0)
					return 1;
				else
					return -1;
			});
			index = 0;
			while (!nodes.get(index).hasService(temporyTask.getType()))
				index++;
			candidate = nodes.get(index);
			//while (Instant.now().isBefore(temporyTask.getTimeOfCreation())) 
			while (Duration.between(startTime, Instant.now()).toNanos() < 1000000000 * temporyTask.getTimeOfCreation()) {
				try {
					sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			this.distributeTask(temporyTask, candidate);
			candidate.addLoad(temporyTask.getCalculationSize() / candidate.getCpuCapacity());
		}
	}

	/**
	 * algorithmRoundRobinSmart1() va prendre en compte les mémoires disponibles des
	 * noeuds pour qu'ils ne rament pas.
	 */
	public void algorithmRoundRobinSmart(double threshold) {
		Task temporyTask;
		Computer candidate;
		boolean hasFoundNodeAvailable;
		int candidateIndex = 0;
		while (!tasks.isEmpty()) {
			temporyTask = tasks.poll();
			// Attendre jusqu'à l'instant de création de la tâche
			while (Duration.between(startTime, Instant.now()).toNanos() < 1000000000 * temporyTask.getTimeOfCreation())  {
				try {
					sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			hasFoundNodeAvailable = false;
			while (!hasFoundNodeAvailable) {
				nodes.sort((Comparator<Computer>) (Computer c1, Computer c2) -> {
					double dif = c1.getTimeTotalExecution() - c2.getTimeTotalExecution();
					if (dif == 0)
						return 0;
					else if (dif > 0)
						return 1;
					else
						return -1;
				});
				for (int i = 0; i < nodes.size(); i++) {
					if (nodes.get(i).hasService(temporyTask.getType())
							&& nodes.get(i).getMemoryUsedTotal() < threshold * nodes.get(i).getRam().getMemoryMax()) {
						hasFoundNodeAvailable = true;
						candidateIndex = i;
						break;
					}
				}
				// Céder la CPU
				this.yield();
			}

			candidate = nodes.get(candidateIndex);

			this.distributeTask(temporyTask, candidate);
			candidate.addLoad(temporyTask.getCalculationSize() / candidate.getCpuCapacity(),
					temporyTask.getMemorySize());
		}
	}

	public List<Computer> getNodes() {
		return nodes;
	}

	public double getAverageWaitingTime() {
		if (results.isEmpty()) {
			return 0;
		} else {
			double totalWaitingTime = 0;

			// System.out.println("results size = " + results.size());
			for (int i = 0; i < results.size(); i++) {
				totalWaitingTime += results.get(i).getWaitingTime();
			}
			return totalWaitingTime / results.size();
		}
	}

	public double getRootMeanSqare() {
		double sumSquare = 0;
		double diff;
		double average = getAverageWaitingTime();
		if (results.isEmpty()) {
			return 0;
		} else {
			for (int i = 0; i < results.size(); i++) {
				diff = results.get(i).getWaitingTime() - average;
				sumSquare += diff * diff;
			}
			return Math.sqrt(sumSquare / results.size());
		}
	}

	public double getWorstCase() {
		double longestWaitingTime = 0;
		double taskWaitingTime;
		for (int i = 0; i < results.size(); i++) {
			taskWaitingTime = results.get(i).getWaitingTime();
			longestWaitingTime = taskWaitingTime > longestWaitingTime ? taskWaitingTime : longestWaitingTime;
		}
		return longestWaitingTime;
	}

	public void start() {

		super.start();
		startTime = Instant.now();
		requestGenerator.start();
		for (int i = 0; i < nodes.size(); i++) {
			nodes.get(i).start();
		}
	}

	public void shutDown() {
		
		requestGenerator.shutDown();
		for (int i = 0; i < nodes.size(); i++) {
			nodes.get(i).shutDown();
		}
		shutDown = true;
	}

	public void run() {
		while(!shutDown) {
			switch (algorithm) {
			case Naive:
				algorithmNaive();
				break;
			case RoundRobin:
				algorithmRoundRobin();
				break;
			case RoundRobinSmart:
				algorithmRoundRobinSmart(this.algorithmParameter);
				break;
			}			
		}

		System.out.println("results size = " + results.size());
		System.out.println("Average Waiting Time : " + getAverageWaitingTime());
		System.out.println("Waiting Time RMS: " + getRootMeanSqare());
		System.out.println("Longest Waiting Time : " + getWorstCase());
	}

	public static void main(String[] args) {
		Controller cloud = new Controller(Algorithm.Naive);
		// cloud.readTasks("Tasks.log");
		cloud.readNodes("Nodes.log");
		cloud.start();

	}

}
