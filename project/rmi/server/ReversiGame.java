import java.rmi.Naming;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class ReversiGame extends UnicastRemoteObject implements ReversiServerInterface{

	static final int WAIT = 0;
	static final int WHITE = 1;
	static final int BLACK = 2;
	static final int OBSERVER = 3;
	static final int BLACKAGAIN = 4;
	static final int WHITEAGAIN = 5;
	static final int GAMEOVER = 6;

	int white[][] = new int[8][8];
    int black[][] = new int[8][8];
    int returnMove[][] = new int[8][8];
	int validMoves[][][] = new int[8][8][8];

	int currentTurn;
	int currentRegisteredClient;
	boolean isValid;
	boolean gameOver;
	boolean noMoreValidMovesBlack;
	boolean noMoreValidMovesWhite;

	ReversiClientInterface opponentWhite = null;
	ReversiClientInterface opponentBlack = null;
	ArrayList<ReversiClientInterface> observersList = new ArrayList<ReversiClientInterface>();

	public String printWinner() throws RemoteException{
		int countBlack = 0;
		int countWhite = 0;

		String returnValue = null;

		for (int x = 0; x < 8; x++){
			for (int y = 0; y < 8; y++){
				if(black[x][y] == 1){
					countBlack++;
				}
				if(white[x][y] == 1){
					countWhite++;
				}
			}
		}

		if(countBlack > countWhite){
			returnValue = "BLACK";
		}
		else if(countWhite > countBlack){
			returnValue = "WHITE";
		}
		else{
			returnValue = "stalemate";
		}

		return returnValue;
	}

	private void findStreak(int x, int y, int direction, int color){
		int myMoves[][] = new int[8][8];
		int opMoves[][] = new int[8][8];
		switch(color){
			case BLACK:
				myMoves = black;
				opMoves = white;
			break;
			case WHITE:
				myMoves = white;
				opMoves = black;
			break;
		}

		boolean boardBoundaryReached = false;
		if(x<0 || y<0 || x>7 || y>7){
			boardBoundaryReached = true;
		}
		while(!boardBoundaryReached && opMoves[x][y] == 1){
			switch(direction){
				case 0:
					//northwest
					x--;
					y--;
					if(x<0 || y<0 || x>7 || y>7){
						boardBoundaryReached = true;
					}
				break;
				case 1:
					//north
					y--;
					if(x<0 || y<0 || x>7 || y>7){
						boardBoundaryReached = true;
					}
				break;
				case 2:
					//northeast
					x++;
					y--;
					if(x<0 || y<0 || x>7 || y>7){
						boardBoundaryReached = true;
					}
				break;
				case 3:
					//west
					x--;
					if(x<0 || y<0 || x>7 || y>7){
						boardBoundaryReached = true;
					}
				break;
				case 4:
					//east
					x++;
					if(x<0 || y<0 || x>7 || y>7){
						boardBoundaryReached = true;
					}
				break;
				case 5:
					//southwest
					x--;
					y++;
					if(x<0 || y<0 || x>7 || y>7){
						boardBoundaryReached = true;
					}
				break;
				case 6:
					//south
					y++;
					if(x<0 || y<0 || x>7 || y>7){
						boardBoundaryReached = true;
					}
				break;
				case 7:
					//southeast
					x++;
					y++;
					if(x<0 || y<0 || x>7 || y>7){
						boardBoundaryReached = true;
					}
				break;
			}
		}

		if(!boardBoundaryReached && myMoves[x][y] != 1 && opMoves[x][y] != 1){
			validMoves[x][y][direction] = 1;
			//System.out.println(x+" "+y);
		}
	}

	private void findValidMoves(int color){
		int myMoves[][] = new int[8][8];
		int opMoves[][] = new int[8][8];
		switch(color){
			case BLACK:
				myMoves = black;
				opMoves = white;
			break;
			case WHITE:
				myMoves = white;
				opMoves = black;
			break;
		}

		//clear array
		for(int a = 0; a < 8; a++){
			for(int b = 0; b < 8; b++){
				for(int c = 0; c < 8; c++){
					validMoves[a][b][c] = 0;
				}
			}
		}

		for (int x = 0; x < 8; x++){
			for (int y = 0; y < 8; y++){
				if(myMoves[x][y] == 1){
					for (int i = 0; i < 8; i++){
						switch(i){
							case 0:
								//northwest
								if(((x-1)>0 && (y-1)>0) && opMoves[x-1][y-1] == 1){
									findStreak(x-1,y-1,i,color);
								}
							break;
							case 1:
								//north
								if(((y-1)>0) && opMoves[x][y-1] == 1){
									findStreak(x,y-1,i,color);
								}
							break;
							case 2:
								//northeast
								if(((x+1)<8 && (y-1)>0) && opMoves[x+1][y-1] == 1){
									findStreak(x+1,y-1,i,color);
								}
							break;
							case 3:
								//west
								if(((x-1)>0) && opMoves[x-1][y] == 1){
									findStreak(x-1,y,i,color);
								}
							break;
							case 4:
								//east
								if(((x+1)<8) && opMoves[x+1][y] == 1){
									findStreak(x+1,y,i,color);
								}
							break;
							case 5:
								//southwest
								if(((x-1)>0 && (y+1)<8) && opMoves[x-1][y+1] == 1){
									findStreak(x-1,y+1,i,color);
								}
							break;
							case 6:
								//south
								if(((y+1)<8) && opMoves[x][y+1] == 1){
									findStreak(x,y+1,i,color);
								}
							break;
							case 7:
								//southeast
								if(((x+1)<8 && (y+1)<8) && opMoves[x+1][y+1] == 1){
									findStreak(x+1,y+1,i,color);
								}
							break;
						}
					}
				}
			}
		}
	}

	private boolean checkMove(int movx, int movy, int color){
		boolean returnValue = false;

		int myMoves[][] = new int[8][8];
		int opMoves[][] = new int[8][8];
		switch(color){
			case BLACK:
				myMoves = black;
				opMoves = white;
			break;
			case WHITE:
				myMoves = white;
				opMoves = black;
			break;
		}

		//calculate valid moves
		//System.out.println("valid moves for "+color+":");
		findValidMoves(color);

		//check if move is valid
		for(int d = 0; d < 8; d++){
			if(validMoves[movx][movy][d] == 1){
				myMoves[movx][movy] = 1;
				//flip opponents squares
				int sx = movx;
				int sy = movy;
				switch(d){
					case 0:
						//southeast
						sx++;
						sy++;
						while(opMoves[sx][sy] == 1){
							opMoves[sx][sy] = 0;
							myMoves[sx][sy] = 1;
							sx++;
							sy++;
						}
					break;
					case 1:
						//south
						sy++;
						while(opMoves[sx][sy] == 1){
							opMoves[sx][sy] = 0;
							myMoves[sx][sy] = 1;
							sy++;
						}
					break;
					case 2:
						//southwest
						sx--;
						sy++;
						while(opMoves[sx][sy] == 1){
							opMoves[sx][sy] = 0;
							myMoves[sx][sy] = 1;
							sx--;
							sy++;
						}
					break;
					case 3:
						//east
						sx++;
						while(opMoves[sx][sy] == 1){
							opMoves[sx][sy] = 0;
							myMoves[sx][sy] = 1;
							sx++;
						}
					break;
					case 4:
						//west
						sx--;
						while(opMoves[sx][sy] == 1){
							opMoves[sx][sy] = 0;
							myMoves[sx][sy] = 1;
							sx--;
						}
					break;
					case 5:
						//northeast
						sx++;
						sy--;
						while(opMoves[sx][sy] == 1){
							opMoves[sx][sy] = 0;
							myMoves[sx][sy] = 1;
							sx++;
							sy--;
						}
					break;
					case 6:
						//north
						sy--;
						while(opMoves[sx][sy] == 1){
							opMoves[sx][sy] = 0;
							myMoves[sx][sy] = 1;
							sy--;
						}
					break;
					case 7:
						//northwest
						sx--;
						sy--;
						while(opMoves[sx][sy] == 1){
							opMoves[sx][sy] = 0;
							myMoves[sx][sy] = 1;
							sx--;
							sy--;
						}
					break;
				}
				switch(color){
					case BLACK:
						black = myMoves;
						white = opMoves;
					break;
					case WHITE:
						white = myMoves;
						black = opMoves;
					break;
				}
				returnValue = true;
			}
		}

    	return returnValue;
    }

    public int[][] getBlack(){
		return black;
    }

    public int[][] getWhite(){
		return white;
    }

	public int move(int x, int y, int color) throws RemoteException{
		switch(currentTurn){
			case BLACKAGAIN:
				currentTurn = BLACK;
			break;
			case WHITEAGAIN:
				currentTurn = WHITE;
			break;
		}
		if(color == currentTurn){
			if(checkMove(x,y,currentTurn)){
				switch(color){
					case BLACK:
						currentTurn = WHITE;

						//check if there are still valid moves
						noMoreValidMovesWhite = true;
						findValidMoves(WHITE);
						for(int a = 0; a < 8; a++){
							for(int b = 0; b < 8; b++){
								for(int c = 0; c < 8; c++){
									if(validMoves[a][b][c] == 1){
										noMoreValidMovesWhite = false;
									}
								}
							}
						}
						noMoreValidMovesBlack = true;
						findValidMoves(BLACK);
						for(int a = 0; a < 8; a++){
							for(int b = 0; b < 8; b++){
								for(int c = 0; c < 8; c++){
									if(validMoves[a][b][c] == 1){
										noMoreValidMovesBlack = false;
									}
								}
							}
						}

						if(noMoreValidMovesWhite){
							if(noMoreValidMovesBlack){
								//game over
								gameOver = true;
								currentTurn = GAMEOVER;
								opponentWhite.pushMove(GAMEOVER);
							}
							else{
								//this opponent cannot move, turn is passed
								currentTurn = BLACKAGAIN;
								opponentWhite.pushMove(BLACK);
							}
						}
						else{
							opponentWhite.pushMove(WHITE);
						}
					break;
					case WHITE:
						currentTurn = BLACK;

						//check if there are still valid moves
						noMoreValidMovesBlack = true;
						findValidMoves(BLACK);
						for(int a = 0; a < 8; a++){
							for(int b = 0; b < 8; b++){
								for(int c = 0; c < 8; c++){
									if(validMoves[a][b][c] == 1){
										noMoreValidMovesBlack = false;
									}
								}
							}
						}
						noMoreValidMovesWhite = true;
						findValidMoves(WHITE);
						for(int a = 0; a < 8; a++){
							for(int b = 0; b < 8; b++){
								for(int c = 0; c < 8; c++){
									if(validMoves[a][b][c] == 1){
										noMoreValidMovesWhite = false;
									}
								}
							}
						}

						if(noMoreValidMovesBlack){
							if(noMoreValidMovesWhite){
								//game over
								gameOver = true;
								currentTurn = GAMEOVER;
								opponentBlack.pushMove(GAMEOVER);
							}
							else{
								//this opponent cannot move, turn is passed
								currentTurn = WHITEAGAIN;
								opponentBlack.pushMove(WHITE);
							}
						}
						else{
							opponentBlack.pushMove(BLACK);
						}
					break;
				}
				//notify observer(s) of new move
				for (int obv = 0; obv < observersList.size(); obv++){
					ReversiClientInterface temp = observersList.get(obv);
					if(gameOver){
						temp.pushMove(GAMEOVER);
					}
					else{
						temp.pushMove(OBSERVER);
					}
				}
			}
		}

		return currentTurn;
    }

    public int registerClient(ReversiClientInterface client) throws RemoteException{

    	if(gameOver){
    		reset();
    	}

    	switch(currentTurn){
    		case WAIT:
    			if(opponentBlack == null){
					opponentBlack = client;
					currentTurn = WAIT;

					currentRegisteredClient = BLACK;
    			}
    			else if(opponentWhite == null){
					opponentWhite = client;
    				currentTurn = BLACK;

    				//call client interfaces for game start
					opponentBlack.startGame(currentTurn);
					opponentWhite.startGame(currentTurn);

    				currentRegisteredClient = WHITE;
    			}
    		break;

    		case BLACK:
    		case WHITE:
    			//do not allow more than two peers to register
    			currentRegisteredClient = OBSERVER;
    			observersList.add(client);
			break;

    	}

    	return currentRegisteredClient;
    }

    private void reset(){
    	for (int x = 0; x < 8; x++){
			for (int y = 0; y < 8; y++){
				white[x][y] = 0;
				black[x][y] = 0;
				returnMove[x][y] = 0;
			}
		}

		white[3][3] = 1;
		black[3][4] = 1;
		white[4][4] = 1;
		black[4][3] = 1;

		currentTurn = WAIT;
		currentRegisteredClient = WAIT;
		isValid = false;
		noMoreValidMovesBlack = false;
		noMoreValidMovesWhite = false;
		gameOver = false;

		opponentWhite = null;
		opponentBlack = null;
		observersList.clear();
    }

   	ReversiGame() throws RemoteException{
		super();

		gameOver = false;

		opponentWhite = null;
		opponentBlack = null;

		currentTurn = WAIT;

		//initialize board
		for (int x = 0; x < 8; x++){
			for (int y = 0; y < 8; y++){
				//white[x][y] = 1;
				white[x][y] = 0;
				black[x][y] = 0;
			}
		}

		white[3][3] = 1;
		black[3][4] = 1;

		//remove
		//white[3][4] = 0;

		white[4][4] = 1;
		black[4][3] = 1;

		//remove
		//white[4][3] = 0;

		//test purposes
		//for(int z = 0;z<8;z++)
		//	white[2][z]=0;
    }

}

