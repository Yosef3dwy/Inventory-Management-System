const state = {
  view: "dashboard",
  auth: JSON.parse(sessionStorage.getItem("inventoryAuth") || "null"),
  products: [],
  customers: [],
  suppliers: [],
  warehouses: [],
  orders: [],
  supplierProducts: [],
  supplierSales: []
};

const titles = {
  dashboard: ["Dashboard", "Role-based overview from the authenticated backend."],
  products: ["Products", "Catalog records from the product API."],
  customers: ["Customers", "Admin customer management."],
  suppliers: ["Suppliers", "Supplier records and supplied product creation."],
  carts: ["Carts", "Customer cart operations."],
  orders: ["Orders", "Checkout, customer history, and admin status updates."],
  inventory: ["Inventory", "Warehouse capacity plus reserve and restock actions."]
};

const roleViews = {
  ADMIN: ["dashboard", "products", "customers", "suppliers", "carts", "orders", "inventory"],
  CUSTOMER: ["dashboard", "products", "carts", "orders"],
  SUPPLIER: ["dashboard", "products", "suppliers"]
};

const $ = (selector, root = document) => root.querySelector(selector);
const $$ = (selector, root = document) => Array.from(root.querySelectorAll(selector));

function showNotice(message, isError = false) {
  const notice = $("#notice");
  notice.textContent = message;
  notice.className = `notice show${isError ? " error" : ""}`;
}

function money(value) {
  return Number(value || 0).toLocaleString(undefined, { style: "currency", currency: "USD" });
}

function dateText(value) {
  if (!value) return "";
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? String(value) : date.toLocaleString();
}

function formData(form) {
  return Object.fromEntries(new FormData(form).entries());
}

function numberOrNull(value) {
  return value === "" || value == null ? null : Number(value);
}

async function api(path, options = {}) {
  const headers = {
    "Content-Type": "application/json",
    ...(state.auth?.token ? { Authorization: `Bearer ${state.auth.token}` } : {}),
    ...(options.headers || {})
  };
  const response = await fetch(path, { ...options, headers });
  if (response.status === 204) return null;

  const text = await response.text();
  const payload = text ? tryJson(text) : null;
  if (!response.ok) {
    const message = payload?.message || payload?.error || text || `Request failed with ${response.status}`;
    throw new Error(message);
  }
  return payload;
}

function tryJson(text) {
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

function setBusy(button, busy) {
  if (button) button.disabled = busy;
}

async function run(button, action) {
  setBusy(button, true);
  try {
    await action();
  } catch (error) {
    showNotice(error.message, true);
  } finally {
    setBusy(button, false);
  }
}

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function emptyRow(columns) {
  return `<tr><td colspan="${columns}" class="muted">No data loaded.</td></tr>`;
}

function fillForm(form, values) {
  Object.entries(values).forEach(([key, value]) => {
    const input = form.elements[key];
    if (input) input.value = value ?? "";
  });
}

function selectView(view) {
  if (!roleViews[state.auth?.role]?.includes(view)) return;
  state.view = view;
  $$(".nav-item").forEach((button) => button.classList.toggle("active", button.dataset.view === view));
  $$(".view").forEach((section) => section.classList.toggle("active", section.id === view));
  $("#pageTitle").textContent = titles[view][0];
  $("#pageSubtitle").textContent = titles[view][1];
}

function applyAuthUi() {
  const loggedIn = Boolean(state.auth?.token);
  $("#loginPanel").classList.toggle("hidden", loggedIn);
  $(".shell").classList.toggle("app-hidden", !loggedIn);
  if (!loggedIn) return;

  $("#userPill").textContent = `${state.auth.name} | ${state.auth.role}`;
  $$(".nav-item").forEach((button) => {
    button.hidden = !button.dataset.roles.split(",").includes(state.auth.role);
  });

  $("#productForm").closest(".panel").hidden = state.auth.role !== "ADMIN";
  $("#deleteProduct").hidden = state.auth.role !== "ADMIN";
  $("#customerForm").closest(".panel").hidden = state.auth.role !== "ADMIN";
  $("#deleteSupplier").hidden = state.auth.role !== "ADMIN";
  $("#loadSuppliers").hidden = state.auth.role !== "ADMIN";
  $("#findSupplierByEmail").hidden = state.auth.role !== "ADMIN";
  $("#supplierEmailLookup").hidden = state.auth.role !== "ADMIN";
  $("#supplierDashboardPanel").hidden = state.auth.role !== "SUPPLIER";
  $("#orderStatusForm").hidden = state.auth.role !== "ADMIN";
  $("#deleteOrder").hidden = state.auth.role !== "ADMIN";
  $("#orderHistoryForm").hidden = state.auth.role === "CUSTOMER";
  $("#checkoutForm input[name='customerId']").closest("label").hidden = state.auth.role === "CUSTOMER";
  $("#cartLoadForm").hidden = state.auth.role === "CUSTOMER";
  $("#cartActionForm input[name='customerId']").closest("label").hidden = state.auth.role === "CUSTOMER";
  $("#supplierProductForm input[name='supplierId']").closest("label").hidden = state.auth.role === "SUPPLIER";

  if (state.auth.role === "SUPPLIER") {
    fillForm($("#supplierProductForm"), { supplierId: state.auth.userId });
    fillForm($("#supplierForm"), { id: state.auth.userId, password: "" });
  }
  if (!roleViews[state.auth.role].includes(state.view)) selectView("dashboard");
}

function statusClass(status) {
  const value = String(status || "").toLowerCase();
  if (value === "pending") return "pending";
  if (value === "delivered") return "delivered";
  return "";
}

function renderProducts() {
  const keyword = $("#productSearch").value.trim().toLowerCase();
  const products = state.products.filter((product) => String(product.title || "").toLowerCase().includes(keyword));
  $("#productsTable").innerHTML = products.map((product) => `
    <tr>
      <td>${product.productId ?? ""}</td>
      <td><strong>${escapeHtml(product.title)}</strong></td>
      <td>${product.size ?? ""}</td>
      <td>${money(product.price)}</td>
      <td>${escapeHtml(product.description)}</td>
    </tr>
  `).join("") || emptyRow(5);
}

function renderCustomers(rows = state.customers) {
  $("#customersTable").innerHTML = rows.map((customer) => `
    <tr>
      <td>${customer.customerId ?? ""}</td>
      <td><strong>${escapeHtml(customer.name)}</strong></td>
      <td>${escapeHtml(customer.email)}</td>
      <td>${escapeHtml(customer.phone)}</td>
      <td>${escapeHtml(customer.address)}</td>
      <td class="actions"><button type="button" data-fill-customer="${customer.customerId}">Use</button></td>
    </tr>
  `).join("") || emptyRow(6);
}

function renderSuppliers(rows = state.suppliers) {
  $("#suppliersTable").innerHTML = rows.map((supplier) => `
    <tr>
      <td>${supplier.supplierId ?? ""}</td>
      <td><strong>${escapeHtml(supplier.name)}</strong></td>
      <td>${escapeHtml(supplier.email)}</td>
      <td>${escapeHtml(supplier.phone)}</td>
      <td class="actions"><button type="button" data-fill-supplier="${supplier.supplierId}">Use</button></td>
    </tr>
  `).join("") || emptyRow(5);
}

function renderCart(cart) {
  $("#cartSummary").textContent = cart
    ? `Cart ${cart.cartId ?? ""} for customer ${cart.customerId ?? ""}. Total: ${money(cart.cartTotal)}`
    : "";
  const items = cart?.items || [];
  $("#cartItemsTable").innerHTML = items.map((item) => `
    <tr>
      <td>${item.cartItemId ?? ""}</td>
      <td>${item.productId ?? ""}</td>
      <td><strong>${escapeHtml(item.productTitle)}</strong></td>
      <td>${item.quantity ?? ""}</td>
      <td>${money(item.unitPrice)}</td>
      <td>${money(item.subTotal)}</td>
    </tr>
  `).join("") || emptyRow(6);
}

function renderOrders(orders = state.orders) {
  $("#ordersTable").innerHTML = orders.map((order) => `
    <tr>
      <td>${order.orderId ?? ""}</td>
      <td>${order.customerId ?? ""}</td>
      <td><span class="status ${statusClass(order.status)}">${escapeHtml(order.status)}</span></td>
      <td>${dateText(order.orderDate)}</td>
      <td>${dateText(order.deliveredDate)}</td>
      <td>${money(order.orderTotal)}</td>
      <td>${(order.items || []).map((item) => `${escapeHtml(item.productTitle)} x ${item.quantity}`).join("<br>")}</td>
    </tr>
  `).join("") || emptyRow(7);
}

function renderWarehouses() {
  $("#warehouseCards").innerHTML = state.warehouses.map((warehouse) => {
    const total = Number(warehouse.totalCapacity || 0);
    const free = Number(warehouse.freeSpace || 0);
    const usedPercent = total > 0 ? Math.max(0, Math.min(100, ((total - free) / total) * 100)) : 0;
    return `
      <article class="warehouse-card">
        <strong>${escapeHtml(warehouse.location)}</strong>
        <span class="muted">ID ${warehouse.warehouseId ?? ""}</span>
        <div class="capacity" aria-label="Used capacity"><span style="width:${usedPercent}%"></span></div>
        <small class="muted">${total - free} used / ${total} total</small>
      </article>
    `;
  }).join("");
  $("#warehousesTable").innerHTML = state.warehouses.map((warehouse) => `
    <tr>
      <td>${warehouse.warehouseId ?? ""}</td>
      <td><strong>${escapeHtml(warehouse.location)}</strong></td>
      <td>${warehouse.totalCapacity ?? ""}</td>
      <td>${warehouse.freeSpace ?? ""}</td>
      <td class="actions">
        <button type="button" data-clear-warehouse="${warehouse.warehouseId}">Clear</button>
        <button class="danger" type="button" data-delete-warehouse="${warehouse.warehouseId}">Delete</button>
      </td>
    </tr>
  `).join("") || emptyRow(5);
}

function renderSupplierDashboard() {
  $("#supplierProductsTable").innerHTML = state.supplierProducts.map((item) => `
    <tr>
      <td>${escapeHtml(item.productTitle)} <span class="muted">#${item.productId}</span></td>
      <td>${money(item.cost)}</td>
      <td class="actions"><button type="button" data-fill-supplier-product="${item.productId}">Use</button></td>
    </tr>
  `).join("") || emptyRow(3);
  $("#supplierSalesTable").innerHTML = state.supplierSales.map((item) => `
    <tr>
      <td>${escapeHtml(item.productTitle)}</td>
      <td>${item.quantitySold}</td>
      <td>${money(item.revenue)}</td>
    </tr>
  `).join("") || emptyRow(3);
}

function renderDashboard() {
  const metrics = [];
  if (state.auth.role === "ADMIN") {
    metrics.push(["Products", state.products.length], ["Customers", state.customers.length], ["Suppliers", state.suppliers.length], ["Orders", state.orders.length], ["Warehouses", state.warehouses.length]);
  }
  if (state.auth.role === "CUSTOMER") {
    metrics.push(["Available products", state.products.length], ["My orders", state.orders.length]);
  }
  if (state.auth.role === "SUPPLIER") {
    const revenue = state.supplierSales.reduce((sum, item) => sum + Number(item.revenue || 0), 0);
    metrics.push(["My products", state.supplierProducts.length], ["Quantity sold", state.supplierSales.reduce((sum, item) => sum + Number(item.quantitySold || 0), 0)], ["Revenue", money(revenue)]);
  }
  $("#dashboardHeading").textContent = `${state.auth.role[0]}${state.auth.role.slice(1).toLowerCase()} Dashboard`;
  $("#dashboardSubtitle").textContent = state.auth.email;
  $("#dashboardMetrics").innerHTML = metrics.map(([label, value]) => `
    <article class="metric">
      <strong>${escapeHtml(value)}</strong>
      <span>${escapeHtml(label)}</span>
    </article>
  `).join("");
}

async function loadProducts() {
  state.products = await api("/api/products");
  renderProducts();
}

async function loadCustomers() {
  state.customers = await api("/api/customers");
  renderCustomers();
}

async function loadSuppliers() {
  if (state.auth.role === "SUPPLIER") {
    const supplier = await api(`/api/suppliers/${state.auth.userId}`);
    state.suppliers = [supplier];
    renderSuppliers(state.suppliers);
    fillForm($("#supplierForm"), { id: supplier.supplierId, ...supplier, password: "" });
    return;
  }
  state.suppliers = await api("/api/suppliers");
  renderSuppliers();
}

async function loadWarehouses() {
  state.warehouses = await api("/api/inventory/warehouses");
  renderWarehouses();
}

async function loadOrders() {
  state.orders = state.auth.role === "ADMIN"
    ? await api("/api/orders")
    : await api(`/api/orders/customer/${state.auth.userId}`);
  renderOrders();
}

async function loadCart() {
  renderCart(await api(`/api/carts/${state.auth.userId}`));
}

async function loadSupplierDashboard() {
  state.supplierProducts = await api(`/api/suppliers/${state.auth.userId}/supplies`);
  state.supplierSales = await api(`/api/suppliers/${state.auth.userId}/sales`);
  renderSupplierDashboard();
}

async function loadDashboard() {
  if (state.auth.role === "ADMIN") {
    await Promise.all([loadProducts(), loadCustomers(), loadSuppliers(), loadWarehouses(), loadOrders()]);
  }
  if (state.auth.role === "CUSTOMER") {
    await Promise.all([loadProducts(), loadOrders(), loadCart()]);
  }
  if (state.auth.role === "SUPPLIER") {
    await Promise.all([loadProducts(), loadSuppliers(), loadSupplierDashboard()]);
  }
  renderDashboard();
  showNotice("Dashboard loaded.");
}

function bindEvents() {
  $("#loginForm").addEventListener("submit", (event) => {
    event.preventDefault();
    run(event.submitter, async () => {
      state.auth = await api("/api/auth/login", {
        method: "POST",
        body: JSON.stringify(formData(event.currentTarget))
      });
      sessionStorage.setItem("inventoryAuth", JSON.stringify(state.auth));
      state.view = "dashboard";
      applyAuthUi();
      selectView("dashboard");
      await loadDashboard();
      showNotice(`Logged in as ${state.auth.role}.`);
    });
  });

  $("#logoutButton").addEventListener("click", () => {
    sessionStorage.removeItem("inventoryAuth");
    state.auth = null;
    location.reload();
  });

  $$(".nav-item").forEach((button) => button.addEventListener("click", () => selectView(button.dataset.view)));

  $("#refreshButton").addEventListener("click", (event) => run(event.currentTarget, async () => {
    if (state.view === "dashboard") await loadDashboard();
    if (state.view === "products") await loadProducts();
    if (state.view === "customers") await loadCustomers();
    if (state.view === "suppliers") {
      await loadSuppliers();
      if (state.auth.role === "SUPPLIER") await loadSupplierDashboard();
    }
    if (state.view === "inventory") await loadWarehouses();
    if (state.view === "carts") state.auth.role === "CUSTOMER" ? await loadCart() : showNotice("Enter a customer ID to load a cart.");
    if (state.view === "orders") await loadOrders();
  }));

  $("#loadProducts").addEventListener("click", (event) => run(event.currentTarget, async () => {
    await loadProducts();
    showNotice("Products loaded.");
  }));
  $("#productSearch").addEventListener("input", renderProducts);

  $("#productForm").addEventListener("submit", (event) => {
    event.preventDefault();
    run(event.submitter, async () => {
      const data = formData(event.currentTarget);
      const id = numberOrNull(data.id);
      await api(id ? `/api/products/${id}` : "/api/products", {
        method: id ? "PUT" : "POST",
        body: JSON.stringify({ title: data.title, size: Number(data.size), description: data.description, price: Number(data.price) })
      });
      await loadProducts();
      showNotice(id ? "Product updated." : "Product created.");
    });
  });
  $("#deleteProduct").addEventListener("click", (event) => run(event.currentTarget, async () => {
    const id = numberOrNull($("#productForm").elements.id.value);
    if (!id) throw new Error("Enter a product ID to delete.");
    await api(`/api/products/${id}`, { method: "DELETE" });
    await loadProducts();
    showNotice("Product deleted.");
  }));

  $("#loadCustomers").addEventListener("click", (event) => run(event.currentTarget, loadCustomers));
  $("#findCustomerByEmail").addEventListener("click", (event) => run(event.currentTarget, async () => {
    const email = $("#customerEmailLookup").value.trim();
    if (!email) throw new Error("Enter a customer email.");
    renderCustomers([await api(`/api/customers/email/${encodeURIComponent(email)}`)]);
    showNotice("Customer loaded.");
  }));
  $("#customersTable").addEventListener("click", (event) => {
    const id = event.target.dataset.fillCustomer;
    if (!id) return;
    const customer = state.customers.find((item) => String(item.customerId) === String(id));
    if (customer) fillForm($("#customerForm"), { id: customer.customerId, ...customer, password: "" });
  });
  $("#customerForm").addEventListener("submit", (event) => {
    event.preventDefault();
    run(event.submitter, async () => {
      const data = formData(event.currentTarget);
      const id = numberOrNull(data.id);
      await api(id ? `/api/customers/${id}` : "/api/customers", {
        method: id ? "PUT" : "POST",
        body: JSON.stringify({ name: data.name, email: data.email, password: data.password, phone: data.phone, address: data.address })
      });
      await loadCustomers();
      showNotice(id ? "Customer updated." : "Customer created.");
    });
  });
  $("#deleteCustomer").addEventListener("click", (event) => run(event.currentTarget, async () => {
    const id = numberOrNull($("#customerForm").elements.id.value);
    if (!id) throw new Error("Enter a customer ID to delete.");
    await api(`/api/customers/${id}`, { method: "DELETE" });
    await loadCustomers();
    showNotice("Customer deleted.");
  }));

  $("#loadSuppliers").addEventListener("click", (event) => run(event.currentTarget, loadSuppliers));
  $("#findSupplierByEmail").addEventListener("click", (event) => run(event.currentTarget, async () => {
    const email = $("#supplierEmailLookup").value.trim();
    if (!email) throw new Error("Enter a supplier email.");
    renderSuppliers([await api(`/api/suppliers/email/${encodeURIComponent(email)}`)]);
    showNotice("Supplier loaded.");
  }));
  $("#suppliersTable").addEventListener("click", (event) => {
    const id = event.target.dataset.fillSupplier;
    if (!id) return;
    const supplier = state.suppliers.find((item) => String(item.supplierId) === String(id));
    if (supplier) fillForm($("#supplierForm"), { id: supplier.supplierId, ...supplier, password: "" });
  });
  $("#supplierForm").addEventListener("submit", (event) => {
    event.preventDefault();
    run(event.submitter, async () => {
      const data = formData(event.currentTarget);
      const id = state.auth.role === "SUPPLIER" ? state.auth.userId : numberOrNull(data.id);
      await api(id ? `/api/suppliers/${id}` : "/api/suppliers", {
        method: id ? "PUT" : "POST",
        body: JSON.stringify({ name: data.name, email: data.email, password: data.password, phone: data.phone })
      });
      await loadSuppliers();
      showNotice(id ? "Supplier updated." : "Supplier created.");
    });
  });
  $("#deleteSupplier").addEventListener("click", (event) => run(event.currentTarget, async () => {
    const id = numberOrNull($("#supplierForm").elements.id.value);
    if (!id) throw new Error("Enter a supplier ID to delete.");
    await api(`/api/suppliers/${id}`, { method: "DELETE" });
    await loadSuppliers();
    showNotice("Supplier deleted.");
  }));
  $("#supplierProductForm").addEventListener("submit", (event) => {
    event.preventDefault();
    run(event.submitter, async () => {
      const data = formData(event.currentTarget);
      const supplierId = state.auth.role === "SUPPLIER" ? state.auth.userId : numberOrNull(data.supplierId);
      await api(`/api/suppliers/${supplierId}/products`, {
        method: "POST",
        body: JSON.stringify({
          title: data.title,
          size: Number(data.size),
          description: data.description,
          price: Number(data.price),
          initialQuantity: Number(data.initialQuantity),
          cost: Number(data.cost)
        })
      });
      await Promise.all([loadProducts(), state.auth.role === "SUPPLIER" ? loadSupplierDashboard() : Promise.resolve()]);
      showNotice("Supplied product added.");
    });
  });
  $("#updateSupplierProduct").addEventListener("click", (event) => run(event.currentTarget, async () => {
    const form = $("#supplierProductForm");
    const data = formData(form);
    const productId = numberOrNull(data.productId);
    const supplierId = state.auth.role === "SUPPLIER" ? state.auth.userId : numberOrNull(data.supplierId);
    if (!productId) throw new Error("Enter a product ID to update.");
    await api(`/api/suppliers/${supplierId}/products/${productId}`, {
      method: "PUT",
      body: JSON.stringify({
        title: data.title,
        size: Number(data.size),
        description: data.description,
        price: Number(data.price),
        initialQuantity: Number(data.initialQuantity || 0),
        cost: Number(data.cost)
      })
    });
    await Promise.all([loadProducts(), state.auth.role === "SUPPLIER" ? loadSupplierDashboard() : Promise.resolve()]);
    showNotice("Supplied product updated.");
  }));
  $("#deleteSupplierProduct").addEventListener("click", (event) => run(event.currentTarget, async () => {
    const data = formData($("#supplierProductForm"));
    const productId = numberOrNull(data.productId);
    const supplierId = state.auth.role === "SUPPLIER" ? state.auth.userId : numberOrNull(data.supplierId);
    if (!productId) throw new Error("Enter a product ID to delete.");
    await api(`/api/suppliers/${supplierId}/products/${productId}`, { method: "DELETE" });
    await Promise.all([loadProducts(), state.auth.role === "SUPPLIER" ? loadSupplierDashboard() : Promise.resolve()]);
    showNotice("Supplied product deleted.");
  }));
  $("#supplierProductsTable").addEventListener("click", (event) => {
    const productId = event.target.dataset.fillSupplierProduct;
    if (!productId) return;
    const supply = state.supplierProducts.find((item) => String(item.productId) === String(productId));
    const product = state.products.find((item) => String(item.productId) === String(productId));
    if (supply && product) {
      fillForm($("#supplierProductForm"), {
        productId: supply.productId,
        supplierId: state.auth.userId,
        title: product.title,
        size: product.size,
        description: product.description,
        price: product.price,
        initialQuantity: 0,
        cost: supply.cost
      });
    }
  });

  $("#cartLoadForm").addEventListener("submit", (event) => {
    event.preventDefault();
    run(event.submitter, async () => {
      renderCart(await api(`/api/carts/${numberOrNull(formData(event.currentTarget).customerId)}`));
      showNotice("Cart loaded.");
    });
  });
  $$("[data-cart-action]").forEach((button) => {
    button.addEventListener("click", () => run(button, async () => {
      const data = formData($("#cartActionForm"));
      const customerId = state.auth.role === "CUSTOMER" ? state.auth.userId : numberOrNull(data.customerId);
      const productId = numberOrNull(data.productId);
      const quantity = numberOrNull(data.quantity);
      const action = button.dataset.cartAction;
      if (!customerId) throw new Error("Enter a customer ID.");
      if (action === "clear") {
        await api(`/api/carts/${customerId}/clear`, { method: "DELETE" });
        renderCart(null);
        showNotice("Cart cleared.");
        return;
      }
      if (!productId) throw new Error("Enter a product ID.");
      if (action === "remove") {
        renderCart(await api(`/api/carts/${customerId}/remove/${productId}`, { method: "DELETE" }));
        showNotice("Cart item removed.");
        return;
      }
      if (quantity == null) throw new Error("Enter a quantity.");
      const path = action === "add" ? "add" : "update";
      renderCart(await api(`/api/carts/${customerId}/${path}`, {
        method: action === "add" ? "POST" : "PUT",
        body: JSON.stringify({ productId, quantity })
      }));
      showNotice(action === "add" ? "Cart item added." : "Cart item updated.");
    }));
  });

  $("#orderHistoryForm").addEventListener("submit", (event) => {
    event.preventDefault();
    run(event.submitter, async () => {
      const customerId = numberOrNull(formData(event.currentTarget).customerId);
      renderOrders(await api(`/api/orders/customer/${customerId}`));
      showNotice("Order history loaded.");
    });
  });
  $("#checkoutForm").addEventListener("submit", (event) => {
    event.preventDefault();
    run(event.submitter, async () => {
      const customerId = state.auth.role === "CUSTOMER" ? state.auth.userId : numberOrNull(formData(event.currentTarget).customerId);
      const order = await api(`/api/orders/checkout/${customerId}`, { method: "POST" });
      renderOrders([order]);
      showNotice("Checkout completed.");
    });
  });
  $("#orderStatusForm").addEventListener("submit", (event) => {
    event.preventDefault();
    run(event.submitter, async () => {
      const data = formData(event.currentTarget);
      const order = await api(`/api/orders/${numberOrNull(data.orderId)}/status`, {
        method: "PATCH",
        body: JSON.stringify({ status: data.status, deliveredDate: data.deliveredDate ? new Date(data.deliveredDate).toISOString() : null })
      });
      renderOrders([order]);
      showNotice("Order status updated.");
    });
  });
  $("#cancelOrder").addEventListener("click", (event) => run(event.currentTarget, async () => {
    const orderId = numberOrNull($("#orderManageForm").elements.orderId.value);
    if (!orderId) throw new Error("Enter an order ID to cancel.");
    const order = await api(`/api/orders/${orderId}/cancel`, { method: "PATCH" });
    renderOrders([order]);
    showNotice("Order cancelled.");
  }));
  $("#deleteOrder").addEventListener("click", (event) => run(event.currentTarget, async () => {
    const orderId = numberOrNull($("#orderManageForm").elements.orderId.value);
    if (!orderId) throw new Error("Enter an order ID to delete.");
    await api(`/api/orders/${orderId}`, { method: "DELETE" });
    await loadOrders();
    showNotice("Order deleted.");
  }));

  $("#loadWarehouses").addEventListener("click", (event) => run(event.currentTarget, loadWarehouses));
  $$("[data-stock-action]").forEach((button) => {
    button.addEventListener("click", () => run(button, async () => {
      const data = formData($("#stockForm"));
      await api(`/api/inventory/${button.dataset.stockAction}`, {
        method: "POST",
        body: JSON.stringify({ productId: numberOrNull(data.productId), quantity: Number(data.quantity) })
      });
      await loadWarehouses();
      showNotice(button.dataset.stockAction === "restock" ? "Stock restocked." : "Stock reserved.");
    }));
  });
  $("#warehouseForm").addEventListener("submit", (event) => {
    event.preventDefault();
    run(event.submitter, async () => {
      const data = formData(event.currentTarget);
      await api("/api/inventory/warehouses", {
        method: "POST",
        body: JSON.stringify({ totalCapacity: Number(data.totalCapacity), location: data.location })
      });
      await loadWarehouses();
      showNotice("Warehouse added.");
    });
  });
  $("#warehousesTable").addEventListener("click", (event) => {
    const clearId = event.target.dataset.clearWarehouse;
    const deleteId = event.target.dataset.deleteWarehouse;
    if (!clearId && !deleteId) return;
    run(event.target, async () => {
      if (clearId) {
        await api(`/api/inventory/warehouses/${clearId}/clear`, { method: "POST" });
        showNotice("Warehouse cleared.");
      }
      if (deleteId) {
        await api(`/api/inventory/warehouses/${deleteId}`, { method: "DELETE" });
        showNotice("Warehouse deleted.");
      }
      await loadWarehouses();
    });
  });
}

bindEvents();
applyAuthUi();
if (state.auth?.token) {
  selectView("dashboard");
  run($("#refreshButton"), loadDashboard);
}
