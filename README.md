# Marker

Marker es una aplicación desarrollada para Android (versión 6+) que permite compartir nuestra posición geográfica con nuestros contactos y así ellos pueden dar un seguimiento de en dónde estamos. En Marker se definen lugares a los que quiero llegar y una vez que estamos dentro de un radio de proximidad (definido por el usuario), se notifica a todos los contactos a los que se le ha compartido el Marker de nuestra llegada.

Por defecto, cada Marker que se crea es compartido con uno mismo, lo que permite tener un funcionamiento tipo alarma cuando llegamos al destino.

## Funcionalidades

  * Login con Facebook: Para gestionar a los usuarios de la aplicación, se utiliza un login con Facebook. Mediante este login, obtenemos también todos aquellos amigos de Facebook que tengan la aplicación instalada.
  * Selección de Destinos: Para definir el destino de un Marker que estamos creando, la aplicación provee de 4 puntos de acceso distintos para una mayor facilidad.
  1. Buscador: En la Toolbar existe un input para buscar una ubicación por su nombre.
  2. Mapa: Se puede seleccionar en el mismo mapa para definir una ubicación.
  3. Historial: Los destinos utilizados previamente _(por 1 y por 2)_ son guardados en un historial accesible desde el drawer izquierdo.
  4. Mis Destinos: Los destinos existentes en el historial pueden ser guardados con un nombre distintivo _(como puede ser "Casa", "Trabajo")_, los cuales también son accesibles desde el drawer.
  * Seguimiento de Ubicación: Desde un drawer derecho se pueden acceder a todos los Markers que han sido compartidos con el usuario, permitiendo elegir entre ellos y mostrar la ubicación en tiempo real de dicho contacto junto con su destino.
  * Notificaciones: 
  1. En el inicio de un Marker, se genera una notificación a aquellos contactos a los que se le ha compartido el Marker avisando de éste inicio.
  2. En el fin de un Marker, se genera una notificación avisando a nuestros contactos que hemos llegado a destino.

## Visualización de la Aplicación

### Pantalla Principal

![init](https://scontent-eze1-1.xx.fbcdn.net/v/t34.0-12/23897754_1260126004088098_567859701_n.png?oh=b37faf3b8afa42a7e831d383e89c25e3&oe=5A18D1B0)

### Drawer Izquierdo

![drawer](https://scontent-eze1-1.xx.fbcdn.net/v/t34.0-12/23845452_1260125934088105_96516359_n.png?oh=6a4675858bf4f0654d7ca66b252308a4&oe=5A18D030)

### Mis Destinos

![places](https://scontent-eze1-1.xx.fbcdn.net/v/t34.0-12/23897764_1260125970754768_1120035530_n.png?oh=7975b78763d47026dde910d06eea255f&oe=5A19F3A7)

### Marker Activo

![marker](https://scontent-eze1-1.xx.fbcdn.net/v/t34.0-12/23846296_1260125977421434_2045809508_n.png?oh=793cdc7b6cf139d7fb9b36fb67eb1c38&oe=5A18C02B)

### Drawer Derecho

![markers_drawer](https://scontent-eze1-1.xx.fbcdn.net/v/t34.0-12/23897845_1260126060754759_1527435481_n.png?oh=f1a45558dbd153c0d2b17f1a66a81d0c&oe=5A19086E)

### Notificación

![notification](https://scontent-eze1-1.xx.fbcdn.net/v/t34.0-12/24008455_1260125987421433_1088826753_n.png?oh=48522d5019e19d1ea1ee5ced59a48f93&oe=5A190763)
