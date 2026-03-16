<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
        <%@taglib uri="jakarta.tags.core" prefix="c" %>
            <t:layout title="Shipment Management">
                <div class="container-fluid">
                    <!-- Summary Cards -->
                    <div class="row mb-4">
                        <div class="col-xl-3 col-md-6 mb-4">
                            <div class="card border-left-primary shadow h-100 py-2">
                                <div class="card-body">
                                    <div class="row no-gutters align-items-center">
                                        <div class="col mr-2">
                                            <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">Total
                                                Shipments</div>
                                            <div class="h5 mb-0 font-weight-bold text-gray-800">${shipments.size()}
                                            </div>
                                        </div>
                                        <div class="col-auto">
                                            <i class="fas fa-shipping-fast fa-2x text-gray-300"></i>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <!-- Add more summary cards if needed -->
                    </div>

                    <!-- Filter Form -->
                    <div class="card shadow mb-4"
                        style="backdrop-filter: blur(5px); background: rgba(255, 255, 255, 0.9);">
                        <div class="card-body">
                            <form action="${pageContext.request.contextPath}/shipment" method="get" class="row g-3">
                                <input type="hidden" name="action" value="list">
                                <div class="col-md-2">
                                    <label class="form-label font-weight-bold ml-1">Shipment #</label>
                                    <input type="text" class="form-control rounded-pill" name="shipmentNumber"
                                        value="${param.shipmentNumber}" placeholder="Search...">
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label font-weight-bold ml-1">Carrier</label>
                                    <select class="form-control rounded-pill" name="carrierId">
                                        <option value="">-- All Carriers --</option>
                                        <c:forEach var="c" items="${carriers}">
                                            <option value="${c.carrierId}" ${param.carrierId==c.carrierId ? 'selected'
                                                : '' }>${c.name}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label font-weight-bold ml-1">Status</label>
                                    <select class="form-control rounded-pill" name="status">
                                        <option value="">-- All Statuses --</option>
                                        <option value="CREATED" ${param.status=='CREATED' ? 'selected' : '' }>Created
                                            (CREATED)</option>
                                        <option value="PICKED_UP" ${param.status=='PICKED_UP' ? 'selected' : '' }>Picked
                                            Up (PICKED_UP)</option>
                                        <option value="IN_TRANSIT" ${param.status=='IN_TRANSIT' ? 'selected' : '' }>In
                                            Transit (IN_TRANSIT)</option>
                                        <option value="DELIVERED" ${param.status=='DELIVERED' ? 'selected' : '' }>
                                            Delivered (DELIVERED)</option>
                                        <option value="CANCELLED" ${param.status=='CANCELLED' ? 'selected' : '' }>
                                            Cancelled/Failed (CANCELLED)</option>
                                    </select>
                                </div>
                                <div class="col-md-2">
                                    <label class="form-label font-weight-bold ml-1">Type</label>
                                    <select class="form-control rounded-pill" name="shipmentType">
                                        <option value="">-- All Types --</option>
                                        <option value="CUSTOMER" ${param.shipmentType=='CUSTOMER' ? 'selected' : '' }>
                                            Customer Delivery</option>
                                        <option value="TRANSFER" ${param.shipmentType=='TRANSFER' ? 'selected' : '' }>
                                            Internal Transfer</option>
                                    </select>
                                </div>
                                <div class="col-md-2 d-flex align-items-end gap-2 justify-content-end">
                                    <button type="submit" class="btn btn-primary rounded-pill px-4">
                                        <i class="fas fa-search mr-1"></i> Filter
                                    </button>
                                    <a href="${pageContext.request.contextPath}/shipment?action=list"
                                        class="btn btn-light rounded-pill px-4" title="Reset">
                                        <i class="fas fa-undo mr-1"></i> Reset
                                    </a>
                                </div>
                            </form>
                        </div>
                    </div>

                    <!-- Action Header -->
                    <div class="d-flex justify-content-between align-items-center mb-3">
                        <h5 class="m-0 font-weight-bold text-primary">Shipment Registry</h5>
                        <a href="${pageContext.request.contextPath}/shipment?action=create"
                            class="btn btn-success btn-icon-split shadow-sm">
                            <span class="icon text-white-50">
                                <i class="fas fa-plus"></i>
                            </span>
                            <span class="text">New Shipment</span>
                        </a>
                    </div>

                    <!-- Data Table -->
                    <div class="card shadow mb-4">
                        <div class="card-body p-0">
                            <div class="table-responsive">
                                <table class="table table-hover align-middle mb-0">
                                    <thead class="bg-light">
                                        <tr>
                                            <th class="text-center px-4" style="cursor:pointer;"
                                                onclick="toggleSort('shipment_id')">ID <i
                                                    class="fas fa-sort-amount-down-alt ml-1 text-muted"></i></th>
                                            <th style="cursor:pointer;" onclick="toggleSort('shipment_number')">Shipment
                                                # <i class="fas fa-sort ml-1 text-muted"></i></th>
                                            <th>Shipment Type</th>
                                            <th style="cursor:pointer;" onclick="toggleSort('carrier_name')">Carrier <i
                                                    class="fas fa-sort ml-1 text-muted"></i></th>
                                            <th>GDN Ref</th>
                                            <th>Tracking Code</th>
                                            <th class="text-center" style="cursor:pointer;"
                                                onclick="toggleSort('status')">Status <i
                                                    class="fas fa-sort ml-1 text-muted"></i></th>
                                            <th style="cursor:pointer;" onclick="toggleSort('created_at')">Created Date
                                                <i class="fas fa-sort ml-1 text-muted"></i>
                                            </th>
                                            <th class="text-center">Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="s" items="${shipments}">
                                            <tr>
                                                <td class="text-center text-muted">${s.shipmentId}</td>
                                                <td><span class="font-weight-bold text-dark">${s.shipmentNumber}</span>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${s.shipmentType == 'CUSTOMER'}">Customer
                                                            Delivery</c:when>
                                                        <c:when test="${s.shipmentType == 'TRANSFER'}">Internal
                                                            Transfer</c:when>
                                                        <c:otherwise>${s.shipmentType}</c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>${s.carrierName}</td>
                                                <td><code class="text-primary">${s.gdnNumber}</code></td>
                                                <td><small class="text-muted">${empty s.trackingCode ? 'N/A' :
                                                        s.trackingCode}</small></td>
                                                <td class="text-center">
                                                    <c:choose>
                                                        <c:when test="${s.status == 'CREATED'}"><span
                                                                class="badge badge-warning px-3 py-2">Created</span>
                                                        </c:when>
                                                        <c:when test="${s.status == 'PICKED_UP'}"><span
                                                                class="badge badge-primary px-3 py-2">Picked Up</span>
                                                        </c:when>
                                                        <c:when test="${s.status == 'IN_TRANSIT'}"><span
                                                                class="badge badge-info px-3 py-2">In Transit</span>
                                                        </c:when>
                                                        <c:when test="${s.status == 'DELIVERED'}"><span
                                                                class="badge badge-success px-3 py-2">Delivered</span>
                                                        </c:when>
                                                        <c:when test="${s.status == 'CANCELLED'}"><span
                                                                class="badge badge-danger px-3 py-2">Cancelled/Failed</span></c:when>
                                                        <c:otherwise><span
                                                                class="badge badge-secondary px-3 py-2">${s.status}</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="time-ago" data-timestamp="${s.createdAt}">
                                                    <small>${s.createdAt}</small>
                                                </td>
                                                <td class="px-3 text-center">
                                                    <div class="d-flex justify-content-center align-items-center">
                                                        <!-- View 버튼 Slot -->
                                                        <div style="width: 60px;" class="d-flex justify-content-center">
                                                            <form action="${pageContext.request.contextPath}/shipment" method="get" class="mb-0">
                                                                <input type="hidden" name="action" value="detail">
                                                                <input type="hidden" name="id" value="${s.shipmentId}">
                                                                <t:button type="submit" size="sm" variant="outline" color="primary">View</t:button>
                                                            </form>
                                                        </div>
                                                        
                                                        <!-- Edit 버튼 Slot -->
                                                        <div style="width: 60px;" class="d-flex justify-content-center">
                                                            <c:if test="${s.status != 'DELIVERED' && s.status != 'CANCELLED'}">
                                                                <form action="${pageContext.request.contextPath}/shipment" method="get" class="mb-0">
                                                                    <input type="hidden" name="action" value="edit">
                                                                    <input type="hidden" name="id" value="${s.shipmentId}">
                                                                    <t:button type="submit" size="sm" variant="outline" color="primary">Update</t:button>
                                                                </form>
                                                            </c:if>
                                                        </div>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                        <c:if test="${empty shipments}">
                                            <tr>
                                                <td colspan="9" class="text-center py-5">
                                                    <img src="${pageContext.request.contextPath}/assets/img/undraw_no_data.svg"
                                                        style="max-width: 150px;" class="mb-3 opacity-50">
                                                    <p class="text-muted">No shipments found matching your criteria.</p>
                                                </td>
                                            </tr>
                                        </c:if>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>

                    <!-- Hidden inputs for sort persistence -->
                    <input type="hidden" name="sortBy" value="${param.sortBy}">
                    <input type="hidden" name="order" value="${param.order}">

                    <!-- Pagination -->
                    <div class="mt-4">
                        <t:pagination page="${currentPage}" pages="${totalPages}" size="${pageSize}"
                            total="${totalRecords}" url="${pageContext.request.contextPath}/shipment?action=list"
                            include="[name='shipmentNumber'], [name='carrierId'], [name='status'], [name='shipmentType'], [name='sortBy'], [name='order']" />
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