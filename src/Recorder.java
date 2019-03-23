import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Recorder {
	private List<Duration> times;
	private List<Integer> cpuWorks; // 0 pour désactiver et 1 pour activer
	private List<Double> ramRates; // pourcentage de la RAM
	private List<Double> cpuRates; // pourcentage de la CPU
	
	public Recorder() {
		times = new ArrayList<>();
		cpuWorks = new ArrayList<>();
		ramRates = new ArrayList<>();
		cpuRates = new ArrayList<>();
	}
	
	public void addData(Duration duration, Integer cpu, double ram) {
		times.add(duration);
		cpuWorks.add(cpu);
		ramRates.add(ram);
	}
	
	public void reset() {
		times.clear();
		cpuWorks.clear();
		ramRates.clear();
		cpuRates.clear();
	}
	
	public void print() {
		for(int i=0;i<times.size();i++) {
			System.out.println(times.get(i) + " : CPU " + cpuWorks.get(i) + " ; RAM " + ramRates.get(i)); 
		}
	}
}
