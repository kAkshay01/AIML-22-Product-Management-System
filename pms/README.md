# Product Management System

Spring Boot 4 / Java 17 inventory app with two account types — **Customers** who browse
the catalog and buy via a cart, and **Suppliers** who list and manage their own products.

## What's new in this version

- **Checkout & Payment methods**:
  - `/customer/checkout` — order summary pulled from the cart, delivery address, and a
    choice of **Cash on Delivery**, **Card**, or **UPI**.
  - `CustomerOrder` / `OrderItem` snapshot the product name/price at purchase time, so
    an order stays accurate even if a supplier later edits or deletes that product.
  - Placing an order decrements stock, clears the cart, and redirects to an order
    confirmation page; `/customer/orders` lists order history, `/customer/orders/{id}`
    shows one order's detail.
  - **This is a simulated payment flow, not a real payment gateway.** For Card, only
    the last 4 digits are ever read from the input (to show "Card ending in ••••") —
    the full number is discarded immediately and never persisted, and the app never
    even collects a CVV field. Don't point this at real card numbers; wire up a real
    provider (Stripe, Razorpay, etc.) before handling real payments.

## Earlier addition: cart

- **Cart & Add to Cart** for customers:
  - `Product` → `CartItem` → `Cart` (one cart per customer, `@OneToOne`)
  - Customer dashboard (`/customer/dashboard`) has a quantity field + **Add to Cart**
    button on every in-stock product; search stays intact through the redirect.
  - `/customer/cart` shows line items, per-row quantity update, remove, clear cart,
    and a running total.
  - Stock-aware: quantity is capped to the product's available stock both when adding
    and when updating.
- `CartService` / `CartServiceImpl` and `CartController` logic (in `CustomerController`)
  handle all of it, backed by `CartRepository` / `CartItemRepository`.

## Project structure

```
src/main/java/com/example/pms/
  model/        Product, User, Role, Cart, CartItem
  repository/   ProductRepository, UserRepository, CartRepository, CartItemRepository
  service/      ProductService, UserService, CartService (+ impls)
  controller/   HomeController, AuthController, CategoryController,
                ProductController (/products — supplier-managed catalog),
                CustomerController (/customer/** — browsing + cart),
                SupplierController (/supplier/** — a supplier's own listings)
  config/       SecurityConfig, LoginSuccessHandler
src/main/resources/
  templates/    all your existing Thymeleaf pages, unchanged
  static/css/   style.css, unchanged
  application.properties
```

## Routes

| Route | Who | What |
|---|---|---|
| `/` | anyone (logged in) | Home |
| `/products`, `/products/new`, `/products/edit/**` etc. | view: any logged-in user · add/edit/delete: **SUPPLIER** | Generic catalog admin |
| `/customer/dashboard` | **CUSTOMER** | Browse + search + Add to Cart |
| `/customer/cart` | **CUSTOMER** | View/update/remove/clear cart |
| `/customer/checkout`, `/customer/checkout/place` | **CUSTOMER** | Delivery address + payment method, place order |
| `/customer/orders`, `/customer/orders/{id}` | **CUSTOMER** | Order history and order detail |
| `/supplier/dashboard`, `/supplier/products/**` | **SUPPLIER** | Manage only your own listings |
| `/categories` | any logged-in user | Distinct categories in the catalog |
| `/login`, `/register` | public | Auth |

Every product created through either "Add Product" flow is stamped with the
logged-in supplier as its owner, so it shows up correctly on that supplier's
own dashboard as well as the shared catalog.

## Running it

1. Make sure MySQL is running locally and matches `application.properties`
   (`productdb`, created automatically; update the username/password there if yours differ).
2. From the project root:
   ```
   mvn clean install
   mvn spring-boot:run
   ```
3. Visit `http://localhost:8080`, register a Customer and a Supplier account, and try it out.

I couldn't run `mvn compile` in this sandbox (no Maven/network access here), so please
do a clean build on your end before running — the code follows standard Spring Boot 4 /
Spring Security 6 / Java 17 patterns, but do give it a look over.

## Notes / things you may want to tighten later

- `/products/edit/**`, `/products/update/**`, and `/products/delete/**` are now
  **SUPPLIER**-only (previously any logged-in user could hit them) — this was a gap
  flagged earlier and closed here for consistency with the new cart/role split.
  If you specifically want customers to retain admin-style edit/delete on the shared
  catalog, loosen that line in `SecurityConfig`.
- Cart/product ownership checks throw `AccessDeniedException` on mismatch, which
  Spring Security turns into a default 403 page — add a custom error page if you'd
  like nicer messaging.
- `spring.jpa.hibernate.ddl-auto=update` will auto-create the new `carts` and
  `cart_items` tables the first time you run it.
