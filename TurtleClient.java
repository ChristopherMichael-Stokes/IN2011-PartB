import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Read a turtle-graphics encoding of ASCII-art from the file named on
 * the command line and send it to a server to be decoded.
 * Print the decoded result to the console.
 */
public class TurtleClient {

	static final String SERVER_HOST = "www.staff.city.ac.uk";
	static final int SERVER_PORT = 80;
	static final String URI = "/s.hunt/Turtle.php";

	public static void main(String[] args) throws IOException {
		if (0 == args.length) {
			System.out.println("no file given");
			System.exit(-1);
		}

		// read the file contents into a byte array
		String fileName = args[0];
		byte[] file = null;
		try {
			file = Files.readAllBytes(Paths.get(fileName));
		} catch (IOException ex) {
			System.out.println("file not found");
			System.exit(-1);
		}

		// open a network connection
		try (Socket socket = new Socket(InetAddress.getByName(SERVER_HOST), SERVER_PORT)) {
			socket.setSoTimeout(0);
			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();

			Writer writer = new java.io.OutputStreamWriter(os, "US-ASCII");

			// send the POST request (can use the Writer here)
			writer.write("POST " + URI + " HTTP/1.1\n");

			// send the necessary HTTP headers (can use the Writer here)
			writer.write("Host: " + SERVER_HOST + "\n");
			writer.write("Content-Length: " + file.length + "\n");

			// send a blank line and flush output to server
			writer.write("\n");
			writer.flush();
			os.flush();

			// send message body (the bytes that we read from the file previously)
			// Use the raw OutputStream here, NOT the Writer.
			// Why? Because we need to send exactly the number of bytes
			// that we advertised in the Content-Length header, without
			// any character encoding messing things up
			os.write(file);

			// flush output to server
			os.flush();

			// Use readHeaderLine(is) to read each response header line in turn.
			//
			// While reading the headers, determine the size in bytes
			// of the response message body (the value of the Content-Length header)
			//
			// The final header line will be empty (everything after that
			// is part of the message body).
			String headerLine;
			int bytes = 0;
			while ((headerLine = readHeaderLine(is)) != null && !headerLine.isEmpty()) {
				if (headerLine.matches("Content-Length(.*)")) {
					bytes = Integer.parseInt(headerLine.split(": ")[1]);
				}
			}

			// read the response message body into a byte array
			int idx = 0;
			byte b;
			byte[] reply = new byte[bytes];
			while (idx < bytes && (b = (byte) is.read()) != -1) {
				reply[idx++] = b;
			}

			// decode the message bytes and output as text
			// For this exercise you should assume that the
			// message body is unicode text encoded with UTF-8.
			String response = new String(reply, "UTF-8");
			System.out.println(response);
		}
	}

	/**
	 * Read an HTTP header line.
	 */
	private static String readHeaderLine(InputStream is) throws IOException {
		String line;
		int ch = is.read();
		if (ch == -1) {
			line = null;
		} else {
			line = "";
		}
		while (ch != -1 && ch != '\r') {
			line += (char) ch;
			ch = is.read();
		}
		if (ch =='\r')
			is.read(); // consume line-feed
		return line;
	}
}
