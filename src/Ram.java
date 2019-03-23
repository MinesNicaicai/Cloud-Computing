
public class Ram {
	private double memoryMax; // La m¨¦moire vive maximale, en Mo
	private double memoryUsed; // La m¨¦moire occup¨¦e actuelle, en Mo
	public Ram(double memoryMax) {
		this.memoryMax = memoryMax;
		this.memoryUsed = 0;
	}
	
	public double getMemoryMax() {
		return memoryMax;
	}
	
	public double getMemoryUsed() {
		return memoryUsed;
	}
	
	public double getMemoryFree() {
		return memoryMax - memoryUsed;
	}
	
	public void addLoad(double size) {
		memoryUsed += size;
	}
	
	public void freeMemory(double size) {
		memoryUsed = memoryUsed - size > 0 ? memoryUsed - size : 0;
	}
	
	public void freeMemory() {
		memoryUsed = 0;
	}
	
	
	
}
 