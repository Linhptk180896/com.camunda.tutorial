package academy.service;

public class ServiceTaskService {

	public String serviceTask(String name, String age) {
		System.out.println("Name: " + name);
		System.out.println("Age: " + age);
		String confirmation = String.valueOf(System.currentTimeMillis());
		return confirmation;

	}
}