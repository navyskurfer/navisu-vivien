/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bzh.terrevirtuelle.navisu.server.impl.gpsd.impl;

import org.capcaval.c3.component.annotation.UsedService;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.net.NetSocket;

import bzh.terrevirtuelle.navisu.server.impl.gpsd.NetReader;

/**
 *
 * @author Serge
 */

public class NetReaderImpl
        implements NetReader {
	
	public NetReaderImpl(int index, Vertx vertx, String hostname, int port) {
        vertx.createNetClient().connect(port, hostname, (AsyncResultHandler<NetSocket>) new AsyncResultHandler<NetSocket>() {

            @Override
            public void handle(AsyncResult<NetSocket> asyncResult) {
                if (asyncResult.succeeded()) {
                    NetSocket socket = asyncResult.result();
                    socket.dataHandler((Buffer buffer) -> {
                        String source = buffer.toString().trim();
                      
                        if (source.contains("tcp://data.aishub.net:4299")) {
                        	System.out.println("atl");
//                            try {
//								TimeUnit.MILLISECONDS.sleep(200);
//							} catch (InterruptedException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
                        }
                        if (source.contains("tcp://data.aishub.net:4572")) {
                        	System.out.println("med");
//                            try {
//								TimeUnit.MILLISECONDS.sleep(200);
//							} catch (InterruptedException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
                        }
                        
                        if ((source.startsWith("{") && source.endsWith("}")) // Gpsd well formatted
                                || source.startsWith("!") // AIS
                                || source.startsWith("$") // NMEA0183
                                || source.startsWith("PGN")) { // N2K
                            if (source.contains("class")) {//Revoir le parser GPSD, supprimer la negation
                                vertx.eventBus().send("comm-address" + index, source);
                                //System.out.println(source);
                            }
                        }

                    });
                    socket.write(new Buffer("?WATCH={\"enable\":true,\"json\":true};"));
                }
            }
        });
    }

    @Override
    public void read() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getData() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
