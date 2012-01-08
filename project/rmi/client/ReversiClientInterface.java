import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.Serializable;

interface ReversiClientInterface extends Remote{
    public void startGame(int color) throws RemoteException;
    public void pushMove(int color) throws RemoteException;

}