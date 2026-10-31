# E-Commerce development update — end-to-end QA case support

This is an update to the existing Spring Boot + Oracle + HTML/CSS/JavaScript development project. It does **not** add Selenium test scripts; it adjusts the application so Selenium/manual tests can exercise the user journeys and validation paths.

## Included test-case documents

- Login: TC01–TC52 (52 cases)
- Registration: TC001–TC070 (70 cases)
- Search: TC01–TC51 (51 cases)
- Cart: TC01–TC54 (54 cases)
- Checkout: TC01–TC60 (60 cases)

**Total: 287 test cases across the five modules.** A number of cases are browser/performance checks or require database features that the current schema does not have, so they cannot be guaranteed to pass from application code alone. See the limitations section below.

## Main implementation changes

### Registration and login
- Server-side and browser-side validation for required fields, username length/allowed characters, email format, phone number format, password length and password complexity.
- Registration now includes Confirm Password and verifies that the two passwords match.
- Email and phone uniqueness checks; registration can use email or phone.
- Passwords remain BCrypt-hashed; responses do not return the password hash.
- Login accepts username, email, or phone; password fields are masked; submit buttons show a loading state and are disabled while a request is running.
- Error/success text uses `textContent`, preventing submitted HTML/script from being rendered as markup.
- Login/logout and session restore use the server session; protected cart, address, order, and payment endpoints require a session.

### Search
- Case-insensitive partial search across product name, description, brand, and numeric product ID (the database schema has no SKU column).
- Search input maximum of 100 characters, whitespace trimming, safe query parameter handling, and “No products found” feedback.
- Category, brand, minimum/maximum price filters and name/price sorting.
- Active products only; inactive/discontinued products are excluded from normal search results.
- Search result content is rendered as text, not HTML.

### Cart
- Persistent cart is stored server-side by logged-in user and survives refresh/logout/login/browser restart as long as the Oracle data and session/account remain available.
- Add, increase/decrease quantity, remove, clear/empty cart, multiple products, repeated add-to-cart, item count, unit price, line subtotal, and cart total.
- Server validates active product, stock, positive quantity, maximum quantity of 99 per product, and maximum of 50 distinct cart products.
- Cart endpoints reject requests without a logged-in session and constrain item IDs to the current user's active cart.

### Checkout and orders
- Checkout requires a logged-in user and non-empty cart; validates recipient name/phone and address fields before submission.
- Supports saved-address selection, new address entry, UPI, credit/debit card (mock only), net banking (mock only), and Cash on Delivery.
- Card input validates digit count, expiry format/date, and CVV format. A test card number ending in `0000` simulates a declined payment before an order is created. Do not enter real card data.
- Order summary calculates subtotal, 5% demo tax, zero shipping, and final total. The same calculation is made server-side.
- Order placement rechecks stock and address ownership, updates stock, stores order items, empties the cart, records a mock payment, and makes the order visible in My Orders.
- Place Order is disabled while submitting to prevent normal double-click duplicates from the UI.

### UI/UX and accessibility
- Semantic labels, accessible status areas, keyboard form submission, loading/button states, mobile layout adjustments, cart product images, quantity controls, and continue-shopping navigation.

## Cases that need more than this code

The PDFs include many optional/conditional or infrastructure-level tests. These are not represented as complete production features in this project:

1. **Login TC15–TC18 (inactive/locked/disabled/expired-password accounts), TC23 (account lockout), TC52 (Remember Me):** the existing USERS schema has no status, lockout counter/expiry, password-expiry, or remember-me fields. No schema was altered.
2. **Registration TC002/phone-only accounts:** phone login is supported. For compatibility with an Oracle schema that may require EMAIL NOT NULL, a phone-only account is stored with a non-routable `@local.invalid` placeholder email; this is not a real mailbox.
3. **Search SKU, autocomplete suggestions, keyword highlighting, and pagination:** no SKU column or existing suggestion/highlighting/pagination requirements are present in the schema/app. Numeric product IDs can be searched, but they are not SKUs.
4. **Cart discount/coupon (TC29), cart tax display (TC30):** there is no coupon/discount table or discount rules. Checkout applies a demo 5% tax and zero shipping; tax rules must be confirmed for a real business.
5. **Checkout saved billing address different from shipping, card gateway responses, payment gateway performance, and confirmation email:** there is no billing-address relationship, real payment provider, email service, or gateway credentials. Payments here are deliberately simulated.
6. **Recipient name/phone persistence:** these are validated on the checkout form, but the existing ADDRESSES schema/entity does not define recipient-name or recipient-phone columns. They are not persisted unless the database schema and entity are extended.
7. **Browser compatibility, actual responsive-device behavior, performance/load testing, simultaneous-user race conditions, and session-timeout timing:** require execution in real browsers and a configured environment; static source edits cannot prove these test results.
8. **Maximum product/cart limits:** the implementation uses an explicit testable limit of 99 units per product and 50 distinct products, also constrained by actual stock.

## Oracle and configuration notes

- JDBC URL and credentials remain in `src/main/resources/application.properties`; review them for your own local Oracle setup.
- App port is **8083** because 8082 was previously reported as occupied. Open `http://localhost:8083/` after starting the app.
- `spring.jpa.hibernate.ddl-auto=validate` does not create or alter tables. The existing Oracle tables and sequences must match the entity mappings, and database-managed timestamp columns need defaults/triggers where required.
- The mock payment controller writes to the existing `PAYMENTS` table. Its timestamp/default/trigger requirements must be satisfied by your Oracle schema.
- Maven was not available in the build environment used to prepare this ZIP, so a Maven compile/test run could not be performed here. JavaScript syntax (`node --check`) and `pom.xml` XML syntax were checked; run **Maven > Update Project**, then **Project > Clean**, and start the app in Eclipse to validate against your Oracle instance.

## Suggested manual smoke flow

1. Register with username `qa_user01`, email or phone, and a password such as `QaTest@123` (use a fresh username/email/phone for each run).
2. Log in; search a known active product; filter/sort results.
3. Add products, change quantities, remove an item, refresh, log out and log back in to verify cart persistence.
4. Proceed to checkout; enter a valid address and select COD or a mock payment method. For a successful mock card, use a non-real test number such as `4111111111111111`, future expiry like `12/30`, and fake CVV `123`. A card number ending in `0000` is the decline path.
5. Verify the confirmation message, My Orders history, stock decrement, and empty cart.

This is a learning/QA demo, not a production e-commerce or payment system.
