# Diseño Propuesto

Antes de implementar el sistema de solicitudes de reembolso, se identificaron los puntos de variabilidad de cada requisito funcional y se seleccionaron los patrones de diseño que permiten resolverlos sin modificar innecesariamente el código existente.

## RF1 — Validación en varias etapas

**Punto de variabilidad:** Las reglas de validación que debe superar una solicitud antes de poder aprobarse. Actualmente se requieren tres verificaciones independientes: validar el tope de la categoría, comprobar los documentos soporte y verificar que no exista otra solicitud pendiente del mismo empleado para la misma categoría y período. En el futuro podrán incorporarse nuevas reglas, como un límite anual acumulado por empleado.

**Patrón seleccionado: Chain of Responsibility (Cadena de Responsabilidad).**

**Justificación:** Se selecciona este patrón porque permite organizar cada verificación como un validador independiente que procesa la solicitud y la pasa al siguiente únicamente cuando cumple la regla, facilitando agregar nuevas validaciones sin modificar las existentes.

## RF2 — Cálculo distinto por categoría de gasto

**Punto de variabilidad:** La fórmula utilizada para calcular el monto que puede aprobarse según la categoría del gasto. Los viáticos utilizan un tope diario multiplicado por los días del viaje, los gastos de representación reembolsan el monto completo hasta un límite mensual y la capacitación reconoce el 80 % del monto hasta un tope anual. Se contempla incorporar nuevas categorías, como bienestar, con fórmulas propias.

**Patrón seleccionado: Strategy (Estrategia).**

**Justificación:** Se selecciona este patrón porque permite encapsular cada fórmula de cálculo en una estrategia independiente y seleccionar la correspondiente según la categoría, incorporando nuevas categorías sin modificar los algoritmos existentes.

## RF3 — Notificación simultánea a varios interesados

**Punto de variabilidad:** Los destinatarios y las acciones que deben ejecutarse cuando una solicitud cambia de estado. El empleado recibe el resultado de su solicitud, Contabilidad recibe únicamente las aprobaciones para programar los pagos y Auditoría registra todos los cambios de estado. En el futuro podrán incorporarse otros interesados, como un panel interno de métricas.

**Patrón seleccionado: Observer (Observador).**

**Justificación:** Se selecciona este patrón porque permite que los interesados se suscriban a los cambios de estado de una solicitud y reaccionen independientemente según sus responsabilidades, incorporando nuevos observadores sin modificar el componente que publica los cambios.

---

## Resumen del diseño propuesto

| Requisito | Punto de variabilidad | Patrón seleccionado |
|---|---|---|
| RF1 | Reglas y etapas de validación | Chain of Responsibility |
| RF2 | Fórmulas de cálculo por categoría | Strategy |
| RF3 | Destinatarios y reacciones ante cambios de estado | Observer |

## Conclusión del diseño

Los tres patrones se seleccionaron porque cada requisito presenta una variabilidad diferente: RF1 necesita extender una secuencia de verificaciones independientes, RF2 requiere intercambiar algoritmos de cálculo según la categoría y RF3 necesita notificar a múltiples interesados con comportamientos independientes.

Esta propuesta busca mantener el sistema extensible y reducir el acoplamiento, evitando que la incorporación de nuevas validaciones, categorías o destinatarios obligue a modificar las implementaciones existentes.

La implementación se realizará después de establecer y documentar estas decisiones de diseño.



### Decisión de diseño 1: Orden de los validadores en Chain of Responsibility

Se decidió establecer el siguiente orden en la cadena de validación: primero `ValidadorDocumentosSoporte`, después `ValidadorMontoMaximo` y finalmente `ValidadorDuplicados`.

Esta decisión se fundamenta en un criterio de eficiencia: ejecutar primero las verificaciones sencillas y dejar al final la operación que requiere comparar la solicitud con los registros existentes. La validación documental permite comprobar inicialmente la presencia de los soportes requeridos, mientras que la validación del monto verifica que el valor solicitado no supere el límite establecido para su categoría.

La validación de duplicados se ubica al final porque representa la operación más costosa, debido a que necesita buscar coincidencias considerando el empleado, la categoría y el período de la solicitud. De esta manera, si una solicitud no contiene los documentos necesarios o incumple el límite de su categoría, la cadena puede detenerse antes de realizar esa búsqueda.

Ubicar primero el validador de duplicados permitiría identificar anticipadamente las solicitudes repetidas, pero tendría como desventaja que todas las solicitudes tendrían que asumir inicialmente el costo de consultar los registros existentes, incluso aquellas que posteriormente serían rechazadas por verificaciones más sencillas.

Por tanto, se prioriza el orden documentos, monto y duplicados, considerando que las dos primeras verificaciones son independientes y menos costosas que la búsqueda de solicitudes existentes.

**Orden implementado:**

1. `ValidadorDocumentosSoporte`.
2. `ValidadorMontoMaximo`.
3. `ValidadorDuplicados`.

La cadena utiliza una estrategia de interrupción: cuando una validación falla, se detiene el procesamiento y devuelve el resultado correspondiente.


### Decisión de diseño 2: Selección de calculadoras mediante un mapa construido por Spring

Se decidió utilizar un `Map<CategoriaGasto, CalculadoraReembolso>` construido a partir de una `List<CalculadoraReembolso>` inyectada automáticamente por Spring, en lugar de implementar un método con un `switch` manual sobre `CategoriaGasto`.

Esta elección se fundamenta directamente en el RF2, que establece que Finanzas planea incorporar nuevas categorías de gasto en los próximos semestres, cada una con su propia fórmula de cálculo, sin modificar los cálculos de las categorías existentes.

Mediante la inyección de dependencias, Spring proporciona automáticamente todas las implementaciones de `CalculadoraReembolso` registradas como componentes. A partir de esa lista se construye un mapa que relaciona cada categoría con su calculadora correspondiente. De esta manera, el mecanismo de selección no necesita conocer anticipadamente todas las categorías disponibles ni incorporar nuevas condiciones cada vez que aparece una categoría.

Para agregar una categoría como bienestar, basta con desarrollar una nueva implementación de `CalculadoraReembolso` y registrarla como componente de Spring. El mapa incorpora automáticamente esa implementación, evitando modificar el selector y las calculadoras que ya existen.

La alternativa de utilizar un `switch` manual no cumple este objetivo de extensibilidad, porque cada nueva categoría obligaría a modificar el método de selección e incorporar otro caso, aumentando el acoplamiento y dificultando el mantenimiento.

Por tanto, se elige la construcción del mapa mediante inyección de dependencias porque permite extender el comportamiento del sistema agregando nuevas implementaciones, sin modificar el código existente de selección, aplicando así el principio Open/Closed: las clases deben estar abiertas a la extensión, pero cerradas a la modificación.

**Mecanismo seleccionado:** `List<CalculadoraReembolso>` inyectada por Spring y transformación a `Map<CategoriaGasto, CalculadoraReembolso>`.

**Consideración:** si `CategoriaGasto` está implementado como un enum, será necesario agregar la nueva constante al enum. Esto no obliga a modificar el selector ni las calculadoras existentes; si se requiere no modificar absolutamente ningún archivo existente, también habría que utilizar un mecanismo extensible de identificación de categorías.


### Decisión de diseño 4: Diferenciación entre dependencias de Spring en el paquete procesamiento

Se decidió permitir las anotaciones de estereotipo de Spring, como `@Component` y `@Service`, dentro del paquete `procesamiento`, manteniendo la prohibición de importar clases de `org.springframework.data`, `jakarta.persistence` y componentes de la capa web.

Esta decisión se fundamenta en que no todas las dependencias del framework representan el mismo tipo de acoplamiento. Las anotaciones de estereotipo permiten que Spring identifique y registre componentes como validadores, calculadoras y notificadores, facilitando su administración e inyección de dependencias sin introducir directamente operaciones de persistencia ni responsabilidades propias de la presentación.

Por el contrario, las clases de Spring Data y JPA incorporan mecanismos específicos de acceso, almacenamiento y mapeo de datos, mientras que las clases de Spring Web introducen responsabilidades relacionadas con HTTP, controladores y recepción de solicitudes. Permitir estas dependencias dentro de `procesamiento` provocaría que las reglas de validación, cálculo y notificación quedaran condicionadas por detalles de infraestructura o de presentación.

Por esta razón, la Regla 1 distingue entre las anotaciones utilizadas para el registro de componentes y las dependencias que introducen responsabilidades de infraestructura. No se prohíbe indiscriminadamente todo el paquete `org.springframework`, sino aquellas dependencias que comprometen la separación de responsabilidades y el aislamiento del procesamiento.

Esta decisión permite aprovechar el contenedor de Spring para administrar los componentes sin convertir el paquete `procesamiento` en una capa de persistencia o presentación. Además, mantiene una restricción concreta y verificable: los validadores, calculadoras y notificadores pueden utilizar anotaciones como `@Component`, pero no deben importar repositorios de Spring Data, anotaciones de persistencia JPA ni componentes de Spring Web.

**Criterio de aplicación:**

- **Permitido:** `org.springframework.stereotype.Component`, `org.springframework.stereotype.Service` y otras anotaciones de estereotipo necesarias.
- **No permitido:** `org.springframework.data.*`, `jakarta.persistence.*` y `org.springframework.web.*`.

La decisión busca equilibrar la integración con Spring y la separación de responsabilidades, evitando restricciones innecesarias que impedirían registrar los componentes mediante el contenedor de dependencias.


## Alternativas de diseño consideradas y descartadas

Durante el desarrollo de las dos partes del proyecto se evaluaron alternativas arquitectónicas y de implementación que ofrecían mayor flexibilidad, pero que no se consideraron necesarias para los requisitos actuales. Siguiendo el criterio de la Guía de Contenido, sección 4.2, se priorizaron soluciones justificadas por necesidades existentes y no por posibilidades futuras sin evidencia concreta.

### 1. Spring ApplicationEventPublisher en lugar de una lista de observadores inyectada

**Alternativa considerada:** utilizar `ApplicationEventPublisher` para publicar eventos de dominio y registrar los receptores mediante `@EventListener`, en lugar de inyectar directamente una `List<ObservadorCambioEstado>` en el servicio.

**Decisión:** se descartó esta alternativa y se conservó la lista de observadores inyectada.

**Justificación:** el sistema cuenta actualmente con tres observadores conocidos y definidos en tiempo de compilación. La interfaz `ObservadorCambioEstado` ya proporciona el desacoplamiento necesario entre el servicio y sus destinatarios. Introducir un bus genérico de eventos agregaría una capa de indirección sin resolver una necesidad adicional comprobada y dificultaría rastrear qué componentes reaccionan ante cada cambio de estado.

**Señal futura para reconsiderar:** si diferentes módulos independientes necesitaran publicar y consumir eventos sin conocerse entre sí, o si se requiriera procesamiento asíncrono y distribución de eventos entre procesos, tendría sentido evaluar `ApplicationEventPublisher` u otra infraestructura de eventos.

### 2. Visitor o Abstract Factory para exportar reportes en varios formatos

**Alternativa considerada:** implementar una jerarquía de exportadores utilizando Visitor o Abstract Factory para permitir la generación de reportes en diferentes formatos, como PDF, Excel y CSV.

**Decisión:** se descartó esta alternativa y se evitó construir anticipadamente una jerarquía de exportación.

**Justificación:** actualmente existe un requisito concreto de exportación en formato CSV, pero no hay solicitudes confirmadas de Contabilidad para incorporar otros formatos. Aplicar estos patrones desde el inicio significaría introducir abstracciones para resolver una variabilidad que todavía no existe, incumpliendo el criterio Rule of Three de la Guía de Contenido.

**Señal futura para reconsiderar:** recibir una segunda solicitud real de exportación, por ejemplo, la necesidad de generar reportes tanto en PDF como en Excel, justificaría evaluar una estructura común de exportadores.

### 3. Jerarquía de excepciones por validador en lugar de ResultadoValidacion

**Alternativa considerada:** crear una jerarquía de excepciones específicas para cada regla de validación, por ejemplo, `MontoExcedidoException`, `DocumentosFaltantesException` y `SolicitudDuplicadaException`, para comunicar los errores detectados por cada validador de la cadena.

**Decisión:** se descartó esta alternativa y se mantuvo el uso de `ResultadoValidacion` como mecanismo común para comunicar el resultado de las verificaciones.

**Justificación:** en el sistema actual, los incumplimientos de las reglas de validación son resultados esperados del proceso de presentación de una solicitud y no situaciones excepcionales que justifiquen crear y administrar diferentes tipos de excepciones. El objeto `ResultadoValidacion` permite comunicar si la solicitud cumple las condiciones y proporcionar el motivo cuando una verificación falla, manteniendo un contrato común entre los validadores y `CadenaValidadores`.

Crear una excepción por cada validador aumentaría la cantidad de clases y obligaría a incorporar mecanismos adicionales para capturar, clasificar y transformar cada excepción en una respuesta comprensible. Esta complejidad no aporta un beneficio proporcional mientras todas las verificaciones compartan el mismo flujo de tratamiento.

**Señal futura para reconsiderar:** si los errores de validación necesitaran tratamientos completamente diferentes fuera de la cadena, como activar procesos de recuperación específicos, generar respuestas HTTP diferenciadas o ejecutar flujos particulares de atención según el tipo de incumplimiento, podría justificarse introducir excepciones especializadas o una clasificación de errores más detallada.

### Conclusión del análisis de alternativas

Las tres alternativas fueron descartadas porque su complejidad adicional no está respaldada por una necesidad actual del sistema. La decisión no implica que estos patrones o mecanismos sean inadecuados, sino que su incorporación debe responder a una variabilidad real y documentada.

El criterio aplicado consiste en mantener las soluciones actuales mientras satisfagan los requisitos existentes y reconsiderar las alternativas cuando aparezcan necesidades concretas que justifiquen su adopción. De esta manera se evita la sobreingeniería y se conserva la posibilidad de evolucionar la arquitectura cuando el proyecto realmente lo requiera.