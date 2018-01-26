package client_tp2;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

public class Client {
	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {

		Socket clientSocket = null;
		try {
			Scanner prompt = new Scanner(System.in);
			String adresse = "";
			String port = "";
			String usager = "";
			String motDePasse = "";
			
			do{
				System.out.print("Entrez l'adresse ip du serveur: ");
				adresse = prompt.nextLine();
			}while(!adressIsValid(adresse));
			
			do{
				System.out.print("Entrez le port du socket: ");
				port = prompt.nextLine();
			}while(!portIsValid(port));
			
			System.out.print("Entrez votre nom d'usager: ");
			usager = prompt.nextLine();
			
			System.out.print("Entrez votre mot de passe: ");
			motDePasse = prompt.nextLine();
			
			clientSocket = new Socket(adresse,Integer.parseInt(port));
			ObjectOutputStream objectOutput = new ObjectOutputStream(clientSocket.getOutputStream());
			
			objectOutput.writeObject(Arrays.asList(usager,motDePasse));
			objectOutput.flush();

			ObjectInputStream obj = new ObjectInputStream(clientSocket.getInputStream());
			@SuppressWarnings("unchecked")
			List<String> receivedStrings = (List<String>) obj.readObject();
		 
			for (String s : receivedStrings)
			{
				System.out.println(s);
			}
			
		} finally {
			// Fermeture du socket.
			clientSocket.close(); 
		}
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
	// Fonction permettant d'écrire dans un fichier les données contenues dans la
	// stack reçu du serveur.
	private static void writeToFile(Stack<String> myStack, String nomFichier) throws IOException {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(nomFichier));
			while (!myStack.isEmpty()) {
				out.write(myStack.pop() + "\n");
			}
		} finally {
			out.close();
		}
	}
}
