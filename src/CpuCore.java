import java.time.Duration;
import java.time.Instant;

public class CpuCore extends Thread {

	private static final long timeStep = 10; // le pas de temps pour simuler, en milli seconde
	private static final int schedulingQuantum = 3; // nombre de pas dans la planification de RoundRobin
	private final double capacity; // capacité de la CPU, en glops
	private Computer host; // L'ordinateur à qui la CPU est associée
	private Ram ram; // La mémoire vive à qui la CPU est associée
	private boolean isWorking; // Indiquer si la CPU est en train de travailler
	private boolean shutDown = false; 
	private Recorder recorder; 
	private Task currentTask; // La tâche que la CPU est en train de traiter
	private double cpuRateCoefficient; // Coefficent pour calculer le pourcentage de l'utilisation de la CPU

	public CpuCore(double capacity) {
		this.capacity = capacity;
		this.isWorking = false;
		this.recorder = new Recorder();
	}

	public CpuCore(double capacity, Computer computer) {
		this(capacity);
		setComputer(computer);
	}

	/**
	 * Lier l'ordinateur et la mémoire à la CPU, et puis initialiser le coefficient
	 * pour calculer le ratio de la CPU
	 * 
	 * @param computer
	 */
	public void setComputer(Computer computer) {
		host = computer;
		ram = host.getRam();
		cpuRateCoefficient = Math.exp(ram.getMemoryMax() / 1000);
	}

	public double getCapacity() {
		return capacity;
	}

	public boolean isFree() {
		return !isWorking;
	}

	public void shutDown() {
		shutDown = true;
	}

	public void restart() {
		shutDown = true;
		start();
	}

	/**
	 * Calculer le ratio d'utilisation de CPU selon l'occupation de RAM. Si tous les
	 * contenus sont stockés dans la mémoire, le ratio de la CPU vaut 1. Si certains
	 * contenus sont stockés dans la disque, le ratio de la CPU vaut coef*exp(-x)
	 * dont x est la mémoire occupée actuelle. Le ratio vaut 1 en x = "mémoire
	 * maximale de RAM" pour garantir la continuité.
	 */
	public double usingRate() {
		if (ram.getMemoryUsed() <= ram.getMemoryMax()) {
			return 1;
		} else {
			return cpuRateCoefficient * Math.exp(-ram.getMemoryUsed() / 1000);
		}
	}
	/**
	 * Un algorithme sur la planification des tâches. La CPU traite chaque tâche 
	 * jusqu’à ce qu’elle soit finie, et puis traite la suite.
	 */
	public void algorithmNaive() {
		if (host.hasTask()) {
			currentTask = host.dropTask();
			isWorking = true;
			double rateIncrement;
			double usingRate;
			while (!currentTask.isFinished()) {
				try {
					sleep(timeStep);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				usingRate = usingRate();
				rateIncrement = currentTask.rateIncrement(capacity * timeStep / 1000 * usingRate);
				ram.addLoad(rateIncrement * currentTask.getMemorySize());
				currentTask.updateProgress(rateIncrement, timeStep);
				host.updateTaskLog(currentTask, usingRate);
			}

			System.out.println("Task finished : No. " + currentTask.getCode());
			currentTask.updateTimeOfEnd();
			synchronized (host.getResultQueue()) {
				host.getResultQueue().add(currentTask);
			}

			isWorking = false;
			ram.freeMemory(currentTask.getMemorySize());
			host.updateTaskLog(currentTask, 0);
			recorder.addData(Duration.between(Controller.startTime, Instant.now()), 0, ram.getMemoryUsed());
		}
	}

	/**
	 * Un algorithme sur la planification des tâches. La CPU sort une tâche de la file d’attente, 
	 * la traite pendant une durée limitée par schedulingQuantum, et puis la renvoie à la queue de 
	 * la file d’attente.
	 */
	public void algorithmRoundRobin() {

		double rateIncrement;
		double usingRate;
		synchronized (host.getTaskQueue()) {
			if (!host.getTaskQueue().isEmpty()) {
				currentTask = host.getTaskQueue().poll();
				isWorking = true;
			} else
				isWorking = false;
		}

		if (isWorking) {
			for (int i = 0; i < schedulingQuantum; i++) {
				if (!currentTask.isFinished()) {
					try {
						sleep(timeStep);

					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					usingRate = usingRate();
					rateIncrement = currentTask.rateIncrement(capacity * timeStep / 1000 * usingRate);
					ram.addLoad(rateIncrement * currentTask.getMemorySize());
					currentTask.updateProgress(rateIncrement, timeStep);
					host.updateTaskLog(currentTask, usingRate);
				}
			}
			if (currentTask.isFinished()) {
				System.out.println("Task finished : No. " + currentTask.getCode());
				host.freeMemory(currentTask.getMemorySize());
				currentTask.updateTimeOfEnd();
				recorder.addData(Duration.between(Controller.startTime, Instant.now()), 0, ram.getMemoryUsed());
				synchronized (host.getResultQueue()) {
					host.getResultQueue().add(currentTask);
				}
				host.updateTaskLog(currentTask, 0);
			} else {
				host.getTaskQueue().add(currentTask);
			}
		}
	}

	public void run() {
		while (!shutDown) {
			// algorithmNaive();
			algorithmRoundRobin();
		}
	}

	public void printRecorder() {
		recorder.print();
		System.out.println();
	}

}
