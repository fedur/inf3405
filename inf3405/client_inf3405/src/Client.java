import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
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

import javax.imageio.IIOException;
import javax.imageio.ImageIO;

public class Client {
	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {

		Socket clientSocket = null;
		try {
			Scanner prompt = new Scanner(System.in);
			InputStream inputStream = null;
			OutputStream outputStream = null;
			String adresse = "";
			String port = "";
			String usager = "";
			String motDePasse = "";
			String nomImage = "";
			String nouveauNomImage = "";
			
			do{
				System.out.print("Entrez l'adresse ip du serveur: ");
				adresse = prompt.nextLine();
			}while(!adressIsValid(adresse));
			
			do{
				System.out.print("Entrez le port du socket: ");
				port = prompt.nextLine();
			}while(!portIsValid(port));

			clientSocket = new Socket(adresse,Integer.parseInt(port));
			outputStream = clientSocket.getOutputStream();
			
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
				
				inputStream = clientSocket.getInputStream();
				
				byte[] response = new byte[1024];
				inputStream.read(response);
				String result = new String(response);
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
			}while (!authenticationIsOk);
			
			boolean fileExists = true;
			do {
				System.out.print("Entrez le nom de l'image à modifier: ");
				nomImage = prompt.nextLine();
				
				System.out.print("Entrez le nouveau nom de l'image après le filtre Sobel appliqué: ");
				nouveauNomImage = prompt.nextLine();
				
				fileExists = envoyerImage(nomImage, outputStream);
				if (!fileExists)
				{
					System.out.println("File does not exist. Write a valid file name.");
				}
			}while(!fileExists);
			
			
			BufferedImage image = recevoirImage(inputStream);
			
	        ImageIO.write(image, "jpg", new File(nouveauNomImage));
	        System.out.println("L'image résultat est au path suivant: " + System.getProperty("user.dir"));
			
		} finally {
			// Fermeture du socket.
			clientSocket.close(); 
		}
	}
	
	private static boolean envoyerImage(String nomImage, OutputStream outputStream)
	{
		try{
			BufferedImage image = ImageIO.read(new File(nomImage));
			outputStream.write(nomImage.getBytes());
			outputStream.flush();

			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ImageIO.write(image, "jpg", byteArrayOutputStream);

			byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
			outputStream.write(size);
			outputStream.write(byteArrayOutputStream.toByteArray());
			outputStream.flush();
			System.out.println("L'image a été envoyé au serveur");
			return true;
		}
		catch(IOException error){
			return false;
		}
	}
	
	private static BufferedImage recevoirImage(InputStream inputStream)
	{
		BufferedImage image = null;
		try{
			byte[] sizeAr = new byte[4];
			inputStream.read(sizeAr);
			int size = ByteBuffer.wrap(sizeAr).asIntBuffer().get();

			byte[] imageAr = new byte[size];
			inputStream.read(imageAr);

			image = ImageIO.read(new ByteArrayInputStream(imageAr));
			System.out.println("L'image a été reçu après sa modification");
		}
		catch(IOException error){}
		
		return image;
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
