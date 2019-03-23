import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Format;
import java.util.Random;

public class RequestGenerator extends Thread {

	public static enum RequestType {
		Poisson, Instantaneous;
	}

	private Controller cloud;
	private boolean shutDown = false;
	public static double calculationSizeMax = 20;
	public static double memorySizeMax = 300;
	private RequestType requestType;
	private double parameter;

	private Random randomGenerator;

	public RequestGenerator(Controller cloud, RequestType requestType, double parameter) {
		this.requestType = requestType;
		this.parameter = parameter;
		this.cloud = cloud;
		randomGenerator = new Random(0);
	}

	public RequestGenerator(Controller cloud, RequestType requestType) {
		this(cloud, requestType, 20);
	}

	public RequestGenerator(Controller cloud) {
		this(cloud, RequestType.Instantaneous);
	}

	public void setParameters(RequestType requestType, double parameter) {
		if (requestType == null)
			shutDown = true;
		else {
			this.requestType = requestType;
			this.parameter = parameter;
		}
	}

	public void setRequestAvgSize(double calculationSize, double memorySize) {
		calculationSizeMax = 2 * calculationSize;
		memorySizeMax = 2 * memorySize;

	}

	public double probabilityPoisson(double lambda, int k) {
		double res = Math.exp(-lambda) * Math.pow(lambda, k);
		for (int i = 1; i <= k; i++)
			res /= i;
		return res;
	}

	public void processPoisson(double tasksDensity) {
		int timeStep = 500;
		Task newTask;
		File file = new File("Processus Poisson");
		FileWriter fw;
		try {
			fw = new FileWriter(file);
			while (!shutDown) {
				double randomValue = randomGenerator.nextDouble();
				double probaTotal = 0;
				int k = -1;
				while (randomValue >= probaTotal) {
					k++;
					probaTotal += probabilityPoisson(tasksDensity * timeStep / 1000.f, k);
				}
				for (int i = 0; i < k; i++) {
					newTask = new Task( randomGenerator.nextDouble() * calculationSizeMax,
							randomGenerator.nextDouble() * memorySizeMax,
							Task.TaskType.values()[randomGenerator.nextInt(5)]);
					cloud.getTasks().add(newTask);

					fw.write("\n" + String.format("%.2f", newTask.getCalculationSize()) + "    " 
							+ String.format("%.2f", newTask.getMemorySize()) + "    "
							+ newTask.getType() + "    " 
							+ String.format("%.2f", newTask.getTimeOfCreation()));

				}

				sleep(timeStep);

			}
			fw.close();
		} catch (IOException | InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	public void processInstantaneous(int numRequests) {
		for (int i = 0; i < numRequests; i++) {
			// System.out.println(cloud.getTasks());
			cloud.getTasks()
					.add(new Task(cloud.randomGenerator.nextDouble() * calculationSizeMax,
							cloud.randomGenerator.nextDouble() * memorySizeMax,
							Task.TaskType.values()[cloud.randomGenerator.nextInt(5)]));
		}
	}

	public void shutDown() {
		shutDown = true;
		System.out.println("Request Generator shuts down");
	}

	public void run() {
		if (shutDown) return;
		if (requestType == RequestType.Poisson)
			processPoisson(parameter);
		else
			processInstantaneous((int) parameter);
	}

}
