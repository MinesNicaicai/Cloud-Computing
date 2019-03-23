import java.time.Duration;
import java.time.Instant;

public class Task {

	
	private static int codeGenerator = 1;
	public static enum TaskType{
		A,B,C,D,E;
	} // Définir 5 différents types de tâches 
	private final int code; // Identifier la tâche
	private final double calculationSize; // La quantité de calcul, en gflo (Giga Floating Operations)
	private final double memorySize; // La mémoire occupée maximale pour traiter cette tâche, en Mo
	private final TaskType type;
	private double rateFinished; // entre 0 et 1
	private double memoryUsed; //en Mo
	private double timeOfCreation; // Le moment où la tâche est créée
	private double timeOfExecution; // 'Le temps d'exécution dans CPU
	private double timeOfEnd; // le moment où la tâche est finie
	private int nodeIp; // l'adresse IP du noeud qui s'occupe de cette tâche
	
	public Task(double CalculationSize, double MemorySize, TaskType TaskType) {
		code = codeGenerator;
		codeGenerator++;
		calculationSize = CalculationSize;
		memorySize = MemorySize;
		type = TaskType;
		
		rateFinished = 0;
		timeOfCreation = (double)Duration.between(Controller.startTime,Instant.now()).toNanos()/1000000000;
		timeOfExecution = 0;
		
	}
	
	public Task(double CalculationSize, double MemorySize, TaskType taskType, double arrivalTime) {
		this(CalculationSize, MemorySize, taskType);
		timeOfCreation = arrivalTime;
		//arrivalTime + Controller.startTime;
		
	}
	
	public double getTimeOfCreation() {
		return timeOfCreation;
	}
	
	
	public double getCalculationSize() {
		return calculationSize;
	}
	public double getMemorySize() {
		return memorySize;
	}
	
	public double getRateFinished() {
		return rateFinished;
	}
	
	public int getCode() {
		return code;
	}
	
	public TaskType getType() {
		return type;
	}
	
	public void setNodeIp(int ip) {
		nodeIp = ip;
	}
	
	public double rateIncrement(double calculationFinished) {
		return calculationFinished/calculationSize;
	}
	
	/**
	 * Mettre à jour l'état de la tâche à chaque fois quand la CPU la traite. 
	 * @param rateIncrement
	 * @param millisToAdd
	 */
	public void updateProgress(double rateIncrement, long millisToAdd) {
		rateFinished += rateIncrement;
		if (rateFinished>1) rateFinished = 1;
		memoryUsed = memorySize * rateFinished;
		timeOfExecution += (double)millisToAdd/1000;
	}
	
	
	public double getMemoryUsed() {
		return memoryUsed;
	}
	
	public boolean isFinished() {
		return rateFinished >= 1;
	}
	
	public void updateTimeOfEnd() {
		timeOfEnd = Duration.between(Controller.startTime, Instant.now()).toNanos()/1000000000.f;
	}
	
	public void updateCreationTime() {
		timeOfCreation = Duration.between(Controller.startTime, Instant.now()).toNanos()/1000000000.f;
	}
	
	public double getWaitingTime() {
		//System.out.println("From " + timeOfCreation + " to " +  timeOfEnd);
		if (timeOfEnd!=0) {
			double waitingTime = timeOfEnd - timeOfCreation - timeOfExecution;
			//System.out.println("Seconds: " + waitingTime.getSeconds() + "nanosec: " + waitingTime.toNanos() );
			return waitingTime;		
		}
		else {
			return 0;
		}
	}
	
	public double getExecutionTime() {
		return timeOfExecution;
	}
	
	public double getTotalTime() {
		return timeOfEnd - timeOfCreation;
	}
	
}
