<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<t:layout title="Dashboard">

    <!-- Products Section -->
    <div class="row mb-4">
      <div class="col-12">
        <div class="card shadow mb-4">
          <div class="card-header py-3 d-flex flex-row align-items-center justify-content-between">
            <h6 class="m-0 font-weight-bold text-primary">
              <i class="fas fa-box"></i> Products
            </h6>
            <a href="${pageContext.request.contextPath}/products" class="btn btn-sm btn-primary">
              View All
            </a>
          </div>
          <div class="card-body">
            <div class="table-responsive">
              <table class="table table-bordered table-hover align-middle mb-0">
                <thead class="thead-dark">
                  <tr class="text-center">
                    <th style="width: 70px;">ID</th>
                    <th style="width: 140px;">SKU</th>
                    <th>Product Name</th>
                    <th style="width: 170px;">Category</th>
                    <th style="width: 190px;">Created At</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  <c:forEach var="product" items="${products}">
                    <tr>
                      <td class="text-center">${product.productId}</td>
                      <td class="font-weight-bold text-primary">${product.sku}</td>
                      <td>${product.name}</td>
                      <td>${product.categoryName}</td>
                      <td class="text-center">${product.createdAt}</td>
                      <td class="text-center">
                        <button type="button"
                                class="btn btn-sm btn-outline-primary shadow-sm btn-view-product"
                                data-product-id="${product.productId}">
                          View Details
                        </button>
                      </td>
                    </tr>
                  </c:forEach>
                  <c:if test="${empty products}">
                    <tr>
                      <td colspan="6" class="text-center py-4 text-muted">No products found.</td>
                    </tr>
                  </c:if>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Warehouse Layout Section -->
    <div class="row mb-4">
      <div class="col-12">
        <div class="card shadow mb-4">
          <div class="card-header py-3 d-flex flex-row align-items-center justify-content-between">
            <h6 class="m-0 font-weight-bold text-primary">
              <i class="fas fa-warehouse"></i> Warehouse Layout
            </h6>
            <a href="${pageContext.request.contextPath}/warehouse-layout" class="btn btn-sm btn-primary">
              Manage Layout
            </a>
          </div>
          <div class="card-body">
            <div class="table-responsive">
              <table class="table table-bordered table-hover align-middle mb-0">
                <thead class="thead-dark">
                  <tr class="text-center">
                    <th style="width: 70px;">ID</th>
                    <th style="width: 140px;">Warehouse Code</th>
                    <th>Warehouse Name</th>
                    <th>Address</th>
                    <th style="width: 120px;">Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  <c:forEach var="warehouse" items="${warehouses}">
                    <tr>
                      <td class="text-center">${warehouse.warehouseId}</td>
                      <td class="font-weight-bold text-primary">${warehouse.code}</td>
                      <td>${warehouse.name}</td>
                      <td>${warehouse.address}</td>
                      <td class="text-center">
                        <span class="badge ${warehouse.status == 'ACTIVE' ? 'bg-success' : 'bg-secondary'}">
                          ${warehouse.status}
                        </span>
                      </td>
                      <td class="text-center">
                        <a href="${pageContext.request.contextPath}/warehouse-layout?warehouseId=${warehouse.warehouseId}" 
                           class="btn btn-sm btn-outline-primary shadow-sm">View Layout</a>
                      </td>
                    </tr>
                  </c:forEach>
                  <c:if test="${empty warehouses}">
                    <tr>
                      <td colspan="6" class="text-center py-4 text-muted">No warehouses found.</td>
                    </tr>
                  </c:if>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Product Detail Modal (same behavior as Products page) -->
    <div class="modal fade" id="viewProductModal" tabindex="-1" aria-labelledby="viewProductModalLabel" aria-hidden="true">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content shadow-lg border-0">
          <div class="modal-header bg-secondary text-white rounded-0">
            <h5 class="modal-title d-flex align-items-center" id="viewProductModalLabel">
              <i class="fas fa-eye me-2"></i>Product Detail
            </h5>
            <button type="button" class="btn-close btn-close-white" aria-label="Close" id="viewModalCloseBtn"></button>
          </div>
          <div class="modal-body p-4">
            <div class="row mb-3">
              <div class="col-md-6">
                <p class="mb-1 small text-muted">SKU</p>
                <p class="mb-2"><code id="viewSku"></code></p>
              </div>
              <div class="col-md-6">
                <p class="mb-1 small text-muted">Created At</p>
                <p class="mb-2" id="viewCreatedAt">-</p>
              </div>
            </div>
            <div class="row mb-3">
              <div class="col-md-6">
                <p class="mb-1 small text-muted">Product Name</p>
                <p class="mb-2 fw-semibold" id="viewName"></p>
              </div>
              <div class="col-md-6">
                <p class="mb-1 small text-muted">Category</p>
                <p class="mb-2" id="viewCategory"></p>
              </div>
            </div>
            <div class="row mb-3">
              <div class="col-md-6">
                <p class="mb-1 small text-muted">UOM</p>
                <p class="mb-2" id="viewUom">-</p>
              </div>
              <div class="col-md-6">
                <p class="mb-1 small text-muted">Dimensions &amp; Weight</p>
                <p class="mb-2 small" id="viewDimensions">-</p>
              </div>
            </div>

            <hr class="my-3">
            <h6 class="fw-semibold mb-2 d-flex align-items-center">
              <i class="fas fa-th-list me-2"></i>Variants
            </h6>
            <div class="table-responsive rounded border">
              <table class="table table-sm table-hover mb-0">
                <thead class="table-secondary">
                  <tr>
                    <th class="border-0 text-center">Variant SKU</th>
                    <th class="border-0 text-center">Color</th>
                    <th class="border-0 text-center">Size</th>
                    <th class="border-0 text-center">Status</th>
                    <th class="border-0 text-center">On hand</th>
                    <th class="border-0 text-center">Available</th>
                  </tr>
                </thead>
                <tbody id="viewVariantTbody"></tbody>
              </table>
            </div>
          </div>
          <div class="modal-footer border-top bg-light rounded-0">
            <button type="button" class="btn btn-outline-secondary btn-sm" id="viewModalFooterCloseBtn">Close</button>
          </div>
        </div>
      </div>
    </div>

    <script>
      (function() {
        document.addEventListener('DOMContentLoaded', function() {
          var ctx = '${pageContext.request.contextPath}';

          var viewModalEl = document.getElementById('viewProductModal');
          var viewModalInstance = viewModalEl ? new bootstrap.Modal(viewModalEl) : null;
          var viewSkuEl = document.getElementById('viewSku');
          var viewNameEl = document.getElementById('viewName');
          var viewCategoryEl = document.getElementById('viewCategory');
          var viewUomEl = document.getElementById('viewUom');
          var viewDimensionsEl = document.getElementById('viewDimensions');
          var viewCreatedAtEl = document.getElementById('viewCreatedAt');
          var viewVariantTbody = document.getElementById('viewVariantTbody');
          var viewModalCloseBtn = document.getElementById('viewModalCloseBtn');
          var viewModalFooterCloseBtn = document.getElementById('viewModalFooterCloseBtn');

          function textOrDash(value) {
            if (value === null || value === undefined || value === '') return '-';
            return value;
          }

          function formatDimensions(p) {
            var parts = [];
            if (p.weight != null) parts.push('Weight: ' + p.weight + ' kg');
            var hasL = p.length != null;
            var hasW = p.width != null;
            var hasH = p.height != null;
            if (hasL || hasW || hasH) {
              parts.push('LxWxH: ' + (hasL ? p.length : '0') + ' x ' + (hasW ? p.width : '0') + ' x ' + (hasH ? p.height : '0') + ' cm');
            }
            return parts.length ? parts.join(' | ') : '-';
          }

          function renderVariants(variants) {
            if (!viewVariantTbody) return;
            viewVariantTbody.innerHTML = '';
            if (!variants || !variants.length) {
              viewVariantTbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted py-3">No variants.</td></tr>';
              return;
            }
            variants.forEach(function(v) {
              var tr = document.createElement('tr');
              tr.innerHTML =
                '<td class="text-center"><code class="small">' + (v.variantSku || '') + '</code></td>' +
                '<td class="text-center">' + (v.color || '') + '</td>' +
                '<td class="text-center">' + (v.size || '') + '</td>' +
                '<td class="text-center">' + ((v.status || '-').toUpperCase()) + '</td>' +
                '<td class="text-center">' + (v.totalQtyOnHand != null ? v.totalQtyOnHand : '0') + '</td>' +
                '<td class="text-center">' + (v.totalQtyAvailable != null ? v.totalQtyAvailable : '0') + '</td>';
              viewVariantTbody.appendChild(tr);
            });
          }

          document.querySelectorAll('.btn-view-product').forEach(function(btn) {
            btn.addEventListener('click', function() {
              var id = this.getAttribute('data-product-id');
              if (!id || !viewModalInstance) return;
              fetch(ctx + '/products?action=detail&id=' + encodeURIComponent(id), { headers: { 'Accept': 'application/json' } })
                .then(function(res) {
                  if (!res.ok) throw new Error('Failed to load product');
                  return res.json();
                })
                .then(function(data) {
                  if (!data) return;
                  if (viewSkuEl) viewSkuEl.textContent = textOrDash(data.sku);
                  if (viewNameEl) viewNameEl.textContent = textOrDash(data.name);
                  if (viewCategoryEl) viewCategoryEl.textContent = textOrDash(data.categoryName);
                  if (viewUomEl) viewUomEl.textContent = textOrDash(data.uomName);
                  if (viewDimensionsEl) viewDimensionsEl.textContent = formatDimensions(data);
                  if (viewCreatedAtEl) viewCreatedAtEl.textContent = textOrDash(data.createdAt);
                  renderVariants(data.variants || []);
                  viewModalInstance.show();
                })
                .catch(function() {
                  alert('Cannot load product details. Please try again.');
                });
            });
          });

          if (viewModalCloseBtn && viewModalInstance) {
            viewModalCloseBtn.addEventListener('click', function() { viewModalInstance.hide(); });
          }
          if (viewModalFooterCloseBtn && viewModalInstance) {
            viewModalFooterCloseBtn.addEventListener('click', function() { viewModalInstance.hide(); });
          }
        });
      })();
    </script>
</t:layout>

