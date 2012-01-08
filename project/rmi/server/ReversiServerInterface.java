import java.rmi.Remote;
import java.rmi.RemoteException;

interface ReversiServerInterface extends Remote{

	public int registerClient(ReversiClientInterface client) throws RemoteException;
	public int move(int x, int y, int color) throws RemoteException;
	public int[][] getBlack() throws RemoteException;
	public int[][] getWhite() throws RemoteException;
	public String printWinner() throws RemoteException;
}