package liming.tool.handle.tcp;

import liming.tool.handle.FileRW;
import liming.tool.pool.ListensPool;
import liming.tool.pool.Pools;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class TcpServetSocket extends TcpSocket implements TcpServer {
    List<ServerSocket> sockets;

    private final int BACK_LOG,HAND_SIZE;
    private final long TIME_OUT;

    ListensPool<Socket> listensPool;

    private ScheduledExecutorService executorService;

    /**
     *Tcp服务端
     * @param serverSocket 监听
     * @param backlog 最大监听数
     * @param handSize 最大同时处理数
     * @param timeOut 处理超时时间
     */
    public TcpServetSocket(ServerSocket serverSocket, int backlog, int handSize, long timeOut) {
        super(serverSocket.getInetAddress(),serverSocket.getLocalPort());
        BACK_LOG=backlog;
        HAND_SIZE=handSize;
        TIME_OUT= timeOut;
        listensPool=new ListensPool<Socket>(Pools.getListPool("System.tcp.server"),0,timeOut, handSize) {
            @Override
            public void handle(Socket socket) throws Exception {
                TcpReceiveMap request=read(socket),respond=new TcpReceiveMap();
                boolean close=false;
                try {
                    close=doWork(request,respond);
                }catch (Exception e){
                    respond.put("Exception", FileRW.getError(e));
                    respond.setState(false);
                }catch (Error e){
                    respond.put("Error", FileRW.getError(e));
                    respond.setState(false);
                }
                finally {
                    write(socket,respond);
                    if(!close)
                        socket.close();
                }
            }
        };
        sockets=new ArrayList<>(backlog);
        executorService= Executors.newScheduledThreadPool(backlog);
        add(serverSocket);
    }

    public void add(ServerSocket serverSocket){
        if(sockets.size()<BACK_LOG){
            sockets.add(serverSocket);
            executorService.scheduleAtFixedRate(()->{while (!serverSocket.isClosed()){
                try {
                    listensPool.put(serverSocket.accept());
                } catch (IOException ignored) {}
            }
            },0,Math.max(TIME_OUT,1), TimeUnit.MILLISECONDS);
        }
    }

    public TcpServetSocket(int port) throws IOException {
        this(new ServerSocket(port,50),50,20,30_000);
    }

    @Override
    public TcpReceiveMap addReceiveMap(TcpReceiveMap request) throws Exception {
        TcpReceiveMap respond=new TcpReceiveMap();
        try {
            doWork(request,respond);
        }catch (Exception e){
            respond.put("Exception", FileRW.getError(e));
            respond.setState(false);
        }catch (Error e){
            respond.put("Error", FileRW.getError(e));
            respond.setState(false);
        }
        return respond;
    }

    @Override
    public void close() throws IOException {
        listensPool.close();
        executorService.shutdownNow();
        for (ServerSocket socket:sockets) socket.close();
    }
}
