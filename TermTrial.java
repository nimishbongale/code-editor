import java.io.*;
import java.util.*;
public class TermTrial{
    public static void main(String args[]){
ProcessBuilder processBuilder = new ProcessBuilder();

	// -- Linux --

	// Run a shell command
	// processBuilder.command("bash", "-c", "ls /home/mkyong/");

	// Run a shell script
	//processBuilder.command("path/to/hello.sh");

	// -- Windows --

	//Run a command
	processBuilder.command("cmd.exe", "/c", "echo hello");

	// Run a bat file
	//processBuilder.command("C:\\Users\\mkyong\\hello.bat");

	try {

		Process process = processBuilder.start();

		StringBuilder output = new StringBuilder();

		BufferedReader reader = new BufferedReader(
				new InputStreamReader(process.getInputStream()));

		String line;
		while ((line = reader.readLine()) != null) {
			output.append(line + "\n");
		}

		int exitVal = process.waitFor();
		if (exitVal == 0) {
			System.out.println("Success!");
			System.out.println(output);
			System.exit(0);
		} else {
			//abnormal...
		}

	} catch (IOException e) {
		e.printStackTrace();
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
    }}