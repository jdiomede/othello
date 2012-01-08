import java.rmi.Naming;
import java.io.IOException;
import java.rmi.RMISecurityManager;

public class ReversiServer{
    ReversiServer(){
		ReversiServerInterface reversiGame = null;
		try{
		    reversiGame = new ReversiGame();
		    Naming.rebind("ReversiGame", reversiGame);
		    System.out.println("Server ready.");
		}
		catch(IOException ioe){
			System.out.println("Exception starting Server:");
		    System.out.println(ioe);
		}
    }

    public static void main(String[] args){

		//start rmiregistry programmatically
    	try {
			java.rmi.registry.LocateRegistry.createRegistry(1099);
			System.out.println("RMI registry ready.");
		}
		catch (Exception e) {
			System.out.println("Exception starting RMI registry:");
			e.printStackTrace();
		}

		try
		{
			Thread.sleep(1000);
			for(int x = 0; x < 3; x++){
				System.out.println(".");
				Thread.sleep(1000);
			}
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}

		new ReversiServer();
    }

}