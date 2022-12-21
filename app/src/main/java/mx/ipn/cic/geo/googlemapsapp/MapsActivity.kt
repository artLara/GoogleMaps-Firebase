package mx.ipn.cic.geo.googlemapsapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

import mx.ipn.cic.geo.googlemapsapp.databinding.ActivityMapsBinding

import mx.ipn.cic.geo.googlemapsapp.PermissionUtils.PermissionDeniedDialog.Companion.newInstance
import mx.ipn.cic.geo.googlemapsapp.PermissionUtils.isPermissionGranted
import mx.ipn.cic.geo.googlemapsapp.PermissionUtils.requestPermission
import java.text.SimpleDateFormat
import java.util.*

// Pasos a realizar en la app.
// Esta información se puede consultar en la sección de Android Developers.
// https://developers.google.com/maps/documentation/android-sdk/start

// 1. Crear un proyecto usando GoogleMaps Activity.
// 2. Actualizar las dependencias a la última versión.
// 3. Deshabilitar la revisión de errores tipográficos.

// 4. Cerciorarse de que se tienen los dos permisos para localización, en el archivo de manifiesto.
//     <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
//     <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

// 5. Personalizar el icono de la app (usando clipart con búsqueda maps).
// 6. Cambiar el manifiesto para que use el nuevo icono (ic_launcher_app & ic_launcher_app_round).
// 7. Ejecutar la app en el emulador. Observar lo que se muestra en pantalla.
// 8. Ocultar el app bar usando supportActionBar?.hide().
// 9. Personalizar el color usado en la parte superior de la app, por el color (115,6,56), o bien, #730638.

// 10. Ir a Google Cloud Console (https://console.cloud.google.com/).
//      10.a. Habilitar API de Maps SDK for Android.
//      10.b. Generar una credencial para la app (restringiar la credencial usando nombre de la app y SHA-1).

// 11. (Opcional). Crear otro proyecto con cualquier nombre usando la misma plantilla,
//     observando los cambios antes y después de restringir la credencial generada previamente.
// 12. Cambiar las coordenadas iniciales del mapa a la Ciudad de México (http://www.latlong.net)

// 13. Hacer el acceso a la credencial más seguro, mediante la siguiente modificación en el archivo build.gradle a nivel raíz.
//   buildscript {
//      dependencies {
//          // ...
//          classpath "com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:1.3.0"
//      }
//  }
// Observar que existe una actualización, de la versión del plugin (2.0.0).

// 14. Agregar el siguiente elemento en la sección de plugins del archivo build.gradle a nivel de app.
//   id 'com.google.android.libraries.mapsplatform.secrets-gradle-plugin'
// 15. Editar archivo local.properties añadiendo el siguiente elemento: MAPS_API_KEY=YOUR_API_KEY.

// 16. Modificar la recuperación de la credencial en el manifiesto de la app.
//   <meta-data
//    android:name="com.google.android.geo.API_KEY"
//    android:value="${MAPS_API_KEY}" />

// 17. Sincronizar el proyecto en algún control de versiones como github, buscar archivo local.properties.
// 18. Cambiar el diseño del layout para agregar 4 botones en la parte superior o inferior.
// 19. Personalizar atributos de cada uno de los botones.
// 20. Cambiar el color primario para que tenga el valor de #730638.

// 21. Modificar propiedades del mapa en diseño:
//      21.a. Habilitar los controles de zoom.
//      21.b. Habilitar los controles de los gestos de rotación.
//      21.c. Establecer la inclinación del mapa en 30 grados.

// 22. Agregar el código para cambiar el tipo de mapa empleando setOnClickListener para cada uno de los botones.
// 23. Modificar propiedades del mapa en tiempo de ejecución.
//      23.a. Habilitar el mapa normal.
//      23.b. Habilitar la capa de tráfico.
// 24. Agregar dos marcadores para centros comerciales donde se pueda visualizar planos de mapas.
// 25. Personalizar uno de los marcadores para usar una imagen.

// 26. Usar un widget tipo material (switcher) para habilitar / deshabilitar la capa de tráfico.
//      <com.google.android.material.switchmaterial.SwitchMaterial
//        android:layout_width="wrap_content"
//        android:layout_height="match_parent"
//        android:checked="true"
//        android:text="Habilitar capa de tráfico"/>

// 27. Habilitar Location siguiendo la guía descrita en la documentación de Android.
// https://developers.google.com/maps/documentation/android-sdk/location

class MapsActivity : AppCompatActivity(), OnMyLocationButtonClickListener,
    OnMyLocationClickListener, OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var latitud: Double = 0.0
    private var longitud: Double = 0.0
    private var markerNum: Int = 1
    private var currentMarkerName: String = "Marcador generico"

    private var permissionDenied = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        // Ocultar el app bar.
        supportActionBar?.hide()


        //Conexion a FireBase
        val database : FirebaseDatabase = FirebaseDatabase.getInstance()
        val referenciaBD : DatabaseReference = database.getReference("app_maps_firebase/coordenada_guardadas")
        // Asignar el código para cada uno de los eventos.

        binding.btnGuardar.setOnClickListener {
            Toast.makeText(this, "Se guardo $currentMarkerName con localización en (${this.longitud} ${this.latitud})", Toast.LENGTH_LONG).show()
            val tmp = " (${this.longitud} ${this.latitud})"
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR).toString()
            val month = c.get(Calendar.MONTH).toString()
            val day = c.get(Calendar.DAY_OF_MONTH).toString()
            val hour = c.get(Calendar.HOUR_OF_DAY).toString()
            val minute = c.get(Calendar.MINUTE).toString()
            val second = c.get(Calendar.SECOND).toString()

            val date = day + "," + month + ","+ year + ", "+ hour + ":" + minute + ":" + second
            referenciaBD.child("coordenadas").child(this.currentMarkerName+ " " + date).setValue(tmp)
        }
        binding.btnBorrar.setOnClickListener {
            Toast.makeText(this, "Se borraron todos los registros", Toast.LENGTH_LONG).show()
            referenciaBD.child("coordenadas").removeValue()
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap2: GoogleMap) {
        this.googleMap = googleMap2

        // Add a marker in Mexico City and move the camera
        val ciudadMexico = LatLng(19.432608, -99.133209)
        this.googleMap.addMarker(MarkerOptions().position(ciudadMexico).title("Ciudad de México").draggable(true))
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(ciudadMexico))
        this.googleMap.moveCamera(CameraUpdateFactory.zoomTo(14F))

        // Agregar marcadores para dos centros comerciales.
        val forumBuenavista = LatLng(19.449552,-99.151623)
        this.googleMap.addMarker(MarkerOptions().position(forumBuenavista).title("Forum Buenavista").draggable(true)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))

        // val parqueLindavista = LatLng(19.486213,-99.131228)
        // this.googleMap.addMarker(MarkerOptions().position(parqueLindavista).title("Parque Lindavista")
        //     .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))

        // Usando un marcador personalizado.
        val parqueLindavista = LatLng(19.486213,-99.131228)
        this.googleMap.addMarker(MarkerOptions().position(parqueLindavista).title("Parque Lindavista").draggable(true)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marcador_mapa)))

        this.latitud = 19.486213
        this.longitud = -99.131228
        // Modificar propiedades en tiempo de ejecución.
        this.googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        this.googleMap.isTrafficEnabled = true
        this.googleMap.isIndoorEnabled = true
        this.googleMap.setOnMapClickListener(object :GoogleMap.OnMapClickListener {
            override fun onMapClick(latlng :LatLng) {
                val location = LatLng(latlng.latitude,latlng.longitude)
                googleMap.addMarker(MarkerOptions().position(location).title("Marker $markerNum").draggable(true))
                currentMarkerName = "Marker $markerNum"
                markerNum++
                latitud = latlng.latitude
                longitud = latlng.longitude
            }
        })

        this.googleMap.setOnMarkerDragListener(object :GoogleMap.OnMarkerDragListener {
            override fun onMarkerDrag(p0: Marker) {
            }

            override fun onMarkerDragEnd(p0: Marker) {
                latitud = p0.position.latitude
                longitud = p0.position.longitude
                currentMarkerName = p0.title.toString()
            }

            override fun onMarkerDragStart(marker: Marker) {

            }
        });

        this.googleMap.setOnMyLocationButtonClickListener(this)
        this.googleMap.setOnMyLocationClickListener(this)
        enableMyLocation()
    }

    // https://developers.google.com/maps/documentation/android-sdk/location
    // https://github.com/googlemaps/android-samples/blob/master/ApiDemos/kotlin/app/src/gms/java/com/example/kotlindemos/MyLocationDemoActivity.kt

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        // Comprobar que la referencia al mapa googleMap es válida.
        if (!::googleMap.isInitialized) return
        // [START maps_check_location_permission]

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION, true
            )
        }
        // [END maps_check_location_permission]
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "Ha presionado el botón de Posición Actual", Toast.LENGTH_SHORT).show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    override fun onMyLocationClick(location: Location) {
        // Obtener las coordenadas de latitud y longitud.
        val latitud = location.latitude
        val longitud = location.longitude

        Toast.makeText(this, "Posición Actual:\n($latitud, $longitud)", Toast.LENGTH_LONG).show()
    }

    // [START maps_check_location_permission_result]
    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }
        if (isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
                Toast.makeText(this, "Permiso concedido", Toast.LENGTH_LONG).show()
            enableMyLocation()
        } else {
            // Permission was denied. Display an error message
            // [START_EXCLUDE]
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true
            // [END_EXCLUDE]
        }
    }

    // [END maps_check_location_permission_result]
    override fun onResumeFragments() {
        super.onResumeFragments()
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError()
            permissionDenied = false
        }
    }


    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private fun showMissingPermissionError() {
        newInstance(true).show(supportFragmentManager, "dialog")
    }

    companion object {
        /**
         * Request code for location permission request.
         *
         * @see .onRequestPermissionsResult
         */
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}