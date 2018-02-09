import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

import javax.imageio.ImageIO;

public class Client {
	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {

		Socket clientSocket = null;
		try {
			Scanner prompt = new Scanner(System.in);
			String adresse = "";
			String port = "";
			String usager = "";
			String motDePasse = "";
			String nomImage = "";
			
			do{
				System.out.print("Entrez l'adresse ip du serveur: ");
				adresse = prompt.nextLine();
			}while(!adressIsValid(adresse));
			
			do{
				System.out.print("Entrez le port du socket: ");
				port = prompt.nextLine();
			}while(!portIsValid(port));

			clientSocket = new Socket(adresse,Integer.parseInt(port));
			OutputStream outputStream = clientSocket.getOutputStream();
			
			boolean authenticationIsOk = true;
			do
			{
				System.out.print("Entrez votre nom d'usager: ");
				usager = prompt.nextLine();
				
				System.out.print("Entrez votre mot de passe: ");
				motDePasse = prompt.nextLine();
				String usagerMP = usager + " " + motDePasse;
				outputStream.write(usagerMP.getBytes());
				outputStream.flush();
				
				InputStream inputStream = clientSocket.getInputStream();
				
				byte[] response = new byte[1024];
				inputStream.read(response);
				String result = new String(response);
				System.out.println(result);
				String[] splitResult = result.split(" ", 2);
				if (splitResult[0].equals("0"))
				{
					authenticationIsOk = false;
				}
				else if (splitResult[0].equals("1"))
				{
					authenticationIsOk = true;
				}
				else
				{
					System.out.println("ERROR: REPEAT");
					continue;
				}
				System.out.println(splitResult[1]);
			}while (!authenticationIsOk);
			
			System.out.print("Entrez le nom de l'image à modifier: ");
			nomImage = prompt.nextLine();
			
			BufferedImage image = ImageIO.read(new File("lassonde.jpg"));
			System.out.println(image);

	        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	        ImageIO.write(image, "jpg", byteArrayOutputStream);

	        byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
	        outputStream.write(size);
	        outputStream.write(byteArrayOutputStream.toByteArray());
	        outputStream.flush();
			
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
