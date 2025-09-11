# Backend

## Normalización de pedidos

El servicio `RegistroService` expone el método `normalize(RegistryDto)` para homogeneizar los datos de un pedido antes de realizar su validación:

- Si `shipping_address.zip` es `null` o solo contiene espacios, guiones o comillas, se establece en `null`.
- Si `shipping_address.street` está vacío y `shipping_address.address1` tiene contenido, se copia su valor en `street`.

Todos los controladores que procesen pedidos (API, CSV o futuros conectores) deben invocar `normalize(registro)` **antes** de llamar a `validacion(registro)` para asegurar un comportamiento consistente.

