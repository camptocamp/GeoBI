package com.c2c.geoutils;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * A class that generates a ReferenceEnvelope object, given a bounding box and a spatial reference system
 *
 * @author jeichar, pmauduit
 */
public class ReferenceEnvelopeFactory {

    public ReferenceEnvelopeFactory() {
    }

    ;


    public static ReferencedEnvelope toReferencedEnvelope(String bbox, String srs) {
        String[] parts = bbox.split(",");

        if (parts.length != 4) {
            throw new IllegalArgumentException("bbox parameter is not correctly formatted.  The format is: minLat,minLong,maxLat,maxLong");
        }

        double minx = Double.parseDouble(parts[0]);
        double miny = Double.parseDouble(parts[1]);
        double maxx = Double.parseDouble(parts[2]);
        double maxy = Double.parseDouble(parts[3]);

        CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;

        if (srs != null) {
            try {
                crs = CRS.decode(srs, true);
            } catch (Throwable e) {
                throw new IllegalArgumentException("unable to parse EPSG code: " + srs + "\n\n" + e.getLocalizedMessage(), e);
            }
        }
        return new ReferencedEnvelope(minx, maxx, miny, maxy, crs);
    }
}
