/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bzh.terrevirtuelle.navisu.instruments.gpstrack.polygon.impl;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfaceText;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.measure.MeasureTool;
import gov.nasa.worldwind.util.measure.MeasureToolController;
import gov.nasa.worldwind.util.WWMath;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.CharBuffer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import java.io.*;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import bzh.terrevirtuelle.navisu.app.dpagent.DpAgentServices;
import bzh.terrevirtuelle.navisu.app.drivers.instrumentdriver.InstrumentDriver;
import bzh.terrevirtuelle.navisu.app.drivers.instrumentdriver.ZoneDriver;
import bzh.terrevirtuelle.navisu.app.guiagent.GuiAgentServices;
import bzh.terrevirtuelle.navisu.app.guiagent.geoview.GeoViewServices;
import bzh.terrevirtuelle.navisu.app.guiagent.layertree.LayerTreeServices;
import bzh.terrevirtuelle.navisu.client.nmea.controller.events.nmea183.GGAEvent;
import bzh.terrevirtuelle.navisu.client.nmea.controller.events.nmea183.RMCEvent;
import bzh.terrevirtuelle.navisu.client.nmea.controller.events.nmea183.VTGEvent;
import bzh.terrevirtuelle.navisu.core.view.geoview.layer.GeoLayer;
import bzh.terrevirtuelle.navisu.core.view.geoview.worldwind.impl.GeoWorldWindViewImpl;
import bzh.terrevirtuelle.navisu.domain.ship.model.Ship;
import bzh.terrevirtuelle.navisu.instruments.ais.base.AisServices;
import bzh.terrevirtuelle.navisu.instruments.ais.base.impl.controller.events.AisCreateTargetEvent;
import bzh.terrevirtuelle.navisu.instruments.ais.base.impl.controller.events.AisUpdateTargetEvent;
import bzh.terrevirtuelle.navisu.instruments.common.view.panel.TrackPanel;
import bzh.terrevirtuelle.navisu.instruments.common.view.targets.GShip;
import bzh.terrevirtuelle.navisu.instruments.gpstrack.polygon.GpsTrackPolygon;
import bzh.terrevirtuelle.navisu.instruments.gpstrack.polygon.GpsTrackPolygonServices;
import bzh.terrevirtuelle.navisu.server.DataServerServices;
import org.capcaval.c3.component.ComponentEventSubscribe;
import org.capcaval.c3.component.ComponentState;
import org.capcaval.c3.component.annotation.UsedService;
import org.capcaval.c3.componentmanager.ComponentManager;

/**
 * @date 3 mars 2015
 * @author Serge Morvan
 */
public class GpsTrackPolygonImpl implements GpsTrackPolygon,
        GpsTrackPolygonServices, ZoneDriver, ComponentState {

    @UsedService
    GeoViewServices geoViewServices;

    @UsedService
    DpAgentServices dpAgentServices;

    @UsedService
    GuiAgentServices guiAgentServices;

    @UsedService
    LayerTreeServices layerTreeServices;

    @UsedService
    AisServices aisServices;
    
    @UsedService
    DataServerServices dataServerServices;

    ComponentManager cm;
    ComponentEventSubscribe<GGAEvent> ggaES;
    ComponentEventSubscribe<RMCEvent> rmcES;
    ComponentEventSubscribe<VTGEvent> vtgES;

    ComponentEventSubscribe<AisCreateTargetEvent> aisCTEvent;
    ComponentEventSubscribe<AisUpdateTargetEvent> aisUTEvent;

    protected WorldWindow wwd;

    protected static final String GROUP = "Watch polygons";

    protected Ship watchedShip;

    protected boolean on = false;
    private final String NAME = "GpsTrackPolygon";

    protected boolean alarmOn = false;

    protected RenderableLayer polygonLayer;

    protected MeasureTool measureTool;
    protected MeasureToolController controller;
    protected LinkedList<MeasureTool> savedMeasureTool;

    protected boolean drawerActivated = false;
    protected int nbPolygon = 1;
    protected boolean freeHandActivated = false;
    protected LinkedList<Boolean> centered;
    protected LinkedList<Boolean> isTextOn;
    protected LinkedList<Boolean> isTextOnAis;
    protected LinkedList<SurfaceText> text;
    protected LinkedList<SurfaceText> textAis;
    protected LinkedList<RenderableLayer> polygonLayers;
    protected LinkedList<Position> centers;
    protected ArrayList<Ship> aisShips;
    protected ArrayList<Date> lastUpdateDate;
    protected LinkedList<String> lastShipArea;
    protected LinkedList<String> lastShipAreaUpdate;
    protected Date lastAtlDate;
    protected Date lastReceptionDateAtl;
    protected Date lastReceptionDateMed;
    protected Date lastSaveDate;
    protected boolean isTimerOn;
    protected Timer t;
    protected MeasureTool dmp;
    protected RenderableLayer dmpLayer;
    protected MeasureToolController dmpController;
    protected boolean dmpActivated = false;
    protected double diameter;
    protected double savedAltitude = 0;
    protected boolean firstDetection = false;
    protected String[][] shipMatrix = new String[6][100000];
    protected long count = 1;
    protected int maxTarget;
    protected String dayMaxTarget;
    protected String hourMaxTarget;
    protected boolean newRecord = false;
    protected int maxLastRun;
    protected int maxLastRunHisto;
    protected String dayMaxLastRun;
    protected String hourMaxLastRun;
    protected float maxRatio;
    protected String dayMaxRatio;
    protected String hourMaxRatio;
    protected boolean maxRatioRecord = false;
    
    ///////////////////////////////////////////// PARAMETERS //////////////////////////////////////////////////////
    
    protected long updateInterval = 15;   //number of minutes within ship position updates are not saved
    protected long updateInterval2 = 45;  //number of seconds for online ship position updates display
    protected int coldStart1 = 0;         //number of ships to create before getting database ships updates
    protected int coldStart3 = 25;        //number of ships to create before getting online ships updates
    protected int delayAtl = 15;          //number of seconds to restart ATL AIS stream (timer)
    protected int delayMed = 15;          //number of seconds to restart MED AIS stream (timer)
    protected int saveDelay = 60;		  //number of seconds to wait before next save
    
    /////////////////////////////////////////// OLD PARAMETERS ////////////////////////////////////////////////////
    
    //protected int coldStart2 = 25;        //number of ships to create before starting MED AIS stream
    //protected int coldStart4 = 550;       //number of ships to create before changing saved areas buffer size
    //protected int coldStart5 = 1050;      //number of ships to create before changing saved areas buffer size again
    //protected int restartFreq = 1;        //number of ship creations before attempting to restart AIS stream
    //protected int areaHistory = 15;       //number of saved areas on ship creation
    //protected int areaHistory2 = 10;      //number of saved areas on ship creation after buffer size change
    //protected int areaHistory3 = 7;       //number of saved areas on ship creation after second buffer size change
    //protected int waitRestartTime = 90;   //number of seconds since last target to restart ATL AIS stream
    //protected int waitRestartTime2 = 180; //number of seconds since last target to restart ATL AIS stream
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    protected int inSight = 0;
    protected int posUpdates = 0;
    protected int lastUpdateIndex = -1;
    protected int onlineDBupdate = 0;
    protected LinkedList<ArrayList<Position>> savedPolygons;
    protected MeasureTool pmt;
    protected MeasureToolController pmtc;
    protected RenderableLayer pLayer = new RenderableLayer();
    protected RenderableLayer tLayer = new RenderableLayer();
    protected ArrayList<Position> path = new ArrayList<Position>();
    protected static final String GROUP1 = "Target";
    protected static final String GROUP2 = "Path";
    protected static final String GROUP3 = "Rules";
    protected static final String GROUP4 = "Buffer";
    protected Ship pShip;
    protected GShip gShip;
    protected boolean pShipCreated = false;
    protected int etape = 0;
    protected Date t0;
    protected boolean verrou = false;
    protected boolean pathActivated = false;
    protected boolean pathLoaded = false;
    protected static final String ANSI_RESET = "\u001B[0m";
    protected static final String ANSI_BLACK = "\u001B[30m";
    protected static final String ANSI_RED = "\u001B[31m";
    protected static final String ANSI_GREEN = "\u001B[32m";
    protected static final String ANSI_YELLOW = "\u001B[33m";
    protected static final String ANSI_BLUE = "\u001B[34m";
    protected static final String ANSI_PURPLE = "\u001B[35m";
    protected static final String ANSI_CYAN = "\u001B[36m";
    protected static final String ANSI_WHITE = "\u001B[37m";
    protected TrackPanel aisTrackPanel;
    protected DateFormat dateFormatDate = new SimpleDateFormat("dd/MM/yyyy");
    protected DateFormat dateFormatTime = new SimpleDateFormat("HH:mm:ss");
    protected Boolean[][] detected = new Boolean[1000][5000];
    protected Boolean[] shipDetected = new Boolean[1000];
    protected Boolean[] aisShipDetected = new Boolean[5000];
    protected int nbSave = 0;
    protected int nbMmsiReceived = 0;
    protected int nbNamesReceived = 0;
    protected int nbNamesRetrieved = 0;
    protected int nbNamesMessages = 0;
    protected int updateMessages = 0;
    protected Date startTime;
    protected int nbNamesDB = 0;
    protected int distanceInterval = 70;
    protected int timeInterval = 7000;//7000;
    protected ArrayList<Position> reco = new ArrayList<Position>();
    protected ArrayList<SamplePositions> recos = new ArrayList<SamplePositions>();
    protected int nbPolygonMax = 10;
    protected int selectedPolygon = 0;
    protected boolean polygonSelected = false;
    protected LinkedList<ArrayList<Position>> trajectories;
    protected MeasureTool rmt;
    protected MeasureToolController rmtc;
    protected RenderableLayer rLayer;
    protected boolean ruleCreated = false;
    protected RenderableLayer aisTrackLayer;
    protected ShapeAttributes attrs;
    protected ArrayList<Path> aisPath;
    protected boolean componentReady = false;
    protected LinkedList<Boolean> hasRule;
    protected Position start = null;
    protected Position end = null;
    protected boolean mouseInsidePolygon = false;
    protected MouseAdapter ma1;
    protected MouseAdapter ma2;
    protected MouseAdapter ma3;
    protected MouseMotionAdapter mma1;

    @Override
    public void componentInitiated() {

        maxTarget = 0;
        watchedShip = new Ship();
        watchedShip.setMMSI(999999998);
        watchedShip.setName("PLASTRON2");
        aisShips = new ArrayList<Ship>();
        lastUpdateDate = new ArrayList<Date>();

        wwd = GeoWorldWindViewImpl.getWW();
        layerTreeServices.createGroup(GROUP);
        geoViewServices.getLayerManager().createGroup(GROUP);
        layerTreeServices.createGroup(GROUP3);
        geoViewServices.getLayerManager().createGroup(GROUP3);
        layerTreeServices.createGroup(GROUP4);
        geoViewServices.getLayerManager().createGroup(GROUP4);

        cm = ComponentManager.componentManager;
        ggaES = cm.getComponentEventSubscribe(GGAEvent.class);
        rmcES = cm.getComponentEventSubscribe(RMCEvent.class);
        vtgES = cm.getComponentEventSubscribe(VTGEvent.class);

        aisCTEvent = cm.getComponentEventSubscribe(AisCreateTargetEvent.class);
        aisUTEvent = cm.getComponentEventSubscribe(AisUpdateTargetEvent.class);

        measureTool = new MeasureTool(GeoWorldWindViewImpl.getWW());
        savedMeasureTool = new LinkedList<MeasureTool>();
        savedPolygons = new LinkedList<ArrayList<Position>>();
        centered = new LinkedList<Boolean>();
        lastShipArea = new LinkedList<String>();
        lastShipAreaUpdate = new LinkedList<String>();
        for (int k = 0; k < 100; k++) {
            centered.add(false);
        }

        controller = new MeasureToolController();
        measureTool.setController(controller);

        measureTool.setFollowTerrain(true);

        controller.setUseRubberBand(true);

        isTextOn = new LinkedList<Boolean>();
        for (int k = 0; k < 100; k++) {
            isTextOn.add(false);
        }

        isTextOnAis = new LinkedList<Boolean>();
        for (int k = 0; k < 100; k++) {
            isTextOnAis.add(false);
        }

        text = new LinkedList<SurfaceText>();
        for (int k = 0; k < 100; k++) {
            text.add(null);
        }

        textAis = new LinkedList<SurfaceText>();
        for (int k = 0; k < 100; k++) {
            textAis.add(null);
        }

        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < 5000; j++) {
                detected[i][j] = false;
            }
        }

        for (int i = 0; i < 1000; i++) {
            shipDetected[i] = false;
        }

        for (int i = 0; i < 5000; i++) {
            aisShipDetected[i] = false;
        }

        polygonLayers = new LinkedList<RenderableLayer>();
        centers = new LinkedList<Position>();

        dmp = new MeasureTool(GeoWorldWindViewImpl.getWW());
        dmpLayer = new RenderableLayer();
        dmpController = new MeasureToolController();
        dmp.setController(dmpController);
        dmp.setMeasureShapeType(MeasureTool.SHAPE_CIRCLE);
        dmp.setFollowTerrain(true);
        dmpController.setUseRubberBand(true);

        measureTool.setMeasureShapeType(MeasureTool.SHAPE_POLYGON);

        pmt = new MeasureTool(wwd);
        pmtc = new MeasureToolController();
        pmt.setController(pmtc);
        pmt.setFollowTerrain(true);
        pmt.setMeasureShapeType(MeasureTool.SHAPE_PATH);
        // couleur de la trajectoire perso : vert
        pmt.setLineColor(WWUtil.decodeColorRGBA("00FF00FF"));
        pmtc.setFreeHand(true);
        pmtc.setFreeHandMinSpacing(distanceInterval);
        tLayer.setName("Custom target");
        geoViewServices.getLayerManager().insertGeoLayer(GROUP1, GeoLayer.factory.newWorldWindGeoLayer(tLayer));
        layerTreeServices.addGeoLayer(GROUP1, GeoLayer.factory.newWorldWindGeoLayer(tLayer));

        trajectories = new LinkedList<ArrayList<Position>>();
        for (int l = 0; l < 10; l++) {
            trajectories.add(null);
        }
        hasRule = new LinkedList<Boolean>();
        for (int o = 0; o < 10; o++) {
        	hasRule.add(false);
        }

        aisTrackLayer = new RenderableLayer();
        aisTrackLayer.setName("AIS path");
        geoViewServices.getLayerManager().insertGeoLayer(GROUP2, GeoLayer.factory.newWorldWindGeoLayer(aisTrackLayer));
        layerTreeServices.addGeoLayer(GROUP2, GeoLayer.factory.newWorldWindGeoLayer(aisTrackLayer));

        attrs = new BasicShapeAttributes();
        // couleur de la trace : vert
        attrs.setOutlineMaterial(new Material(WWUtil.decodeColorRGBA("00FF00FF")));
        attrs.setOutlineWidth(3);

        aisPath = new ArrayList<Path>();
        for (int n = 0; n < 100000; n++) {
            aisPath.add(null);
        }
        
        //playSound();
        
    }

    @Override
    public void componentStarted() {
    }

    @Override
    public void componentStopped() {
    }

    @Override
    public void on(String... files) {

        if (on == false) {
            on = true;
            
            // Création d'un mouse listener de sélection des polygones
            ma1 = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent pE) {
                	Position aCurrentPosition = wwd.getCurrentPosition();
                    if (aCurrentPosition != null) {
                        //System.out.println("Current Pos= " + aCurrentPosition);
                        polygonSelected = false;
                        for (MeasureTool m : savedMeasureTool) {
                            if (WWMath.isLocationInside(aCurrentPosition, m.getPositions())) {
                                polygonSelected = true;
                                selectedPolygon = savedMeasureTool.indexOf(m) + 1;
                            }
                        }
                        if (polygonSelected) {
                            aisTrackPanel.updateAisPanelStatus("Polygon P" + selectedPolygon + " selected");
                        }
		                else {
		                	aisTrackPanel.updateAisPanelStatus("No polygon selected");
		                }

                    } else {
                        System.err.println("Error with mouse listener clicked : current position is null");
                    }
                }
            };
            
            // Création d'un mouse listener pour prise de position de départ de la translation
            ma2 = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent pE) {
                    Position aCurrentPosition = wwd.getCurrentPosition();
                    if (aCurrentPosition != null) {
                    	start = aCurrentPosition;
                    	
                    } else {
                        System.err.println("Error with mouse listener pressed : current position is null");
                    }
                }
            };
            
            // Création d'un mouse listener pour prise de position de fin de la translation + réalisatin de la translation
            ma3 = new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent pE) {
                    Position aCurrentPosition = wwd.getCurrentPosition();
                    if (aCurrentPosition != null) {
                        end = aCurrentPosition;
                        if (polygonSelected && !(start.equals(end))) {
                        	ArrayList<Position> resu = new ArrayList<Position>();
                        	for (Position pos : savedMeasureTool.get(selectedPolygon-1).getPositions()) {
                        		resu.add(Utils.translate(start, end, pos));
                        	}
                        	savedMeasureTool.get(selectedPolygon-1).setPositions(resu);
                        	centers.set(selectedPolygon-1, Utils.barycenter(savedMeasureTool.get(selectedPolygon-1).getPositions()));
                            savedPolygons.set(selectedPolygon-1, resu);
                        }
                    } else {
                        System.err.println("Error with mouse listener released : current position is null");
                    }
                }
            };
            
            // inhibition du recentrage de la vue
            mma1 = new MouseMotionAdapter()
            {
                public void mouseDragged(MouseEvent mouseEvent)
                {
                    if ((mouseEvent.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0)
                    {
                        if (polygonSelected)
                            mouseEvent.consume();
                    }
                }
            };
            
            wwd.getInputHandler().addMouseListener(ma1);
            wwd.getInputHandler().addMouseListener(ma2);
            wwd.getInputHandler().addMouseListener(ma3);
            wwd.getInputHandler().addMouseMotionListener(mma1);
            
            startTime = new Date();
            lastReceptionDateAtl = startTime;
            lastReceptionDateMed = startTime;
            isTimerOn = false;
            readShips();
            addPanelController();
            
            //feu vert pour l'actualisation des noms depuis la base pour le module AIS
            componentReady = true;

            // souscription aux événements GPS
//            ggaES.subscribe(new GGAEvent() {
//
//                @Override
//                public <T extends NMEA> void notifyNmeaMessageChanged(T d) {
//
//                    GGA data = (GGA) d;
//                    if (on) {
//
//                        watchedShip.setLatitude(data.getLatitude());
//                        watchedShip.setLongitude(data.getLongitude());
//
//                        if (dmpActivated) {
//                            translatePolygon(watchedShip.getLatitude(), watchedShip.getLongitude());
//                        }
//
//                        watchTarget(watchedShip, savedMeasureTool);
//                    }
//
//                }
//            });
        }

        if (!aisServices.isOn()) {
            aisServices.on();
        }

        aisCTEvent.subscribe((AisCreateTargetEvent) (Ship updatedData) -> {
            createTarget(updatedData);
            wwd.redrawNow();
            for (int j = 0; j < savedMeasureTool.size(); j++) {
                watchTargetAis(aisShips, savedMeasureTool.get(j));
            }
            if (dmpActivated) {
                watchTargetDmp(aisShips);
            }
        });
        aisUTEvent.subscribe((AisUpdateTargetEvent) (Ship updatedData) -> {
            if (inSight > coldStart3) {
            	updateOnlineTarget(updatedData);
//            Date t = new Date();
//            if (pShipCreated && !verrou && (int) (t.getTime() - t0.getTime()) % 5 == 0 && etape < path.size() - 1) {
//                verrou = true;
//                Position p1 = new Position(LatLon.fromDegrees(path.get(etape).getLatitude().getDegrees(), path.get(etape).getLongitude().getDegrees()), 0);
//                Position p2 = new Position(LatLon.fromDegrees(path.get(etape + 1).getLatitude().getDegrees(), path.get(etape + 1).getLongitude().getDegrees()), 0);
//                double course = Utils.computeCourse(p1, p2);
//                pShip.setCog(course);
//                etape++;
//                pShip.setLatitude(path.get(etape).getLatitude().getDegrees());
//                pShip.setLongitude(path.get(etape).getLongitude().getDegrees());
//                updatePathTarget(pShip);
//                watchTarget(pShip, savedMeasureTool);
//                if (dmpActivated) {
//                    translatePolygon(pShip.getLatitude(), pShip.getLongitude());
//                }
//
//                Timer timer = new Timer();
//                timer.schedule(new TimerTask() {
//                    public void run() {
//                        verrou = false;
//                    }
//                }, timeInterval);
//            }

            for (int j = 0; j < savedMeasureTool.size(); j++) {
                watchTargetAis(aisShips, savedMeasureTool.get(j));
            }

            if (dmpActivated) {
                watchTargetDmp(aisShips);
            }
            }
        });
        
      //dataServerServices.openGpsd("sinagot.net", 2947);//atlantique
      //dataServerServices.openGpsd("sinagot.net", 2948);//m�diterran�e
      dataServerServices.openGpsd("5.39.78.33", 2947);//atlantique
      dataServerServices.openGpsd("5.39.78.33", 2948);//m�diterran�e
      //dataServerServices.openGpsd("sinagot.net", 2947);
      //dataServerServices.openGpsd("fridu.net", 2947);
      //dataServerServices.openGpsd("http://hd-sf.com", 9009);
      
      lastSaveDate = new Date();
      playSound2();
      
      if (!isTimerOn) {
      	isTimerOn = true;
      	System.out.println("timer on");
      	t = new Timer();
      	t.scheduleAtFixedRate(new MonAction(), 5*1000, 10*1000);
      }
        
    }
    
	private class MonAction extends TimerTask {

		public void run() {
			Date date = new Date();
			
			if (dataServerServices.getAtlDate() != null) {
				lastReceptionDateAtl = dataServerServices.getAtlDate();
				}
			
			if (dataServerServices.getMedDate() != null) {
				lastReceptionDateMed = dataServerServices.getMedDate();
				}

			long delayAtlCalculated = Utils.secondsBetween(date, lastReceptionDateAtl);
			long delayMedCalculated = Utils.secondsBetween(date, lastReceptionDateMed);
			if (delayAtlCalculated > delayAtl) {
				dataServerServices.openGpsd("5.39.78.33", 2947);// atlantique
				System.err.println("restart ATL AIS stream (delay : " + delayAtlCalculated + ")");
				lastReceptionDateAtl.setTime(date.getTime() + 30*1000);
			} else {
				System.out.println(ANSI_GREEN + "ATL stream OK (delay : " + delayAtlCalculated + ")" + ANSI_RESET);
//				if (dataServerServices.getAtlDate() != null) {
//					System.err.println(dateFormatTime.format(dataServerServices.getAtlDate()));
//					}
				// t.cancel();
				// isTimerOn = false;
			}

			if (delayMedCalculated > delayMed) {
				dataServerServices.openGpsd("5.39.78.33", 2948);// m�diterran�e
				System.err.println("restart MED AIS stream (delay : " + delayMedCalculated + ")");
				lastReceptionDateMed.setTime(date.getTime() + 30*1000);
			} else {
				System.out.println(ANSI_GREEN + "MED stream OK (delay : " + delayMedCalculated + ")" + ANSI_RESET);
//				if (dataServerServices.getMedDate() != null) {
//					System.err.println(dateFormatTime.format(dataServerServices.getMedDate()));
//					}
				// t.cancel();
				// isTimerOn = false;
			}
		}
	}

    private void createTarget(Ship target) {
        
        boolean shipExists = false;
        Date date = new Date();
        
//		if (target.getLatitude() > 45.9) {
//			lastReceptionDateAtl = date;// atl
//		} else {
//			lastReceptionDateMed = date;
//		}
		
        inSight++;
        
        aisTrackPanel.updateAisPanelShips(dateFormatTime.format(date), inSight);
        
        //-------------------------------------------------------------
        
//        if (!isTimerOn) {
//        	isTimerOn = true;
//        	System.out.println("timer on");
//        	t = new Timer();
//        	t.scheduleAtFixedRate(new MonAction(), 5*1000, 10*1000);
//        }
        
        //-------------------------------------------------------------
        
//        if (inSight == coldStart4) {
//        	areaHistory = areaHistory2;
//        	while (lastShipArea.size() >= areaHistory) {
//        		String temp2 = lastShipArea.removeFirst();
//        	}
//        	aisTrackPanel.updateAisPanelStatus("Area buffer set to " + areaHistory2);
//        	playSound2();
//        }
//        
//        //------------------------------------------------------------
//        
//        if (inSight == coldStart5) {
//        	areaHistory = areaHistory3;
//        	waitRestartTime = waitRestartTime2;
//        	while (lastShipArea.size() >= areaHistory) {
//        		String temp3 = lastShipArea.removeFirst();
//        	}
//        	aisTrackPanel.updateAisPanelStatus("Area buffer set to " + areaHistory3);
//        	playSound2();
//        }
        
        //------------------------------------------------------------
        
//		if (inSight > coldStart2 && inSight < (coldStart5+250)) {
//			
//			if (lastShipArea.size() >= areaHistory) {
//				String temp = lastShipArea.removeFirst();
//			}
//
//			if (target.getLatitude() < 45.9) {
//				lastShipArea.add("M");// med
//			} else {
//				lastShipArea.add("A");// atl
//				lastAtlDate = date;
//			}
//
//			String listArea = "";
//			for (String s : lastShipArea) {
//				listArea = listArea + s + " ";
//			}
//
//			//aisTrackPanel.updateAisPanelStatus(listArea);
//
//			if (inSight > 100 && inSight % restartFreq == 0 && lastShipArea.size() == areaHistory
//					&& !(lastShipArea.contains("A"))) {
//				dataServerServices.openGpsd("5.39.78.33", 2947);// atlantique
//				aisTrackPanel.updateAisPanelStatus("Altantic ships stream restarted");
//				lastShipArea = new LinkedList<String>();
//			}
//
//			if (inSight > 100 && inSight % restartFreq == 0 && lastShipArea.size() == areaHistory
//					&& !(lastShipArea.contains("M"))) {
//				dataServerServices.openGpsd("5.39.78.33", 2948);// med
//				aisTrackPanel.updateAisPanelStatus("Mediterranean ships stream restarted");
//				lastShipArea = new LinkedList<String>();
//			
//			}
//		}
        
		//-------------------------------------------
		
//        if (inSight == 2000) {
//        	delayAtl = 2*delayAtl;
//        	delayMed = 2*delayMed;
//        	System.err.println("========== ATL delay set to " + delayAtl + " ==========");
//        	System.err.println("========== MED delay set to " + delayMed + " ==========");
//        }
//        
//        if (inSight == 4000) {
//        	delayAtl = 2*delayAtl;
//        	delayMed = 2*delayMed;
//        	System.err.println("========== ATL delay set to " + delayAtl + " ==========");
//        	System.err.println("========== MED delay set to " + delayMed + " ==========");
//        }
//        
//        if (inSight == 6000) {
//        	delayAtl = 3*delayAtl;
//        	delayMed = 2*delayMed;
//        	System.err.println("========== ATL delay set to " + delayAtl + " ==========");
//        	System.err.println("========== MED delay set to " + delayMed + " ==========");
//        }
        
        //-------------------------------------------
        	
        int indice = -1;
        
        for (int i = 0; i < aisShips.size(); i++) {
            if (aisShips.get(i).getMMSI() == target.getMmsi()) {
                shipExists = true;
                indice = i;
                if (inSight > coldStart1) {
                	ArrayList<Position> resu = new ArrayList<Position>();
                	resu.add(new Position(LatLon.fromDegrees(target.getLatitude(), target.getLongitude()), 1));
                	aisPath.set(i, new Path(resu));
                	aisPath.get(i).setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
                	aisPath.get(i).setVisible(true);
                	aisPath.get(i).setExtrude(true);
                	aisPath.get(i).setPathType(AVKey.GREAT_CIRCLE);
                	aisPath.get(i).setAttributes(attrs);
                	}
                break;
            }
        }

		if (shipExists) {
			if (shipMatrix[4][indice] == null || shipMatrix[4][indice].equals(null) || shipMatrix[4][indice].equals("")) {
				updateCreatedTargetDB(target, indice);
			} else {
				try {
					if (inSight > coldStart1 && Utils.minutesBetween(date, Utils.convDate(dateFormatDate.parse(shipMatrix[4][indice]), shipMatrix[5][indice])) > updateInterval) {
//						System.err.println("            ");
//						System.err.println("---------------------------------------------");
//						System.err.println(daysBetween(date, convDate(dateFormatDate.parse(shipMatrix[4][indice]), shipMatrix[5][indice])) + " days");
//						System.err.println(hoursBetween(date, convDate(dateFormatDate.parse(shipMatrix[4][indice]), shipMatrix[5][indice])) + " hours");
//						System.err.println(minutesBetween(date, convDate(dateFormatDate.parse(shipMatrix[4][indice]), shipMatrix[5][indice])) + " minutes");
//						System.err.println(date);
//						System.err.println(shipMatrix[5][indice] + " - " + shipMatrix[4][indice]);
//						System.err.println("---------------------------------------------");
						updateCreatedTargetDB(target, indice);
					}
				} catch (ParseException e) {
					System.err.println("Date parse exception : " + aisShips.get(indice).getMMSI() + " / " + aisShips.get(indice).getName()+ " / " +shipMatrix[4][indice]);
					aisTrackPanel.updateAisPanelStatus("Date parse exception :");
					aisTrackPanel.updateAisPanelStatus(aisShips.get(indice).getMMSI() + "/" + aisShips.get(indice).getName()+ "/" +shipMatrix[4][indice]);
					updateCreatedTargetDB(target, indice);
				}

			}
		} else {

            ArrayList<Position> resu = new ArrayList<Position>();
            resu.add(new Position(LatLon.fromDegrees(target.getLatitude(), target.getLongitude()), 1));
            aisPath.set(aisShips.size(), new Path(resu));
            aisPath.get(aisShips.size()).setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            aisPath.get(aisShips.size()).setVisible(true);
            aisPath.get(aisShips.size()).setExtrude(true);
            aisPath.get(aisShips.size()).setPathType(AVKey.GREAT_CIRCLE);
            aisPath.get(aisShips.size()).setAttributes(attrs);

            nbMmsiReceived++;
            Ship aisShip = new Ship();
            aisShip.setMMSI(target.getMmsi());
            aisShip.setLatitude(target.getLatitude());
            aisShip.setLongitude(target.getLongitude());
            aisShips.add(aisShip);
            lastUpdateDate.add(date);
            aisTrackPanel.updateAisPanelMmsi(dateFormatTime.format(date), inSight, target.getMmsi());
            //aisTrackPanel.updateAisPanelStatus("New ship : " + target.getMmsi());
            
            shipMatrix[0][aisShips.size() - 1] = Integer.toString(aisShip.getMMSI());
            shipMatrix[1][aisShips.size() - 1] = aisShip.getName();
            shipMatrix[2][aisShips.size() - 1] = Double.toString(target.getLatitude());
            shipMatrix[3][aisShips.size() - 1] = Double.toString(target.getLongitude());
            shipMatrix[4][aisShips.size() - 1] = dateFormatDate.format(date);
            shipMatrix[5][aisShips.size() - 1] = dateFormatTime.format(date);

        }
	    
	    if (inSight == coldStart1) {
        	aisTrackPanel.updateAisPanelStatus("Cold start 1 complete ("+coldStart1+" ships)");
        	playSound2();
        }
	    
//	    if (inSight == coldStart2) {
//	    	dataServerServices.openGpsd("5.39.78.33", 2948);//m�diterran�e
//        	aisTrackPanel.updateAisPanelStatus("Cold start 2 complete ("+coldStart2+" ships)");
//        	aisTrackPanel.updateAisPanelStatus("Mediterranean ships stream started");
//        	playSound2();
//        }
        
        if (inSight == coldStart3) {
        	aisTrackPanel.updateAisPanelStatus("Cold start 3 complete ("+coldStart3+" ships)");
        	aisTrackPanel.updateAisPanelStatus("Starting online ship updates");
        	playSound2();
        }
        
        
        if (inSight % 250 == 0) {
			saveData();
        }
        
    }

    private void updateOnlineTarget(Ship target) {

		Date date = new Date();
		
//		long delta = secondsBetween(date, lastAtlDate);
//				
//		if (delta >= waitRestartTime) {
//			dataServerServices.openGpsd("5.39.78.33", 2947);// atlantique
//			aisTrackPanel.updateAisPanelStatus("Altantic ships stream restarted at " + dateFormatTime.format(date));
//			aisTrackPanel.updateAisPanelStatus(delta + " seconds since last atlantic target creation");
//			lastAtlDate = date;
//		}		

		for (int i = 0; i < aisShips.size(); i++) {
			if (aisShips.get(i).getMMSI() == target.getMmsi() && Utils.secondsBetween(date, lastUpdateDate.get(i)) > updateInterval2) {
				//System.out.println(target.getMmsi());
					//aisTrackPanel.updateAisPanelStatus(target.getMmsi() + "-online update: " + secondsBetween(date, lastUpdateDate.get(i)) + " seconds");
					updateMessages++;
					lastUpdateDate.set(i, date);
					
					if (shipMatrix[4][i] == null || shipMatrix[4][i].equals(null) || shipMatrix[4][i].equals("")) {
						updateCreatedTargetDB(target, i);
						onlineDBupdate++;
					} else {
						try {
							if (inSight > coldStart1 && Utils.minutesBetween(date, Utils.convDate(dateFormatDate.parse(shipMatrix[4][i]), shipMatrix[5][i])) > updateInterval) {
								updateCreatedTargetDB(target, i);
								onlineDBupdate++;
							}
						} catch (ParseException e) {
							System.err.println("Date parse exception : " + aisShips.get(i).getMMSI() + " / " + aisShips.get(i).getName()+ " / " +shipMatrix[4][i]);
							aisTrackPanel.updateAisPanelStatus("Date parse exception :");
							aisTrackPanel.updateAisPanelStatus(aisShips.get(i).getMMSI() + "/" + aisShips.get(i).getName()+ "/" +shipMatrix[4][i]);
							updateCreatedTargetDB(target, i);
							onlineDBupdate++;
						}
					}
					
					if (updateMessages % 100 == 0) {
						
						int onlineUpdatedShips = computeUpdatedTarget();
						
						float percent = (onlineUpdatedShips * 100.0f) / inSight;
						float percent2 = (onlineUpdatedShips * 100.0f) / aisShips.size();
						
						if (percent2 > maxRatio) {
							maxRatioRecord = true;
							maxRatio = percent2;
							Date now = new Date();
							dayMaxRatio = dateFormatDate.format(now);
							hourMaxRatio = dateFormatTime.format(now);
							saveMaxLastRun();
							}
						else {
							maxRatioRecord = false;
							}
						
						int length = String.valueOf(onlineUpdatedShips).length();
						String SPACES = Utils.spaces(length+16);//repeatChar('-', length+10);
						
						aisTrackPanel.updateAisPanelStatus(onlineUpdatedShips + " updates / " + inSight + " in sight (ratio : " + String.format ("%.1f", percent) + "%)");
						aisTrackPanel.updateAisPanelStatus(SPACES + " / " + aisShips.size() + " in database (ratio : " + String.format ("%.1f", percent2) + "%)");
						
						if (!maxRatioRecord) {
							aisTrackPanel.updateAisPanelStatus("Max ratio : " + String.format ("%.1f", maxRatio) + "%" + " (" + dayMaxRatio + " - " + hourMaxRatio + ")");
							}
					}

					if (updateMessages % 250 == 0) {
						saveData();
					}
					
					if (aisPath.get(i) != null) {
						if (aisTrackLayer.isEnabled()) {
							aisTrackLayer.removeRenderable(aisPath.get(i));
						}
						ArrayList<Position> resu1 = (ArrayList<Position>) aisPath.get(i).getPositions();
						if (!(resu1.get(resu1.size() - 1).equals(
								new Position(LatLon.fromDegrees(target.getLatitude(), target.getLongitude()), 10)))) {
							resu1.add(
									new Position(LatLon.fromDegrees(target.getLatitude(), target.getLongitude()), 10));
							aisPath.get(i).setPositions(resu1);
						}

						if (aisTrackLayer.isEnabled()) {
							aisTrackLayer.addRenderable(aisPath.get(i));
							wwd.redrawNow();
						}
					} else {
						ArrayList<Position> resu2 = new ArrayList<Position>();
						resu2.add(new Position(LatLon.fromDegrees(target.getLatitude(), target.getLongitude()), 1));
						aisPath.set(i, new Path(resu2));
						aisPath.get(i).setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
						aisPath.get(i).setVisible(true);
						aisPath.get(i).setExtrude(true);
						aisPath.get(i).setPathType(AVKey.GREAT_CIRCLE);
						aisPath.get(i).setAttributes(attrs);
					}
				 
				
//				else {
//					if (secondsBetween(date, lastUpdateDate.get(i)) != 0) {
//						aisTrackPanel.updateAisPanelStatus(
//								target.getMmsi() + " no update (" + secondsBetween(date, lastUpdateDate.get(i)) + " seconds)");
//					}
//				}
				break;
			}
		}
	}
    
    
	private void updateCreatedTargetDB(Ship target, int j) {

		Date date = new Date();
		
//		if (target.getName() != null && !target.getName().equals("")) {
//			aisTrackPanel.updateAisPanelStatus(target.getMMSI() + " - " + target.getName() + " : position update");
//			if (shipMatrix[4][j] != null && !(shipMatrix[4][j].equals(null)) && !(shipMatrix[4][j].equals(""))) {
//				aisTrackPanel.updateAisPanelStatus("Last known position at " + shipMatrix[5][j] + " - " + shipMatrix[4][j]);
//				}
//		} else {
//			aisTrackPanel.updateAisPanelStatus(target.getMMSI() + " : position update");
//			if (shipMatrix[4][j] != null && !(shipMatrix[4][j].equals(null)) && !(shipMatrix[4][j].equals(""))) {
//				aisTrackPanel.updateAisPanelStatus("Last known position at " + shipMatrix[5][j] + " - " + shipMatrix[4][j]);
//			}
//		}

		shipMatrix[2][j] = Double.toString(target.getLatitude());
		shipMatrix[3][j] = Double.toString(target.getLongitude());
		shipMatrix[4][j] = dateFormatDate.format(date);
		shipMatrix[5][j] = dateFormatTime.format(date);

		posUpdates++;
		
		if (posUpdates % 50 == 0) {
			aisTrackPanel.updateAisPanelStatus((posUpdates-onlineDBupdate) + " position updates (on ship creation)");
			aisTrackPanel.updateAisPanelStatus(onlineDBupdate + " position updates (on ship update)");
		}

		if (posUpdates % 100 == 0) {
			saveData();
		}
	}

    @Override
    public void off() {
        // Pb dans la lib C3 ? objet non retiré de la liste
        if (on == true) {
            on = false;

        }
    }

    @Override
    public InstrumentDriver getDriver() {
        return this;
    }

    @Override
    public boolean canOpen(String category) {

        return category.equals(NAME);
    }

    @Override
    public boolean isOn() {
        return on;
    }

    private void watchTarget(Ship target, LinkedList<MeasureTool> list) {

        LatLon pos;
        pos = LatLon.fromDegrees(target.getLatitude(), target.getLongitude());

        for (int i = 0; i < list.size(); i++) {

            if (WWMath.isLocationInside(pos, list.get(i).getPositions()) && list.get(i) != null) {

                if (!shipDetected[i]) {
                    aisTrackPanel.updateAisPanelStatus("MMSI " + target.getMMSI() + " - " + target.getName() + " inside P" + (i + 1));
                    shipDetected[i] = true;
                    reco = new ArrayList<Position>();
                }

                reco.add(new Position(pos, 0));

                if (!(centered.get(i))) {
                    wwd.getView().setEyePosition(new Position(LatLon.fromDegrees(target.getLatitude(), target.getLongitude()), 20000));
                    centered.set(i, true);
                }

                if (!(isTextOn.get(i))) {
                    layerTreeServices.search("Target").setSelected(true);
                    layerTreeServices.search("Path").setSelected(true);
                    layerTreeServices.search("Watch polygons").setSelected(true);
                    textOn(i);
                }

                if (!alarmOn) {
                    MediaPlayer mediaPlayer;
                    javafx.scene.media.Media media;
                    String userDir = System.getProperty("user.dir");
                    userDir = userDir.replace("\\", "/");
                    String url = userDir + "/data/sounds/alarm10.wav";
                    media = new Media("file:///" + url);
                    mediaPlayer = new MediaPlayer(media);
                    mediaPlayer.setAutoPlay(true);
                    //mediaPlayer.setCycleCount(1);
                    alarmOn = true;
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        public void run() {
                            alarmOn = false;
                        }
                    }, 7500);
                }

            }

            if (!(WWMath.isLocationInside(pos, list.get(i).getPositions())) && list.get(i) != null) {
                textOff(i);
            }

            if (!(WWMath.isLocationInside(pos, list.get(i).getPositions())) && list.get(i) != null) {
                if (shipDetected[i]) {
                    aisTrackPanel.updateAisPanelStatus("MMSI " + target.getMMSI() + " - " + target.getName() + " outside P" + (i + 1));
                    shipDetected[i] = false;

                    if (hasRule.get(i)) {
                    	MeasureTool poly = new MeasureTool(wwd);
						MeasureToolController polyc = new MeasureToolController();
						RenderableLayer polyLayer = new RenderableLayer();
						poly.setController(polyc);
						poly.setFollowTerrain(true);
						poly.setMeasureShapeType(MeasureTool.SHAPE_POLYGON);
						// couleur de la forme : rouge
						poly.setLineColor(WWUtil.decodeColorRGBA("FF0000FF"));
						poly.setShowControlPoints(false);
						polyc.setUseRubberBand(true);
						poly.setArmed(true);
						polyc.setArmed(true);
						// création du buffer avec JTS
						poly.setPositions(Utils.createTranslatedBuffer(trajectories.get(i), trajectories.get(i).get(0), reco.get(0), 200));
						polyLayer = poly.getLayer();
						polyLayer.setName("Buffer P" + (i + 1));
						geoViewServices.getLayerManager().insertGeoLayer(GROUP4, GeoLayer.factory.newWorldWindGeoLayer(polyLayer));
						layerTreeServices.addGeoLayer(GROUP4, GeoLayer.factory.newWorldWindGeoLayer(polyLayer));
						wwd.redrawNow();
						poly.setArmed(false);
						polyc.setArmed(false);
						aisTrackPanel.updateAisPanelStatus("Path matches at " + Utils.pathInsideBuffer(reco, poly).getPercent() + "% for MMSI " + target.getMMSI() + " - " + target.getName() + " in P" + (i + 1));
                    }

                    /*
                     * Détection de virages + demi-tours
                     * 
                     */
                    for (int k = 45; k <= 135; k = k + 45) {
                        if (Utils.detectTurn(reco, k).getResult()) {
                            aisTrackPanel.updateAisPanelStatus("Right turn " + k + "° detected at " + Utils.detectTurn(reco, k).getPercent() + "% for MMSI " + target.getMMSI() + " - " + target.getName() + " in P" + (i + 1));
                        }
                        if (Utils.detectTurn(reco, -k).getResult()) {
                            aisTrackPanel.updateAisPanelStatus("Left turn " + k + "° detected at " + Utils.detectTurn(reco, -k).getPercent() + "% for MMSI " + target.getMMSI() + " - " + target.getName() + " in P" + (i + 1));
                        }
                    }

                    if (Utils.detectTurn(reco, 180).getResult()) {
                        aisTrackPanel.updateAisPanelStatus("U-turn detected at " + Utils.detectTurn(reco, 180).getPercent() + "% for MMSI " + target.getMMSI() + " - " + target.getName() + " in P" + (i + 1));
                    }

                    if (Utils.detectStraightLine(reco).getResult()) {
                        aisTrackPanel.updateAisPanelStatus("Straight line detected at " + Utils.detectStraightLine(reco).getPercent() + "% for MMSI " + target.getMMSI() + " - " + target.getName() + " in P" + (i + 1));
                    }
                }
            }

        }
    }

    private void watchTargetAis(ArrayList<Ship> targets, MeasureTool tool) {

        int index = 0;
        boolean putTextOn = false;
        for (Ship target : targets) {

            if (WWMath.isLocationInside(LatLon.fromDegrees(target.getLatitude(), target.getLongitude()), tool.getPositions()) && tool != null) {
                if (!detected[savedMeasureTool.indexOf(tool)][targets.indexOf(target)]) {
                    aisTrackPanel.updateAisPanelStatus("MMSI " + target.getMMSI() + " - " + target.getName() + " inside P" + (savedMeasureTool.indexOf(tool) + 1));
                    detected[savedMeasureTool.indexOf(tool)][targets.indexOf(target)] = true;
                    SamplePositions sample = new SamplePositions(target.getMMSI(), savedMeasureTool.indexOf(tool));
                    recos.add(sample);
                }
                putTextOn = true;
                index = targets.indexOf(target);
            }

            for (SamplePositions p : recos) {
                if (p.getMmsi() == target.getMMSI() && p.getZone() == savedMeasureTool.indexOf(tool)) {
                    ArrayList<Position> resuPos = p.getPositions();
                    resuPos.add(new Position(LatLon.fromDegrees(target.getLatitude(), target.getLongitude()), 0));
                    p.setPositions(resuPos);
                }
            }

            if (!(WWMath.isLocationInside(LatLon.fromDegrees(target.getLatitude(), target.getLongitude()), tool.getPositions())) && tool != null) {
                if (detected[savedMeasureTool.indexOf(tool)][targets.indexOf(target)]) {
                    layerTreeServices.search("Path").setSelected(true);
                    aisTrackPanel.updateAisPanelStatus("MMSI " + target.getMMSI() + " - " + target.getName() + " outside P" + (savedMeasureTool.indexOf(tool) + 1));
                    detected[savedMeasureTool.indexOf(tool)][targets.indexOf(target)] = false;

                    ArrayList<Position> resuPos1 = new ArrayList<Position>();
                    for (SamplePositions p : recos) {
                        if (p.getMmsi() == target.getMMSI() && p.getZone() == savedMeasureTool.indexOf(tool)) {
                            resuPos1 = p.getPositions();
                        }
                    }
                    MeasureTool aisMt = new MeasureTool(wwd);
                    MeasureToolController aisMtc = new MeasureToolController();
                    aisMt.setController(aisMtc);
                    aisMt.setArmed(true);
                    aisMtc.setArmed(true);
                    aisMt.setFollowTerrain(true);
                    aisMt.setMeasureShapeType(MeasureTool.SHAPE_PATH);
                    // couleur de la trajectoire AIS : orange
                    aisMt.setLineColor(WWUtil.decodeColorRGBA("FFA500FF"));
                    aisMt.setPositions(resuPos1);
                    aisMt.setShowControlPoints(false);
                    layerTreeServices.search("Path").setSelected(false);
                    RenderableLayer aisMtLayer = aisMt.getLayer();
                    if (!target.getName().equals("") && !target.getName().equals(null)) {
                        aisMtLayer.setName("P" + (savedMeasureTool.indexOf(tool) + 1) + "-" + target.getName());
                    } else {
                        aisMtLayer.setName("P" + (savedMeasureTool.indexOf(tool) + 1) + "-" + target.getMMSI());
                    }
                    geoViewServices.getLayerManager().insertGeoLayer(GROUP2, GeoLayer.factory.newWorldWindGeoLayer(aisMtLayer));
                    layerTreeServices.addGeoLayer(GROUP2, GeoLayer.factory.newWorldWindGeoLayer(aisMtLayer));
                    aisMt.setArmed(false);
                    aisMtc.setArmed(false);

                    if (hasRule.get(savedMeasureTool.indexOf(tool))) {
                        MeasureTool poly1 = new MeasureTool(wwd);
                        MeasureToolController polyc1 = new MeasureToolController();
                        poly1.setController(polyc1);
                        poly1.setFollowTerrain(true);
                        poly1.setMeasureShapeType(MeasureTool.SHAPE_POLYGON);
                        // couleur de la forme : rouge
                        poly1.setLineColor(WWUtil.decodeColorRGBA("FF0000FF"));
                        poly1.setShowControlPoints(false);
                        polyc1.setUseRubberBand(true);
                        poly1.setArmed(true);
                        polyc1.setArmed(true);
                        // création du buffer avec JTS
                        poly1.setPositions(Utils.createTranslatedBuffer(trajectories.get(savedMeasureTool.indexOf(tool)), trajectories.get(savedMeasureTool.indexOf(tool)).get(0), resuPos1.get(0), 200));
                        RenderableLayer bufferLayer = poly1.getLayer();
                        aisMtLayer.addRenderables(bufferLayer.getRenderables());
                        bufferLayer.removeAllRenderables();
                        wwd.redrawNow();
                        poly1.setArmed(false);
                        polyc1.setArmed(false);
                        aisTrackPanel.updateAisPanelStatus("Path matches at " + Utils.pathInsideBuffer(resuPos1, poly1).getPercent() + "% for MMSI " + target.getMMSI() + " - " + target.getName() + " in P" + (savedMeasureTool.indexOf(tool) + 1));
                    }
                    /*
                     * Détection de virages + demi-tours
                     * 
                     */
                    for (int k = 45; k <= 135; k = k + 45) {
                        if (Utils.detectTurn(resuPos1, k).getResult()) {
                            aisTrackPanel.updateAisPanelStatus("Right turn " + k + "° detected at " + Utils.detectTurn(resuPos1, k).getPercent() + "% for MMSI " + target.getMMSI() + " - " + target.getName() + " in P" + (savedMeasureTool.indexOf(tool) + 1));
                        }
                        if (Utils.detectTurn(resuPos1, -k).getResult()) {
                            aisTrackPanel.updateAisPanelStatus("Left turn " + k + "° detected at " + Utils.detectTurn(resuPos1, -k).getPercent() + "% for MMSI " + target.getMMSI() + " - " + target.getName() + " in P" + (savedMeasureTool.indexOf(tool) + 1));
                        }
                    }

                    if (Utils.detectTurn(resuPos1, 180).getResult()) {
                        aisTrackPanel.updateAisPanelStatus("U-turn detected at " + Utils.detectTurn(resuPos1, 180).getPercent() + "% for MMSI " + target.getMMSI() + " - " + target.getName() + " in P" + (savedMeasureTool.indexOf(tool) + 1));
                    }

                    if (Utils.detectStraightLine(resuPos1).getResult()) {
                        aisTrackPanel.updateAisPanelStatus("Straight line detected at " + Utils.detectStraightLine(resuPos1).getPercent() + "% for MMSI " + target.getMMSI() + " - " + target.getName() + " in P" + (savedMeasureTool.indexOf(tool) + 1));
                    }
                }
            }

        }

        if (!(isTextOnAis.get(savedMeasureTool.indexOf(tool))) && putTextOn) {
            wwd.getView().setEyePosition(new Position(LatLon.fromDegrees(targets.get(index).getLatitude(), targets.get(index).getLongitude()), 20000));
            layerTreeServices.search("Watch polygons").setSelected(false);
            layerTreeServices.search("Watch polygons").setSelected(true);
            textOnAis(savedMeasureTool.indexOf(tool));
        }

        if (!alarmOn && putTextOn) {
            MediaPlayer mediaPlayer;
            javafx.scene.media.Media media;
            String userDir = System.getProperty("user.dir");
            userDir = userDir.replace("\\", "/");
            String url = userDir + "/data/sounds/alarm10.wav";
            media = new Media("file:///" + url);
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setAutoPlay(true);
            //mediaPlayer.setCycleCount(1);
            alarmOn = true;
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {
                    alarmOn = false;
                }
            }, 7500);
        }

        if (isTextOnAis.get(savedMeasureTool.indexOf(tool)) && !putTextOn) {
            textOffAis(savedMeasureTool.indexOf(tool));
        }
    }

    private void textOn(int i) {
        text.set(i, new SurfaceText("P" + (i + 1), centers.get(i)));
        // couleur du texte : violet
        text.get(i).setColor(WWUtil.decodeColorRGBA("FF00FFFF"));
        if (polygonLayers.get(i).isEnabled()) {
            polygonLayers.get(i).addRenderable(text.get(i));
        } else {
            polygonLayers.get(i).setEnabled(true);
            polygonLayers.get(i).addRenderable(text.get(i));
        }
        wwd.redrawNow();
        isTextOn.set(i, true);
    }

    private void textOff(int i) {
        if (text.get(i) != null && polygonLayers.get(i).isEnabled()) {
            polygonLayers.get(i).removeRenderable(text.get(i));
        }
        wwd.redrawNow();
        isTextOn.set(i, false);
    }

    private void textOnAis(int i) {
        textAis.set(i, new SurfaceText("P" + (i + 1), centers.get(i)));
        // couleur du texte : violet
        textAis.get(i).setColor(WWUtil.decodeColorRGBA("FF00FFFF"));
        if (polygonLayers.get(i).isEnabled()) {
            polygonLayers.get(i).addRenderable(textAis.get(i));
        } else {
            polygonLayers.get(i).setEnabled(true);
            polygonLayers.get(i).addRenderable(textAis.get(i));
        }
        wwd.redrawNow();
        isTextOnAis.set(i, true);
    }

    private void textOffAis(int i) {
        if (textAis.get(i) != null && polygonLayers.get(i).isEnabled()) {
            polygonLayers.get(i).removeRenderable(textAis.get(i));
        }
        wwd.redrawNow();
        isTextOnAis.set(i, false);
    }

    private void translatePolygon(double lat, double lon) {
        dmp.setMeasureShapeType(MeasureTool.SHAPE_CIRCLE, new Position(LatLon.fromDegrees(lat, lon), 0), diameter / 2);
        // couleur : blanc transparent a 032/256
        dmp.setFillColor(WWUtil.decodeColorRGBA("FFFFFF20"));
        dmp.setShowControlPoints(false);
    }

    private void watchTargetDmp(ArrayList<Ship> targets) {
        int index = 0;
        boolean detection = false;
        for (Ship target : targets) {

            if (WWMath.isLocationInside(LatLon.fromDegrees(target.getLatitude(), target.getLongitude()), dmp.getPositions()) && dmp != null && !firstDetection) {
                if (!aisShipDetected[targets.indexOf(target)]) {
                    aisTrackPanel.updateAisPanelStatus("MMSI " + target.getMMSI() + " - " + target.getName() + " inside CPA zone");
                    aisShipDetected[targets.indexOf(target)] = true;
                }
                detection = true;
                //couleur de la DMP passe en rouge
                dmp.setLineColor(WWUtil.decodeColorRGBA("FF0000FF"));
                index = targets.indexOf(target);
                layerTreeServices.search("Watch polygons").setSelected(true);
                wwd.getView().setEyePosition(new Position(LatLon.fromDegrees(targets.get(index).getLatitude(), targets.get(index).getLongitude()), 20000));
                firstDetection = true;
            }

            if (WWMath.isLocationInside(LatLon.fromDegrees(target.getLatitude(), target.getLongitude()), dmp.getPositions()) && dmp != null && firstDetection) {
                if (!aisShipDetected[targets.indexOf(target)]) {
                    aisTrackPanel.updateAisPanelStatus("MMSI " + target.getMMSI() + " - " + target.getName() + " inside CPA zone");
                    aisShipDetected[targets.indexOf(target)] = true;
                }
                detection = true;
                //couleur de la DMP passe en rouge
                dmp.setLineColor(WWUtil.decodeColorRGBA("FF0000FF"));
                index = targets.indexOf(target);
                layerTreeServices.search("Watch polygons").setSelected(true);
            }

            if (!(WWMath.isLocationInside(LatLon.fromDegrees(target.getLatitude(), target.getLongitude()), dmp.getPositions())) && dmp != null && firstDetection && aisShipDetected[targets.indexOf(target)]) {
                aisShipDetected[targets.indexOf(target)] = false;
                aisTrackPanel.updateAisPanelStatus("MMSI " + target.getMMSI() + " - " + target.getName() + " outside CPA zone");
            }

        }

        if (!alarmOn && detection) {
            MediaPlayer mediaPlayer;
            javafx.scene.media.Media media;
            String userDir = System.getProperty("user.dir");
            userDir = userDir.replace("\\", "/");
            String url = userDir + "/data/sounds/alarm10.wav";
            media = new Media("file:///" + url);
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setAutoPlay(true);
            //mediaPlayer.setCycleCount(1);
            alarmOn = true;
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {
                    alarmOn = false;
                }
            }, 7500);
        }

        if (!detection) {
            //couleur de la DMP repasse en vert
            dmp.setLineColor(WWUtil.decodeColorRGBA("00FF00FF"));
            firstDetection = false;
        }

    }

    @Override
    public void drawerOn() {
        if (nbPolygon == 1) {
            polygonLayer = measureTool.getLayer();
            polygonLayer.setName("Polygon#1");
            geoViewServices.getLayerManager().insertGeoLayer(GROUP, GeoLayer.factory.newWorldWindGeoLayer(polygonLayer));
            layerTreeServices.addGeoLayer(GROUP, GeoLayer.factory.newWorldWindGeoLayer(polygonLayer));
        }

        if (nbPolygon <= nbPolygonMax) {
            if (!drawerActivated) {
                measureTool.setArmed(true);
                controller.setArmed(true);
                aisTrackPanel.updateAisPanelStatus("Drawer ready");
                drawerActivated = true;
            	wwd.getInputHandler().removeMouseListener(ma2);
            	wwd.getInputHandler().removeMouseListener(ma3);
            } else {
                measureTool.setArmed(false);
                controller.setArmed(false);
                aisTrackPanel.updateAisPanelStatus("Drawer deactivated");
                drawerActivated = false;
            	wwd.getInputHandler().addMouseListener(ma2);
            	wwd.getInputHandler().addMouseListener(ma3);
            }
        } else {
            aisTrackPanel.updateAisPanelStatus("Maximum number of polygons reached");
        }
    }

    @Override
    public void createPath() {
        if (!pathActivated && !pathLoaded) {
            pmt.setArmed(true);
            pmtc.setArmed(true);
            aisTrackPanel.updateAisPanelStatus("Custom path ready to be drawn");
        	wwd.getInputHandler().removeMouseListener(ma2);
        	wwd.getInputHandler().removeMouseListener(ma3);
        } else {
            aisTrackPanel.updateAisPanelStatus("Only one path can be created at the same time");
        }
    }

    @Override
    public void activatePath() {
        // masquage des points de contrôle de la trajectoire
        pmt.setShowControlPoints(false);
        path = (ArrayList<Position>) pmt.getPositions();
        pLayer = pmt.getLayer();
        pLayer.setName("Custom path");
        geoViewServices.getLayerManager().insertGeoLayer(GROUP2, GeoLayer.factory.newWorldWindGeoLayer(pLayer));
        layerTreeServices.addGeoLayer(GROUP2, GeoLayer.factory.newWorldWindGeoLayer(pLayer));
        pmt.setArmed(false);
        pmtc.setArmed(false);
        pShip = new Ship();
        pShip.setMMSI(999999999);
        pShip.setName("PLASTRON");
        pShip.setLatitude(path.get(etape).getLatitude().getDegrees());
        pShip.setLongitude(path.get(etape).getLongitude().getDegrees());
        createPathTarget(pShip);
        pathActivated = true;
        layerTreeServices.search("Target").setSelected(true);
        //layerTreeServices.search("Path").setSelected(true);
        aisTrackPanel.updateAisPanelStatus("Custom path activated");
    	wwd.getInputHandler().addMouseListener(ma2);
    	wwd.getInputHandler().addMouseListener(ma3);
    }

    private void createPathTarget(Ship target) {
        gShip = new GShip(target);
        //gShip.update(0);
        target.setShipType(80);
        double sog = (distanceInterval * 1000 * 3600) / (1852 * timeInterval);
        target.setSog(sog);
        if (target.getLatitude() != 0.0 && target.getLongitude() != 0.0) {
            Renderable[] renderables = gShip.getRenderables();
            for (Renderable r : renderables) {
                tLayer.addRenderable(r);
            }
            wwd.redrawNow();
        }
        pShipCreated = true;
        t0 = new Date();
        layerTreeServices.search("Path").setSelected(false);
        gShip.setShip(target);
    }

    private void updatePathTarget(Ship target) {
        gShip.update();
        wwd.redrawNow();
    }

    @Override
    public void savePolygon() {
        centers.add(Utils.barycenter(measureTool.getPositions()));
		//System.out.println("Polygon center position is lat : " + centers.getLast().getLatitude().getDegrees() + " lon : " + centers.getLast().getLongitude().getDegrees() + " alt : " + centers.getLast().getAltitude() +"\n");							

        ArrayList<Position> list = new ArrayList<Position>();
        list = (ArrayList<Position>) measureTool.getPositions();
        savedPolygons.add(list);

        measureTool.setArmed(false);
        controller.setArmed(false);
        drawerActivated = false;
        aisTrackPanel.updateAisPanelStatus("Drawer deactivated");
        savedMeasureTool.add(measureTool);

        if (nbPolygon < nbPolygonMax) {
            nbPolygon++;
            controller = new MeasureToolController();
            measureTool = new MeasureTool(wwd);
            measureTool.setController(controller);
            measureTool.setMeasureShapeType(MeasureTool.SHAPE_POLYGON);
            measureTool.setFollowTerrain(true);
            controller.setUseRubberBand(true);
            polygonLayer = measureTool.getLayer();
            polygonLayers.add(polygonLayer);
            polygonLayer.setName("Polygon#" + nbPolygon);
            geoViewServices.getLayerManager().insertGeoLayer(GROUP, GeoLayer.factory.newWorldWindGeoLayer(polygonLayer));
            layerTreeServices.addGeoLayer(GROUP, GeoLayer.factory.newWorldWindGeoLayer(polygonLayer));
            aisTrackPanel.updateAisPanelStatus("New polygon (polygon#" + nbPolygon + ") ready to be drawn");
        	wwd.getInputHandler().addMouseListener(ma2);
        	wwd.getInputHandler().addMouseListener(ma3);
        } else {
            aisTrackPanel.updateAisPanelStatus("Maximum number of polygons reached");
            nbPolygon = nbPolygonMax + 1;
        }
    }

    @Override
    public void saveAllPolygons() {
		// sauvegarde des polygons
        //declare output stream
        BufferedWriter writer = null;

        try {
            // open file for writing
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("data/saved/savedPolygons.csv"), "utf-8"));
            //Put data - if needed put the loop around more than orw of records
            for (int j = 0; j < savedPolygons.size(); j++) {
                int size = savedPolygons.get(j).size();
                for (int i = 0; i < size - 1; i++) {
                    writer.write(savedPolygons.get(j).get(i).getLatitude().getDegrees() + ";" + savedPolygons.get(j).get(i).getLongitude().getDegrees() + ";");
                }
                writer.write(savedPolygons.get(j).get(size - 1).getLatitude().getDegrees() + ";" + savedPolygons.get(j).get(size - 1).getLongitude().getDegrees() + "\r\n");
            }

        } catch (IOException ex) {
            // report
        } finally {
            //close file
            try {
                writer.close();
            } catch (Exception ex) {
            }
            aisTrackPanel.updateAisPanelStatus(savedPolygons.size() + " polygons saved successfully");
        }

    }

    @Override
    public void loadPolygons() {
        if (nbPolygon != 1) {
            aisTrackPanel.updateAisPanelStatus("Not possible to load while Polygons have already been created");
        } else {
            // chargement des polygons sauvegardés
            int i = 0;
            String csvFile = "data/saved/savedPolygons.csv";
            BufferedReader br = null;
            String line = "";
            String cvsSplitBy = ";";
            wwd.redrawNow();
            savedMeasureTool = new LinkedList<MeasureTool>();
            centers = new LinkedList<Position>();
            savedPolygons = new LinkedList<ArrayList<Position>>();
            polygonLayers = new LinkedList<RenderableLayer>();

            try {

                br = new BufferedReader(new FileReader(csvFile));
                while ((line = br.readLine()) != null) {
                    ArrayList<Position> list = new ArrayList<Position>();
                    // use separator
                    String[] positions = line.split(cvsSplitBy);
                    for (int j = 0; j < positions.length - 1; j = j + 2) {
                        Position pos = new Position(LatLon.fromDegrees(Double.parseDouble(positions[j]), Double.parseDouble(positions[j + 1])), 0);
                        list.add(pos);
                    }
                    MeasureTool mt = new MeasureTool(wwd);
                    MeasureToolController mtc = new MeasureToolController();
                    mt.setController(mtc);
                    mt.setArmed(true);
                    mtc.setArmed(true);
                    mtc.setUseRubberBand(true);
                    mt.setPositions(list);
                    mt.setFollowTerrain(true);
                    RenderableLayer layer = mt.getLayer();
                    polygonLayers.add(layer);
                    layer.setName("Polygon#" + nbPolygon);
                    geoViewServices.getLayerManager().insertGeoLayer(GROUP, GeoLayer.factory.newWorldWindGeoLayer(layer));
                    layerTreeServices.addGeoLayer(GROUP, GeoLayer.factory.newWorldWindGeoLayer(layer));
                    wwd.redrawNow();
                    savedMeasureTool.add(mt);
                    centers.add(Utils.barycenter(mt.getPositions()));
                    //System.out.println("Polygon center position is lat : " + centers.getLast().getLatitude().getDegrees() + " lon : " + centers.getLast().getLongitude().getDegrees() + " alt : " + centers.getLast().getAltitude() +"\n");
                    ArrayList<Position> list1 = new ArrayList<Position>();
                    list1 = (ArrayList<Position>) mt.getPositions();
                    savedPolygons.add(list1);
                    mt.setArmed(false);
                    mtc.setArmed(false);
                    aisTrackPanel.updateAisPanelStatus("New polygon (polygon#" + nbPolygon + ") loaded");
                    nbPolygon++;
                    i++;
                }
                controller = new MeasureToolController();
                measureTool = new MeasureTool(wwd);
                measureTool.setController(controller);
                measureTool.setMeasureShapeType(MeasureTool.SHAPE_POLYGON);
                measureTool.setFollowTerrain(true);
                controller.setUseRubberBand(true);
                polygonLayer = new RenderableLayer();
                polygonLayer = measureTool.getLayer();
                polygonLayers.add(polygonLayer);
                polygonLayer.setName("Polygon#" + nbPolygon);
                geoViewServices.getLayerManager().insertGeoLayer(GROUP, GeoLayer.factory.newWorldWindGeoLayer(polygonLayer));
                layerTreeServices.addGeoLayer(GROUP, GeoLayer.factory.newWorldWindGeoLayer(polygonLayer));

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            aisTrackPanel.updateAisPanelStatus(i + " polygons loaded successfully");
        }
    }

    @Override
    public void savePath() {
        if (pathActivated) {
			// sauvegarde de la trajectoire path tracée
            //declare output stream
            BufferedWriter writer = null;

            try {
                // open file for writing
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("data/saved/savedPath.csv"), "utf-8"));
                //Put data - if needed put the loop around more than orw of records
                for (int k = 0; k < path.size() - 1; k++) {
                    writer.write(path.get(k).getLatitude().getDegrees() + ";" + path.get(k).getLongitude().getDegrees() + ";");
                }
                writer.write(path.get(path.size() - 1).getLatitude().getDegrees() + ";" + path.get(path.size() - 1).getLongitude().getDegrees() + "\r\n");
            } catch (IOException ex) {
                // report
            } finally {
                //close file
                try {
                    writer.close();
                } catch (Exception ex) {
                }
                aisTrackPanel.updateAisPanelStatus("Custom path saved successfully");
            }

        } else {
            aisTrackPanel.updateAisPanelStatus("Please activate path before saving it");
        }

    }

    @Override
    public void loadPath() {
        if (!pathActivated) {
            // chargement de la trajectoire path sauvegardée
            String csvFile = "data/saved/savedPath.csv";
            BufferedReader br = null;
            String line = "";
            String cvsSplitBy = ";";

            try {

                br = new BufferedReader(new FileReader(csvFile));
                while ((line = br.readLine()) != null) {
                    // use separator
                    String[] positions = line.split(cvsSplitBy);
                    for (int j = 0; j < positions.length - 1; j = j + 2) {
                        Position pos = new Position(LatLon.fromDegrees(
                                Double.parseDouble(positions[j]),
                                Double.parseDouble(positions[j + 1])), 0);
                        path.add(pos);
                    }
                    pmt.setArmed(true);
                    pmtc.setArmed(true);
                    pmt.setPositions(path);
                    pmt.setFollowTerrain(true);
                    pLayer = pmt.getLayer();
                    pmt.setArmed(false);
                    pmtc.setArmed(false);
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            pathLoaded = true;
            aisTrackPanel.updateAisPanelStatus("Path loaded successfully");
        } else {
            aisTrackPanel.updateAisPanelStatus("Only one path can be created at the same time");
        }
    }

    @Override
    public void polyShapeOn() {
        measureTool.setMeasureShapeType(MeasureTool.SHAPE_POLYGON);
        aisTrackPanel.updateAisPanelStatus("Polygon shape activated");
        
        dataServerServices.openGpsd("5.39.78.33", 2947);//atlantique
    }

    @Override
    public void ellipseShapeOn() {
        measureTool.setMeasureShapeType(MeasureTool.SHAPE_ELLIPSE);
        aisTrackPanel.updateAisPanelStatus("Ellipse shape activated");
        
		saveData();
    }

    @Override
    public void circleShapeOn() {
        measureTool.setMeasureShapeType(MeasureTool.SHAPE_CIRCLE);
        aisTrackPanel.updateAisPanelStatus("Circle shape activated");
    }

    @Override
    public void quadShapeOn() {
        measureTool.setMeasureShapeType(MeasureTool.SHAPE_QUAD);
        aisTrackPanel.updateAisPanelStatus("Quad shape activated");
    }

    @Override
    public void freeHandOn() {
        if (!freeHandActivated) {
            // Altitude en mètres
            double altitude = wwd.getView().getEyePosition().getAltitude();
            double spacing = (double) ((200 * altitude) / (15000));
            controller.setFreeHand(true);
            controller.setFreeHandMinSpacing(spacing);
            measureTool.setMeasureShapeType(MeasureTool.SHAPE_POLYGON);
            freeHandActivated = true;
            aisTrackPanel.updateAisPanelStatus("Free hand mode activated");
            if (altitude != savedAltitude) {
                savedAltitude = altitude;
                aisTrackPanel.updateAisPanelStatus("Eye altitude : " + ((double) (Math.round(10 * altitude)) / 10) + " meters");
                aisTrackPanel.updateAisPanelStatus("Points spacing : " + ((double) (Math.round(10 * spacing)) / 10) + " meters");
            }
        } else {
            controller.setFreeHand(false);
            freeHandActivated = false;
            aisTrackPanel.updateAisPanelStatus("Free hand mode deactivated");
        }
    }

    @Override
    public void createCpaZone() {
        dmp.setArmed(true);
        dmpController.setArmed(true);
        aisTrackPanel.updateAisPanelStatus("CPA zone ready to be drawn");
    }

    @Override
    public void createCpaZone(double yards) {
        if (!dmpActivated) {
            dmpActivated = true;
            //couleur de la DMP : vert
            dmp.setLineColor(WWUtil.decodeColorRGBA("00FF00FF"));
            //rayon statique de 500 yards
            diameter = 2 * yards * 0.9144;
            dmpLayer = dmp.getLayer();
            dmpLayer.setEnabled(true);
            dmpLayer.setName("CPA zone " + Math.round(yards) + " yards");
            geoViewServices.getLayerManager().insertGeoLayer(GROUP, GeoLayer.factory.newWorldWindGeoLayer(dmpLayer));
            layerTreeServices.addGeoLayer(GROUP, GeoLayer.factory.newWorldWindGeoLayer(dmpLayer));
            aisTrackPanel.updateAisPanelStatus("CPA zone " + Math.round(yards) + " yards activated");
        }
    }

    @Override
    public void activateCpaZone() {
        if (!dmpActivated) {
            dmpActivated = true;
            //couleur de la DMP : vert
            dmp.setLineColor(WWUtil.decodeColorRGBA("00FF00FF"));
            //diamètre dynamique (commenter/décommenter la ligne suivante)
            diameter = dmp.getWidth();
			//ou : rayon statique de x yards (commenter/décommenter les 2 lignes suivantes)
            //double x = 1000;
            //diameter = 2*x*0.9144;
            dmpLayer = dmp.getLayer();
            dmpLayer.setEnabled(true);
            dmpLayer.setName("CPA zone");
            geoViewServices.getLayerManager().insertGeoLayer(GROUP, GeoLayer.factory.newWorldWindGeoLayer(dmpLayer));
            layerTreeServices.addGeoLayer(GROUP, GeoLayer.factory.newWorldWindGeoLayer(dmpLayer));
            dmp.setArmed(false);
            dmpController.setArmed(false);
            aisTrackPanel.updateAisPanelStatus("CPA zone activated - radius : " + Math.round(diameter / 2) + " meters");
        }
    }
    
    public void playSound() {
    	try{
    		String userDir = System.getProperty("user.dir");
            userDir = userDir.replace("\\", "/");
            String url = userDir + "/data/sounds/pling.wav";
            AudioInputStream ais = AudioSystem.getAudioInputStream(new File(url));
            Clip test = AudioSystem.getClip();  

            test.open(ais);
            test.start();

            while (!test.isRunning())
                Thread.sleep(10);
            while (test.isRunning())
                Thread.sleep(10);

            test.close();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
    public void playSound2() {
    	try{
    		String userDir = System.getProperty("user.dir");
            userDir = userDir.replace("\\", "/");
            String url = userDir + "/data/sounds/beep-07.wav";
            AudioInputStream ais = AudioSystem.getAudioInputStream(new File(url));
            Clip test = AudioSystem.getClip();  

            test.open(ais);
            test.start();

            while (!test.isRunning())
                Thread.sleep(10);
            while (test.isRunning())
                Thread.sleep(10);

            test.close();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
    public void newNameAis5(Ship target) {
    	
    	Date date = new Date();
    	boolean shipExistsAis5 = false;
    	nbNamesMessages++;
        
    	for (int i = 0; i < aisShips.size(); i++) {
            if (aisShips.get(i).getMMSI() == target.getMMSI()) {
            	shipExistsAis5 = true;

                	Ship resu = new Ship();
                	resu.setLatitude(target.getLatitude());
                	resu.setLongitude(target.getLongitude());
                	resu.setMMSI(target.getMMSI());
                		
                	if (!target.getName().equals(aisShips.get(i).getName())) {
                		resu.setName(target.getName());
                		aisShips.set(i, resu);
                        nbNamesReceived++;
                       
                        aisTrackPanel.updateAisPanelName(dateFormatTime.format(date), inSight, (target.getName() + " new name - (AIS5)"));
                        playSound();
                        
                        if (nbNamesReceived % 10 == 0) {
                        	aisTrackPanel.updateAisPanelStatus(nbNamesReceived + " new name(s) received");
                        	}
                        //aisTrackPanel.updateAisPanelStatus("New name " + target.getName());
                        
                        if (nbNamesReceived % 20 == 0) {
                        	saveData();
                        }

                	} else {
                		//aisTrackPanel.updateAisPanelName(dateFormatTime.format(date), inSight, (target.getName() + " name already in database (AIS5)"));
                		//ou bien : aisTrackPanel.updateAisPanelStatus(target.getName() + " name already in database");
                		}
                
                shipMatrix[0][i] = Integer.toString(resu.getMMSI());
                shipMatrix[1][i] = aisShips.get(i).getName();
                if (target.getLatitude()!=0 && target.getLongitude()!=0) {
                	shipMatrix[2][i] = Double.toString(resu.getLatitude());
                	shipMatrix[3][i] = Double.toString(resu.getLongitude());
                    shipMatrix[4][i] = dateFormatDate.format(date);
                    shipMatrix[5][i] = dateFormatTime.format(date);
                    }

                break;
            }
        }
    	if (!shipExistsAis5) {

            nbMmsiReceived++;
            Ship aisShip = new Ship();
            aisShip.setMMSI(target.getMMSI());
            aisShip.setLatitude(target.getLatitude());
            aisShip.setLongitude(target.getLongitude());
            if (!target.getName().equals("") && target.getName() != null) {
            	aisShip.setName(target.getName());
                nbNamesReceived++;
            	aisTrackPanel.updateAisPanelName(dateFormatTime.format(date), inSight, (target.getName() + " new ship & name - (AIS5)"));
            	playSound();
            	
                if (nbNamesReceived % 10 == 0) {
                	aisTrackPanel.updateAisPanelStatus(nbNamesReceived + " new name(s) received");
                	}
            	aisTrackPanel.updateAisPanelStatus("New ship " + target.getMmsi() + "/" + target.getName());
                
                if (nbNamesReceived % 10 == 0) {
					saveData();
                }
            }
            aisShips.add(aisShip);
            aisTrackPanel.updateAisPanelMmsi(dateFormatTime.format(date), inSight, target.getMmsi());
            
            shipMatrix[0][aisShips.size() - 1] = Integer.toString(aisShip.getMMSI());
            shipMatrix[1][aisShips.size() - 1] = aisShip.getName();
            
            if (target.getLatitude()!=0 && target.getLongitude()!=0) {
            	shipMatrix[2][aisShips.size() - 1] = Double.toString(target.getLatitude());
            	shipMatrix[3][aisShips.size() - 1] = Double.toString(target.getLongitude());
                shipMatrix[4][aisShips.size() - 1] = dateFormatDate.format(date);
                shipMatrix[5][aisShips.size() - 1] = dateFormatTime.format(date);
                }
    	}
    }

    private void saveShips() {
        //declare output stream
        BufferedWriter writer = null;
        //define headers
        String[] CSVHeaders = {"MMSI", "Name", "Last known latitude", "Last known longitude", "Date", "Time"};

        try {
            // open file for writing
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("data/saved/savedAisShips.csv"), "utf-8"));
            // put headers in CSV file
            for (int i = 0; i < CSVHeaders.length - 1; i++) {
                writer.write(CSVHeaders[i] + ";");
            }
            writer.write(CSVHeaders[CSVHeaders.length - 1] + "\n");

            //Put data - if needed put the loop around more than orw of records
            for (int j = 0; j < aisShips.size(); j++) {
                for (int i = 0; i < 6; i++) {
                    writer.write(shipMatrix[i][j] + ";");
                }
                writer.write("\r\n");
            }
            
            computeMaxTarget();

        } catch (IOException ex) {
            // report
        } finally {
            //close file
            try {
                writer.close();
            } catch (Exception ex) {
            }
        }
    }

    private void readShips() {
        boolean firstLine = true;
        int i = 0;
        String csvFile = "data/saved/savedAisShips.csv";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ";";

        try {

            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {

                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                // use separator
                String[] ship = line.split(cvsSplitBy);

                shipMatrix[0][i] = ship[0];
                shipMatrix[1][i] = ship[1];
                shipMatrix[2][i] = ship[2];
                shipMatrix[3][i] = ship[3];
                shipMatrix[4][i] = ship[4];
                shipMatrix[5][i] = ship[5];

    			//System.out.println("Ship [MMSI= " + ship[0] + " , name=" + ship[1] + " , lat=" + ship[2] + " , lon=" + ship[3] + " , date=" + ship[4] + " , time=" + ship[5] + " , i=" + i + "]");
                Ship s = new Ship();
                s.setMMSI(Integer.parseInt(ship[0]));
                s.setName(ship[1]);
                if (!ship[1].equals("")) {
                    nbNamesDB++;
                }
    			//on ne charge pas les dernières positions connues des bateaux car ceux-ci ne sont peut-être plus actifs, les positions seront mises à jour à la réception de nouveaux signaux AIS (pour les bateaux actifs)
                //s.setLatitude(Double.parseDouble(ship[2]));
                //s.setLongitude(Double.parseDouble(ship[3]));
                aisShips.add(s);
                i++;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
		Date echantillon = new Date();
		echantillon.setTime(0);
        //System.out.println(echantillon);
        
		for (Ship s : aisShips) {
			int indice = 0;
			if (shipMatrix[4][indice] != null && shipMatrix[5][indice] != null) {
				try {
					lastUpdateDate.add(Utils.convDate(dateFormatDate.parse(shipMatrix[4][indice]), shipMatrix[5][indice]));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					System.err.println("Date parse exception");
					lastUpdateDate.add(echantillon);
				}
			} else {
				lastUpdateDate.add(echantillon);
			}
			indice++;
		}
        loadMaxTarget();
        //aisTrackPanel.updateAisPanelStatus("Max target : " + maxTarget);
    }

    private void addPanelController() {
        Platform.runLater(() -> {
            Date date = new Date();
            aisTrackPanel = new TrackPanel(KeyCode.T, KeyCombination.CONTROL_DOWN);
// parametres ubuntu ATOL
            //aisTrackPanel.setTranslateX(750);
            //aisTrackPanel.setTranslateY(150);
// parametres windows perso Vivien		  
            aisTrackPanel.setTranslateX(50);
            aisTrackPanel.setTranslateY(-50);
            guiAgentServices.getScene().addEventFilter(KeyEvent.KEY_RELEASED, aisTrackPanel);
            guiAgentServices.getRoot().getChildren().add(aisTrackPanel);
            aisTrackPanel.setScale(1.0);
            aisTrackPanel.setVisible(true);

            aisTrackPanel.updateAisPanelCount(dateFormatTime.format(date), 0, aisShips.size(), nbNamesDB);
            aisTrackPanel.updateAisPanelStatus("Reading file done (" + aisShips.size() + " ships / " + nbNamesDB + " names)");
            aisTrackPanel.updateAisPanelStatus("In sight record : " + maxTarget + " (" + dayMaxTarget + " - " + hourMaxTarget + ")");
            aisTrackPanel.updateAisPanelStatus("Last run : " + maxLastRun + " (" + dayMaxLastRun + " - " + hourMaxLastRun + ")");
            aisTrackPanel.updateAisPanelStatus("Max ratio : " + String.format ("%.1f", maxRatio) + "%" + " (" + dayMaxRatio + " - " + hourMaxRatio + ")");
        });

    }

    @Override
    public void createRule() {
        if (polygonSelected) {
            if (trajectories.get(selectedPolygon - 1) == null) {
            	wwd.getInputHandler().removeMouseListener(ma2);
            	wwd.getInputHandler().removeMouseListener(ma3);
            	rmt = new MeasureTool(wwd);
                rmtc = new MeasureToolController();
                rmt.setController(rmtc);
                rmt.setFollowTerrain(true);
                rmt.setMeasureShapeType(MeasureTool.SHAPE_PATH);
                // couleur de la règle : bleu
                rmt.setLineColor(WWUtil.decodeColorRGBA("0000FFFF"));
                rmtc.setFreeHand(true);
                rmtc.setFreeHandMinSpacing(distanceInterval);
                rmt.setArmed(true);
                rmtc.setArmed(true);
                aisTrackPanel.updateAisPanelStatus("New rule ready to be drawn");
                ruleCreated = true;
            } else {
                aisTrackPanel.updateAisPanelStatus("Only one rule can be associated to an area");
            }
        } else {
            aisTrackPanel.updateAisPanelStatus("You must select a Polygon first");
        }
    }

    @Override
    public void activateRule() {
        if (ruleCreated) {
            //rmt.setShowControlPoints(false);
            trajectories.set((selectedPolygon - 1), ((ArrayList<Position>) rmt.getPositions()));
            hasRule.set((selectedPolygon - 1), true);
            rLayer = new RenderableLayer();
            rLayer = rmt.getLayer();
            wwd.redrawNow();
            rLayer.setName("P" + selectedPolygon + " rule");
            geoViewServices.getLayerManager().insertGeoLayer(GROUP3, GeoLayer.factory.newWorldWindGeoLayer(rLayer));
            layerTreeServices.addGeoLayer(GROUP3, GeoLayer.factory.newWorldWindGeoLayer(rLayer));
            rmt.setArmed(false);
            rmtc.setArmed(false);
            aisTrackPanel.updateAisPanelStatus("New rule associated with polygon P" + selectedPolygon);
            ruleCreated = false;
            polygonSelected = false;
        	wwd.getInputHandler().addMouseListener(ma2);
        	wwd.getInputHandler().addMouseListener(ma3);
        } else {
            aisTrackPanel.updateAisPanelStatus("Please create a rule first");
        }
    }

    public ArrayList<Ship> getSavedAisShips() {
        return aisShips;
    }

    public TrackPanel getPanel() {
        return aisTrackPanel;
    }
    
    public boolean getComponentReady() {
    	return componentReady;
    }
    
    public int getInSight() {
    	return inSight;
    }
    
	public void retrieveShipName(Ship target) {
		for (Ship s : aisShips) {
			if (s.getMMSI() == target.getMMSI() && target.getName() == null) {
				if (!(s.getName().equals("")) && !(s.getName().equals(null))) {
					target.setShipName(s.getName());
					nbNamesRetrieved++;
					//aisTrackPanel.updateAisPanelStatus(s.getName() + " retrieved from database");
					if (nbNamesRetrieved % 250 == 0) {
						aisTrackPanel.updateAisPanelStatus(nbNamesRetrieved + " names retrieved from database");
					}
				}
				break;
			}
		}
	}
	
	public void loadMaxTarget() {
		// chargement du maxTarget sauvegard�
		String csvFile = "data/saved/savedMaxTarget.csv";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ";";
		int resu = 0;

		try {

			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
				// use separator
				String[] numbers = line.split(cvsSplitBy);
				// double maxi = Double.parseDouble(numbers[0]);
				// aisTrackPanel.updateAisPanelStatus("Max target received : " +
				// maxi);
				// for (int j = 0; j <= numbers.length - 1; j = j + 1) {
				// System.out.println(numbers[j]);
				// System.out.println(Integer.parseInt(numbers[j]));
				// }
				try {
					maxTarget = Integer.parseInt(numbers[0]);
					dayMaxTarget = numbers[1];
					hourMaxTarget = numbers[2];
					maxLastRun = Integer.parseInt(numbers[3]);
					maxLastRunHisto = maxLastRun;
					dayMaxLastRun = numbers[4];
					hourMaxLastRun = numbers[5];
					maxRatio = Float.parseFloat(numbers[6]);
					dayMaxRatio = numbers[7];
					hourMaxRatio = numbers[8];
				} catch (Exception e) {
					System.out.println("Please enter a number not string");
				}

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	public void saveMaxTarget() {
			// sauvegarde du maxTarget
            //declare output stream
            BufferedWriter writer = null;
            Date date = new Date();
            String day = dateFormatDate.format(date);
            String hour = dateFormatTime.format(date);

            try {
                // open file for writing
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("data/saved/savedMaxTarget.csv"), "utf-8"));
                //Put data - if needed put the loop around more than orw of records
                writer.write(maxTarget + ";" + day + ";" + hour + ";" + maxTarget + ";" + day + ";" + hour + ";" + maxRatio + ";" + dayMaxRatio + ";" + hourMaxRatio + ";" + "\r\n");
            } catch (IOException ex) {
                // report
            } finally {
                //close file
                try {
                    writer.close();
                } catch (Exception ex) {
                }
            }
    }
	
	public void saveMaxLastRun() {
		// sauvegarde du maxTarget
        //declare output stream
        BufferedWriter writer = null;

        try {
            // open file for writing
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("data/saved/savedMaxTarget.csv"), "utf-8"));
            //Put data - if needed put the loop around more than orw of records
            writer.write(maxTarget + ";" + dayMaxTarget + ";" + hourMaxTarget + ";" + maxLastRun + ";" + dayMaxLastRun + ";" + hourMaxLastRun + ";" + maxRatio + ";" + dayMaxRatio + ";" + hourMaxRatio + ";" + "\r\n");
        } catch (IOException ex) {
            // report
        } finally {
            //close file
            try {
                writer.close();
            } catch (Exception ex) {
            }
        }
}
	
	public void computeMaxTarget () {
		if (inSight > maxTarget) {
			maxTarget = inSight;
			newRecord = true;
			saveMaxTarget();
		}
		else {
			Date date = new Date();
			maxLastRun = inSight;
			dayMaxLastRun = dateFormatDate.format(date);
			hourMaxLastRun = dateFormatTime.format(date);
			saveMaxLastRun();
		}
	}
	
	public int computeUpdatedTarget() {
		
		int notUpdated = 0;
		
		for (Date d : lastUpdateDate) {
			if (Utils.secondsBetween(d, startTime) < 0) {
				notUpdated++;
			}
		}
		
		int resu = lastUpdateDate.size()-notUpdated;
		
		return resu;
	}
	
	public void saveData() {

		Date now = new Date();

		if (Utils.secondsBetween(now, lastSaveDate) > saveDelay) {

			saveShips();
			nbSave++;

			long diff = now.getTime() - startTime.getTime();
			long diffSeconds = diff / 1000 % 60;
			long diffMinutes = diff / (60 * 1000) % 60;
			long diffHours = diff / (60 * 60 * 1000) % 24;
			long diffDays = diff / (60 * 60 * 1000) / 24;

			aisTrackPanel.updateAisPanelStatus("Database saved (save #" + nbSave + ")");
			aisTrackPanel.updateAisPanelStatus((posUpdates - onlineDBupdate) + " pos updated / " + nbMmsiReceived
					+ " new ships / " + nbNamesReceived + " new names");
			aisTrackPanel.updateAisPanelStatus("Running for " + diffDays + " days " + diffHours + " hours "
					+ diffMinutes + " minutes " + diffSeconds + " seconds");
			aisTrackPanel.updateAisPanelCount(dateFormatTime.format(now), inSight, aisShips.size(),
					nbNamesDB + nbNamesReceived);

			if (newRecord) {
				aisTrackPanel.updateAisPanelStatus("Record : " + maxTarget + " (new record !)");
				aisTrackPanel.updateAisPanelStatus("Last : " + maxLastRunHisto + " - AIS5 msg : " + nbNamesMessages);
				newRecord = false;
			} else {
				aisTrackPanel.updateAisPanelStatus(
						"Record : " + maxTarget + " - last : " + maxLastRunHisto + " - AIS5 msg : " + nbNamesMessages);
			}

			lastSaveDate = now;

		} else {
			aisTrackPanel.updateAisPanelStatus(
					"Please wait " + (saveDelay - (Utils.secondsBetween(now, lastSaveDate))) + " seconds before next save");
		}
	}
	

}
