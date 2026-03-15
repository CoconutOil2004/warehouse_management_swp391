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
              <table class="table table-bordered table-hover table-striped align-middle mb-0">
                <thead class="thead-dark">
                  <tr class="text-center">
                    <th>ID</th>
                    <th>SKU</th>
                    <th>Tên sản phẩm</th>
                    <th>Barcode</th>
                    <th>Ngày tạo</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  <c:forEach var="product" items="${products}">
                    <tr>
                      <td class="text-center">${product.productId}</td>
                      <td class="font-weight-bold text-primary">${product.sku}</td>
                      <td>${product.name}</td>
                      <td>${product.barcode}</td>
                      <td class="text-center">${product.createdAt}</td>
                      <td class="text-center">
                        <a href="${pageContext.request.contextPath}/products?action=detail&id=${product.productId}" 
                           class="btn btn-sm btn-info shadow-sm">View Details</a>
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
              <table class="table table-bordered table-hover table-striped align-middle mb-0">
                <thead class="thead-dark">
                  <tr class="text-center">
                    <th>ID</th>
                    <th>Mã kho</th>
                    <th>Tên kho</th>
                    <th>Địa chỉ</th>
                    <th>Trạng thái</th>
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
                        <span class="badge badge-pill ${warehouse.status == 'ACTIVE' ? 'badge-success' : 'badge-secondary'}">
                          ${warehouse.status}
                        </span>
                      </td>
                      <td class="text-center">
                        <a href="${pageContext.request.contextPath}/warehouse-layout?warehouseId=${warehouse.warehouseId}" 
                           class="btn btn-sm btn-info shadow-sm">View Layout</a>
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
</t:layout>

