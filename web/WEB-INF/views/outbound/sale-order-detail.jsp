<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="jakarta.tags.core" prefix="c"%>
<%@taglib uri="jakarta.tags.functions" prefix="fn"%>
<%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<t:layout title="Sale Order Detail">

    <div class="d-sm-flex align-items-center justify-content-between mb-4">
        <a href="${pageContext.request.contextPath}/sales-orders?action=list" class="btn btn-sm btn-secondary shadow-sm">
            <i class="fas fa-arrow-left fa-sm text-white-50"></i> Back to List
        </a>
    </div>

    <div class="row">
        <!-- Customer & SO Info -->
        <div class="col-xl-8 col-lg-7">
            <div class="card shadow mb-4">
                <div class="card-header py-3 d-flex flex-row align-items-center justify-content-between">
                    <h6 class="m-0 font-weight-bold text-primary">
                        <i class="fas fa-file-invoice me-2"></i> Sale Order #${soHeader.soNumber}
                    </h6>
                    <div class="dropdown no-arrow">
                        <span class="badge badge-light text-dark" style="font-size: 0.9rem;">
                            SO ID: ${soId}
                        </span>
                    </div>
                </div>
                <div class="card-body">
                    <div class="row mb-3">
                        <div class="col-md-6">
                            <h5 class="small font-weight-bold text-secondary text-uppercase mb-1">Customer</h5>
                            <div class="h5 mb-1 font-weight-bold text-gray-800">${soHeader.customerName}</div>
                            <p class="mb-0 text-gray-600">
                                <i class="fas fa-map-marker-alt fa-fw me-1"></i> ${soHeader.customerAddress}
                            </p>
                            <p class="mb-0 text-gray-600">
                                <i class="fas fa-phone fa-fw me-1"></i> ${soHeader.customerPhone}
                            </p>
                            <p class="mb-0 text-gray-600">
                                <i class="fas fa-envelope fa-fw me-1"></i> ${soHeader.customerEmail}
                            </p>
                        </div>
                        <div class="col-md-6 border-left-secondary">
                            <h5 class="small font-weight-bold text-secondary text-uppercase mb-1">Shipment</h5>
                            <div class="mb-2">
                                <span class="text-gray-600">Ship to address:</span><br>
                                <span class="font-weight-bold text-gray-800">
                                    ${soHeader.shipToAddress}
                                </span>
                            </div>
                            <div class="mb-2">
                                <span class="text-gray-600">Imported by:</span><br>
                                <span class="font-weight-bold text-gray-800">
                                    <c:out value="${soHeader.importedByUsername}" default="N/A" />
                                </span>
                            </div>
                            <div>
                                <span class="text-gray-600">Imported at:</span><br>
                                <span class="font-italic text-gray-700">
                                    <c:choose>
                                        <c:when test="${soHeader.importedAt != null}">
                                            ${soHeader.importedAt}
                                        </c:when>
                                        <c:otherwise>Not available</c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Status -->
        <div class="col-xl-4 col-lg-5">
            <div class="card shadow mb-4 border-left-info">
                <div class="card-header py-3">
                    <h6 class="m-0 font-weight-bold text-info">Current Status</h6>
                </div>
                <div class="card-body text-center">
                    <div class="h2 mb-3 font-weight-bold text-gray-800">
                        ${soHeader.status}
                    </div>
                    <p class="text-muted mb-0 small">
                        Review sale order lines before processing outbound.
                    </p>
                </div>
            </div>
        </div>
    </div>

    <!-- Lines table -->
    <div class="card shadow mb-4">
        <div class="card-header py-3">
            <h6 class="m-0 font-weight-bold text-primary">
                Items List (${fn:length(lines)})
            </h6>
        </div>
        <div class="card-body">
            <div class="table-responsive">
                <table class="table table-bordered table-hover" width="100%" cellspacing="0">
                    <thead class="thead-light">
                        <tr>
                            <th style="width: 5%">#</th>
                            <th style="width: 30%">Product</th>
                            <th style="width: 15%">Variant</th>
                            <th style="width: 15%">Barcode</th>
                            <th style="width: 10%" class="text-center">Quantity</th>
                            <th style="width: 10%" class="text-end">Unit Price</th>
                            <!--<th style="width: 15%" class="text-end">Discount</th>-->
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty lines}">
                                <tr>
                                    <td colspan="7" class="text-center py-4 text-muted">
                                        No items found in this Sale Order.
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach items="${lines}" var="l" varStatus="st">
                                    <tr>
                                        <td>${st.index + 1}</td>
                                        <td>
                                            <div class="font-weight-bold text-primary">${l.productName}</div>
                                            <div class="small text-gray-500">SKU: ${l.variantSku}</div>
                                        </td>
                                        <td>
                                            <div>
                                                <span class="text-xs font-weight-bold text-uppercase text-secondary">Color:</span>
                                                ${l.color}
                                            </div>
                                            <div>
                                                <span class="text-xs font-weight-bold text-uppercase text-secondary">Size:</span>
                                                ${l.size}
                                            </div>
                                        </td>
                                        <td class="text-monospace small">${l.barcode}</td>
                                        <td class="text-center font-weight-bold">
                                            <fmt:formatNumber value="${l.orderedQty}" maxFractionDigits="0"/>
                                        </td>
                                        <td class="text-end">
                                            <fmt:formatNumber value="${l.unitPrice}" type="number"
                                                              minFractionDigits="0" maxFractionDigits="0"/>
                                        </td>
<!--                                        <td class="text-end">
                                            <fmt:formatNumber value="${l.discount}" type="number"
                                                              minFractionDigits="0" maxFractionDigits="0"/>
                                        </td>-->
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

</t:layout>

