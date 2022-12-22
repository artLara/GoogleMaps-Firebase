# GoogleMaps-Firebase
Aplicación móvil que guarda los marcadores puestos en google maps dentro de firebase al oprimir un botón. También tiene la opción de borrar

Se inicia con 3 marcadores previos, pero cada vez que se da un click en la zona de una mapa se agrega un marcador nuevo y este tendrá un nombre de "Marcador #" iniciando con el número 1 hasta los n toques que se den. Cada marcador puede ser movido de lugar. En la siguiente imagen se muestra una toma de captura de a aplicación.

<img src="https://user-images.githubusercontent.com/63621038/209041597-4f13c6f0-0c22-4294-89ec-1a2c3945c7b6.jpg" alt="drawing" width="200"/>

El botón guardar escribirá un nuevo registro en la base de datos de firebase, como llave se utiliza la palabra Marcador concatenado con el número asignado y la fecha con hora, minutos y segundos para asegurar la no replicación de llaves. Como valor se utilizan la latitud y longitud. La siguiente imagen es un ejmplo de como se observa la consola de Firebase con unos registros previos hechos en pruebas. Además se cuenta con el botón de "Borrar" que elimina todos los registros previamente hechos.

<img src="https://user-images.githubusercontent.com/63621038/209041685-ce27dcb1-5b50-45a5-b4ed-5860ff5b35e0.png" alt="drawing" width="400"/>


**Nota:** Para ejecutar la aplicación es necesario crear una API KEY de Google Maps y ponerla dentro del archivo local.properties como MAPS_API_KEY= TU_API_KEY
