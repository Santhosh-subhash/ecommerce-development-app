# E-Commerce QA Development Project (Spring Boot + Oracle 11g)

This project is the existing E-Commerce development app updated to exercise Login, Registration, Search, Cart and Checkout test scenarios from the provided PDFs.

## Run in Eclipse
1. Extract the ZIP and import the `ecommerce-project` folder using **File > Import > Maven > Existing Maven Projects**.
2. Review `src/main/resources/application.properties` and set your Oracle JDBC URL, username and password.
3. Verify the existing Oracle tables and sequences match the entity mappings. The app uses `spring.jpa.hibernate.ddl-auto=validate`; it does not create or alter tables.
4. Right-click the project > **Maven > Update Project**; then **Project > Clean**.
5. Run `com.example.ecommerce.EcommerceProjectApplication` as a Java Application.
6. Open `http://localhost:8083/`.

Port **8083** is configured because 8082 was previously reported as occupied. Change `server.port` if that port is also in use.

## Documentation
See [`END_TO_END_QA_CASE_SUPPORT.md`](END_TO_END_QA_CASE_SUPPORT.md) for implemented behavior, the test-case scope, known limitations, Oracle notes and a smoke-test flow. The earlier [`LOGIN_TC01_TC30_IMPLEMENTATION.md`](LOGIN_TC01_TC30_IMPLEMENTATION.md) documents the first login-module update.

## Important
- Payments are simulated; no real payment gateway is connected. Never enter real card details or UPI PINs.
- Account lockout/status/expired-password features, coupons, billing-address separation, email notifications and several conditional UI features require schema/services not present in the provided project.
- A Maven build could not be run in the preparation environment because Maven was not installed. Validate the project in Eclipse against your own Oracle database before relying on it.
