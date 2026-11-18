# Proyecto Bambi Redesign

Rediseño visual de Bambi para Android TV en la rama `redesign` del fork de `grodri99/bambi`. El alcance incluye el sistema de diseño, Inicio, las fichas, la búsqueda, la biblioteca, los ajustes y los perfiles. Se preservan las funcionalidades, la arquitectura y el rendimiento del proyecto original. La hoja de ruta general de Bambi sigue en [docs/ROADMAP.md](docs/ROADMAP.md).

**Estados.** ✅ Completada cuando hay evidencia en el checkout. 🟡 En curso para trabajo iniciado con verificaciones pendientes. ⬜ Pendiente para trabajo por iniciar o cuya ejecución aún no está verificada. La presencia de una pantalla original no implica que su rediseño esté terminado.

## Arquitectura actual

| Módulo | Componentes y responsabilidades actuales |
| --- | --- |
| `app` | `MainActivity` y `AppNavHost`. El grafo conecta configuración inicial, perfiles, pantalla principal, fichas, personas, descargas, reproductor, ajustes y sus rutas auxiliares. |
| `core:ui` | `ShieldStreamTheme`, `Tokens.kt` y `Fonts.kt` definen la base visual. `TopBar`, `TvButton`, `PosterCard`, `CatalogCard`, `UpNextCard`, `ProfileAvatar` y `BottomActionDialog` ofrecen piezas compartidas. `TvFocus.kt` y `LoopingRow.kt` concentran utilidades de foco, teclas y desplazamiento. |
| `feature:home` | `MainScreen` coordina las pestañas de `TopBar` y la vuelta del foco a la cabecera. `HomeScreen` presenta Inicio, Series y Películas según el filtro. El destacado real de Inicio es `Billboard` con `FeaturedCarousel`. `RowsBrowser` presenta las filas y recupera el foco. El módulo también contiene `CategoryScreen`, `SearchScreen`, `MyListScreen`, `EpisodeCenterScreen` y `DownloadsScreen`. |
| `feature:details` | `DetailsScreen` contiene el bloque informativo y coordina las acciones y paneles. `DetailsActions.kt`, `DetailsEpisodes.kt`, `DetailsSources.kt`, `DetailsExtras.kt` y `DetailsRatings.kt` separan esas secciones. `DetailsViewModel` gestiona el estado y la resolución de fuentes. |
| `feature:settings` | `SettingsScreen` usa componentes y tarjetas de Inicio, reproducción, fuentes y servicios. `SetupScreen` y `SetupViewModel` gestionan el asistente. `ProfilePickerScreen`, `ProfileEditScreen`, `ProfileIconPicker`, `PinDialog` y `TraktConnectScreen` cubren selección, edición y acciones de perfil. |
| Módulos de soporte | `core:strings` contiene recursos localizados y `localizedStringResource`. `core:domain` contiene contratos y `SelectedContentHolder`; `core:model`, los modelos; `core:data` y `core:network`, persistencia e integraciones; `core:media` y `feature:player`, la reproducción. |

La navegación combina las rutas de `AppNavHost` con las pestañas de `MainScreen`. Continuar viendo pasa por Detalles y usa `SelectedContentHolder` para transferir el episodio y la intención de reproducción. Las modificaciones se concentran en Compose. Antes de alterar un flujo existente, se explicarán los archivos y comportamientos afectados. Se conservará el sistema bilingüe existente y se justificará técnicamente cualquier dependencia nueva.

## Fase 0. Preparación

**Estado:** 🟡 En curso. **Módulos:** repositorio, configuración de Gradle y `app`.

- ✅ Creación de `redesign` y comprobación de que está activa. En la revisión comparte el commit base `54c41f0` con `main`.
- ✅ Identificación del fork en `origin` e inventario de módulos, navegación y componentes de interfaz.
- 🟡 Validar la preparación de Git, Codex y Android Studio. Codex opera en el checkout y las directrices de `AGENTS.md` ya figuran en el árbol de trabajo. Queda confirmar la sincronización en Android Studio y documentar cómo consultar e integrar upstream desde el fork.
- ✅ Compilación de la versión original completada en Windows 11 con Java 17 y Gradle 9.6.0. El APK debug generado tenía **27.578.307 bytes**.
- ✅ Arranque y navegación correctos en un emulador Android TV con salida a 1080p y API 36.
- ⬜ Completar las pruebas de reproducción y las pruebas en un dispositivo físico. Queda por confirmar el resultado de las pruebas unitarias y registrar la línea base de rendimiento con una compilación equivalente a la que se mida en la fase 6.
- ✅ Documentación del rediseño iniciada con las directrices y este roadmap.

**Dependencias.** El entorno de compilación debe estar validado antes de tomar la línea base. La línea base permite comparar el impacto visual y de rendimiento de las fases siguientes.

**Cierre.** Entorno reproducible, rama y relación con upstream documentadas, compilación y pruebas de referencia registradas y recorridos originales comprobados con mando.

## Fase 1. Sistema de diseño

**Estado:** ⬜ Pendiente. **Módulos:** `core:ui`, `core:strings` y pantallas consumidoras de `feature:*`.

- ⬜ Definir paleta, contraste, tipografías, espaciados y dimensiones sobre `ShieldStreamTheme`, `Tokens.kt` y `Fonts.kt`.
- ⬜ Aplicar la base a `TvButton`, `PosterCard`, `CatalogCard`, `UpNextCard` y diálogos, manteniendo sus contratos y los textos en español e inglés.
- ⬜ Definir los estados normal, enfocado, seleccionado y deshabilitado donde correspondan. Conservar las utilidades de `TvFocus.kt`, el escalado de póster de `1.04x` y el orden de los modificadores de interacción y escala.
- ⬜ Validar legibilidad, márgenes, recortes y foco visible en salidas de 1080p y 4K con textos de ambas lenguas.

**Dependencias.** Fase 0 para la comparación. Los tokens preceden a la adaptación de componentes y estos preceden a su adopción en pantallas.

**Cierre.** Los componentes compartidos tienen estados coherentes y accionables con mando. Las pantallas de referencia conservan la legibilidad y el foco en ambas resoluciones.

## Fase 2. Pantalla principal

**Estado:** ⬜ Pendiente. **Módulos:** `feature:home`, `core:ui` y `app` para verificar las rutas.

- ⬜ Rediseñar navegación y cabecera sobre `MainScreen` y `TopBar`, manteniendo accesos existentes, selección de pestaña, indicador de episodios y vuelta a la cabecera con Atrás.
- ⬜ Rediseñar `Billboard` y `FeaturedCarousel`. Conservar las imágenes estáticas y las transiciones inmediatas, así como la actualización diferida del fondo de las filas y la diferencia entre modo normal y ligero.
- ⬜ Adaptar `RowsBrowser`, `LoopingRow` y las tarjetas. Preservar claves, índices virtuales, precarga de dos tarjetas y restauración del foco tras volver de una ficha o retirar un elemento.
- ⬜ Ajustar la jerarquía visual de las secciones existentes en `HomeScreen` y `CategoryScreen`. Mantener los filtros de Serie y Películas, la personalización de categorías y la carga diferida del centro de episodios al abrir la pestaña.

**Dependencias.** Fase 1 antes de aplicar los componentes. Definir primero los recorridos entre cabecera, destacado y filas. Verificar el retorno desde las fichas con la fase 3.

**Cierre.** El mando alcanza todas las secciones existentes. Cambiar de pestaña, mantener pulsado Arriba o Abajo y regresar de una ficha conservan el comportamiento del foco y no añaden trabajo innecesario al cargar imágenes o episodios.

## Fase 3. Fichas de películas y series

**Estado:** ⬜ Pendiente. **Módulos:** `feature:details` y `core:ui`. `app`, `core:domain` y `feature:player` intervienen en la verificación del flujo.

- ⬜ Reorganizar imagen, título, metadatos y sinopsis en `DetailsScreen` y su `InfoBlock`. Mantener las secciones existentes de reparto, contenido relacionado y valoraciones.
- ⬜ Revisar la jerarquía de acciones con `DetailsActions`, incluyendo reproducir o reanudar y las acciones secundarias disponibles según el contenido.
- ⬜ Adaptar `SeasonsAndEpisodes` y `SourcesPanel`. Conservar la selección de temporada y episodio, el estado visto, los filtros y agrupaciones de fuentes que ya ofrece el panel y la resolución centralizada en `DetailsViewModel`.
- ⬜ Verificar la entrada y devolución del foco en los paneles, la prioridad de cierre con Atrás y el paso al reproductor, incluida la apertura con episodio seleccionado y reproducción automática puntual.

**Dependencias.** Fase 1 para botones, tarjetas y diálogos. Fase 2 para validar entradas y retornos desde Inicio. Definir primero la jerarquía de información y acciones para diseñar después los recorridos de foco.

**Cierre.** Películas y series se pueden abrir y reproducir con mando. Temporadas, episodios, fuentes y menús conservan sus acciones y estados, y Atrás devuelve el foco al punto previsto.

## Fase 4. Búsqueda y biblioteca

**Estado:** ⬜ Pendiente. **Módulos:** `feature:home` y `core:ui`, con verificación del paso a `feature:details`.

- ⬜ Rediseñar la entrada y el teclado de `SearchScreen` preservando la lógica de consulta de `SearchViewModel` y los recorridos entre teclado y resultados.
- ⬜ Adaptar los resultados y sus estados, incluidos los títulos restringidos y sus solicitudes cuando estén disponibles. Los filtros por tipo y la ordenación identificados pertenecen a `MyListScreen`, y los filtros de catálogo viven en Inicio. La búsqueda actual no expone filtros equivalentes.
- ⬜ Rediseñar Mi lista con `MyListScreen` y `MyListViewModel`. Conservar los filtros Todo, Series y Películas, las opciones de ordenación y el estado asociado al perfil.
- ⬜ Adaptar la fila Continuar viendo de `HomeRows.kt`. Mantener la reanudación con OK y el menú de pulsación larga para abrir Detalles, quitar o cancelar. Mantener el paso por Detalles para resolver la reproducción y respetar la posición guardada.

**Dependencias.** Fase 1 para las tarjetas y controles. Fase 2 para pestañas, filas y vuelta del foco. Fase 3 para verificar aperturas desde resultados o biblioteca y la reanudación.

**Cierre.** Se puede escribir, corregir y abrir un resultado con mando. Mi lista conserva filtros, ordenación y datos por perfil. Continuar viendo reanuda y permite quitar elementos sin perder el foco operativo.

## Fase 5. Ajustes y perfiles

**Estado:** ⬜ Pendiente. **Módulos:** `feature:settings`, `core:ui` y `app` para verificar las rutas.

- ⬜ Rediseñar `SettingsScreen` y las tarjetas de Inicio, reproducción, fuentes y servicios. Mantener las rutas secundarias actuales y el desplazamiento vertical de los formularios largos.
- ⬜ Revisar `ProfilePickerScreen`, `ProfileEditScreen`, `ProfileIconPicker` y `PinDialog`. Mantener creación, cambio y edición de perfiles, PIN cuando proceda y acceso al enlace con Trakt.
- ⬜ Rediseñar `SetupScreen`, incluida la bienvenida y sus cuatro pasos numerados de catálogo, debrid, subtítulos extra y preferencias. Mantener la opción de configuración por teléfono y diferir el reinicio por idioma hasta finalizar el asistente.

**Dependencias.** Fase 1 para controles, formularios, avatares y diálogos. Fase 2 para comprobar entradas desde Inicio y fase 4 para verificar el aislamiento de biblioteca y progreso al cambiar de perfil. Establecer primero patrones de foco y desplazamiento para reutilizarlos.

**Cierre.** Se completan el asistente inicial y el cambio de perfil con mando, con botones alcanzables a 1080p. El idioma de interfaz y las preferencias de audio y subtítulos mantienen su alcance y momento de aplicación originales.

## Fase 6. Revisión y optimización

**Estado:** ⬜ Pendiente. **Módulos:** `app`, `core:ui` y `feature:*`. Los demás módulos se incluyen en las pruebas de regresión cuando participan en los recorridos.

- ⬜ Comparar con la fase 0 el tiempo de cuadro, la respuesta al mando, la memoria y el tamaño del APK. Comparar la misma variante y condiciones, incluida la variante `benchmark` para medir un comportamiento cercano a release. Priorizar costes de interfaz y explicar cualquier diferencia relevante.
- ⬜ Pasar la matriz de mando y foco en emulador Android TV y en un dispositivo físico, preferiblemente el Nokia Streaming Box 8010 de referencia. Cubrir 1080p y 4K, modos normal y ligero, ambos idiomas, retorno desde Detalles y reproductor, pulsación larga, Atrás y repetición de teclas.
- ⬜ Ejecutar pruebas y compilación finales. Verificar en dispositivo los flujos que la compilación y las pruebas unitarias no cubren, como la reproducción directa y la restauración del foco.
- ⬜ Corregir regresiones y revisar el diff frente a la base. Documentar los puntos de integración con upstream y mantener los cambios pequeños y verificables.

**Dependencias.** La línea base de la fase 0 y los flujos terminados de las fases 1 a 5. Medir antes de optimizar y repetir las mediciones y pruebas afectadas después de cada corrección.

**Cierre.** Resultados de compilación, pruebas y mediciones registrados. Recorridos de mando aprobados, regresiones bloqueantes resueltas y cambios acotados para facilitar la integración con upstream.

## Ideas futuras pendientes de decidir

- Evaluar si incorporar filtros propios a la búsqueda. Requeriría definir el comportamiento del catálogo, las restricciones de perfil y la navegación del control con mando. Queda fuera de los criterios de cierre del rediseño.
