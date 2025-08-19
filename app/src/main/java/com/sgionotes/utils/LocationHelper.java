package com.sgionotes.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

public class LocationHelper {
    private static final String TAG = "LocationHelper";
    public interface LocationCallback {
        void onLocationReceived(String coordinates);
        void onLocationError(String error);
    }
    public static void getCurrentLocation(Context context, LocationCallback callback) {
        if (!hasLocationPermissions(context)) {
            callback.onLocationError("Permisos de ubicación no concedidos");
            return;
        }

        if (!isLocationEnabled(context)) {
            callback.onLocationError("GPS deshabilitado. Por favor habilita la ubicación en configuración");
            return;
        }
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            String coordinates = formatCoordinates(location.getLatitude(), location.getLongitude());
                            Log.d(TAG, "Ubicación obtenida: " + coordinates);
                            callback.onLocationReceived(coordinates);
                        } else {
                            callback.onLocationError("No se pudo obtener la ubicación actual");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Error obteniendo ubicación", e);
                        callback.onLocationError("Error al obtener ubicación: " + e.getMessage());
                    }
                });
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException al obtener ubicación", e);
            callback.onLocationError("Error de permisos al obtener ubicación");
        }
    }

    public static boolean hasLocationPermissions(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
               ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isLocationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
               locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public static String formatCoordinates(double latitude, double longitude) {
        return String.format("%.6f,%.6f", latitude, longitude);
    }

    public static double[] parseCoordinates(String coordinates) {
        if (coordinates == null || coordinates.trim().isEmpty()) {
            return null;
        }

        try {
            String[] parts = coordinates.split(",");
            if (parts.length == 2) {
                double lat = Double.parseDouble(parts[0].trim());
                double lng = Double.parseDouble(parts[1].trim());
                return new double[]{lat, lng};
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parseando coordenadas: " + coordinates, e);
        }

        return null;
    }

    public static String getReadableLocation(String coordinates) {
        double[] coords = parseCoordinates(coordinates);
        if (coords != null) {
            return String.format("Lat: %.4f, Lng: %.4f", coords[0], coords[1]);
        }
        return "Ubicación no disponible";
    }
}
