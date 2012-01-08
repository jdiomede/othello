import java.rmi.Naming;
import java.io.Serializable;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.io.IOException;

import java.rmi.NotBoundException;
import java.net.MalformedURLException;
import java.util.Scanner;
import java.util.StringTokenizer;

public class ReversiClient extends UnicastRemoteObject implements ReversiClientInterface {

	static final int WAIT = 0;
	static final int WHITE = 1;
	static final int BLACK = 2;
	static final int OBSERVER = 3;
	static final int BLACKAGAIN = 4;
	static final int WHITEAGAIN = 5;
	static final int GAMEOVER = 6;

	int myColor;
	int currentTurn;
	boolean isValid;
	boolean isValidLocal;

	String action = null;

    int newMove[][] = new int[8][8];
    int x, y;

    private ReversiServerInterface reversiGame = null;

     private void gameOver(String winner){
    	myColor = WAIT;
		currentTurn = WAIT;
		try{
    		System.out.println("");
		    printBoard();
		}
		catch(Exception e){
		    System.out.println(e);
		}
	    System.out.println("Client>> game over, "+winner+" has won!");
	    System.out.println("Client>> reconnect if you would like to play again");
    }

    public void pushMove(int color){
    	currentTurn = color;

    	try{
    		System.out.println("");
		    printBoard();
		}
		catch(Exception e){
		    System.out.println(e);
		}

    	if(color == myColor){

			if(currentTurn == OBSERVER){
				System.out.println("Client>> an opponent has moved");
				System.out.print("Client>> ");
			}
			else{
				System.out.println("Client>> opponent has moved");
				switch(myColor){
					case BLACK:
						System.out.println("Client>> it is your turn (BLACK)...");
					break;
					case WHITE:
						System.out.println("Client>> it is your turn (WHITE)...");
					break;
				}
				System.out.print("Client>> ");
			}
		}
		else if(color == GAMEOVER){
			try{
	    		gameOver(reversiGame.printWinner());
			}
			catch(RemoteException re){
			    System.out.println(re);
			}

			System.out.print("Client>> ");
		}
		else{

			if(currentTurn == OBSERVER){
				System.out.println("Client>> an opponent has moved");
				System.out.print("Client>> ");
			}
			else{
				System.out.println("Client>> opponent has moved");
				switch(myColor){
					case BLACK:
						System.out.println("Client>> you do not have any valid moves (BLACK), waiting for opponent...");
					break;
					case WHITE:
						System.out.println("Client>> you do not have any valid moves (WHITE), waiting for opponent...");
					break;
				}
				System.out.print("Client>> ");
			}
		}
    }

    public void startGame(int color){
    	//first move is always BLACK
    	currentTurn = color;

    	try{
    		System.out.println("");
		    printBoard();
		}
		catch(Exception e){
		    System.out.println(e);
		}

		//depending on client's color, display information for next move
    	if(currentTurn == myColor){
			System.out.println("Client>> an opponent has joined!");
			System.out.println("Client>> it is your move...");
			System.out.print("Client>> ");
		}
		else{
			System.out.println("Client>> waiting for opponent to move...");
		}
	}

	public void printBoard() throws Exception{
		int black[][] = reversiGame.getBlack();
		int white[][] = reversiGame.getWhite();

		System.out.println("\n");

    	System.out.println("  0 1 2 3 4 5 6 7 ");
    	for (int y = 0; y < 8; y++){
    		System.out.print(y + "|");
    		for (int x = 0; x < 8; x++){
    			if (white[x][y] != 0) {
    				System.out.print("0");
    			}
    			else if (black[x][y] != 0){
    				System.out.print("X");
    			}
    			else{
    				System.out.print(" ");
    			}
    			System.out.print("|");
    		}
    		System.out.print("\n");
    	}

    	System.out.println("\n");
    }

    private boolean checkMove(int x, int y){
    	if(x >= 0 && x < 8){
    		isValidLocal = true;
    	}
    	else{
    		isValidLocal = false;
    	}

    	if(y >= 0 && y < 8){
    		isValidLocal = true;
    	}
    	else{
    		isValidLocal = false;
    	}

    	return isValidLocal;
    }

    private void start() throws Exception{
		while (true){
		    System.out.print("Client>> ");
		    Scanner s = new Scanner(System.in);
		    String inputLine = s.nextLine();
		    StringTokenizer tokenizedString = new StringTokenizer(inputLine);
		    if(tokenizedString.hasMoreTokens()){
		    	action = tokenizedString.nextToken();
		    }
		    else{
		    	action = "null";
		    }

		    if (action.equals("connect")){
				if (myColor == WAIT){
					//retrieve reference to server and register this peer
					try{
						reversiGame = (ReversiServerInterface) Naming.lookup("ReversiGame");

						myColor = reversiGame.registerClient(this);

						switch(myColor){
							case BLACK:
								System.out.println("Client>> myColor is: BLACK");
							break;
							case WHITE:
								System.out.println("Client>> myColor is: WHITE");
							break;
							case OBSERVER:
								currentTurn = OBSERVER;
								printBoard();
								System.out.println("Client>> the game has started, you are an observer only");
							break;
						}
					}
					catch(Exception e){
						System.out.println(e);
					}
				}
				else if (myColor == OBSERVER){
					System.out.println("Client>> you can only observe");
				}
				else{
					System.out.println("Client>> you are already connected");
				}
		    }
		    else if (action.equals("move")){
		    	if(currentTurn == myColor && currentTurn != WAIT && currentTurn != OBSERVER){
		    		if(tokenizedString.hasMoreTokens()){
						x = Integer.parseInt(tokenizedString.nextToken());
		    		}
		    		else{
		    			//force out of bounds, no input
		    			x = 8;
		    		}

		    		if(tokenizedString.hasMoreTokens()){
						y = Integer.parseInt(tokenizedString.nextToken());
		    		}
		    		else{
		    			//force out of bounds, no input
		    			y = 8;
		    		}

					//check for valid move
					isValid = checkMove(x,y);

					//if valid, send move to server
					if(isValid){
						currentTurn = reversiGame.move(x,y, myColor);

						//if accepted, turns will swap
						if(currentTurn == BLACKAGAIN || currentTurn == WHITEAGAIN){
							//return AGAIN value will always be for the moving client
							currentTurn = myColor;
							printBoard();
							System.out.println("Client>> opponent does not have any valid moves");
							System.out.println("Client>> it is still your turn...");
						}
						else if(currentTurn == GAMEOVER){
							try{
					    		gameOver(reversiGame.printWinner());
							}
							catch(RemoteException re){
							    System.out.println(re);
							}
						}
						else if(currentTurn != myColor){
							printBoard();
							System.out.println("Client>> server accepted this update");
							System.out.println("Client>> waiting for opponent to move...");
						}
						else{
							System.out.println("Client>> server did not accept this move");
							System.out.println("Client>> it is still your turn...");
						}

					}
					else{
						System.out.println("Client>> invalid move");
						System.out.println("Client>> it is still your turn...");
					}
				}
				else if(currentTurn == WAIT){
					System.out.println("Client>> game has not started");
					System.out.println("Client>> waiting for opponent to join...");
				}
				else if(currentTurn == OBSERVER){
					System.out.println("Client>> you can only observe");
				}
				else{
					System.out.println("Client>> it is not your turn");
					System.out.println("Client>> waiting for opponent to move...");
				}
		    }
		    else if (action.equalsIgnoreCase("print")){
				printBoard();
		    }
		    else if (action.equals("?")){
		    	System.out.println("\n");
		    	System.out.println("valid system commands:");
		    	System.out.println("   connect");
		    	System.out.println("   print");
		    	System.out.println("   move <x> <y>");
		    	System.out.println("   end");
		    	System.out.println("\n");
		    	System.out.println("Client>> ");
		    }
		    else if (action.equalsIgnoreCase("end")){
				System.exit(0);
		    }
		}
    }

    ReversiClient() throws RemoteException{
		try{
		    reversiGame = (ReversiServerInterface) Naming.lookup("ReversiGame");
		}
		catch(Exception e){
		    System.out.println(e);
		}

		myColor = WAIT;
		currentTurn = WAIT;
    }

   	public static void main(String[] args) throws Exception{
		new ReversiClient().start();
    }
}
