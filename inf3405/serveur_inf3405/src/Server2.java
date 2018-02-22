import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		//initialisation de toutes les variables
		Socket socket = null;
		String IpAddress = "";
		String port = "";
		Scanner prompt = new Scanner(System.in);
		ServerSocket serverSocket = null;
		
		// boucle pour que l'utilisateur entre un adresse IP valide
		do{
			System.out.print("Entrez l'adresse ip de la machine pour l'utilisation du serveur: ");
			IpAddress = prompt.nextLine();
		}while(!adressIsValid(IpAddress));
		
		// boucle pour que l'utilisateur entre un port valide
		do{
			System.out.print("Entrez le port du socket: ");
			port = prompt.nextLine();
		}while(!portIsValid(port));
		
		// création du socket, l'acceptation de nouveaux clients et un thread de créer pour chaque nouveau client
		serverSocket = new ServerSocket(Integer.parseInt(port), 1000, InetAddress.getByName(IpAddress));
		while(true){
			try {
				socket = serverSocket.accept();
			} 
			catch(Exception e){}

			new ServerThread(socket, IpAddress,port).start();
		}
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
	
	//méthode permettant de vérifier si l'entrée du port est validee
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




