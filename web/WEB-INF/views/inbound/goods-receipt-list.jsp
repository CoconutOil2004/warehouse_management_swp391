<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
<%@taglib uri="jakarta.tags.core" prefix="c" %>

<t:layout title="Goods Receipt List">
    <div class="container-fluid">

        <!-- Filter Form -->
        <div class="card mb-4 shadow-sm">
            <div class="card-body">
                <form action="${pageContext.request.contextPath}/goods-receipt" method="get"
                    class="row g-3">
                    <input type="hidden" name="action" value="list">
                    <div class="col-md-2">
                        <label class="form-label font-weight-bold small text-uppercase">GRN Number</label>
                        <input type="text" class="form-control form-control-sm" name="grnNumber" value="${param.grnNumber}"
                            placeholder="Code...">
                    </div>
                    <div class="col-md-2">
                        <label class="form-label font-weight-bold small text-uppercase">Purchase Order</label>
                        <input type="text" class="form-control form-control-sm" name="poNumber" value="${param.poNumber}"
                            placeholder="PO Code...">
                    </div>
                    <div class="col-md-3">
                        <label class="form-label font-weight-bold small text-uppercase">Supplier</label>
                        <select class="form-control form-control-sm" name="supplierId">
                            <option value="">-- All Suppliers --</option>
                            <c:forEach var="s" items="${suppliers}">
                                <option value="${s.supplierId}" ${param.supplierId==s.supplierId
                                    ? 'selected' : '' }>${s.name}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-md-1">
                        <label class="form-label font-weight-bold small text-uppercase">Status</label>
                        <select class="form-control form-control-sm" name="status">
                            <option value="">-- All --</option>
                            <option value="PENDING" ${param.status=='PENDING' ? 'selected' : '' }>PENDING
                            </option>
                            <option value="APPROVED" ${param.status=='APPROVED' ? 'selected' : '' }>APPROVED
                            </option>
                            <option value="REJECTED" ${param.status=='REJECTED' ? 'selected' : '' }>REJECTED
                            </option>
                        </select>
                    </div>
                    <div class="col-md-2 d-flex align-items-end">
                        <button type="submit" class="btn btn-primary btn-sm w-100 shadow-sm">
                            <i class="fas fa-search me-1"></i> Filter
                        </button>
                    </div>
                    <div class="col-md-2 d-flex align-items-end">
                        <a href="${pageContext.request.contextPath}/goods-receipt?action=list"
                            class="btn btn-secondary btn-sm w-100 shadow-sm">
                            <i class="fas fa-undo me-1"></i> Reset
                        </a>
                    </div>
                </form>
            </div>
        </div>

        <c:if test="${canMutation}">
            <div class="mb-3 d-flex justify-content-end">
                <a href="${pageContext.request.contextPath}/goods-receipt?action=create"
                    class="btn btn-success shadow-sm">
                    <i class="fas fa-plus"></i> Create New GRN
                </a>
            </div>
        </c:if>

        <div class="table-responsive shadow-sm rounded">
            <table class="table table-bordered table-hover table-striped align-middle mb-0">
                <thead class="thead-dark">
                    <tr class="text-center">
                        <th style="cursor: pointer;" onclick="toggleSort('grn_id')">ID <i
                                class="fas fa-sort"></i></th>
                        <th style="cursor: pointer;" onclick="toggleSort('grn_number')">GRN Number <i
                                class="fas fa-sort"></i></th>
                        <th style="cursor: pointer;" onclick="toggleSort('po_number')">Purchase Order <i
                                class="fas fa-sort"></i></th>
                        <th style="cursor: pointer;" onclick="toggleSort('supplier_name')">Supplier <i
                                class="fas fa-sort"></i></th>
                        <th style="cursor: pointer;" onclick="toggleSort('status')">Status <i
                                class="fas fa-sort"></i></th>
                        <th>Created At</th>
                        <th class="text-center">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="gr" items="${grns}">
                        <tr>
                            <td class="text-center">${gr.grnId}</td>
                            <td class="font-weight-bold text-primary">${gr.grnNumber}</td>
                            <td>${gr.poNumber}</td>
                            <td>${gr.supplierName}</td>
                            <td class="text-center">
                                <span
                                    class="badge badge-pill ${gr.status == 'PENDING' ? 'badge-warning' : (gr.status == 'APPROVED' ? 'badge-success' : 'badge-danger')}">
                                    ${gr.status}
                                </span>
                            </td>
                            <td class="text-center">${gr.createdAt}</td>
                            <td class="px-3 text-center">
                                <div class="d-flex justify-content-center align-items-center">
                                    <!-- View 버튼 Slot -->
                                    <div style="width: 60px;" class="d-flex justify-content-center">
                                        <form action="${pageContext.request.contextPath}/goods-receipt" method="get" class="mb-0">
                                            <input type="hidden" name="action" value="detail">
                                            <input type="hidden" name="id" value="${gr.grnId}">
                                            <t:button type="submit" size="sm" variant="outline" color="primary">View</t:button>
                                        </form>
                                    </div>

                                    <!-- Delete 버튼 Slot -->
                                    <div style="width: 70px;" class="d-flex justify-content-center">
                                        <c:if test="${canMutation && (gr.status == 'PENDING' || gr.status == 'DRAFT')}">
                                            <button type="button" class="btn btn-sm btn-outline-danger"
                                                    data-bs-toggle="modal" data-bs-target="#deleteGrnModal${gr.grnId}">
                                                Delete
                                            </button>

                                            <t:alert id="deleteGrnModal${gr.grnId}">
                                                <jsp:attribute name="title"> Confirm Delete </jsp:attribute>
                                                <jsp:attribute name="desciption">
                                                    Are you sure you want to delete Goods Receipt
                                                    <strong>${gr.grnNumber}</strong>? This action cannot be undone.
                                                </jsp:attribute>
                                                <jsp:attribute name="action">
                                                    <button type="button" class="btn btn-danger" data-bs-dismiss="modal"
                                                            onclick="document.getElementById('deleteGrnForm${gr.grnId}').submit()">
                                                        Delete
                                                    </button>
                                                </jsp:attribute>
                                            </t:alert>

                                            <form id="deleteGrnForm${gr.grnId}" action="${pageContext.request.contextPath}/goods-receipt"
                                                    method="get" class="d-none">
                                                <input type="hidden" name="action" value="delete">
                                                <input type="hidden" name="id" value="${gr.grnId}">
                                            </form>
                                        </c:if>
                                    </div>

                                    <!-- Edit 버튼 Slot -->
                                    <div style="width: 60px;" class="d-flex justify-content-center">
                                        <c:if test="${canMutation && (gr.status == 'PENDING' || gr.status == 'DRAFT')}">
                                            <form action="${pageContext.request.contextPath}/goods-receipt" method="get" class="mb-0">
                                                <input type="hidden" name="action" value="edit">
                                                <input type="hidden" name="id" value="${gr.grnId}">
                                                <t:button type="submit" size="sm" variant="outline" color="primary">Edit</t:button>
                                            </form>
                                        </c:if>
                                    </div>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty grns}">
                        <tr>
                            <td colspan="6" class="text-center py-4 text-muted">No data found in the system.
                            </td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>

        <!-- Hidden inputs for sort persistence -->
        <input type="hidden" name="sortBy" value="${param.sortBy}">
        <input type="hidden" name="order" value="${param.order}">

        <!-- Pagination -->
        <div class="mt-4">
            <t:pagination page="${currentPage}" pages="${totalPages}" size="${pageSize}"
                total="${totalRecords}" url="${pageContext.request.contextPath}/goods-receipt?action=list"
                include="[name='grnNumber'], [name='poNumber'], [name='supplierId'], [name='status'], [name='sortBy'], [name='order']" />
        </div>
    </div>

    <script>
        function toggleSort(field) {
            const urlParams = new URLSearchParams(window.location.search);
            const currentOrder = urlParams.get('order') === 'ASC' ? 'DESC' : 'ASC';
            urlParams.set('sortBy', field);
            urlParams.set('order', currentOrder);
            window.location.href = window.location.pathname + '?' + urlParams.toString();
        }
    </script>
</t:layout>
