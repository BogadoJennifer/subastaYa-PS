| Metodo y recurso | Comportamiento |
|------------------|----------------|
|`GET /api/auctions `                          |Devuelve entidades de subastas.                |
|`GET /api/auctions/{id}`                      |Devuelve una entidad o 404.            |
|`POST /api/auctions  `                        |Recibe una entidad y la guarda; devuelve 201.              |
|`GET /api/auctions/catalog`                   |Devuelve DTOs con categoría, oferta máxima y cantidad de ofertas.              |
|`GET /api/auctions/{id}/details`              |Devuelve DTO de detalle con oferta máxima e incremento.                |
|`POST /api/auctions/{auctionId}/bids`         |Registra una oferta mediante el servicio.               |
|`GET /api/users`                              |Devuelve entidades `User`             |
|`GET /api/wallets/user/{userId}`              |Consulta una billetera por usuario.             |
|`GET /api/wallets/{id}/balance`               |Devuelve desglose de saldos.              |
|`POST /api/wallets/{id}/deposits?amount=...  `|Simula un depósito y registra un movimiento.          |
|`GET /api/wallets/{id}/transactions`          |Devuelve movimientos.              |

Evidencia: `/src/main/java/unaj.subasaya/controller`