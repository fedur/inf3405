package inf3405_tp2_server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

import javafx.util.Pair;

public class Server {
	private static String FILENAME = "bd.txt";
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		Socket socket = null;
		ObjectInputStream in = null;
		ObjectOutputStream out = null;
		String port = "";
		String usager = "";
		String motDePasse = "";
		Scanner prompt = new Scanner(System.in);
		ServerSocket serverSocket = null;
		
		do{
			System.out.print("Entrez le port du socket: ");
			port = prompt.nextLine();
		}while(!portIsValid(port));
		serverSocket = new ServerSocket(Integer.parseInt(port));
		

		STATUS status;
		try {
			do{
				socket = serverSocket.accept();
				out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
				in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
				
				@SuppressWarnings("unchecked")
				List<String> strings = (List<String>) in.readObject();
				status = fetchDb(strings.get(0), strings.get(1));
				Pair<Boolean, String> result = new Pair<>();
				switch(status)
				{
					case VALID:
						out.writeObject(Arrays.asList("Valid"));
						break;
					case NEW_USER:
						out.writeObject(Arrays.asList("New user has been created."));
						break;
					case ERROR:
						out.writeObject(Arrays.asList("Error in DB"));
						break;
					case PASSWORD_NON_MATCHING:
						out.writeObject(Arrays.asList("Password does not match"));
						break;
					default:
						break;
				}
				out.flush();
				
			}while (status == STATUS.ERROR || status == STATUS.PASSWORD_NON_MATCHING);
			
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




