<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@taglib prefix="t" tagdir="/WEB-INF/tags/" %>

            <t:layout title="Warehouse Detail: ${warehouse.name}">
                <jsp:attribute name="actions">
                    <div class="d-flex gap-3">
                        <t:link url="${pageContext.request.contextPath}/admin/warehouse" variant="split" color="dark"
                            icon="chevron-left">
                            Go back
                        </t:link>
                        <t:link
                            url="${pageContext.request.contextPath}/admin/warehouse/update?id=${warehouse.warehouseId}"
                            variant="split" color="primary" icon="pencil">
                            Edit Warehouse
                        </t:link>
                    </div>
                </jsp:attribute>

                <jsp:body>
                    <div class="row">
                        <!-- Main Info Card -->
                        <div class="col-xl-4 col-md-6 mb-4">
                            <div class="card shadow-sm border-left-primary h-100 py-2">
                                <div class="card-body">
                                    <div class="row no-gutters align-items-center">
                                        <div class="col mr-2">
                                            <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">
                                                Warehouse Code</div>
                                            <div class="h5 mb-0 font-weight-bold text-gray-800">${warehouse.code}</div>
                                        </div>
                                        <div class="col-auto">
                                            <i class="bi bi-tag-fill fa-2x text-gray-300"></i>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="col-xl-4 col-md-6 mb-4">
                            <div class="card shadow-sm border-left-success h-100 py-2">
                                <div class="card-body">
                                    <div class="row no-gutters align-items-center">
                                        <div class="col mr-2">
                                            <div class="text-xs font-weight-bold text-success text-uppercase mb-1">
                                                Status</div>
                                            <div class="h5 mb-0 font-weight-bold text-gray-800">
                                                <span
                                                    class="badge ${warehouse.status == 'ACTIVE' ? 'bg-success' : 'bg-secondary'}">
                                                    ${warehouse.status}
                                                </span>
                                            </div>
                                        </div>
                                        <div class="col-auto">
                                            <i class="bi bi-info-circle-fill fa-2x text-gray-300"></i>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Details Card -->
                    <div class="card shadow-sm mb-4 border-0">
                        <div class="card-header py-3 bg-white border-bottom-0">
                            <h6 class="m-0 font-weight-bold text-primary">Warehouse Information</h6>
                        </div>
                        <div class="card-body">
                            <div class="row mb-3">
                                <div class="col-sm-3 text-muted small text-uppercase fw-bold">Full Name</div>
                                <div class="col-sm-9 fs-5 fw-bold text-dark">${warehouse.name}</div>
                            </div>
                            <hr class="bg-light">
                            <div class="row mb-0">
                                <div class="col-sm-3 text-muted small text-uppercase fw-bold">Address</div>
                                <div class="col-sm-9">${empty warehouse.address ? 'N/A' : warehouse.address}</div>
                            </div>
                        </div>
                    </div>

                </jsp:body>
            </t:layout>