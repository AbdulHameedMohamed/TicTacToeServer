/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Player;
import util.Constants;
import java.lang.reflect.Type;

import java.util.HashSet;
import java.util.Set;

import util.Database;

/**
 *
 * @author w
 */
public class ServerHandler extends Thread {

    private static final Vector<ServerHandler> PLAYERS_SOCKET = new Vector(); //maybe Set
    public Socket socket;
    public DataInputStream in;
    public PrintStream out;
    boolean isRunning = true;

    Gson gson = new Gson();
    ArrayList requestData;

    public ServerHandler(Socket socket) {

        try {
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new PrintStream(socket.getOutputStream());
            PLAYERS_SOCKET.add(this);

        } catch (IOException e) {
            e.printStackTrace();
        }

        start();

    }

    @Override
    public void run() {
        try {
            while (isRunning) {
                String gsonRequest = in.readLine();
                if (!gsonRequest.isEmpty()) {
                    handleRequest(gsonRequest);
                }
            }
        } catch (IOException ex) {
            try {
                out.close();
                in.close();
                socket.close();
                isRunning = false;

                PLAYERS_SOCKET.remove(this);
            } catch (IOException ex1) {
                Logger.getLogger(ServerHandler.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }

    private void handleRequest(String gsonRequest) {

        Type listType = new TypeToken<ArrayList<Object>>() {
        }.getType();
        requestData = gson.fromJson(gsonRequest, listType);

        double action = (double) requestData.get(0);

        switch ((int) action) {
            case Constants.REGISTER:
                register();
                break;
            case Constants.LOGIN:
                login();
                break;
            case Constants.GET_AVAILIABLE_PLAYERS:
                System.out.println("getAvailablePlayers from server1111");
                getAvailablePlayers();

                break;
            case Constants.REQUEST:
                request();
                break;
            case 5:
                //TODO accept();
                break;
            case 6:
                //TODO updateBoard();
                break;
            case 7:
                //TODO logout();
                break;
            case 8:
                // TODO save();
                break;
            case 9:
                //TODO finish();
                break;
            case 10:
                //TODO updateScore();
                break;
            case 11:
                //getAvailablePlayers();
                break;
            case Constants.BROADCAST_MESSAGE:
                sendMessageToAll();
        }
    }

    private void register() throws JsonSyntaxException {
        Player newPlayer = gson.fromJson(gson.toJson(requestData.get(1)), Player.class);
        boolean isRegisterd = Database.registerPlayer(newPlayer);

        ArrayList jsonResponse = new ArrayList();
        jsonResponse.add(Constants.REGISTER);
        jsonResponse.add(isRegisterd);

        String gsonRequest = gson.toJson(jsonResponse);
        out.println(gsonRequest);
    }

   private void login() throws JsonSyntaxException {
        Player currentplayer = gson.fromJson(gson.toJson(requestData.get(1)), Player.class);

        int playerId = 2;
        //int playerId= checkInDB(currentPlayer);

        int authenticatePlayerId = Database.authenticatePlayer(currentplayer);

        ArrayList<Integer> jsonResponse = new ArrayList();
        jsonResponse.add(Constants.LOGIN);
        jsonResponse.add(authenticatePlayerId);
        
        

        String gsonResponse = gson.toJson(jsonResponse);
        out.println(gsonResponse);
        
    }

   
  private void getAvailablePlayers() {
        System.out.println("getAvailablePlayers from server");
        ArrayList<Player> players = Database.getAvailablePlayers();
        ArrayList<Object> jsonResponse = new ArrayList();
        jsonResponse.add(Constants.GET_AVAILIABLE_PLAYERS);
        
         for (Player player : players) {
             jsonResponse.add((double)player.getId());
             jsonResponse.add(player.getName());
             jsonResponse.add((double)player.getScore());
             System.out.println("player Data :"+player.getId()+" "+player.getName()+ " "+player.getScore());
            }
         
        String gsonRequest = gson.toJson(jsonResponse);
        out.println(gsonRequest);
    }
  
 private void sendMessageToAll() {
        double sourceId = (double) requestData.get(1);
        String broadCastMessage = (String) requestData.get(2);

        String sourcePlayerName = Database.getPlayerName((int) sourceId);

        ArrayList<Object> jsonResponse = new ArrayList();
        jsonResponse.add(Constants.BROADCAST_MESSAGE);
        jsonResponse.add(sourcePlayerName);
        jsonResponse.add(broadCastMessage);
        String gsonResponse = gson.toJson(jsonResponse);

        PLAYERS_SOCKET.forEach((serverHandler) -> {
            System.out.println(serverHandler);
            serverHandler.out.println(gsonResponse);
        });
    }
    private void request() {
        double senderId = (double) requestData.get(1);
        double receiverId = (double) requestData.get(2);
        Player p = Database.getPlayerNameAndScore((int)senderId);
        System.out.println("player Data :"+(int)senderId+" "+p.getName()+" "+p.getScore());
        //Player player = new Player((int)senderId, p.getEmail(), p.getPassword());
        ArrayList<Object> jsonResponse = new ArrayList<>();
        jsonResponse.add(Constants.REQUEST);
        jsonResponse.add(senderId);
        jsonResponse.add( p.getName());
        jsonResponse.add(p.getScore());
        String gsonResponse = gson.toJson(jsonResponse);
        out.println(gsonResponse);
        System.out.println("Request received from: " + senderId + " to: " + receiverId);

    }

   

    public static void closeSockets() {
        try {
            for (ServerHandler serverHandler : PLAYERS_SOCKET) {
                serverHandler.in.close();
                serverHandler.out.close();
                serverHandler.socket.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
