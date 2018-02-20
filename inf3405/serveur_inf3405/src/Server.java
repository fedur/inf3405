

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

import javax.imageio.ImageIO;

import javafx.util.Pair;

public class Server {
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		Socket socket = null;
		String IpAddress = "";
		String port = "";
		Scanner prompt = new Scanner(System.in);
		ServerSocket serverSocket = null;
		
		do{
			System.out.print("Entrez l'adresse ip de la machine pour l'utilisation du serveur: ");
			IpAddress = prompt.nextLine();
		}while(!adressIsValid(IpAddress));
		do{
			System.out.print("Entrez le port du socket: ");
			port = prompt.nextLine();
		}while(!portIsValid(port));
		
		serverSocket = new ServerSocket(Integer.parseInt(port), 1000, InetAddress.getByName(IpAddress));
		while(true){
			try {
				socket = serverSocket.accept();
			} 
			catch(Exception e){}

			new ServerThread(socket, IpAddress,port).start();
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
	
}




