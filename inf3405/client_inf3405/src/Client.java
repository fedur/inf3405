import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Scanner;

import javax.imageio.ImageIO;

public class Client {
	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {

		Socket clientSocket = null;
		try {
			//initialisation de toutes les variables
			Scanner prompt = new Scanner(System.in);
			InputStream inputStream = null;
			OutputStream outputStream = null;
			String adresse = "";
			String port = "";
			String usager = "";
			String motDePasse = "";
			String nomImage = "";
			String nouveauNomImage = "";
			
			// boucle pour que l'utilisateur entre un adresse IP valide
			do{
				System.out.print("Entrez l'adresse ip du serveur: ");
				adresse = prompt.nextLine();
			}while(!adressIsValid(adresse));
			
			// boucle pour que l'utilisateur entre un port valide
			do{
				System.out.print("Entrez le port du socket: ");
				port = prompt.nextLine();
			}while(!portIsValid(port));

			// création du socket et de son outputStream
			clientSocket = new Socket(adresse,Integer.parseInt(port));
			outputStream = clientSocket.getOutputStream();
			
			// boucle pour s'assurer que l'authentification de l'usager est valide
			boolean authenticationIsOk = true;
			do
			{
				System.out.print("Entrez votre nom d'usager: ");
				usager = prompt.nextLine();
				
				System.out.print("Entrez votre mot de passe: ");
				motDePasse = prompt.nextLine();
				
				// envoyer le nom d'usager et le mot de passe au serveur pour la validation
				String usagerMP = usager + " " + motDePasse;
				outputStream.write(usagerMP.getBytes());
				outputStream.flush();
				
				inputStream = clientSocket.getInputStream();
				
				// reception de la réponse du serveur pour savoir si l'usager est bel et bien dans la base de données du serveur
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
				if (!authenticationIsOk)
				{
					System.out.println("Authentication is not valid. Enter your username and password again.");
				}
			}while (!authenticationIsOk);
			
			// boucle pour savoir si l'usager entre le nom d'une image valide
			boolean fileExists = true;
			do {
				System.out.print("Entrez le nom de l'image à modifier: ");
				nomImage = prompt.nextLine();
				
				System.out.print("Entrez le nouveau nom de l'image après le filtre Sobel appliqué: ");
				nouveauNomImage = prompt.nextLine();
				
				// envoyer l'image avec le nom au serveur pour la modification Sobel
				fileExists = envoyerImage(nomImage, outputStream);
				if (!fileExists)
				{
					System.out.println("File does not exist. Write a valid file name.");
				}
			}while(!fileExists);
			
			// recevoir l'image après modification par le serveur
			BufferedImage image = recevoirImage(inputStream);
			
			// enregistrer l'image modifié dans le répertoire source et afficher le path de l'enregistrement
	        ImageIO.write(image, "jpg", new File(nouveauNomImage));
	        System.out.println("L'image résultat est au path suivant: " + System.getProperty("user.dir"));
			
		} finally {
			// Fermeture du socket.
			clientSocket.close(); 
		}
	}
	
	// méthode permettant l'envoi de l'image au serveur
	private static boolean envoyerImage(String nomImage, OutputStream outputStream)
	{
		try{
			// Lire l'image source dans le répertoire source et voir si elle existe et si oui envoyer le nom de l'image au serveur
			BufferedImage image = ImageIO.read(new File(nomImage));
			outputStream.write(nomImage.getBytes());
			outputStream.flush();

			// mettre l'image dans un byteArrayOutputStream pour l'envoi de l'image au serveur
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ImageIO.write(image, "jpg", byteArrayOutputStream);

			// envoyer le size de l'image en premier pour envoyer l'image par la suite
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
	
	// méthode permettant la réception de l'image du serveur
	private static BufferedImage recevoirImage(InputStream inputStream)
	{
		BufferedImage image = null;
		try{
			DataInputStream in = new DataInputStream(inputStream);
			//création d'un byte array pour accueillir l'image avec le size reçu en premier
			byte[] sizeAr = new byte[4];
			inputStream.read(sizeAr);
			int size = ByteBuffer.wrap(sizeAr).asIntBuffer().get();

			// Lire l'image du socket
			byte[] imageAr = new byte[size];
			in.readFully(imageAr);

			image = ImageIO.read(new ByteArrayInputStream(imageAr));
			System.out.println("L'image a été reçu après sa modification");
		}
		catch(IOException error){}
		
		return image;
	}
	
	//méthode permettant de vérifier si l'entrée d'une addresse IP est valide
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
	
	//méthode permettant de vérifier si l'entrée du port est valide
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
}
