package rsb.io.net.io;

import java.util.Scanner;

abstract class ScannerUtils {

	public static String next(Scanner scanner) {
		try {
			return scanner.nextLine();
		} //
		catch (Exception e) {
			return null;
		}
	}

}
