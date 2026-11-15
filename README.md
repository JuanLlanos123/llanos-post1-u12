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