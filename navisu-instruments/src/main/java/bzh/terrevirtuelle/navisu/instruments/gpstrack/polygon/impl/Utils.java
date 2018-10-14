package bzh.terrevirtuelle.navisu.instruments.gpstrack.polygon.impl;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.WWMath;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.measure.MeasureTool;
import gov.nasa.worldwind.util.measure.MeasureToolController;

import java.nio.CharBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.capcaval.c3.component.annotation.UsedService;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import bzh.terrevirtuelle.navisu.instruments.common.view.panel.TrackPanel;
import bzh.terrevirtuelle.navisu.instruments.gpstrack.polygon.GpsTrackPolygon;
import bzh.terrevirtuelle.navisu.instruments.gpstrack.polygon.GpsTrackPolygonServices;
import bzh.terrevirtuelle.navisu.server.DataServerServices;

/*import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;*/

public class Utils {
	
    @UsedService
	static
    DataServerServices dataServerServices;
	
    protected static final String ANSI_RESET = "\u001B[0m";
    protected static final String ANSI_BLACK = "\u001B[30m";
    protected static final String ANSI_RED = "\u001B[31m";
    protected static final String ANSI_GREEN = "\u001B[32m";
    protected static final String ANSI_YELLOW = "\u001B[33m";
    protected static final String ANSI_BLUE = "\u001B[34m";
    protected static final String ANSI_PURPLE = "\u001B[35m";
    protected static final String ANSI_CYAN = "\u001B[36m";
    protected static final String ANSI_WHITE = "\u001B[37m";

	public static Position barycenter(ArrayList<? extends Position> list) {
		double latmax = list.get(0).getLatitude().getDegrees();
		double longmax = list.get(0).getLongitude().getDegrees();
		double latmin = list.get(0).getLatitude().getDegrees();
		double longmin = list.get(0).getLongitude().getDegrees();
		double altmax = list.get(0).getAltitude();
		double latmoy = 0;
		double longmoy = 0;
		Position resu;

		for (Position pos : list) {
			if (Math.abs(latmax) < Math.abs(pos.getLatitude().getDegrees())) {
				latmax = pos.getLatitude().getDegrees();
			}
			if (Math.abs(latmin) > Math.abs(pos.getLatitude().getDegrees())) {
				latmin = pos.getLatitude().getDegrees();
			}
			if (Math.abs(longmax) < Math.abs(pos.getLongitude().getDegrees())) {
				longmax = pos.getLongitude().getDegrees();
			}
			if (Math.abs(longmin) > Math.abs(pos.getLongitude().getDegrees())) {
				longmin = pos.getLongitude().getDegrees();
			}
			if (altmax < pos.getAltitude()) {
				altmax = pos.getAltitude();
			}
		}
		// System.out.println(latmin +" "+ latmax+ " " + latmoy);
		latmoy = (latmin + latmax) / 2;
		longmoy = (longmin + longmax) / 2;
		if (altmax <= 0) {
			altmax = 0;
		}

		resu = new Position(LatLon.fromDegrees(latmoy, longmoy), altmax);

		return resu;
	}

	public static double computeCourse(Position start, Position end) {
		if (start.getLatitude().getDegrees() == end.getLatitude().getDegrees()
				&& start.getLongitude().getDegrees() == end.getLongitude().getDegrees()) {
			return 500;
		}

		double lat1 = start.getLatitude().getRadians();
		double lat2 = end.getLatitude().getRadians();
		double lon1 = start.getLongitude().getRadians();
		double lon2 = end.getLongitude().getRadians();
		double resu = Math.atan2((Math.sin(lon2 - lon1)) * (Math.cos(lat2)),
				(Math.cos(lat1)) * (Math.sin(lat2)) - (Math.sin(lat1)) * (Math.cos(lat2)) * (Math.cos(lon2 - lon1)));
		resu = Math.toDegrees(resu);
		resu = Math.round(resu);
		if (resu < 0) {
			resu = resu + 360;
		}
		return resu;
	}

	public static Doublon detectTurn(ArrayList<Position> positions, double alpha) {
		ArrayList<Double> headings = new ArrayList<Double>();
		for (int i = 0; i < positions.size() - 1; i++) {
			// ajout des caps dans la liste headings seulement si les deux
			// points i et i+1 sont bien distincts
			if (Utils.computeCourse(positions.get(i), positions.get(i + 1)) != 500) {
				headings.add(Utils.computeCourse(positions.get(i), positions.get(i + 1)));
			}
		}
		/*
		 * String resu = ""; for (Double d : headings) { resu = resu +
		 * d.toString() + "-"; } aisTrackPanel.updateAisPanelStatus(resu);
		 */
		double h0 = headings.get(0);
		double hf = h0 + alpha;
		if (hf >= 360) {
			hf = hf - 360;
		}
		if (hf < 0) {
			hf = hf + 360;
		}
		int a = 0;
		int b = 0;
		int c = 0;
		int d = 0;
		boolean left = false;
		boolean right = false;
		double h0min = h0 - 20;
		double h0max = h0 + 20;
		if (h0min < 0) {
			h0min = h0min + 360;
			left = true;
		}
		if (h0max >= 360) {
			h0max = h0max - 360;
			right = true;
		}

		boolean leftf = false;
		boolean rightf = false;
		double hfmin = hf - 20;
		double hfmax = hf + 20;
		if (hfmin < 0) {
			hfmin = hfmin + 360;
			leftf = true;
		}
		if (hfmax >= 360) {
			hfmax = hfmax - 360;
			rightf = true;
		}

		for (Double h : headings) {
			if (left || right) {
				if ((0 < h && h < h0max) || (h0min < h && h < 360)) {
					a++;
				}
			}
			if (!left && !right) {
				if (h0min < h && h < h0max) {
					a++;
				}
			}
			if (leftf || rightf) {
				if ((0 < h && h < hfmax) || (hfmin < h && h < 360)) {
					b++;
				}
			}
			if (!leftf && !rightf) {
				if (hfmin < h && h < hfmax) {
					b++;
				}
			}
		}

		for (int j = 0; j < headings.size() - 1; j++) {
			if (Math.abs(headings.get(j) - headings.get(j + 1)) > 40) {
				d++;
			}
		}

		int taille = headings.size();
		c = taille - a - b;
		double resultat = 0;

		// aisTrackPanel.updateAisPanelStatus("a=" + a + " b=" + b + " c=" + c +
		// " d=" + d);
		if (taille > (a + b)) {
			resultat = (Math.round(((a + b) * 1000) / taille)) / 10;
		} else {
			resultat = 100.0;
		}

		if ((double) a >= taille * 0.1 && (double) b >= taille * 0.1 && (double) (a + b) >= taille * 0.7
				&& (double) c <= taille * 0.45 && (double) d <= taille * 0.4) {
			Doublon doublon = new Doublon(true, resultat);
			return doublon;
		} else {
			Doublon doublon = new Doublon(false, resultat);
			return doublon;
		}
	}

	public static Doublon detectStraightLine(ArrayList<Position> positions) {
		ArrayList<Double> headings = new ArrayList<Double>();
		for (int i = 0; i < positions.size() - 1; i++) {
			// ajout des caps dans la liste headings seulement si les deux
			// points i et i+1 sont bien distincts
			if (Utils.computeCourse(positions.get(i), positions.get(i + 1)) != 500) {
				headings.add(Utils.computeCourse(positions.get(i), positions.get(i + 1)));
			}
		}
		/*
		 * String resu = ""; for (Double d : headings) { resu = resu +
		 * d.toString() + "-"; } aisTrackPanel.updateAisPanelStatus(resu);
		 */
		double h0 = headings.get(0);
		int a = 0;
		int d = 0;
		boolean left = false;
		boolean right = false;
		double h0min = h0 - 20;
		double h0max = h0 + 20;
		if (h0min < 0) {
			h0min = h0min + 360;
			left = true;
		}
		if (h0max >= 360) {
			h0max = h0max - 360;
			right = true;
		}

		for (Double h : headings) {
			if (left || right) {
				if ((0 < h && h < h0max) || (h0min < h && h < 360)) {
					a++;
				}
			}
			if (!left && !right) {
				if (h0min < h && h < h0max) {
					a++;
				}
			}
		}

		for (int j = 0; j < headings.size() - 1; j++) {
			if (Math.abs(headings.get(j) - headings.get(j + 1)) > 40) {
				d++;
			}
		}

		int taille = headings.size();
		double resultat = 0;

		// aisTrackPanel.updateAisPanelStatus("a=" + a + " d=" + d);
		resultat = (Math.round((a * 1000) / taille)) / 10;

		if (((double) a) >= taille * 0.7 && (double) d <= taille * 0.4) {
			Doublon doublon = new Doublon(true, resultat);
			return doublon;
		} else {
			Doublon doublon = new Doublon(false, resultat);
			return doublon;
		}
	}

	public static Position translate(Position start, Position end, Position pos) {
		return new Position(LatLon.fromDegrees(
				pos.getLatitude().getDegrees() + end.getLatitude().getDegrees() - start.getLatitude().getDegrees(),
				pos.getLongitude().getDegrees() + end.getLongitude().getDegrees() - start.getLongitude().getDegrees()),
				0);
	}

	public static Doublon pathInsideBuffer(ArrayList<Position> positions, MeasureTool mt) {
		int total = 0;
		int outside = 0;
		for (Position p : positions) {
			total++;
			if (!WWMath.isLocationInside(p, mt.getPositions())) {
				outside++;
			}
		}
		if (outside == 0) {
			return new Doublon(true, (Math.round(1000 * (total - outside) / total)) / 10);
		} else {
			return new Doublon(false, (Math.round(1000 * (total - outside) / total)) / 10);
		}
	}

	public static ArrayList<Position> createTranslatedBuffer(ArrayList<Position> positions, Position start,
			Position end, double meters) {
		ArrayList<Position> positions2 = new ArrayList<Position>();
		for (Position p : positions) {
			positions2.add(Utils.translate(start, end, p));
		}
		Coordinate[] coordinates2 = new Coordinate[positions2.size()];
		int i = 0;
		for (Position p : positions2) {
			coordinates2[i] = new Coordinate(p.getLatitude().getDegrees(), p.getLongitude().getDegrees());
			i++;
		}
		Geometry g1 = new GeometryFactory().createLineString(coordinates2);
		Geometry g2 = g1.buffer(meters / 111120);
		ArrayList<Position> resu = new ArrayList<Position>();
		for (Coordinate c : g2.getCoordinates()) {
			Position pos = new Position(LatLon.fromDegrees(c.x, c.y), 0);
			resu.add(pos);
		}
		return resu;
	}

	public static String repeatChar(char c, int length) {

		char[] data = new char[length];
		Arrays.fill(data, c);
		return new String(data);

	}

	public static String spaces(int spaces) {

		return CharBuffer.allocate(spaces).toString().replace('\0', ' ');

	}
	
	public static boolean isStringNullOrWhiteSpace(String value) {
		if (value == null) {
			return true;
		}

		for (int i = 0; i < value.length(); i++) {
			if (!Character.isWhitespace(value.charAt(i))) {
				return false;
			}
		}

		return true;
	}
	
    public static boolean isEmptyReceived(int MMSI, String str) {
    	Date date = new Date();
    	DateFormat dateFormatTime = new SimpleDateFormat("HH:mm:ss");
    	if (isStringNullOrWhiteSpace(str) || str.replaceAll("\\s+","").length() <= 1 || "".equals(str.replaceAll("\\s+","")))
            {
        	int n = GpsTrackPolygonImpl.inSight;
        	Date d = new Date();
        	System.err.println(dateFormatTime.format(date) + " - MMSI " + MMSI + " - Name received is empty");
        	GpsTrackPolygonImpl.aisTrackPanel.updateAisPanelName(dateFormatTime.format(d), n, MMSI + " name empty - (AIS5)");
        	GpsTrackPolygonImpl.nbEmptyNamesReceived++;
        	return true;
        	}
        else
            {
        	System.out.println(ANSI_GREEN + dateFormatTime.format(date) + " - MMSI " + MMSI + " - Name received OK (not empty) : " + str + ANSI_RESET);
        	return false;
        	}
    }
    
    /**
     * Returns true if the string is null or 0-length.
     * @param str the string to be examined
     * @return true if str is null or zero length
     */
    
    public static boolean isEmpty(String str) {
        if (isStringNullOrWhiteSpace(str) || str.replaceAll("\\s+","").length() <= 1 || "".equals(str.replaceAll("\\s+","")))
            {
        	return true;
        	}
        else
            {
        	return false;
        	}
    }

	public static Date convDate(Date date, String hour) {

		Character a = hour.charAt(0);// ab:cd:xx
		Character b = hour.charAt(1);
		Character c = hour.charAt(3);
		Character d = hour.charAt(4);
		String h = "" + a + b;
		String m = "" + c + d;

		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(h.toString()));
		cal.set(Calendar.MINUTE, Integer.parseInt(m.toString()));
		cal.setTimeZone(TimeZone.getTimeZone("CET"));

		date = cal.getTime();

		return date;
	}

	public static long diffDates(Date one, Date two) {

		long difference = one.getTime() - two.getTime();
		return difference;
	}

	public static long daysBetween(Date one, Date two) {

		return TimeUnit.MILLISECONDS.toDays(diffDates(one, two));
	}

	public static long hoursBetween(Date one, Date two) {

		return TimeUnit.MILLISECONDS.toHours(diffDates(one, two));
	}

	public static long minutesBetween(Date one, Date two) {

		return TimeUnit.MILLISECONDS.toMinutes(diffDates(one, two));
	}

	public static long secondsBetween(Date one, Date two) {

		return TimeUnit.MILLISECONDS.toSeconds(diffDates(one, two));
	}

}
