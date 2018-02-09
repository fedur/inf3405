package inf3405_tp2_server;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

import javax.imageio.ImageIO;

import javafx.util.Pair;

public class Server {
	private static String FILENAME = "bd.txt";
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		Socket socket = null;
		OutputStream outputStream = null;
		InputStream inputStream = null;
		String port = "";
		String usager = "";
		String motDePasse = "";
		Scanner prompt = new Scanner(System.in);
		ServerSocket serverSocket = null;
		
		do{
			System.out.print("Entrez le port du socket: ");
			port = prompt.nextLine();
		}while(!portIsValid(port));
		serverSocket = new ServerSocket(Integer.parseInt(port), 1000, InetAddress.getByName("127.0.0.1"));
		

		STATUS status;
		try {
			socket = serverSocket.accept();
			do{
				System.out.println("Waiting for client message...");
				outputStream = socket.getOutputStream();
				inputStream = socket.getInputStream();
				@SuppressWarnings("unchecked")
				byte[] response = new byte[1024];
				inputStream.read(response);
				String result = new String(response);
				result = result.replaceAll("\0", "").trim();
				System.out.println(result);
				String[] splitResponse = result.split(" ", 2);
				status = fetchDb(splitResponse[0], splitResponse[1]); 
				
				switch(status)
				{
					case VALID:
						outputStream.write(("1 Valid").getBytes());
						break;
					case NEW_USER:
						outputStream.write(("1 New user has been created.").getBytes());
						break;
					case ERROR:
						outputStream.write(("0 Error in DB").getBytes());
						break;
					case PASSWORD_NON_MATCHING:
						outputStream.write(("0 Password does not match").getBytes());
						break;
					default:
						break;
				}
				outputStream.flush();
				
			}while (status == STATUS.ERROR || status == STATUS.PASSWORD_NON_MATCHING);
			
			// READ IMAGE BRUH
			System.out.println("Reading: " + System.currentTimeMillis());

	        byte[] sizeAr = new byte[4];
	        inputStream.read(sizeAr);
	        int size = ByteBuffer.wrap(sizeAr).asIntBuffer().get();

	        byte[] imageAr = new byte[size];
	        inputStream.read(imageAr);

	        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageAr));

	        System.out.println("Received " + image.getHeight() + "x" + image.getWidth() + ": " + System.currentTimeMillis());
	        ImageIO.write(image, "jpg", new File("C:\\TEMP\\inf3405\\inf3405\\client_inf3405\\lassonde22.jpg"));
			
		} finally {
			serverSocket.close();
			socket.close();
		}
	}
	
	private static STATUS fetchDb(String nomUsager, String motDePasse) throws IOException
	{
		List<String> listOfLines = new ArrayList<String>();
		String line = "";
		FileReader fileReader = new FileReader(FILENAME);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		
		try 
		{
			while ((line = bufferedReader.readLine()) != null) 
			{
				if (line.trim().isEmpty())
				{
					continue;
				}
				System.out.println(line);
				String[] splitLine = line.split(";;;");
				
				if (splitLine.length != 2){
					System.out.println("Error in Database...");
					return STATUS.ERROR;
				}
				
				if (splitLine[0].equals(nomUsager))
				{
					if (splitLine[1].equals(motDePasse))
					{
						return STATUS.VALID;
					}
					else
					{
						System.out.println("Password does not match.");
						return STATUS.PASSWORD_NON_MATCHING;
					}
				}
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		} 
		
		finally {
			fileReader.close();
			bufferedReader.close();
		}
		
		// If function did not returnl, append the new user to the db
		BufferedWriter writer = new BufferedWriter(new FileWriter(FILENAME,true));
		try {
			writer.newLine();
			writer.write("\n");
			writer.write(nomUsager);
			writer.write(";;;");
			writer.write(motDePasse + '\n');
		} finally {
			writer.close();
		}
		return STATUS.NEW_USER;
	}

	private static boolean adressIsValid(String adress)
	{
		String[] octets = adress.split("\\.");
		if (octets.length != 4)
		{
			return false;
		}
		
		for (int i =0; i<octets.length; i++)
		{
			try {
				Integer.parseInt(octets[i]);
			}
			catch(Exception e)
			{
				return false;
			}
		}
		return true;
	}
	
	private static boolean portIsValid(String port)
	{
		int portAdress = 0;
		try{
			portAdress = Integer.parseInt(port);
		}
		catch(Exception e){
			return false;
		}
		if (portAdress < 5000 || portAdress > 5050) {
			return false;
		}
		return true;
	}
	
	enum STATUS{
		VALID,
		PASSWORD_NON_MATCHING,
		NEW_USER,
		ERROR
	}
	
}




