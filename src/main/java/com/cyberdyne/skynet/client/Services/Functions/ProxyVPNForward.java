package com.cyberdyne.skynet.client.Services.Functions;

import com.cyberdyne.skynet.client.Services.Config.Config;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProxyVPNForward
{

    private final ExecutorService threadPool;


    //Get constractor function
    public ProxyVPNForward(String VPNHostAddress, int Port)
    {

        // Create a thread pool instead of creating unlimited threads
        threadPool = Executors.newCachedThreadPool();


        try
        {
            ServerSocket Server = new ServerSocket(Port);

            while (true)
            {
                Socket request=Server.accept();

                threadPool.submit(()->{
                    try
                    {
                        GetHandleProxy(VPNHostAddress,request);
                    }
                    catch (Exception e)
                    {

                    }
                });
            }
        }
        catch (Exception e)
        {

        }
    }



    //Get handle request from socket
    public static void GetHandleProxy(String VPNHostAddress,Socket request)
    {
        try
        {
            //Get add forward socket
            Socket ForwardSocket = new Socket(VPNHostAddress, Config.VPNPort);

            //Proxy streams
            BufferedWriter BW=new BufferedWriter(new OutputStreamWriter(request.getOutputStream()));
            BufferedReader BR=new BufferedReader(new InputStreamReader(request.getInputStream()));

            System.out.println("New request...");

            Thread CTS = ForwardRequestsAndReponses(ForwardSocket,request,"CTS",BR,BW);
            Thread STC = ForwardRequestsAndReponses(ForwardSocket,request,"STC",BR,BW);

            CTS.join();
            STC.join();
        }
        catch (Exception e)
        {

        }
    }



    //Get forward function start
    public static Thread ForwardRequestsAndReponses(Socket ForwardSocket,Socket request,String Direction,BufferedReader BR,BufferedWriter BW) throws Exception
    {
        Thread result = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream IS=ForwardSocket.getInputStream();
                    OutputStream OS=ForwardSocket.getOutputStream();

                    byte[] buffer = new byte[4096];
                    int bytesRead;

                    while (true)
                    {

                        if(request.getInputStream().available() > 0)
                        {
                            bytesRead = request.getInputStream().read(buffer);
                            if (bytesRead == -1) break;
                            OS.write(buffer,0,bytesRead);
                            OS.flush();
                        }

                        if(ForwardSocket.getInputStream().available() > 0)
                        {
                            bytesRead = ForwardSocket.getInputStream().read(buffer);
                            if (bytesRead == -1) break;
                            request.getOutputStream().write(buffer,0,bytesRead);
                            request.getOutputStream().flush();
                        }

                        // Small delay to prevent tight spinning
//                Thread.sleep(10);

                    }


                }
                catch (Exception e)
                {

                }
            }
        });

        result.start();
        return result;
    }
    //Get forward function end


}