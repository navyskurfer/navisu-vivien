/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bzh.terrevirtuelle.navisu.server.impl.gpsd.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.print.attribute.standard.Media;

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
    
    protected int countAtl = 0;
	protected int countMed = 0;
	
	protected Date dateAtl;
	protected Date dateMed;
	
    protected static final String ANSI_RESET = "\u001B[0m";
    protected static final String ANSI_BLACK = "\u001B[30m";
    protected static final String ANSI_RED = "\u001B[31m";
    protected static final String ANSI_GREEN = "\u001B[32m";
    protected static final String ANSI_YELLOW = "\u001B[33m";
    protected static final String ANSI_BLUE = "\u001B[34m";
    protected static final String ANSI_PURPLE = "\u001B[35m";
    protected static final String ANSI_CYAN = "\u001B[36m";
    protected static final String ANSI_WHITE = "\u001B[37m";
    
    protected DateFormat dateFormatDate = new SimpleDateFormat("dd/MM/yyyy");
    protected DateFormat dateFormatTime = new SimpleDateFormat("HH:mm:ss");
    
    public NetReaderImpl(int index, Vertx vertx, String hostname, int port) {
        vertx.createNetClient().connect(port, hostname, (AsyncResultHandler<NetSocket>) new AsyncResultHandler<NetSocket>() {

            @Override
            public void handle(AsyncResult<NetSocket> asyncResult) {
                if (asyncResult.succeeded()) {
                    NetSocket socket = asyncResult.result();
                    socket.dataHandler((Buffer buffer) -> {
                        String source = buffer.toString().trim();
                        
                        //System.out.println(source);
                      
								if (source.contains("tcp://data.aishub.net:4299")) {
									countAtl++;
									dateAtl = new Date();
									if (countAtl % 1000 == 0) {
										System.out.println(ANSI_PURPLE + dateFormatTime.format(dateAtl) + " - ATL stream" + ANSI_RESET);
									}
								}

								if (source.contains("tcp://data.aishub.net:4572")) {
									countMed++;
									dateMed = new Date();
									if (countMed % 500 == 0) {
										System.out.println(ANSI_BLUE + dateFormatTime.format(dateMed) + " - MED stream" + ANSI_RESET);
									}
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
    
    public Date getAtlDate() {
    	return dateAtl;
    }
    
    public Date getMedDate() {
    	return dateMed;
    }

}
