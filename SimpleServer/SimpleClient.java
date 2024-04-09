/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package SocketProgramming;

import java.net.*;
import java.io.*;
import java.util.*;

public class SimpleClient {
    
    public static void main(String[] args) throws IOException {
        
        args = new String[] {"127.0.0.1", "30121"}; 
        //args contains IP address and port number that client will use to request connection with server

        if (args.length != 2) {
            System.err.println( "Usage: java EchoClient <host name> <port number>");
            System.exit(1);
        }

            String hostName = args[0];
            int portNumber = Integer.parseInt(args[1]);

            try (Socket client = new Socket(hostName, portNumber); //client requests connection to server
                PrintWriter requestWriter = new PrintWriter(client.getOutputStream(), true); // stream to write text requests to server
                BufferedReader responseReader = new BufferedReader(new InputStreamReader(client.getInputStream())); ) // stream to read text response from server
            {
                String serverResponse;
                while ((serverResponse = responseReader.readLine()) != null)  { //client receives message from server
                    System.out.println(serverResponse); 
                    if (serverResponse.contains("Generating message:"))
                    {   System.out.println("ready for packets"); //indicates to user that packets will arrive momentarily
                        break;  } 
                }
                           
                boolean messageSent = false; //will identify when all packets are received
                String p; //will temporarily hold packet
                int numPackets = 0; //how many packets in total
                ArrayList<String> packetsFinal = new ArrayList<String>(); //will hold packets in order
                
                while (messageSent == false) {
                    ArrayList<String> packetsRandom = new ArrayList<>(); //will hold packets out of order
                    while((p = responseReader.readLine()) != null) //client receives packet as a message from server
                    {
                        if (p.equals("re-check")) //signal to re-check if any packets are missing
                            break;
                        System.out.println("packet received."); //indicate to user that a packet was delivered
                        packetsRandom.add(p); //packetsRandom will be cleared before each new run of loop
                        if (p.substring(p.length()-3).equals("&&&")) //if it is the last packet
                        {   requestWriter.println("last packet received."); //send message to server
                            System.out.println("last packet received."); //indicate to user
                            int index = p.indexOf("@@@");
                            numPackets = (Integer.parseInt(p.substring(0, index)))+1;
                            for (int i=0; i<numPackets; i++)
                                packetsFinal.add( null); //will allow us to fill packetsFinal accurately later
                            break;
                        }
                    }
                    
                    //places packets in correct locations in packetsFinal
                    for (int i=0; i<packetsRandom.size(); i++)
                    {
                        p = packetsRandom.get(i);
                        int index = p.indexOf("@@@"); 
                        int packetNum = 0;
                        try { packetNum = Integer.parseInt(p.substring(0, index)); } //isolates number
                        catch (NumberFormatException ex) { 
                            System.err.println("Error with packet number."); 
                            System.exit(1); }
                        if (packetNum == packetsFinal.size()-1) //if p is the last packet
                            packetsFinal.set(packetNum, p.substring(index+3, p.length()-3));
                        else
                            packetsFinal.set(packetNum, p.substring(index+3));
                    }
                    
                    //checking for null values
                    String missingPackets = "";
                    for (int i=0; i<packetsFinal.size(); i++)
                    {
                        if (packetsFinal.get(i) == null)
                            missingPackets += i + " "; //creates string of missing packets seperated by space
                    }
                    
                    if (missingPackets.equals("")) { //if empty, all packets have been received
                        messageSent = true; //will no longer run through loop
                    }
                    else {
                        System.out.println("Missing packets: " + missingPackets);
                        requestWriter.println(missingPackets); //sends message to server
                    }
                }
                
                System.out.println("All packets received.");
                requestWriter.println("All packets received."); //sends message to server
                
                String message = "";
                for (String str : packetsFinal)
                    message += str; //compiles full message
                System.out.println("Message from Server: " + message); //displays to user
                
                requestWriter.close();
                responseReader.close();
            }
            
            catch (UnknownHostException e) {
                System.err.println("Don't know about host " + hostName);
                System.exit(1);
            }

            catch (IOException e) {
                System.err.println("Couldn't get I/O for the connection to " + hostName);
                System.exit(1);
            }

        }
}
