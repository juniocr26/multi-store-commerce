# Planned commerce capabilities

The following requirements are preserved from the original project plan. Only the store directory is implemented; these commerce rules and endpoints remain proposals.

## Domain Model

Proposed entities:

| Entity            | Responsibility                        |
| ----------------- | ------------------------------------- |
| Store             | Store identity and configuration      |
| User              | Authenticated identity                |
| StoreMembership   | Administrative access to stores       |
| Customer          | Customer information                  |
| Address           | Delivery address                      |
| Category          | Product classification                |
| Product           | Shared product information            |
| StoreProduct      | Store-specific price and availability |
| Inventory         | Stock associated with a store product |
| Cart / CartItem   | Shopping selections                   |
| Order / OrderItem | Purchase records and price snapshots  |
| Payment           | Payment state and provider references |
| Delivery          | Delivery information                  |

The initial model represents stores within one network. Support for unrelated businesses with separate tenancy boundaries is outside the initial scope.

### Core Rules

- Each cart belongs to one store.
- Products from different stores cannot be mixed in one cart.
- Each order belongs to one store.
- Checkout revalidates prices, availability, and stock.
- Order items preserve purchased prices and quantities.
- Administrative permissions are scoped to authorized stores.
- Invalid order state transitions must be rejected.

### Inventory Decisions

The implementation must define reservation timing, expiration, confirmation, and release.

Concurrency handling and payments completed after reservation expiration will be documented and tested.

## API

The following endpoints are proposals, not an implemented API contract:

| Method | Endpoint                             | Purpose                             |
| ------ | ------------------------------------ | ----------------------------------- |
| GET    | `/stores`                            | List stores                         |
| GET    | `/stores/{storeId}`                  | Retrieve a store                    |
| GET    | `/stores/{storeId}/products`         | Browse a store catalog              |
| POST   | `/stores/{storeId}/orders`           | Submit checkout and create an order |
| GET    | `/stores/{storeId}/orders/{orderId}` | Retrieve an authorized order        |
| POST   | `/webhooks/stripe`                   | Receive payment events              |

Authentication, cart management, and administrative endpoints will be defined during implementation.

The webhook endpoint will use Stripe signature verification rather than customer authentication. Order access will require ownership or appropriate administrative permissions.

The final contract will be documented through OpenAPI.

## Authentication and Authorization

Spring Security will enforce access control.

Proposed initial roles:

- `CUSTOMER`
- `STORE_MANAGER`
- `PLATFORM_ADMIN`

Authorization will consider both role and resource scope. A store manager must not gain access to another store by changing an identifier in a request.

JWT is under consideration. Token storage, expiration, renewal, and revocation policies remain to be defined.

## Payments and Idempotency

V1 will support card payments in Stripe's test environment.

Payment processing will account for:

- Signature verification.
- Duplicate webhook deliveries.
- Concurrent event processing.
- Failed processing and safe retries.
- Out-of-order events.
- Consistent payment and order updates.

An event must not be considered successfully processed solely because it was received.

Database constraints and transaction boundaries will protect against duplicate processing. The exact event tracking strategy will be documented in an ADR.

