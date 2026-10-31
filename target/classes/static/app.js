// E-COMMERCE APPLICATION FRONTEND
// Java + Spring Boot + Oracle 11g

const API = {
    products: "/api/products",
    categories: "/api/categories",
    register: "/api/auth/register",
    login: "/api/auth/login",
    me: "/api/auth/me",
    logout: "/api/auth/logout",
    cart: "/api/cart",
    cartItems: "/api/cart/items",
    addresses: "/api/addresses",
    orders: "/api/orders",
    mockPayment: "/api/payments/mock"
};

const $ = (selector) => document.querySelector(selector);
const cartItems = new Map();

let currentUser = null;
let cartIsServerBacked = false;

function money(value) {
    return Number(value || 0).toLocaleString("en-IN", {
        style: "currency",
        currency: "INR"
    });
}

function showMessage(selector, message, kind = "") {
    const element = $(selector);
    if (!element) return;

    element.textContent = message;
    element.className = kind;
}

async function apiRequest(url, options = {}) {
    const response = await fetch(url, {
        credentials: "same-origin",
        ...options,
        headers: {
            ...(options.body
                ? { "Content-Type": "application/json" }
                : {}),
            ...(options.headers || {})
        }
    });

    const responseText = await response.text();
    let data = null;

    if (responseText) {
        try {
            data = JSON.parse(responseText);
        } catch {
            data = responseText;
        }
    }

    if (!response.ok) {
        const message =
            (data && typeof data === "object" &&
                (data.message || data.error ||
                 data.detail || data.title)) ||
            (typeof data === "string" && data) ||
            `HTTP ${response.status}`;

        throw new Error(message);
    }

    return data;
}

// NAVIGATION
function showSection(sectionId) {
    document.querySelectorAll(".page").forEach((section) => {
        section.hidden = section.id !== sectionId;
    });

    if (sectionId === "cartSection") loadCart();
    if (sectionId === "ordersSection") loadOrders();

    if (sectionId === "home") {
        loadProducts($("#q")?.value || "");
    }
}

// AUTHENTICATION UI
function updateAuthUI() {
    const userMessage = $("#userMessage");
    const loginNav = $("#loginNav");
    const logoutBtn = $("#logoutBtn");

    if (userMessage) {
        userMessage.textContent = currentUser
            ? `Welcome, ${currentUser.fullName ||
                currentUser.username || "Customer"}!`
            : "Welcome, Guest!";
    }

    if (loginNav) loginNav.hidden = Boolean(currentUser);
    if (logoutBtn) logoutBtn.hidden = !currentUser;
}

// RESTORE LOGIN SESSION
async function restoreSession() {
    try {
        const data = await apiRequest(API.me);
        currentUser = data?.user || data;

        updateAuthUI();
        await loadCart();
    } catch {
        currentUser = null;
        cartIsServerBacked = false;
        cartItems.clear();

        updateAuthUI();
        renderCart();
    }
}

// Populate the category filter when category data exists.
async function loadCategories() {
    const select = $("#categoryFilter");
    if (!select) return;
    try {
        const categories = await apiRequest(API.categories);
        if (!Array.isArray(categories)) return;
        categories.forEach((category) => select.add(new Option(category.categoryName || `Category ${category.categoryId}`, String(category.categoryId))));
    } catch (error) { console.warn("Category filter unavailable:", error.message); }
}

// PRODUCT CATALOG
async function loadProducts(query = "") {
    const grid = $("#products");
    if (!grid) return;
    const cleanQuery = String(query || "").trim();
    if (cleanQuery.length > 100) {
        showMessage("#message", "Search text must not exceed 100 characters.", "notice-error");
        return;
    }
    if (String(query || "").trim() === "" && String(query || "").length > 0 && !$("#brandFilter")?.value.trim()) {
        // Whitespace-only search is treated as an empty search; the catalogue remains usable.
    }
    grid.replaceChildren();
    showMessage("#message", "Loading products...");
    const params = new URLSearchParams();
    if (cleanQuery) params.set("q", cleanQuery);
    const brand = $("#brandFilter")?.value.trim() || "";
    const categoryId = $("#categoryFilter")?.value || "";
    const minPrice = $("#minPrice")?.value.trim() || "";
    const maxPrice = $("#maxPrice")?.value.trim() || "";
    const sort = $("#sortProducts")?.value || "name-asc";
    if (brand) params.set("brand", brand);
    if (categoryId) params.set("categoryId", categoryId);
    if (minPrice) {
        if (!Number.isFinite(Number(minPrice)) || Number(minPrice) < 0) {
            showMessage("#message", "Minimum price must be a non-negative number.", "notice-error"); return;
        }
        params.set("minPrice", minPrice);
    }
    if (maxPrice) {
        if (!Number.isFinite(Number(maxPrice)) || Number(maxPrice) < 0) {
            showMessage("#message", "Maximum price must be a non-negative number.", "notice-error"); return;
        }
        params.set("maxPrice", maxPrice);
    }
    if (minPrice && maxPrice && Number(minPrice) > Number(maxPrice)) {
        showMessage("#message", "Minimum price cannot exceed maximum price.", "notice-error"); return;
    }
    params.set("sort", sort);
    try {
        const products = await apiRequest(`${API.products}?${params.toString()}`);
        if (!Array.isArray(products)) throw new Error("Product API did not return a product list.");
        showMessage("#message", products.length ? `${products.length} product(s) found` : "No products found");
        products.forEach((product) => {
            const card = document.createElement("article"); card.className = "card product-card";
            if (product.imageUrl) {
                const image = document.createElement("img"); image.className = "product-image";
                image.src = product.imageUrl; image.alt = product.productName || "Product"; image.loading = "lazy";
                image.onerror = () => image.remove(); card.append(image);
            }
            const title = document.createElement("h3"); title.textContent = product.productName || "Unnamed product";
            const description = document.createElement("p"); description.textContent = product.description || "";
            const brandText = document.createElement("p"); brandText.className = "muted"; brandText.textContent = product.brand || "";
            const price = document.createElement("p"); price.className = "price"; price.textContent = money(product.price);
            const stock = Number(product.stockQuantity || 0);
            const stockText = document.createElement("p"); stockText.className = "muted"; stockText.textContent = stock > 0 ? `Stock: ${stock}` : "Out of stock";
            const button = document.createElement("button"); button.textContent = "Add to Cart";
            button.disabled = stock <= 0 || product.isActive === "N"; button.addEventListener("click", () => addToCart(product));
            card.append(title, description, brandText, price, stockText, button); grid.append(card);
        });
    } catch (error) { showMessage("#message", `Unable to load products: ${error.message}`, "notice-error"); }
}

// ADD TO CART — SAVE TO BACKEND
async function addToCart(product) {
    if (!currentUser) {
        showMessage(
            "#message",
            "Please log in before adding items to your cart.",
            "notice-error"
        );
        showSection("authSection");
        return;
    }

    if (!product || product.productId == null) {
        showMessage(
            "#message",
            "Invalid product. Please refresh the products.",
            "notice-error"
        );
        return;
    }

    try {
        await apiRequest(API.cartItems, {
            method: "POST",
            body: JSON.stringify({
                productId: Number(product.productId),
                quantity: 1
            })
        });

        await loadCart();

        showMessage(
            "#cartMessage",
            "Product added to your saved cart.",
            "notice-success"
        );
    } catch (error) {
        showMessage(
            "#message",
            `Unable to add item: ${error.message}`,
            "notice-error"
        );
    }
}
// LOAD SAVED CART
async function loadCart() {
    if (currentUser) {
        try {
            const data = await apiRequest(API.cart);
            const rows = Array.isArray(data) ? data : data?.items;

            if (Array.isArray(rows)) {
                cartIsServerBacked = true;
                cartItems.clear();

                rows.forEach((item) => {
                    const product = item.product || item;
                    const productId =
                        item.productId ?? product.productId;

                    if (productId == null) return;

                    cartItems.set(String(productId), {
                        product: {
                            ...product,
                            productId,
                            productName:
                                item.productName || product.productName,
                            price:
                                item.unitPrice ??
                                item.price ??
                                product.price,
                            stockQuantity:
                                item.stockQuantity ??
                                product.stockQuantity
                        },
                        quantity: Number(item.quantity || 1),
                        cartItemId: item.cartItemId
                    });
                });

                renderCart();
                return;
            }
        } catch (error) {
            cartIsServerBacked = false;

            showMessage(
                "#cartMessage",
                `Unable to load saved cart: ${error.message}`,
                "notice-error"
            );
        }
    }

    renderCart();
}

// RENDER CART
function renderCart() {
    const container = $("#cartItems");
    if (!container) return;

    container.replaceChildren();

    let total = 0;
    let count = 0;

    cartItems.forEach(({ product, quantity, cartItemId }, key) => {
        const lineTotal = Number(product.price || 0) * quantity;

        total += lineTotal;
        count += quantity;

        const row = document.createElement("div");
        row.className = "cart-row";

        const info = document.createElement("div");
        if (product.imageUrl) {
            const image = document.createElement("img"); image.className = "cart-product-image";
            image.src = product.imageUrl; image.alt = product.productName || "Product"; image.loading = "lazy";
            image.onerror = () => image.remove(); info.append(image);
        }

        const title = document.createElement("strong");
        title.textContent = product.productName || "Product";

        const unit = document.createElement("p");
        unit.className = "muted";
        unit.textContent = `${money(product.price)} each`;

        info.append(title, unit);

        const controls = document.createElement("div");
        controls.className = "quantity-controls";

        const minus = document.createElement("button");
        minus.textContent = "−";
        minus.type = "button";
        minus.addEventListener("click", () =>
            updateQuantity(key, quantity - 1, cartItemId)
        );

        const qty = document.createElement("span");
        qty.textContent = String(quantity);

        const plus = document.createElement("button");
        plus.textContent = "+";
        plus.type = "button";
        plus.addEventListener("click", () =>
            updateQuantity(key, quantity + 1, cartItemId)
        );

        controls.append(minus, qty, plus);

        const line = document.createElement("strong");
        line.className = "line-total";
        line.textContent = money(lineTotal);

        const remove = document.createElement("button");
        remove.className = "remove-button";
        remove.textContent = "Remove";
        remove.type = "button";
        remove.addEventListener("click", () =>
            removeFromCart(key, cartItemId)
        );

        row.append(info, controls, line, remove);
        container.append(row);
    });

    if (cartItems.size === 0) {
        const empty = document.createElement("p");
        empty.textContent = "Your cart is empty.";
        container.append(empty);
    }

    if ($("#cartTotal")) {
        $("#cartTotal").textContent = `Subtotal: ${money(total)}`;
    }

    if ($("#cartCount")) {
        $("#cartCount").textContent = String(count);
    }

    if ($("#checkoutBtn")) {
        $("#checkoutBtn").disabled = cartItems.size === 0;
    }
}

// UPDATE QUANTITY
async function updateQuantity(key, quantity, cartItemId) {
    const item = cartItems.get(String(key));
    if (!item) return;

    if (quantity <= 0) {
        await removeFromCart(key, cartItemId);
        return;
    }

    if (quantity > Number(item.product.stockQuantity)) {
        showMessage(
            "#cartMessage",
            "Requested quantity exceeds available stock.",
            "notice-error"
        );
        return;
    }

    try {
        if (cartIsServerBacked) {
            const id = cartItemId || item.cartItemId;

            if (!id) {
                throw new Error("Cart item ID is missing from the API response.");
            }

            await apiRequest(
                `${API.cartItems}/${encodeURIComponent(id)}`,
                {
                    method: "PUT",
                    body: JSON.stringify({ quantity })
                }
            );

            await loadCart();
        } else {
            item.quantity = quantity;
            cartItems.set(String(key), item);
            renderCart();
        }
    } catch (error) {
        showMessage(
            "#cartMessage",
            `Unable to update quantity: ${error.message}`,
            "notice-error"
        );
    }
}

// REMOVE CART ITEM
async function removeFromCart(key, cartItemId) {
    try {
        const item = cartItems.get(String(key));

        if (cartIsServerBacked) {
            const id = cartItemId || item?.cartItemId;

            if (!id) {
                throw new Error("Cart item ID is missing from the API response.");
            }

            await apiRequest(
                `${API.cartItems}/${encodeURIComponent(id)}`,
                { method: "DELETE" }
            );
        }

        cartItems.delete(String(key));

        if (cartIsServerBacked) {
            await loadCart();
        } else {
            renderCart();
        }

        showMessage(
            "#cartMessage",
            "Item removed from cart.",
            "notice-success"
        );
    } catch (error) {
        showMessage(
            "#cartMessage",
            `Unable to remove item: ${error.message}`,
            "notice-error"
        );
    }
}

// PRODUCT SEARCH
$("#search")?.addEventListener("click", () => {
    const raw = $("#q")?.value || "";
    if (!raw.trim() && !($("#brandFilter")?.value.trim()) && !($("#categoryFilter")?.value) && !($("#minPrice")?.value) && !($("#maxPrice")?.value)) {
        showMessage("#message", "Enter search text or choose a filter.", "notice-error");
        return;
    }
    loadProducts(raw);
});
$("#all")?.addEventListener("click", () => {
    if ($("#q")) $("#q").value = "";
    if ($("#brandFilter")) $("#brandFilter").value = "";
    if ($("#minPrice")) $("#minPrice").value = "";
    if ($("#maxPrice")) $("#maxPrice").value = "";
    if ($("#categoryFilter")) $("#categoryFilter").value = "";
    loadProducts("");
});
$("#clearSearch")?.addEventListener("click", () => {
    ["#q", "#brandFilter", "#minPrice", "#maxPrice", "#categoryFilter"].forEach((selector) => { if ($(selector)) $(selector).value = ""; });
    showMessage("#message", "Search filters cleared.");
    loadProducts("");
});
$("#q")?.addEventListener("keydown", (event) => { if (event.key === "Enter") { event.preventDefault(); $("#search")?.click(); } });
$("#sortProducts")?.addEventListener("change", () => loadProducts($("#q")?.value || ""));
$("#categoryFilter")?.addEventListener("change", () => loadProducts($("#q")?.value || ""));
$("#brandFilter")?.addEventListener("keydown", (event) => { if (event.key === "Enter") { event.preventDefault(); loadProducts($("#q")?.value || ""); } });

// REGISTRATION: client and server validations intentionally mirror each other.
$("#registerForm")?.addEventListener("submit", async (event) => {
    event.preventDefault();
    const fullName = $("#fullName")?.value.trim() || "";
    const email = $("#email")?.value.trim() || "";
    const phone = $("#phone")?.value.trim() || "";
    const username = $("#username")?.value.trim() || "";
    const password = $("#registerPassword")?.value || "";
    const confirmPassword = $("#confirmPassword")?.value || "";
    const button = $("#registerSubmit");
    const fail = (message, selector) => { showMessage("#registerMessage", message, "notice-error"); $(selector)?.focus(); };
    if (!fullName && !username && !email && !phone && !password && !confirmPassword) {
        showMessage("#registerMessage", "Full name, username, email or phone, password, and confirm password are required.", "notice-error");
        $("#fullName")?.focus(); return;
    }
    if (!fullName) return fail("Full name is required.", "#fullName");
    if (!username) return fail("Username is required.", "#username");
    if (username.length < 3) return fail("Username must contain at least 3 characters.", "#username");
    if (username.length > 50) return fail("Username must not exceed 50 characters.", "#username");
    if (!/^[A-Za-z0-9._-]+$/.test(username)) return fail("Username may contain only letters, numbers, dot, underscore, and hyphen.", "#username");
    if (!email && !phone) return fail("Email or phone number is required.", "#email");
    if (email && (!EMAIL_FORMAT.test(email) || email.length > 100)) return fail("Please enter a valid email address (maximum 100 characters).", "#email");
    if (phone && !/^[0-9]{10,15}$/.test(phone)) return fail("Phone number must contain 10 to 15 digits.", "#phone");
    if (!password) return fail("Password is required.", "#registerPassword");
    if (password.length < 8 || password.length > 72) return fail("Password must contain 8 to 72 characters.", "#registerPassword");
    if (!/[A-Z]/.test(password)) return fail("Password must contain at least one uppercase letter.", "#registerPassword");
    if (!/[a-z]/.test(password)) return fail("Password must contain at least one lowercase letter.", "#registerPassword");
    if (!/[0-9]/.test(password)) return fail("Password must contain at least one number.", "#registerPassword");
    if (!/[^A-Za-z0-9]/.test(password)) return fail("Password must contain at least one special character.", "#registerPassword");
    if (!confirmPassword) return fail("Confirm Password is required.", "#confirmPassword");
    if (password !== confirmPassword) return fail("Passwords do not match.", "#confirmPassword");
    if (button) { button.disabled = true; button.textContent = "Registering..."; }
    try {
        await apiRequest(API.register, { method: "POST", body: JSON.stringify({ fullName, email, phone, username, password, confirmPassword }) });
        showMessage("#registerMessage", "Registration successful. Please log in.", "notice-success");
        $("#registerForm").reset();
    } catch (error) { showMessage("#registerMessage", `Registration failed: ${error.message}`, "notice-error"); }
    finally { if (button) { button.disabled = false; button.textContent = "Register"; } }
});

// LOGIN - validation is performed here and again by the backend.
const EMAIL_FORMAT = /^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+$/;

$("#loginForm")?.addEventListener("submit", async (event) => {
    event.preventDefault();

    const username = $("#loginUsername")?.value ?? "";
    const password = $("#loginPassword")?.value ?? "";
    const identifier = username.trim();
    const submitButton = $("#loginSubmit");

    // Keep field-specific messages visible for Selenium and assistive technology.
    if (!identifier && !password.trim()) {
        showMessage("#loginMessage", "Username/email and password are required.", "notice-error");
        $("#loginUsername")?.focus();
        return;
    }
    if (!identifier) {
        showMessage("#loginMessage", "Username/email is required.", "notice-error");
        $("#loginUsername")?.focus();
        return;
    }
    if (!password.trim()) {
        showMessage("#loginMessage", "Password is required.", "notice-error");
        $("#loginPassword")?.focus();
        return;
    }
    if (identifier.length > 100) {
        showMessage("#loginMessage", "Username/email must not exceed 100 characters.", "notice-error");
        return;
    }
    if (identifier.includes("@") && !EMAIL_FORMAT.test(identifier)) {
        showMessage("#loginMessage", "Invalid email format.", "notice-error");
        $("#loginUsername")?.focus();
        return;
    }
    if (!identifier.includes("@") && identifier.length < 3) {
        showMessage("#loginMessage", "Username must contain at least 3 characters.", "notice-error");
        $("#loginUsername")?.focus();
        return;
    }
    if (!identifier.includes("@") && identifier.length > 50) {
        showMessage("#loginMessage", "Username must not exceed 50 characters.", "notice-error");
        $("#loginUsername")?.focus();
        return;
    }
    if (password.length < 8) {
        showMessage("#loginMessage", "Password must contain at least 8 characters.", "notice-error");
        $("#loginPassword")?.focus();
        return;
    }
    if (password.length > 72) {
        showMessage("#loginMessage", "Password must not exceed 72 characters.", "notice-error");
        $("#loginPassword")?.focus();
        return;
    }

    if (submitButton) {
        submitButton.disabled = true;
        submitButton.textContent = "Signing in...";
    }
    showMessage("#loginMessage", "Signing in...", "");

    try {
        const result = await apiRequest(API.login, {
            method: "POST",
            body: JSON.stringify({ username: identifier, password })
        });

        currentUser = result?.user || result;
        updateAuthUI();
        $("#loginForm").reset();
        showMessage("#loginMessage", "Login successful.", "notice-success");
        await loadCart();
        showSection("home");
    } catch (error) {
        // showMessage uses textContent, so response text is never interpreted as HTML.
        showMessage("#loginMessage", `Login failed: ${error.message}`, "notice-error");
    } finally {
        if (submitButton) {
            submitButton.disabled = false;
            submitButton.textContent = "Login";
        }
    }
});

// LOGOUT
async function logout() {
    try {
        await apiRequest(API.logout, { method: "POST" });
    } catch (error) {
        console.warn("Backend logout failed:", error.message);
    }

    currentUser = null;
    cartIsServerBacked = false;
    cartItems.clear();

    updateAuthUI();
    renderCart();
    showSection("home");
}

// Connect logout button
$("#logoutBtn")?.addEventListener("click", logout);
// CHECKOUT BUTTON
$("#checkoutBtn")?.addEventListener("click", () => {
    if (!currentUser) {
        showMessage(
            "#cartMessage",
            "Please log in before checkout.",
            "notice-error"
        );
        showSection("authSection");
        return;
    }

    if (cartItems.size === 0) {
        showMessage(
            "#cartMessage",
            "Your cart is empty.",
            "notice-error"
        );
        return;
    }

    showSection("checkoutSection");
    renderCheckoutSummary();
    loadSavedAddresses();
});

function renderCheckoutSummary() {
    const box = $("#checkoutSummary");
    if (!box) return;
    box.replaceChildren();
    let subtotal = 0;
    cartItems.forEach(({ product, quantity }) => {
        const line = Number(product.price || 0) * quantity; subtotal += line;
        const row = document.createElement("p"); row.textContent = `${product.productName || "Product"} × ${quantity}: ${money(line)}`; box.append(row);
    });
    const tax = Math.round(subtotal * 5) / 100;
    const totals = document.createElement("p"); totals.textContent = `Subtotal: ${money(subtotal)} | Tax (5%): ${money(tax)} | Shipping: ${money(0)} | Total: ${money(subtotal + tax)}`; box.append(totals);
}

async function loadSavedAddresses() {
    const select = $("#savedAddress");
    if (!select || !currentUser) return;
    try {
        const rows = await apiRequest(API.addresses);
        if (!Array.isArray(rows)) return;
        select.replaceChildren(new Option("Enter a new address", ""));
        rows.forEach((address) => {
            const option = new Option([address.addressLine1, address.city, address.state, address.postalCode].filter(Boolean).join(", "), String(address.addressId));
            option.dataset.address = JSON.stringify(address); select.add(option);
        });
    } catch (error) { console.warn("Saved addresses unavailable:", error.message); }
}
$("#savedAddress")?.addEventListener("change", (event) => {
    const option = event.target.selectedOptions[0];
    if (!option?.value) return;
    try {
        const a = JSON.parse(option.dataset.address || "{}");
        $("#addressLine1").value = a.addressLine1 || ""; $("#addressLine2").value = a.addressLine2 || "";
        $("#city").value = a.city || ""; $("#state").value = a.state || ""; $("#postalCode").value = a.postalCode || ""; $("#country").value = a.country || "India";
    } catch { /* ignore malformed option */ }
});

document.querySelectorAll('input[name="paymentMethod"]').forEach((radio) => radio.addEventListener("change", () => {
    const card = radio.value === "CREDIT_CARD" || radio.value === "DEBIT_CARD";
    if (radio.checked && $("#cardFields")) $("#cardFields").hidden = !card;
}));
$("#continueShopping")?.addEventListener("click", () => showSection("home"));

// PAYMENT AND ORDER PLACEMENT
$("#checkoutForm")?.addEventListener("submit", async (event) => {
    event.preventDefault();
    if (!currentUser) { showMessage("#checkoutMessage", "Please log in before placing an order.", "notice-error"); showSection("authSection"); return; }
    if (cartItems.size === 0) { showMessage("#checkoutMessage", "Your cart is empty.", "notice-error"); return; }
    const method = document.querySelector('input[name="paymentMethod"]:checked')?.value || "";
    const name = $("#recipientName")?.value.trim() || "";
    const phone = $("#recipientPhone")?.value.trim() || "";
    const address = {
        addressLine1: $("#addressLine1")?.value.trim() || "", addressLine2: $("#addressLine2")?.value.trim() || "",
        city: $("#city")?.value.trim() || "", state: $("#state")?.value.trim() || "",
        postalCode: $("#postalCode")?.value.trim() || "", country: $("#country")?.value.trim() || "India",
        addressType: "HOME", isDefault: "Y"
    };
    const fail = (message, selector) => { showMessage("#checkoutMessage", message, "notice-error"); $(selector)?.focus(); };
    if (!name) return fail("Recipient name is required.", "#recipientName");
    if (!/^[0-9]{10,15}$/.test(phone)) return fail("Phone number must contain 10 to 15 digits.", "#recipientPhone");
    if (address.addressLine1.length < 5) return fail("Address must contain at least 5 characters.", "#addressLine1");
    if (address.addressLine1.length > 200 || address.addressLine2.length > 200) return fail("Address must not exceed 200 characters.", "#addressLine1");
    if (!address.city) return fail("City is required.", "#city");
    if (!address.state) return fail("State is required.", "#state");
    if (!/^[0-9]{4,10}$/.test(address.postalCode)) return fail("Postal code must contain 4 to 10 digits.", "#postalCode");
    if (!address.country) return fail("Country is required.", "#country");
    if (!method) return fail("Please select a payment method.", 'input[name="paymentMethod"]');
    const paymentData = { paymentMethod: method, cardNumber: $("#cardNumber")?.value || "", expiry: $("#cardExpiry")?.value || "", cvv: $("#cardCvv")?.value || "" };
    const button = $("#placeOrderButton");
    if (button) { button.disabled = true; button.textContent = "Processing..."; }
    showMessage("#checkoutMessage", "Validating checkout details...");
    try {
        // Preflight mock payment before creating the order, so a simulated decline cannot leave an order behind.
        await apiRequest("/api/payments/mock/validate", { method: "POST", body: JSON.stringify(paymentData) });
        const addressResult = await apiRequest(API.addresses, { method: "POST", body: JSON.stringify(address) });
        const addressId = addressResult?.addressId ?? addressResult?.id;
        if (!addressId) throw new Error("Address API did not return addressId.");
        const order = await apiRequest(API.orders, { method: "POST", body: JSON.stringify({ addressId: Number(addressId) }) });
        const orderId = order?.orderId ?? order?.id;
        if (orderId == null) throw new Error("Order API did not return an order ID.");
        await apiRequest(API.mockPayment, { method: "POST", body: JSON.stringify({ orderId: Number(orderId), paymentMethod: method }) });
        await loadCart();
        $("#checkoutForm").reset();
        if ($("#cardFields")) $("#cardFields").hidden = true;
        showMessage("#checkoutMessage", `Order placed successfully. Order ID: ${orderId}. Total: ${money(order.totalAmount)}.`, "notice-success");
        showSection("ordersSection");
    } catch (error) {
        showMessage("#checkoutMessage", `Checkout failed: ${error.message}`, "notice-error");
    } finally { if (button) { button.disabled = false; button.textContent = "Place Order"; } }
});

// MY ORDERS
	async function loadOrders() {
	    const list = $("#ordersList");
	    if (!list) return;

	    list.replaceChildren();

	    if (!currentUser) {
	        showMessage(
	            "#ordersMessage",
	            "Please log in to view your orders.",
	            "notice-error"
	        );
	        return;
	    }

	    showMessage("#ordersMessage", "Loading orders...");

	    try {
	        const result = await apiRequest(API.orders);

	        const orders = Array.isArray(result)
	            ? result
	            : result?.orders;

	        if (!Array.isArray(orders)) {
	            throw new Error("Orders API did not return an order list.");
	        }

	        showMessage(
	            "#ordersMessage",
	            orders.length ? "" : "No orders found."
	        );

	        orders.forEach((order) => {
	            const card = document.createElement("article");
	            card.className = "order-card";

	            const heading = document.createElement("h3");
	            heading.textContent = `Order #${order.orderId}`;

	            const status = document.createElement("span");
	            status.className = "status";
	            status.textContent = order.status || "Status unavailable";

	            const total = document.createElement("p");
	            total.textContent = `Total: ${money(order.totalAmount)}`;

	            const date = document.createElement("p");
	            date.className = "muted";
	            date.textContent =
	                `Date: ${order.orderDate || "Not provided"}`;

	            card.append(heading, status, total, date);
	            list.append(card);
	        });
	    } catch (error) {
	        showMessage(
	            "#ordersMessage",
	            `Unable to load orders: ${error.message}`,
	            "notice-error"
	        );
	    }
	}

	// APPLICATION STARTUP
	document.addEventListener("DOMContentLoaded", async () => {
	    updateAuthUI();
	    renderCart();

	    await restoreSession();
	    await loadProducts();
	});