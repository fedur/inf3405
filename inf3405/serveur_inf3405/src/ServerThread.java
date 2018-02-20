import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;


public class ServerThread extends Thread {
	
		private static String FILENAME = "bd.txt";
		private String ipAdress = "";
		private String port = "";
	    protected Socket socket;
		private static final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd@HH:mm:ss");

	    public ServerThread(Socket clientSocket, String ipAdress, String port) {
	    	this.port = port;
	    	this.ipAdress = ipAdress;
	        this.socket = clientSocket;
	    }

	    public void run() {
	    	STATUS status;
	    	OutputStream outputStream = null;
			InputStream inputStream = null;
			String usager = "";
	    	try {
	    		do{
					System.out.println("Waiting for client message...");
					
					outputStream = socket.getOutputStream();
					inputStream = socket.getInputStream();
					byte[] response = new byte[1024];
					inputStream.read(response);
					String result = new String(response);
					result = result.replaceAll("\0", "").trim();
					
					String[] splitResponse = result.split(" ", 2);
					status = fetchDb(splitResponse[0], splitResponse[1]); 
					usager = splitResponse[0];
					
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
				
				byte[] bnomImage = new byte[1024];
				inputStream.read(bnomImage);
				String nomImage = new String(bnomImage);
				nomImage = nomImage.replaceAll("\0", "").trim();
				
				//recevoir l'image, la traitée et la réenvoyer
		        BufferedImage image = recevoirImage(inputStream);
				Date Date = new Date();
				String date = sdf.format(Date);
		        
		        String messageReception = "[" + usager + " - " + ipAdress + ":" + port + " - " + date + "] : " + nomImage;
		        System.out.println(messageReception);
		        
		        image  = Sobel.process(image);
		        envoyerImage(image, outputStream);
				socket.close();
	    	}
	    	catch(Exception e){}
	    	
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
		private static void envoyerImage(BufferedImage image, OutputStream outputStream)
		{
			try{
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				ImageIO.write(image, "jpg", byteArrayOutputStream);

				byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
				outputStream.write(size);
				outputStream.write(byteArrayOutputStream.toByteArray());
				outputStream.flush();
			}
			catch(IOException error){}
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
			}
			catch(IOException error){}
			
			return image;
		}

		
		enum STATUS{
			VALID,
			PASSWORD_NON_MATCHING,
			NEW_USER,
			ERROR
		}
	    
	    
	}