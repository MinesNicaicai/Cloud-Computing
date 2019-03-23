
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Computer {
	private static int ipGenerator = 0;
	private int ip; // l'identité de l'ordinateur
	private CpuCore core; // CPU 
	private Ram ram; // RAM
	private Network network; // port de réseau
	private Controller cloud; // contrôleur maître
	private int numCore;
	private boolean isFree;
	private Queue<Task> taskQueue; // file d'attente des tâches non finies
	private Queue<Task> resultQueue; // file des résultats (tâches finies)
	private TaskLog taskLog; // enregistrement des états des tâches en attente et des informations de l'ordinateur
	
	private double timeTotalExecution; // timeTotalExecution est le temps d'exécution total pour finir toutes les tâches dans le file actuel.
	private double memoryUsedTotal; // memoryUsedTotal est la somme des mémoires occupée des tâches distribuées
	private boolean serviceBitmap[] = { false, false, false, false, false }; // Indiquer les types des tâches que l'ordinateur traite 

	public Computer(Ram ram, CpuCore cpu, Network network, Controller cloud, Task.TaskType... types) {
		this.ip = ipGenerator;
		ipGenerator++;
		this.ram = ram;
		this.core = cpu;
		this.network = network;
		this.cloud = cloud;
		this.numCore = 1;
		this.taskLog = new TaskLog(1000);
		taskQueue = new LinkedList<>();
		resultQueue = new LinkedList<>();

		serviceBitmap[0] = true;
		for (int i = 0; i < types.length; i++) {
			switch (types[i]) {
			case A:
				serviceBitmap[0] = true;
				break;
			case B:
				serviceBitmap[1] = true;
				break;
			case C:
				serviceBitmap[2] = true;
				break;
			case D:
				serviceBitmap[3] = true;
				break;
			case E:
				serviceBitmap[4] = true;
				break;
			}
		}

		this.core.setComputer(this);
		this.network.setting(this);

		timeTotalExecution = 0;
		memoryUsedTotal = 0;
	}

	public boolean isFree() {
		return isFree;
	}

	public void start() {
		this.core.start();
		this.network.start();
	}

	public void receiveTask(Task t) {
		taskQueue.add(t);
	}

	public double getMomeryFree() {
		return ram.getMemoryFree();
	}

	public double getCpuCapacity() {
		return core.getCapacity();
	}

	public double getTimeTotalExecution() {
		return timeTotalExecution;
	}

	public double getMemoryUsedTotal() {
		return memoryUsedTotal;
	}

	public void addLoad(double burstTime, double memoryNeeded) {
		timeTotalExecution += burstTime;
		memoryUsedTotal += memoryNeeded;
	}

	public void addLoad(double burstTime) {
		timeTotalExecution += burstTime;
	}

	public void updateTaskLog(Task task, double cpuUsingRate) {
		taskLog.cpuRate = cpuUsingRate;
		taskLog.ramUsed = ram.getMemoryUsed();
		taskLog.update(task);
		//System.out.println("IP " + ip + " task num : " + taskLog.ratesFinished.size());

	}

	public void updateTaskLog(Task task) {
		taskLog.update(task);
	}

	public Task dropTask() {
		synchronized (taskQueue) {
			return taskQueue.poll();
		}
	}

	public boolean hasTask() {
		synchronized (taskQueue) {
			return !taskQueue.isEmpty();
		}
	}

	public Queue<Task> getResultQueue() {
		return resultQueue;
	}

	public Queue<Task> getTaskQueue() {
		return taskQueue;
	}

	public Ram getRam() {
		return ram;
	}

	public Controller getCloud() {
		return cloud;
	}

	public int getIp() {
		return ip;
	}

	public void shutDown() {

		while (!this.core.isFree()) {
			try {
				cloud.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.core.shutDown();
		this.network.shutDown();
	}

	public void printRecorder() {
		this.core.printRecorder();
	}

	public boolean hasService(Task.TaskType type) {
		switch (type) {
		case A:
			return serviceBitmap[0];
		case B:
			return serviceBitmap[1];
		case C:
			return serviceBitmap[2];
		case D:
			return serviceBitmap[3];
		case E:
			return serviceBitmap[4];
		default:
			return false;
		}
	}

	public void addTask(Task task) {
		synchronized (taskQueue) {
			taskQueue.add(task);
		}
	}

	public int getNumTasks() {
		return taskLog.ratesFinished.size();
	}

	public int getTaskLogKey(int index) {
		return taskLog.codes.get(index);
	}

	public double getTaskLogValue(int index) {
		return taskLog.ratesFinished.get(index);
	}

	public double getCpuUsedRate() {
		return taskLog.cpuRate;
	}

	public double getRamUsedRate() {
		return taskLog.ramUsed;
	}

	public void freeMemory(double memorySize) {
		ram.freeMemory(memorySize);
		memoryUsedTotal -= memorySize;
	}

}

class TaskLog {
	public int numTasksMax;
	public List<Double> ratesFinished;
	public List<Integer> codes;
	public double cpuRate;
	public double ramUsed;
	public int numTasksFinished;

	public TaskLog(int maxNum) {
		ratesFinished = new ArrayList<>();
		codes = new ArrayList<>();
		numTasksFinished = 0;
	}

	public synchronized void update(Task task) {
		if (task.isFinished()) {
			for (int i = 0; i < ratesFinished.size(); i++) {
				if (codes.get(i) == task.getCode()) {
					ratesFinished.remove(i);
					codes.remove(i);
					numTasksFinished++;
					//System.out.println("remove " + i);
					return;
				}
			}
			return;
		}
		else {
			for (int i = 0; i < ratesFinished.size(); i++) {
				if (codes.get(i) == task.getCode()) {
					ratesFinished.set(i, task.getRateFinished());
					return;

				}
			}
			codes.add(task.getCode());
			ratesFinished.add(task.getRateFinished());
			return;
		}
		
	}

}
