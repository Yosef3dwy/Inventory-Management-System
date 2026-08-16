const state = {
  view: "products",
  products: [],
  customers: [],
  suppliers: [],
  warehouses: []
};

const titles = {
  products: ["Products", "Catalog records from the existing product API."],
  customers: ["Customers", "Customer records from the existing customer API."],
  suppliers: ["Suppliers", "Supplier records and supplied product creation."],
  carts: ["Carts", "Customer cart operations already exposed by the backend."],
  orders: ["Orders", "Checkout, customer history, and status updates."],
  inventory: ["Inventory", "Warehouse capacity plus reserve and restock actions."]
};

const $ = (selector, root = document) => root.querySelector(selector);
const $$ = (selector, root = document) => Array.from(root.querySelectorAll(selector));

function showNotice(message, isError = false) {
  const notice = $("#notice");
  notice.textContent = message;
  notice.className = `notice show${isError ? " error" : ""}`;
}

function money(value) {
  const number = Number(value || 0);
  return number.toLocaleString(undefined, { style: "currency", currency: "USD" });
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
  const response = await fetch(path, {
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {})
    },
    ...options
  });

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
  if (!button) return;
  button.disabled = busy;
}

function fillForm(form, values) {
  Object.entries(values).forEach(([key, value]) => {
    const input = form.elements[key];
    if (input) input.value = value ?? "";
  });
}

function selectView(view) {
  state.view = view;
  $$(".nav-item").forEach((button) => button.classList.toggle("active", button.dataset.view === view));
  $$(".view").forEach((section) => section.classList.toggle("active", section.id === view));
  $("#pageTitle").textContent = titles[view][0];
  $("#pageSubtitle").textContent = titles[view][1];
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

function renderOrders(orders) {
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

function emptyRow(columns) {
  return `<tr><td colspan="${columns}" class="muted">No data loaded.</td></tr>`;
}

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

async function loadProducts() {
  state.products = await api("/api/products");
  renderProducts();
  showNotice("Products loaded.");
}

async function loadCustomers() {
  state.customers = await api("/api/customers");
  renderCustomers();
  showNotice("Customers loaded.");
}

async function loadSuppliers() {
  state.suppliers = await api("/api/suppliers");
  renderSuppliers();
  showNotice("Suppliers loaded.");
}

async function loadWarehouses() {
  state.warehouses = await api("/api/inventory/warehouses");
  renderWarehouses();
  showNotice("Warehouses loaded.");
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

function bindEvents() {
  $$(".nav-item").forEach((button) => {
    button.addEventListener("click", () => selectView(button.dataset.view));
  });

  $("#refreshButton").addEventListener("click", (event) => {
    run(event.currentTarget, async () => {
      if (state.view === "products") await loadProducts();
      if (state.view === "customers") await loadCustomers();
      if (state.view === "suppliers") await loadSuppliers();
      if (state.view === "inventory") await loadWarehouses();
      if (state.view === "carts") showNotice("Enter a customer ID to load a cart.");
      if (state.view === "orders") showNotice("Enter a customer ID to load order history.");
    });
  });

  $("#loadProducts").addEventListener("click", (event) => run(event.currentTarget, loadProducts));
  $("#productSearch").addEventListener("input", renderProducts);

  $("#productForm").addEventListener("submit", (event) => {
    event.preventDefault();
    run(event.submitter, async () => {
      const data = formData(event.currentTarget);
      const id = numberOrNull(data.id);
      const body = {
        title: data.title,
        size: Number(data.size),
        description: data.description,
        price: Number(data.price)
      };
      await api(id ? `/api/products/${id}` : "/api/products", {
        method: id ? "PUT" : "POST",
        body: JSON.stringify(body)
      });
      await loadProducts();
      showNotice(id ? "Product updated." : "Product created.");
    });
  });

  $("#loadCustomers").addEventListener("click", (event) => run(event.currentTarget, loadCustomers));
  $("#findCustomerByEmail").addEventListener("click", (event) => {
    run(event.currentTarget, async () => {
      const email = $("#customerEmailLookup").value.trim();
      if (!email) throw new Error("Enter a customer email.");
      renderCustomers([await api(`/api/customers/email/${encodeURIComponent(email)}`)]);
      showNotice("Customer loaded.");
    });
  });

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
      const body = {
        name: data.name,
        email: data.email,
        password: data.password,
        phone: data.phone,
        address: data.address
      };
      await api(id ? `/api/customers/${id}` : "/api/customers", {
        method: id ? "PUT" : "POST",
        body: JSON.stringify(body)
      });
      await loadCustomers();
      showNotice(id ? "Customer updated." : "Customer created.");
    });
  });

  $("#deleteCustomer").addEventListener("click", (event) => {
    run(event.currentTarget, async () => {
      const id = numberOrNull($("#customerForm").elements.id.value);
      if (!id) throw new Error("Enter a customer ID to delete.");
      await api(`/api/customers/${id}`, { method: "DELETE" });
      await loadCustomers();
      showNotice("Customer deleted.");
    });
  });

  $("#loadSuppliers").addEventListener("click", (event) => run(event.currentTarget, loadSuppliers));
  $("#findSupplierByEmail").addEventListener("click", (event) => {
    run(event.currentTarget, async () => {
      const email = $("#supplierEmailLookup").value.trim();
      if (!email) throw new Error("Enter a supplier email.");
      renderSuppliers([await api(`/api/suppliers/email/${encodeURIComponent(email)}`)]);
      showNotice("Supplier loaded.");
    });
  });

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
      const id = numberOrNull(data.id);
      const body = {
        name: data.name,
        email: data.email,
        password: data.password,
        phone: data.phone
      };
      await api(id ? `/api/suppliers/${id}` : "/api/suppliers", {
        method: id ? "PUT" : "POST",
        body: JSON.stringify(body)
      });
      await loadSuppliers();
      showNotice(id ? "Supplier updated." : "Supplier created.");
    });
  });

  $("#deleteSupplier").addEventListener("click", (event) => {
    run(event.currentTarget, async () => {
      const id = numberOrNull($("#supplierForm").elements.id.value);
      if (!id) throw new Error("Enter a supplier ID to delete.");
      await api(`/api/suppliers/${id}`, { method: "DELETE" });
      await loadSuppliers();
      showNotice("Supplier deleted.");
    });
  });

  $("#supplierProductForm").addEventListener("submit", (event) => {
    event.preventDefault();
    run(event.submitter, async () => {
      const data = formData(event.currentTarget);
      const supplierId = numberOrNull(data.supplierId);
      const body = {
        title: data.title,
        size: Number(data.size),
        description: data.description,
        price: Number(data.price),
        initialQuantity: Number(data.initialQuantity),
        cost: Number(data.cost)
      };
      await api(`/api/suppliers/${supplierId}/products`, {
        method: "POST",
        body: JSON.stringify(body)
      });
      showNotice("Supplied product added.");
      await loadProducts();
    });
  });

  $("#cartLoadForm").addEventListener("submit", (event) => {
    event.preventDefault();
    run(event.submitter, async () => {
      const customerId = numberOrNull(formData(event.currentTarget).customerId);
      renderCart(await api(`/api/carts/${customerId}`));
      showNotice("Cart loaded.");
    });
  });

  $$("[data-cart-action]").forEach((button) => {
    button.addEventListener("click", () => run(button, async () => {
      const data = formData($("#cartActionForm"));
      const customerId = numberOrNull(data.customerId);
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
      const customerId = numberOrNull(formData(event.currentTarget).customerId);
      const order = await api(`/api/orders/checkout/${customerId}`, { method: "POST" });
      renderOrders([order]);
      showNotice("Checkout completed.");
    });
  });

  $("#orderStatusForm").addEventListener("submit", (event) => {
    event.preventDefault();
    run(event.submitter, async () => {
      const data = formData(event.currentTarget);
      const orderId = numberOrNull(data.orderId);
      const body = {
        status: data.status,
        deliveredDate: data.deliveredDate ? new Date(data.deliveredDate).toISOString() : null
      };
      const order = await api(`/api/orders/${orderId}/status`, {
        method: "PATCH",
        body: JSON.stringify(body)
      });
      renderOrders([order]);
      showNotice("Order status updated.");
    });
  });

  $("#loadWarehouses").addEventListener("click", (event) => run(event.currentTarget, loadWarehouses));

  $$("[data-stock-action]").forEach((button) => {
    button.addEventListener("click", () => run(button, async () => {
      const data = formData($("#stockForm"));
      const body = {
        productId: numberOrNull(data.productId),
        quantity: Number(data.quantity)
      };
      await api(`/api/inventory/${button.dataset.stockAction}`, {
        method: "POST",
        body: JSON.stringify(body)
      });
      showNotice(button.dataset.stockAction === "restock" ? "Stock restocked." : "Stock reserved.");
      await loadWarehouses();
    }));
  });

  $("#warehouseForm").addEventListener("submit", (event) => {
    event.preventDefault();
    run(event.submitter, async () => {
      const data = formData(event.currentTarget);
      await api("/api/inventory/warehouses", {
        method: "POST",
        body: JSON.stringify({
          totalCapacity: Number(data.totalCapacity),
          location: data.location
        })
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
run($("#loadProducts"), loadProducts);
