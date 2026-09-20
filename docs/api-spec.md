## API REST

| Método y recurso | Parámetros o cuerpo | Comportamiento y respuesta |
|---|---|---|
| `GET /api/auctions` | — | Devuelve la lista de entidades `Auction`. |
| `GET /api/auctions/{id}` | `id`: identificador de subasta. | Devuelve la entidad `Auction` o `404 Not Found` si no existe. |
| `POST /api/auctions` | JSON: `title`, `description`, `categoryId`, `imageUrl`, `basePrice`, `minimumIncrement`, `startDate`, `endDate`. | Crea una subasta para el vendedor `vendor@test.com`. Devuelve `201 Created` con `CreateAuctionResponseDto`; valida campos, categoría y fechas. El estado inicial es `SCHEDULED` si comienza en el futuro, o `ACTIVE` en caso contrario. |
| `GET /api/auctions/catalog` | — | Devuelve `AuctionCatalogDto` por subasta, con categoría, precio base, oferta máxima, cantidad de ofertas, estado y fechas. |
| `GET /api/auctions/{id}/details` | `id`: identificador de subasta. `userId` (opcional): usuario para informar si ya ofertó. | Devuelve `AuctionDetailsDto`, incluida la oferta máxima, el incremento mínimo, el postor de la oferta máxima y `currentUserHasBid`; devuelve `404 Not Found` si la subasta no existe. |
| `GET /api/auctions/{auctionId}/bids` | `auctionId`: identificador de subasta. | Devuelve, como máximo, las últimas 50 ofertas en orden descendente de fecha e id (`BidHistoryDto`). Devuelve `404 Not Found` si la subasta no existe. |
| `POST /api/auctions/{auctionId}/bids` | `auctionId`: identificador de subasta. JSON: `bidderId`, `amount`. | Registra una oferta y devuelve `201 Created` con `BidResultDto` (`bidId`, `amount`, `endDate`, `wasExtended`). Requiere una subasta activa, un monto válido y suficiente saldo; puede responder `400`, `404`, `409` o `422` según la causa. |
| `GET /api/categories` | — | Devuelve las categorías disponibles como `CategoryDto` (`id`, `name`). |
| `GET /api/users` | — | Devuelve la lista de entidades `User`. |
| `GET /api/users/{userId}/activities` | `userId`: identificador de usuario. | Devuelve `UserActivitiesDto`: participaciones, publicaciones e ingresos totales del usuario. Devuelve `404 Not Found` si el usuario no existe. |
| `GET /api/wallets/user/{userId}` | `userId`: identificador de usuario. | Devuelve `WalletSummaryDto` con la billetera, su titular y los saldos total, retenido y disponible. Devuelve `404 Not Found` si no existe una billetera para el usuario. |
| `GET /api/wallets/{id}/balance` | `id`: identificador de billetera. | Devuelve un objeto con `totalBalance`, `retainedBalance` y `availableBalance`; este último se calcula como total menos retenido. Devuelve `404 Not Found` si no existe. |
| `POST /api/wallets/{id}/deposits?amount=...` | `id`: identificador de billetera. Query param `amount`: monto positivo. | Acredita el monto, registra un movimiento `DEPOSIT` y devuelve `201 Created` con la entidad `Wallet` actualizada. Devuelve `400 Bad Request` para un monto no positivo y `404 Not Found` si no existe la billetera. |
| `GET /api/wallets/{id}/transactions` | `id`: identificador de billetera. | Devuelve los movimientos asociados a la billetera. Devuelve `404 Not Found` si no existe. |

## Actualizaciones en tiempo real (STOMP)

| Recurso | Comportamiento |
|---|---|
| `GET /ws` (SockJS) | Punto de conexión STOMP con orígenes permitidos. |
| `GET /ws-live` (WebSocket) | Punto de conexión STOMP para orígenes `http://localhost:*`. |
| Envío a `/app/auction/{auctionId}/bid` | Recibe un mensaje de texto y lo reenvía a `/topic/auction/{auctionId}`. |
| Suscripción a `/topic/auctions/{auctionId}` | Recibe las notificaciones que publica el servicio al registrar una oferta o al cerrar una subasta. |
