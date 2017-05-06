package bzh.terrevirtuelle.navisu.main;

import bzh.terrevirtuelle.navisu.app.drivers.directorydriver.impl.DirectoryDriverManagerImpl;
import bzh.terrevirtuelle.navisu.app.dpagent.impl.DpAgentImpl;
import bzh.terrevirtuelle.navisu.app.drivers.databasedriver.DatabaseDriverManagerServices;
import bzh.terrevirtuelle.navisu.app.drivers.databasedriver.impl.DatabaseDriverManagerImpl;
import bzh.terrevirtuelle.navisu.app.drivers.driver.Driver;
import bzh.terrevirtuelle.navisu.app.drivers.driver.DriverManagerServices;
import bzh.terrevirtuelle.navisu.app.drivers.driver.impl.DriverManagerImpl;
import bzh.terrevirtuelle.navisu.app.drivers.instrumentdriver.InstrumentDriverManagerServices;
import bzh.terrevirtuelle.navisu.app.drivers.instrumentdriver.impl.InstrumentDriverManagerImpl;
import bzh.terrevirtuelle.navisu.app.drivers.webdriver.WebDriverManagerServices;
import bzh.terrevirtuelle.navisu.app.drivers.webdriver.impl.WebDriverManagerImpl;
import bzh.terrevirtuelle.navisu.app.guiagent.GuiAgentServices;
import bzh.terrevirtuelle.navisu.app.guiagent.impl.GuiAgentImpl;
import bzh.terrevirtuelle.navisu.app.guiagent.options.OptionsManagerServices;
import bzh.terrevirtuelle.navisu.app.guiagent.options.impl.OptionsManagerImpl;
import bzh.terrevirtuelle.navisu.app.guiagent.utilities.I18nLangEnum;
import bzh.terrevirtuelle.navisu.app.guiagent.utilities.Translator;
import bzh.terrevirtuelle.navisu.bathymetry.catalog.local.BathymetryLocalCatalogServices;
import bzh.terrevirtuelle.navisu.bathymetry.catalog.local.impl.BathymetryLocalCatalogImpl;
import bzh.terrevirtuelle.navisu.bathymetry.charts.BathymetryServices;
import bzh.terrevirtuelle.navisu.bathymetry.charts.impl.BathymetryImpl;
import bzh.terrevirtuelle.navisu.bathymetry.controller.eventsProducer.BathymetryEventProducerServices;
import bzh.terrevirtuelle.navisu.bathymetry.controller.eventsProducer.impl.BathymetryEventProducerImpl;
import bzh.terrevirtuelle.navisu.bathymetry.db.BathymetryDBServices;
import bzh.terrevirtuelle.navisu.bathymetry.db.impl.BathymetryDBImpl;
import bzh.terrevirtuelle.navisu.charts.raster.geotiff.GeoTiffChartServices;
import bzh.terrevirtuelle.navisu.charts.raster.geotiff.impl.GeoTiffChartImpl;
import bzh.terrevirtuelle.navisu.charts.raster.kap.KapChartServices;
import bzh.terrevirtuelle.navisu.charts.raster.kap.impl.KapChartImpl;
import bzh.terrevirtuelle.navisu.charts.vector.s57.catalog.global.S57GlobalCatalogServices;
import bzh.terrevirtuelle.navisu.charts.vector.s57.catalog.global.impl.S57GlobalCatalogImpl;
import bzh.terrevirtuelle.navisu.charts.vector.s57.catalog.local.S57LocalCatalogServices;
import bzh.terrevirtuelle.navisu.charts.vector.s57.charts.S57ChartServices;
import bzh.terrevirtuelle.navisu.charts.vector.s57.charts.impl.S57ChartImpl;
import bzh.terrevirtuelle.navisu.client.nmea.NmeaClientServices;
import bzh.terrevirtuelle.navisu.client.nmea.impl.vertx.NmeaClientImpl;
import bzh.terrevirtuelle.navisu.core.view.geoview.worldwind.impl.GeoWorldWindViewImpl;
import bzh.terrevirtuelle.navisu.currents.CurrentsServices;
import bzh.terrevirtuelle.navisu.currents.impl.CurrentsImpl;
import bzh.terrevirtuelle.navisu.database.DatabaseServices;
import bzh.terrevirtuelle.navisu.database.app.TestDBServices;
import bzh.terrevirtuelle.navisu.database.app.impl.TestDBImpl;
import bzh.terrevirtuelle.navisu.database.impl.DatabaseImpl;
import bzh.terrevirtuelle.navisu.geometry.curves2D.bezier.Bezier2DServices;
import bzh.terrevirtuelle.navisu.geometry.curves2D.bezier.impl.Bezier2DImpl;
import bzh.terrevirtuelle.navisu.gpx.GpxObjectServices;
import bzh.terrevirtuelle.navisu.gpx.impl.GpxObjectImpl;
import bzh.terrevirtuelle.navisu.netcdf.grib.GribServices;
import bzh.terrevirtuelle.navisu.netcdf.grib.impl.GribImpl;
import bzh.terrevirtuelle.navisu.instruments.ais.base.AisServices;
import bzh.terrevirtuelle.navisu.instruments.ais.base.impl.AisImpl;
import bzh.terrevirtuelle.navisu.instruments.ais.logger.AisLoggerServices;
import bzh.terrevirtuelle.navisu.instruments.ais.logger.impl.AisLoggerImpl;
import bzh.terrevirtuelle.navisu.instruments.ais.plotter.AisPlotterServices;
import bzh.terrevirtuelle.navisu.instruments.ais.plotter.impl.AisPlotterImpl;
import bzh.terrevirtuelle.navisu.instruments.gpstrack.plotter.GpsTrackPlotterServices;
import bzh.terrevirtuelle.navisu.instruments.gpstrack.plotter.impl.GpsTrackPlotterImpl;
import bzh.terrevirtuelle.navisu.instruments.gpstrack.polygon.GpsTrackPolygonServices;
import bzh.terrevirtuelle.navisu.instruments.gpstrack.polygon.impl.GpsTrackPolygonImpl;
import bzh.terrevirtuelle.navisu.instruments.ais.aisradar.AisRadarServices;
import bzh.terrevirtuelle.navisu.instruments.ais.aisradar.impl.AisRadarImpl;
import bzh.terrevirtuelle.navisu.instruments.sonar.SonarServices;
import bzh.terrevirtuelle.navisu.instruments.sonar.impl.SonarImpl;
import bzh.terrevirtuelle.navisu.instruments.template.InstrumentTemplateServices;
import bzh.terrevirtuelle.navisu.instruments.template.impl.InstrumentTemplateImpl;
import bzh.terrevirtuelle.navisu.instruments.compass.CompassServices;
import bzh.terrevirtuelle.navisu.instruments.compass.impl.CompassImpl;
import bzh.terrevirtuelle.navisu.instruments.gps.logger.GpsLoggerServices;
import bzh.terrevirtuelle.navisu.instruments.gps.logger.impl.GpsLoggerImpl;
import bzh.terrevirtuelle.navisu.instruments.gps.plotter.GpsPlotterServices;
import bzh.terrevirtuelle.navisu.instruments.gps.plotter.impl.GpsPlotterImpl;
import bzh.terrevirtuelle.navisu.kml.KmlObjectServices;
import bzh.terrevirtuelle.navisu.kml.impl.KmlObjectImpl;
import bzh.terrevirtuelle.navisu.server.DataServerServices;
import bzh.terrevirtuelle.navisu.server.impl.vertx.DataServerImpl;
import bzh.terrevirtuelle.navisu.magnetic.MagneticServices;
import bzh.terrevirtuelle.navisu.magnetic.impl.MagneticImpl;
import bzh.terrevirtuelle.navisu.media.sound.SoundServices;
import bzh.terrevirtuelle.navisu.media.sound.impl.SoundImpl;
import bzh.terrevirtuelle.navisu.navigation.route.RouteServices;
import bzh.terrevirtuelle.navisu.navigation.route.impl.RouteImpl;
import bzh.terrevirtuelle.navisu.sedimentology.SedimentologyServices;
import bzh.terrevirtuelle.navisu.sedimentology.impl.SedimentologyImpl;
import bzh.terrevirtuelle.navisu.shapefiles.ShapefileObjectServices;
import bzh.terrevirtuelle.navisu.shapefiles.impl.ShapefileObjectImpl;
import bzh.terrevirtuelle.navisu.speech.SpeakerServices;
import bzh.terrevirtuelle.navisu.speech.impl.SpeakerImpl;
import bzh.terrevirtuelle.navisu.system.files.FilesServices;
import bzh.terrevirtuelle.navisu.system.files.impl.FilesImpl;
import bzh.terrevirtuelle.navisu.util.Pair;
import bzh.terrevirtuelle.navisu.wms.WMSServices;
import bzh.terrevirtuelle.navisu.wms.impl.WMSImpl;
import gov.nasa.worldwind.geom.Position;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.stage.Stage;

import org.capcaval.c3.componentmanager.ComponentManager;

/**
 * @author Serge Morvan <morvan at enib.fr>
 * @author Thibault Pensec <thibault.pensec at gmail.com>
 * @author Jordan Mens <jordan.mens at gmail.com>
 * @author Dominique Marques <dom.marques at free.fr>
 */
public class AppMain extends Application {

    private static final Logger LOGGER = Logger.getLogger(AppMain.class.getName());
    private final String DATA_S57_CATALOG_1 = "data/charts/vector/s57/catalog/ENC_NP1.kmz";
    private final String DATA_S57_CATALOG_2 = "data/charts/vector/s57/catalog/ENC_NP2.kmz";
    private final String DATA_S57_CATALOG_3 = "data/charts/vector/s57/catalog/ENC_NP3.kmz";
    private final String DATA_S57_CATALOG_4 = "data/charts/vector/s57/catalog/ENC_NP4.kmz";
    private final String DATA_S57_CATALOG_5 = "data/charts/vector/s57/catalog/ENC_NP5.kmz";
    private final String DATA_S57_CATALOG_6 = "data/charts/vector/s57/catalog/ENC_NP6.kmz";

    @Override
    public void start(Stage stage) throws Exception {

        Translator.setLang(I18nLangEnum.FRENCH);

        LogManager.getLogManager().readConfiguration(new FileInputStream("conf/logging.properties"));

        final ComponentManager componentManager = ComponentManager.componentManager;

        /* Deploy components */
        LOGGER.info("\n"
                + componentManager.startApplication(
                        GuiAgentImpl.class,//in first
                        AisImpl.class,
                        AisLoggerImpl.class,
                        AisPlotterImpl.class,
                        AisRadarImpl.class,
                        BathymetryDBImpl.class,
                        BathymetryEventProducerImpl.class,
                        BathymetryImpl.class,
                        BathymetryLocalCatalogImpl.class,
                        Bezier2DImpl.class,
                        CurrentsImpl.class,
                        DataServerImpl.class,
                        DatabaseImpl.class,
                        DatabaseDriverManagerImpl.class,
                        DirectoryDriverManagerImpl.class,
                        DpAgentImpl.class,
                        DriverManagerImpl.class,
                        FilesImpl.class,
                        GeoTiffChartImpl.class,
                        GpsLoggerImpl.class,
                        GpsTrackPlotterImpl.class,
                        GpsTrackPolygonImpl.class,
                        GpsPlotterImpl.class,
                        GpxObjectImpl.class,
                        GribImpl.class,
                        InstrumentDriverManagerImpl.class,
                        InstrumentTemplateImpl.class,
                        CompassImpl.class,
                        KapChartImpl.class,
                        KmlObjectImpl.class,
                        MagneticImpl.class,
                        RouteImpl.class,
                        NmeaClientImpl.class,
                        OptionsManagerImpl.class,
                        SedimentologyImpl.class,
                        ShapefileObjectImpl.class,
                        SonarImpl.class,
                        SoundImpl.class,
                        SpeakerImpl.class,
                        S57ChartImpl.class,
                        S57GlobalCatalogImpl.class,
                        TestDBImpl.class,
                        WebDriverManagerImpl.class,
                        WMSImpl.class
                )
        );
        /* Services */
        AisServices aisServices = componentManager.getComponentService(AisServices.class);
        AisLoggerServices aisLoggerServices = componentManager.getComponentService(AisLoggerServices.class);
        AisPlotterServices aisPlotterServices = componentManager.getComponentService(AisPlotterServices.class);
        AisRadarServices aisRadarServices = componentManager.getComponentService(AisRadarServices.class);

        BathymetryServices bathymetryServices = componentManager.getComponentService(BathymetryServices.class);
        BathymetryLocalCatalogServices bathymetryLocalCatalogServices = componentManager.getComponentService(BathymetryLocalCatalogServices.class);
        BathymetryDBServices bathymetryDBServices = componentManager.getComponentService(BathymetryDBServices.class);
        BathymetryEventProducerServices bathymetryEventProducerServices = componentManager.getComponentService(BathymetryEventProducerServices.class);

        Bezier2DServices bezier2DServices = componentManager.getComponentService(Bezier2DServices.class);

        CurrentsServices currentsServices = componentManager.getComponentService(CurrentsServices.class);

        DatabaseServices databaseServices = componentManager.getComponentService(DatabaseServices.class);
        DataServerServices dataServerServices = componentManager.getComponentService(DataServerServices.class);

        FilesServices filesServices = componentManager.getComponentService(FilesServices.class);

        GeoTiffChartServices geoTiffChartServices = componentManager.getComponentService(GeoTiffChartServices.class);
        GpsLoggerServices gpsLoggerServices = componentManager.getComponentService(GpsLoggerServices.class);

        GpsTrackPlotterServices gpsTrackPlotterServices = componentManager.getComponentService(GpsTrackPlotterServices.class);
        GpsTrackPolygonServices gpsTrackPolygonServices = componentManager.getComponentService(GpsTrackPolygonServices.class);

        GpsPlotterServices gpsPlotterServices = componentManager.getComponentService(GpsPlotterServices.class);
        GpxObjectServices gpxObjectServices = componentManager.getComponentService(GpxObjectServices.class);
        GribServices gribServices = componentManager.getComponentService(GribServices.class);
        GuiAgentServices guiAgentServices = componentManager.getComponentService(GuiAgentServices.class);
        guiAgentServices.showGui(stage, 1080, 700);

        InstrumentTemplateServices instrumentTemplateServices = componentManager.getComponentService(InstrumentTemplateServices.class);
        CompassServices compassServices = componentManager.getComponentService(CompassServices.class);
        KapChartServices chartsServices = componentManager.getComponentService(KapChartServices.class);
        KmlObjectServices kmlObjectServices = componentManager.getComponentService(KmlObjectServices.class);

        MagneticServices magneticServices = componentManager.getComponentService(MagneticServices.class);

        OptionsManagerServices optionsManagerServices = componentManager.getComponentService(OptionsManagerServices.class);
        //optionsManagerServices.show();

        RouteServices routeServives = componentManager.getComponentService(RouteServices.class);

        SedimentologyServices sedimentologyServices = componentManager.getComponentService(SedimentologyServices.class);
        ShapefileObjectServices shapefileObjectServices = componentManager.getComponentService(ShapefileObjectServices.class);
        SonarServices sonarServices = componentManager.getComponentService(SonarServices.class);
        SoundServices soundServices = componentManager.getComponentService(SoundServices.class);
        SpeakerServices speakerServices = componentManager.getComponentService(SpeakerServices.class);
        S57LocalCatalogServices catalogS57Services = componentManager.getComponentService(S57LocalCatalogServices.class);
        S57GlobalCatalogServices s57GlobalCatalogServices = componentManager.getComponentService(S57GlobalCatalogServices.class);
        S57ChartServices chartS57Services = componentManager.getComponentService(S57ChartServices.class);

        TestDBServices testDBServices = componentManager.getComponentService(TestDBServices.class);

        WMSServices wmsServices = componentManager.getComponentService(WMSServices.class);
        wmsServices.init();

        // Manager services
        DatabaseDriverManagerServices databaseDriverManagerServices = componentManager.getComponentService(DatabaseDriverManagerServices.class);
        databaseDriverManagerServices.registerNewDriver(bathymetryDBServices.getDriver());

        // DirectoryDriverManagerServices ddriverServices = componentManager.getComponentService(DirectoryDriverManagerServices.class);
        //    ddriverServices.init();
        //    ddriverServices.registerNewDriver(catalogS57Services.getDriver());
        DriverManagerServices driverServices = componentManager.getComponentService(DriverManagerServices.class);
        driverServices.init();
        driverServices.registerNewDriver(bathymetryLocalCatalogServices.getDriver());
        driverServices.registerNewDriver(bathymetryServices.getDriver());
        driverServices.registerNewDriver((Driver) chartsServices.getDriver());
        driverServices.registerNewDriver(chartS57Services.getDriver());
        driverServices.registerNewDriver(currentsServices.getDriver());
        driverServices.registerNewDriver((Driver) geoTiffChartServices.getDriver());
        driverServices.registerNewDriver(gpxObjectServices.getDriver());
        driverServices.registerNewDriver((Driver) gribServices.getDriver());
        driverServices.registerNewDriver(kmlObjectServices.getDriver());
        driverServices.registerNewDriver(magneticServices.getDriver());
        driverServices.registerNewDriver(shapefileObjectServices.getDriver());
        driverServices.registerNewDriver(sedimentologyServices.getDriver());
        driverServices.registerNewDriver(s57GlobalCatalogServices.getDriver());
        driverServices.registerNewDriver(filesServices.getDriver());

        InstrumentDriverManagerServices instrumentDriverManagerServices = componentManager.getComponentService(InstrumentDriverManagerServices.class);
        instrumentDriverManagerServices.init();

        instrumentDriverManagerServices.registerNewDriver(gpsLoggerServices.getDriver());

        instrumentDriverManagerServices.registerNewDriver(gpsTrackPlotterServices.getDriver());
        instrumentDriverManagerServices.registerNewDriver(gpsTrackPolygonServices.getDriver());

        instrumentDriverManagerServices.registerNewDriver(compassServices.getDriver());
        instrumentDriverManagerServices.registerNewDriver(aisLoggerServices.getDriver());
        instrumentDriverManagerServices.registerNewDriver(aisPlotterServices.getDriver());
        instrumentDriverManagerServices.registerNewDriver(aisRadarServices.getDriver());
        instrumentDriverManagerServices.registerNewDriver(gpsLoggerServices.getDriver());
        instrumentDriverManagerServices.registerNewDriver(gpsPlotterServices.getDriver());
        instrumentDriverManagerServices.registerNewDriver(instrumentTemplateServices.getDriver());
        instrumentDriverManagerServices.registerNewDriver(sonarServices.getDriver());
        instrumentDriverManagerServices.registerNewDriver(soundServices.getDriver());

        WebDriverManagerServices webDriverServices = componentManager.getComponentService(WebDriverManagerServices.class);
        webDriverServices.init("http://ows.emodnet-bathymetry.eu/wms");
        // webDriverServices.init("http://www.gebco.net/data_and_products/gebco_web_services/web_map_service/mapserv?");
        // webDriverServices.init("http://maps.ngdc.noaa.gov/arcgis/services/etopo1/MapServer/WmsServer?");
        webDriverServices.registerNewDriver(wmsServices.getDriver());

        //Loading S57 catalog
        s57GlobalCatalogServices.load(DATA_S57_CATALOG_1,
                DATA_S57_CATALOG_2,
                DATA_S57_CATALOG_3,
                DATA_S57_CATALOG_4,
                DATA_S57_CATALOG_5,
                DATA_S57_CATALOG_6);

        //First position
        GeoWorldWindViewImpl.getWW().getView().setEyePosition(Position.fromDegrees(48.40, -4.4853, 15000));

        // Initialisation des paramètres de diffusion des data.
        dataServerServices.init("localhost", 8585);

        /* Test connexion GPS */
        // dataServerServices.openSerialPort("COM5", 4800, 8, 1, 0);
        // dataServerServices.openSerialPort("COM4", 4800, 8, 1, 0);
        /* Test connexion Gpsd */
        //dataServerServices.openGpsd("sinagot.net", 2947);
        //dataServerServices.openGpsd("fridu.net", 2947);
        // dataServerServices.openGpsd("sinagot.net", 4001); 
        //dataServerServices.openGpsd("hd-sf.com", 9009);
        // A tester, ref OCPN
        //tcp://sinagot.net:4002 NMEA/GPRMC
        //tcp://sinagot.net:4003 AIS
        /* Test lecture fichier */
        //dataServerServices.openFile("data/ais/ais.txt");  //AIS
        // dataServerServices.openFile("data/nmea/gpsLostennic.txt"); //NMEA0183 //gps.txt
        //dataServerServices.openFile("data/nmea/hcgdg.txt"); //test compas
        //dataServerServices.openFile("data/gpsd/gpsd_1.txt");//AIS Gpsd
        //dataServerServices.openFile("data/n2k/out1.json");//N2K
        //dataServerServices.openFile("data/n2k/sample.json");//N2K
        /* Test serveur Web Http */
        // dataServerServices.openHttpServer("localhost", 8181);
        /* Instanciation d'un client */
        NmeaClientServices nmeaClientServices = componentManager.getComponentService(NmeaClientServices.class);
        nmeaClientServices.open("localhost", 8585);//Attention même valeurs que le serveur !
        nmeaClientServices.request(500);

        /* Test clients à l'écoute des événements Nmea */
        aisServices.on();
        //aisLoggerServices.on();
        aisPlotterServices.on();
        //gpsTrackPolygonServices.on();
        //aisRadarServices.on();
        //gpsLoggerServices.on("data/nmea/test2.txt");
        //gpsPlotterServices.on();

        /* Test Bezier, approxiamtion trajectoire */
        /*List<Pair<Double, Double>> data = bezier2DServices.readCsv("data/saved/", "savedPath.csv");
        bezier2DServices.toKML("path.kml", data);

        List<Pair<Double, Double>> bezSi = bezier2DServices.leastSquare(data, 8);
        List<Pair<Double, Double>> bez = bezier2DServices.compute(bezSi, 0.01);
        bezier2DServices.toKML("data/kml/", "testBezier.kml", bez, "5000FF14", "2");
        
        // La liste headings est utile si on souhaite récupérer le cap en chaque point de la trajectoire
        // sous la forme [[Lat, Lon], heading]
        // si ce n'est pas nécessaire mettre null
        List<Pair<Pair<Double, Double>, Double>> headings = new ArrayList<>();
        List<Pair<Pair<Double, Double>, Pair<Double, Double>>> vectorTg
                = bezier2DServices.vectorTangentCompute(bezSi, 0.01, 0.5,  headings);
        bezier2DServices.toKML2("data/kml/", "testTgBezier.kml", vectorTg, "5014F0FF", "2");
        System.out.println(headings);*/

        /* Test CPA zone et reconnaissance de trajectoire */
     //dataServerServices.openFile("data/ais/ais.txt");  //AIS
        /* Test cibles AIS en direct */
     dataServerServices.openGpsd("sinagot.net", 2947);
        //dataServerServices.openGpsd("fridu.net", 2947);

        /* Test DB */
       // testDBServices.connect("data/databases/TestJDBC", "navisu", "!!navisu??");
        // testDBServices.runJDBC();//OK
        //Pas de connect() pour JPA, la DB est NavisuDB dans data/databases
        //testDBServices.runJPA();//OK
        /* Test speech */
        //speakerServices.read("data/text", "installation.txt", null);// local par defaut
        //speakerServices.read("data/text", "installation.txt", "fr_FR");//en_GB, en_US
        // speakerServices.read("naVisu est un logiciel de visualisation et de simulation de données maritimes.");
    }

    public static void main(String[] args) throws Exception {
        Application.launch();
    }
}
