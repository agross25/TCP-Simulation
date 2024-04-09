/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package SocketProgramming;

import java.net.*;
import java.io.*;
import java.util.*;

public class SimpleServer {
    
    public static void main(String[] args) throws IOException {
       
        args = new String[] {"30121"}; //port number enables connection with client

        if (args.length != 1) {
            System.err.println("Usage: java EchoServer <port number>");
            System.exit(1);
}
       
        int portNumber = Integer.parseInt(args[0]);
       
        try (ServerSocket server = new ServerSocket(Integer.parseInt(args[0]));
            Socket client = server.accept(); //accepts connection request from client
            PrintWriter responseWriter = new PrintWriter(client.getOutputStream(), true); //will send messages to client
            BufferedReader requestReader = new BufferedReader(new InputStreamReader(client.getInputStream()));) //will read messages from client
            {
                responseWriter.println("Server: Connection successful."); //sends to client
                System.out.println("Server: Connection successful.");
                responseWriter.println("Server: Generating message:"); //sends to client
                System.out.println("Server: Generating message:");
                
                String message = "This is a message to send to the client via TCP simulation."; //could be manually changed
                int numPackets = 20; //could be manually changed
                int packetSize = 0;
                if (message.length() >= 20)
                    packetSize = (int)(message.length()/numPackets); //each packet except the last one will have this many chars
                else
                    System.out.println("Invalid message. Message length must exceed number of packets.");
                
                ArrayList<String> packets = new ArrayList<String>(); //will hold packets
                
                //loop will separate message into packets and put them in array list
                int start = 0;
                int end = packetSize;
                for (int i=0; i<numPackets; i++)
                {
                    String str;
                    if (i == numPackets-1) //if this is the final packet
                    {
                        Collections.shuffle(packets); //randomizes array so packets will deliver out of order
                        str = i + "@@@" + message.substring(start); //to the end of the message
                        str += "&&&"; //end with final packet signature
                    }
                    else {
                        str = i + "@@@" + message.substring(start, end);
                        start += packetSize;
                        end += packetSize; //updates index for next substring 
                    }
                    packets.add(str); //ends with randomized array list with last packet at end
                }
                
                //each packet is sent using a loop with 20% probability of dropping
                for (int j=0; j<packets.size(); j++)
                {
                    int rand = (int)((Math.random()*100)+1); //generates random number between 1 and 100
                    if ((rand > 20) || (j==packets.size()-1)) //80% chance of delivery, unless it is the last packet in which case it will always send
                        responseWriter.println(packets.get(j)); //sends to client
                    System.out.println("Server: packet sent."); //server maintains no record of dropped packets
                }
                System.out.println("Final packet sent."); //server assumes all packets were sent        
                
                String userRequest;
                if ((userRequest = requestReader.readLine()) != null)
                    System.out.println("Client: " + userRequest); //receives message from client that last packet was received
                                
                boolean messageSent = false;
                
                //check for missing packets
                while (messageSent == false)
                {
                    ArrayList<Integer> missingPackets = new ArrayList<>();
                    if ((userRequest = requestReader.readLine()) != null)
                        System.out.println("Client: " + userRequest); //receives message from client indicating if and which packets are missing
                    if (userRequest.equals("All packets received."))
                        messageSent = true; //will not run loop again
                    else {
                        String[] missingNums = userRequest.split(" ", -2); //isolates sequence numbers of missing packets
                        for (String str : missingNums)
                        {
                            if (str.length() > 0)
                                missingPackets.add(Integer.valueOf(str));
                        }
                        for (int i=0; i<packets.size(); i++)
                        {
                            int index = packets.get(i).indexOf("@@@");
                            int currentNum = Integer.parseInt(packets.get(i).substring(0, index));
                            if (missingPackets.contains(currentNum)) //re-sends if the packet was dropped
                            {
                                    int rand = (int)((Math.random()*100)+1); //random number between 1 and 100
                                    if (rand > 20) //80% chance of delivery
                                    {
                                        responseWriter.println(packets.get(i)); //re-sends packet to client
                                        System.out.println("Server: packet sent.");
                                    }
                            }
                        }
                        responseWriter.println("re-check"); //sends client message to re-check if packets were dropped
                        System.out.println("Server: re-check");
                    }
                    
                }
                
                if ((userRequest = requestReader.readLine()) != null)
                            System.out.println("Client: " + userRequest); //message from client indicating all packets were received
                
                responseWriter.close();
                requestReader.close();
                System.out.println("Message Sent. Terminating Connection.");
                //end of program
                //connection terminated
                
            }
    
        catch (IOException e) {
            System.out.println(
            "Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
   
    }
    
}
