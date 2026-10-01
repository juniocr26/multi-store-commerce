# Capacidades de comércio planejadas

Os requisitos abaixo preservam o planejamento original. Apenas a listagem de lojas está implementada; as regras de comércio e endpoints abaixo continuam como propostas.

## Modelo de Domínio

Entidades propostas:

| Entidade          | Responsabilidade                              |
| ----------------- | --------------------------------------------- |
| Store             | Identificação e configuração da loja          |
| User              | Identidade autenticada                        |
| StoreMembership   | Acesso administrativo às lojas                |
| Customer          | Informações do cliente                        |
| Address           | Endereço de entrega                           |
| Category          | Classificação de produtos                     |
| Product           | Informações compartilhadas do produto         |
| StoreProduct      | Preço e disponibilidade por loja              |
| Inventory         | Estoque associado ao produto da loja          |
| Cart / CartItem   | Seleções de compra                            |
| Order / OrderItem | Registros da compra e preços adquiridos       |
| Payment           | Estado do pagamento e referências do provedor |
| Delivery          | Informações da entrega                        |

O modelo inicial representa unidades de uma mesma rede. O isolamento completo entre empresas independentes está fora do escopo inicial.

### Regras Centrais

- Cada carrinho pertence a uma loja.
- Um carrinho não mistura produtos de lojas diferentes.
- Cada pedido pertence a uma loja.
- O checkout revalida preços, disponibilidade e estoque.
- Os itens do pedido preservam os preços e as quantidades adquiridos.
- Permissões administrativas respeitam o escopo das lojas autorizadas.
- Transições inválidas de pedido devem ser rejeitadas.

### Decisões de Estoque

A implementação deverá definir os momentos de reserva, expiração, confirmação e liberação de estoque.

O tratamento de concorrência e de pagamentos concluídos após a expiração da reserva será documentado e testado.

## API

Os endpoints abaixo são propostas, não um contrato de API implementado:

| Método | Endpoint                             | Finalidade                       |
| ------ | ------------------------------------ | -------------------------------- |
| GET    | `/stores`                            | Listar lojas                     |
| GET    | `/stores/{storeId}`                  | Consultar uma loja               |
| GET    | `/stores/{storeId}/products`         | Consultar o catálogo da loja     |
| POST   | `/stores/{storeId}/orders`           | Realizar checkout e criar pedido |
| GET    | `/stores/{storeId}/orders/{orderId}` | Consultar um pedido autorizado   |
| POST   | `/webhooks/stripe`                   | Receber eventos de pagamento     |

Endpoints de autenticação, carrinho e administração serão definidos durante a implementação.

O webhook utilizará verificação de assinatura da Stripe em vez de autenticação de cliente. O acesso a pedidos exigirá titularidade ou permissão administrativa adequada.

O contrato definitivo será documentado com OpenAPI.

## Autenticação e Autorização

O Spring Security aplicará os controles de acesso.

Papéis iniciais propostos:

- `CUSTOMER`
- `STORE_MANAGER`
- `PLATFORM_ADMIN`

A autorização considerará o papel e o escopo do recurso. Um gerente não poderá acessar outra loja apenas alterando um identificador na requisição.

JWT está em consideração. Armazenamento, expiração, renovação e revogação de tokens ainda serão definidos.

## Pagamentos e Idempotência

A V1 aceitará pagamentos com cartão no ambiente de testes da Stripe.

O processamento considerará:

- Verificação de assinatura.
- Entregas duplicadas de webhooks.
- Processamento concorrente de eventos.
- Falhas e retentativas seguras.
- Eventos fora de ordem.
- Atualização consistente de pagamentos e pedidos.

Um evento não será considerado processado com sucesso apenas porque foi recebido.

Restrições no banco e limites transacionais protegerão contra processamento duplicado. A estratégia de registro dos eventos será documentada em um ADR.

